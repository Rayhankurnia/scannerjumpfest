package com.example.scannerappjumptfest

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get_events.php")
    fun getEvents(): Call<EventResponse>

    @FormUrlEncoded
    @POST("validate_qr_event.php")
    fun validateQr(
        @Field("qr_Data") qrData: String,
        @Field("event_id") eventId: String
    ): Call<ValidationResponse>

    companion object {
        fun create(): ApiService {
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("http://jumpfest21.alwaysdata.net/jumpfest/api/") // ganti IP sesuai laptop
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
