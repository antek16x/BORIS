package org.boris.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class VehicleRouter {

    public static final String VEHICLE_URL = "/api/v1/vehicle";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = VEHICLE_URL,
                    operation = @Operation(
                            operationId = "addNewVehicle",
                            tags = {"Vehicle"},
                            summary = "Add new vehicle",
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(implementation = AddNewVehicleDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Vehicle accepted",
                                            content = @Content(schema = @Schema(implementation = String.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "New vehicle added failed"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routePostVehicle(VehicleHandler handler) {
        return route(POST(VEHICLE_URL).and(accept(MediaType.APPLICATION_JSON)),
                handler::addNewVehicle);
    }
}
