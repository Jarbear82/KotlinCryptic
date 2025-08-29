package com.tau.cryptic.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

object ListOfMapAnySerializer : KSerializer<List<Map<String, Any?>>> by ListSerializer(MapAnySerializer)

@Serializable
data class QueryResult(
    @Serializable(with = ListOfMapAnySerializer::class)
    val rows: List<Map<String, Any?>>,
    val columnTypes: Map<String, String>
)