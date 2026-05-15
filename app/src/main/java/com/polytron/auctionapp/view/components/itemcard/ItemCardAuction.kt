package com.polytron.auctionapp.view.components.itemcard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.polytron.auctionapp.domain.model.ItemsUserAuction
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@Composable
fun ItemCardAuction(
    modifier: Modifier = Modifier,
    item: ItemResponse,
    currentBuyer: String,
    currentPrice: String,
    auctionUsers: List<ItemsUserAuction> = emptyList(),
    onNameChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onCancelPriceInput: () -> Unit,
    onClickDelete: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(currentPrice.isNotBlank()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val backgroundColor = Color(0xFFFDFDFD)
    val formattedPrice = remember(item.basePrice) { formatRupiah(item.basePrice) }

    var rawPrice by remember { mutableStateOf("") }
    var formattedInput by remember { mutableStateOf("") }

    val filteredUsers = remember(currentBuyer, auctionUsers) {
        if (currentBuyer.isBlank()) emptyList()
        else auctionUsers
            .filter { it.name?.contains(currentBuyer, ignoreCase = true) == true }
            .distinctBy { it.name?.lowercase() }
            .take(5)
    }

    // Sinkronisasi saat currentPrice berubah dari luar
    LaunchedEffect(currentPrice) {
        rawPrice = currentPrice
        formattedInput = formatCurrencyInput(currentPrice)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.nameItem ?: "Nama Item", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onClickDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Info kode dan harga awal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kode Item", style = MaterialTheme.typography.labelSmall)
                    Text(item.codeItem ?: "-", style = MaterialTheme.typography.bodyLarge)
                }

                Column {
                    Text("Harga Awal", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Input Nama Pemenang
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
            ) {
                OutlinedTextField(
                    value = currentBuyer,
                    onValueChange = { input ->
                        val capitalized = input
                            .split(" ")
                            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.titlecase() } }
                        onNameChanged(capitalized)
                        isDropdownExpanded = true
                    },
                    label = { Text("Nama Pemenang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                DropdownMenu(
                    expanded = isDropdownExpanded && filteredUsers.isNotEmpty(),
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                ) {
                    filteredUsers.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.name.orEmpty()) },
                            onClick = {
                                onNameChanged(user.name.orEmpty())
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol buka input harga
            if (!expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Input Harga Baru", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Input harga baru
            AnimatedVisibility(visible = expanded) {
                Column {
                    OutlinedTextField(
                        value = formattedInput,
                        onValueChange = {
                            rawPrice = it.filter(Char::isDigit)
                            formattedInput = formatCurrencyInput(rawPrice)
                            onPriceChanged(rawPrice)
                        },
                        label = { Text("Harga Baru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Batal input harga
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                rawPrice = ""
                                formattedInput = ""
                                onPriceChanged("")
                                onCancelPriceInput()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Batal Input Harga", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}


