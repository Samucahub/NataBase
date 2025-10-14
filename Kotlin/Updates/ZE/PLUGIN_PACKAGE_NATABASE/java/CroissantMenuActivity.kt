package com.example.natabaseprime

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class CroissantMenuActivity : AppCompatActivity() {

    // Top bar elements
    private lateinit var buttonEnviar: Button
    private lateinit var buttonRestart: Button
    private lateinit var buttonLogout: Button
    private lateinit var textClock: TextView

    // Product cards
    private lateinit var cardChocAvela: CardView
    private lateinit var cardSimples: CardView
    private lateinit var cardMulticereais: CardView
    private lateinit var cardMulticereaisMisto: CardView
    private lateinit var cardMisto: CardView
    private lateinit var cardPaoDesusMisto: CardView

    // Back button
    private lateinit var buttonRetroceder: Button

    // Clock handler
    private val clockHandler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.croissant_menu)

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Start clock
        startClock()
    }

    private fun initializeViews() {
        // Top bar buttons
        buttonEnviar = findViewById(R.id.buttonEnviar)
        buttonRestart = findViewById(R.id.buttonRestart)
        buttonLogout = findViewById(R.id.buttonLogout)
        textClock = findViewById(R.id.textClock)

        // Product cards
        cardChocAvela = findViewById(R.id.cardChocAvela)
        cardSimples = findViewById(R.id.cardSimples)
        cardMulticereais = findViewById(R.id.cardMulticereais)
        cardMulticereaisMisto = findViewById(R.id.cardMulticereaisMisto)
        cardMisto = findViewById(R.id.cardMisto)
        cardPaoDesusMisto = findViewById(R.id.cardPaoDesusMisto)

        // Back button
        buttonRetroceder = findViewById(R.id.buttonRetroceder)
    }

    private fun setupClickListeners() {
        // Top bar actions
        buttonEnviar.setOnClickListener {
            handleEnviar()
        }

        buttonRestart.setOnClickListener {
            handleRestart()
        }

        buttonLogout.setOnClickListener {
            handleLogout()
        }

        // Product cards
        cardChocAvela.setOnClickListener {
            handleProductClick("CHOC E AVELÃ")
        }

        cardSimples.setOnClickListener {
            handleProductClick("SIMPLES")
        }

        cardMulticereais.setOnClickListener {
            handleProductClick("MULTICEREAIS")
        }

        cardMulticereaisMisto.setOnClickListener {
            handleProductClick("MULTICEREAIS MISTO")
        }

        cardMisto.setOnClickListener {
            handleProductClick("MISTO")
        }

        cardPaoDesusMisto.setOnClickListener {
            handleProductClick("PÃO DE DEUS MISTO")
        }

        // Back button
        buttonRetroceder.setOnClickListener {
            handleBack()
        }
    }

    private fun startClock() {
        clockRunnable = object : Runnable {
            override fun run() {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                textClock.text = timeFormat.format(Date())
                clockHandler.postDelayed(this, 1000) // Update every second
            }
        }
        clockHandler.post(clockRunnable)
    }

    // ========== ACTION HANDLERS (For backend integration) ==========

    private fun handleEnviar() {
        // TODO: Implement send/confirm action
        Toast.makeText(this, "Enviar clicked", Toast.LENGTH_SHORT).show()
        
        // Your friends can add code here to:
        // - Send current order to server
        // - Confirm selections
        // - Navigate to confirmation screen
    }

    private fun handleRestart() {
        // TODO: Implement restart action
        Toast.makeText(this, "Restart clicked", Toast.LENGTH_SHORT).show()
        
        // Your friends can add code here to:
        // - Clear current selections
        // - Reset order state
        // - Return to initial state
    }

    private fun handleLogout() {
        // TODO: Implement logout action
        Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
        
        // Your friends can add code here to:
        // - Clear user session
        // - Navigate to login screen
        // - Save any pending data
        
        // Example: finish() or navigate to MainActivity
    }

    private fun handleProductClick(productName: String) {
        // TODO: Implement product selection
        Toast.makeText(this, "$productName selecionado", Toast.LENGTH_SHORT).show()
        
        // Your friends can add code here to:
        // - Navigate to product detail screen
        // - Open quantity selection dialog
        // - Add product to cart/order
        // - Pass product data to next screen
        
        // Example:
        // val intent = Intent(this, ProductDetailActivity::class.java)
        // intent.putExtra("PRODUCT_NAME", productName)
        // startActivity(intent)
    }

    private fun handleBack() {
        // TODO: Implement back navigation
        Toast.makeText(this, "Retroceder clicked", Toast.LENGTH_SHORT).show()
        
        // Your friends can add code here to:
        // - Navigate to previous category menu
        // - Return to main menu
        
        // Example:
        finish() // This will go back to the previous activity
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop clock updates when activity is destroyed
        clockHandler.removeCallbacks(clockRunnable)
    }
}
