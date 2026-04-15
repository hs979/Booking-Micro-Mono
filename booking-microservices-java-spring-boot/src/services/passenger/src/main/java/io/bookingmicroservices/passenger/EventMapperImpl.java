package io.bookingmicroservices.passenger;

import buildingblocks.contracts.passenger.PassengerCreated;
import buildingblocks.core.event.DomainEvent;
import buildingblocks.core.event.EventMapper;
import buildingblocks.core.event.IntegrationEvent;
import buildingblocks.core.event.InternalCommand;
import io.bookingmicroservices.passenger.passengers.features.createpassenger.PassengerCreatedDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {
    @Override
    public IntegrationEvent MapToIntegrationEvent(DomainEvent event) {
        if (event instanceof PassengerCreatedDomainEvent e) {
            return new PassengerCreated(e.id());
        }
        return null;
    }

    @Override
    public InternalCommand MapToInternalCommand(DomainEvent event) {
        return null;
    }
}
