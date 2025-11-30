package com.example.scannerappjumpfest

data class TicketResponse(
    val status: Boolean,
    val message: String,
    val buyer: String?,
    val event: String?,
    val ticket_type: String?,
    val scan_time: String?
)
