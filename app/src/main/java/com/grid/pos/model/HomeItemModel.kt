package com.grid.pos.model

data class HomeItemModel(
        val icon: Int,
        val title: String,
        val composable: String,
)

data class HomeCategoryModel(
        val title: String,
        val items: List<HomeItemModel>,
)
