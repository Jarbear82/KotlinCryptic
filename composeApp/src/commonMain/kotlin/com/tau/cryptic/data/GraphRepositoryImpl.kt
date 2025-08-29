package com.tau.cryptic.data

import com.tau.cryptic.KuzuDBService
import com.tau.cryptic.NoteGraph
import com.tau.cryptic.pages.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * The default implementation of the graph repository.
 */
class GraphRepositoryImpl(private val kuzuDBService: KuzuDBService) : GraphRepository {

    private val _noteGraphs = MutableStateFlow<List<NoteGraph>>(emptyList())
    override val noteGraphs: Flow<List<NoteGraph>> = _noteGraphs.asStateFlow()

    private val _selectedNoteGraph = MutableStateFlow<NoteGraph?>(null)
    override val selectedNoteGraph: Flow<NoteGraph?> = _selectedNoteGraph.asStateFlow()

    override fun createNoteGraph(filePath: String) {
        val directory = File(filePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val dbPath = File(directory, "database.kuzudb").absolutePath
        kuzuDBService.initialize(dbPath)
        val newGraph = NoteGraph(filePath = filePath)
        _noteGraphs.update { it + newGraph }
        setSelectedNoteGraph(newGraph)
    }

    override fun addNoteGraph(filePath: String) {
        val newGraph = NoteGraph(filePath = filePath)
        _noteGraphs.update { it + newGraph }
    }

    override fun removeNoteGraph(graph: NoteGraph) {
        // Implementation would involve deleting the database file and removing from the list
        _noteGraphs.update { it - graph }
        if (_selectedNoteGraph.value == graph) {
            _selectedNoteGraph.value = null
            kuzuDBService.close()
        }
    }

    override fun setSelectedNoteGraph(graph: NoteGraph) {
        val dbPath = File(graph.filePath, "database.kuzudb").absolutePath
        if (_selectedNoteGraph.value?.filePath != graph.filePath) {
            kuzuDBService.close()
            kuzuDBService.initialize(dbPath)
        }
        _selectedNoteGraph.value = graph
        loadGraphSchemas()
        loadGraphNodes()
        loadGraphEdges()
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

    override fun loadGraphSchemas() {
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
            )
        }
    }

    override fun loadGraphNodes() {
        val graph = _selectedNoteGraph.value ?: return
        val allNodes = kuzuDBService.getAllNodes()
        val graphNodes = allNodes.mapNotNull { nodeData ->
            val id = nodeData["id"] as? String
            val typeName = nodeData["label"] as? String
            if (id != null && typeName != null) {
                val schema = graph.nodeSchemas.find { it.typeName == typeName }
                val schemaPropertyKeys = schema?.properties?.map { it.key }?.toSet() ?: emptySet()

                val propertyInstances = nodeData.filter { (key, _) ->
                    // Only include properties defined in the schema for this node type, plus the metadata 'label'.
                    key in schemaPropertyKeys || key == "label"
                }.map { (key, value) ->
                    PropertyInstance(key, value)
                }.toMutableList()

                GraphNode(id = id, typeName = typeName, properties = propertyInstances)
            } else {
                null
            }
        }
        _selectedNoteGraph.update { it?.copy(nodes = graphNodes.toMutableList()) }
    }

    override fun loadGraphEdges() {
        val graph = _selectedNoteGraph.value ?: return
        val allEdges = kuzuDBService.getAllEdges()
        val graphEdges = allEdges.mapNotNull { edgeData ->
            val id = edgeData["id"] as? String
            val typeName = edgeData["label"] as? String
            val src = edgeData["src"] as? String
            val dst = edgeData["dst"] as? String
            if (id != null && typeName != null && src != null && dst != null) {
                val schema = graph.edgeSchemas.find { it.typeName == typeName }
                val schemaPropertyKeys = schema?.properties?.map { it.key }?.toSet() ?: emptySet()

                val propertyInstances = edgeData.filter { (key, _) ->
                    key in schemaPropertyKeys || key in setOf("id", "label", "src", "dst")
                }.map { (key, value) ->
                    PropertyInstance(key, value)
                }.toMutableList()
                GraphEdge(id = id, typeName = typeName, sourceNodeId = src, targetNodeId = dst, properties = propertyInstances)
            } else {
                null
            }
        }
        _selectedNoteGraph.update { it?.copy(edges = graphEdges.toMutableList()) }
    }


    override fun addNodeSchema(schema: NodeSchema) {
        kuzuDBService.createNodeSchema(schema)
        loadGraphSchemas()
    }

    override fun removeNodeSchema(schema: NodeSchema) {
        kuzuDBService.dropTable(schema.typeName)
        loadGraphSchemas()
    }

    override fun updateNodeSchema(updatedSchema: NodeSchema) {
        // This would involve complex ALTER TABLE operations
        loadGraphSchemas()
    }

    override fun addEdgeSchema(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String) {
        kuzuDBService.createEdgeSchema(schema, fromNodeTypeName, toNodeTypeName)
        loadGraphSchemas()
    }

    override fun removeEdgeSchema(schema: EdgeSchema) {
        kuzuDBService.dropTable(schema.typeName)
        loadGraphSchemas()
    }

    override fun updateEdgeSchema(updatedSchema: EdgeSchema) {
        // This would involve complex ALTER TABLE operations
        loadGraphSchemas()
    }

    override fun addNode(node: GraphNode) {
        val properties = node.properties.associate { it.key to (it.value ?: "") }
        kuzuDBService.createNode(node.typeName, properties)
        loadGraphNodes() // Refresh state from DB
    }

    override fun addEdge(edge: GraphEdge) {
        val properties = edge.properties.associate { it.key to (it.value ?: "") }
        kuzuDBService.addEdge(edge.typeName, edge.sourceNodeId, edge.targetNodeId, properties)
        loadGraphEdges()
    }

    override fun updateNode(updatedNode: GraphNode) {
        val properties = updatedNode.properties.associate { it.key to (it.value ?: "") }
        kuzuDBService.updateNode(updatedNode.typeName, updatedNode.id, properties)
        loadGraphNodes()
    }

    override fun updateEdge(updatedEdge: GraphEdge) {
        val properties = updatedEdge.properties.associate { it.key to (it.value ?: "") }
        kuzuDBService.updateEdge(updatedEdge.typeName, updatedEdge.id, properties)
        loadGraphEdges()
    }

    override fun removeNode(node: GraphNode) {
        kuzuDBService.deleteNode(node.typeName, node.id)
        loadGraphNodes()
    }

    override fun removeEdge(edge: GraphEdge) {
        kuzuDBService.deleteEdge(edge.typeName, edge.id)
        loadGraphEdges()
    }

    override fun executeQuery(query: String): List<Map<String, Any?>> {
        val result = kuzuDBService.executeQuery(query)
        loadGraphNodes()
        loadGraphEdges()
        return result
    }
}