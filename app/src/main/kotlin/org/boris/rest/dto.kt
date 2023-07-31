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
    val positions: List<Position>
)

@Schema(description = "Content of vehicle position")
data class Position(
    @get:Schema(description = "Geographical coordinates")
    val coordinate: Coordinate,
    @get:Schema(description = "The code of the country (ISO Alpha-3)", example = "POL")
    val country: String,
    @get:Schema(description = "Position timestamp", example = "2023-07-09T10:00:00Z")
    val timestamp: Instant
)

@Schema(description = "Content of coordinate")
data class Coordinate(
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
    var report: List<Report>,
)

@Schema(description = "Content of report")
data class Report(
    @get:Schema(description = "Information's about crossing the borders")
    var borderCrossingEvents: BorderCrossingEvents?,
)

@Schema(description = "Content of information about borders crossing")
data class BorderCrossingEvents(
    @get:Schema(description = "Vehicle registration plate" , example = "ABC123")
    var vehicleReg: String,
    @get:Schema(description = "List of crossing border events")
    var events: List<Event>
)

@Schema(description = "Content of border crossing event")
data class Event(
    @get:Schema(description = "Event timestamp", example = "2023-07-09T10:00:00Z")
    var eventTimestamp: Instant,
    @get:Schema(description = "The code of the country that has been left (ISO Alpha-3)", example = "POL")
    var countryOut: String,
    @get:Schema(description = "The code of the country you entered (ISO Alpha-3)", example = "DEU")
    var countryIn: String,
)