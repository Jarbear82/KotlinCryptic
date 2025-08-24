package com.tau.cryptic.data

import org.tau.cryptic.KuzuDBService
import org.tau.cryptic.ui.graph.LayoutManager
import org.tau.cryptic.ui.viewmodel.QueryViewModel

/**
 * A simple dependency injection container.
 */
interface AppContainer {
    val graphRepository: GraphRepository
    val layoutManager: LayoutManager
    val queryViewModel: QueryViewModel
}

/**
 * The default implementation of the dependency injection container.
 */
class DefaultAppContainer : AppContainer {
    private val kuzuDBService = KuzuDBService()

    override val graphRepository: GraphRepository by lazy {
        GraphRepositoryImpl(kuzuDBService)
    }

    override val layoutManager: LayoutManager by lazy {
        LayoutManager()
    }

    override val queryViewModel: QueryViewModel by lazy {
        QueryViewModel(kuzuDBService)
    }
}