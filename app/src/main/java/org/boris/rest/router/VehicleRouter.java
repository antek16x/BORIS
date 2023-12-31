package org.boris.rest.router;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.boris.rest.*;
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
@SuppressWarnings("unused")
public class VehicleRouter {

    public static final String VEHICLE_URL = "/api/v1/vehicle";
    public static final String VEHICLE_TELEMETICS_SWITCH = VEHICLE_URL + "/{vehicleReg}/telematics";
    public static final String VEHICLE_POSITION_UPDATE_MANUALLY = VEHICLE_URL + "/{vehicleReg}/update-manually";
    public static final String VEHICLE_GET_POSITION = VEHICLE_URL + "/{vehicleReg}/get-position";
    public static final String VEHICLE_BORDER_CROSSING_REPORT = VEHICLE_URL + "/generate-report";

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
                                    description = "Vehicle created",
                                    content = @Content(schema = @Schema(implementation = String.class))
                            )
                    }))
    public RouterFunction<ServerResponse> routePostVehicle(VehicleHandler handler) {
        return route(
                POST(VEHICLE_URL)
                        .and(contentType(MediaType.APPLICATION_JSON))
                        .and(accept(MediaType.TEXT_PLAIN)),
                handler::addNewVehicle
        );
    }

    @Bean
    @RouterOperation(
            path = VEHICLE_TELEMETICS_SWITCH,
            operation = @Operation(
                    operationId = "patchVehicleTelematics",
                    tags = {"Vehicle"},
                    summary = "On/Off vehicle telematics",
                    parameters = @Parameter(
                            in = ParameterIn.PATH, name = "vehicleReg",
                            description = "Vehicle registration plate"
                    ),
                    requestBody = @RequestBody(
                            content = @Content(schema = @Schema(implementation = UpdateVehicleTelematicsDTO.class))),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Vehicle telematics successful updated",
                                    content = @Content(schema = @Schema(implementation = String.class))
                            )
                    }))
    public RouterFunction<ServerResponse> routeUpdateVehicleTelematics(VehicleHandler handler) {
        return route(
                PATCH(VEHICLE_TELEMETICS_SWITCH)
                        .and(contentType(MediaType.APPLICATION_JSON))
                        .and(accept(MediaType.TEXT_PLAIN)),
                handler::updateVehicleTelematics
        );
    }

    @Bean
    @RouterOperation(
            path = VEHICLE_POSITION_UPDATE_MANUALLY,
            operation = @Operation(
                    operationId = "postVehiclePositionManually",
                    tags = {"Vehicle"},
                    summary = "Enter new vehicle position manually",
                    parameters = @Parameter(
                            in = ParameterIn.PATH, name = "vehicleReg",
                            description = "Vehicle registration plate"
                    ),
                    requestBody = @RequestBody(
                            content = @Content(schema = @Schema(implementation = UpdateVehiclePositionDTO.class))),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Vehicle position successful updated",
                                    content = @Content(schema = @Schema(implementation = PositionDTO.class))
                            )
                    }))
    public RouterFunction<ServerResponse> routeUpdateVehiclePosition(VehicleHandler handler) {
        return route(
                POST(VEHICLE_POSITION_UPDATE_MANUALLY)
                        .and(contentType(MediaType.APPLICATION_JSON))
                        .and(accept(MediaType.APPLICATION_JSON).or(accept(MediaType.TEXT_PLAIN))),
                handler::updateVehiclePosition
        );
    }

    @Bean
    @RouterOperation(
            path = VEHICLE_GET_POSITION,
            operation = @Operation(
                    operationId = "runGettingVehiclePositionMechanism",
                    tags = {"Vehicle"},
                    summary = "Run mechanism for getting the position of the vehicle",
                    parameters = @Parameter(
                            in = ParameterIn.PATH, name = "vehicleReg",
                            description = "Vehicle registration plate"
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Activation of the vehicle position get mechanism was successful",
                                    content = @Content(schema = @Schema(implementation = String.class))
                            )
                    }))
    public RouterFunction<ServerResponse> routeRunGettingPositionMechanism(VehicleHandler handler) {
        return route(
                POST(VEHICLE_GET_POSITION)
                        .and(accept(MediaType.TEXT_PLAIN)),
                handler::runGettingPositionMechanism
        );
    }

    @Bean
    @RouterOperation(
            path = VEHICLE_BORDER_CROSSING_REPORT,
            operation = @Operation(
                    operationId = "getVehicleBorderCrossingReport",
                    tags = {"Vehicle"},
                    summary = "Generate vehicle border crossing report",
                    parameters = {
                            @Parameter(
                                    in = ParameterIn.QUERY, name = "vehicleReg",
                                    required = true,
                                    description = "Vehicle registration plate"
                            ),
                            @Parameter(
                                    in = ParameterIn.QUERY, name = "startingDate",
                                    description = "Report starting date",
                                    required = true,
                                    example = "2023-07-09T10:00:00Z"
                            ),
                            @Parameter(
                                    in = ParameterIn.QUERY, name = "endDate",
                                    description = "Report end date",
                                    required = true,
                                    example = "2023-07-09T10:00:00Z"
                            )
                    },
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Crossing border report successfully generated"
                            )
                    }))
    public RouterFunction<ServerResponse> routeGetReport(VehicleHandler handler) {
        return route(
                GET(VEHICLE_BORDER_CROSSING_REPORT)
                    .and(accept(MediaType.APPLICATION_JSON)),
                handler::generateBorderCrossingReport
        );
    }
}
