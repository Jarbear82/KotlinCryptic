package com.tau.cryptic.ui.graph

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode

class LayoutManager {

    private val _graphVisualState = MutableStateFlow(GraphVisualState())
    val graphVisualState = _graphVisualState.asStateFlow()

    private val forceDirectedLayout = ForceDirectedLayout()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun addNode(node: GraphNode) {
        val currentNodes = _graphVisualState.value.nodes.toMutableList()
        currentNodes.add(node to Offset(0f, 0f))
        _graphVisualState.value = _graphVisualState.value.copy(nodes = currentNodes)
        runLayoutAlgorithm()
    }

    fun addEdge(edge: GraphEdge) {
        val currentEdges = _graphVisualState.value.edges.toMutableList()
        val sourceNode = _graphVisualState.value.nodes.first { it.first.id == edge.sourceNodeId }
        val targetNode = _graphVisualState.value.nodes.first { it.first.id == edge.targetNodeId }
        currentEdges.add(Triple(edge, sourceNode.second, targetNode.second))
        _graphVisualState.value = _graphVisualState.value.copy(edges = currentEdges)
        runLayoutAlgorithm()
    }

    fun updateNode(node: GraphNode) {
        val currentNodes = _graphVisualState.value.nodes.toMutableList()
        val index = currentNodes.indexOfFirst { it.first.id == node.id }
        if (index != -1) {
            currentNodes[index] = node to currentNodes[index].second
            _graphVisualState.value = _graphVisualState.value.copy(nodes = currentNodes)
        }
    }

    fun onNodeDrag(node: GraphNode, newX: Float, newY: Float) {
        val currentNodes = _graphVisualState.value.nodes.map {
            if (it.first.id == node.id) {
                it.first to Offset(newX, newY)
            } else {
                it
            }
        }
        val currentPinnedNodes = _graphVisualState.value.pinnedNodes + node.id
        _graphVisualState.value = _graphVisualState.value.copy(nodes = currentNodes, pinnedNodes = currentPinnedNodes)
        runLayoutAlgorithm()
    }

    fun onNodeDragEnd(node: GraphNode) {
        val currentPinnedNodes = _graphVisualState.value.pinnedNodes - node.id
        _graphVisualState.value = _graphVisualState.value.copy(pinnedNodes = currentPinnedNodes)
    }

    private fun runLayoutAlgorithm() {
        coroutineScope.launch {
            val newPositions = forceDirectedLayout.calculateLayout(
                _graphVisualState.value.nodes.map { it.first },
                _graphVisualState.value.edges.map { it.first },
                _graphVisualState.value.pinnedNodes.associateWith { nodeId ->
                    _graphVisualState.value.nodes.first { it.first.id == nodeId }.second
                }
            )
            val newNodes = _graphVisualState.value.nodes.map { (node, _) ->
                node to (newPositions[node.id] ?: Offset(0f, 0f))
            }
            _graphVisualState.value = _graphVisualState.value.copy(nodes = newNodes)
        }
    }
}