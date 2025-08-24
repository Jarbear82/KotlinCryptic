package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tau.cryptic.KuzuDBService

class QueryViewModel(private val kuzuDBService: KuzuDBService) : ViewModel() {

    private val _queryResult = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val queryResult = _queryResult.asStateFlow()

    fun executeQuery(query: String) {
        viewModelScope.launch {
            _queryResult.value = kuzuDBService.executeQuery(query)
        }
    }
}