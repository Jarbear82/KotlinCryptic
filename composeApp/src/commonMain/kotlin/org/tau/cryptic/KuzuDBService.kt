package org.tau.cryptic

expect class KuzuDBService() {
    fun initialize()
    fun close()
}