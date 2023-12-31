package org.boris.projection;

import org.axonframework.eventhandling.EventHandler;
import org.boris.NewVehicleAddedEvent;
import org.boris.VehicleTelematicsUpdatedEvent;
import org.boris.entity.Vehicles;
import org.boris.repository.VehiclesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VehicleProjection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleProjection.class);

    private final VehiclesRepository vehiclesRepository;

    public VehicleProjection(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    @EventHandler
    public void on(NewVehicleAddedEvent event) {
        final String vehicleReg = event.getVehicleReg().getIdentifier();

        final Vehicles view = new Vehicles();
        view.setVehicleReg(vehicleReg);
        view.setTelematics(event.getTelematicsEnabled());

        vehiclesRepository.save(view);
        LOGGER.info("Vehicles [{}] has been saved", vehicleReg);
    }

    @EventHandler
    public void on(VehicleTelematicsUpdatedEvent event) {
        final String vehicleId = event.getVehicleReg().getIdentifier();

        var viewOpt = vehiclesRepository.findByVehicleReg(vehicleId);
        var view = viewOpt.orElseGet(() -> {
            LOGGER.warn("Missing vehicle [{}] record, creating new one", vehicleId);
            return createVehiclesView(vehicleId, event.getTelematicsEnabled());
        });
        view.setTelematics(event.getTelematicsEnabled());

        vehiclesRepository.save(view);
        LOGGER.info("Telematics for vehicle [{}] switched to {}", vehicleId, event.getTelematicsEnabled());
    }

    private Vehicles createVehiclesView(String vehicleReg, Boolean telematics) {
        var view = new Vehicles();
        view.setVehicleReg(vehicleReg);
        view.setTelematics(telematics);
        return view;
    }
}
