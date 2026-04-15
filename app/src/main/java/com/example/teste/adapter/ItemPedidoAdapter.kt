package com.example.teste

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.teste.model.ItemPedido

class ItemPedidoAdapter(context: Context, val itens: List<ItemPedido>) :
    ArrayAdapter<ItemPedido>(context, 0, itens) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            // Infla um layout simples de linha (pode usar o padrão do Android para teste)
            itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        }

        val item = getItem(position)
        val text1 = itemView!!.findViewById<TextView>(android.R.id.text1)
        val text2 = itemView.findViewById<TextView>(android.R.id.text2)

        text1.text = "${item?.produto?.descricao} (x${item?.quantidade})"
        text2.text = "Subtotal: R$ ${String.format("%.2f", item?.subtotal)}"

        return itemView
    }
}