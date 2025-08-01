package org.tau.cryptic

import androidx.compose.runtime.mutableStateListOf
import org.tau.cryptic.components.Identifiable
import org.tau.cryptic.pages.*
import java.util.*

/**
 * A data class to hold all the components of a single graph.
 * This includes the schemas for nodes and edges, as well as the
 * actual graph instances (nodes and edges).
 *
 * It implements Identifiable to be used in DeletableSelectableListView.
 */
data class NoteGraph(
    override val id: String = UUID.randomUUID().toString(),
    var name: String = "My Awesome Graph",

    // Schema Definitions
    val nodeSchemas: MutableList<NodeSchema> = mutableStateListOf(),
    val edgeSchemas: MutableList<EdgeSchema> = mutableStateListOf(),

    // Graph Data
    val nodes: MutableList<GraphNode> = mutableStateListOf(),
    val edges: MutableList<GraphEdge> = mutableStateListOf()
) : Identifiable