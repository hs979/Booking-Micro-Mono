package io.bookingmicroservices.passenger.passengers.features.getpassengerbyid;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmicroservices.passenger.data.jpa.entities.PassengerEntity;
import io.bookingmicroservices.passenger.data.jpa.repositories.PassengerRepository;
import io.bookingmicroservices.passenger.passengers.dtos.PassengerDto;
import io.bookingmicroservices.passenger.passengers.exceptions.PassengerNotFoundException;
import io.bookingmicroservices.passenger.passengers.features.Mappings;
import org.springframework.stereotype.Service;

@Service
public class GetPassengerByIdQueryHandler implements IQueryHandler<GetPassengerByIdQuery, PassengerDto> {
    private final PassengerRepository passengerRepository;

    public GetPassengerByIdQueryHandler(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public PassengerDto handle(GetPassengerByIdQuery query) {
        PassengerEntity passenger = passengerRepository.findPassengerByIdAndIsDeletedFalse(query.id());

        if (passenger == null) {
            throw new PassengerNotFoundException();
        }

        return Mappings.toPassengerDto(passenger);
    }
}
