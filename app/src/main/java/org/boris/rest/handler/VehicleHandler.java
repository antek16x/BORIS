package org.boris.rest.handler;

import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.boris.core_api.*;
import org.boris.core_api.Coordinate;
import org.boris.query.entity.BorderCrossing;
import org.boris.rest.*;
import org.boris.rest.exceptions.InvalidBodyException;
import org.boris.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class VehicleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleHandler.class);

    private final ReactorCommandGateway commandGateway;
    private final VehicleService vehicleService;

    public VehicleHandler(ReactorCommandGateway commandGateway, VehicleService vehicleService) {
        this.commandGateway = commandGateway;
        this.vehicleService = vehicleService;
    }

    @NotNull
    public Mono<ServerResponse> addNewVehicle(ServerRequest request) {
        return request
                .bodyToMono(AddNewVehicleDTO.class)
                .onErrorMap(throwable -> new InvalidBodyException(throwable.getMessage()))
                .flatMap(dto -> {
                    var vehicleExists = vehicleService.checkIfVehicleExists(dto.getVehicleReg());
                    if (vehicleExists) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue("Vehicle already exists");
                    } else {
                        return commandGateway.<VehicleId>send(
                                new AddNewVehicleCommand(
                                        new VehicleId(dto.getVehicleReg().toUpperCase()),
                                        dto.getTelematicsEnabled(),
                                        dto.getInitialCountry()
                                )
                        ).flatMap(id -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(id.getIdentifier()));
                    }
                });
    }

    @NotNull
    public Mono<ServerResponse> updateVehicleTelematics(ServerRequest request) {
        var vehicleId = new VehicleId(request.pathVariable("vehicleReg"));

        return request
                .bodyToMono(UpdateVehicleTelematicsDTO.class)
                .onErrorMap(throwable -> new InvalidBodyException(throwable.getMessage()))
                .flatMap(dto -> {
                    return commandGateway.<VehicleId>send(
                            new UpdateVehicleTelematicsCommand(
                                    vehicleId,
                                    dto.getEnabled()
                            )
                    );
                }).flatMap(resp -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(String.format("Successful change telematics status for vehicle %s", vehicleId.getIdentifier()))
                )
                .onErrorResume(this::onError);
    }

    public Mono<ServerResponse> updateVehiclePosition(ServerRequest request) {
        var vehicleId = new VehicleId(request.pathVariable("vehicleReg"));
        var dtoMono = request.bodyToMono(UpdateVehiclePositionDTO.class);

        return dtoMono
                .flatMapMany(dto -> Flux.fromIterable(dto.getPositions()))
                .flatMap(position -> {
                    var command = new UpdateVehiclePositionCommand(
                            vehicleId,
                            true,
                            convertDTOToValueObject(position.getCoordinate()),
                            position.getCountry(),
                            position.getTimestamp()
                    );
                    return commandGateway.send(command).thenReturn(position);
                })
                .collectList()
                .flatMap(positions -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(positions))
                .onErrorResume(this::onError);
    }

    public Mono<ServerResponse> generateBorderCrossingReport(ServerRequest request) {
        var vehicleId = request.pathVariable("vehicleReg");
        var startingDateStr = request.queryParam("startingDate").orElse("");
        var endDateStr = request.queryParam("endDate").orElse("");

        try {
            var startingDate = Instant.parse(startingDateStr);
            var endDate = Instant.parse(endDateStr);

            var borderCrossingReport = vehicleService.generateReport(
                    vehicleId,
                    startingDate,
                    endDate
            );

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(borderCrossingReport);

        } catch (DateTimeParseException e) {

            return ServerResponse.badRequest()
                    .bodyValue("Invalid date format");
        }
    }

    private Coordinate convertDTOToValueObject(org.boris.rest.Coordinate coordinate) {
        return new Coordinate(
                coordinate.getLongitude(),
                coordinate.getLatitude()
        );
    }

    private Mono<ServerResponse> onError(Throwable throwable) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(throwable.getLocalizedMessage());
        }
        if (throwable.getMessage().contains("The aggregate was not found in the event store")) {
            return ServerResponse
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue("The vehicle does not exists");
        }
        if (throwable.getMessage().contains("Can't change telematics status it's already")) {
            return ServerResponse
                    .status(HttpStatus.CONFLICT)
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue("Cannot change telmetics status to the same");
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}