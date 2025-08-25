package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tau.cryptic.NoteGraph
import com.tau.cryptic.data.GraphRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


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

    fun createNoteGraph(filePath: String) {
        graphRepository.createNoteGraph(filePath)
    }

    fun addNoteGraph(filePath: String) {
        graphRepository.addNoteGraph(filePath)
    }

    fun removeNoteGraph(graph: NoteGraph) {
        graphRepository.removeNoteGraph(graph)
    }

    fun selectNoteGraph(graph: NoteGraph) {
        graphRepository.setSelectedNoteGraph(graph)
    }
}