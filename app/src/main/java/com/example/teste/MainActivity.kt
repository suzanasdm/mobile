package com.example.teste

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.teste.model.*
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

        findViewById<Button>(R.id.btnIrCadastroCliente)
            .setOnClickListener { abrirCadastroCliente() }

        findViewById<Button>(R.id.btnIrCadastroItens)
            .setOnClickListener { abrirCadastroItensVenda() }

        findViewById<Button>(R.id.btnIrPedido)
            .setOnClickListener { abrirLancamentoPedido() }
    }

    private fun abrirCadastroCliente() {
        setContentView(R.layout.cadastro_cliente)

        val etNome = findViewById<EditText>(R.id.etNome)
        val etCpf = findViewById<EditText>(R.id.etCpf)

        findViewById<Button>(R.id.btnSalvar).setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cpf = etCpf.text.toString().trim()

            if (nome.isNotEmpty() && cpf.isNotEmpty()) {
                DataRepository.clientes.add(Cliente(nome, cpf))
                Toast.makeText(this, "Cliente salvo!", Toast.LENGTH_SHORT).show()
                abrirMenuPrincipal()
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirCadastroItensVenda() {
        setContentView(R.layout.cadastro_itens)

        val etCod = findViewById<EditText>(R.id.etCodigoItem)
        val etDesc = findViewById<EditText>(R.id.etDescricaoItem)
        val etVal = findViewById<EditText>(R.id.etValorUnitario)

        findViewById<Button>(R.id.btnSalvarItem).setOnClickListener {
            val cod = etCod.text.toString().toIntOrNull() ?: 0
            val desc = etDesc.text.toString().trim()
            val valor = etVal.text.toString().toDoubleOrNull() ?: 0.0

            if (cod > 0 && desc.isNotEmpty() && valor > 0) {
                DataRepository.produtos.add(ItemVenda(cod, desc, valor))
                Toast.makeText(this, "Item salvo!", Toast.LENGTH_SHORT).show()
                abrirMenuPrincipal()
            } else {
                Toast.makeText(this, "Dados inválidos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirLancamentoPedido() {
        setContentView(R.layout.lancamento_pedido)

        if (DataRepository.clientes.isEmpty()) {
            Toast.makeText(this, "Cadastre um cliente primeiro!", Toast.LENGTH_SHORT).show()
            abrirMenuPrincipal()
            return
        }

        val novoId = (DataRepository.pedidos.lastOrNull()?.codigoPedido ?: 1000) + 1
        pedidoAtual = Pedido(novoId, DataRepository.clientes[0])

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
        val lvItens = findViewById<ListView>(R.id.lvItensPedido)

        val adapter = ItemPedidoAdapter(this, pedidoAtual!!.itens)
        lvItens.adapter = adapter

        etCodPedido.setText(novoId.toString())

        // Spinner Cliente
        spCliente.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            DataRepository.clientes.map { it.nome }
        )

        // Spinner Item
        spItem.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            DataRepository.produtos.map { it.descricao }
        )

        // Auto preencher valor
        spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (DataRepository.produtos.isNotEmpty()) {
                    val produto = DataRepository.produtos[position]
                    etValUnit.setText(produto.valorUnitario.toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Adicionar item
        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            val qtd = etQtd.text.toString().toIntOrNull() ?: 0
            val valor = etValUnit.text.toString().toDoubleOrNull() ?: 0.0
            val pos = spItem.selectedItemPosition

            if (DataRepository.produtos.isEmpty()) {
                Toast.makeText(this, "Cadastre um item primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (qtd > 0 && valor > 0) {
                val produto = DataRepository.produtos[pos]

                pedidoAtual!!.itens.add(
                    ItemPedido(produto, qtd, valor)
                )

                adapter.notifyDataSetChanged()

                atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)

                etQtd.text.clear()
                etValUnit.text.clear()
            } else {
                Toast.makeText(this, "Quantidade e valor inválidos!", Toast.LENGTH_SHORT).show()
            }
        }

        // Pagamento
        rgPagamento.setOnCheckedChangeListener { _, id ->
            pedidoAtual?.isAVista = (id == R.id.rbAVista)
            llParcelas.visibility = if (id == R.id.rbAPrazo) View.VISIBLE else View.GONE
            atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)
        }

        // Concluir pedido
        findViewById<Button>(R.id.btnConcluirPedido).setOnClickListener {

            if (pedidoAtual!!.itens.isEmpty()) {
                Toast.makeText(this, "Adicione itens ao pedido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val clienteSelecionado = DataRepository.clientes[spCliente.selectedItemPosition]
            pedidoAtual!!.cliente = clienteSelecionado

            DataRepository.pedidos.add(pedidoAtual!!)

            Toast.makeText(
                this,
                "Pedido Nº ${pedidoAtual!!.codigoPedido} cadastrado!",
                Toast.LENGTH_LONG
            ).show()

            abrirMenuPrincipal()
        }

        // Buscar pedido
        findViewById<Button>(R.id.btnBuscarPedido).setOnClickListener {
            val codigo = etCodPedido.text.toString().toIntOrNull()

            if (codigo != null) {
                val pedido = DataRepository.buscarPedido(codigo)

                if (pedido != null) {
                    pedidoAtual = pedido
                    lvItens.adapter = ItemPedidoAdapter(this, pedidoAtual!!.itens)

                    atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)

                    Toast.makeText(this, "Pedido carregado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Pedido não encontrado!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun atualizarFinanceiro(
        tvRes: TextView,
        tvTot: TextView,
        rg: RadioGroup,
        etParc: EditText,
        tvLParc: TextView
    ) {
        val locale = Locale.getDefault()
        val bruto = pedidoAtual?.valorBruto ?: 0.0
        val total = if (pedidoAtual?.isAVista == true) bruto * 0.95 else bruto * 1.05

        tvRes.text = String.format(locale, "Itens: %d | Total: R$ %.2f", pedidoAtual?.totalItens, bruto)
        tvTot.text = String.format(locale, "TOTAL FINAL: R$ %.2f", total)

        if (rg.checkedRadioButtonId == R.id.rbAPrazo) {
            val n = etParc.text.toString().toIntOrNull() ?: 1

            if (n <= 0) {
                tvLParc.text = "Parcelas inválidas"
                return
            }

            val valorParcela = total / n
            var texto = ""

            for (i in 1..n) {
                texto += "Parcela $i: R$ ${String.format("%.2f", valorParcela)}\n"
            }

            tvLParc.text = texto
        } else {
            tvLParc.text = ""
        }
    }
}