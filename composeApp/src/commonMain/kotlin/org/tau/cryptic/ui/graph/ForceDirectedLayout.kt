package org.tau.cryptic.ui.graph

import androidx.compose.ui.geometry.Offset
import org.tau.cryptic.pages.GraphEdge
import org.tau.cryptic.pages.GraphNode
import kotlin.math.sqrt

class ForceDirectedLayout {

    suspend fun calculateLayout(
        nodes: List<GraphNode>,
        edges: List<GraphEdge>,
        pinnedNodes: Map<String, Offset>
    ): Map<String, Offset> {
        val positions = nodes.associate {
            it.id to (pinnedNodes[it.id] ?: Offset(
                (0..500).random().toFloat(),
                (0..500).random().toFloat()
            ))
        }.toMutableMap()

        repeat(100) {
            val forces = mutableMapOf<String, Offset>()
            nodes.forEach { forces[it.id] = Offset.Zero }

            // Repulsive forces
            for (i in nodes.indices) {
                for (j in i + 1 until nodes.size) {
                    val node1 = nodes[i]
                    val node2 = nodes[j]
                    val pos1 = positions[node1.id]!!
                    val pos2 = positions[node2.id]!!
                    val delta = pos1 - pos2
                    val distance = sqrt(delta.x * delta.x + delta.y * delta.y)
                    if (distance > 0) {
                        val repulsiveForce = 1000f / (distance * distance)
                        forces[node1.id] = forces[node1.id]!! + (delta / distance) * repulsiveForce
                        forces[node2.id] = forces[node2.id]!! - (delta / distance) * repulsiveForce
                    }
                }
            }

            // Attractive forces
            edges.forEach { edge ->
                val sourcePos = positions[edge.sourceNodeId]!!
                val targetPos = positions[edge.targetNodeId]!!
                val delta = sourcePos - targetPos
                val distance = sqrt(delta.x * delta.x + delta.y * delta.y)
                if (distance > 0) {
                    val attractiveForce = 0.1f * distance
                    forces[edge.sourceNodeId] = forces[edge.sourceNodeId]!! - (delta / distance) * attractiveForce
                    forces[edge.targetNodeId] = forces[edge.targetNodeId]!! + (delta / distance) * attractiveForce
                }
            }

            // Update positions
            nodes.forEach { node ->
                if (!pinnedNodes.containsKey(node.id)) {
                    positions[node.id] = positions[node.id]!! + forces[node.id]!!
                }
            }
        }
        return positions
    }
}