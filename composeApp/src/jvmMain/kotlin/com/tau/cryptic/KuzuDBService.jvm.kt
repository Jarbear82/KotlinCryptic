package com.tau.cryptic

import com.kuzudb.Connection
import com.kuzudb.Database
import com.kuzudb.DataTypeID as KuzuTypeId
import com.kuzudb.Value
import com.kuzudb.*
import java.nio.file.Files
import java.nio.file.Paths
import com.tau.cryptic.pages.*


actual class KuzuDBService actual constructor() {
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

    /**
     * Get all nodes
     */
    actual fun getAllNodes(): List<Map<String, Any?>> {
        val query = "MATCH (n) RETURN n"
        return executeQueryAndParseResults(query, "get all nodes")
    }

    /**
     * Get all edges
     */
    actual fun getAllEdges(): List<Map<String, Any?>> {
        val query = "MATCH ()-[r]-() RETURN r"
        return executeQueryAndParseResults(query, "get all nodes")
    }

    /**
     * Creates a node using a prepared statement to safely handle properties.
     */
    actual fun createNode(tableName: String, properties: Map<String, Any>): Boolean {
        // Build property placeholders, e.g., "{id: $id, name: $name}"
        val propertyPlaceholders = properties.keys.joinToString(", ") { key ->
            val sanitizedKey = key.replace(" ", "_")
            "$sanitizedKey: \$$sanitizedKey"
        }
        // Table names cannot be parameters, so they are part of the query string.
        val query = "CREATE (n:$tableName {$propertyPlaceholders})"

        return try {
            // Build the parameter map where keys match the placeholders.
            val params = properties.entries.associate { (key, value) ->
                key.replace(" ", "_") to Value(value)
            }
            // Prepare the statement.
            val preparedStatement = conn?.prepare(query)
            // Execute with the parameters.
            conn?.execute(preparedStatement, params)
            println("Successfully created node in '$tableName'")
            true
        } catch (e: Exception) {
            println("Failed to create node in '$tableName': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a node using a prepared statement to safely handle the node ID.
     */
    actual fun deleteNode(tableName: String, nodeId: String): Boolean {
        // Table names cannot be parameters; the node ID will be a parameter.
        val query = "MATCH (n:$tableName {id: \$nodeId}) DETACH DELETE n"
        return try {
            // Prepare the statement.
            val preparedStatement = conn?.prepare(query)
            // Create the parameter map. The key "nodeId" matches "$nodeId" in the query.
            val params = mapOf("nodeId" to Value(nodeId))
            // Execute with the parameters.
            conn?.execute(preparedStatement, params)
            println("Successfully deleted node from '$tableName'")
            true
        } catch (e: Exception) {
            println("Failed to delete node from '$tableName': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Drops a table. Note: Table names cannot be parameterized in Cypher.
     */
    actual fun dropTable(schemaTypeName: String): Boolean {
        // Schema identifiers (like table names) cannot be parameters.
        // Therefore, we use string formatting and execute the query directly.
        val query = "DROP TABLE $schemaTypeName"
        return executeQuery(query, "drop table '$schemaTypeName'")
    }

    actual fun executeQuery(query: String): List<Map<String, Any?>> {
        return executeQueryAndParseResults(query, "custom query")
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
                    val row = it.getNext()
                    val rowMap = mutableMapOf<String, Any?>()
                    for (i in 0 until it.numColumns) {
                        val columnName = it.getColumnName(i)
                        val value = row.getValue(i)
                        rowMap[columnName] = convertKuzuValueToJavaType(value)
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

    private fun convertKuzuValueToJavaType(kuzuValue: Value): Any? {
        return when (kuzuValue.dataType.id) {
            KuzuTypeId.INT8 -> kuzuValue.getValue<Byte>()
            KuzuTypeId.INT16 -> kuzuValue.getValue<Short>()
            KuzuTypeId.INT32 -> kuzuValue.getValue<Int>()
            KuzuTypeId.INT64 -> kuzuValue.getValue<Long>()
            KuzuTypeId.FLOAT -> kuzuValue.getValue<Float>()
            KuzuTypeId.DOUBLE -> kuzuValue.getValue<Double>()
            KuzuTypeId.BOOL -> kuzuValue.getValue<Boolean>()
            KuzuTypeId.STRING -> kuzuValue.getValue<String>()
            KuzuTypeId.DATE -> kuzuValue.getValue<String>()
            KuzuTypeId.TIMESTAMP -> kuzuValue.getValue<String>()
            KuzuTypeId.INTERVAL -> kuzuValue.getValue<String>()
            KuzuTypeId.LIST -> {
                val listValues = kuzuValue.getValue<List<Value>>()
                listValues.map { element -> convertKuzuValueToJavaType(element) }
            }
            KuzuTypeId.STRUCT -> {
                val structMap = mutableMapOf<String, Any?>()
                kuzuValue.getValue<Map<String, Value>>().forEach { (key, structValue) ->
                    structMap[key] = convertKuzuValueToJavaType(structValue)
                }
                structMap
            }
            KuzuTypeId.MAP -> {
                val mapValues = mutableMapOf<String, Any?>()
                kuzuValue.getValue<Map<String, Value>>().forEach { (key, mapValue) ->
                    mapValues[key] = convertKuzuValueToJavaType(mapValue)
                }
                mapValues
            }
            else -> {
                println("Unhandled Kuzu data type in conversion: ${kuzuValue.dataType.id}")
                null
            }
        }
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