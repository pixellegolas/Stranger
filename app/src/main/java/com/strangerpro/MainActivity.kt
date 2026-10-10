
package com.strangerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerpro.audio.AudioEngine
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.init(48000)
        audioEngine.start()
        setContent {
            MaterialTheme {
                var mode by remember { mutableStateOf(0) }
                var activeNotes by remember { mutableStateOf(setOf<Int>()) }
                var encoderVals by remember { mutableStateOf(listOf(0.35f, 0.58f, 0.42f, 0.71f)) }
                var selectedPreset by remember { mutableStateOf(2) }
                var tapeTracks by remember { mutableStateOf(listOf(true, true, false, false)) }
                var isRec by remember { mutableStateOf(false) }
                var isPlay by remember { mutableStateOf(false) }

                val infinite = rememberInfiniteTransition(label="oled")
                val cloudOffset by infinite.animateFloat(0f, 14f, infiniteRepeatable(tween(2800, easing=LinearEasing), RepeatMode.Reverse), label="cloud")
                val sunRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing=LinearEasing), RepeatMode.Restart), label="sun")
                val wavePhase by infinite.animateFloat(0f, 6.28f, infiniteRepeatable(tween(1600, easing=LinearEasing), RepeatMode.Restart), label="wave")
                val tapeRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(2200, easing=LinearEasing), RepeatMode.Restart), label="tape")

                LaunchedEffect(mode) { audioEngine.setEngineType(if(mode==2) 0 else mode) }
                LaunchedEffect(encoderVals) {
                    audioEngine.setParam(0, encoderVals[0])
                    audioEngine.setParam(1, encoderVals[1])
                    audioEngine.setAlgorithm((encoderVals[2]*31f).toInt())
                    audioEngine.setFeedback(encoderVals[3])
                }

                // TOP DOWN FLAT LAY - like mockup, no 3D perspective, directly above
                BoxWithConstraints(
                    Modifier.fillMaxSize().background(Color(0xFFF2EEE6)).padding(8.dp),
                    contentAlignment=Alignment.Center
                ) {
                    val isTablet = maxWidth > 600.dp
                    val deviceWidth = if(isTablet) maxWidth * 0.94f else maxWidth

                    Box(
                        Modifier.width(deviceWidth)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEFEFE))
                            .border(1.dp, Color(0xFFE8E6E1), RoundedCornerShape(12.dp))
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            // TOP ROW - flat top down, 2 screw dots + 4 small orange + large orange knob - all flat circles from above
                            Box(
                                Modifier.fillMaxWidth().height(if(isTablet) 88.dp else 72.dp).background(Color(0xFFFEFEFE)).padding(horizontal=16.dp),
                                contentAlignment=Alignment.Center
                            ) {
                                // Screw dots - flat top down
                                Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopStart).offset(y=10.dp))
                                Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopEnd).offset(y=10.dp))

                                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val isActive = mode == idx
                                            Box(
                                                Modifier.size(18.dp).clip(CircleShape).background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B)).border(1.dp, Color.Black.copy(0.08f), CircleShape).clickable { mode = idx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                            }
                                        }
                                    }
                                    // Large orange knob - FLAT TOP DOWN with knurling around edge like mockup
                                    Box(Modifier.size(if(isTablet) 64.dp else 54.dp), contentAlignment=Alignment.Center) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            // Flat orange circle - top down
                                            drawCircle(Color(0xFFFF5A1F), radius=size.minDimension/2)
                                            // Knurling - small ticks around circumference - flat from above
                                            for(i in 0..35) {
                                                val angle = i * 10f * PI.toFloat() / 180f
                                                val r1 = size.minDimension/2 - 3.dp.toPx()
                                                val r2 = size.minDimension/2 - 0.5f.dp.toPx()
                                                val x1 = center.x + cos(angle)*r1
                                                val y1 = center.y + sin(angle)*r1
                                                val x2 = center.x + cos(angle)*r2
                                                val y2 = center.y + sin(angle)*r2
                                                drawLine(Color.Black.copy(0.15f), Offset(x1,y1), Offset(x2,y2), 0.8f)
                                            }
                                            // Inner slightly lighter circle - flat shading
                                            drawCircle(Color(0xFFFF7A33), radius=size.minDimension*0.32f)
                                        }
                                    }
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val realIdx = idx + 2
                                            val isActive = mode == realIdx
                                            Box(
                                                Modifier.size(18.dp).clip(CircleShape).background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B)).border(1.dp, Color.Black.copy(0.08f), CircleShape).clickable { mode = realIdx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                            }
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // OLED + side panels - FLAT TOP DOWN
                            Row(Modifier.fillMaxWidth().height(if(isTablet) 88.dp else 74.dp)) {
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(0.5.dp, Color(0xFFE8E6E1)))
                                Box(Modifier.weight(1.9f).fillMaxHeight().background(Color(0xFF0A0A0A)).padding(2.dp)) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        when(mode) {
                                            0 -> {
                                                val sunX = w * 0.28f
                                                val sunY = h * 0.32f
                                                drawCircle(Color.White, radius=11.dp.toPx(), center=Offset(sunX, sunY), style=Stroke(1.4f.dp.toPx()))
                                                for(i in 0..7) {
                                                    val ang = i*45f + sunRot
                                                    val rad = Math.toRadians(ang.toDouble())
                                                    drawLine(Color.White, Offset(sunX + cos(rad).toFloat()*16.dp.toPx(), sunY + sin(rad).toFloat()*16.dp.toPx()), Offset(sunX + cos(rad).toFloat()*22.dp.toPx(), sunY + sin(rad).toFloat()*22.dp.toPx()), 0.9f)
                                                }
                                                val cx = w*0.48f + cloudOffset
                                                val cy = h*0.56f
                                                drawCircle(Color.White, radius=8.dp.toPx(), center=Offset(cx-7f, cy), style=Stroke(1.3f.dp.toPx()))
                                                drawCircle(Color.White, radius=11.dp.toPx(), center=Offset(cx+4f, cy), style=Stroke(1.3f.dp.toPx()))
                                                drawCircle(Color.White, radius=7.dp.toPx(), center=Offset(cx+13f, cy+1f), style=Stroke(1.3f.dp.toPx()))
                                                val path = Path()
                                                for(x in 0..w.toInt() step 3) {
                                                    val wy = h*0.74f + sin(x*0.09f + wavePhase)*3f
                                                    if(x==0) path.moveTo(x.toFloat(), wy) else path.lineTo(x.toFloat(), wy)
                                                }
                                                drawPath(path, Color.White, style=Stroke(1.1f.dp.toPx()))
                                            }
                                            1 -> {
                                                for(i in 0..3) {
                                                    val px = w*0.20f + i*w*0.20f
                                                    val py = h*0.50f + sin(wavePhase + i*0.9f)*4f
                                                    drawCircle(Color.White, radius=9.dp.toPx(), center=Offset(px, py), style=Stroke(1.2f.dp.toPx()))
                                                    if(encoderVals[i]>0.55f) drawCircle(Color.White, radius=3.5f.dp.toPx(), center=Offset(px, py))
                                                }
                                            }
                                            2 -> {
                                                for(i in 0..3) {
                                                    val tx = w*0.17f + i*w*0.22f
                                                    val ty = h*0.46f
                                                    rotate(if(isPlay) tapeRot + i*40f else i*40f, pivot=Offset(tx, ty)) {
                                                        drawCircle(Color.White, radius=13.dp.toPx(), center=Offset(tx, ty), style=Stroke(1f.dp.toPx()))
                                                    }
                                                    val vuH = encoderVals[i]*h*0.38f
                                                    drawRect(if(tapeTracks[i]) Color.White else Color.White.copy(0.25f), topLeft=Offset(tx-6.dp.toPx(), h*0.80f - vuH), size=Size(12.dp.toPx(), vuH))
                                                }
                                            }
                                            3 -> {
                                                for(i in 0..3) {
                                                    val mx = w*0.17f + i*w*0.22f
                                                    val faderY = h*0.78f - encoderVals[i]*h*0.55f
                                                    drawLine(Color.White.copy(0.28f), Offset(mx, h*0.18f), Offset(mx, h*0.82f), 0.8f)
                                                    drawRect(Color.White, topLeft=Offset(mx-5.dp.toPx(), faderY), size=Size(10.dp.toPx(), 3f.dp.toPx()))
                                                }
                                            }
                                        }
                                    }
                                }
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(0.5.dp, Color(0xFFE8E6E1)))
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // ENCODERS - FLAT TOP DOWN, 4 light gray circles with orange indicator, like mockup
                            Row(Modifier.fillMaxWidth().height(if(isTablet) 78.dp else 66.dp).background(Color(0xFFFEFEFE))) {
                                repeat(4) { idx ->
                                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(0.5.dp, Color(0xFFE8E6E1)).padding(top=6.dp), contentAlignment=Alignment.TopCenter) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.width(2.dp).height(8.dp).background(Color(0xFFFF5A1F)))
                                            Spacer(Modifier.height(4.dp))
                                            Box(
                                                Modifier.size(if(isTablet) 44.dp else 36.dp).clip(CircleShape).background(Color(0xFFE8E6E1)).border(1.dp, Color(0xFFD8D5D0), CircleShape)
                                                    .pointerInput(idx) {
                                                        detectDragGestures(onDragStart={}, onDragEnd={}, onDrag={ _, dragAmount ->
                                                            val newVals = encoderVals.toMutableList()
                                                            newVals[idx] = (newVals[idx] + dragAmount.y * -0.008f).coerceIn(0f,1f)
                                                            encoderVals = newVals
                                                        })
                                                    },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                Canvas(Modifier.fillMaxSize()) {
                                                    // Flat top down encoder with orange line - like mockup
                                                    rotate(encoderVals[idx]*300f - 150f) {
                                                        drawLine(Color(0xFFFF5A1F), start=Offset(center.x, center.y - size.minDimension*0.15f), end=Offset(center.x, center.y - size.minDimension*0.38f), strokeWidth=2f)
                                                    }
                                                    drawCircle(Color(0xFFCCCCCC), radius=3.dp.toPx())
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text("${(encoderVals[idx]*100f).toInt()}", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // 1-8 BUTTONS - FLAT TOP DOWN, light gray with number, like mockup
                            Row(Modifier.fillMaxWidth().height(if(isTablet) 52.dp else 44.dp).background(Color(0xFFFEFEFE))) {
                                repeat(8) { idx ->
                                    val isSelected = selectedPreset == idx+1
                                    Box(
                                        Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color(0xFFE8E6E1)).background(if(isSelected) Color(0xFFF0EEE8) else Color(0xFFFEFEFE)).clickable { selectedPreset = idx+1 },
                                        contentAlignment=Alignment.Center
                                    ) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(
                                                Modifier.size(if(isTablet) 32.dp else 26.dp).clip(RoundedCornerShape(6.dp)).background(if(isSelected) Color.Black else Color(0xFFE8E6E1)).border(0.5.dp, Color(0xFFD0D0D0), RoundedCornerShape(6.dp)),
                                                contentAlignment=Alignment.Center
                                            ) {
                                                Text("${idx+1}", fontSize=11.sp, fontWeight=FontWeight.Medium, color=if(isSelected) Color.White else Color(0xFF6B6B6B), fontFamily=FontFamily.Monospace)
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text("${idx+1}", fontSize=6.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // KEYBOARD - FLAT TOP DOWN like mockup - white keys with light gray border and rounded bottom, gray sharps inset, NO 3D perspective
                            Box(Modifier.fillMaxWidth().height(if(isTablet) 92.dp else 76.dp).background(Color(0xFFFEFEFE)).padding(2.dp)) {
                                Row(Modifier.fillMaxSize(), horizontalArrangement=Arrangement.spacedBy(1.dp)) {
                                    repeat(14) { wIdx ->
                                        val whiteNotes = listOf(0,2,4,5,7,9,11,12,14,16,17,19,21,23)
                                        val midiNote = 60 + whiteNotes[wIdx]
                                        val isActive = midiNote in activeNotes
                                        Box(
                                            Modifier.weight(1f).fillMaxHeight()
                                                .clip(RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp))
                                                .background(if(isActive) Color(0xFFFF5A1F) else Color.White)
                                                .border(1.dp, Color(0xFFE0DDD8), RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp))
                                                .clickable {
                                                    if(midiNote in activeNotes) { activeNotes = activeNotes - midiNote; audioEngine.noteOff(midiNote) }
                                                    else { activeNotes = activeNotes + midiNote; audioEngine.noteOn(midiNote,100) }
                                                }
                                        )
                                    }
                                }
                                // Gray sharps - FLAT TOP DOWN, inset, like mockup
                                Row(Modifier.fillMaxWidth().height(if(isTablet) 46.dp else 38.dp).padding(horizontal=14.dp), horizontalArrangement=Arrangement.SpaceBetween) {
                                    Spacer(Modifier.width(14.dp))
                                    listOf(61,63,66,68,70,73,75,78,80,82).forEach { midi ->
                                        val isActive = midi in activeNotes
                                        Box(
                                            Modifier.width(if(isTablet) 18.dp else 14.dp).fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFD1D1D1))
                                                .border(0.5.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp))
                                                .clickable {
                                                    if(midi in activeNotes) { activeNotes = activeNotes - midi; audioEngine.noteOff(midi) }
                                                    else { activeNotes = activeNotes + midi; audioEngine.noteOn(midi,100) }
                                                }
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFFEFEFE)).padding(horizontal=10.dp),
                                horizontalArrangement=Arrangement.SpaceBetween,
                                verticalAlignment=Alignment.CenterVertically
                            ) {
                                Text("BRAUN • DIETER RAMS", fontSize=6.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A), letterSpacing=0.5.sp)
                                Text("OP-1", fontSize=6.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A))
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() { audioEngine.stop(); super.onDestroy() }
}
