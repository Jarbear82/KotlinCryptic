package org.tau.cryptic.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.tau.cryptic.KuzuDBService
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.pages.*

/**
 * The default implementation of the graph repository.
 */
class GraphRepositoryImpl(private val kuzuDBService: KuzuDBService) : GraphRepository {

    private val _noteGraphs = MutableStateFlow<List<NoteGraph>>(emptyList())
    override val noteGraphs: Flow<List<NoteGraph>> = _noteGraphs.asStateFlow()

    private val _selectedNoteGraph = MutableStateFlow<NoteGraph?>(null)
    override val selectedNoteGraph: Flow<NoteGraph?> = _selectedNoteGraph.asStateFlow()

    init {
        kuzuDBService.initialize()
        // Initialize with a default graph for demonstration
        val defaultGraph = NoteGraph(name = "My_First_Graph").apply {
            val userSchema = NodeSchema(
                id = 1, typeName = "User", properties = listOf(
                    PropertyDefinition(key = "name", type = PropertyType.TEXT),
                    PropertyDefinition(key = "age", type = PropertyType.NUMBER),
                    PropertyDefinition(key = "is_verified", type = PropertyType.BOOLEAN)
                )
            )
            val productSchema = NodeSchema(
                id = 2, typeName = "Product", properties = listOf(
                    PropertyDefinition(key = "name", type = PropertyType.TEXT),
                    PropertyDefinition(key = "price", type = PropertyType.NUMBER)
                )
            )
            nodeSchemas.addAll(listOf(userSchema, productSchema))
        }
        _noteGraphs.value = listOf(defaultGraph)
        _selectedNoteGraph.value = defaultGraph
    }

    override fun addNoteGraph(name: String) {
        // Sanitize the name for use in table names
        val sanitizedName = name.replace(" ", "_")
        _noteGraphs.update { it + NoteGraph(name = sanitizedName) }
    }

    override fun removeNoteGraph(graph: NoteGraph) {
        _noteGraphs.update { it - graph }
        if (_selectedNoteGraph.value == graph) {
            _selectedNoteGraph.value = _noteGraphs.value.firstOrNull()
        }
    }

    override fun setSelectedNoteGraph(graph: NoteGraph) {
        _selectedNoteGraph.value = graph
    }

    override fun addNodeSchema(schema: NodeSchema) {
        _selectedNoteGraph.value?.let { graph ->
            kuzuDBService.createNodeSchema(graph.name, schema)
            _selectedNoteGraph.update {
                it?.copy(nodeSchemas = (it.nodeSchemas + schema).toMutableList())
            }
        }
    }

    override fun removeNodeSchema(schema: NodeSchema) {
        _selectedNoteGraph.update {
            it?.copy(nodeSchemas = (it.nodeSchemas - schema).toMutableList())
        }
    }

    override fun updateNodeSchema(updatedSchema: NodeSchema) {
        _selectedNoteGraph.update { graph ->
            graph?.let {
                val schemaIndex = it.nodeSchemas.indexOfFirst { s -> s.id == updatedSchema.id }
                if (schemaIndex != -1) {
                    val newSchemas = it.nodeSchemas.toMutableList()
                    newSchemas[schemaIndex] = updatedSchema
                    it.copy(nodeSchemas = newSchemas)
                } else {
                    it
                }
            }
        }
    }

    override fun addEdgeSchema(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String) {
        _selectedNoteGraph.value?.let { graph ->
            kuzuDBService.createEdgeSchema(graph.name, schema, fromNodeTypeName, toNodeTypeName)
            _selectedNoteGraph.update {
                it?.copy(edgeSchemas = (it.edgeSchemas + schema).toMutableList())
            }
        }
    }

    override fun removeEdgeSchema(schema: EdgeSchema) {
        _selectedNoteGraph.update {
            it?.copy(edgeSchemas = (it.edgeSchemas - schema).toMutableList())
        }
    }

    override fun updateEdgeSchema(updatedSchema: EdgeSchema) {
        _selectedNoteGraph.update { graph ->
            graph?.let {
                val schemaIndex = it.edgeSchemas.indexOfFirst { s -> s.id == updatedSchema.id }
                if (schemaIndex != -1) {
                    val newSchemas = it.edgeSchemas.toMutableList()
                    newSchemas[schemaIndex] = updatedSchema
                    it.copy(edgeSchemas = newSchemas)
                } else {
                    it
                }
            }
        }
    }

    override fun addNode(node: GraphNode) {
        _selectedNoteGraph.update {
            it?.copy(nodes = (it.nodes + node).toMutableList())
        }
    }

    override fun addEdge(edge: GraphEdge) {
        _selectedNoteGraph.update {
            it?.copy(edges = (it.edges + edge).toMutableList())
        }
    }

    override fun updateNode(updatedNode: GraphNode) {
        _selectedNoteGraph.update { graph ->
            graph?.let {
                val index = it.nodes.indexOfFirst { n -> n.id == updatedNode.id }
                if (index != -1) {
                    val newNodes = it.nodes.toMutableList()
                    newNodes[index] = updatedNode
                    it.copy(nodes = newNodes)
                } else {
                    it
                }
            }
        }
    }

    override fun updateEdge(updatedEdge: GraphEdge) {
        _selectedNoteGraph.update { graph ->
            graph?.let {
                val index = it.edges.indexOfFirst { e -> e.id == updatedEdge.id }
                if (index != -1) {
                    val newEdges = it.edges.toMutableList()
                    newEdges[index] = updatedEdge
                    it.copy(edges = newEdges)
                } else {
                    it
                }
            }
        }
    }

    override fun removeNode(node: GraphNode) {
        _selectedNoteGraph.update {
            it?.copy(nodes = (it.nodes - node).toMutableList())
        }
    }

    override fun removeEdge(edge: GraphEdge) {
        _selectedNoteGraph.update {
            it?.copy(edges = (it.edges - edge).toMutableList())
        }
    }
}