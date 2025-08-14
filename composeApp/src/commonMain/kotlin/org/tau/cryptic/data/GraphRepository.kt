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

    suspend fun createNoteGraph(name: String, filePath: String)
    suspend fun createInMemoryDatabase()
    suspend fun addNoteGraph(name: String)
    suspend fun removeNoteGraph(graph: NoteGraph)
    suspend fun setSelectedNoteGraph(graph: NoteGraph)

    // Node Schema Methods
    suspend fun addNodeSchema(schema: NodeSchema)
    suspend fun removeNodeSchema(schema: NodeSchema)
    suspend fun updateNodeSchema(updatedSchema: NodeSchema)

    // Edge Schema Methods
    suspend fun addEdgeSchema(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String)
    suspend fun removeEdgeSchema(schema: EdgeSchema)
    suspend fun updateEdgeSchema(updatedSchema: EdgeSchema)

    // Graph Element Methods
    suspend fun addNode(node: GraphNode)
    suspend fun addEdge(edge: GraphEdge)
    suspend fun updateNode(updatedNode: GraphNode)
    suspend fun updateEdge(updatedEdge: GraphEdge)
    suspend fun removeNode(node: GraphNode)
    suspend fun removeEdge(edge: GraphEdge)

    suspend fun executeQuery(query: String): List<Map<String, Any?>>
}