package org.boris.services;

import org.boris.core_api.Position;
import org.boris.core_api.VehiclePositionServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@DependsOn(value = {"externalApiWebClient"})
public class VehiclePositionService {

    private final WebClient externalApiWebClient;

    @Autowired
    public VehiclePositionService(WebClient externalApiWebClient) {
        this.externalApiWebClient = externalApiWebClient;
    }

    public Mono<Position> getVehiclePosition(String vehicleReg) {
        var getUrl = "/vehicle/{vehicleReg}";

        return externalApiWebClient.get()
                .uri(getUrl, vehicleReg)
                .retrieve()
                .bodyToMono(VehiclePositionServiceResponse.class)
                .map(VehiclePositionServiceResponse::getPosition);
    }
}
