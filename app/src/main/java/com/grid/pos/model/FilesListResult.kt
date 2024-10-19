package com.grid.pos.model

data class FilesListResult(
        var findSelected: MutableMap<String,Boolean> = mutableMapOf(),
        val filesList : MutableList<FileModel> = mutableListOf()
)