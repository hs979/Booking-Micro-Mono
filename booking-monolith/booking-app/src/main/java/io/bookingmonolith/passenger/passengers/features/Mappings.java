package io.bookingmonolith.passenger.passengers.features;

import com.github.f4b6a3.uuid.UuidCreator;
import io.bookingmonolith.passenger.data.jpa.entities.PassengerEntity;
import io.bookingmonolith.passenger.passengers.dtos.PassengerDto;
import io.bookingmonolith.passenger.passengers.features.createpassenger.CreatePassengerCommand;
import io.bookingmonolith.passenger.passengers.features.createpassenger.CreatePassengerRequestDto;
import io.bookingmonolith.passenger.passengers.models.Passenger;

public final class Mappings {

    public static PassengerEntity toPassengerEntity(Passenger passenger) {
        return new PassengerEntity(
                passenger.getId().getPassengerId(),
                passenger.getName(),
                passenger.getPassportNumber(),
                passenger.getPassengerType(),
                passenger.getAge(),
                passenger.getCreatedAt(),
                passenger.getCreatedBy(),
                passenger.getLastModified(),
                passenger.getLastModifiedBy(),
                passenger.getVersion(),
                passenger.isDeleted()
        );
    }

    public static PassengerDto toPassengerDto(PassengerEntity passengerEntity) {
        return new PassengerDto(
                passengerEntity.getId(),
                passengerEntity.getName().getName(),
                passengerEntity.getPassportNumber().getPassportNumber(),
                passengerEntity.getPassengerType(),
                passengerEntity.getAge().getAge());
    }


    public static CreatePassengerCommand toCreatePassengerCommand(CreatePassengerRequestDto passengerRequestDto) {
        return new CreatePassengerCommand(
                UuidCreator.getTimeOrderedEpoch(),
                passengerRequestDto.name(),
                passengerRequestDto.PassportNumber(),
                passengerRequestDto.passengerType(),
                passengerRequestDto.age()
        );
    }


}