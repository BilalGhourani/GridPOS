package com.grid.pos.model

sealed class Resource {

    class Success(val data: MutableList<Any>) : Resource()
    class Failed(val message: String) : Resource()
    class Loading : Resource()
}