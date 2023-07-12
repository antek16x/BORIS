package org.boris.core_api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.Instant

abstract class VehicleCommand(
    @TargetAggregateIdentifier
    open val vehicleReg: VehicleId
)

data class AddNewVehicleCommand(
    override val vehicleReg: VehicleId = VehicleId(),
    val telematicsEnabled: Boolean?
) : VehicleCommand(vehicleReg)

data class UpdateVehicleTelematicsCommand(
    override val vehicleReg: VehicleId,
    val telematicsEnabled: Boolean
) : VehicleCommand(vehicleReg)

data class UpdateVehiclePositionCommand(
    override val vehicleReg: VehicleId,
    val isRunManually: Boolean,
    val isUpdateManually: Boolean,
    val coordinate: Coordinate?,
    val country: String?,
    val timestamp: Instant?
) : VehicleCommand(vehicleReg)

data class ConfirmCrossingBorderCommand(
    override val vehicleReg: VehicleId,
    val timestamp: Instant,
    val countryOut: String,
    val countryIn: String
) : VehicleCommand(vehicleReg)