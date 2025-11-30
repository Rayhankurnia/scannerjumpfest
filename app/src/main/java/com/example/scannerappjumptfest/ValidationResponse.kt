package com.example.scannerappjumptfest
data class ValidationResponse(
    val success: Boolean,
    val message: String,
    val nama: String?,
    val nik: String?,
    val event: String?
)
