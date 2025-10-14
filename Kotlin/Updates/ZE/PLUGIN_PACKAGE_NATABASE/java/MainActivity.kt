package com.example.natabaseprime

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoogleLogin: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menulogin_pro)
        
        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoogleLogin = findViewById(R.id.buttonGoogleLogin)
        
        // Set login button click listener
        buttonLogin.setOnClickListener {
            handleLogin()
        }
        
        // Set Google login button click listener (placeholder)
        buttonGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }
    }
    
    private fun handleLogin() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        
        // Basic validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome de utilizador", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Por favor, insira a password", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Implement actual authentication logic here
        // For now, just show a success message
        Toast.makeText(this, "Login bem-sucedido! Bem-vindo, $username", Toast.LENGTH_SHORT).show()
        
        // You can add navigation to another activity here
        // Example: startActivity(Intent(this, HomeActivity::class.java))
    }
    
    private fun handleGoogleLogin() {
        // TODO: Implement Google OAuth authentication
        Toast.makeText(
            this, 
            "Login com Google - funcionalidade em desenvolvimento", 
            Toast.LENGTH_SHORT
        ).show()
        
        // Future implementation:
        // 1. Set up Google Sign-In API
        // 2. Configure OAuth 2.0 credentials in Google Cloud Console
        // 3. Add Google Play Services dependency
        // 4. Implement GoogleSignInClient
    }
}