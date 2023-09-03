package org.boris.rest

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Content of vehicle")
data class AddNewVehicleDTO(
    @get:Schema(description = "Vehicle registration plate")
    val vehicleReg: String,
    @get:Schema(description = "Telematics on/off")
    val telematicsEnabled: Boolean?
)

@Schema(description = "Content of update vehicle telematics")
data class UpdateVehicleTelematicsDTO(
    @get:Schema(
        description = "Parameter to disabled/enabled vehicle telematics",
        defaultValue = "false"
    )
    val enabled: Boolean
)

@Schema(description = "Content of update vehicle position")
data class UpdateVehiclePositionDTO(
    @get:Schema(description = "Vehicle position to entry")
    val positions: List<PositionDTO>
)

@Schema(description = "Content of vehicle position")
data class PositionDTO(
    @get:Schema(description = "Geographical coordinates")
    val coordinate: CoordinateDTO,
    @get:Schema(description = "The code of the country (ISO Alpha-3)", example = "POL")
    val country: String,
    @get:Schema(description = "Position timestamp", example = "2023-07-09T10:00:00Z")
    val timestamp: Instant
)

@Schema(description = "Content of coordinate")
data class CoordinateDTO(
    @get:Schema(description = "Longitude", example = "52.2297")
    val longitude: Double,
    @get:Schema(description = "Latitude", example = "21.0122")
    val latitude: Double
)

@Schema(description = "Content of border crossing report")
data class BorderCrossingReportDTO(
    @get:Schema(description = "Report generation timestamp", example = "2023-07-09T10:00:00Z")
    var reportTimestamp: Instant,
    @get:Schema(description = "Broder crossing event report")
    var report: List<ReportDTO>,
)

@Schema(description = "Content of report")
data class ReportDTO(
    @get:Schema(description = "Information's about crossing the borders")
    var borderCrossingEvents: BorderCrossingEventsDTO?,
)

@Schema(description = "Content of information about borders crossing")
data class BorderCrossingEventsDTO(
    @get:Schema(description = "Vehicle registration plate" , example = "ABC123")
    var vehicleReg: String,
    @get:Schema(description = "List of crossing border events")
    var events: List<EventDTO>
)

@Schema(description = "Content of border crossing event")
data class EventDTO(
    @get:Schema(description = "Event timestamp", example = "2023-07-09T10:00:00Z")
    var eventTimestamp: Instant,
    @get:Schema(description = "The code of the country that has been left (ISO Alpha-3)", example = "POL")
    var countryOut: String,
    @get:Schema(description = "The code of the country you entered (ISO Alpha-3)", example = "DEU")
    var countryIn: String,
)