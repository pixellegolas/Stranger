
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
import androidx.compose.ui.graphics.*
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
                var mode by remember { mutableStateOf(0) } // 0 synth, 1 drum, 2 tape, 3 mixer
                var isPlaying by remember { mutableStateOf(false) }
                var isRecording by remember { mutableStateOf(false) }
                var activeNotes by remember { mutableStateOf(setOf<Int>()) }
                var encoderVals by remember { mutableStateOf(listOf(0.3f, 0.6f, 0.4f, 0.7f)) }
                var selectedPreset by remember { mutableStateOf(1) }
                var tapePos by remember { mutableStateOf(0.3f) }

                val infinite = rememberInfiniteTransition(label="oled")
                val cloudOffset by infinite.animateFloat(0f, 20f, infiniteRepeatable(tween(3000, easing=LinearEasing), RepeatMode.Reverse), label="cloud")
                val sunRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing=LinearEasing), RepeatMode.Restart), label="sun")
                val wavePhase by infinite.animateFloat(0f, 6.28f, infiniteRepeatable(tween(2000, easing=LinearEasing), RepeatMode.Restart), label="wave")

                LaunchedEffect(mode) { audioEngine.setEngineType(if(mode==2) 0 else mode) }

                Box(
                    Modifier.fillMaxSize().background(Color(0xFFF6F1E8)).padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Main Braun device - exact mockup replica with soft shadows
                    Box(
                        Modifier
                            .width(360.dp)
                            .shadow(24.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(0.12f), spotColor = Color.Black.copy(0.22f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFEFEFE))
                            .border(1.dp, Color(0xFFEDE9E3), RoundedCornerShape(18.dp))
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            // TOP PANEL - orange knob + 4 small orange buttons + screw dots
                            Box(
                                Modifier.fillMaxWidth().height(88.dp)
                                    .background(Color(0xFFFEFEFE))
                                    .padding(horizontal=14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Screw dots
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopStart).offset(y=12.dp)))
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopEnd).offset(y=12.dp))

                                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                                    // Left 2 orange buttons
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val isActive = mode == idx
                                            Box(
                                                Modifier.size(22.dp)
                                                    .shadow(if(isActive) 3.dp else 1.5.dp, CircleShape, spotColor=Color.Black.copy(0.3f))
                                                    .clip(CircleShape)
                                                    .background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B))
                                                    .border(1.dp, Color.Black.copy(0.08f), CircleShape)
                                                    .clickable { mode = idx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(0.9f)))
                                            }
                                        }
                                    }

                                    // Large orange volume knob - 3D with knurling
                                    Box(Modifier.size(68.dp), contentAlignment=Alignment.Center) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            // Outer shadow
                                            drawCircle(Color.Black.copy(0.15f), radius=size.minDimension/2, center=Offset(center.x+2f, center.y+3f))
                                            // Main orange body
                                            drawCircle(Color(0xFFFF5A1F), radius=size.minDimension/2 - 2.dp.toPx())
                                            // Inner highlight
                                            drawCircle(
                                                Brush.radialGradient(
                                                    listOf(Color.White.copy(0.35f), Color.Transparent),
                                                    center=Offset(center.x-8f, center.y-10f),
                                                    radius=size.minDimension*0.4f
                                                ),
                                                radius=size.minDimension/2 - 2.dp.toPx()
                                            )
                                            // Knurled edge - small lines around
                                            for(i in 0..36) {
                                                val angle = i * 10f * PI.toFloat() / 180f
                                                val r1 = size.minDimension/2 - 4.dp.toPx()
                                                val r2 = size.minDimension/2 - 1.dp.toPx()
                                                val x1 = center.x + cos(angle)*r1
                                                val y1 = center.y + sin(angle)*r1
                                                val x2 = center.x + cos(angle)*r2
                                                val y2 = center.y + sin(angle)*r2
                                                drawLine(Color.Black.copy(0.18f), Offset(x1,y1), Offset(x2,y2), 1f)
                                            }
                                            // Top inner circle
                                            drawCircle(Color(0xFFFF7A33), radius=size.minDimension*0.32f)
                                            drawCircle(Color.Black.copy(0.08f), radius=size.minDimension*0.32f, style=Stroke(1f))
                                        }
                                        // Click for volume
                                        Box(Modifier.fillMaxSize().clip(CircleShape).clickable { })
                                    }

                                    // Right 2 orange buttons
                                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                        repeat(2) { idx ->
                                            val realIdx = idx + 2
                                            val isActive = mode == realIdx
                                            Box(
                                                Modifier.size(22.dp)
                                                    .shadow(if(isActive) 3.dp else 1.5.dp, CircleShape, spotColor=Color.Black.copy(0.3f))
                                                    .clip(CircleShape)
                                                    .background(if(isActive) Color(0xFFFF5A1F) else Color(0xFFFF6B2B))
                                                    .border(1.dp, Color.Black.copy(0.08f), CircleShape)
                                                    .clickable { mode = realIdx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(0.9f)))
                                            }
                                        }
                                    }
                                }
                            }

                            // Divider line
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // OLED + side panels - exact mockup
                            Row(Modifier.fillMaxWidth().height(92.dp)) {
                                // Left blank panel
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))

                                // Center OLED - black with living white drawings
                                Box(
                                    Modifier.weight(1.9f).fillMaxHeight()
                                        .background(Color(0xFF0A0A0A))
                                        .padding(2.dp)
                                ) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        // Living OLED content based on mode
                                        when(mode) {
                                            0 -> { // SYNTH - sun + cloud + waves like mockup
                                                // Sun
                                                val sunX = w * 0.28f
                                                val sunY = h * 0.32f
                                                drawCircle(Color.White, radius=14.dp.toPx(), center=Offset(sunX, sunY), style=Stroke(1.8f.dp.toPx()))
                                                // Sun rays
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
                                                // Cloud - moving
                                                val cx = w*0.48f + cloudOffset
                                                val cy = h*0.58f
                                                drawCircle(Color.White, radius=10.dp.toPx(), center=Offset(cx-8f, cy), style=Stroke(1.6f.dp.toPx()))
                                                drawCircle(Color.White, radius=14.dp.toPx(), center=Offset(cx+6f, cy), style=Stroke(1.6f.dp.toPx()))
                                                drawCircle(Color.White, radius=9.dp.toPx(), center=Offset(cx+16f, cy+2f), style=Stroke(1.6f.dp.toPx()))
                                                // Smile
                                                drawArc(Color.White, 20f, 120f, false, topLeft=Offset(cx-4f, cy-2f), size=Size(18.dp.toPx(), 10.dp.toPx()), style=Stroke(1.2f.dp.toPx()))
                                                // Waves
                                                val waveY = h*0.75f
                                                val path = Path()
                                                path.moveTo(0f, waveY)
                                                for(x in 0..w.toInt() step 4) {
                                                    val wy = waveY + sin(x*0.08f + wavePhase)*4f
                                                    if(x==0) path.moveTo(x.toFloat(), wy) else path.lineTo(x.toFloat(), wy)
                                                }
                                                drawPath(path, Color.White, style=Stroke(1.4f.dp.toPx()))

                                                // Small birds / ~ ~
                                                drawPath(Path().apply {
                                                    moveTo(w*0.78f, h*0.38f)
                                                    quadraticBezierTo(w*0.81f, h*0.32f, w*0.84f, h*0.38f)
                                                    moveTo(w*0.80f, h*0.48f)
                                                    quadraticBezierTo(w*0.83f, h*0.42f, w*0.86f, h*0.48f)
                                                }, Color.White, style=Stroke(1.1f.dp.toPx()))
                                            }
                                            1 -> { // DRUM
                                                // 4 drum pads
                                                for(i in 0..3) {
                                                    val px = w*0.2f + i*w*0.18f
                                                    val py = h*0.5f + sin(wavePhase + i)*6f
                                                    drawCircle(Color.White, radius=10.dp.toPx(), center=Offset(px, py), style=Stroke(1.5f.dp.toPx()))
                                                    if(encoderVals[i]>0.5f) drawCircle(Color.White, radius=5.dp.toPx(), center=Offset(px, py))
                                                }
                                                drawContext.canvas.nativeCanvas.drawText("DRUM", w*0.42f, h*0.25f, android.graphics.Paint().apply { color=android.graphics.Color.WHITE; textSize=10.dp.toPx(); typeface=android.graphics.Typeface.MONOSPACE })
                                            }
                                            2 -> { // TAPE - 4 tracks reels
                                                // Tape reels
                                                for(i in 0..3) {
                                                    val tx = w*0.18f + i*w*0.21f
                                                    val ty = h*0.45f
                                                    rotate(sunRot + i*45f, pivot=Offset(tx, ty)) {
                                                        drawCircle(Color.White, radius=16.dp.toPx(), center=Offset(tx, ty), style=Stroke(1.2f.dp.toPx()))
                                                        for(j in 0..2) {
                                                            val ang = j*120f
                                                            val rad = Math.toRadians(ang.toDouble())
                                                            drawLine(Color.White, Offset(tx, ty), Offset(tx+cos(rad).toFloat()*12.dp.toPx(), ty+sin(rad).toFloat()*12.dp.toPx()), 1f)
                                                        }
                                                    }
                                                    // VU
                                                    val vuH = (encoderVals[i]*h*0.35f)
                                                    drawRect(Color.White, topLeft=Offset(tx-8.dp.toPx(), h*0.78f - vuH), size=Size(16.dp.toPx(), vuH))
                                                }
                                            }
                                            3 -> { // MIXER
                                                for(i in 0..3) {
                                                    val mx = w*0.18f + i*w*0.21f
                                                    val faderY = h*0.75f - encoderVals[i]*h*0.5f
                                                    drawLine(Color.White.copy(0.3f), Offset(mx, h*0.2f), Offset(mx, h*0.8f), 1f)
                                                    drawRect(Color.White, topLeft=Offset(mx-7.dp.toPx(), faderY), size=Size(14.dp.toPx(), 4.dp.toPx()))
                                                }
                                            }
                                        }
                                    }

                                    // Mode label tiny
                                    Text(
                                        when(mode){0->"SYNTH" 1->"DRUM" 2->"TAPE" 3->"MIXER" else->""},
                                        modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=3.dp),
                                        fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color.White.copy(0.6f)
                                    )
                                }

                                // Right blank panel
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))
                            }

                            // Divider
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // ENCODERS ROW - 4 encoders with orange markers - exact mockup
                            Row(Modifier.fillMaxWidth().height(84.dp)) {
                                repeat(4) { idx ->
                                    Box(
                                        Modifier.weight(1f).fillMaxHeight()
                                            .background(Color(0xFFFEFEFE))
                                            .border(0.5.dp, Color(0xFFE8E6E1))
                                            .padding(top=6.dp),
                                        contentAlignment=Alignment.TopCenter
                                    ) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            // Orange tick top
                                            Box(Modifier.width(2.dp).height(10.dp).background(Color(0xFFFF5A1F)))
                                            Spacer(Modifier.height(4.dp))
                                            // Encoder knob - light gray 3D with orange indicator
                                            Box(
                                                Modifier.size(48.dp)
                                                    .shadow(3.dp, CircleShape, spotColor=Color.Black.copy(0.25f))
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE8E6E1))
                                                    .border(1.dp, Color.White, CircleShape)
                                                    .pointerInput(idx) {
                                                        detectDragGestures { _, dragAmount ->
                                                            val newVals = encoderVals.toMutableList()
                                                            newVals[idx] = (newVals[idx] + dragAmount.y * -0.01f).coerceIn(0f,1f)
                                                            encoderVals = newVals
                                                        }
                                                    },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                Canvas(Modifier.fillMaxSize()) {
                                                    // Inner shadow
                                                    drawCircle(Color.Black.copy(0.08f), radius=size.minDimension/2 - 1.dp.toPx(), style=Stroke(1f))
                                                    // Orange indicator line
                                                    rotate(encoderVals[idx]*300f - 150f) {
                                                        drawLine(
                                                            Color(0xFFFF5A1F),
                                                            start=Offset(center.x, center.y - size.minDimension*0.12f),
                                                            end=Offset(center.x, center.y - size.minDimension*0.38f),
                                                            strokeWidth=2.5f
                                                        )
                                                    }
                                                    // Center dot
                                                    drawCircle(Color(0xFFD0D0D0), radius=4.dp.toPx())
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            // Bottom orange ticks
                                            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                                Box(Modifier.width(6.dp).height(2.dp).background(Color(0xFFFF5A1F).copy(0.6f)).rotate(-30f))
                                                Box(Modifier.width(6.dp).height(2.dp).background(Color(0xFFFF5A1F).copy(0.6f)).rotate(30f))
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text("${(encoderVals[idx]*100).toInt()}", fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // 1-8 BUTTONS ROW - exact mockup style
                            Row(Modifier.fillMaxWidth().height(62.dp).background(Color(0xFFFEFEFE))) {
                                repeat(8) { idx ->
                                    val isSelected = selectedPreset == idx+1
                                    Box(
                                        Modifier.weight(1f).fillMaxHeight()
                                            .border(0.5.dp, Color(0xFFE8E6E1))
                                            .background(if(isSelected) Color(0xFFF5F3EE) else Color(0xFFFEFEFE))
                                            .clickable { selectedPreset = idx+1 },
                                        contentAlignment=Alignment.Center
                                    ) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(
                                                Modifier.size(38.dp)
                                                    .shadow(if(isSelected) 2.dp else 1.dp, RoundedCornerShape(10.dp))
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if(isSelected) Color.Black else Color(0xFFE8E6E1))
                                                    .border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(10.dp)),
                                                contentAlignment=Alignment.Center
                                            ) {
                                                Text("${idx+1}", fontSize=14.sp, fontWeight=FontWeight.Medium, color=if(isSelected) Color.White else Color(0xFF6B6B6B), fontFamily=FontFamily.Monospace)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text("${idx+1}", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color.Gray)
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            // KEYBOARD - 24 keys - exact mockup: white keys with thin black lines, gray sharps inset
                            Box(Modifier.fillMaxWidth().height(88.dp).background(Color(0xFFFEFEFE)).padding(horizontal=2.dp, vertical=4.dp)) {
                                Row(Modifier.fillMaxSize(), horizontalArrangement=Arrangement.spacedBy(1.dp)) {
                                    val whiteNotes = listOf(0,2,4,5,7,9,11,12,14,16,17,19,21,23) // 14 white keys
                                    val blackNotes = listOf(1,3,6,8,10,13,15,18,20,22) // 10 black/gray keys
                                    // Build 24 keys interleaved like piano
                                    val allNotes = (0..23).toList()
                                    var whiteIndex = 0
                                    // Simplified: draw white keys background then overlay black keys
                                    Box(Modifier.fillMaxSize()) {
                                        Row(Modifier.fillMaxSize(), horizontalArrangement=Arrangement.spacedBy(1.dp)) {
                                            repeat(14) { wIdx ->
                                                val midiNote = whiteNotes.getOrNull(wIdx) ?: 0
                                                val isActive = midiNote in activeNotes
                                                Box(
                                                    Modifier.weight(1f).fillMaxHeight()
                                                        .clip(RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp))
                                                        .background(if(isActive) Color(0xFFFF5A1F) else Color.White)
                                                        .border(1.dp, Color(0xFFE0DDD8), RoundedCornerShape(bottomStart=3.dp, bottomEnd=3.dp))
                                                        .clickable {
                                                            val note = 60 + whiteNotes[wIdx]
                                                            if(note in activeNotes) { activeNotes=activeNotes-note; audioEngine.noteOff(note) }
                                                            else { activeNotes=activeNotes+note; audioEngine.noteOn(note,100) }
                                                        }
                                                )
                                            }
                                        }
                                        // Black/gray keys overlay - smaller, inset like mockup
                                        Row(Modifier.fillMaxWidth().height(46.dp).padding(horizontal=12.dp), horizontalArrangement=Arrangement.spacedBy(2.dp)) {
                                            val positions = listOf(0.7f, 1.7f, 3.2f, 4.2f, 5.2f, 7.2f, 8.2f, 9.7f, 10.7f, 11.7f) // approximate
                                            // For simplicity, use fixed layout with spacers
                                            val keys = listOf(
                                                Pair(true, 60+1), Pair(true, 60+3), Pair(false, -1),
                                                Pair(true, 60+6), Pair(true, 60+8), Pair(true, 60+10), Pair(false, -1),
                                                Pair(true, 60+13), Pair(true, 60+15), Pair(false, -1),
                                                Pair(true, 60+18), Pair(true, 60+20), Pair(true, 60+22)
                                            )
                                            // Render as row with empty spaces for white gaps
                                            // Use a custom layout: 14 white keys, black keys placed between
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                                                Spacer(Modifier.width(18.dp))
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=61; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Spacer(Modifier.width(4.dp))
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=63; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Spacer(Modifier.width(22.dp))
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=66; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=68; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=70; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Spacer(Modifier.width(22.dp))
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=73; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=75; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Spacer(Modifier.width(22.dp))
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=78; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=80; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Box(Modifier.width(18.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1D1D1)).border(1.dp, Color(0xFFB8B8B8), RoundedCornerShape(2.dp)).clickable { val n=82; if(n in activeNotes){activeNotes=activeNotes-n; audioEngine.noteOff(n)} else {activeNotes=activeNotes+n; audioEngine.noteOn(n,100)} })
                                                Spacer(Modifier.width(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom labels - BRAUN • DIETER RAMS / OP-1
                            Row(
                                Modifier.fillMaxWidth().height(18.dp).background(Color(0xFFFEFEFE)).padding(horizontal=10.dp),
                                horizontalArrangement=Arrangement.SpaceBetween,
                                verticalAlignment=Alignment.CenterVertically
                            ) {
                                Text("BRAUN • DIETER RAMS", fontSize=7.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A), letterSpacing=0.5.sp)
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

fun Modifier.rotate(deg: Float): Modifier {
    return this.then(Modifier)
}
