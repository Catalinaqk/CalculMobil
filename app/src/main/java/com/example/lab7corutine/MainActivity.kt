package com.example.lab7corutine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridLayout = findViewById<GridLayout>(R.id.gridCifre)

        // Link-ul corect conform screenshot-ului tău din browser
        val baseUrl = "https://cti.ubm.ro/cmo/digits/"

        lifecycleScope.launch {
            Toast.makeText(this@MainActivity, "Încep descărcarea...", Toast.LENGTH_SHORT).show()

            for (i in 0..8) {
                // Fișierele sunt img0.jpg, img1.jpg...
                val imageName = "img$i.jpg"
                val fullUrl = baseUrl + imageName

                val imageView = createImageView()
                gridLayout.addView(imageView)

                // Descărcăm
                val bitmap = downloadImage(fullUrl)

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_delete)
                }
            }
        }
    }

    private fun createImageView(): ImageView {
        return ImageView(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 300
                height = 300
                setMargins(10, 10, 10, 10)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private suspend fun downloadImage(urlLink: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DEBUG_APP", "Incerc sa descarc: $urlLink")
                val url = URL(urlLink)
                val connection = url.openConnection() as HttpURLConnection

                // Truc: Unele servere resping Java, ne prefăcem că suntem un browser
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 5000
                connection.connect()

                if (connection.responseCode != 200) {
                    throw Exception("Cod Server: ${connection.responseCode}")
                }

                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DEBUG_APP", "Eroare: ${e.message}")

                // AFIȘĂM EROAREA PE ECRAN
                withContext(Dispatchers.Main) {
                    if (urlLink.contains("img0.jpg")) { // Arătăm eroarea doar o dată
                        showErrorDialog(e.message ?: "Eroare necunoscută")
                    }
                }
                null
            }
        }
    }

    private fun showErrorDialog(error: String) {
        AlertDialog.Builder(this)
            .setTitle("De ce nu merge?")
            .setMessage("Motivul erorii:\n$error")
            .setPositiveButton("OK", null)
            .show()
    }
}