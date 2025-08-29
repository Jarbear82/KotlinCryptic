package com.tau.cryptic

import com.tau.cryptic.data.QueryResult
import com.tau.cryptic.pages.EdgeSchema
import com.tau.cryptic.pages.NodeSchema

expect class KuzuDBService() {
    fun initialize(dbPath: String)
    fun close()
    fun createNodeSchema(schema: NodeSchema)
    fun createEdgeSchema(schema: EdgeSchema, fromTable: String, toTable: String)
    fun getNodeTables(): List<Map<String, Any?>>
    fun getEdgeTables(): List<Map<String, Any?>>
    fun getTableSchema(tableName: String): List<Map<String, Any?>>
    fun getAllNodes(): List<Map<String, Any?>>
    fun getAllEdges(): List<Map<String, Any?>>
    fun getNode(tableName: String, nodeId: String): Map<String, Any?>?
    fun getEdge(tableName: String, edgeId: String): Map<String, Any?>?
    fun deleteEdge(tableName: String, edgeId: String): Boolean
    fun getNodesByType(tableName: String): List<Map<String, Any?>>
    fun getEdgesByType(tableName: String): List<Map<String, Any?>>
    fun createNode(tableName: String, properties: Map<String, Any>): Boolean
    fun addEdge(tableName: String, fromNodeId: String, toNodeId: String, properties: Map<String, Any>): Boolean
    fun updateNode(tableName: String, nodeId: String, properties: Map<String, Any>): Boolean
    fun updateEdge(tableName: String, edgeId: String, properties: Map<String, Any>): Boolean
    fun deleteNode(tableName: String, nodeId: String): Boolean
    fun dropTable(schemaTypeName: String) : Boolean
    fun executeQuery(query: String): QueryResult
}