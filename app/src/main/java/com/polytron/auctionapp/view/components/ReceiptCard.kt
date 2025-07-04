package com.polytron.auctionapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.Item
import com.polytron.auctionapp.utils.formatRupiah

@Composable
fun ReceiptCard(modifier: Modifier = Modifier, selectedItems: List<Item>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Nota Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("No", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                    Text("Barang", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                    Text("Pembeli", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                    Text("Harga", modifier = Modifier.weight(2f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
                }
                HorizontalDivider()
            }

            itemsIndexed(selectedItems) { index, item ->

                val formattedPrice = remember(item.price) {
                    formatRupiah(item.price)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}", modifier = Modifier.weight(0.5f))
                    Text(item.nameItem.orEmpty(), modifier = Modifier.weight(2f))
                    Text(item.buyer.orEmpty(), modifier = Modifier.weight(2f))
                    Text(formattedPrice, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                val total = selectedItems.sumOf { item ->
                    item.price
                        ?.replace(Regex("\\D"), "") // hapus semua non-digit (misalnya titik)
                        ?.toLongOrNull()
                        ?: 0L
                }

                val formattedPrice = remember(total) {
                    formatRupiah(total.toString())
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(4.5f)
                    )
                    Text(
                        formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(2f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}