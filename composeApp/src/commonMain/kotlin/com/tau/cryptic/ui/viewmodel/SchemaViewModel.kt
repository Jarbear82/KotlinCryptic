package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.tau.cryptic.NoteGraph
import org.tau.cryptic.data.GraphRepository
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema

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

    fun onNodeSchemaAdd(schema: NodeSchema) {
        graphRepository.addNodeSchema(schema)
    }

    fun onNodeSchemaRemove(schema: NodeSchema) {
        graphRepository.removeNodeSchema(schema)
    }

    fun onEdgeSchemaUpdate(schema: EdgeSchema) {
        graphRepository.updateEdgeSchema(schema)
    }

    fun onEdgeSchemaAdd(schema: EdgeSchema, fromNodeTypeName: String, toNodeTypeName: String) {
        graphRepository.addEdgeSchema(schema, fromNodeTypeName, toNodeTypeName)
    }

    fun onEdgeSchemaRemove(schema: EdgeSchema) {
        graphRepository.removeEdgeSchema(schema)
    }
}