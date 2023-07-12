package org.boris.query.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Vehicles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentityId")
    Long identityId;

    @Column(name = "VehicleReg")
    String vehicleReg;

    public String getVehicleReg() {
        return vehicleReg;
    }

    public void setVehicleReg(String vehicleReg) {
        this.vehicleReg = vehicleReg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicles that)) {
            return false;
        }
        return vehicleReg.equalsIgnoreCase(that.vehicleReg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleReg);
    }
}
