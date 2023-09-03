package org.boris.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.boris.config.WebClientAutoConfiguration;
import org.boris.Coordinate;
import org.boris.Position;
import org.boris.VehiclePositionServiceResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;

@WebFluxTest
@ContextConfiguration(classes = {
        VehiclePositionService.class,
        WebClientAutoConfiguration.class
})
public class VehiclePositionServiceTest {


    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @AfterEach
    public void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetVehiclePosition() throws JsonProcessingException {
        String vehicleReg = "REG_TEST";
        var response = new VehiclePositionServiceResponse(
                new Position(
                        new Coordinate(52.2297, 21.0122),
                        "POL",
                        Instant.parse("2023-07-09T10:00:00Z")
                )
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(response)));

        HttpUrl url = mockWebServer.url("");
        WebClient mockWebClient = WebClient.builder()
                .baseUrl(url.toString())
                .build();

        VehiclePositionService vehiclePositionService = new VehiclePositionService(mockWebClient);

        StepVerifier.create(vehiclePositionService.getVehiclePosition(vehicleReg))
                .expectNext(response.getPosition())
                .expectComplete()
                .verify();
    }
}
