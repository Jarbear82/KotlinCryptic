package org.tau.cryptic.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.tau.cryptic.Config
import org.tau.cryptic.pages.*
import kotlin.random.Random

private data class NavItem(val label: String, val icon: ImageVector)

private val mainNavItems = listOf(
    NavItem(label = "Home", icon = Icons.Default.Home),
    NavItem(label = "Graph", icon = Icons.Default.GridOn),
    NavItem(label = "Schema", icon = Icons.Default.Schema)
)

private val actionNavItems = listOf(
    NavItem(label = "Import/Export", icon = Icons.Default.ImportExport)
)

private val configsNavItems = listOf(
    NavItem(label = "Settings", icon = Icons.Default.Settings),
    NavItem(label = "Login", icon = Icons.Default.Login)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                mainNavItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == item.label,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem = item.label
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                actionNavItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == item.label,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem = item.label
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                configsNavItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == item.label,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem = item.label
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (selectedItem == "Home") "Cryptic" else "${Config.selectedNoteGraph?.name ?: ""} - $selectedItem") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply { if (isClosed) open() else close() }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                val activeGraph = Config.selectedNoteGraph

                when (selectedItem) {
                    "Home" -> Home()
                    "Graph" -> {
                        if (activeGraph != null) {
                            Graph(
                                graph = activeGraph,
                                onUpdateNode = { Config.updateNode(it) },
                                onUpdateEdge = { Config.updateEdge(it) },
                                onCreateNode = { Config.addNode(it) },
                                onCreateEdge = { Config.addEdge(it) }
                            )
                        } else {
                            NoGraphSelected()
                        }
                    }
                    "Schema" -> {
                        if (activeGraph != null) {
                            Schema(
                                nodeSchemas = activeGraph.nodeSchemas,
                                edgeSchemas = activeGraph.edgeSchemas,
                                onNodeSchemaUpdate = { Config.updateNodeSchema(it) },
                                onNodeSchemaAdd = { name, props -> Config.addNodeSchema(NodeSchema(Random.nextInt(), name, props)) },
                                onNodeSchemaRemove = { Config.removeNodeSchema(it) },
                                onEdgeSchemaUpdate = { Config.updateEdgeSchema(it) },
                                onEdgeSchemaAdd = { name, props -> Config.addEdgeSchema(EdgeSchema(Random.nextInt(), name, props)) },
                                onEdgeSchemaRemove = { Config.removeEdgeSchema(it) }
                            )
                        } else {
                            NoGraphSelected()
                        }
                    }
                    "Import/Export" -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("Import/Export Page Content") }
                    "Settings" -> Settings()
                    "Login" -> Login()
                }
            }
        }
    }
}

@Composable
private fun NoGraphSelected() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Please select a graph from the Home page.", style = MaterialTheme.typography.headlineSmall)
    }
}