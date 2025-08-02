package org.tau.cryptic

import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema

expect class KuzuDBService() {
    fun initialize()
    fun close()
    fun createNodeSchema(graphName: String, schema: NodeSchema)
    fun createEdgeSchema(graphName: String, schema: EdgeSchema, fromTable: String, toTable: String)
}