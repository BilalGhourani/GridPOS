package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.Event
import com.grid.pos.model.FileModel

data class ReportsListState(
        val paySlips: MutableList<FileModel> = mutableListOf(),
        val payTickets: MutableList<FileModel> = mutableListOf(),
        var isLoading: Boolean = false,
        var isDone: Boolean = false,
        var clear: Boolean = false,
        var warning: Event<String>? = null,
) {
    fun getFileModels(type: String): MutableList<FileModel> {
       return when (type){
            ReportTypeEnum.PAY_SLIP.key->{
                paySlips
            }
           ReportTypeEnum.PAY_TICKET.key->{
               payTickets
            }

           else -> {
               paySlips
           }
       }
    }
}