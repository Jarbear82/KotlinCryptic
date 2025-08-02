package org.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.data.GraphRepository
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode

/**
 * A view model for the graph screen.
 */
class GraphViewModel(private val graphRepository: GraphRepository) : ViewModel() {

    val noteGraph: StateFlow<NoteGraph?> = graphRepository.selectedNoteGraph
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun onUpdateNode(node: GraphNode) {
        graphRepository.updateNode(node)
    }

    fun onUpdateEdge(edge: GraphEdge) {
        graphRepository.updateEdge(edge)
    }

    fun onCreateNode(node: GraphNode) {
        graphRepository.addNode(node)
    }

    fun onCreateEdge(edge: GraphEdge) {
        graphRepository.addEdge(edge)
    }
}