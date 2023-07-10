package org.boris.rest.handler;

import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.boris.rest.AddNewVehicleDTO;
import org.boris.rest.exceptions.InvalidBodyException;
import org.boris.core_api.AddNewVehicleCommand;
import org.boris.core_api.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class VehicleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleHandler.class);

    private final ReactorCommandGateway commandGateway;

    public VehicleHandler(ReactorCommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @NotNull
    public Mono<ServerResponse> addNewVehicle(ServerRequest request) {
        return request
                .bodyToMono(AddNewVehicleDTO.class)
                .onErrorMap(throwable -> new InvalidBodyException(throwable.getMessage()))
                .flatMap(dto -> {
                    return commandGateway.<VehicleId>send(
                            new AddNewVehicleCommand(
                                    new VehicleId(dto.getVehicleReg()),
                                    dto.getTelematicsEnabled()
                            )
                    );
                }).flatMap(id -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(id.getIdentifier()));
    }
}