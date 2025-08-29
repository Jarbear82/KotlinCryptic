package com.tau.cryptic

import com.kuzudb.Connection
import com.kuzudb.Database
import com.kuzudb.DataTypeID as KuzuTypeId
import com.kuzudb.Value
import com.kuzudb.*
import java.nio.file.Files
import java.nio.file.Paths
import com.tau.cryptic.pages.*
import java.math.BigInteger

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
            print(queryResult.toString())
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
        if (kuzuValue.isNull) {
            return null
        }
        return when (val typeId = kuzuValue.dataType.id) {
            KuzuTypeId.NODE -> {
                val propertyMap = mutableMapOf<String, Any?>()
                propertyMap["id"] = ValueNodeUtil.getID(kuzuValue).toString()
                propertyMap["label"] = ValueNodeUtil.getLabelName(kuzuValue)
                for (i in 0 until ValueNodeUtil.getPropertySize(kuzuValue)) {
                    val name = ValueNodeUtil.getPropertyNameAt(kuzuValue, i)
                    val value = ValueNodeUtil.getPropertyValueAt(kuzuValue, i)
                    propertyMap[name] = convertKuzuValueToJavaType(value)
                }
                propertyMap
            }
            KuzuTypeId.REL -> {
                val propertyMap = mutableMapOf<String, Any?>()
                propertyMap["id"] = ValueRelUtil.getID(kuzuValue).toString()
                propertyMap["label"] = ValueRelUtil.getLabelName(kuzuValue)
                propertyMap["src"] = ValueRelUtil.getSrcID(kuzuValue).toString()
                propertyMap["dst"] = ValueRelUtil.getDstID(kuzuValue).toString()
                for (i in 0 until ValueRelUtil.getPropertySize(kuzuValue)) {
                    val name = ValueRelUtil.getPropertyNameAt(kuzuValue, i)
                    val value = ValueRelUtil.getPropertyValueAt(kuzuValue, i)
                    propertyMap[name] = convertKuzuValueToJavaType(value)

                }
                propertyMap
            }
            KuzuTypeId.RECURSIVE_REL -> {
                val recursiveRelMap = mutableMapOf<String, Any?>()
                val nodes = ValueRecursiveRelUtil.getNodeList(kuzuValue)
                val rels = ValueRecursiveRelUtil.getRelList(kuzuValue)
                recursiveRelMap["nodes"] = convertKuzuValueToJavaType(nodes)
                recursiveRelMap["rels"] = convertKuzuValueToJavaType(rels)
                recursiveRelMap
            }
            KuzuTypeId.BOOL -> kuzuValue.getValue<Boolean>()
            KuzuTypeId.INT64 -> kuzuValue.getValue<Long>()
            KuzuTypeId.INT32 -> kuzuValue.getValue<Int>()
            KuzuTypeId.INT16 -> kuzuValue.getValue<Short>()
            KuzuTypeId.INT8 -> kuzuValue.getValue<Byte>()
            KuzuTypeId.UINT64 -> kuzuValue.getValue<ULong>()
            KuzuTypeId.UINT32 -> kuzuValue.getValue<UInt>()
            KuzuTypeId.UINT16 -> kuzuValue.getValue<UShort>()
            KuzuTypeId.UINT8 -> kuzuValue.getValue<UByte>()
            KuzuTypeId.INT128 -> kuzuValue.getValue<BigInteger>().toString()
            KuzuTypeId.DOUBLE -> kuzuValue.getValue<Double>()
            KuzuTypeId.FLOAT -> kuzuValue.getValue<Float>()
            KuzuTypeId.DATE, KuzuTypeId.TIMESTAMP, KuzuTypeId.TIMESTAMP_MS, KuzuTypeId.TIMESTAMP_NS, KuzuTypeId.TIMESTAMP_SEC, KuzuTypeId.TIMESTAMP_TZ, KuzuTypeId.INTERVAL -> TODO()
            KuzuTypeId.STRING -> kuzuValue.getValue<String>()
            KuzuTypeId.ANY -> TODO()
            KuzuTypeId.SERIAL -> TODO()
            KuzuTypeId.DECIMAL -> TODO()
            KuzuTypeId.INTERNAL_ID -> TODO()
            KuzuTypeId.BLOB -> TODO()
            KuzuTypeId.LIST -> TODO()
            KuzuTypeId.ARRAY -> TODO()
            KuzuTypeId.STRUCT -> TODO()
            KuzuTypeId.MAP -> TODO()
            KuzuTypeId.UNION -> TODO()
            KuzuTypeId.UUID -> TODO()
            else -> {
                println("Unhandled Kuzu data type in conversion: $typeId")
                kuzuValue.toString()
            }
        }
    }

    private fun mapPropertyType(type: PropertyType): String {
        return when (type) {
            PropertyType.STRING -> "STRING"
            PropertyType.INT64 -> "INT64"
            PropertyType.DOUBLE -> "DOUBLE"
            PropertyType.BOOL -> "BOOL"
            PropertyType.DATE -> "DATE"
            PropertyType.TIMESTAMP -> "TIMESTAMP"
            PropertyType.INTERVAL -> "INTERVAL"
            PropertyType.INTERNAL_ID -> "INTERNAL_ID"
            PropertyType.BLOB -> "BLOB"
            PropertyType.UUID -> "UUID"
            PropertyType.LIST -> "LIST"
            PropertyType.MAP -> "MAP"
            PropertyType.STRUCT -> "STRUCT"
        }
    }
}