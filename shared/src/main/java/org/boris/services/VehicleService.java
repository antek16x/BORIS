package org.boris.services;

import org.boris.entity.BorderCrossing;
import org.boris.repository.BorderCrossingRepository;
import org.boris.repository.VehiclesRepository;
import org.boris.rest.BorderCrossingEventsDTO;
import org.boris.rest.BorderCrossingReportDTO;
import org.boris.rest.EventDTO;
import org.boris.rest.ReportDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleService {

    private final VehiclesRepository vehiclesRepository;
    private final BorderCrossingRepository borderCrossingRepository;


    public VehicleService(VehiclesRepository vehiclesRepository, BorderCrossingRepository borderCrossingRepository) {
        this.vehiclesRepository = vehiclesRepository;
        this.borderCrossingRepository = borderCrossingRepository;
    }

    public Boolean checkIfVehicleExists(String vehicleReg) {
        return vehiclesRepository.existsByVehicleReg(vehicleReg.toUpperCase());
    }

    public BorderCrossingReportDTO generateReport(String vehicleReg, Instant startingDate, Instant endDate) {
        var borderCrossings = borderCrossingRepository.findByVehicleRegAndTimestampBetween(
                vehicleReg,
                startingDate,
                endDate
        );

        List<EventDTO> events = new ArrayList<>();
        for (BorderCrossing borderCrossing : borderCrossings) {
            events.add(new EventDTO(
                    borderCrossing.getTimestamp(),
                    borderCrossing.getCountryOut(),
                    borderCrossing.getCountryIn()
            ));
        }

        var borderCrossingEvents = new BorderCrossingEventsDTO(
                vehicleReg,
                events
        );

        var report = new ReportDTO(borderCrossingEvents);

        return new BorderCrossingReportDTO(
                Instant.now(),
                List.of(report)
        );
    }
}
