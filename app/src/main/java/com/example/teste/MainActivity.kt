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

        findViewById<Button>(R.id.btnIrCadastroCliente).setOnClickListener { abrirCadastroCliente() }
        findViewById<Button>(R.id.btnIrCadastroItens).setOnClickListener { abrirCadastroItensVenda() }
        findViewById<Button>(R.id.btnIrPedido).setOnClickListener { abrirLancamentoPedido() }

        val tvUltimos = findViewById<TextView>(R.id.tvUltimosPedidos)
        if (DataRepository.pedidos.isEmpty()) {
            tvUltimos?.text = "Nenhum pedido cadastrado."
        } else {
            var resumo = ""
            DataRepository.pedidos.reversed().forEach { p ->
                val total = if (p.isAVista) p.valorBruto * 0.95 else p.valorBruto * 1.05
                resumo += "Pedido #${p.codigoPedido} | Total: R$ ${String.format("%.2f", total)}\n"
            }
            tvUltimos?.text = resumo
        }
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
            }
        }

        findViewById<Button>(R.id.btnVoltar)?.setOnClickListener { abrirMenuPrincipal() }
    }


    private fun abrirCadastroItensVenda() {
        setContentView(R.layout.cadastro_itens)

        val etCod = findViewById<EditText>(R.id.etCodigoItem)
        val etDesc = findViewById<EditText>(R.id.etDescricaoItem)
        val etVal = findViewById<EditText>(R.id.etValorUnitario)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarItem)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarItem) // Novo botão

        btnSalvar.setOnClickListener {
            val cod = etCod.text.toString().toIntOrNull() ?: 0
            val desc = etDesc.text.toString().trim()
            val valor = etVal.text.toString().toDoubleOrNull() ?: 0.0

            if (cod > 0 && desc.isNotEmpty() && valor > 0) {
                DataRepository.produtos.add(ItemVenda(cod, desc, valor))
                Toast.makeText(this, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                abrirMenuPrincipal()
            } else {
                Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
            }
        }

        btnVoltar.setOnClickListener {
            abrirMenuPrincipal()
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
        val btnVoltar1 = findViewById<Button>(R.id.btnVoltar1)
        val adapter = ItemPedidoAdapter(this, pedidoAtual!!.itens)
        lvItens.adapter = adapter
        etCodPedido.setText(novoId.toString())

        spCliente.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DataRepository.clientes.map { it.nome })
        spItem.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, DataRepository.produtos.map { it.descricao })


        spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(DataRepository.produtos.isNotEmpty()) etValUnit.setText(DataRepository.produtos[pos].valorUnitario.toString())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            val qtd = etQtd.text.toString().toIntOrNull() ?: 0
            val vUnit = etValUnit.text.toString().toDoubleOrNull() ?: 0.0
            if (qtd > 0 && vUnit > 0) {
                val produto = DataRepository.produtos[spItem.selectedItemPosition]
                pedidoAtual!!.itens.add(ItemPedido(produto, qtd, vUnit))
                adapter.notifyDataSetChanged()
                atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)
                etQtd.text.clear()
            }
        }

        rgPagamento.setOnCheckedChangeListener { _, id ->
            pedidoAtual?.isAVista = (id == R.id.rbAVista)
            llParcelas.visibility = if (id == R.id.rbAPrazo) View.VISIBLE else View.GONE
            atualizarFinanceiro(tvResumo, tvTotalFinal, rgPagamento, etNumParcelas, tvListaParcelas)
        }

        findViewById<Button>(R.id.btnConcluirPedido).setOnClickListener {
            if (pedidoAtual!!.itens.isEmpty()) {
                Toast.makeText(this, "Adicione itens!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pedidoAtual!!.cliente = DataRepository.clientes[spCliente.selectedItemPosition]
            DataRepository.pedidos.add(pedidoAtual!!)
            Toast.makeText(this, "Pedido concluído!", Toast.LENGTH_SHORT).show()
            abrirMenuPrincipal()
        }
        btnVoltar1.setOnClickListener {
            abrirMenuPrincipal()
        }
    }

    private fun atualizarFinanceiro(tvRes: TextView, tvTot: TextView, rg: RadioGroup, etParc: EditText, tvLParc: TextView) {
        val bruto = pedidoAtual?.valorBruto ?: 0.0
        val total = if (pedidoAtual?.isAVista == true) bruto * 0.95 else bruto * 1.05
        tvRes.text = String.format("Itens: %d | Total: R$ %.2f", pedidoAtual?.totalItens, bruto)
        tvTot.text = String.format("TOTAL FINAL: R$ %.2f", total)

        if (rg.checkedRadioButtonId == R.id.rbAPrazo) {
            val n = etParc.text.toString().toIntOrNull() ?: 1
            val vParc = total / n
            var txt = ""
            for (i in 1..n) txt += "Parcela $i: R$ ${String.format("%.2f", vParc)}\n"
            tvLParc.text = txt
        } else { tvLParc.text = "" }
    }
}