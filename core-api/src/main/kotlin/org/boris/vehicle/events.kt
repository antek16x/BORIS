package org.boris.vehicle

import java.time.Instant

abstract class VehicleEvent(
    open val vehicleReg: String
)

data class NewVehicleAddedEvent(
    override val vehicleReg: String,
    val telematicsEnabled: Boolean
) : VehicleEvent(vehicleReg)

data class LastVehiclePositionUpdatedEvent(
    override val vehicleReg: String,
    val coordinate: Coordinate,
    val country: String,
    val timestamp: Instant
) : VehicleEvent(vehicleReg)

data class VehicleCrossedBorderEvent(
    override val vehicleReg: String,
    val countryBeforeCrossing: String,
    val crossingTimestamp: Instant,
) : VehicleEvent(vehicleReg)

data class CrossingBorderConfirmedEvent(
    override val vehicleReg: String,
    val timestamp: Instant,
    val countryOut: String,
    val countryIn: String
) : VehicleEvent(vehicleReg)