package com.example.teste.model

data class Pedido(
    val codigoPedido: Int,
    var cliente: Cliente,
    val itens: MutableList<ItemPedido> = mutableListOf(),
    var isAVista: Boolean = true,
    var qtdParcelas: Int = 1
) {

    val totalItens: Int
        get() = itens.sumOf { it.quantidade }


    val valorBruto: Double
        get() = itens.sumOf { it.subtotal }


    val valorTotalFinal: Double
        get() = if (isAVista) valorBruto * 0.95 else valorBruto * 1.05

    val valorParcela: Double
        get() = valorTotalFinal / qtdParcelas
}
