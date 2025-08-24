package com.tau.cryptic.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A simple interface requiring only a unique ID.
 */
interface Identifiable {
    val id: Any
}

/**
 * A reusable list that now accepts any composable content for its items.
 *
 * @param itemContent A composable lambda to render the content of each item.
 */
@Composable
fun <T : Identifiable> DeletableSelectableListView(
    items: List<T>,
    selectedItem: T?,
    onItemClick: (T) -> Unit,
    onDeleteItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ListItemRow(
                isSelected = item.id == selectedItem?.id,
                onItemClick = { onItemClick(item) },
                onDeleteClick = { onDeleteItemClick(item) }
            ) {
                // Pass the item-specific content to the row
                itemContent(item)
            }
        }
    }
}

/**
 * The row accepts its main content as a composable lambda.
 */
@Composable
private fun ListItemRow(
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onItemClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render the provided content, giving it most of the space
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}