package org.tau.cryptic.data

import org.tau.cryptic.KuzuDBService
import org.tau.cryptic.ui.graph.LayoutManager

/**
 * A simple dependency injection container.
 */
interface AppContainer {
    val graphRepository: GraphRepository
    val layoutManager: LayoutManager
}

/**
 * The default implementation of the dependency injection container.
 */
class DefaultAppContainer : AppContainer {
    override val graphRepository: GraphRepository by lazy {
        GraphRepositoryImpl(KuzuDBService())
    }

    override val layoutManager: LayoutManager by lazy {
        LayoutManager()
    }
}