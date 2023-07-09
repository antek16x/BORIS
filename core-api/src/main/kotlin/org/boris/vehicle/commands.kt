package org.boris.vehicle

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.Instant

data class AddNewVehicleCommand(
    @TargetAggregateIdentifier
    val vehicleReg: String,
    val telematicsEnabled: Boolean?
)

data class UpdateVehiclePositionCommand(
    @TargetAggregateIdentifier
    val vehicleReg: String,
    val isRunManually: Boolean,
    val coordinate: Coordinate?,
    val country: String?,
    val timestamp: Instant?
)

data class ConfirmCrossingBorderCommand(
    @TargetAggregateIdentifier
    val vehicleReg: String,
    val timestamp: Instant,
    val countryOut: String,
    val countryIn: String
)