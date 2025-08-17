package org.tau.cryptic

import kotlinx.coroutines.test.runTest
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema
import org.tau.cryptic.pages.PropertyDefinition
import org.tau.cryptic.pages.PropertyType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode
import org.tau.cryptic.pages.PropertyInstance

class KuzuDBServiceIntTest {

    private lateinit var kuzuDBService: KuzuDBService

    @BeforeTest
    fun setup() {
        kuzuDBService = KuzuDBService()
        kuzuDBService.initialize(":memory:")
    }

    @AfterTest
    fun tearDown() {
        kuzuDBService.close()
    }

    @Test
    fun testCreateAndReadNode() = runTest {
        val nodeSchema = NodeSchema(
            id = 0,
            typeName = "Person",
            properties = listOf(
                PropertyDefinition(key = "name", type = PropertyType.TEXT),
                PropertyDefinition(key = "age", type = PropertyType.NUMBER)
            )
        )
        kuzuDBService.createNodeSchema(nodeSchema)

        val properties = mapOf("name" to "John Doe", "age" to 30)
        kuzuDBService.insertNode("Person", properties)

        val result = kuzuDBService.executeQuery("MATCH (p:Person) RETURN p.name AS name, p.age AS age")
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0]["name"])
        assertEquals(30L, result[0]["age"])
    }

    @Test
    fun testCreateAndReadEdge() = runTest {
        val personSchema = NodeSchema(
            id = 0,
            typeName = "Person",
            properties = listOf(PropertyDefinition(key = "name", type = PropertyType.TEXT))
        )
        kuzuDBService.createNodeSchema(personSchema)

        kuzuDBService.insertNode("Person", mapOf("name" to "Alice"))
        kuzuDBService.insertNode("Person", mapOf("name" to "Bob"))

        val knowsSchema = EdgeSchema(
            id = 0,
            typeName = "KNOWS",
            properties = listOf(PropertyDefinition(key = "since", type = PropertyType.NUMBER))
        )
        kuzuDBService.createEdgeSchema(knowsSchema, "Person", "Person")

        val aliceId = kuzuDBService.executeQuery("MATCH (p:Person WHERE p.name = 'Alice') RETURN p._id as id")[0][ "id"].toString()
        val bobId = kuzuDBService.executeQuery("MATCH (p:Person WHERE p.name = 'Bob') RETURN p._id as id")[0]["id"].toString()

        val edge = GraphEdge(
            typeName = "KNOWS",
            sourceNodeId = aliceId,
            targetNodeId = bobId,
            properties = mutableListOf(PropertyInstance(key = "since", value = 2021))
        )
        kuzuDBService.insertEdge(edge)

        val result = kuzuDBService.executeQuery("MATCH (a:Person)-[r:KNOWS]->(b:Person) RETURN a.name as a_name, r.since as since, b.name as b_name")

        assertEquals(1, result.size)
        assertEquals("Alice", result[0]["a_name"])
        assertEquals(2021L, result[0]["since"])
        assertEquals("Bob", result[0]["b_name"])
    }

    @Test
    fun testUpdateAndVerifyNode() = runTest {
        val nodeSchema = NodeSchema(
            id = 0,
            typeName = "Person",
            properties = listOf(
                PropertyDefinition(key = "name", type = PropertyType.TEXT),
                PropertyDefinition(key = "age", type = PropertyType.NUMBER)
            )
        )
        kuzuDBService.createNodeSchema(nodeSchema)

        kuzuDBService.insertNode("Person", mapOf("name" to "Jane Doe", "age" to 25))
        val personId = kuzuDBService.executeQuery("MATCH (p:Person) RETURN p._id as id")[0]["id"].toString()

        val updatedNode = GraphNode(
            id = personId,
            typeName = "Person",
            properties = mutableListOf(
                PropertyInstance(key = "name", value = "Jane Smith"),
                PropertyInstance(key = "age", value = 26)
            )
        )
        kuzuDBService.updateNode(updatedNode)

        val result = kuzuDBService.executeQuery("MATCH (p:Person) RETURN p.name AS name, p.age AS age")
        assertEquals(1, result.size)
        assertEquals("Jane Smith", result[0]["name"])
        assertEquals(26L, result[0]["age"])
    }

    @Test
    fun testDeleteAndVerifyNode() = runTest {
        val nodeSchema = NodeSchema(
            id = 0,
            typeName = "Person",
            properties = listOf(PropertyDefinition(key = "name", type = PropertyType.TEXT))
        )
        kuzuDBService.createNodeSchema(nodeSchema)
        kuzuDBService.insertNode("Person", mapOf("name" to "Delete Me"))

        val personId = kuzuDBService.executeQuery("MATCH (p:Person) RETURN p._id as id")[0]["id"].toString()
        kuzuDBService.deleteNode("Person", personId)

        val result = kuzuDBService.executeQuery("MATCH (p:Person) RETURN p")
        assertEquals(0, result.size)
    }
}