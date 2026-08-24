package com.mcpetexture.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.floor

enum class Tool { PEN, ERASER, FILL, EYEDROPPER }

data class PixelCanvasState(
    val width: Int = 16,
    val height: Int = 16,
    val pixels: Array<IntArray> = Array(16) { IntArray(16) { 0xFFFFFFFF.toInt() } }
)

@Composable
fun FullPixelEditor(
    initial: PixelCanvasState = PixelCanvasState(),
    onExport: (PixelCanvasState) -> Unit = {}
) {
    var state by remember { mutableStateOf(initial) }
    var tool by remember { mutableStateOf(Tool.PEN) }
    var color by remember { mutableStateOf(Color.Black) }
    var zoom by remember { mutableFloatStateOf(20f) }

    fun paint(x: Int, y: Int) {
        if (x !in 0 until state.width || y !in 0 until state.height) return
        val copy = state.pixels.map { it.clone() }.toTypedArray()
        copy[y][x] = when (tool) {
            Tool.PEN -> color.value.toInt()
            Tool.ERASER -> 0
            Tool.EYEDROPPER -> {
                color = Color(state.pixels[y][x])
                return
            }
            Tool.FILL -> {
                val old = state.pixels[y][x]
                val newValue = color.value.toInt()
                if (old == newValue) return
                val q = ArrayDeque<Pair<Int, Int>>()
                q.add(x to y)
                val visited = HashSet<Pair<Int, Int>>()
                while (q.isNotEmpty()) {
                    val p = q.removeFirst()
                    if (!visited.add(p)) continue
                    val (px, py) = p
                    if (px !in 0 until state.width || py !in 0 until state.height) continue
                    if (state.pixels[py][px] != old) continue
                    copy[py][px] = newValue
                    q.add((px + 1) to py); q.add((px - 1) to py)
                    q.add(px to (py + 1)); q.add(px to (py - 1))
                }
                state = state.copy(pixels = copy)
                return
            }
        }
        state = state.copy(pixels = copy)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("像素编辑器", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(tool == Tool.PEN, { tool = Tool.PEN }, label = { Text("画笔") })
            FilterChip(tool == Tool.ERASER, { tool = Tool.ERASER }, label = { Text("橡皮") })
            FilterChip(tool == Tool.FILL, { tool = Tool.FILL }, label = { Text("填充") })
            FilterChip(tool == Tool.EYEDROPPER, { tool = Tool.EYEDROPPER }, label = { Text("吸管") })
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Color.Black, Color.White, Color.Red, Color.Green, Color.Blue, Color.Yellow).forEach {
                Button(
                    onClick = { color = it },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(Modifier.size(24.dp).background(it))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFDDDDDD))
        ) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(state, tool, color) {
                        detectTapGestures { pos ->
                            val cell = zoom
                            paint(floor(pos.x / cell).toInt(), floor(pos.y / cell).toInt())
                        }
                    }
                    .pointerInput(state, tool, color) {
                        detectDragGestures { change, _ ->
                            val cell = zoom
                            paint(floor(change.position.x / cell).toInt(), floor(change.position.y / cell).toInt())
                        }
                    }
            ) {
                val cell = zoom
                for (y in 0 until state.height) {
                    for (x in 0 until state.width) {
                        val c = Color(state.pixels[y][x])
                        drawRect(
                            color = c,
                            topLeft = Offset(x * cell, y * cell),
                            size = androidx.compose.ui.geometry.Size(cell, cell)
                        )
                        drawRect(
                            color = Color.LightGray,
                            topLeft = Offset(x * cell, y * cell),
                            size = androidx.compose.ui.geometry.Size(cell, cell),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(0.5f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { zoom = (zoom - 4f).coerceAtLeast(8f) }) { Text("−") }
            Text("缩放 ${zoom.toInt()}px")
            Button(onClick = { zoom = (zoom + 4f).coerceAtMost(48f) }) { Text("+") }
            Button(onClick = { onExport(state) }) { Text("导出 PNG") }
        }
    }
}
