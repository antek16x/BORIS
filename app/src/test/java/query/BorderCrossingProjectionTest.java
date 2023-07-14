package query;

import org.boris.core_api.CrossingBorderConfirmedEvent;
import org.boris.core_api.VehicleId;
import org.boris.query.entity.BorderCrossing;
import org.boris.query.projection.BorderCrossingProjection;
import org.boris.query.repository.BorderCrossingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {DBTestConfiguration.class, BorderCrossingProjection.class}
)
@ActiveProfiles("test")
public class BorderCrossingProjectionTest {


    @Resource
    @SuppressWarnings("unused")
    private BorderCrossingRepository borderCrossingRepository;

    @Autowired
    @SuppressWarnings("unused")
    private BorderCrossingProjection borderCrossingProjection;

    @Transactional
    @Test
    void saveBorderCrossingInformation() {
        var vehicleId = new VehicleId("REG_TEST");
        var date = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        borderCrossingProjection.on(new CrossingBorderConfirmedEvent(
                vehicleId,
                date,
                "POL",
                "DEU"
        ));

        var expected = new BorderCrossing();
        expected.setVehicleReg(vehicleId.getIdentifier());
        expected.setTimestamp(date);
        expected.setCountryOut("POL");
        expected.setCountryIn("DEU");

        var actual = borderCrossingRepository.findByVehicleReg(vehicleId.getIdentifier());

        assertEquals(expected, actual);
    }
}
