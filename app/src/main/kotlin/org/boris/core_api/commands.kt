package org.boris.core_api

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class VehicleCommand(
    @TargetAggregateIdentifier
    open val vehicleReg: VehicleId
)

data class AddNewVehicleCommand(
    override val vehicleReg: VehicleId = VehicleId(),
    val telematicsEnabled: Boolean?
) : VehicleCommand(vehicleReg)

//data class UpdateVehiclePositionCommand(
//    @TargetAggregateIdentifier
//    val vehicleReg: String,
//    val isRunManually: Boolean,
//    val coordinate: Coordinate?,
//    val country: String?,
//    val timestamp: Instant?
//)
//
//data class ConfirmCrossingBorderCommand(
//    @TargetAggregateIdentifier
//    val vehicleReg: String,
//    val timestamp: Instant,
//    val countryOut: String,
//    val countryIn: String
//)