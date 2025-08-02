package org.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.data.GraphRepository
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema
import org.tau.cryptic.pages.PropertyDefinition

/**
 * A view model for the schema screen.
 */
class SchemaViewModel(private val graphRepository: GraphRepository) : ViewModel() {

    val noteGraph: StateFlow<NoteGraph?> = graphRepository.selectedNoteGraph
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun onNodeSchemaUpdate(schema: NodeSchema) {
        graphRepository.updateNodeSchema(schema)
    }

    fun onNodeSchemaAdd(name: String, properties: List<PropertyDefinition>) {
        graphRepository.addNodeSchema(NodeSchema(0, name, properties))
    }

    fun onNodeSchemaRemove(schema: NodeSchema) {
        graphRepository.removeNodeSchema(schema)
    }

    fun onEdgeSchemaUpdate(schema: EdgeSchema) {
        graphRepository.updateEdgeSchema(schema)
    }

    fun onEdgeSchemaAdd(name: String, properties: List<PropertyDefinition>) {
        graphRepository.addEdgeSchema(EdgeSchema(0, name, properties))
    }

    fun onEdgeSchemaRemove(schema: EdgeSchema) {
        graphRepository.removeEdgeSchema(schema)
    }
}