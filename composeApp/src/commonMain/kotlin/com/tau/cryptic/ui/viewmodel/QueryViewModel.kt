package com.tau.cryptic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tau.cryptic.data.GraphRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueryViewModel(private val graphRepository: GraphRepository) : ViewModel() {

    private val _queryResult = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val queryResult = _queryResult.asStateFlow()

    fun executeQuery(query: String) {
        viewModelScope.launch {
            _queryResult.value = graphRepository.executeQuery(query)
            print(_queryResult.toString())
        }
    }
}