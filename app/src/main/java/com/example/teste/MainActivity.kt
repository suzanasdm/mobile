package com.example.teste

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.teste.model.Cliente
import com.example.teste.model.ItemPedido
import com.example.teste.model.ItemVenda
import com.example.teste.model.Pedido
import com.example.teste.repository.DataRepository
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var pedidoAtual: Pedido? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        abrirMenuPrincipal()
    }

    private fun abrirMenuPrincipal() {
        setContentView(R.layout.dashboard)
        findViewById<Button>(R.id.btnIrCadastroCliente)?.setOnClickListener { abrirCadastroCliente() }
        findViewById<Button>(R.id.btnIrCadastroItens)?.setOnClickListener { abrirCadastroItensVenda() }
        findViewById<Button>(R.id.btnIrPedido)?.setOnClickListener { abrirLancamentoPedido() }
    }

    private fun abrirCadastroCliente() {
        setContentView(R.layout.cadastro_cliente)
        val etNome = findViewById<EditText>(R.id.etNome)
        val etCpf = findViewById<EditText>(R.id.etCpf)
        findViewById<Button>(R.id.btnSalvar)?.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cpf = etCpf.text.toString().trim()
            if (nome.isNotEmpty() && cpf.isNotEmpty()) {
                DataRepository.clientes.add(Cliente(nome, cpf))
                Toast.makeText(this, "Cliente $nome salvo!", Toast.LENGTH_SHORT).show()
                abrirMenuPrincipal()
            }
        }
    }

    private fun abrirCadastroItensVenda() {
        setContentView(R.layout.cadastro_itens)
        val etCod = findViewById<EditText>(R.id.etCodigoItem)
        val etDesc = findViewById<EditText>(R.id.etDescricaoItem)
        val etVal = findViewById<EditText>(R.id.etValorUnitario)
        findViewById<Button>(R.id.btnSalvarItem)?.setOnClickListener {
            val cod = etCod.text.toString().toIntOrNull() ?: 0
            val desc = etDesc.text.toString().trim()
            val valor = etVal.text.toString().toDoubleOrNull() ?: 0.0
            if (cod > 0 && desc.isNotEmpty() && valor > 0) {
                DataRepository.produtos.add(ItemVenda(cod, desc, valor))
                Toast.makeText(this, "Item $desc salvo!", Toast.LENGTH_SHORT).show()
                abrirMenuPrincipal()
            }
        }
    }

    private fun abrirLancamentoPedido() {
        setContentView(R.layout.lancamento_pedido)

        // Gerar ID Automático (Requisito 4)
        val novoId = (DataRepository.pedidos.lastOrNull()?.codigoPedido ?: 1000) + 1
        pedidoAtual = Pedido(codigoPedido = novoId, cliente = Cliente("Consumidor", ""))

        // Mapeamento de IDs conforme seu novo XML
        val etCodPedido = findViewById<EditText>(R.id.etCodigoPedido)
        val spCliente = findViewById<Spinner>(R.id.spCliente)
        val spItem = findViewById<Spinner>(R.id.spItem)
        val etQtd = findViewById<EditText>(R.id.etQuantidade)
        val etValUnit = findViewById<EditText>(R.id.etValorUnitarioPedido)
        val tvResumo = findViewById<TextView>(R.id.tvResumoItens)
        val tvTotalFinal = findViewById<TextView>(R.id.tvValorTotalFinal)
        val rgPagamento = findViewById<RadioGroup>(R.id.rgPagamento)
        val llParcelas = findViewById<LinearLayout>(R.id.llParcelas)
        val etNumParcelas = findViewById<EditText>(R.id.etNumParcelas)
        val tvListaParcelas = findViewById<TextView>(R.id.tvListaParcelas)

        // Preencher Spinners (Requisito D e E)
        spCliente.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DataRepository.clientes.map { it.nome })
        spItem.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DataRepository.produtos.map { it.descricao })

        etCodPedido.setText(novoId.toString())

        // Botão Adicionar Item (Requisito E, F, G)
        findViewById<Button>(R.id.btnAddItem)?.setOnClickListener {
            val qtd = etQtd.text.toString().toIntOrNull() ?: 0
            val valor = etValUnit.text.toString().toDoubleOrNull() ?: 0.0
            val pos = spItem.selectedItemPosition

            if (pos >= 0 && qtd > 0 && valor > 0) {
                val prod = DataRepository.produtos[pos]
                pedidoAtual!!.itens.add(ItemPedido(prod, qtd, valor))

                // Atualiza Interface e Limpa Campos (Requisito 6)
                atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)
                etQtd.text.clear()
                etValUnit.text.clear()
            } else {
                Toast.makeText(this, "Informe quantidade e valor > 0", Toast.LENGTH_SHORT).show()
            }
        }

        // Lógica de Condição de Pagamento (Requisito I, J)
        rgPagamento.setOnCheckedChangeListener { _, id ->
            pedidoAtual?.isAVista = (id == R.id.rbAVista)
            llParcelas.visibility = if (id == R.id.rbAPrazo) View.VISIBLE else View.GONE
            atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)
        }

        // Botão Concluir (Requisito K, 5, 6)
        findViewById<Button>(R.id.btnConcluirPedido)?.setOnClickListener {
            if (pedidoAtual!!.itens.isNotEmpty()) {
                DataRepository.pedidos.add(pedidoAtual!!)
                Toast.makeText(this, "Pedido Nº ${pedidoAtual!!.codigoPedido} cadastrado com sucesso!", Toast.LENGTH_LONG).show()
                abrirMenuPrincipal()
            }
        }
    }

    private fun atualizarFinanceiro(tvRes: TextView, tvTot: TextView, rg: RadioGroup, etParc: EditText, tvLParc: TextView) {
        val locale = Locale.getDefault()
        val bruto = pedidoAtual?.valorBruto ?: 0.0
        val totalComAjuste = if (pedidoAtual?.isAVista == true) bruto * 0.95 else bruto * 1.05

        tvRes.text = String.format(locale, "Itens: %d | Total Bruto: R$ %.2f", pedidoAtual?.totalItens, bruto)
        tvTot.text = String.format(locale, "TOTAL FINAL: R$ %.2f", totalComAjuste)

        if (rg.checkedRadioButtonId == R.id.rbAPrazo) {
            val n = etParc.text.toString().toIntOrNull() ?: 1
            val vParc = totalComAjuste / n
            var txtP = ""
            for (i in 1..n) txtP += "Parcela $i: R$ ${String.format("%.2f", vParc)}\n"
            tvLParc.text = txtP
        } else {
            tvLParc.text = ""
        }
    }
}