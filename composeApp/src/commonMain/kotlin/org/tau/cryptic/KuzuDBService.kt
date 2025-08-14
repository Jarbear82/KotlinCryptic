package org.tau.cryptic

import com.tau.kuzudb.*
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema

class KuzuDBService {

    private var db: KuzuDatabase? = null
    private var conn: KuzuConnection? = null

    fun initialize(dbPath: String) {
        if (db == null) {
            db = KuzuDatabase(dbPath)
            conn = KuzuConnection(db!!)
        }
    }

    fun close() {
        conn?.close()
        db?.close()
        conn = null
        db = null
    }

    fun createNodeSchema(schema: NodeSchema) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key} ${it.type.name}"
        }
        val primaryKey = schema.properties.first().key
        executeQuery("CREATE NODE TABLE ${schema.typeName}($properties, PRIMARY KEY ($primaryKey))")
    }

    fun createEdgeSchema(schema: EdgeSchema, fromTable: String, toTable: String) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key} ${it.type.name}"
        }
        val query = "CREATE REL TABLE ${schema.typeName}(FROM $fromTable TO $toTable, $properties)"
        executeQuery(query)
    }

    fun getNodeTables(): List<Map<String, Any?>> {
        return executeQuery("CALL SHOW_TABLES() WHERE type = 'NODE'")
    }

    fun getEdgeTables(): List<Map<String, Any?>> {
        return executeQuery("CALL SHOW_TABLES() WHERE type = 'REL'")
    }

    fun getTableSchema(tableName: String): List<Map<String, Any?>> {
        return executeQuery("CALL TABLE_INFO('$tableName')")
    }

    fun insertNode(tableName: String, properties: Map<String, Any>): Boolean {
        val keys = properties.keys.joinToString(", ")
        val values = properties.values.joinToString(", ") { "'$it'" }
        val query = "CREATE (n:$tableName {$keys: $values})"
        val result = conn?.query(query)
        return result?.isSuccess() ?: false
    }

    fun deleteNode(tableName: String, nodeId: String): Boolean {
        val query = "MATCH (n:$tableName) WHERE n._id = $nodeId DELETE n"
        val result = conn?.query(query)
        return result?.isSuccess() ?: false
    }

    fun executeQuery(query: String): List<Map<String, Any?>> {
        val result = conn?.query(query)
        val list = mutableListOf<Map<String, Any?>>()
        if (result?.isSuccess() == true) {
            while (result.hasNext()) {
                val row = result.getNext()
                val map = mutableMapOf<String, Any?>()
                for (i in 0 until result.getNumColumns()) {
                    map[result.getColumnName(i)] = row.getValue(i).getValue()
                }
                list.add(map)
            }
        }
        return list
    }
}