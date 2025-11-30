package com.example.scannerappjumptfest

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var btnScan: Button
    private lateinit var tvResult: TextView
    private lateinit var spinnerEvent: Spinner
    private lateinit var progressBar: ProgressBar

    private val api by lazy { ApiService.create() }
    private var selectedEventId: String? = null
    private var eventsList = listOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        tvResult = findViewById(R.id.tvResult)
        spinnerEvent = findViewById(R.id.spinnerEvent)
        progressBar = findViewById(R.id.progressBar)

        loadEvents()

        btnScan.setOnClickListener {
            if (selectedEventId == null) {
                Toast.makeText(this, "⚠️ Pilih event terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Arahkan kamera ke QR Tiket JumpFest")
            integrator.setOrientationLocked(false)
            integrator.setBeepEnabled(true)
            integrator.initiateScan()
        }
    }

    /** 🔹 Fungsi untuk memuat daftar event dari server */
    private fun loadEvents() {
        api.getEvents().enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val events = response.body()?.events ?: emptyList()

                    // 🔹 Tambahkan deklarasi adapter di sini
                    val names = events.map { "${it.nama_event} (${it.tanggal_event})" }
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    spinnerEvent.adapter = adapter  // ✅ ini yang akan menghubungkan ke spinner

                    spinnerEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedEventId = events[position].id
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            selectedEventId = null
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal memuat event!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /** 🔹 Fungsi pemrosesan hasil scan QR */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null && selectedEventId != null) {
            validateTicket(result.contents, selectedEventId!!)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /** 🔹 Validasi tiket ke server */
    private fun validateTicket(qrData: String, eventId: String) {
        progressBar.visibility = View.VISIBLE
        api.validateQr(qrData, eventId).enqueue(object : Callback<ValidationResponse> {
            override fun onResponse(call: Call<ValidationResponse>, response: Response<ValidationResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        showResultDialog("✅ Tiket Valid", "Nama: ${result.nama}\nNIK: ${result.nik}\nEvent: ${result.event}")
                    } else {
                        showResultDialog("❌ Tiket Tidak Valid", result.message)
                    }
                } else {
                    showResultDialog("⚠️ Error", "Server tidak merespons dengan benar.")
                }
            }

            override fun onFailure(call: Call<ValidationResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showResultDialog("❌ Koneksi Gagal", "Periksa koneksi internet atau server Anda.\n${t.message}")
            }
        })
    }

    /** 🔹 Tampilkan dialog hasil */
    private fun showResultDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
