package org.tau.cryptic

import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema

expect class KuzuDBService() {
    fun initialize(dbPath: String)
    fun close()
    fun createNodeSchema(schema: NodeSchema)
    fun createEdgeSchema(schema: EdgeSchema, fromTable: String, toTable: String)
    fun getNodeTables(): List<Map<String, Any?>>
    fun getEdgeTables(): List<Map<String, Any?>>
    fun getTableSchema(tableName: String): List<Map<String, Any?>>
    fun insertNode(tableName: String, properties: Map<String, Any>): Boolean
    fun deleteNode(tableName: String, nodeId: String): Boolean
    fun executeQuery(query: String): List<Map<String, Any?>>
}