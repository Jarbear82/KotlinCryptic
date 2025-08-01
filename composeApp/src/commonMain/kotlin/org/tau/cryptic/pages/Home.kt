package org.tau.cryptic.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tau.cryptic.Config
// import org.tau.cryptic.NoteGraph
import org.tau.cryptic.components.DeletableSelectableListView

@Composable
fun Home() {
    var newItemText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My NoteGraphs",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("New graph name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newItemText.isNotBlank()) {
                        Config.addNoteGraph(newItemText)
                        newItemText = ""
                    }
                },
                enabled = newItemText.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add graph")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DeletableSelectableListView(
            items = Config.noteGraphs,
            selectedItem = Config.selectedNoteGraph,
            onItemClick = { Config.selectedNoteGraph = it },
            onDeleteItemClick = { Config.removeNoteGraph(it) },
            modifier = Modifier.fillMaxSize()
        ) { item ->
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}