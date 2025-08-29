package com.tau.cryptic.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

@Serializable
data class QueryResult(
    @Serializable(with = ListSerializer(MapAnySerializer::class))
    val rows: List<Map<String, Any?>>,
    val columnTypes: Map<String, String>
)