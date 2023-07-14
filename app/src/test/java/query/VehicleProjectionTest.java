package query;

import org.boris.core_api.NewVehicleAddedEvent;
import org.boris.core_api.VehicleId;
import org.boris.core_api.VehicleTelematicsUpdatedEvent;
import org.boris.query.entity.Vehicles;
import org.boris.query.projection.VehicleProjection;
import org.boris.query.repository.VehiclesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {DBTestConfiguration.class, VehicleProjection.class}
)
@ActiveProfiles("test")
public class VehicleProjectionTest {

    @Resource
    @SuppressWarnings("unused")
    private VehiclesRepository vehiclesRepository;

    @Autowired
    @SuppressWarnings("unused")
    private VehicleProjection vehicleProjection;

    @Transactional
    @Test
    void saveNewVehicleTest() {
        var vehicleId = new VehicleId("REG_TEST");

        vehicleProjection.on(
                new NewVehicleAddedEvent(
                        vehicleId,
                        true,
                        "POL"
                )
        );

        var expected = new Vehicles();
        expected.setVehicleReg(vehicleId.getIdentifier());
        expected.setTelematics(true);

        var actual = vehiclesRepository.findByVehicleReg(vehicleId.getIdentifier());

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Transactional
    @Test
    void updateVehicleTelematinsTest() {
        var vehicleId = new VehicleId("REG_TEST");
        insertVehicle(vehicleId.getIdentifier());

        vehicleProjection.on(
                new VehicleTelematicsUpdatedEvent(vehicleId, true)
        );

        var expected = new Vehicles();
        expected.setVehicleReg(vehicleId.getIdentifier());
        expected.setTelematics(true);

        var actual = vehiclesRepository.findByVehicleReg(vehicleId.getIdentifier());

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    private void insertVehicle(String vehicleId) {
        var createdVehicle = new Vehicles();
        createdVehicle.setVehicleReg(vehicleId);
        createdVehicle.setTelematics(false);
        vehiclesRepository.save(createdVehicle);
    }
}
