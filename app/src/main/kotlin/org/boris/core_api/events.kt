package org.boris.core_api

import java.time.Instant

abstract class VehicleEvent(
    open val vehicleReg: VehicleId
)

data class NewVehicleAddedEvent(
    override val vehicleReg: VehicleId,
    val telematicsEnabled: Boolean,
    val initialCountry: String?
) : VehicleEvent(vehicleReg)

data class VehicleTelematicsUpdatedEvent(
    override val vehicleReg: VehicleId,
    val telematicsEnabled: Boolean
) : VehicleEvent(vehicleReg)

data class VehicleInitialCountryUpdatedEvent(
    override val vehicleReg: VehicleId,
    val initialCountry: String
) : VehicleEvent(vehicleReg)

data class LastVehiclePositionUpdatedEvent(
    override val vehicleReg: VehicleId,
    val coordinate: Coordinate,
    val country: String,
    val timestamp: Instant
) : VehicleEvent(vehicleReg)

data class VehicleCrossedBorderEvent(
    override val vehicleReg: VehicleId,
    val countryBeforeCrossing: String,
    val crossingTimestamp: Instant,
) : VehicleEvent(vehicleReg)

data class CrossingBorderConfirmedEvent(
    override val vehicleReg: VehicleId,
    val timestamp: Instant,
    val countryOut: String,
    val countryIn: String
) : VehicleEvent(vehicleReg)