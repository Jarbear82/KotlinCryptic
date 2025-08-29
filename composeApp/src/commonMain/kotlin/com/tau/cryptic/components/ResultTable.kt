package com.tau.cryptic.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.tau.cryptic.data.QueryResult

@Composable
fun ResultTable(queryResult: QueryResult) {
    LazyColumn {
        // Header row
        item {
            Row {
                queryResult.columnTypes.keys.forEach { header ->
                    Text(header, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Data rows
        items(queryResult.rows) { row ->
            Row {
                row.values.forEach { cell ->
                    Text(cell.toString())
                }
            }
        }
    }
}