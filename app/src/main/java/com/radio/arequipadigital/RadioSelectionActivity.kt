package com.radio.arequipadigital

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RadioSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var radioAdapter: RadioAdapter
    private lateinit var radioList: MutableList<RadioStation>
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_radio_selection)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        radioList = mutableListOf()

        radioAdapter = RadioAdapter(radioList) { station ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_radio_url", station.url)
            setResult(RESULT_OK, resultIntent)
            finish()  // Regresar a la actividad principal
        }
        recyclerView.adapter = radioAdapter

        db = FirebaseFirestore.getInstance()

        // Cargar las estaciones de radio desde Firebase
        db.collection("radioStations")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val radioStation = document.toObject(RadioStation::class.java)
                    radioList.add(radioStation)
                }
                radioAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Manejar errores de carga
            }
    }

}