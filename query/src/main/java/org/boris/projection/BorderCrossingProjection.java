package org.boris.projection;

import org.axonframework.eventhandling.EventHandler;
import org.boris.CrossingBorderConfirmedEvent;
import org.boris.entity.BorderCrossing;
import org.boris.repository.BorderCrossingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BorderCrossingProjection {

    private static final Logger LOGGER = LoggerFactory.getLogger(BorderCrossingProjection.class);

    private final BorderCrossingRepository borderCrossingRepository;


    public BorderCrossingProjection(BorderCrossingRepository borderCrossingRepository) {
        this.borderCrossingRepository = borderCrossingRepository;
    }

    @EventHandler
    public void on(CrossingBorderConfirmedEvent event) {
        final String vehicleReg = event.getVehicleReg().getIdentifier();

        final BorderCrossing view = new BorderCrossing();
        view.setVehicleReg(vehicleReg);
        view.setTimestamp(event.getTimestamp());
        view.setCountryOut(event.getCountryOut());
        view.setCountryIn(event.getCountryIn());

        borderCrossingRepository.save(view);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Border crossing for vehicle [{}] has been saved", vehicleReg);
        }
    }
}
