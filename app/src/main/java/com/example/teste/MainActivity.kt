package com.example.teste

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Defina o layout para a tela de cadastro
        setContentView(R.layout.cadastro_cliente)

        // 2. Referencie os campos do seu XML
        val etNome = findViewById<EditText>(R.id.etNome)
        val etCpf = findViewById<EditText>(R.id.etCpf)
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)

        // 3. Lógica do botão Salvar
        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cpf = etCpf.text.toString().trim()

            // Regra 1: Validar se os campos não estão nulos/vazios
            if (nome.isEmpty() || cpf.isEmpty()) {
                Toast.makeText(this, "Erro: preencha Nome e CPF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Regra: Validar tamanho mínimo do CPF (exemplo de validação simples)
            if (cpf.length < 11) {
                Toast.makeText(this, "Erro: CPF inválido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Regra 5: Mensagem de sucesso
            Toast.makeText(this, "Cliente $nome cadastrado com sucesso!", Toast.LENGTH_LONG).show()

            // Regra 6: Limpar os campos após salvar
            etNome.text.clear()
            etCpf.text.clear()
        }
    }
}