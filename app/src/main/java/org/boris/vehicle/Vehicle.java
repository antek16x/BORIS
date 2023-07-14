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
import org.boris.services.VehiclePositionService;
import org.boris.validation.VehicleValidator;
import org.boris.vehicle.exceptions.InvalidTelematicsUpdateException;
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
    private String lastKnownCountry;
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
                Optional.ofNullable(command.getTelematicsEnabled()).orElse(false),
                command.getInitialCountry()
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
    public void on(UpdateVehiclePositionCommand command, VehiclePositionService service, DeadlineManager deadlineManager) {
        if (command.isUpdateManually()) {
            LOGGER.info("Manually update position of vehicle with registration plate [{}]", command.getVehicleReg());
            if (!Objects.equals(command.getCountry(), lastKnownCountry)) {
                LOGGER.info("Vehicle with registration plate [{}] crossed the border, confirmation not required", command.getVehicleReg());
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
                LOGGER.info("Vehicle has not crossed the border");
                apply(new LastVehiclePositionUpdatedEvent(
                        command.getVehicleReg(),
                        Objects.requireNonNull(command.getCoordinate()),
                        Objects.requireNonNull(command.getCountry()),
                        Objects.requireNonNull(command.getTimestamp())
                ));
            }
        } else {
            cancelGetVehiclePositionDeadline(deadlineManager);
            processServiceResponse(service, deadlineManager);
        }
    }

    @EventSourcingHandler
    public void on(NewVehicleAddedEvent event) {
        this.vehicleReg = event.getVehicleReg();
        this.telematics = event.getTelematicsEnabled();
        this.lastKnownCountry = event.getInitialCountry();
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
    }

    @EventSourcingHandler
    public void on(CrossingBorderConfirmedEvent event) {
        this.countryOut = null;
        this.crossingBorderTimestamp = null;
    }

    @DeadlineHandler(deadlineName = GET_VEHICLE_POSITION_DEADLINE)
    public void on(String payload, VehiclePositionService service, DeadlineManager deadlineManager) {
        LOGGER.info("Deadline for getting vehicle position passed");
        processServiceResponse(service, deadlineManager);
    }

    @DeadlineHandler(deadlineName = CONFIRM_BORDER_CROSSING_DEADLINE)
    public void onConfirmationDeadline(String payload, VehiclePositionService service, DeadlineManager deadlineManager) {
        LOGGER.info("Deadline for crossing border confirmation passed, trying to confirm");
        var serviceResponse = service.getVehiclePosition(this.vehicleReg.getIdentifier());
        serviceResponse.subscribe(positions -> {
            var position = positions.get(0);
            position.setCountry(getCountryCodeIfInvalid(position.getCountry(), position.getCoordinate()));
            if (!position.getCountry().equals(countryOut)) {
                LOGGER.info("Crossing border for vehicle [{}] has been confirmed", this.vehicleReg.getIdentifier());
                apply(new CrossingBorderConfirmedEvent(
                        this.vehicleReg,
                        this.crossingBorderTimestamp,
                        this.countryOut,
                        position.getCountry()
                )).andThenApply(() -> new LastVehiclePositionUpdatedEvent(
                        this.vehicleReg,
                        position.getCoordinate(),
                        position.getCountry(),
                        position.getTimestamp()
                ));
            } else {
                LOGGER.info("Crossing border for vehicle [{}] confirmed failed", this.vehicleReg.getIdentifier());
                apply(new LastVehiclePositionUpdatedEvent(
                        this.vehicleReg,
                        position.getCoordinate(),
                        position.getCountry(),
                        position.getTimestamp()
                ));
            }
        });
        if (this.telematics) {
            scheduleDeadline(deadlineManager, this.vehicleReg, GET_VEHICLE_POSITION_DEADLINE);
        }
    }


    private void scheduleDeadline(DeadlineManager deadlineManager, VehicleId vehicleReg, String deadlineName) {
        deadlineManager.schedule(
                Duration.ofMinutes(1),
                deadlineName,
                vehicleReg.toString(),
                new AggregateScopeDescriptor("Vehicle", vehicleReg.toString())
        );
    }

    private void cancelGetVehiclePositionDeadline(DeadlineManager deadlineManager) {
        LOGGER.info("Get vehicle position deadline for aggregate [{}] cancelled", this.vehicleReg.getIdentifier());
        deadlineManager.cancelAllWithinScope(GET_VEHICLE_POSITION_DEADLINE,
                new AggregateScopeDescriptor("Vehicle", this.vehicleReg.toString()));
    }

    private void processServiceResponse(VehiclePositionService service, DeadlineManager deadlineManager) {
        LOGGER.info("Update position of vehicle with registration plate [{}] by service", this.vehicleReg);
        var serviceResponse = service.getVehiclePosition(this.vehicleReg.getIdentifier());
        serviceResponse.subscribe(positions -> {
            var position = positions.get(0);
            position.setCountry(getCountryCodeIfInvalid(position.getCountry(), position.getCoordinate()));
            if (!Objects.equals(position.getCountry(), lastKnownCountry)) {
                LOGGER.info("Vehicle with registration plate [{}] crossed the border, confirmation required", this.vehicleReg);
                apply(new VehicleCrossedBorderEvent(
                        this.vehicleReg,
                        this.lastKnownCountry,
                        position.getTimestamp()
                )).andThenApply(() -> new LastVehiclePositionUpdatedEvent(
                        this.vehicleReg,
                        position.getCoordinate(),
                        position.getCountry(),
                        position.getTimestamp()
                )).andThen(() -> scheduleDeadline(deadlineManager, this.vehicleReg, CONFIRM_BORDER_CROSSING_DEADLINE));
            } else {
                LOGGER.info("Vehicle has not crossed the border");
                apply(new LastVehiclePositionUpdatedEvent(
                        this.vehicleReg,
                        position.getCoordinate(),
                        position.getCountry(),
                        position.getTimestamp()
                )).andThen(() -> {
                    if (this.telematics) {
                        scheduleDeadline(deadlineManager, this.vehicleReg, GET_VEHICLE_POSITION_DEADLINE);
                    }
                });
            }
        });
    }

    private String getCountryCodeIfInvalid(String countryCode, Coordinate coordinate) {
        if (VehicleValidator.isValidCountryCode(countryCode)) {
            return countryCode;
        } else {
            return VehicleValidator.getCountryCodeFromCoordinates(coordinate.getLongitude(), coordinate.getLatitude());
        }
    }
}
