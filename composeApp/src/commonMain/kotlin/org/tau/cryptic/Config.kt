package org.tau.cryptic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode
import org.tau.cryptic.pages.NodeSchema
import org.tau.cryptic.pages.PropertyDefinition
import org.tau.cryptic.pages.PropertyInstance
import org.tau.cryptic.pages.PropertyType
import org.tau.cryptic.pages.defaultValue

/**
 * A singleton object to hold the global application configuration state.
 *
 * This object provides a single source of truth for UI-related constants,
 * theme settings, and the application's data, such as the note graphs.
 */
object Config {

    //region Theme Configuration
    enum class AppTheme {
        LIGHT,
        DARK
    }

    var theme by mutableStateOf(AppTheme.LIGHT)

    object Padding {
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
    }

    data class ColorPalette(
        val primary: Color,
        val secondary: Color,
        val background: Color,
        val surface: Color,
        val onPrimary: Color,
        val onBackground: Color,
        val onSurface: Color,
        val textPrimary: Color,
        val textSecondary: Color,
    )

    private val LightColorPalette = ColorPalette(
        primary = Color(0xffbf2424),
        secondary = Color(0xffff6464),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF0F0F0),
        onPrimary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        textPrimary = Color(0xFF212121),
        textSecondary = Color(0xFF757575)
    )

    private val DarkColorPalette = ColorPalette(
        primary = Color(0xffbf2424),
        secondary = Color(0xffff6464),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        textPrimary = Color(0xFFE0E0E0),
        textSecondary = Color(0xFFBDBDBD)
    )

    val colors: ColorPalette
        get() = when (theme) {
            AppTheme.LIGHT -> LightColorPalette
            AppTheme.DARK -> DarkColorPalette
        }
    //endregion

    //region Graph Data Management
    var noteGraphs by mutableStateOf(mutableListOf<NoteGraph>())
    var selectedNoteGraph by mutableStateOf<NoteGraph?>(null)

    init {
        // Initialize with a default graph for demonstration
        val defaultGraph = NoteGraph(name = "My First Graph").apply {
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
            val hasOrderedSchema = EdgeSchema(
                id = 3, typeName = "HAS_ORDERED", properties = listOf(
                    PropertyDefinition(key = "order_date", type = PropertyType.TEXT)
                )
            )
            nodeSchemas.addAll(listOf(userSchema, productSchema))
            edgeSchemas.add(hasOrderedSchema)
        }
        noteGraphs.add(defaultGraph)
        selectedNoteGraph = defaultGraph
    }

    fun addNoteGraph(name: String) {
        noteGraphs.add(NoteGraph(name = name))
    }

    fun removeNoteGraph(graph: NoteGraph) {
        noteGraphs.remove(graph)
        if (selectedNoteGraph == graph) {
            selectedNoteGraph = noteGraphs.firstOrNull()
        }
    }

    // --- Node Schema Methods ---
    fun addNodeSchema(schema: NodeSchema) {
        selectedNoteGraph?.nodeSchemas?.add(schema)
    }

    fun removeNodeSchema(schema: NodeSchema) {
        selectedNoteGraph?.nodeSchemas?.remove(schema)
    }

    fun updateNodeSchema(updatedSchema: NodeSchema) {
        selectedNoteGraph?.let { graph ->
            val schemaIndex = graph.nodeSchemas.indexOfFirst { it.id == updatedSchema.id }
            if (schemaIndex != -1) {
                // 1. Replace the old schema with the updated one
                graph.nodeSchemas[schemaIndex] = updatedSchema

                // 2. Find all nodes that use this schema
                val affectedNodes = graph.nodes.filter { it.typeName == updatedSchema.typeName }

                // 3. Update the properties of each affected node
                affectedNodes.forEach { node ->
                    val newProperties = mutableListOf<PropertyInstance>()
                    val oldPropertiesMap = node.properties.associateBy { it.key }

                    // 4. Create the new property list from the updated schema
                    updatedSchema.properties.forEach { propDef ->
                        val existingValue = oldPropertiesMap[propDef.key]?.value
                        newProperties.add(
                            PropertyInstance(
                                key = propDef.key,
                                // Keep old value if it exists, otherwise use the new schema's default
                                value = existingValue ?: propDef.type.defaultValue
                            )
                        )
                    }

                    // 5. Create an updated node and replace the old one
                    val updatedNode = node.copy(properties = newProperties)
                    updateNode(updatedNode)
                }
            }
        }
    }

    // --- Edge Schema Methods ---
    fun addEdgeSchema(schema: EdgeSchema) {
        selectedNoteGraph?.edgeSchemas?.add(schema)
    }

    fun removeEdgeSchema(schema: EdgeSchema) {
        selectedNoteGraph?.edgeSchemas?.remove(schema)
    }

    fun updateEdgeSchema(updatedSchema: EdgeSchema) {
        selectedNoteGraph?.let { graph ->
            val schemaIndex = graph.edgeSchemas.indexOfFirst { it.id == updatedSchema.id }
            if (schemaIndex != -1) {
                // 1. Replace the old schema with the updated one
                graph.edgeSchemas[schemaIndex] = updatedSchema

                // 2. Find all edges that use this schema
                val affectedEdges = graph.edges.filter { it.typeName == updatedSchema.typeName }

                // 3. Update the properties of each affected edge
                affectedEdges.forEach { edge ->
                    val newProperties = mutableListOf<PropertyInstance>()
                    val oldPropertiesMap = edge.properties.associateBy { it.key }

                    // 4. Create the new property list from the updated schema
                    updatedSchema.properties.forEach { propDef ->
                        val existingValue = oldPropertiesMap[propDef.key]?.value
                        newProperties.add(
                            PropertyInstance(
                                key = propDef.key,
                                // Keep old value if it exists, otherwise use the new schema's default
                                value = existingValue ?: propDef.type.defaultValue
                            )
                        )
                    }

                    // 5. Create an updated edge and replace the old one
                    val updatedEdge = edge.copy(properties = newProperties)
                    updateEdge(updatedEdge)
                }
            }
        }
    }

    // --- Graph Element Methods ---
    fun addNode(node: GraphNode) {
        selectedNoteGraph?.nodes?.add(node)
    }

    fun addEdge(edge: GraphEdge) {
        selectedNoteGraph?.edges?.add(edge)
    }

    fun updateNode(updatedNode: GraphNode) {
        selectedNoteGraph?.let { graph ->
            val index = graph.nodes.indexOfFirst { it.id == updatedNode.id }
            if (index != -1) {
                graph.nodes[index] = updatedNode
            }
        }
    }

    fun updateEdge(updatedEdge: GraphEdge) {
        selectedNoteGraph?.let { graph ->
            val index = graph.edges.indexOfFirst { it.id == updatedEdge.id }
            if (index != -1) {
                graph.edges[index] = updatedEdge
            }
        }
    }
    //endregion
}