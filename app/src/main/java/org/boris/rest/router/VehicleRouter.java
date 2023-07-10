package org.boris.rest.router;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.boris.rest.AddNewVehicleDTO;
import org.boris.rest.handler.VehicleHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Vehicle API", version = "v1"))
public class VehicleRouter {

    @Bean
    public VehicleHandler vehicleHandler(ReactorCommandGateway commandGateway) {
        return new VehicleHandler(commandGateway);
    }

    public static final String VEHICLE_URL = "/api/v1/vehicle";
//    public static final String VEHICLE_POSITION_URL = VEHICLE_URL + "/position";

    @Bean
    @RouterOperation(
            path = VEHICLE_URL,
            operation = @Operation(
                    operationId = "postNewVehicle",
                    tags = {"Vehicle"},
                    summary = "Add new vehicle",
                    requestBody = @RequestBody(
                            content = @Content(schema = @Schema(implementation = AddNewVehicleDTO.class))),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Vehicle accepted",
                                    content = @Content(schema = @Schema(implementation = String.class))
                            )
                    }))
    public RouterFunction<ServerResponse> routePostVehicle(VehicleHandler handler) {
        return route(
                POST(VEHICLE_URL)
                        .and(contentType(MediaType.APPLICATION_JSON))
                        .and(accept(MediaType.APPLICATION_JSON)),
                handler::addNewVehicle
        );
    }
}
