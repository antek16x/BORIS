package org.boris.vehicle;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateScopeDescriptor;
import org.axonframework.modelling.command.CommandHandlerInterceptor;
import org.axonframework.spring.stereotype.Aggregate;
import org.boris.core_api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Vehicle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vehicle.class);

    private static final String GET_VEHICLE_POSITION_DEADLINE = "getVehiclePositionDeadline";
    private static final String CONFIRM_BORDER_CROSSING_DEADLINE = "confirmBorderCrossingDeadline";

    @AggregateIdentifier
    private VehicleId vehicleReg;

    private Boolean telematics;
    private Coordinate lastKnownCoordinate;
    private String lastKnownCountry;
    private Instant lastKnownTimestamp;
    private String countryOut;
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
                scheduleDeadline(deadlineManager, command.getVehicleReg(), GET_VEHICLE_POSITION_DEADLINE);
            }
        });
    }

    @CommandHandlerInterceptor
    public void intercept(UpdateVehicleTelematicsCommand command, InterceptorChain interceptorChain) throws Exception {
        if (telematics.equals(command.getTelematicsEnabled())) {
            throw new InvalidTelematicsUpdateException("Can't change telematics status it's already " + command.getTelematicsEnabled());
        } else {
            interceptorChain.proceed();
        }
    }

    @CommandHandler
    public void on(UpdateVehicleTelematicsCommand command, DeadlineManager deadlineManager) {
        apply(new VehicleTelematicsUpdatedEvent(
                command.getVehicleReg(),
                command.getTelematicsEnabled()
        )).andThen(() -> {
            if (this.telematics) {
                scheduleDeadline(deadlineManager, command.getVehicleReg(), GET_VEHICLE_POSITION_DEADLINE);
            }
        });
    }

    @CommandHandler
    public void on(UpdateVehiclePositionCommand command, DeadlineManager deadlineManager) {
        if (command.isRunManually()) {
            //miejsce na odpytanie serwisu
        }
        else if (command.isUpdateManually()) {
            if (!Objects.equals(command.getCountry(), lastKnownCountry)) {
                apply(new CrossingBorderConfirmedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getTimestamp()),
                        this.lastKnownCountry,
                        Objects.requireNonNull(command.getCountry())
                )).andThenApply(() -> new LastVehiclePositionUpdatedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getCoordinate()),
                        command.getCountry(),
                        command.getTimestamp()
                ));
            } else {
                apply(new LastVehiclePositionUpdatedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getCoordinate()),
                        Objects.requireNonNull(command.getCountry()),
                        Objects.requireNonNull(command.getTimestamp())
                ));
            }
        }
        else {
            if (!Objects.equals(command.getCountry(), lastKnownCountry)) {
                apply(new VehicleCrossedBorderEvent(
                        command.getVehicleReg(),
                        this.lastKnownCountry,
                        Objects.requireNonNull(command.getTimestamp())
                )).andThenApply(() -> new LastVehiclePositionUpdatedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getCoordinate()),
                        Objects.requireNonNull(command.getCountry()),
                        command.getTimestamp()
                )).andThen(() -> scheduleDeadline(deadlineManager, command.getVehicleReg(), CONFIRM_BORDER_CROSSING_DEADLINE));
            } else {
                apply(new LastVehiclePositionUpdatedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getCoordinate()),
                        Objects.requireNonNull(command.getCountry()),
                        Objects.requireNonNull(command.getTimestamp())
                ));
            }
        }
    }

    @EventSourcingHandler
    public void on(NewVehicleAddedEvent event) {
        this.vehicleReg = event.getVehicleReg();
        this.telematics = event.getTelematicsEnabled();
        this.lastKnownCoordinate = null;
        this.lastKnownCountry = null;
        this.lastKnownTimestamp = null;
        this.countryOut = null;
        this.crossingBorderTimestamp = null;
    }

    @EventSourcingHandler
    public void on(VehicleTelematicsUpdatedEvent event) {
        this.telematics = event.getTelematicsEnabled();
    }

    @EventSourcingHandler
    public void on(VehicleCrossedBorderEvent event) {
        this.countryOut = event.getCountryBeforeCrossing();
        this.crossingBorderTimestamp = event.getCrossingTimestamp();
    }

    @EventSourcingHandler
    public void on(LastVehiclePositionUpdatedEvent event) {
        this.lastKnownCountry = event.getCountry();
        this.lastKnownCoordinate = event.getCoordinate();
        this.lastKnownTimestamp = event.getTimestamp();
    }

    @DeadlineHandler(deadlineName = GET_VEHICLE_POSITION_DEADLINE)
    public void on() {
        LOGGER.info("Hello from deadline handler");
    }


    private void scheduleDeadline(DeadlineManager deadlineManager, VehicleId vehicleReg, String deadlineName) {
        deadlineManager.schedule(
                Duration.ofMinutes(5),
                deadlineName,
                vehicleReg.toString(),
                new AggregateScopeDescriptor("org.boris.vehicle.Vehicle", vehicleReg.toString())
        );
    }
}
