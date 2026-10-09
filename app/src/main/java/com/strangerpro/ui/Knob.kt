package com.strangerpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun Knob(
    label: String,
    value: Float, // 0-1
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var current by remember(value) { mutableStateOf(value) }
    Column(
        modifier = modifier.width(70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(56.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val delta = -dragAmount.y * 0.01f
                        current = (current + delta).coerceIn(0f, 1f)
                        onValueChange(current)
                    }
                }
        ) {
            val stroke = 6.dp.toPx()
            drawCircle(color = Color(0xFF2A2A2A), radius = size.minDimension/2)
            // arc background
            drawArc(
                color = Color(0xFF444444),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // arc value
            drawArc(
                color = Color(0xFF00E5FF),
                startAngle = 135f,
                sweepAngle = 270f * current,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // indicator
            val angle = Math.toRadians((135 + 270*current).toDouble())
            val r = size.minDimension/2 - stroke
            val cx = size.width/2 + (cos(angle) * r * 0.7f).toFloat()
            val cy = size.height/2 + (sin(angle) * r * 0.7f).toFloat()
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.LightGray, textAlign = TextAlign.Center, maxLines = 1)
        Text("${(current*100).toInt()}", fontSize = 9.sp, color = Color.Gray)
    }
}
