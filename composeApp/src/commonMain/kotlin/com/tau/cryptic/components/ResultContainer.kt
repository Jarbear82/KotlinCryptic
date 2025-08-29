package com.tau.cryptic.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.tau.cryptic.data.QueryResult

enum class ResultView {
    GRAPH, TABLE, JSON
}

@Composable
fun ResultContainer(queryResult: QueryResult) {
    var currentView by remember { mutableStateOf(ResultView.GRAPH) }

    Column {
        // Buttons to switch between views
        Row {
            Button(onClick = { currentView = ResultView.GRAPH }) { Text("Graph") }
            Button(onClick = { currentView = ResultView.TABLE }) { Text("Table") }
            Button(onClick = { currentView = ResultView.JSON }) { Text("JSON") }
        }

        // Display the selected view
        when (currentView) {
            ResultView.GRAPH -> ResultGraph(queryResult)
            ResultView.TABLE -> ResultTable(queryResult)
            ResultView.JSON -> ResultJson(queryResult)
        }
    }
}