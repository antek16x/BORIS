package org.boris.vehicle;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateScopeDescriptor;
import org.axonframework.spring.stereotype.Aggregate;
import org.boris.core_api.AddNewVehicleCommand;
import org.boris.core_api.Coordinate;
import org.boris.core_api.NewVehicleAddedEvent;
import org.boris.core_api.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Vehicle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vehicle.class);

    private static final String DEADLINE_NAME = "getVehiclePositionDeadline";

    @AggregateIdentifier
    private VehicleId vehicleReg;

    private Boolean telematics;
    private Coordinate lastKnownCoordinate;
    private String lastKnownCountry;
    private String countryToConfirm;
    private Instant crossingBorderTimestamp;


    public Vehicle() {
        // Required by Axon Framework
    }

    @CommandHandler
    public Vehicle(AddNewVehicleCommand command, DeadlineManager deadlineManager) {
        LOGGER.info("Successfully added vehicle with registration number [{}]", command.getVehicleReg().getIdentifier());
        apply(new NewVehicleAddedEvent(
                command.getVehicleReg(),
                Optional.ofNullable(command.getTelematicsEnabled()).orElse(false)
        )).andThen(() -> {
            if (this.telematics) {
                scheduleDeadline(deadlineManager, command.getVehicleReg());
            }
        });
    }

    @EventSourcingHandler
    public void on(NewVehicleAddedEvent event) {
        this.vehicleReg = event.getVehicleReg();
        this.telematics = event.getTelematicsEnabled();
        this.lastKnownCoordinate = null;
        this.lastKnownCountry = null;
        this.countryToConfirm = null;
        this.crossingBorderTimestamp = null;
    }

    private void scheduleDeadline(DeadlineManager deadlineManager, VehicleId vehicleReg) {
        deadlineManager.schedule(
                Duration.ofMinutes(5),
                DEADLINE_NAME,
                vehicleReg.toString(),
                new AggregateScopeDescriptor("org.boris.vehicle.Vehicle", vehicleReg.toString())
        );
    }
}
