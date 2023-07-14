package services;

import org.boris.query.entity.BorderCrossing;
import org.boris.query.repository.BorderCrossingRepository;
import org.boris.query.repository.VehiclesRepository;
import org.boris.rest.BorderCrossingEvents;
import org.boris.rest.BorderCrossingReportDTO;
import org.boris.rest.Event;
import org.boris.rest.Report;
import org.boris.services.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @InjectMocks
    private VehicleService vehicleService;

    @Mock
    private VehiclesRepository vehiclesRepository;

    @Mock
    private BorderCrossingRepository borderCrossingRepository;

    private static final String vehicleReg = "REG_TEST";
    private static Instant startingDate = Instant.parse("2023-07-01T00:00:00Z");
    private static Instant endDate = Instant.parse("2023-07-31T23:59:59Z");

    @Test
    void checkIfVehicleExistsExistingVehicleTest() {
        when(vehiclesRepository.existsByVehicleReg(vehicleReg)).thenReturn(true);
        var result = vehicleService.checkIfVehicleExists(vehicleReg);

        assertTrue(result);
    }

    @Test
    void checkIfVehicleExistsNonExistingVehicleTest() {
        when(vehiclesRepository.existsByVehicleReg(vehicleReg)).thenReturn(false);
        var result = vehicleService.checkIfVehicleExists(vehicleReg);

        assertFalse(result);
    }

    @Test
    void generateReportExistingElementInRepoTest() {
        var borderCrossing = new BorderCrossing();
        borderCrossing.setVehicleReg(vehicleReg);
        borderCrossing.setTimestamp(Instant.parse("2023-07-31T23:59:59Z"));
        borderCrossing.setCountryIn("FRA");
        borderCrossing.setCountryOut("POL");

        var borderCrossingList = List.of(borderCrossing);

        when(borderCrossingRepository.findByVehicleRegAndTimestampBetween(vehicleReg, startingDate, endDate))
                .thenReturn(borderCrossingList);

        var actual = vehicleService.generateReport(vehicleReg, startingDate, endDate);
        var expected = new BorderCrossingReportDTO(
                Instant.now(),
                List.of(
                        new Report(
                                new BorderCrossingEvents(
                                        vehicleReg,
                                        List.of(
                                                new Event(
                                                        Instant.parse("2023-07-31T23:59:59Z"),
                                                        "POL",
                                                        "FRA"
                                                )
                                        )
                                )
                        )
                )
        );

        verify(borderCrossingRepository).findByVehicleRegAndTimestampBetween(vehicleReg, startingDate, endDate);
        assertEquals(expected, actual);
    }

    @Test
    void generateReportNothingInRepoTest() {
        when(borderCrossingRepository.findByVehicleRegAndTimestampBetween(vehicleReg, startingDate, endDate))
                .thenReturn(List.of());

        var actual = vehicleService.generateReport(vehicleReg, startingDate, endDate);
        var expected = new BorderCrossingReportDTO(
                Instant.now(),
                List.of(
                        new Report(
                                new BorderCrossingEvents(
                                        vehicleReg,
                                        List.of()
                                )
                        )
                )
        );

        verify(borderCrossingRepository).findByVehicleRegAndTimestampBetween(vehicleReg, startingDate, endDate);
        assertEquals(expected, actual);
    }

}
