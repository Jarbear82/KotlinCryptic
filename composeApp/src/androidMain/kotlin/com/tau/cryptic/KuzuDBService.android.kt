package com.tau.cryptic

import com.kuzudb.Connection
import com.kuzudb.Database
import com.kuzudb.DataTypeID as KuzuTypeId
import com.kuzudb.Value
import com.kuzudb.*
import java.nio.file.Files
import java.nio.file.Paths
import com.tau.cryptic.pages.*
import java.io.File

actual class KuzuDBService actual constructor() {
    private var db: Database? = null
    private var conn: Connection? = null

    actual fun initialize(dbPath: String) {
        try {
            val dbDirectory = File(dbPath).parentFile
            if (!dbDirectory.exists()) {
                dbDirectory.mkdirs()
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
        executeQuery(query, "create edge table '${schema.typeName}')")
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

    actual fun getAllNodes(): List<Map<String, Any?>> {
        val query = "MATCH (n) RETURN n"
        return executeQueryAndParseResults(query, "get all nodes")
    }

    actual fun getAllEdges(): List<Map<String, Any?>> {
        val query = "MATCH ()-[r]-() RETURN r"
        return executeQueryAndParseResults(query, "get all edges")
    }

    actual fun getNode(tableName: String, nodeId: String): Map<String, Any?>? {
        val query = "MATCH (n:$tableName {id: '$nodeId'}) RETURN n"
        return executeQueryAndParseResults(query, "get node from '$tableName'").firstOrNull()
    }

    actual fun getEdge(tableName: String, edgeId: String): Map<String, Any?>? {
        val query = "MATCH ()-[r:$tableName {id: '$edgeId'}]-() RETURN r"
        return executeQueryAndParseResults(query, "get edge from '$tableName'").firstOrNull()
    }

    actual fun deleteEdge(tableName: String, edgeId: String): Boolean {
        val query = "MATCH ()-[r:$tableName {id: '$edgeId'}]-() DELETE r"
        return executeQuery(query, "delete edge from '$tableName'")
    }

    actual fun getNodesByType(tableName: String): List<Map<String, Any?>> {
        val query = "MATCH (n:$tableName) RETURN n"
        return executeQueryAndParseResults(query, "get all nodes of type $tableName")
    }

    actual fun getEdgesByType(tableName: String): List<Map<String, Any?>> {
        val query = "MATCH ()-[r:$tableName]-() RETURN r"
        return executeQueryAndParseResults(query, "get all edges of type $tableName")
    }

    actual fun createNode(tableName: String, properties: Map<String, Any>): Boolean {
        val keys = properties.keys.joinToString(", ")
        val values = properties.values.joinToString(", ") { "'$it'" }
        val query = "CREATE (n:$tableName {$keys: $values})"
        return executeQuery(query, "insert node into '$tableName'")
    }

    actual fun addEdge(tableName: String, fromNodeId: String, toNodeId: String, properties: Map<String, Any>): Boolean {
        val props = properties.entries.joinToString(", ") {
            "${it.key}: '${it.value}'"
        }
        val query = "MATCH (a), (b) WHERE a.id = '$fromNodeId' AND b.id = '$toNodeId' CREATE (a)-[r:$tableName {$props}]->(b)"
        return executeQuery(query, "insert edge into '$tableName'")
    }

    actual fun updateNode(tableName: String, nodeId: String, properties: Map<String, Any>): Boolean {
        val props = properties.entries.joinToString(", ") {
            "n.${it.key} = '${it.value}'"
        }
        val query = "MATCH (n:$tableName {id: '$nodeId'}) SET $props"
        return executeQuery(query, "update node in '$tableName'")
    }

    actual fun updateEdge(tableName: String, edgeId: String, properties: Map<String, Any>): Boolean {
        val props = properties.entries.joinToString(", ") {
            "r.${it.key} = '${it.value}'"
        }
        val query = "MATCH ()-[r:$tableName {id: '$edgeId'}]-() SET $props"
        return executeQuery(query, "update edge in '$tableName'")
    }

    actual fun deleteNode(tableName: String, nodeId: String): Boolean {
        val query = "MATCH (n:$tableName {id: '$nodeId'}) DETACH DELETE n"
        return executeQuery(query, "delete node from '$tableName'")
    }

    actual fun dropTable(schemaTypeName: String): Boolean {
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
            KuzuTypeId.NODE -> {
                val nodeValue = kuzuValue.getValue<Map<String, Value>>()
                val properties = nodeValue["properties"]?.getValue<Map<String, Value>>()
                val label = nodeValue["label"]?.getValue<String>()
                val id = nodeValue["id"]?.getValue<String>()

                val propertyMap = properties?.mapValues { convertKuzuValueToJavaType(it.value) }?.toMutableMap()
                if (label != null) {
                    propertyMap?.put("label", label)
                }
                if (id != null) {
                    propertyMap?.put("id", id)
                }
                propertyMap
            }
            KuzuTypeId.REL -> {
                val relValue = kuzuValue.getValue<Map<String, Value>>()
                val properties = relValue["properties"]?.getValue<Map<String, Value>>()
                val label = relValue["label"]?.getValue<String>()
                val src = relValue["src"]?.getValue<String>()
                val dst = relValue["dst"]?.getValue<String>()

                val propertyMap = properties?.mapValues { convertKuzuValueToJavaType(it.value) }?.toMutableMap()
                if (label != null) {
                    propertyMap?.put("label", label)
                }
                if (src != null) {
                    propertyMap?.put("src", src)
                }
                if (dst != null) {
                    propertyMap?.put("dst", dst)
                }

                propertyMap
            }
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