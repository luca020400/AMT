package com.luca020400.amt2.classes

data class Stop(
    val code: String,
    val name: String?,
    val stops: List<StopData>
)
