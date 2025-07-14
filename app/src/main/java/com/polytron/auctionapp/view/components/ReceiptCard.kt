package com.polytron.auctionapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ReceiptCard(
    modifier: Modifier = Modifier,
    selectedItems: List<ItemResponse>,
    onDeleteItem: (ItemResponse) -> Unit
) {
    val backgroundColor = Color(0xFFFDFDFD)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Nota Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No", Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                    Text("Kode barang", Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                    Text("Pembeli", Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                    Text("Harga", Modifier.weight(2f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End)
                    Spacer(Modifier.weight(0.5f)) // untuk delete icon
                }

                HorizontalDivider(
                    Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
            }

            // Data rows
            itemsIndexed(selectedItems) { index, item ->
                val formattedPrice = formatRupiah(item.price)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${index + 1}", Modifier.weight(0.5f))
                    Text(item.codeItem.orEmpty(), Modifier.weight(2f))
                    Text(item.buyer.orEmpty(), Modifier.weight(2f))
                    Text(formattedPrice, Modifier.weight(2f), textAlign = TextAlign.End)

                    IconButton(
                        onClick = { onDeleteItem(item) },
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus item",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Total
            item {
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Spacer(Modifier.height(12.dp))

                val total = selectedItems.sumOf { item ->
                    item.price?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
                }
                val formattedTotal = formatRupiah(total.toString())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text( // Total gabungan 3 kolom pertama
                        text = "Total",
                        modifier = Modifier.weight(4.5f), // 0.5 + 2 + 2 (No + Barang + Pembeli)
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Text(
                        text = formattedTotal,
                        modifier = Modifier.weight(2f), // sejajar dengan Harga
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )

                    Spacer(Modifier.weight(0.5f)) // sejajar dengan kolom delete
                }
            }
        }
    }
}

