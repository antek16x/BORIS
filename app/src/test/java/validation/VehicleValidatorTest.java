package validation;

import org.boris.validation.VehicleValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VehicleValidatorTest {

    @ParameterizedTest
    @MethodSource("provideCountryCode")
    void isValidCountryCodeTest(String countryCode, Boolean expected) {
        var actual = VehicleValidator.isValidCountryCode(countryCode);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideCoordinates")
    void getCountryCodeFromCoordinatesTest(Double longitude, Double latitude, String expected) {
        var actual = VehicleValidator.getCountryCodeFromCoordinates(longitude, latitude);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> provideCountryCode() {
        return Stream.of(
                Arguments.of("POL", true),
                Arguments.of("LDKF", false),
                Arguments.of("", false),
                Arguments.of("   ", false)
        );
    }

    private static Stream<Arguments> provideCoordinates() {
        return Stream.of(
                Arguments.of(45.1729, -22.9068, "MDG"),
                Arguments.of(24322.2342, 234224.343242, ""),
                Arguments.of(2.3522, 48.8566, "FRA")
        );
    }
}
