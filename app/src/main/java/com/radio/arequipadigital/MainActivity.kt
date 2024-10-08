package com.radio.arequipadigital

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.radio.arequipadigital.R
import pl.droidsonroids.gif.GifImageView

class   MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var selectedRadioUrl: String? = null
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var bottomNavigationView: BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        playButton = findViewById<ImageButton>(R.id.buttton_play)
        pauseButton = findViewById<ImageButton>(R.id.button_pause)
        bottomNavigationView = findViewById(R.id.bottom_navigation)


        // Cargar el estado guardado del reproductor (persistencia entre sesiones)
        loadPlayerState()

        // Cargar la estación de radio predeterminada pero no reproducirla aún
        loadDefaultRadio()

        // Solicitar permisos de notificación si es necesario
        requestNotificationPermission()



        playButton.setOnClickListener {
            if (selectedRadioUrl != null) {
                toggleRadioService(!isPlaying)
            } else {
                Toast.makeText(this, "Seleccione una estación de radio primero", Toast.LENGTH_SHORT).show()
            }
        }
        // Manejamos el click del botón pausa
        pauseButton.setOnClickListener {
            if (selectedRadioUrl != null) {
                toggleRadioService(false)  // Pausa la reproducción
            } else {
                Toast.makeText(this, "Seleccione una estación de radio primero", Toast.LENGTH_SHORT).show()
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_select_radio -> {
                    // Código para manejar la selección de radio
                    val intent = Intent(this, RadioSelectionActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_favorites -> {
                    // Código para manejar la vista de Favoritos
                    Toast.makeText(this, "Favoritos seleccionados", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    // Código para manejar la Configuración
                    Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }


    }

    private fun loadPlayerState() {
        val sharedPreferences = getSharedPreferences("RadioPreferences", MODE_PRIVATE)
        selectedRadioUrl = sharedPreferences.getString("selectedRadioUrl", null)
        isPlaying = sharedPreferences.getBoolean("isPlaying", false)
        updateUI(isPlaying)
    }

    private fun savePlayerState() {
        val sharedPreferences = getSharedPreferences("RadioPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selectedRadioUrl", selectedRadioUrl)
            putBoolean("isPlaying", isPlaying)
            apply()
        }
    }
    private fun loadDefaultRadio() {
        val db = FirebaseFirestore.getInstance()
        db.collection("defaultRadio").document("default")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    selectedRadioUrl = document.getString("url")
                    val radioName = document.getString("name")
                    Toast.makeText(this, "Estación predeterminada cargada: $radioName", Toast.LENGTH_SHORT).show()
                    updateUI(false)
                } else {
                    Toast.makeText(this, "No se encontró la estación predeterminada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener la estación predeterminada", Toast.LENGTH_SHORT).show()
            }
    }

    // Manejo del resultado de la actividad de selección de radio
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            selectedRadioUrl = data?.getStringExtra("selected_radio_url")
            if (selectedRadioUrl != null) {
                savePlayerState()  // Guardar el estado tras seleccionar la estación
                Toast.makeText(this, "Estación seleccionada", Toast.LENGTH_SHORT).show()
                updateUI(false)
            }
        }
    }

    private fun startRadioService(action: String) {
        val intent = Intent(this, RadioService::class.java).apply {
            this.action = action
            this.putExtra("radio_url", selectedRadioUrl)
        }
        startService(intent)
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
    // Método para reproducir o pausar la radio
    private fun toggleRadioService(play: Boolean) {
        val action = if (play) "ACTION_PLAY" else "ACTION_STOP"
        val intent = Intent(this, RadioService::class.java).apply {
            this.action = action
            this.putExtra("radio_url", selectedRadioUrl)
        }

        if (play) {
            startService(intent)
            updateUI(true)
        } else {
            stopService(intent)
            updateUI(false)
        }
        savePlayerState()  // Guardar el estado cada vez que cambia
    }
    // Actualizar la UI según el estado de reproducción
    private fun updateUI(isPlaying: Boolean) {
        this.isPlaying = isPlaying

        // Cambiar visibilidad de los botones
        if (isPlaying) {
            playButton.visibility = View.GONE  // Ocultar el botón de play
            pauseButton.visibility = View.VISIBLE  // Mostrar el botón de pausa
        } else {
            playButton.visibility = View.VISIBLE  // Mostrar el botón de play
            pauseButton.visibility = View.GONE  // Ocultar el botón de pausa
        }



    }





}