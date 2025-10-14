package com.example.natabaseprime

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class PaoMenuActivity : AppCompatActivity() {

    private val clockHandler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pao_menu)

        // Initialize views
        val buttonEnviar = findViewById<Button>(R.id.buttonEnviar)
        val buttonRestart = findViewById<Button>(R.id.buttonRestart)
        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        val buttonRetroceder = findViewById<Button>(R.id.buttonRetroceder)

        // Product cards
        val cardBaguete = findViewById<CardView>(R.id.cardBaguete)
        val cardBolaLenha = findViewById<CardView>(R.id.cardBolaLenha)
        val cardPaoCereais = findViewById<CardView>(R.id.cardPaoCereais)
        val cardPaoRusticoFatias = findViewById<CardView>(R.id.cardPaoRusticoFatias)

        // Set up top bar button handlers
        buttonEnviar.setOnClickListener {
            handleEnviar()
        }

        buttonRestart.setOnClickListener {
            handleRestart()
        }

        buttonLogout.setOnClickListener {
            handleLogout()
        }

        // Set up product card handlers
        cardBaguete.setOnClickListener {
            handleProductClick("Baguete")
        }

        cardBolaLenha.setOnClickListener {
            handleProductClick("Bola Lenha")
        }

        cardPaoCereais.setOnClickListener {
            handleProductClick("Pão Cereais")
        }

        cardPaoRusticoFatias.setOnClickListener {
            handleProductClick("Pão Rustico Fatias")
        }

        // Back button handler
        buttonRetroceder.setOnClickListener {
            handleBack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacks(clockRunnable)
    }

    // TODO: Implement this method to handle sending order/data
    private fun handleEnviar() {
        Toast.makeText(this, "Enviar clicked - TODO: Implement", Toast.LENGTH_SHORT).show()
        // TODO: Add your backend integration here
        // Example: Send current order to backend
    }

    // TODO: Implement this method to handle restart functionality
    private fun handleRestart() {
        Toast.makeText(this, "Restart clicked - TODO: Implement", Toast.LENGTH_SHORT).show()
        // TODO: Add your backend integration here
        // Example: Clear current session/order
    }

    // TODO: Implement this method to handle logout
    private fun handleLogout() {
        Toast.makeText(this, "Logout clicked - TODO: Implement", Toast.LENGTH_SHORT).show()
        // TODO: Add your backend integration here
        // Example: Clear user session and return to login
        // finish()
    }

    // TODO: Implement this method to handle product selection
    private fun handleProductClick(productName: String) {
        Toast.makeText(this, "Selected: $productName", Toast.LENGTH_SHORT).show()
        // TODO: Add your backend integration here
        // Example: Navigate to product detail page or add to order
    }

    // TODO: Implement this method to handle back navigation
    private fun handleBack() {
        Toast.makeText(this, "Going back - TODO: Implement", Toast.LENGTH_SHORT).show()
        // TODO: Add your navigation logic here
        // Example: Return to main menu
        finish()
    }
}
