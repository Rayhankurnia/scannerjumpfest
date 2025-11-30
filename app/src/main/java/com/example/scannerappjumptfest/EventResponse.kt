package com.example.scannerappjumptfest

data class EventResponse(
    val success: Boolean,
    val events: List<Event>
)

data class Event(
    val id: String,
    val nama_event: String,
    val tanggal_event: String,
    val lokasi: String
)
