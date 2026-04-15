package com.example.teste.repository

import com.example.teste.model.Cliente
import com.example.teste.model.ItemVenda
import com.example.teste.model.Pedido

object DataRepository {

    val clientes = mutableListOf<Cliente>()
    val produtos = mutableListOf<ItemVenda>()
    val pedidos = mutableListOf<Pedido>()

//     Auxiliar para busca de pedido (Regra 3)
    fun buscarPedido(codigo: Int): Pedido? {
        return pedidos.find { it.codigoPedido == codigo }
    }
}