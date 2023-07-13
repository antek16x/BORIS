package org.boris.validator;

import java.util.Locale;

public class VehicleValidator {

    public static boolean isValidCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return false;
        }

        return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).contains(countryCode);
    }
}
