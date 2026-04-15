# 微服务架构 Service Call 总结

## (A) 同步调用：gRPC Blocking Stub

所有 gRPC 同步调用都发生在 booking 服务的 `CreateBookingCommandHandler` 中，通过 `FlightServiceBlockingStub` 和 `PassengerServiceBlockingStub` 调用 flight 和 passenger 服务。

| # | 调用方 | 被调用方 | 方法 | 业务目的 | 源文件位置 | 单体中的替代方式 |
|---|--------|---------|------|---------|-----------|----------------|
| 1 | booking-service | flight-service | `FlightServiceBlockingStub.getById()` | 获取航班信息，验证航班存在 | `CreateBookingCommandHandler.java:49` | `mediator.send(new GetFlightByIdQuery(flightId))` → 返回 `FlightDto` |
| 2 | booking-service | passenger-service | `PassengerServiceBlockingStub.getById()` | 获取乘客信息，验证乘客存在 | `CreateBookingCommandHandler.java:56` | `mediator.send(new GetPassengerByIdQuery(passengerId))` → 返回 `PassengerDto` |
| 3 | booking-service | flight-service | `FlightServiceBlockingStub.getAvailableSeats()` | 获取可用座位列表 | `CreateBookingCommandHandler.java:62` | `mediator.send(new GetAvailableSeatsQuery(flightId))` → 返回 `List<SeatDto>` |
| 4 | booking-service | flight-service | `FlightServiceBlockingStub.reserveSeat()` | 预订选中的座位 | `CreateBookingCommandHandler.java:79` | `mediator.send(new ReserveSeatCommand(seatNumber, flightId))` → 返回 `SeatDto` |

### gRPC 服务端实现

| 服务 | gRPC 实现类 | 暴露的 RPC 方法 | 内部委托给 |
|------|-----------|----------------|----------|
| flight-service | `FlightServiceGrpcImpl` | `GetById` | `mediator.send(new GetFlightByIdQuery(...))` |
| flight-service | `FlightServiceGrpcImpl` | `GetAvailableSeats` | `mediator.send(new GetAvailableSeatsQuery(...))` |
| flight-service | `FlightServiceGrpcImpl` | `ReserveSeat` | `mediator.send(new ReserveSeatCommand(...))` |
| passenger-service | `PassengerServiceGrpcImpl` | `GetById` | `mediator.send(new GetPassengerByIdQuery(...))` |

### gRPC 客户端配置

| 配置类 | 创建的 Stub | 连接目标 |
|--------|-----------|---------|
| `GrpcClientsConfiguration` | `FlightServiceBlockingStub` | `localhost:9092` (flight gRPC) |
| `GrpcClientsConfiguration` | `PassengerServiceBlockingStub` | `localhost:9093` (passenger gRPC) |

---

## (B) 异步调用：RabbitMQ + Outbox Pattern

每个服务通过 fanout exchange (`booking-microservices`) 发布事件。完整事件流转链路：

```
Command Handler → 持久化聚合根 → Domain Event
  → EventDispatcherImpl → EventMapper.MapToIntegrationEvent()
    → PersistMessageProcessor.publishMessage() → 存入 persist_messages 表 (Outbox)
      → PersistMessageBackgroundJob (定时 1s) → RabbitTemplate.convertSendAndReceive()
        → RabbitMQ fanout exchange → 消费方队列
          → RabbitmqConfiguration.addListeners() → MessageHandler<T>.onMessage()
```

### 确认存在的跨服务事件流转

| # | 事件名 | 生产方 | 消费方 | 触发场景 | 消费方处理逻辑 | 单体中的替代方式 |
|---|--------|-------|--------|---------|-------------|----------------|
| 1 | `FlightUpdated` | flight-service | booking-service | `PUT /api/v1/flight/{id}` → `UpdateFlightCommandHandler` → `Flight.update()` → `FlightUpdatedDomainEvent` | `FlightUpdatedListener`: 查找所有关联该 flightId 的 booking，更新每个 booking 的 Trip 信息 | `ApplicationEventPublisher.publishEvent()` → `@EventListener` |
| 2 | `FlightDeleted` | flight-service | booking-service | `DELETE /api/v1/flight/{id}` → `DeleteFlightCommandHandler` → `Flight.delete()` → `FlightDeletedDomainEvent` | `FlightDeletedListener`: 查找所有关联该 flightId 的 booking，标记 `isDeleted = true` | `ApplicationEventPublisher.publishEvent()` → `@EventListener` |

### 已发布但无消费方的事件（仅写入 Outbox）

| # | 事件名 | 生产方 | 触发场景 | 备注 |
|---|--------|-------|---------|------|
| 3 | `FlightCreated` | flight-service | `POST /api/v1/flight` → `CreateFlightCommandHandler` | 无消费方监听此事件 |
| 4 | `AircraftCreated` | flight-service | `POST /api/v1/flight/aircraft` → `CreateAircraftCommandHandler` | 无消费方监听此事件 |
| 5 | `AirportCreated` | flight-service | `POST /api/v1/flight/airport` → `CreateAirportCommandHandler` | 无消费方监听此事件 |
| 6 | `SeatCreated` | flight-service | `POST /api/v1/flight/seat` → `CrateSeatCommandHandler` | 无消费方监听此事件 |
| 7 | `SeatReserved` | flight-service | `POST /api/v1/flight/reserve-seat` → `ReserveSeatCommandHandler` | 无消费方监听此事件 |
| 8 | `PassengerCreated` | passenger-service | `POST /api/v1/passenger` → `CreatePassengerCommandHandler` | 无消费方监听此事件 |
| 9 | `BookingCreated` | booking-service | `POST /api/v1/booking` → `CreateBookingCommandHandler` | 无消费方监听此事件 |

---

## (C) API Gateway 路由（HTTP 转发）

| 路由名 | 匹配路径 | 目标服务 | 过滤器 |
|--------|---------|---------|--------|
| `flight-service` | `/api/{version}/flight/**` | `http://localhost:8082` | `TokenRelay` |
| `passenger-service` | `/api/{version}/passenger/**` | `http://localhost:8083` | `TokenRelay` |
| `booking-service` | `/api/{version}/booking/**` | `http://localhost:8084` | `TokenRelay` |

**单体中处理方式**: API Gateway 整体移除，所有路由由单体应用自身的 Controller 直接处理（端口 8080）。

---

## 总计

| 调用类型 | 数量 | 单体中的替代 |
|---------|------|------------|
| gRPC 同步调用 | 4 个 | Mediator 进程内调用 |
| RabbitMQ 异步事件（有消费方） | 2 个 | ApplicationEventPublisher + @EventListener |
| RabbitMQ 异步事件（无消费方） | 7 个 | ApplicationEventPublisher（保留发布，无监听方） |
| API Gateway 路由 | 3 个 | 移除，Controller 直接处理 |
