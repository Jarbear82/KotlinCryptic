package com.tau.cryptic.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.UUID

// Local Imports
import com.tau.cryptic.NoteGraph
import com.tau.cryptic.components.Identifiable
import com.tau.cryptic.ui.viewmodel.GraphViewModel
import com.tau.cryptic.ui.viewmodel.QueryViewModel


//region Data Models
/**
 * Represents a single property instance in a graph element.
 * @param key The name of the property, derived from a PropertyDefinition.
 * @param value The actual data stored for this property.
 */
data class PropertyInstance(
    val key: String,
    var value: Any?
)

/**
 * A sealed interface for graph elements, ensuring they are identifiable.
 */
sealed interface GraphElement : Identifiable {
    override val id: String
    val typeName: String
    val properties: MutableList<PropertyInstance>
}

/**
 * Represents a node in the graph.
 * @param id A unique identifier.
 * @param typeName The name of the schema this node is based on (e.g., "User").
 * @param properties The list of property instances for this node.
 */
data class GraphNode(
    override val id: String = UUID.randomUUID().toString(),
    override val typeName: String,
    override val properties: MutableList<PropertyInstance>
) : GraphElement

/**
 * Represents an edge connecting two nodes in the graph.
 * @param id A unique identifier.
 * @param typeName The name of the schema this edge is based on (e.g., "HAS_ORDERED").
 * @param sourceNodeId The ID of the node where the edge originates.
 * @param targetNodeId The ID of the node where the edge terminates.
 * @param properties The list of property instances for this edge.
 */
data class GraphEdge(
    override val id: String = UUID.randomUUID().toString(),
    override val typeName: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    override val properties: MutableList<PropertyInstance>
) : GraphElement
//endregion

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

@Composable
fun Graph(
    graphViewModel: GraphViewModel,
    graph: NoteGraph,
    queryViewModel: QueryViewModel
) {
    // State for selections
    var primarySelected by remember { mutableStateOf<GraphElement?>(null) }
    var secondarySelected by remember { mutableStateOf<GraphNode?>(null) }

    // State for the right-hand side tabs
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Metadata", "Selected", "New", "Query")

    // State for tracking the Ctrl key press
    var isCtrlPressed by remember { mutableStateOf(false) }

    // State for the resizable panel and its visibility
    var isInfoPanelVisible by remember { mutableStateOf(true) }
    var panelWeight by remember { mutableFloatStateOf(0.4f) }

    val visualState by graphViewModel.graphVisualState.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            // Detect Ctrl key presses for secondary selection
            .onKeyEvent {
                if (it.key == Key.CtrlLeft || it.key == Key.CtrlRight) {
                    isCtrlPressed = it.type == KeyEventType.KeyDown
                }
                false
            }
    ) {
        BoxWithConstraints {
            val totalWidth = this.maxWidth

            Row(modifier = Modifier.fillMaxSize()) {
                // ## First Column: Graph View
                Box(
                    modifier = Modifier
                        .weight(1f - if (isInfoPanelVisible) panelWeight else 0f) // Takes remaining space
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // For this example, we'll just print the gestures
                                println("Pan: $pan, Zoom: $zoom")
                            }
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        visualState.edges.forEach { (edge, sourcePos, targetPos) ->
                            drawLine(
                                color = Color.Gray,
                                start = sourcePos,
                                end = targetPos,
                                strokeWidth = 2f
                            )
                        }

                        visualState.nodes.forEach { (node, pos) ->
                            drawCircle(
                                color = if (visualState.pinnedNodes.contains(node.id)) Color.Red else Color.Blue,
                                radius = 20f,
                                center = pos,
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }

                // Show divider and panel only if visible
                if (isInfoPanelVisible) {
                    // Draggable Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Convert pixel delta to Dp and calculate weight change against total width
                                    val weightDelta = (dragAmount.x.toDp() / totalWidth)
                                    // Dragging right (positive delta) should decrease the right panel's weight
                                    // Dragging left (negative delta) should increase it. So, we subtract.
                                    panelWeight = (panelWeight - weightDelta).coerceIn(0.2f, 0.7f)
                                }
                            }
                    ) {
                        VerticalDivider(modifier = Modifier.fillMaxHeight())
                    }

                    // ## Second Column: Graph Data
                    Column(modifier = Modifier.weight(panelWeight)) {
                        TabRow(selectedTabIndex = selectedTabIndex) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        // Tab Content
                        when (selectedTabIndex) {
                            0 -> MetadataTab(
                                graphName = graph.name,
                                primarySelected = primarySelected,
                                secondarySelected = secondarySelected,
                                nodes = graph.nodes,
                                edges = graph.edges,
                                onElementSelect = { element ->
                                    if (element is GraphNode && isCtrlPressed) {
                                        secondarySelected = if (secondarySelected?.id == element.id) null else element
                                    } else {
                                        primarySelected = if (primarySelected?.id == element.id) null else element
                                        secondarySelected = null // Clear secondary when primary changes
                                    }
                                }
                            )
                            1 -> SelectedTab(
                                selectedElement = primarySelected,
                                schemas = graph.nodeSchemas + graph.edgeSchemas,
                                onUpdate = { updatedElement ->
                                    when (updatedElement) {
                                        is GraphNode -> graphViewModel.onUpdateNode(updatedElement)
                                        is GraphEdge -> graphViewModel.onUpdateEdge(updatedElement)
                                    }
                                    primarySelected = updatedElement
                                }
                            )
                            2 -> NewTab(
                                nodeSchemas = graph.nodeSchemas,
                                edgeSchemas = graph.edgeSchemas,
                                nodes = graph.nodes,
                                onCreateNode = {
                                    graphViewModel.onCreateNode(it)
                                    primarySelected = it
                                    selectedTabIndex = 1
                                },
                                onCreateEdge = {
                                    graphViewModel.onCreateEdge(it)
                                    primarySelected = it
                                    selectedTabIndex = 1
                                }
                            )
                            3 -> QueryTab(queryViewModel)
                        }
                    }
                }
            }
        }


        // Info Button to toggle the panel visibility
        IconButton(
            onClick = { isInfoPanelVisible = !isInfoPanelVisible },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Toggle Info Panel"
            )
        }
    }
}

//region Tabs
@Composable
private fun MetadataTab(
    graphName: String,
    primarySelected: GraphElement?,
    secondarySelected: GraphNode?,
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    onElementSelect: (GraphElement) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Graph Name: $graphName", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Primary Selected: ${primarySelected?.displayId ?: "None"}", style = MaterialTheme.typography.bodyLarge)
            Text("Secondary Selected: ${secondarySelected?.displayId ?: "None"}", style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item {
            Text("Node List", style = MaterialTheme.typography.titleLarge)
        }
        items(nodes, key = { it.id }) { node ->
            SelectableListItem(
                text = node.displayId,
                isSelected = primarySelected?.id == node.id || secondarySelected?.id == node.id,
                onClick = { onElementSelect(node) }
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Edge List", style = MaterialTheme.typography.titleLarge)
        }
        items(edges, key = { it.id }) { edge ->
            SelectableListItem(
                text = edge.displayId,
                isSelected = primarySelected?.id == edge.id,
                onClick = { onElementSelect(edge) }
            )
        }
    }
}

@Composable
private fun SelectedTab(
    selectedElement: GraphElement?,
    schemas: List<SchemaDefinition>,
    onUpdate: (GraphElement) -> Unit
) {
    if (selectedElement == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a node or edge to see its properties.")
        }
        return
    }

    var editableElement by remember(selectedElement) { mutableStateOf(selectedElement) }

    fun triggerUpdate() {
        onUpdate(editableElement)
    }

    val elementSchema = schemas.find { it.typeName == editableElement.typeName }

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Edit Properties for ${editableElement.displayId}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
        }

        items(editableElement.properties) { prop ->
            val propDef = elementSchema?.properties?.find { it.key == prop.key }
            if (propDef != null) {
                PropertyInput(
                    propertyDef = propDef,
                    state = remember(prop) { mutableStateOf(prop.value) },
                    onValueChanged = { newValue ->
                        val index = editableElement.properties.indexOf(prop)
                        if (index != -1) {
                            val updatedProperties = editableElement.properties.toMutableList()
                            updatedProperties[index] = prop.copy(value = newValue)
                            editableElement = when (val s = editableElement) {
                                is GraphNode -> s.copy(properties = updatedProperties)
                                is GraphEdge -> s.copy(properties = updatedProperties)
                            }
                            triggerUpdate()
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewTab(
    nodeSchemas: List<NodeSchema>,
    edgeSchemas: List<EdgeSchema>,
    nodes: List<GraphNode>,
    onCreateNode: (GraphNode) -> Unit,
    onCreateEdge: (GraphEdge) -> Unit
) {
    var creationType by remember { mutableStateOf<CreationType?>(null) }
    var selectedSchema by remember { mutableStateOf<SchemaDefinition?>(null) }
    val properties = remember { mutableStateMapOf<String, Any?>() }

    // For edge creation
    var sourceNode by remember { mutableStateOf<GraphNode?>(null) }
    var targetNode by remember { mutableStateOf<GraphNode?>(null) }


    fun resetForm(type: CreationType? = null) {
        creationType = type
        selectedSchema = null
        properties.clear()
        sourceNode = null
        targetNode = null
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Create New Graph Element", style = MaterialTheme.typography.headlineSmall)
        }

        // 1. Select Node or Edge via Dropdown
        item {
            var expanded by remember { mutableStateOf(false) }
            val creationTypes = CreationType.values()

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = creationType?.let { "New ${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }}" } ?: "Select element type...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Element Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    creationTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text("New ${type.name.lowercase().replaceFirstChar { c -> c.uppercase() }}") },
                            onClick = {
                                resetForm(type)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (type == CreationType.NODE) Icons.Default.DataObject else Icons.Default.Link,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }


        if (creationType != null) {
            // 2. Select Schema Type
            item {
                val schemas: List<SchemaDefinition> = when (creationType) {
                    CreationType.NODE -> nodeSchemas
                    CreationType.EDGE -> edgeSchemas
                    null -> emptyList()
                }
                SchemaDropdown(
                    label = "Select ${creationType?.name?.lowercase()?.replaceFirstChar { it.uppercase() }} Type",
                    schemas = schemas,
                    selectedSchema = selectedSchema,
                    onSchemaSelected = { schema ->
                        selectedSchema = schema
                        properties.clear()
                        schema.properties.forEach {
                            properties[it.key] = it.type.defaultValue
                        }
                    }
                )
            }
        }

        if (selectedSchema != null) {
            // Special fields for Edges
            if (creationType == CreationType.EDGE) {
                item {
                    NodeDropdown("Source Node", nodes, sourceNode) { sourceNode = it }
                }
                item {
                    NodeDropdown("Target Node", nodes, targetNode) { targetNode = it }
                }
            }

            // 3. Fill Properties
            items(selectedSchema!!.properties) { propDef ->
                PropertyInput(
                    propertyDef = propDef,
                    state = remember(propDef.key) { mutableStateOf(properties[propDef.key]) },
                    onValueChanged = { newValue ->
                        properties[propDef.key] = newValue
                    }
                )
            }

            // 4. Create Button
            item {
                Button(
                    onClick = {
                        val propertyInstances = properties.map {
                            PropertyInstance(it.key, it.value)
                        }.toMutableList()

                        when (val schema = selectedSchema) {
                            is NodeSchema -> {
                                onCreateNode(GraphNode(typeName = schema.typeName, properties = propertyInstances))
                            }
                            is EdgeSchema -> {
                                if (sourceNode != null && targetNode != null) {
                                    onCreateEdge(
                                        GraphEdge(
                                            typeName = schema.typeName,
                                            sourceNodeId = sourceNode!!.id,
                                            targetNodeId = targetNode!!.id,
                                            properties = propertyInstances
                                        )
                                    )
                                }
                            }
                            null -> {}
                        }
                        resetForm() // Reset after creation
                    },
                    // Enable button only when all required fields are filled
                    enabled = when (creationType) {
                        CreationType.NODE -> selectedSchema != null && properties.none { it.value == null }
                        CreationType.EDGE -> selectedSchema != null && sourceNode != null && targetNode != null && properties.none { it.value == null }
                        null -> false
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Element")
                }
            }
        }
    }
}

@Composable
private fun QueryTab(queryViewModel: QueryViewModel) {
    var query by remember { mutableStateOf("MATCH (n) RETURN n") }
    val queryResult by queryViewModel.queryResult.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cypher Query", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Enter your Cypher-like query") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            maxLines = 10
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            queryViewModel.executeQuery(query)
        }) {
            Text("Execute")
        }
        Spacer(Modifier.height(16.dp))
        Text("Result", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(queryResult) { row ->
                    Text(row.toString())
                }
            }
        }
    }
}
//endregion

//region Helper Composables & Data
private enum class CreationType { NODE, EDGE }

private val GraphElement.displayId: String
    get() = when (this) {
        is GraphNode -> properties.firstOrNull { it.key == "name" }?.value?.toString() ?: "Node (${id.take(4)})"
        is GraphEdge -> "$typeName Edge (${id.take(4)})"
    }

@Composable
private fun SelectableListItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (text.contains("Edge")) Icons.Default.Link else Icons.Default.DataObject,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchemaDropdown(
    label: String,
    schemas: List<SchemaDefinition>,
    selectedSchema: SchemaDefinition?,
    onSchemaSelected: (SchemaDefinition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSchema?.typeName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            schemas.forEach { schema ->
                DropdownMenuItem(
                    text = { Text(schema.typeName) },
                    onClick = {
                        onSchemaSelected(schema)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeDropdown(
    label: String,
    nodes: List<GraphNode>,
    selectedNode: GraphNode?,
    onNodeSelected: (GraphNode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedNode?.displayId ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            nodes.forEach { node ->
                DropdownMenuItem(
                    text = { Text(node.displayId) },
                    onClick = {
                        onNodeSelected(node)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun PropertyInput(
    propertyDef: PropertyDefinition,
    state: MutableState<Any?>,
    onValueChanged: (Any?) -> Unit
) {
    when (propertyDef.type) {
        PropertyType.BOOLEAN -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onValueChanged(!(state.value as? Boolean ?: false)) }
            ) {
                Text(text = propertyDef.key, modifier = Modifier.weight(1f))
                Switch(
                    checked = state.value as? Boolean ?: false,
                    onCheckedChange = {
                        state.value = it
                        onValueChanged(it)
                    }
                )
            }
        }
        PropertyType.NUMBER -> {
            OutlinedTextField(
                value = state.value?.toString() ?: "",
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        state.value = it
                        onValueChanged(it)
                    }
                },
                label = { Text(propertyDef.key) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        else -> { // TEXT, LONG_TEXT, IMAGE
            OutlinedTextField(
                value = state.value?.toString() ?: "",
                onValueChange = {
                    state.value = it
                    onValueChanged(it)
                },
                label = { Text(propertyDef.key) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = propertyDef.type != PropertyType.LONG_TEXT,
                maxLines = if (propertyDef.type == PropertyType.LONG_TEXT) 5 else 1
            )
        }
    }
}
//endregion