package rest;

import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.boris.core_api.AddNewVehicleCommand;
import org.boris.core_api.UpdateVehiclePositionCommand;
import org.boris.core_api.UpdateVehicleTelematicsCommand;
import org.boris.core_api.VehicleId;
import org.boris.query.repository.BorderCrossingRepository;
import org.boris.query.repository.VehiclesRepository;
import org.boris.rest.*;
import org.boris.rest.handler.VehicleHandler;
import org.boris.rest.router.VehicleRouter;
import org.boris.services.VehicleService;
import org.boris.validation.VehicleValidator;
import org.boris.vehicle.exceptions.InvalidTelematicsUpdateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebFluxTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {
        VehicleRouter.class,
        VehicleHandler.class
})
public class VehicleRouterTest {

    private static final VehicleId VEHICLE_ID = new VehicleId("REG_TEST");

    private static final AddNewVehicleDTO ADD_NEW_VEHICLE_DTO = new AddNewVehicleDTO(
            VEHICLE_ID.getIdentifier(),
            true,
            "POL"
    );

    private static final UpdateVehiclePositionDTO UPDATE_VEHICLE_POSITION_DTO_MANUALLY = new UpdateVehiclePositionDTO(
            List.of(
                    new Position(
                            new Coordinate(52.2297, 21.0122),
                            "POL",
                            Instant.parse("2023-07-09T10:00:00Z")
                    )
            )
    );

    private static final UpdateVehicleTelematicsDTO UPDATE_VEHICLE_TELEMATICS_DTO = new UpdateVehicleTelematicsDTO(true);

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactorCommandGateway commandGateway;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private VehiclesRepository vehiclesRepository;

    @MockBean
    private VehicleValidator vehicleValidator;

    @MockBean
    private BorderCrossingRepository borderCrossingRepository;

    @Test
    void addVehicleInvalidBodyTest() {
        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("body")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        verify(commandGateway, times(0)).send(any(AddNewVehicleCommand.class));
    }

    @Test
    void addVehicleTest() {
        doReturn(Mono.just(VEHICLE_ID)).when(commandGateway).send(any(AddNewVehicleCommand.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ADD_NEW_VEHICLE_DTO).exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .value(id -> assertEquals(VEHICLE_ID.getIdentifier(), id));
    }

    @Test
    void addVehicleUnknownErrorTest() {
        doReturn(Mono.error(new Exception("exception"))).when(commandGateway).send(any(AddNewVehicleCommand.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ADD_NEW_VEHICLE_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void addVehicleAlreadyExistsTest() {
        doReturn(true).when(vehicleService).checkIfVehicleExists(any());

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ADD_NEW_VEHICLE_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        verify(commandGateway, times(0)).send(any(AddNewVehicleCommand.class));
    }

    @Test
    void addVehicleAggregateNotFound() {
        doReturn(Mono.error(new AggregateNotFoundException(VEHICLE_ID.getIdentifier(), "The aggregate was not found in the event store")))
                .when(commandGateway)
                .send(any(AddNewVehicleCommand.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ADD_NEW_VEHICLE_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateVehicleTelematicsInvalidBodyTest() {
        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_TELEMETICS_SWITCH, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("body")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        verify(commandGateway, times(0)).send(any(UpdateVehicleTelematicsCommand.class));
    }

    @Test
    void updateVehicleTelematicsTest() {
        doReturn(Mono.just(VEHICLE_ID)).when(commandGateway).send(any(UpdateVehicleTelematicsCommand.class));

        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_TELEMETICS_SWITCH, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_TELEMATICS_DTO).exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody(String.class);
        verify(commandGateway, times(1)).send(any(UpdateVehicleTelematicsCommand.class));
    }

    @Test
    void updateVehicleTelematicsUnknownErrorTest() {
        doReturn(Mono.error(new Exception("exception"))).when(commandGateway).send(any(UpdateVehicleTelematicsCommand.class));

        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_TELEMETICS_SWITCH, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_TELEMATICS_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateVehicleTelematicsSameStatusTest() {
        doReturn(Mono.error(new InvalidTelematicsUpdateException("Can't change telematics status it's already")))
                .when(commandGateway).send(any(UpdateVehicleTelematicsCommand.class));

        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_TELEMETICS_SWITCH, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_TELEMATICS_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        verify(commandGateway, times(1)).send(any(UpdateVehicleTelematicsCommand.class));
    }

    @Test
    void updateVehicleTelematicsAggregateNotFound() {
        doReturn(Mono.error(new AggregateNotFoundException(VEHICLE_ID.getIdentifier(), "The aggregate was not found in the event store")))
                .when(commandGateway)
                .send(any(UpdateVehicleTelematicsCommand.class));

        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_TELEMETICS_SWITCH, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_TELEMATICS_DTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateVehiclePositionInvalidBodyTest() {
        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_POSITION_UPDATE_MANUALLY, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("body")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        verify(commandGateway, times(0)).send(any(UpdateVehiclePositionCommand.class));
    }

    @Test
    void updateVehiclePositionTest() {
        doReturn(Mono.just(VEHICLE_ID)).when(commandGateway).send(any(UpdateVehiclePositionCommand.class));
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_POSITION_UPDATE_MANUALLY, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_POSITION_DTO_MANUALLY).exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody(String.class);
        verify(commandGateway, times(1)).send(any(UpdateVehiclePositionCommand.class));
    }

    @Test
    void updateVehiclePositionUnknownErrorTest() {
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));
        doReturn(Mono.error(new Exception("exception"))).when(commandGateway).send(any(UpdateVehiclePositionCommand.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_POSITION_UPDATE_MANUALLY, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_POSITION_DTO_MANUALLY)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateVehiclePositionInvalidCountryCodeTest() {
        doReturn(false).when(vehicleValidator).isValidCountryCode(any(String.class));

        webTestClient.post()
                .uri(VehicleRouter.VEHICLE_POSITION_UPDATE_MANUALLY, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_POSITION_DTO_MANUALLY)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        verify(commandGateway, times(0)).send(any(UpdateVehiclePositionCommand.class));
    }

    @Test
    void updateVehiclePositionAggregateNotFound() {
        doReturn(Mono.error(new AggregateNotFoundException(VEHICLE_ID.getIdentifier(), "The aggregate was not found in the event store")))
                .when(commandGateway)
                .send(any(UpdateVehiclePositionCommand.class));

        webTestClient.patch()
                .uri(VehicleRouter.VEHICLE_POSITION_UPDATE_MANUALLY, VEHICLE_ID.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UPDATE_VEHICLE_POSITION_DTO_MANUALLY)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateReportTest() {
        var reportDto = new BorderCrossingReportDTO(
                Instant.now(),
                List.of(new Report(
                        new BorderCrossingEvents(
                                VEHICLE_ID.getIdentifier(),
                                List.of(new Event(
                                        Instant.parse("2023-07-09T10:00:00Z"),
                                        "POL",
                                        "FRA"
                                ))
                        )
                ))
        );
        doReturn(reportDto).when(vehicleService).generateReport(
                any(String.class),
                any(Instant.class),
                any(Instant.class)
        );

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(VehicleRouter.VEHICLE_BORDER_CROSSING_REPORT)
                        .queryParam("vehicleReg", VEHICLE_ID.getIdentifier())
                        .queryParam("startingDate","2023-07-09T10:00:00Z")
                        .queryParam("endDate", "2023-07-09T10:00:00Z")
                        .build()
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody(BorderCrossingReportDTO.class);
    }

    @Test
    void generateReportInvalidDateFormatTest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(VehicleRouter.VEHICLE_BORDER_CROSSING_REPORT)
                        .queryParam("vehicleReg", VEHICLE_ID.getIdentifier())
                        .queryParam("startingDate","sdf")
                        .queryParam("endDate", "2023-07-09T10:00:00Z")
                        .build()
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generateReportEmptyVehicleRegTest() {
        doReturn(false).when(vehicleValidator).isValidCountryCode(any(String.class));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(VehicleRouter.VEHICLE_BORDER_CROSSING_REPORT)
                        .queryParam("vehicleReg", "")
                        .queryParam("startingDate","sdf")
                        .queryParam("endDate", "2023-07-09T10:00:00Z")
                        .build()
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
