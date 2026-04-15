package com.example.teste.model

data class ItemPedido(
    val produto: ItemVenda,
    val quantidade: Int,
    val valorVenda: Double // Valor retornado do item no momento da seleção
) {
    val subtotal: Double
        get() = quantidade * valorVenda
}