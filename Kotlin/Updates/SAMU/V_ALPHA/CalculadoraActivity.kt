package com.belasoft.natabase_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalculadoraActivity : AppCompatActivity() {

    private lateinit var etValor: EditText
    private lateinit var tvProduto: TextView
    private lateinit var tvTipo: TextView

    private var produtoNome: String = ""
    private var tipoOperacao: String = ""
    private var producaoIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora)

        produtoNome = intent.getStringExtra("PRODUTO_NOME") ?: ""
        tipoOperacao = intent.getStringExtra("TIPO_OPERACAO") ?: ""
        producaoIndex = intent.getIntExtra("PRODUCAO_INDEX", 1)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etValor = findViewById(R.id.etValor)
        tvProduto = findViewById(R.id.tvProduto)
        tvTipo = findViewById(R.id.tvTipo)

        tvProduto.text = "Produto: $produtoNome"
        tvTipo.text = "Tipo: $tipoOperacao"
        findViewById<TextView>(R.id.tvTitulo).text = "Calculadora - $tipoOperacao"
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn0).setOnClickListener { appendNumber("0") }
        findViewById<Button>(R.id.btn1).setOnClickListener { appendNumber("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { appendNumber("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { appendNumber("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { appendNumber("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { appendNumber("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { appendNumber("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { appendNumber("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { appendNumber("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { appendNumber("9") }

        findViewById<Button>(R.id.btnClear).setOnClickListener { clearInput() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }

        findViewById<Button>(R.id.btnCancelar).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnConfirmar).setOnClickListener { confirmar() }
    }

    private fun appendNumber(number: String) {
        val currentText = etValor.text.toString()
        if (currentText == "0") {
            etValor.setText(number)
        } else {
            etValor.setText(currentText + number)
        }
    }

    private fun clearInput() {
        etValor.setText("0")
    }

    private fun backspace() {
        val currentText = etValor.text.toString()
        if (currentText.isNotEmpty() && currentText != "0") {
            if (currentText.length == 1) {
                etValor.setText("0")
            } else {
                etValor.setText(currentText.substring(0, currentText.length - 1))
            }
        }
    }

    private fun confirmar() {
        val valor = etValor.text.toString().toIntOrNull() ?: 0

        val resultIntent = Intent().apply {
            putExtra("PRODUTO_NOME", produtoNome)
            putExtra("TIPO_OPERACAO", tipoOperacao)
            putExtra("VALOR", valor)
            putExtra("PRODUCAO_INDEX", producaoIndex)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}