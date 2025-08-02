package org.tau.cryptic.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.UUID

// Local Imports
import org.tau.cryptic.components.DeletableSelectableListView
import org.tau.cryptic.components.Identifiable
import org.tau.cryptic.ui.viewmodel.SchemaViewModel

//region Data Models
/**
 * Defines the types for schema properties, now with more advanced types.
 */
enum class PropertyType {
    TEXT, NUMBER, BOOLEAN, LONG_TEXT, IMAGE, DATE, TIMESTAMP, LIST, MAP, VECTOR, STRUCT
}

/**
 * Returns the default value for a given property type.
 */
val PropertyType.defaultValue: Any?
    get() = when (this) {
        PropertyType.TEXT, PropertyType.LONG_TEXT, PropertyType.IMAGE -> ""
        PropertyType.NUMBER -> 0L // Defaulting to Long for INT64
        PropertyType.BOOLEAN -> false
        PropertyType.DATE -> "2023-01-01" // KuzuDB expects date as string
        PropertyType.TIMESTAMP -> System.currentTimeMillis()
        PropertyType.LIST -> "[]"
        PropertyType.MAP -> "{}"
        PropertyType.VECTOR -> "[]"
        PropertyType.STRUCT -> "{}"
    }

/**
 * Represents a single property within a schema.
 * @param id A unique identifier for UI stability.
 * @param key The name of the property.
 * @param type The data type of the property.
 * @param isIndexed Whether an index should be created for this property.
 * @param isFullTextIndexed Whether a full-text search index should be created.
 */
data class PropertyDefinition(
    val id: String = UUID.randomUUID().toString(),
    var key: String,
    var type: PropertyType,
    var isIndexed: Boolean = false,
    var isFullTextIndexed: Boolean = false
)

/**
 * A sealed interface representing a schema definition, ensuring it's identifiable.
 */
sealed interface SchemaDefinition : Identifiable {
    override val id: Int
    val typeName: String
    val properties: List<PropertyDefinition>
    val allowSemiStructured: Boolean // New flag for semi-structured data
}

/**
 * Represents a Node's schema, including a mandatory 'name' property.
 */
data class NodeSchema(
    override val id: Int,
    override var typeName: String,
    override var properties: List<PropertyDefinition>,
    override var allowSemiStructured: Boolean = false
) : SchemaDefinition

/**
 * Represents an Edge's schema.
 */
data class EdgeSchema(
    override val id: Int,
    override var typeName: String,
    override var properties: List<PropertyDefinition>,
    override var allowSemiStructured: Boolean = false
) : SchemaDefinition
//endregion

@Composable
fun Schema(
    schemaViewModel: SchemaViewModel,
    nodeSchemas: List<NodeSchema>,
    edgeSchemas: List<EdgeSchema>
) {
    // State for the currently selected schema for editing
    var selectedSchema by remember { mutableStateOf<SchemaDefinition?>(null) }

    // State for managing the selected tab (Nodes vs. Edges)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Nodes", "Edges")

    // Update handler to reflect edits from the detail view back to the list
    val onSchemaUpdated: (SchemaDefinition) -> Unit = { updatedSchema ->
        when (updatedSchema) {
            is NodeSchema -> schemaViewModel.onNodeSchemaUpdate(updatedSchema)
            is EdgeSchema -> schemaViewModel.onEdgeSchemaUpdate(updatedSchema)
        }
        selectedSchema = updatedSchema // Keep the updated item selected
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // ## First Column: Display/Edit Selected Schema
        Box(
            modifier = Modifier
                .weight(1.2f) // Give it a bit more space
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            SchemaDetailView(
                schema = selectedSchema,
                onUpdate = onSchemaUpdated
            )
        }

        VerticalDivider()

        // ## Second Column: Tabbed list and creation forms
        Column(modifier = Modifier.weight(1f)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            selectedSchema = null // Clear selection when switching tabs
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Display content based on the selected tab
            when (selectedTabIndex) {
                0 -> NodeSchemaContent(
                    items = nodeSchemas,
                    selectedItem = selectedSchema,
                    onItemClick = { selectedSchema = it },
                    onDeleteItemClick = {
                        if (selectedSchema?.id == it.id) selectedSchema = null
                        schemaViewModel.onNodeSchemaRemove(it)
                    },
                    onCreate = { typeName, properties ->
                        val finalProperties = mutableListOf(PropertyDefinition(key = "name", type = PropertyType.TEXT))
                        finalProperties.addAll(properties)
                        // Create a new NodeSchema with a unique ID
                        val newSchema = NodeSchema(
                            id = (nodeSchemas.maxOfOrNull { s -> s.id } ?: 0) + 1,
                            typeName = typeName,
                            properties = finalProperties
                        )
                        schemaViewModel.onNodeSchemaAdd(newSchema)
                    }
                )
                1 -> EdgeSchemaContent(
                    items = edgeSchemas,
                    allNodeSchemas = nodeSchemas,
                    selectedItem = selectedSchema,
                    onItemClick = { selectedSchema = it },
                    onDeleteItemClick = {
                        if (selectedSchema?.id == it.id) selectedSchema = null
                        schemaViewModel.onEdgeSchemaRemove(it)
                    },
                    onCreate = { typeName, properties, from, to ->
                        val newSchema = EdgeSchema(
                            id = (edgeSchemas.maxOfOrNull { s -> s.id } ?: 0) + 1,
                            typeName = typeName,
                            properties = properties
                        )
                        schemaViewModel.onEdgeSchemaAdd(newSchema, from, to)
                    }
                )
            }
        }
    }
}

//region Tab Content Composables
@Composable
private fun NodeSchemaContent(
    items: List<NodeSchema>,
    selectedItem: SchemaDefinition?,
    onItemClick: (NodeSchema) -> Unit,
    onDeleteItemClick: (NodeSchema) -> Unit,
    onCreate: (String, List<PropertyDefinition>) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Node Schemas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            DeletableSelectableListView(
                items = items,
                selectedItem = selectedItem as? NodeSchema,
                onItemClick = onItemClick,
                onDeleteItemClick = onDeleteItemClick,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) { node ->
                ListItemWithIcon(text = node.typeName, icon = Icons.Default.DataObject)
            }
        }
        HorizontalDivider()
        CreateSchemaForm(
            title = "Create New Node Schema",
            onSchemaCreate = onCreate,
            formatTypeName = { it } // No special formatting for nodes
        )
    }
}

@Composable
private fun EdgeSchemaContent(
    items: List<EdgeSchema>,
    allNodeSchemas: List<NodeSchema>,
    selectedItem: SchemaDefinition?,
    onItemClick: (EdgeSchema) -> Unit,
    onDeleteItemClick: (EdgeSchema) -> Unit,
    onCreate: (String, List<PropertyDefinition>, String, String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Edge Schemas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            DeletableSelectableListView(
                items = items,
                selectedItem = selectedItem as? EdgeSchema,
                onItemClick = onItemClick,
                onDeleteItemClick = onDeleteItemClick,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) { edge ->
                ListItemWithIcon(text = edge.typeName, icon = Icons.Default.Link)
            }
        }
        HorizontalDivider()
        CreateEdgeSchemaForm(
            title = "Create New Edge Schema",
            allNodeSchemas = allNodeSchemas,
            onSchemaCreate = onCreate,
            // Automatic formatting for edge type names
            formatTypeName = { it.uppercase().replace(' ', '_') }
        )
    }
}
//endregion

//region Detail/Editor View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaDetailView(schema: SchemaDefinition?, onUpdate: (SchemaDefinition) -> Unit) {
    if (schema == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Select a schema to view its details",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Create a mutable copy for editing
    var editableSchema by remember(schema) {
        mutableStateOf(
            when(schema) {
                is NodeSchema -> schema.copy(properties = schema.properties.map { it.copy() })
                is EdgeSchema -> schema.copy(properties = schema.properties.map { it.copy() })
            }
        )
    }

    // State for the new property form
    var newPropKey by remember { mutableStateOf("") }
    var newPropType by remember { mutableStateOf(PropertyType.TEXT) }


    // Function to handle updates and propagate them
    fun triggerUpdate() {
        onUpdate(editableSchema)
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Edit Schema", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
        }

        // Edit Type Name
        item {
            OutlinedTextField(
                value = editableSchema.typeName,
                onValueChange = {
                    editableSchema = when(editableSchema) {
                        is NodeSchema -> (editableSchema as NodeSchema).copy(typeName = it)
                        is EdgeSchema -> (editableSchema as EdgeSchema).copy(typeName = it.uppercase().replace(' ', '_'))
                    }
                    triggerUpdate()
                },
                label = { Text("Type Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Properties", style = MaterialTheme.typography.titleLarge)
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        }

        // List properties for editing
        items(editableSchema.properties) { prop ->
            val isNameProperty = (schema is NodeSchema && prop.key == "name")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Property Key
                OutlinedTextField(
                    value = prop.key,
                    onValueChange = { newKey ->
                        // Create a new property and update the list
                        val index = editableSchema.properties.indexOf(prop)
                        if (index != -1) {
                            val updatedProperties = editableSchema.properties.toMutableList()
                            updatedProperties[index] = prop.copy(key = newKey)
                            editableSchema = when (val s = editableSchema) {
                                is NodeSchema -> s.copy(properties = updatedProperties)
                                is EdgeSchema -> s.copy(properties = updatedProperties)
                            }
                            triggerUpdate()
                        }
                    },
                    label = { Text("Key") },
                    modifier = Modifier.weight(1f),
                    readOnly = isNameProperty,
                    enabled = !isNameProperty
                )

                // Property Type
                PropertyTypeDropdown(
                    selectedType = prop.type,
                    onTypeSelected = { newType ->
                        val index = editableSchema.properties.indexOf(prop)
                        if (index != -1) {
                            val updatedProperties = editableSchema.properties.toMutableList()
                            updatedProperties[index] = prop.copy(type = newType)
                            editableSchema = when (val s = editableSchema) {
                                is NodeSchema -> s.copy(properties = updatedProperties)
                                is EdgeSchema -> s.copy(properties = updatedProperties)
                            }
                            triggerUpdate()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isNameProperty
                )


                // Delete Button
                IconButton(
                    onClick = {
                        val updatedProperties = editableSchema.properties.toMutableList().apply {
                            remove(prop)
                        }
                        editableSchema = when (val s = editableSchema) {
                            is NodeSchema -> s.copy(properties = updatedProperties)
                            is EdgeSchema -> s.copy(properties = updatedProperties)
                        }
                        triggerUpdate()
                    },
                    enabled = !isNameProperty
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Property",
                        tint = if (!isNameProperty) MaterialTheme.colorScheme.error else Color.Transparent
                    )
                }
            }
        }

        // NEW: Form to add a new property
        item {
            Spacer(Modifier.height(16.dp))
            Text("Add New Property", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newPropKey,
                    onValueChange = { newPropKey = it },
                    label = { Text("Property Key") },
                    modifier = Modifier.weight(1f)
                )
                PropertyTypeDropdown(
                    selectedType = newPropType,
                    onTypeSelected = { newPropType = it },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newPropKey.isNotBlank()) {
                            val newProperty = PropertyDefinition(key = newPropKey, type = newPropType)
                            val updatedProperties = editableSchema.properties.toMutableList().apply {
                                add(newProperty)
                            }
                            editableSchema = when (val s = editableSchema) {
                                is NodeSchema -> s.copy(properties = updatedProperties)
                                is EdgeSchema -> s.copy(properties = updatedProperties)
                            }
                            triggerUpdate()
                            // Reset form
                            newPropKey = ""
                            newPropType = PropertyType.TEXT
                        }
                    },
                    enabled = newPropKey.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Property",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyTypeDropdown(
    selectedType: PropertyType,
    onTypeSelected: (PropertyType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedType.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PropertyType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
//endregion

//region Creation Form
@Composable
private fun CreateSchemaForm(
    title: String,
    onSchemaCreate: (String, List<PropertyDefinition>) -> Unit,
    formatTypeName: (String) -> String
) {
    var typeName by remember { mutableStateOf("") }
    val properties = remember { mutableStateListOf<PropertyDefinition>() }
    var newPropKey by remember { mutableStateOf("") }
    var newPropType by remember { mutableStateOf(PropertyType.TEXT) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = typeName,
            onValueChange = { typeName = formatTypeName(it) },
            label = { Text("Schema Type Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Properties being added
        properties.forEach { prop ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${prop.key}: ${prop.type.name}", modifier = Modifier.weight(1f))
                IconButton(onClick = { properties.remove(prop) }) {
                    Icon(Icons.Default.RemoveCircleOutline, "Remove Property")
                }
            }
        }

        // Form to add a new property
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newPropKey,
                onValueChange = { newPropKey = it },
                label = { Text("Property Key") },
                modifier = Modifier.weight(1f)
            )
            PropertyTypeDropdown(
                selectedType = newPropType,
                onTypeSelected = { newPropType = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (newPropKey.isNotBlank()) {
                        properties.add(PropertyDefinition(key = newPropKey, type = newPropType))
                        newPropKey = "" // Reset for next entry
                    }
                },
                enabled = newPropKey.isNotBlank()
            ) {
                Icon(Icons.Default.AddCircle, "Add Property")
            }
        }

        Button(
            onClick = {
                onSchemaCreate(typeName, properties.toList())
                typeName = ""
                properties.clear()
            },
            enabled = typeName.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Create")
        }
    }
}


@Composable
private fun CreateEdgeSchemaForm(
    title: String,
    allNodeSchemas: List<NodeSchema>,
    onSchemaCreate: (String, List<PropertyDefinition>, String, String) -> Unit,
    formatTypeName: (String) -> String
) {
    var typeName by remember { mutableStateOf("") }
    val properties = remember { mutableStateListOf<PropertyDefinition>() }
    var newPropKey by remember { mutableStateOf("") }
    var newPropType by remember { mutableStateOf(PropertyType.TEXT) }
    var fromNodeSchema by remember { mutableStateOf<NodeSchema?>(null) }
    var toNodeSchema by remember { mutableStateOf<NodeSchema?>(null) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = typeName,
            onValueChange = { typeName = formatTypeName(it) },
            label = { Text("Schema Type Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdowns for From and To
        NodeSchemaDropdown("From Node", allNodeSchemas, fromNodeSchema) { fromNodeSchema = it }
        NodeSchemaDropdown("To Node", allNodeSchemas, toNodeSchema) { toNodeSchema = it }


        // Properties being added
        properties.forEach { prop ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${prop.key}: ${prop.type.name}", modifier = Modifier.weight(1f))
                IconButton(onClick = { properties.remove(prop) }) {
                    Icon(Icons.Default.RemoveCircleOutline, "Remove Property")
                }
            }
        }

        // Form to add a new property
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newPropKey,
                onValueChange = { newPropKey = it },
                label = { Text("Property Key") },
                modifier = Modifier.weight(1f)
            )
            PropertyTypeDropdown(
                selectedType = newPropType,
                onTypeSelected = { newPropType = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (newPropKey.isNotBlank()) {
                        properties.add(PropertyDefinition(key = newPropKey, type = newPropType))
                        newPropKey = "" // Reset for next entry
                    }
                },
                enabled = newPropKey.isNotBlank()
            ) {
                Icon(Icons.Default.AddCircle, "Add Property")
            }
        }

        Button(
            onClick = {
                if(fromNodeSchema != null && toNodeSchema != null) {
                    onSchemaCreate(typeName, properties.toList(), fromNodeSchema!!.typeName, toNodeSchema!!.typeName)
                    typeName = ""
                    properties.clear()
                    fromNodeSchema = null
                    toNodeSchema = null
                }
            },
            enabled = typeName.isNotBlank() && fromNodeSchema != null && toNodeSchema != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Create")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeSchemaDropdown(
    label: String,
    schemas: List<NodeSchema>,
    selectedSchema: NodeSchema?,
    onSchemaSelected: (NodeSchema) -> Unit
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
//endregion

//region Helper Composables
@Composable
private fun ListItemWithIcon(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
//endregion