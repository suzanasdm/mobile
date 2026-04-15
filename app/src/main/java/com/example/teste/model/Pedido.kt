package com.example.teste.model

data class Pedido(
    val codigoPedido: Int,
    val cliente: Cliente,
    val itens: MutableList<ItemPedido> = mutableListOf(),
    var isAVista: Boolean = true,
    var qtdParcelas: Int = 1
) {
    // Regra G: Total de itens
    val totalItens: Int
        get() = itens.sumOf { it.quantidade }

    // Soma bruta sem descontos/acréscimos
    val valorBruto: Double
        get() = itens.sumOf { it.subtotal }

    // Regra J: Valor Total com ajuste de 5%
    val valorTotalFinal: Double
        get() = if (isAVista) valorBruto * 0.95 else valorBruto * 1.05

    // Regra I: Valor de cada parcela
    val valorParcela: Double
        get() = valorTotalFinal / qtdParcelas
}
