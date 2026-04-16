package com.example.teste.model

data class ItemPedido(
    val produto: ItemVenda,
    val quantidade: Int,
    val valorVenda: Double
) {
    val subtotal: Double
        get() = quantidade * valorVenda
}