package com.tau.cryptic.ui.graph

import androidx.compose.ui.geometry.Offset
import com.tau.cryptic.pages.GraphEdge
import com.tau.cryptic.pages.GraphNode

data class GraphVisualState(
    val nodes: List<Pair<GraphNode, Offset>> = emptyList(),
    val edges: List<Triple<GraphEdge, Offset, Offset>> = emptyList(),
    val pinnedNodes: Set<String> = emptySet()
)