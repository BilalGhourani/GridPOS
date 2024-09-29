package com.grid.pos.model

data class FilesListResult(
        var findSelected: Boolean = false,
        val filesList : MutableList<FileModel> = mutableListOf()
)