package com.example.projectbmi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: androidx.navigation.NavController, vm: HistoryViewModel = viewModel()) {
    Log.d("HistoryScreen", "Rendering history screen")
    val list = vm.history.collectAsState().value
    Log.d("HistoryScreen", "History loaded: ${list.size} records")

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("History") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No history records yet.", color = Color(0xFF6B7280))
                }
            } else {
                // Show a small trend chart for recent BMI (oldest->newest left to right)
                val recent = list.take(30).reversed()
                TrendChart(records = recent, modifier = Modifier.fillMaxWidth().height(140.dp))
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(list) { idx, item ->
                        // Compare this item to the next one (older) to compute delta
                        val older = list.getOrNull(idx + 1)
                        HistoryRow(item, older)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChart(records: List<BMIRecord>, modifier: Modifier = Modifier) {
    if (records.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No trend data", color = Color(0xFF6B7280))
        }
        return
    }

    // prepare values (oldest -> newest)
    val values = records.map { it.bmi }
    val minV = values.minOrNull() ?: 0f
    val maxV = values.maxOrNull() ?: 0f
    val padding = (maxV - minV) * 0.12f + 0.1f // visual padding

    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFFBF5), modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
            val w = size.width
            val h = size.height
            val n = values.size
            val usableMin = minV - padding
            val usableMax = maxV + padding
            val range = (usableMax - usableMin).coerceAtLeast(0.1f)

            // points
            val stepX = if (n > 1) w / (n - 1) else w
            val points = values.mapIndexed { i, v ->
                val x = i * stepX
                val norm = (v - usableMin) / range
                val y = h - (norm * h)
                Offset(x, y)
            }

            // draw grid lines
            val gridColor = Color(0xFFEEF2FF)
            for (i in 0..2) {
                val yy = h * i / 2f
                drawLine(color = gridColor, start = Offset(0f, yy), end = Offset(w, yy), strokeWidth = 1f)
            }

            // area fill path (soft amber)
            val areaPath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, h)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, h)
                    close()
                }
            }
            drawPath(path = areaPath, color = Color(0xFFFFF3D9).copy(alpha = 0.7f), style = Fill)

            // line (deep amber/orange)
            val lineColor = Color(0xFFEA580C)
            drawPath(path = Path().apply { if (points.isNotEmpty()) { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } } },
                color = lineColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // points and highlight last (white outer + colored inner)
            points.forEachIndexed { idx, p ->
                val isLast = idx == points.lastIndex
                drawCircle(color = Color.White, radius = if (isLast) 7f else 5f, center = p)
                drawCircle(color = if (isLast) Color(0xFFB45309) else lineColor, radius = if (isLast) 4f else 3f, center = p)
            }
        }
    }
}

@Composable
fun HistoryRow(item: BMIRecord, older: BMIRecord?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                Text(sdf.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("BMI ${String.format("%.1f", item.bmi)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("- ${item.category}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    // Delta vs older
                    older?.let {
                        val delta = item.bmi - it.bmi
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (delta > 0) "▲ ${String.format("%.1f", kotlin.math.abs(delta))}" else if (delta < 0) "▼ ${String.format("%.1f", kotlin.math.abs(delta))}" else "—",
                            color = if (delta > 0) Color(0xFFDC2626) else if (delta < 0) Color(0xFF16A34A) else Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("${item.gender} • ${item.heightCm} cm • ${String.format("%.1f", item.weightKg)} kg", style = MaterialTheme.typography.bodySmall)
            }
            // Optional quick actions or badges
            Column(horizontalAlignment = Alignment.End) {
                Text(text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(item.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            }
        }
    }
}
