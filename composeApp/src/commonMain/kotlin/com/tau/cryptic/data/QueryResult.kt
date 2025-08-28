package com.tau.cryptic.data

data class QueryResult(
    val columns: List<String>,
    val rows: List<List<Any?>>
)