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
import org.tau.cryptic.pages.*
import org.tau.cryptic.data.AppContainer
import org.tau.cryptic.ui.viewmodel.GraphViewModel
import org.tau.cryptic.ui.viewmodel.HomeViewModel
import org.tau.cryptic.ui.viewmodel.SchemaViewModel
import org.tau.cryptic.ui.viewmodel.QueryViewModel

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
fun NavDrawer(appContainer: AppContainer) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Home") }

    val homeViewModel = remember { HomeViewModel(appContainer.graphRepository) }
    val graphViewModel = remember { GraphViewModel(appContainer.graphRepository, appContainer.layoutManager) }
    val schemaViewModel = remember { SchemaViewModel(appContainer.graphRepository) }
    val queryViewModel = appContainer.queryViewModel
    val selectedNoteGraph by homeViewModel.selectedNoteGraph.collectAsState()


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
                    title = { Text(if (selectedItem == "Home") "Cryptic" else "${selectedNoteGraph?.name ?: ""} - $selectedItem") },
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
                when (selectedItem) {
                    "Home" -> Home(homeViewModel)
                    "Graph" -> {
                        if (selectedNoteGraph != null) {
                            Graph(
                                graphViewModel = graphViewModel,
                                graph = selectedNoteGraph!!,
                                queryViewModel = queryViewModel
                            )
                        } else {
                            NoGraphSelected()
                        }
                    }
                    "Schema" -> {
                        if (selectedNoteGraph != null) {
                            Schema(
                                schemaViewModel = schemaViewModel,
                                nodeSchemas = selectedNoteGraph!!.nodeSchemas,
                                edgeSchemas = selectedNoteGraph!!.edgeSchemas,
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