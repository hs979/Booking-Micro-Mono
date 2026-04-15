package io.bookingmonolith.passenger.passengers.features.getpassengerbyid;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmonolith.passenger.data.jpa.entities.PassengerEntity;
import io.bookingmonolith.passenger.data.jpa.repositories.PassengerRepository;
import io.bookingmonolith.passenger.passengers.dtos.PassengerDto;
import io.bookingmonolith.passenger.passengers.exceptions.PassengerNotFoundException;
import io.bookingmonolith.passenger.passengers.features.Mappings;
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
