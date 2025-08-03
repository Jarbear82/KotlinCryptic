package org.tau.cryptic

import com.kuzudb.Database
import com.kuzudb.Connection
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema
import org.tau.cryptic.pages.PropertyType
import java.nio.file.Files
import java.nio.file.Paths

actual class KuzuDBService {
    private var db: Database? = null
    private var conn: Connection? = null

    actual fun initialize(dbPath: String) {
        try {
            val dbDirectory = Paths.get(dbPath).parent
            if (!Files.exists(dbDirectory)) {
                Files.createDirectories(dbDirectory)
            }
            db = Database(dbPath)
            conn = Connection(db)
            println("KuzuDB initialized successfully at: $dbPath")
        } catch (e: Exception) {
            println("Failed to initialize KuzuDB: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun close() {
        try {
            conn?.close()
            db?.close()
            println("KuzuDB connection closed.")
        } catch (e: Exception) {
            println("Failed to close KuzuDB connection: ${e.message}")
        }
    }

    actual fun createNodeSchema(schema: NodeSchema) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key.replace(" ", "_")} ${mapPropertyType(it.type)}"
        }
        val query = "CREATE NODE TABLE ${schema.typeName} (id STRING, $properties, PRIMARY KEY (id))"
        executeQuery(query, "create node table '${schema.typeName}'")
    }

    actual fun createEdgeSchema(schema: EdgeSchema, fromTable: String, toTable: String) {
        val properties = schema.properties.joinToString(", ") {
            "${it.key.replace(" ", "_")} ${mapPropertyType(it.type)}"
        }
        val query = "CREATE REL TABLE ${schema.typeName} (FROM $fromTable TO $toTable, $properties)"
        executeQuery(query, "create edge table '${schema.typeName}'")
    }

    actual fun getNodeTables(): List<Map<String, Any?>> {
        val query = "CALL SHOW_TABLES() WHERE type = 'NODE' RETURN name"
        return executeQueryAndParseResults(query, "get node tables")
    }

    actual fun getEdgeTables(): List<Map<String, Any?>> {
        val query = "CALL SHOW_TABLES() WHERE type = 'REL' RETURN name"
        return executeQueryAndParseResults(query, "get edge tables")
    }

    actual fun getTableSchema(tableName: String): List<Map<String, Any?>> {
        val query = "CALL TABLE_INFO('$tableName') RETURN name, type"
        return executeQueryAndParseResults(query, "get table schema for '$tableName'")
    }

    actual fun insertNode(tableName: String, properties: Map<String, Any>): Boolean {
        val keys = properties.keys.joinToString(", ")
        val values = properties.values.joinToString(", ") { "'$it'" }
        val query = "CREATE (n:$tableName {$keys: $values})"
        return executeQuery(query, "insert node into '$tableName'")
    }

    actual fun deleteNode(tableName: String, nodeId: String): Boolean {
        val query = "MATCH (n:$tableName {id: '$nodeId'}) DETACH DELETE n"
        return executeQuery(query, "delete node from '$tableName'")
    }

    private fun executeQuery(query: String, description: String): Boolean {
        return try {
            println("Executing query: $query")
            conn?.query(query)
            println("Successfully executed query: $description")
            true
        } catch (e: Exception) {
            println("Failed to execute query '$description': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun executeQueryAndParseResults(query: String, description: String): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        try {
            println("Executing query: $query")
            val queryResult = conn?.query(query)
            queryResult?.let {
                while (it.hasNext()) {
                    val row = it.next()
                    val rowMap = mutableMapOf<String, Any?>()
                    row.keys.forEach { key ->
                        rowMap[key] = row.getValue(key).value
                    }
                    results.add(rowMap)
                }
            }
            println("Successfully executed query and parsed results: $description")
        } catch (e: Exception) {
            println("Failed to execute query '$description': ${e.message}")
            e.printStackTrace()
        }
        return results
    }

    private fun mapPropertyType(type: PropertyType): String {
        return when (type) {
            PropertyType.TEXT, PropertyType.LONG_TEXT, PropertyType.IMAGE -> "STRING"
            PropertyType.NUMBER -> "INT64"
            PropertyType.BOOLEAN -> "BOOLEAN"
            PropertyType.DATE -> "DATE"
            PropertyType.TIMESTAMP -> "TIMESTAMP"
            PropertyType.LIST, PropertyType.MAP, PropertyType.VECTOR, PropertyType.STRUCT -> "STRING"
        }
    }
}