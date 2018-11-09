package com.luca020400.amt2.classes

data class StopData(
    val line: String,
    val destination: String,
    val schedule: String,
    val remaining_time: String
)
