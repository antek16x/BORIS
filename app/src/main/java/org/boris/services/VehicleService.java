package org.boris.services;

import org.boris.query.repository.VehiclesRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private final VehiclesRepository vehiclesRepository;


    public VehicleService(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    public Boolean checkIfVehicleExists(String vehicleReg) {
        return vehiclesRepository.existsByVehicleReg(vehicleReg.toUpperCase());
    }
}
