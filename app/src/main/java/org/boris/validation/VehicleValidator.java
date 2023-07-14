package org.boris.validation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import io.github.coordinates2country.Coordinates2Country;
import org.boris.vehicle.exceptions.CountryCodeNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleValidator {

    private final static Logger LOGGER = LoggerFactory.getLogger(VehicleValidator.class);

    private static final Map<String, String> isoMap = createISOMap();

    public static String getCountryCodeFromCoordinates(Double longitude, Double latitude) {
        var countryName = Coordinates2Country.country(latitude, longitude);
        try {
            return getCountryISO3Code(countryName);
        } catch (Exception exception) {
            LOGGER.info("Can't find country code for country {}", countryName);
            return "";
        }
    }

    public static boolean isValidCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return false;
        }

        return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).contains(countryCode);
    }

    private static Map<String, String> createISOMap() {
        Map<String, String> map = new HashMap<>();
        for (String isoCountry : Locale.getISOCountries()) {
            Locale locale = new Locale("", isoCountry);
            map.put(isoCountry, locale.getISO3Country());
        }
        return map;
    }

    private static String getCountryISO3Code(String countryName) throws CountryCodeNotFound {
        return isoMap.entrySet().stream()
                .filter(entry -> countryName.equalsIgnoreCase(new Locale("", entry.getKey()).getDisplayCountry(Locale.ENGLISH)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new CountryCodeNotFound(String.format("Can't find country code for country %s", countryName)));
    }
}
