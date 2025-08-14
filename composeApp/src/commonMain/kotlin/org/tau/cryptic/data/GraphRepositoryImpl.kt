package org.tau.cryptic.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.tau.cryptic.KuzuDBService
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.pages.*
import java.io.File

/**
 * The default implementation of the graph repository.
 */
class GraphRepositoryImpl(private val kuzuDBService: KuzuDBService) : GraphRepository {

    private val _noteGraphs = MutableStateFlow<List<NoteGraph>>(emptyList())
    override val noteGraphs: Flow<List<NoteGraph>> = _noteGraphs.asStateFlow()

    private val _selectedNoteGraph = MutableStateFlow<NoteGraph?>(null)
    override val selectedNoteGraph: Flow<NoteGraph?> = _selectedNoteGraph.asStateFlow()

    override suspend fun createNoteGraph(name: String, filePath: String) {
        val fullPath = File(filePath, "$name.kuzudb").absolutePath
        kuzuDBService.initialize(fullPath)
        val newGraph = NoteGraph(name = name, filePath = fullPath)
        _noteGraphs.update { it + newGraph }
        setSelectedNoteGraph(newGraph)
    }

    override suspend fun addNoteGraph(name: String) {
        // This is now handled by createNoteGraph with a file path
    }

    override suspend fun removeNoteGraph(graph: NoteGraph) {
        // Implementation would involve deleting the database file and removing from the list
        _noteGraphs.update { it - graph }
        if (_selectedNoteGraph.value == graph) {
            _selectedNoteGraph.value = null
            kuzuDBService.close()
        }
    }

    override suspend fun setSelectedNoteGraph(graph: NoteGraph) {
        if (_selectedNoteGraph.value?.filePath != graph.filePath) {
            kuzuDBService.close()
            kuzuDBService.initialize(graph.filePath)
        }
        _selectedNoteGraph.value = graph
        loadGraphFromDB()
    }

    private fun mapDbTypetoPropertyType(dbType: String): PropertyType {
        return when (dbType.uppercase()) {
            "STRING" -> PropertyType.TEXT
            "INT64" -> PropertyType.NUMBER
            "BOOLEAN" -> PropertyType.BOOLEAN
            "DATE" -> PropertyType.DATE
            "TIMESTAMP" -> PropertyType.TIMESTAMP
            else -> PropertyType.TEXT // Default to TEXT for unknown types
        }
    }

    private suspend fun loadGraphFromDB() {
        val graph = _selectedNoteGraph.value ?: return
        val nodeTableNames = kuzuDBService.getNodeTables().mapNotNull { it["name"] as? String }
        val edgeTableNames = kuzuDBService.getEdgeTables().mapNotNull { it["name"] as? String }

        val nodeSchemas = nodeTableNames.mapIndexed { index, name ->
            val properties = kuzuDBService.getTableSchema(name).map {
                PropertyDefinition(
                    key = it["name"] as String,
                    type = mapDbTypetoPropertyType(it["type"] as String)
                )
            }
            NodeSchema(id = index, typeName = name, properties = properties)
        }

        val edgeSchemas = edgeTableNames.mapIndexed { index, name ->
            val properties = kuzuDBService.getTableSchema(name).map {
                PropertyDefinition(
                    key = it["name"] as String,
                    type = mapDbTypetoPropertyType(it["type"] as String)
                )
            }
            EdgeSchema(id = index, typeName = name, properties = properties)
        }

        _selectedNoteGraph.update {
            it?.copy(
                nodeSchemas = nodeSchemas.toMutableList(),
                edgeSchemas = edgeSchemas.toMutableList()
                // Loading nodes and edges would require additional service methods
            )
        }
    }


    override suspend fun addNodeSchema(schema: NodeSchema) {
        kuzuDBService.createNodeSchema(schema)
        loadGraphFromDB()
    }

    override suspend fun removeNodeSchema(schema: NodeSchema) {
        // kuzuDBService.dropTable(schema.typeName)
        loadGraphFromDB()
    }

    override suspend fun updateNodeSchema(updatedSchema: NodeSchema) {
        // This would involve complex ALTER TABLE operations
        loadGraphFromDB()
    }

    override suspend fun addEdgeSchema(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String) {
        kuzuDBService.createEdgeSchema(schema, fromNodeTypeName, toNodeTypeName)
        loadGraphFromDB()
    }

    override suspend fun removeEdgeSchema(schema: EdgeSchema) {
        // kuzuDBService.dropTable(schema.typeName)
        loadGraphFromDB()
    }

    override suspend fun updateEdgeSchema(updatedSchema: EdgeSchema) {
        // This would involve complex ALTER TABLE operations
        loadGraphFromDB()
    }

    override suspend fun addNode(node: GraphNode) {
        val properties = node.properties.associate { it.key to (it.value ?: "") }
        kuzuDBService.insertNode(node.typeName, properties)
        loadGraphFromDB() // Refresh state from DB
    }

    override suspend fun addEdge(edge: GraphEdge) {
        // kuzuDBService.insertEdge(...)
        loadGraphFromDB()
    }

    override suspend fun updateNode(updatedNode: GraphNode) {
        // kuzuDBService.updateNode(...)
        loadGraphFromDB()
    }

    override suspend fun updateEdge(updatedEdge: GraphEdge) {
        // kuzuDBService.updateEdge(...)
        loadGraphFromDB()
    }

    override suspend fun removeNode(node: GraphNode) {
        kuzuDBService.deleteNode(node.typeName, node.id)
        loadGraphFromDB()
    }

    override suspend fun removeEdge(edge: GraphEdge) {
        // kuzuDBService.deleteEdge(...)
        loadGraphFromDB()
    }

    override suspend fun executeQuery(query: String): List<Map<String, Any?>> {
        return kuzuDBService.executeQuery(query)
    }
}