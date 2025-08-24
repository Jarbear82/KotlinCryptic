package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.tau.cryptic.NoteGraph
import com.tau.cryptic.data.GraphRepository
import com.tau.cryptic.pages.GraphEdge
import com.tau.cryptic.pages.GraphNode
import com.tau.cryptic.ui.graph.GraphVisualState
import com.tau.cryptic.ui.graph.LayoutManager

/**
 * A view model for the graph screen.
 */

class GraphViewModel(
    private val graphRepository: GraphRepository,
    private val layoutManager: LayoutManager
) : ViewModel() {

    val noteGraph: StateFlow<NoteGraph?> = graphRepository.selectedNoteGraph
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val graphVisualState: StateFlow<GraphVisualState> = layoutManager.graphVisualState

    fun onUpdateNode(node: GraphNode) {
        graphRepository.updateNode(node)
        layoutManager.updateNode(node)
    }

    fun onUpdateEdge(edge: GraphEdge) {
        graphRepository.updateEdge(edge)
    }

    fun onCreateNode(node: GraphNode) {
        graphRepository.addNode(node)
        layoutManager.addNode(node)
    }

    fun onCreateEdge(edge: GraphEdge) {
        graphRepository.addEdge(edge)
        layoutManager.addEdge(edge)
    }

    fun onNodeDrag(node: GraphNode, newX: Float, newY: Float) {
        layoutManager.onNodeDrag(node, newX, newY)
    }

    fun onNodeDragEnd(node: GraphNode) {
        layoutManager.onNodeDragEnd(node)
    }

    fun onQueryResult(result: List<Map<String, Any?>>) {
        // TODO: Process and update the graph visual state based on the query result
    }
}