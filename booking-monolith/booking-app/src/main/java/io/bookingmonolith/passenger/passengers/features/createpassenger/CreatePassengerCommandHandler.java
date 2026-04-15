package io.bookingmonolith.passenger.passengers.features.createpassenger;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.passenger.data.jpa.entities.PassengerEntity;
import io.bookingmonolith.passenger.data.jpa.repositories.PassengerRepository;
import io.bookingmonolith.passenger.passengers.dtos.PassengerDto;
import io.bookingmonolith.passenger.passengers.exceptions.PassengerAlreadyExistException;
import io.bookingmonolith.passenger.passengers.features.Mappings;
import io.bookingmonolith.passenger.passengers.models.Passenger;
import io.bookingmonolith.passenger.passengers.valueobjects.Age;
import io.bookingmonolith.passenger.passengers.valueobjects.Name;
import io.bookingmonolith.passenger.passengers.valueobjects.PassengerId;
import io.bookingmonolith.passenger.passengers.valueobjects.PassportNumber;
import org.springframework.stereotype.Service;

@Service
public class CreatePassengerCommandHandler implements ICommandHandler<CreatePassengerCommand, PassengerDto> {
    private final PassengerRepository passengerRepository;

    public CreatePassengerCommandHandler(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public PassengerDto handle(CreatePassengerCommand command) {

        PassengerEntity existPassenger = passengerRepository.findPassengerByPassportNumber_PassportNumberAndIsDeletedFalse(command.passportNumber());
        if (existPassenger != null) {
         throw new PassengerAlreadyExistException();
        }

        Passenger passengerAggregate = Passenger.create(
                new PassengerId(command.id()),
                new Name(command.name()),
                new PassportNumber(command.passportNumber()),
                command.passengerType(),
                new Age(command.age())
        );

        PassengerEntity passengerEntity = Mappings.toPassengerEntity(passengerAggregate);

        PassengerEntity createdPassenger = passengerRepository.save(passengerEntity);

        return Mappings.toPassengerDto(createdPassenger);
    }
}
