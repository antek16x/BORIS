package org.boris.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Vehicles {

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentityId")
    Long identityId;

    @Column(name = "VehicleReg")
    String vehicleReg;

    @Column(name = "TelematiceEnabled")
    Boolean telematics;

    public Vehicles() {
    }

    public String getVehicleReg() {
        return vehicleReg;
    }

    public void setVehicleReg(String vehicleReg) {
        this.vehicleReg = vehicleReg;
    }

    @SuppressWarnings("unused")
    public Boolean getTelematics() {
        return telematics;
    }

    public void setTelematics(Boolean telematics) {
        this.telematics = telematics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicles that)) {
            return false;
        }
        return vehicleReg.equalsIgnoreCase(that.vehicleReg) && telematics.equals(that.telematics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleReg, telematics);
    }
}
