package com.tau.cryptic.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tau.cryptic.data.QueryResult
import kotlinx.serialization.json.Json

@Composable
fun ResultJson(queryResult: QueryResult) {
    val json = Json { prettyPrint = true }
    Text(json.encodeToString(QueryResult.serializer(), queryResult))
}