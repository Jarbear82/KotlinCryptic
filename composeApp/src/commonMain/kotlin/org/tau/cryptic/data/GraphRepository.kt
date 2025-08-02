package org.tau.cryptic.data

import kotlinx.coroutines.flow.Flow
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.pages.*

/**
 * A repository to manage the graph data.
 */
interface GraphRepository {
    val noteGraphs: Flow<List<NoteGraph>>
    val selectedNoteGraph: Flow<NoteGraph?>

    fun addNoteGraph(name: String)
    fun removeNoteGraph(graph: NoteGraph)
    fun setSelectedNoteGraph(graph: NoteGraph)

    // Node Schema Methods
    fun addNodeSchema(schema: NodeSchema)
    fun removeNodeSchema(schema: NodeSchema)
    fun updateNodeSchema(updatedSchema: NodeSchema)

    // Edge Schema Methods
    fun addEdgeSchema(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String)
    fun removeEdgeSchema(schema: EdgeSchema)
    fun updateEdgeSchema(updatedSchema: EdgeSchema)

    // Graph Element Methods
    fun addNode(node: GraphNode)
    fun addEdge(edge: GraphEdge)
    fun updateNode(updatedNode: GraphNode)
    fun updateEdge(updatedEdge: GraphEdge)
}