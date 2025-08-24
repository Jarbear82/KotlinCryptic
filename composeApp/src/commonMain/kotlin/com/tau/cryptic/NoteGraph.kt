package com.tau.cryptic

import androidx.compose.runtime.mutableStateListOf
import com.tau.cryptic.components.Identifiable
import com.tau.cryptic.pages.*
import kotlin.uuid.Uuid

/**
 * A data class to hold all the components of a single graph.
 * This includes the schemas for nodes and edges, as well as the
 * actual graph instances (nodes and edges).
 *
 * It implements Identifiable to be used in DeletableSelectableListView.
 */
@kotlin.uuid.ExperimentalUuidApi
data class NoteGraph(
    override val id: String = Uuid.random().toString(),
    var name: String = "My Awesome Graph",
    var filePath: String, // Path to the KuzuDB file

    // Schema Definitions
    val nodeSchemas: MutableList<NodeSchema> = mutableStateListOf(),
    val edgeSchemas: MutableList<EdgeSchema> = mutableStateListOf(),

    // Graph Data
    val nodes: MutableList<GraphNode> = mutableStateListOf(),
    val edges: MutableList<GraphEdge> = mutableStateListOf()
) : Identifiable