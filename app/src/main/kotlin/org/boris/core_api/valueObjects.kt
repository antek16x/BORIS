package org.boris.core_api

import org.axonframework.common.IdentifierFactory
import java.beans.ConstructorProperties
import java.io.Serializable
import java.time.Instant

data class VehicleId
@ConstructorProperties("identifier")
constructor(val identifier: String = IdentifierFactory.getInstance().generateIdentifier().uppercase()) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

data class VehiclePositionServiceResponse(
    val position: List<Position>
)

data class Position(
    val coordinate: Coordinate,
    var country: String,
    val timestamp: Instant
)

data class Coordinate(
    val longitude: Double,
    val latitude: Double
)