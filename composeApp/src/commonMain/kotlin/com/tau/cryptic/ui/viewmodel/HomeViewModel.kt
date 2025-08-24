package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.data.GraphRepository

class HomeViewModel(private val graphRepository: GraphRepository) : ViewModel() {

    val noteGraphs: StateFlow<List<NoteGraph>> = graphRepository.noteGraphs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val selectedNoteGraph: StateFlow<NoteGraph?> = graphRepository.selectedNoteGraph
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun createNoteGraph(name: String, filePath: String) {
        graphRepository.createNoteGraph(name, filePath)
    }

    fun addNoteGraph(name: String) {
        graphRepository.addNoteGraph(name)
    }

    fun removeNoteGraph(graph: NoteGraph) {
        graphRepository.removeNoteGraph(graph)
    }

    fun selectNoteGraph(graph: NoteGraph) {
        graphRepository.setSelectedNoteGraph(graph)
    }
}