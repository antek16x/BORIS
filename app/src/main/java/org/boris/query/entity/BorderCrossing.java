package org.boris.query.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
public class BorderCrossing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentityId")
    Long identityId;

    @Column(name = "VehicleReg")
    String vehicleReg;

    @Column(name = "Timestamp")
    Instant timestamp;

    @Column(name = "CountryOut")
    String countryOut;

    @Column(name = "CountryIn")
    String countryIn;

    public String getVehicleReg() {
        return vehicleReg;
    }

    public void setVehicleReg(String vehicleReg) {
        this.vehicleReg = vehicleReg;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCountryOut() {
        return countryOut;
    }

    public void setCountryOut(String countryOut) {
        this.countryOut = countryOut;
    }

    public String getCountryIn() {
        return countryIn;
    }

    public void setCountryIn(String countryIn) {
        this.countryIn = countryIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BorderCrossing that)) {
            return false;
        }
        return vehicleReg.equalsIgnoreCase(that.vehicleReg) && timestamp.equals(that.timestamp) && countryOut.equalsIgnoreCase(that.countryOut) && countryIn.equalsIgnoreCase(that.countryIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleReg, timestamp, countryOut, countryIn);
    }
}
