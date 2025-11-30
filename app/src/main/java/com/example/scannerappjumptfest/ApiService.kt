package com.example.scannerappjumpfest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("scan_ticket.php")
    fun scanTicket(
        @Body payload: Map<String, String>
    ): Call<TicketResponse>

}
