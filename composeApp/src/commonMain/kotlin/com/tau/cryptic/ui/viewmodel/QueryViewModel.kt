package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tau.cryptic.KuzuDBService
import com.tau.cryptic.data.QueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueryViewModel(private val kuzuDBService: KuzuDBService) : ViewModel() {

    private val _queryResult = MutableStateFlow<QueryResult?>(null)
    val queryResult = _queryResult.asStateFlow()

    fun executeQuery(query: String) {
        viewModelScope.launch {
            _queryResult.value = kuzuDBService.executeQuery(query)
        }
    }
}