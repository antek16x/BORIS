package vehicle;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.boris.core_api.*;
import org.boris.services.VehiclePositionService;
import org.boris.validation.VehicleValidator;
import org.boris.vehicle.Vehicle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("prod")
public class VehicleTest {

    private static final String GET_VEHICLE_POSITION_DEADLINE = "getVehiclePositionDeadline";
    private static final String CONFIRM_BORDER_CROSSING_DEADLINE = "confirmBorderCrossingDeadline";

    private AggregateTestFixture<Vehicle> fixture;

    @MockBean
    @SuppressWarnings("unused")
    private VehiclePositionService vehiclePositionService;
    @MockBean
    @SuppressWarnings("unused")
    private VehicleValidator vehicleValidator;
    private VehicleId vehicleId;
    private LocalDate currentDate;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Vehicle.class);
        vehicleId = new VehicleId("REG_TEST");
        currentDate = LocalDate.now();

        fixture.registerInjectableResource(vehicleValidator);
        fixture.registerInjectableResource(vehiclePositionService);
    }

    @Test
    void addNewVehicleWithTelematicsEnabledTest() {
        this.fixture.givenNoPriorActivity()
                .when(new AddNewVehicleCommand(
                        vehicleId,
                        true,
                        "POL"
                )).expectEvents(new NewVehicleAddedEvent(
                        vehicleId,
                        true,
                        "POL"
                )).expectState(aggregate -> {
                    Assertions.assertEquals(vehicleId, aggregate.getVehicleReg());
                    Assertions.assertEquals(true, aggregate.getTelematics());
                    Assertions.assertEquals("POL", aggregate.getLastKnownCountry());
                })
                .expectResultMessagePayload(vehicleId)
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), GET_VEHICLE_POSITION_DEADLINE);
    }

    @Test
    void addNewVehicleWithTelematicsDisabledTest() {
        this.fixture.givenNoPriorActivity()
                .when(new AddNewVehicleCommand(
                        vehicleId,
                        false,
                        "POL"
                )).expectEvents(new NewVehicleAddedEvent(
                        vehicleId,
                        false,
                        "POL"
                )).expectState(aggregate -> {
                    Assertions.assertEquals(vehicleId, aggregate.getVehicleReg());
                    Assertions.assertEquals(false, aggregate.getTelematics());
                    Assertions.assertEquals("POL", aggregate.getLastKnownCountry());
                })
                .expectResultMessagePayload(vehicleId)
                .expectNoScheduledDeadlines();
    }

    @Test
    void updateVehicleTelematicsEnableTelematicsTest() {
        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                false,
                                "POL"
                        )
                )
                .when(new UpdateVehicleTelematicsCommand(vehicleId, true))
                .expectEvents(new VehicleTelematicsUpdatedEvent(vehicleId, true))
                .expectState(aggregate -> Assertions.assertEquals(true, aggregate.getTelematics()))
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), GET_VEHICLE_POSITION_DEADLINE);
    }

    @Test
    void updateVehicleTelematicsDisableTelematicsTest() {
        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "POL"
                        )
                )
                .when(new UpdateVehicleTelematicsCommand(vehicleId, false))
                .expectEvents(new VehicleTelematicsUpdatedEvent(vehicleId, false))
                .expectState(aggregate -> Assertions.assertEquals(false, aggregate.getTelematics()));
    }

    @Test
    void updateVehicleTelematicsSameValueTest() {
        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                false,
                                "POL"
                        )
                )
                .when(new UpdateVehicleTelematicsCommand(vehicleId, false))
                .expectNoEvents();
    }

    @Test
    void updateVehiclePositionManuallyWithoutCrossingBorderTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                false,
                                "POL"
                        )
                )
                .when(new UpdateVehiclePositionCommand(
                        vehicleId,
                        true,
                        new Coordinate(19.6914, 50.4948),
                        "POL",
                        date
                ))
                .expectEvents(
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(19.6914, 50.4948),
                                "POL",
                                date
                        )
                ).expectState(aggregate -> Assertions.assertEquals("POL", aggregate.getLastKnownCountry()));
    }

    @Test
    void updateVehiclePositionManuallyWithCrossingBorderTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                false,
                                "POL"
                        )
                )
                .when(new UpdateVehiclePositionCommand(
                        vehicleId,
                        true,
                        new Coordinate(11.4666, 51.2545),
                        "DEU",
                        date
                ))
                .expectEvents(
                        new CrossingBorderConfirmedEvent(
                                vehicleId,
                                date,
                                "POL",
                                "DEU"
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(11.4666, 51.2545),
                                "DEU",
                                date
                        )
                ).expectState(aggregate -> Assertions.assertEquals("DEU", aggregate.getLastKnownCountry()));
    }

    @Test
    void updateVehiclePositionRunMechanismManuallyWithoutCrossingBorderTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        var serviceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "POL",
                        date
                )
        ));
        doReturn(serviceResponse).when(vehiclePositionService).getVehiclePosition(vehicleId.getIdentifier());
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));

        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "POL"
                        )
                )
                .when(new UpdateVehiclePositionCommand(
                        vehicleId,
                        false,
                        null,
                        null,
                        null
                ))
                .expectEvents(
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "POL",
                                date
                        )
                ).expectState(aggregate -> Assertions.assertEquals("POL", aggregate.getLastKnownCountry()))
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), GET_VEHICLE_POSITION_DEADLINE);

        verify(vehiclePositionService, times(1)).getVehiclePosition(vehicleId.getIdentifier());

    }

    @Test
    void updateVehiclePositionRunMechanismManuallyWithCrossingBorderTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        var serviceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "DEU",
                        date
                )
        ));
        doReturn(serviceResponse).when(vehiclePositionService).getVehiclePosition(vehicleId.getIdentifier());
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));

        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "POL"
                        )
                )
                .when(new UpdateVehiclePositionCommand(
                        vehicleId,
                        false,
                        null,
                        null,
                        null
                ))
                .expectEvents(
                        new VehicleCrossedBorderEvent(
                                vehicleId,
                                "POL",
                                date
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "DEU",
                                date
                        )
                ).expectState(aggregate -> {
                    Assertions.assertEquals("DEU", aggregate.getLastKnownCountry());
                    Assertions.assertEquals(date, aggregate.getCrossingBorderTimestamp());
                    Assertions.assertEquals("POL", aggregate.getCountryOut());
                })
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), CONFIRM_BORDER_CROSSING_DEADLINE);

        verify(vehiclePositionService, times(1)).getVehiclePosition(vehicleId.getIdentifier());
    }

    @Test
    @DisplayName("Test of mechanism with deadline, crossing border but invalid country code in response")
    void updateVehiclePositionWithDeadlineAndMechanismTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        var serviceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "",
                        date
                )
        ));
        doReturn(serviceResponse).when(vehiclePositionService).getVehiclePosition(vehicleId.getIdentifier());
        doReturn("SEN").when(vehicleValidator).getCountryCodeFromCoordinates(any(Double.class), any(Double.class));
        ;

        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "DEU"
                        )
                )
                .whenTimeElapses(Duration.ofMinutes(5))
                .expectTriggeredDeadlinesWithName(GET_VEHICLE_POSITION_DEADLINE)
                .expectEvents(
                        new VehicleCrossedBorderEvent(
                                vehicleId,
                                "DEU",
                                date
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "SEN",
                                date
                        )
                ).expectState(aggregate -> Assertions.assertEquals("SEN", aggregate.getLastKnownCountry()))
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), CONFIRM_BORDER_CROSSING_DEADLINE);

        verify(vehiclePositionService, times(1)).getVehiclePosition(vehicleId.getIdentifier());
        verify(vehicleValidator, times(1)).getCountryCodeFromCoordinates(any(Double.class), any(Double.class));
        ;
    }

    @Test
    @DisplayName("Test of mechanism with deadline, crossing border buy without confirmation")
    void confirmationMechanismCannotConfirmTest() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        var firstServiceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "SAU",
                        date
                )
        ));
        var secondServiceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "DEU",
                        date
                )
        ));
        doReturn(firstServiceResponse).doReturn(secondServiceResponse).when(vehiclePositionService).getVehiclePosition(vehicleId.getIdentifier());
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));

        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "DEU"
                        )
                )
                .whenTimeElapses(Duration.ofMinutes(10))
                .expectTriggeredDeadlinesWithName(GET_VEHICLE_POSITION_DEADLINE, CONFIRM_BORDER_CROSSING_DEADLINE)
                .expectEvents(
                        new VehicleCrossedBorderEvent(
                                vehicleId,
                                "DEU",
                                date
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "SAU",
                                date
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "DEU",
                                date

                        )
                ).expectState(aggregate -> Assertions.assertEquals("DEU", aggregate.getLastKnownCountry()))
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), GET_VEHICLE_POSITION_DEADLINE);

        verify(vehiclePositionService, times(2)).getVehiclePosition(vehicleId.getIdentifier());
        verify(vehicleValidator, times(0)).getCountryCodeFromCoordinates(any(Double.class), any(Double.class));
    }

    @Test
    @DisplayName("Test of mechanism with deadline, crossing border buy without confirmation")
    void confirmationMechanismConfirmationSucceed() {
        var date = LocalDate.of(2023, 7, 14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        var firstServiceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "SAU",
                        date
                )
        ));
        var secondServiceResponse = Mono.just(List.of(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "SAU",
                        date
                )
        ));
        doReturn(firstServiceResponse).doReturn(secondServiceResponse).when(vehiclePositionService).getVehiclePosition(vehicleId.getIdentifier());
        doReturn(true).when(vehicleValidator).isValidCountryCode(any(String.class));

        this.fixture.givenCurrentTime(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .andGivenCommands(
                        new AddNewVehicleCommand(
                                vehicleId,
                                true,
                                "DEU"
                        )
                )
                .whenTimeElapses(Duration.ofMinutes(10))
                .expectTriggeredDeadlinesWithName(GET_VEHICLE_POSITION_DEADLINE, CONFIRM_BORDER_CROSSING_DEADLINE)
                .expectEvents(
                        new VehicleCrossedBorderEvent(
                                vehicleId,
                                "DEU",
                                date
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "SAU",
                                date
                        ),
                        new CrossingBorderConfirmedEvent(
                                vehicleId,
                                date,
                                "DEU",
                                "SAU"
                        ),
                        new LastVehiclePositionUpdatedEvent(
                                vehicleId,
                                new Coordinate(52.2297, 21.0122),
                                "SAU",
                                date

                        )
                ).expectState(aggregate -> Assertions.assertEquals("SAU", aggregate.getLastKnownCountry()))
                .expectScheduledDeadlineWithName(Duration.ofMinutes(5), GET_VEHICLE_POSITION_DEADLINE);

        verify(vehiclePositionService, times(2)).getVehiclePosition(vehicleId.getIdentifier());
        verify(vehicleValidator, times(0)).getCountryCodeFromCoordinates(any(Double.class), any(Double.class));
        ;
    }
}
