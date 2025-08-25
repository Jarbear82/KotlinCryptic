package com.tau.cryptic.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tau.cryptic.components.DeletableSelectableListView
import com.tau.cryptic.components.DirectoryPicker
import com.tau.cryptic.ui.viewmodel.HomeViewModel

@Composable
fun Home(viewModel: HomeViewModel) {
    val noteGraphs by viewModel.noteGraphs.collectAsState()
    val selectedNoteGraph by viewModel.selectedNoteGraph.collectAsState()
    var showDirectoryPicker by remember { mutableStateOf(false) }
    var isCreatingNew by remember { mutableStateOf(false) }


    DirectoryPicker(showDirectoryPicker) { path ->
        showDirectoryPicker = false
        if (path != null) {
            if (isCreatingNew) {
                viewModel.createNoteGraph(path)
            } else {
                viewModel.addNoteGraph(path)
            }
        }
    }

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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                isCreatingNew = false
                showDirectoryPicker = true
            }) {
                Text("Open Graph")
            }
            Button(onClick = {
                isCreatingNew = true
                showDirectoryPicker = true
            }) {
                Text("New Graph")
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
                text = item.filePath,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}