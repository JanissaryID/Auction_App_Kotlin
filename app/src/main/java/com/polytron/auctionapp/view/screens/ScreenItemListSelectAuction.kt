package com.polytron.auctionapp.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.ui.viewmodel.AuctionViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.itemcard.ItemCardSelectAuction
import org.koin.compose.viewmodel.koinViewModel

private fun normalizeAuctionGroupName(name: String?): String {
    if (name.isNullOrBlank()) return "Tanpa Nama"
    return name.replace(Regex("\\s*[-â€“]?\\s*\\d+$"), "").trim().ifBlank { "Tanpa Nama" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemListSelectAuction(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    auctionViewModel: AuctionViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    navBack: () -> Unit,
) {
    val items by itemsViewModel.items.collectAsState()
    val isLoading by itemsViewModel.isLoading.collectAsState()
    val selectedItems by auctionViewModel.selectedItems.collectAsState()

    val filteredItemsStatZero = items.filter { it.status == 0 }
    var searchQuery by remember { mutableStateOf("") }
    var showSelectionMenu by remember { mutableStateOf(false) }

    val filteredItems = filteredItemsStatZero.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }
    val groupedItems = filteredItems.groupBy { normalizeAuctionGroupName(it.nameItem) }
    val sortedGroups = groupedItems.toSortedMap().entries.toList()
    val selectionMenuItemCount = sortedGroups.size + 1
    val selectionMenuVisibleItems = selectionMenuItemCount.coerceAtMost(4).coerceAtLeast(1)
    val selectionMenuItemHeight = 48.dp
    val selectionMenuHeight = selectionMenuItemHeight * selectionMenuVisibleItems.toFloat()
    val showSelectionScrollbar = selectionMenuItemCount > 4
    val selectionMenuScrollState = rememberScrollState()
    val scrollbarTrackHeight = selectionMenuHeight - 12.dp
    val scrollbarThumbHeight = (scrollbarTrackHeight * (selectionMenuVisibleItems.toFloat() / selectionMenuItemCount.toFloat())).coerceAtLeast(32.dp)
    val scrollbarProgress = if (showSelectionScrollbar) {
        val maxScroll = selectionMenuScrollState.maxValue.coerceAtLeast(1)
        (selectionMenuScrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f)
    } else {
        0f
    }
    val scrollbarThumbOffset = (scrollbarTrackHeight - scrollbarThumbHeight) * scrollbarProgress

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Pilih Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                isRefreshing = isLoading,
                onRefresh = { itemsViewModel.fetchItems() },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = selectedItems.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                ExtendedFloatingActionButton(
                    onClick = { navBack() },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("${selectedItems.size} Barang Terpilih") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Nama atau Kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )
            if (filteredItems.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedItems.isNotEmpty()) "${selectedItems.size} barang dipilih" else "Pilih cepat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box {
                            TextButton(onClick = { showSelectionMenu = true }) {
                                Text("Pilih")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showSelectionMenu,
                                onDismissRequest = { showSelectionMenu = false }
                            ) {
                                Box(modifier = Modifier.height(selectionMenuHeight)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(end = if (showSelectionScrollbar) 8.dp else 0.dp)
                                            .verticalScroll(selectionMenuScrollState)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Pilih Semua (${filteredItems.size})") },
                                            onClick = {
                                                auctionViewModel.setSelectedItems(filteredItems)
                                                showSelectionMenu = false
                                            }
                                        )
                                        sortedGroups.forEach { (groupName, groupedItemList) ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "Group: $groupName (${groupedItemList.size})",
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                onClick = {
                                                    auctionViewModel.setSelectedItems(groupedItemList)
                                                    showSelectionMenu = false
                                                }
                                            )
                                        }
                                    }
                                    if (showSelectionScrollbar) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 4.dp)
                                                .width(3.dp)
                                                .height(scrollbarTrackHeight)
                                                .background(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 6.dp, end = 4.dp)
                                                .offset(y = scrollbarThumbOffset)
                                                .width(3.dp)
                                                .height(scrollbarThumbHeight)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        if (selectedItems.isNotEmpty()) {
                            TextButton(onClick = { auctionViewModel.clearSelectedItems() }) {
                                Text("Batal")
                            }
                        }
                    }
                }
            }
            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Barang tidak ditemukan")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(filteredItems, key = { it.id ?: "" }) { item ->
                        val isSelected = selectedItems.any { it.id == item.id }
                        ItemCardSelectAuction(
                            item = item,
                            isSelected = isSelected,
                            onSelectToggle = {
                                if (isSelected) auctionViewModel.removeSelectedItem(item)
                                else auctionViewModel.addSelectedItem(item)
                            }
                        )
                    }
                }
            }
        }
    }
}
