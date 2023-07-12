package org.boris.rest.handler;

import io.grpc.Server;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.boris.core_api.UpdateVehicleTelematicsCommand;
import org.boris.rest.AddNewVehicleDTO;
import org.boris.rest.UpdateVehicleTelematicsDTO;
import org.boris.rest.exceptions.InvalidBodyException;
import org.boris.core_api.AddNewVehicleCommand;
import org.boris.core_api.VehicleId;
import org.boris.rest.exceptions.VehicleNotFoundException;
import org.boris.services.VehicleService;
import org.boris.vehicle.InvalidTelematicsUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;

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
        LOGGER.debug("hello from app");
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
                                        dto.getTelematicsEnabled()
                                )
                        ).flatMap(id -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(String.format("Successful change telematics status for vehicle %s", vehicleId.getIdentifier()))
                )
                .onErrorResume(this::onError);
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