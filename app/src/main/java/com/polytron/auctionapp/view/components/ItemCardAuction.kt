package com.polytron.auctionapp.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ItemCardAuction(
    modifier: Modifier = Modifier,
    item: Item,
    onNameChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf("") }
    var namePerson by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Nama Item
            Text(text = item.nameItem ?: "Nama Item", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Informasi kode dan harga dalam kolom (lebih terbaca)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column() {
                    Text("Kode Item", style = MaterialTheme.typography.labelSmall)
                    Text(item.codeItem ?: "-", style = MaterialTheme.typography.bodyLarge)
                }

                Column() {
                    Text("Harga Awal", style = MaterialTheme.typography.labelSmall)
                    Text("Rp ${item.basePrice}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Input Nama Pemenang
            OutlinedTextField(
                value = namePerson,
                onValueChange = {
                    namePerson = it
                    onNameChanged(it)
                },
                label = { Text("Nama Pemenang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interaksi natural tanpa Button
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
                    Text(
                        "Input Harga Baru",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Expand TextField + Batal
            AnimatedVisibility(visible = expanded) {
                Column {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = {
                            priceInput = it
                            onPriceChanged(it)
                        },
                        label = { Text("Harga Baru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                priceInput = ""
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Batal Input Harga",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}





