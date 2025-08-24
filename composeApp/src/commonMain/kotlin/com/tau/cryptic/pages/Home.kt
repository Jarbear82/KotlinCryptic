package com.tau.cryptic.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tau.cryptic.components.DeletableSelectableListView
import org.tau.cryptic.ui.viewmodel.HomeViewModel

@Composable
fun Home(viewModel: HomeViewModel) {
    var newGraphName by remember { mutableStateOf("") }
    var newGraphPath by remember { mutableStateOf("") }
    val noteGraphs by viewModel.noteGraphs.collectAsState()
    val selectedNoteGraph by viewModel.selectedNoteGraph.collectAsState()

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
                value = newGraphName,
                onValueChange = { newGraphName = it },
                label = { Text("New graph name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = newGraphPath,
                onValueChange = { newGraphPath = it },
                label = { Text("Directory path") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newGraphName.isNotBlank() && newGraphPath.isNotBlank()) {
                        viewModel.createNoteGraph(newGraphName, newGraphPath)
                        newGraphName = ""
                        newGraphPath = ""
                    }
                },
                enabled = newGraphName.isNotBlank() && newGraphPath.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add graph")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DeletableSelectableListView(
            items = noteGraphs,
            selectedItem = selectedNoteGraph,
            onItemClick = { viewModel.selectNoteGraph(it) },
            onDeleteItemClick = { viewModel.removeNoteGraph(it) },
            modifier = Modifier.fillMaxSize()
        ) { item ->
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}