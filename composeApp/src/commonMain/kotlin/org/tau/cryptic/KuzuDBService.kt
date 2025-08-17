package org.tau.cryptic

import com.tau.kuzudb.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode
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

    suspend fun createNodeSchema(schema: NodeSchema) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key} ${it.type.name}"
        }
        val primaryKey = schema.properties.first().key
        executeQuery("CREATE NODE TABLE ${schema.typeName}($properties, PRIMARY KEY ($primaryKey))")
    }

    suspend fun createEdgeSchema(schema: EdgeSchema, fromTable: String, toTable: String) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key} ${it.type.name}"
        }
        val query = "CREATE REL TABLE ${schema.typeName}(FROM $fromTable TO $toTable, $properties)"
        executeQuery(query)
    }

    suspend fun dropTable(tableName: String) {
        executeQuery("DROP TABLE $tableName")
    }

    suspend fun renameTable(oldName: String, newName: String) {
        executeQuery("ALTER TABLE $oldName RENAME TO $newName")
    }

    suspend fun renameProperty(tableName: String, oldName: String, newName: String) {
        executeQuery("ALTER TABLE $tableName RENAME $oldName TO $newName")
    }

    suspend fun getNodeTables(): List<Map<String, Any?>> {
        return executeQuery("CALL SHOW_TABLES() WHERE type = 'NODE'")
    }

    suspend fun getEdgeTables(): List<Map<String, Any?>> {
        return executeQuery("CALL SHOW_TABLES() WHERE type = 'REL'")
    }

    suspend fun getTableSchema(tableName: String): List<Map<String, Any?>> {
        return executeQuery("CALL TABLE_INFO('$tableName')")
    }

    suspend fun insertNode(tableName: String, properties: Map<String, Any>): Boolean {
        val keys = properties.keys.joinToString(", ")
        val values = properties.values.joinToString(", ") { "'$it'" }
        val query = "CREATE (n:$tableName {$keys: $values})"
        val result = conn?.query(query)
        return result?.isSuccess() ?: false
    }

    suspend fun insertEdge(edge: GraphEdge) {
        val properties = edge.properties.joinToString(", ") {
            "${it.key}: '${it.value}'"
        }
        val query = """
            MATCH (a), (b)
            WHERE a._id = '${edge.sourceNodeId}' AND b._id = '${edge.targetNodeId}'
            CREATE (a)-[r:${edge.typeName} {$properties}]->(b)
        """.trimIndent()
        executeQuery(query)
    }

    suspend fun updateNode(node: GraphNode) {
        val properties = node.properties.joinToString(", ") {
            "n.${it.key} = '${it.value}'"
        }
        val query = "MATCH (n:${node.typeName}) WHERE n._id = '${node.id}' SET $properties"
        executeQuery(query)
    }

    suspend fun updateEdge(edge: GraphEdge) {
        val properties = edge.properties.joinToString(", ") {
            "r.${it.key} = '${it.value}'"
        }
        val query = "MATCH ()-[r:${edge.typeName}]->() WHERE r._id = '${edge.id}' SET $properties"
        executeQuery(query)
    }

    suspend fun deleteNode(tableName: String, nodeId: String): Boolean {
        val query = "MATCH (n:$tableName) WHERE n._id = '$nodeId' DETACH DELETE n"
        val result = conn?.query(query)
        return result?.isSuccess() ?: false
    }

    suspend fun deleteEdge(edge: GraphEdge) {
        val query = "MATCH ()-[r:${edge.typeName}]->() WHERE r._id = '${edge.id}' DELETE r"
        executeQuery(query)
    }

    suspend fun executeQuery(query: String): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
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
        list
    }
}