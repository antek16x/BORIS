package org.boris.services;

import org.boris.query.entity.BorderCrossing;
import org.boris.query.repository.BorderCrossingRepository;
import org.boris.query.repository.VehiclesRepository;
import org.boris.rest.BorderCrossingEvents;
import org.boris.rest.BorderCrossingReportDTO;
import org.boris.rest.Event;
import org.boris.rest.Report;
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

        List<Event> events = new ArrayList<>();
        for (BorderCrossing borderCrossing : borderCrossings) {
            events.add(new Event(
                    borderCrossing.getTimestamp(),
                    borderCrossing.getCountryOut(),
                    borderCrossing.getCountryIn()
            ));
        }

        var borderCrossingEvents = new BorderCrossingEvents(
                vehicleReg,
                events
        );

        var report = new Report(borderCrossingEvents);

        return new BorderCrossingReportDTO(
                Instant.now(),
                List.of(report)
        );
    }
}
