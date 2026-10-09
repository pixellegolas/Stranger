
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
                var encoderVals by remember { mutableStateOf(listOf(0.3f, 0.6f, 0.4f, 0.7f)) }
                var selectedPreset by remember { mutableStateOf(1) }
                val infinite = rememberInfiniteTransition(label="anim")
                val cloudOffset by infinite.animateFloat(0f, 18f, infiniteRepeatable(tween(2800, easing=LinearEasing), RepeatMode.Reverse), label="cloud")
                val sunRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(10000, easing=LinearEasing), RepeatMode.Restart), label="sun")
                val wavePhase by infinite.animateFloat(0f, 6.28f, infiniteRepeatable(tween(1800, easing=LinearEasing), RepeatMode.Restart), label="wave")
                LaunchedEffect(mode) { audioEngine.setEngineType(if(mode==2) 0 else mode) }
                Box(Modifier.fillMaxSize().background(Color(0xFFF6F1E8)).padding(10.dp), contentAlignment=Alignment.Center) {
                    Box(Modifier.width(360.dp).shadow(22.dp, RoundedCornerShape(18.dp), ambientColor=Color.Black.copy(0.12f), spotColor=Color.Black.copy(0.22f)).clip(RoundedCornerShape(18.dp)).background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFEDE9E3), RoundedCornerShape(18.dp))) {
                        Column(Modifier.fillMaxWidth()) {
                            Box(Modifier.fillMaxWidth().height(86.dp).background(Color(0xFFFEFEFE)).padding(horizontal=14.dp), contentAlignment=Alignment.Center) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopStart).offset(y=12.dp))
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopEnd).offset(y=12.dp))
                                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val isActive = mode == idx
                                            Box(Modifier.size(22.dp).shadow(if(isActive) 3.dp else 1.5.dp, CircleShape).clip(CircleShape).background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B)).border(1.dp, Color.Black.copy(0.08f), CircleShape).clickable { mode = idx }, contentAlignment=Alignment.Center) {
                                                if(isActive) Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(0.9f)))
                                            }
                                        }
                                    }
                                    Box(Modifier.size(68.dp), contentAlignment=Alignment.Center) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            drawCircle(Color.Black.copy(0.15f), radius=size.minDimension/2, center=Offset(center.x+2f, center.y+3f))
                                            drawCircle(Color(0xFFFF5A1F), radius=size.minDimension/2 - 2.dp.toPx())
                                            drawCircle(Brush.radialGradient(listOf(Color.White.copy(0.35f), Color.Transparent), center=Offset(center.x-8f, center.y-10f), radius=size.minDimension*0.4f), radius=size.minDimension/2 - 2.dp.toPx())
                                            for(i in 0..35) {
                                                val angle = i * 10f * PI.toFloat() / 180f
                                                val r1 = size.minDimension/2 - 4.dp.toPx()
                                                val r2 = size.minDimension/2 - 1.dp.toPx()
                                                val x1 = center.x + cos(angle)*r1
                                                val y1 = center.y + sin(angle)*r1
                                                val x2 = center.x + cos(angle)*r2
                                                val y2 = center.y + sin(angle)*r2
                                                drawLine(Color.Black.copy(0.18f), Offset(x1,y1), Offset(x2,y2), 1f)
                                            }
                                            drawCircle(Color(0xFFFF7A33), radius=size.minDimension*0.32f)
                                            drawCircle(Color.Black.copy(0.08f), radius=size.minDimension*0.32f, style=Stroke(1f))
                                        }
                                    }
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val realIdx = idx + 2
                                            val isActive = mode == realIdx
                                            Box(Modifier.size(22.dp).shadow(if(isActive) 3.dp else 1.5.dp, CircleShape).clip(CircleShape).background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B)).border(1.dp, Color.Black.copy(0.08f), CircleShape).clickable { mode = realIdx }, contentAlignment=Alignment.Center) {
                                                if(isActive) Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(0.9f)))
                                            }
                                        }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))
                            Row(Modifier.fillMaxWidth().height(92.dp)) {
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))
                                Box(Modifier.weight(1.9f).fillMaxHeight().background(Color(0xFF0A0A0A)).padding(2.dp)) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        if(mode==0) {
                                            val sunX = w * 0.28f
                                            val sunY = h * 0.32f
                                            drawCircle(Color.White, radius=14.dp.toPx(), center=Offset(sunX, sunY), style=Stroke(1.8f.dp.toPx()))
                                            for(i in 0..7) {
                                                val ang = i*45f + sunRot
                                                val rad = Math.toRadians(ang.toDouble())
                                                val r1 = 20.dp.toPx()
                                                val r2 = 28.dp.toPx()
                                                val x1 = sunX + cos(rad).toFloat()*r1
                                                val y1 = sunY + sin(rad).toFloat()*r1
                                                val x2 = sunX + cos(rad).toFloat()*r2
                                                val y2 = sunY + sin(rad).toFloat()*r2
                                                drawLine(Color.White, Offset(x1,y1), Offset(x2,y2), 1.2f)
                                            }
                                            val cx = w*0.48f + cloudOffset
                                            val cy = h*0.58f
                                            drawCircle(Color.White, radius=10.dp.toPx(), center=Offset(cx-8f, cy), style=Stroke(1.6f.dp.toPx()))
                                            drawCircle(Color.White, radius=14.dp.toPx(), center=Offset(cx+6f, cy), style=Stroke(1.6f.dp.toPx()))
                                            drawCircle(Color.White, radius=9.dp.toPx(), center=Offset(cx+16f, cy+2f), style=Stroke(1.6f.dp.toPx()))
                                            val waveY = h*0.75f
                                            val path = Path()
                                            for(x in 0..w.toInt() step 4) {
                                                val wy = waveY + sin(x*0.08f + wavePhase)*4f
                                                if(x==0) path.moveTo(x.toFloat(), wy) else path.lineTo(x.toFloat(), wy)
                                            }
                                            drawPath(path, Color.White, style=Stroke(1.4f.dp.toPx()))
                                        } else if(mode==1) {
                                            for(i in 0..3) {
                                                val px = w*0.2f + i*w*0.18f
                                                val py = h*0.5f + sin(wavePhase + i)*6f
                                                drawCircle(Color.White, radius=10.dp.toPx(), center=Offset(px, py), style=Stroke(1.5f.dp.toPx()))
                                                if(encoderVals[i]>0.5f) drawCircle(Color.White, radius=5.dp.toPx(), center=Offset(px, py))
                                            }
                                        } else if(mode==2) {
                                            for(i in 0..3) {
                                                val tx = w*0.18f + i*w*0.21f
                                                val ty = h*0.45f
                                                rotate(sunRot + i*45f, pivot=Offset(tx, ty)) {
                                                    drawCircle(Color.White, radius=16.dp.toPx(), center=Offset(tx, ty), style=Stroke(1.2f.dp.toPx()))
                                                }
                                                val vuH = encoderVals[i]*h*0.35f
                                                drawRect(Color.White, topLeft=Offset(tx-8.dp.toPx(), h*0.78f - vuH), size=Size(16.dp.toPx(), vuH))
                                            }
                                        } else {
                                            for(i in 0..3) {
                                                val mx = w*0.18f + i*w*0.21f
                                                val faderY = h*0.75f - encoderVals[i]*h*0.5f
                                                drawLine(Color.White.copy(0.3f), Offset(mx, h*0.2f), Offset(mx, h*0.8f), 1f)
                                                drawRect(Color.White, topLeft=Offset(mx-7.dp.toPx(), faderY), size=Size(14.dp.toPx(), 4.dp.toPx()))
                                            }
                                        }
                                    }
                                    Text(when(mode){0->"SYNTH" 1->"DRUM" 2->"TAPE" 3->"MIXER" else->""}, modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=3.dp), fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color.White.copy(0.6f))
                                }
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))
                            Row(Modifier.fillMaxWidth().height(84.dp)) {
                                repeat(4) { idx ->
                                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(0.5.dp, Color(0xFFE8E6E1)).padding(top=6.dp), contentAlignment=Alignment.TopCenter) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.width(2.dp).height(10.dp).background(Color(0xFFFF5A1F)))
                                            Spacer(Modifier.height(4.dp))
                                            Box(Modifier.size(48.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color(0xFFE8E6E1)).border(1.dp, Color.White, CircleShape).pointerInput(idx) { detectDragGestures { _, dragAmount -> val newVals = encoderVals.toMutableList(); newVals[idx] = (newVals[idx] + dragAmount.y * -0.01f).coerceIn(0f,1f); encoderVals = newVals } }, contentAlignment=Alignment.Center) {
                                                Canvas(Modifier.fillMaxSize()) {
                                                    drawCircle(Color.Black.copy(0.08f), radius=size.minDimension/2 - 1.dp.toPx(), style=Stroke(1f))
                                                    rotate(encoderVals[idx]*300f - 150f) {
                                                        drawLine(Color(0xFFFF5A1F), start=Offset(center.x, center.y - size.minDimension*0.12f), end=Offset(center.x, center.y - size.minDimension*0.38f), strokeWidth=2.5f)
                                                    }
                                                    drawCircle(Color(0xFFD0D0D0), radius=4.dp.toPx())
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text("${(encoderVals[idx]*100).toInt()}", fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))
                            Row(Modifier.fillMaxWidth().height(62.dp).background(Color(0xFFFEFEFE))) {
                                repeat(8) { idx ->
                                    val isSelected = selectedPreset == idx+1
                                    Box(Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color(0xFFE8E6E1)).background(if(isSelected) Color(0xFFF5F3EE) else Color(0xFFFEFEFE)).clickable { selectedPreset = idx+1 }, contentAlignment=Alignment.Center) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.size(38.dp).shadow(if(isSelected) 2.dp else 1.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp)).background(if(isSelected) Color.Black else Color(0xFFE8E6E1)).border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(10.dp)), contentAlignment=Alignment.Center) {
                                                Text("${idx+1}", fontSize=14.sp, fontWeight=FontWeight.Medium, color=if(isSelected) Color.White else Color(0xFF6B6B6B), fontFamily=FontFamily.Monospace)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text("${idx+1}", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))
                            Row(Modifier.fillMaxWidth().height(72.dp).background(Color(0xFFFEFEFE)).padding(2.dp), horizontalArrangement=Arrangement.spacedBy(1.dp)) {
                                repeat(14) { wIdx ->
                                    val midiNote = 60 + wIdx*2
                                    val isActive = midiNote in activeNotes
                                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp)).background(if(isActive) Color(0xFFFF5A1F) else Color.White).border(1.dp, Color(0xFFE0DDD8), RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp)).clickable {
                                        if(midiNote in activeNotes) {
                                            activeNotes = activeNotes - midiNote
                                            audioEngine.noteOff(midiNote)
                                        } else {
                                            activeNotes = activeNotes + midiNote
                                            audioEngine.noteOn(midiNote,100)
                                        }
                                    })
                                }
                            }
                            Row(Modifier.fillMaxWidth().height(18.dp).background(Color(0xFFFEFEFE)).padding(horizontal=10.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                                Text("BRAUN • DIETER RAMS", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A))
                                Text("OP-1", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A))
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() { audioEngine.stop(); super.onDestroy() }
}
