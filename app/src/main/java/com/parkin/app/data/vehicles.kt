package com.parkin.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("vehicles")
data class Vehicle(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("license_plate")
    val licensePlate: String,

    @SerialName("brand_model")
    val brandModel: String,

    @SerialName("fuel_type")
    val fuelType: String = "Gasolina",

    @SerialName("is_primary")
    val isPrimary: Boolean = false
)