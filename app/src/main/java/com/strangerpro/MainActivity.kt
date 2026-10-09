
package com.strangerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
                var tapePos by remember { mutableStateOf(0.32f) }

                val infinite = rememberInfiniteTransition(label="oled")
                val cloudOffset by infinite.animateFloat(0f, 16f, infiniteRepeatable(tween(2800, easing=LinearEasing), RepeatMode.Reverse), label="cloud")
                val sunRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing=LinearEasing), RepeatMode.Restart), label="sun")
                val wavePhase by infinite.animateFloat(0f, 6.28f, infiniteRepeatable(tween(1600, easing=LinearEasing), RepeatMode.Restart), label="wave")
                val tapeRot by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(2200, easing=LinearEasing), RepeatMode.Restart), label="tape")

                LaunchedEffect(mode) {
                    audioEngine.setEngineType(if(mode==2) 0 else mode)
                }
                LaunchedEffect(encoderVals) {
                    audioEngine.setParam(0, encoderVals[0])
                    audioEngine.setParam(1, encoderVals[1])
                    audioEngine.setAlgorithm((encoderVals[2]*31f).toInt())
                    audioEngine.setFeedback(encoderVals[3])
                }

                BoxWithConstraints(
                    Modifier.fillMaxSize().background(Color(0xFFF2EEE6)).padding(12.dp),
                    contentAlignment=Alignment.Center
                ) {
                    val isTablet = maxWidth > 600.dp
                    val deviceWidth = if(isTablet) maxWidth * 0.92f else maxWidth * 0.98f
                    val devicePadding = if(isTablet) 22.dp else 14.dp

                    Box(
                        Modifier.width(deviceWidth)
                            .shadow(32.dp, RoundedCornerShape(22.dp), ambientColor=Color.Black.copy(0.14f), spotColor=Color.Black.copy(0.28f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFFEFEFE))
                            .border(1.dp, Color(0xFFEDE9E3), RoundedCornerShape(22.dp))
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier.fillMaxWidth().height(if(isTablet) 112.dp else 88.dp).background(Color(0xFFFEFEFE)).padding(horizontal=devicePadding),
                                contentAlignment=Alignment.Center
                            ) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopStart).offset(y=14.dp))
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF9A9A9A)).align(Alignment.TopEnd).offset(y=14.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                                        repeat(2) { idx ->
                                            val isActive = mode == idx
                                            Box(
                                                Modifier.size(if(isTablet) 28.dp else 22.dp)
                                                    .shadow(if(isActive) 4.dp else 2.dp, CircleShape, spotColor=Color.Black.copy(0.35f))
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF5A1F))
                                                    .border(1.dp, Color.Black.copy(0.12f), CircleShape)
                                                    .clickable { mode = idx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                            }
                                        }
                                    }
                                    Box(
                                        Modifier.size(if(isTablet) 88.dp else 68.dp)
                                            .shadow(8.dp, CircleShape, spotColor=Color.Black.copy(0.3f))
                                            .clip(CircleShape)
                                            .clickable { },
                                        contentAlignment=Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.orange_knob_large),
                                            contentDescription = "Volume",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                                        repeat(2) { idx ->
                                            val realIdx = idx + 2
                                            val isActive = mode == realIdx
                                            Box(
                                                Modifier.size(if(isTablet) 28.dp else 22.dp)
                                                    .shadow(if(isActive) 4.dp else 2.dp, CircleShape, spotColor=Color.Black.copy(0.35f))
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF5A1F))
                                                    .border(1.dp, Color.Black.copy(0.12f), CircleShape)
                                                    .clickable { mode = realIdx },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                if(isActive) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                            }
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            Row(Modifier.fillMaxWidth().height(if(isTablet) 128.dp else 96.dp)) {
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))
                                Box(
                                    Modifier.weight(1.95f).fillMaxHeight()
                                        .background(Brush.verticalGradient(listOf(Color(0xFF151515), Color(0xFF0A0A0A))))
                                        .border(2.dp, Color.Black)
                                        .padding(3.dp)
                                ) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        when(mode) {
                                            0 -> {
                                                val sunX = w * 0.26f
                                                val sunY = h * 0.30f
                                                drawCircle(Color.White, radius=13.dp.toPx(), center=Offset(sunX, sunY), style=Stroke(1.7f.dp.toPx()))
                                                for(i in 0..7) {
                                                    val ang = i*45f + sunRot
                                                    val rad = Math.toRadians(ang.toDouble())
                                                    val r1 = 18.dp.toPx()
                                                    val r2 = 26.dp.toPx()
                                                    drawLine(Color.White, Offset(sunX + cos(rad).toFloat()*r1, sunY + sin(rad).toFloat()*r1), Offset(sunX + cos(rad).toFloat()*r2, sunY + sin(rad).toFloat()*r2), 1.1f)
                                                }
                                                val cx = w*0.50f + cloudOffset
                                                val cy = h*0.56f
                                                drawCircle(Color.White, radius=9.dp.toPx(), center=Offset(cx-8f, cy), style=Stroke(1.5f.dp.toPx()))
                                                drawCircle(Color.White, radius=13.dp.toPx(), center=Offset(cx+5f, cy), style=Stroke(1.5f.dp.toPx()))
                                                drawCircle(Color.White, radius=8.dp.toPx(), center=Offset(cx+15f, cy+1f), style=Stroke(1.5f.dp.toPx()))
                                                val waveY = h*0.76f
                                                val path = Path()
                                                for(x in 0..w.toInt() step 3) {
                                                    val wy = waveY + sin(x*0.09f + wavePhase)*3.5f
                                                    if(x==0) path.moveTo(x.toFloat(), wy) else path.lineTo(x.toFloat(), wy)
                                                }
                                                drawPath(path, Color.White, style=Stroke(1.3f.dp.toPx()))
                                            }
                                            1 -> {
                                                for(i in 0..3) {
                                                    val px = w*0.20f + i*w*0.20f
                                                    val py = h*0.50f + sin(wavePhase + i*0.9f)*5f
                                                    drawCircle(Color.White, radius=11.dp.toPx(), center=Offset(px, py), style=Stroke(1.4f.dp.toPx()))
                                                    if(encoderVals[i]>0.55f) drawCircle(Color.White, radius=4.5f.dp.toPx(), center=Offset(px, py))
                                                }
                                            }
                                            2 -> {
                                                for(i in 0..3) {
                                                    val tx = w*0.17f + i*w*0.22f
                                                    val ty = h*0.46f
                                                    rotate(if(isPlay) tapeRot + i*40f else i*40f, pivot=Offset(tx, ty)) {
                                                        drawCircle(Color.White, radius=15.dp.toPx(), center=Offset(tx, ty), style=Stroke(1.1f.dp.toPx()))
                                                    }
                                                    val vuH = encoderVals[i]*h*0.38f
                                                    drawRect(if(tapeTracks[i]) Color.White else Color.White.copy(0.25f), topLeft=Offset(tx-7.dp.toPx(), h*0.80f - vuH), size=Size(14.dp.toPx(), vuH))
                                                }
                                            }
                                            3 -> {
                                                for(i in 0..3) {
                                                    val mx = w*0.17f + i*w*0.22f
                                                    val faderY = h*0.78f - encoderVals[i]*h*0.55f
                                                    drawLine(Color.White.copy(0.28f), Offset(mx, h*0.18f), Offset(mx, h*0.82f), 1f)
                                                    drawRect(Color.White, topLeft=Offset(mx-6.dp.toPx(), faderY), size=Size(12.dp.toPx(), 3.5f.dp.toPx()))
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        when(mode){0->"SYNTH ALGO ${(encoderVals[2]*31f).toInt()} FB ${(encoderVals[3]*100f).toInt()}%" 1->"DRUM KIT $selectedPreset" 2->"TAPE ${if(isRec) "REC" else if(isPlay) "PLAY" else "STOP"}" 3->"MIXER" else->""},
                                        modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=4.dp),
                                        fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color.White.copy(0.7f)
                                    )
                                }
                                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(1.dp, Color(0xFFE8E6E1)))
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            Row(Modifier.fillMaxWidth().height(if(isTablet) 102.dp else 82.dp).background(Color(0xFFFEFEFE))) {
                                val labels = listOf("BLUE", "GREEN", "WHITE", "ORANGE")
                                repeat(4) { idx ->
                                    Box(
                                        Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFEFEFE)).border(0.5.dp, Color(0xFFE8E6E1)).padding(top=8.dp),
                                        contentAlignment=Alignment.TopCenter
                                    ) {
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.width(2.dp).height(12.dp).background(Color(0xFFFF5A1F)))
                                            Spacer(Modifier.height(6.dp))
                                            Box(
                                                Modifier.size(if(isTablet) 62.dp else 48.dp)
                                                    .shadow(5.dp, CircleShape, spotColor=Color.Black.copy(0.28f))
                                                    .clip(CircleShape)
                                                    .pointerInput(idx) {
                                                        detectDragGestures(
                                                            onDragStart = {},
                                                            onDragEnd = {},
                                                            onDrag = { _, dragAmount ->
                                                                val newVals = encoderVals.toMutableList()
                                                                newVals[idx] = (newVals[idx] + dragAmount.y * -0.008f).coerceIn(0f,1f)
                                                                encoderVals = newVals
                                                            }
                                                        )
                                                    },
                                                contentAlignment=Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.gray_encoder_knob),
                                                    contentDescription = labels[idx],
                                                    modifier = Modifier.fillMaxSize().rotate(encoderVals[idx]*300f - 150f),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(labels[idx], fontSize=8.sp, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold, color=Color(0xFF9A9A9A))
                                            Text("${(encoderVals[idx]*100f).toInt()}", fontSize=9.sp, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Medium, color=Color.Black)
                                        }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            Row(Modifier.fillMaxWidth().height(if(isTablet) 78.dp else 62.dp).background(Color(0xFFFEFEFE)), verticalAlignment=Alignment.CenterVertically) {
                                Row(Modifier.padding(start=devicePadding), horizontalArrangement=Arrangement.spacedBy(10.dp), verticalAlignment=Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(36.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(if(isRec) Color(0xFFFF3B30) else Color(0xFFE8E6E1)).border(1.dp, Color.Black.copy(0.1f), CircleShape).clickable { isRec = !isRec; if(isRec) isPlay=true },
                                        contentAlignment=Alignment.Center
                                    ) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color.White)) }
                                    Box(
                                        Modifier.size(36.dp).shadow(2.dp, RoundedCornerShape(6.dp)).clip(RoundedCornerShape(6.dp)).background(Color(0xFFE8E6E1)).border(1.dp, Color.Black.copy(0.1f), RoundedCornerShape(6.dp)).clickable { isRec=false; isPlay=false; activeNotes.forEach { audioEngine.noteOff(it) }; activeNotes=emptySet() },
                                        contentAlignment=Alignment.Center
                                    ) { Box(Modifier.size(12.dp).background(Color.Black)) }
                                    Box(
                                        Modifier.size(36.dp).shadow(2.dp, RoundedCornerShape(6.dp)).clip(RoundedCornerShape(6.dp)).background(if(isPlay) Color.Black else Color(0xFFE8E6E1)).border(1.dp, Color.Black.copy(0.1f), RoundedCornerShape(6.dp)).clickable { isPlay = !isPlay },
                                        contentAlignment=Alignment.Center
                                    ) { Text("▶", fontSize=12.sp, color=if(isPlay) Color.White else Color.Black) }
                                }
                                Spacer(Modifier.width(16.dp))
                                Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE8E6E1)))
                                Spacer(Modifier.width(12.dp))
                                Row(Modifier.weight(1f), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                                    repeat(8) { idx ->
                                        val isSelected = selectedPreset == idx+1
                                        val isTapeActive = if(idx<4) tapeTracks[idx] else false
                                        Box(
                                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                                                .background(if(isSelected) Color.Black else if(isTapeActive) Color(0xFFFF5A1F).copy(0.15f) else Color(0xFFE8E6E1))
                                                .border(1.dp, if(isSelected) Color.Black else if(isTapeActive) Color(0xFFFF5A1F) else Color(0xFFE0DDD8), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    selectedPreset = idx+1
                                                    if(mode==2 && idx<4) {
                                                        val newTracks = tapeTracks.toMutableList()
                                                        newTracks[idx] = !newTracks[idx]
                                                        tapeTracks = newTracks
                                                    }
                                                },
                                            contentAlignment=Alignment.Center
                                        ) {
                                            Text("${idx+1}", fontSize=13.sp, fontWeight=FontWeight.Bold, color=if(isSelected) Color.White else if(isTapeActive) Color(0xFFFF5A1F) else Color(0xFF5A5A5A), fontFamily=FontFamily.Monospace)
                                        }
                                    }
                                }
                                Spacer(Modifier.width(devicePadding))
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E6E1)))

                            Row(Modifier.fillMaxWidth().height(if(isTablet) 108.dp else 84.dp).background(Color(0xFFFEFEFE)).padding(3.dp), horizontalArrangement=Arrangement.spacedBy(2.dp)) {
                                repeat(14) { wIdx ->
                                    val whiteNotes = listOf(0,2,4,5,7,9,11,12,14,16,17,19,21,23)
                                    val midiNote = 60 + whiteNotes[wIdx]
                                    val isActive = midiNote in activeNotes
                                    Box(
                                        Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(bottomStart=4.dp, bottomEnd=4.dp)).background(if(isActive) Color(0xFFFF5A1F) else Color.White).border(1.dp, Color(0xFFD8D5D0), RoundedCornerShape(bottomStart=4.dp, bottomEnd=4.dp)).clickable {
                                            if(midiNote in activeNotes) {
                                                activeNotes = activeNotes - midiNote
                                                audioEngine.noteOff(midiNote)
                                            } else {
                                                activeNotes = activeNotes + midiNote
                                                audioEngine.noteOn(midiNote,100)
                                            }
                                        }
                                    )
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth().height(22.dp).background(Color(0xFFFEFEFE)).padding(horizontal=12.dp),
                                horizontalArrangement=Arrangement.SpaceBetween,
                                verticalAlignment=Alignment.CenterVertically
                            ) {
                                Text("BRAUN • DIETER RAMS", fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A))
                                Text("OP-1 • ${listOf("SYNTH","DRUM","TAPE","MIXER")[mode]}", fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color(0xFF9A9A9A))
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() { audioEngine.stop(); super.onDestroy() }
}
