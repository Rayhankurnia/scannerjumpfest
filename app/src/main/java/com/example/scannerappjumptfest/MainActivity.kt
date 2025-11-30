package com.example.scannerappjumpfest

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var btnScan: Button
    private lateinit var tvResult: TextView
    private lateinit var progressBar: ProgressBar

    private val api = RetrofitClient.instance.create(ApiService::class.java)
    private val SCANNER_TOKEN = "JUMPFEST_SCANNER_2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        tvResult = findViewById(R.id.tvResult)
        progressBar = findViewById(R.id.progressBar)

        btnScan.setOnClickListener { startScan() }
    }

    private fun startScan() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Arahkan kamera ke QR Tiket JumpFest")
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            validateTicket(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun validateTicket(qrData: String) {
        progressBar.visibility = View.VISIBLE

        val body = mapOf(
            "qr_code" to qrData,
            "token" to SCANNER_TOKEN
        )

        api.scanTicket(body).enqueue(object : Callback<TicketResponse> {
            override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
                progressBar.visibility = View.GONE

                val res = response.body()
                if (res == null) {
                    showDialog("⚠️ Error", "Server tidak memberikan data.")
                    return
                }

                if (res.status) {
                    val msg = """
                        Nama: ${res.buyer}
                        Event: ${res.event}
                        Tiket: ${res.ticket_type}
                        Scan: ${res.scan_time}
                    """.trimIndent()

                    showDialog("✅ Tiket Valid", msg)
                } else {
                    showDialog("❌ Tidak Valid", res.message)
                }
            }

            override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showDialog("❌ Koneksi Gagal", t.message ?: "Error tidak diketahui")
            }
        })
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
