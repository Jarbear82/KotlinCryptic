package org.tau.cryptic.data

import org.tau.cryptic.KuzuDBService

/**
 * A simple dependency injection container.
 */
interface AppContainer {
    val graphRepository: GraphRepository
}

/**
 * The default implementation of the dependency injection container.
 */
class DefaultAppContainer : AppContainer {
    override val graphRepository: GraphRepository by lazy {
        GraphRepositoryImpl(KuzuDBService())
    }
}