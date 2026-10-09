
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerpro.audio.AudioEngine
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.init(48000)
        audioEngine.start()
        setContent {
            MaterialTheme {
                var isPlaying by remember { mutableStateOf(false) }
                var isRecording by remember { mutableStateOf(false) }
                var playheadPos by remember { mutableStateOf(0.68f) }
                var engineType by remember { mutableStateOf(0) }
                var selectedAlgo by remember { mutableStateOf(5) }
                var feedback by remember { mutableStateOf(0.6f) }
                var activeNotes by remember { mutableStateOf(setOf<Int>()) }
                val waveform = remember { List(180) { val r = Random.nextFloat(); (r * r * 0.85f + 0.04f) } }
                val recPulse by rememberInfiniteTransition(label="rec").animateFloat(initialValue=0.5f, targetValue=1f, animationSpec=infiniteRepeatable(tween(600), RepeatMode.Reverse), label="recPulse")
                LaunchedEffect(engineType) { audioEngine.setEngineType(if(engineType==0) 0 else if(engineType==1) 0 else 1) }
                Box(Modifier.fillMaxSize().background(Color(0xFFDAD8D3)).padding(8.dp), contentAlignment=Alignment.Center) {
                    Box(Modifier.fillMaxWidth().shadow(24.dp, RoundedCornerShape(32.dp), spotColor=Color.Black.copy(0.3f)).border(7.dp, Color.Black, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(24.dp)).background(Color(0xFFF7F5F2)).padding(18.dp)) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0EEE8))) {
                                Canvas(Modifier.fillMaxSize()) {
                                    val barW = size.width / waveform.size
                                    waveform.forEachIndexed { i, h ->
                                        val x = i * barW
                                        val barH = h * size.height * 0.48f
                                        drawLine(Color.Black, Offset(x, size.height*0.38f - barH/2), Offset(x, size.height*0.38f + barH/2), 1.2f)
                                    }
                                }
                                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom=5.dp, start=10.dp, end=10.dp), horizontalArrangement=Arrangement.SpaceBetween) {
                                    listOf("00:00","00:15","00:30","00:45","01:00","01:15","01:30").forEach { t ->
                                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.width(1.dp).height(3.dp).background(Color.Black.copy(0.3f)))
                                            Spacer(Modifier.height(2.dp))
                                            Text(t, fontSize=9.sp, fontFamily=FontFamily.Monospace, color=Color.Black)
                                        }
                                    }
                                }
                                Box(Modifier.fillMaxHeight().width(2.dp).background(Color(0xFFC41E1E)).align(Alignment.TopStart).offset(x = (playheadPos * 360).dp))
                                Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> playheadPos = (change.position.x / size.width).coerceIn(0f,1f) } })
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                                Box(Modifier.width(272.dp).height(142.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
                                    Row(Modifier.fillMaxSize()) {
                                        Box(Modifier.weight(1.15f).fillMaxHeight().background(Color(0xFF0F0F0F)).padding(10.dp)) {
                                            Column {
                                                Row(verticalAlignment=Alignment.CenterVertically) {
                                                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White).padding(horizontal=5.dp, vertical=2.dp)) {
                                                        Row(verticalAlignment=Alignment.CenterVertically) {
                                                            Box(Modifier.size(5.dp).clip(CircleShape).background(if(isRecording) Color.Red.copy(alpha=recPulse) else Color.Black))
                                                            Spacer(Modifier.width(3.dp))
                                                            Text("REC", fontSize=8.sp, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                        }
                                                    }
                                                    Spacer(Modifier.width(5.dp))
                                                    Text("HD", fontSize=9.sp, color=Color.White, fontFamily=FontFamily.Monospace)
                                                    Spacer(Modifier.weight(1f))
                                                    Text("3/10", fontSize=9.sp, color=Color.Gray, fontFamily=FontFamily.Monospace)
                                                }
                                                Spacer(Modifier.height(16.dp))
                                                Text("96.00 kHz • 24 bit", fontSize=8.sp, color=Color(0xFFAAAAAA), fontFamily=FontFamily.Monospace)
                                                Spacer(Modifier.height(5.dp))
                                                Row(verticalAlignment=Alignment.Bottom) {
                                                    Text("01", fontSize=24.sp, color=Color.White, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                    Text(" H ", fontSize=9.sp, color=Color.Gray)
                                                    Text("04", fontSize=24.sp, color=Color.White, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                    Text(" M ", fontSize=9.sp, color=Color.Gray)
                                                    Text("25", fontSize=24.sp, color=Color.White, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                    Text(" S", fontSize=9.sp, color=Color.Gray)
                                                }
                                            }
                                        }
                                        Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEDE9E3)).padding(10.dp)) {
                                            Column {
                                                if (engineType==0) {
                                                    Text("MyFavSong.wav", fontSize=11.sp, color=Color.Black, fontWeight=FontWeight.SemiBold)
                                                    Text("35 MB", fontSize=9.sp, color=Color.Gray, fontFamily=FontFamily.Monospace)
                                                    Spacer(Modifier.height(10.dp))
                                                    Row(verticalAlignment=Alignment.CenterVertically) {
                                                        Text("L", fontSize=8.sp, fontFamily=FontFamily.Monospace)
                                                        Spacer(Modifier.width(4.dp))
                                                        Canvas(Modifier.width(58.dp).height(5.dp)) {
                                                            val active = if(isPlaying||isRecording) 9 else 1
                                                            for(i in 0..11) { drawRect(if(i<active) Color.Black else Color.Black.copy(0.15f), Offset(i*size.width/12,0f), Size(size.width/12*0.65f, size.height)) }
                                                        }
                                                        Spacer(Modifier.width(3.dp))
                                                        Text("-16dB", fontSize=7.sp, fontFamily=FontFamily.Monospace)
                                                    }
                                                    Spacer(Modifier.height(3.dp))
                                                    Row(verticalAlignment=Alignment.CenterVertically) {
                                                        Text("R", fontSize=8.sp, fontFamily=FontFamily.Monospace)
                                                        Spacer(Modifier.width(4.dp))
                                                        Canvas(Modifier.width(58.dp).height(5.dp)) {
                                                            val active = if(isPlaying||isRecording) 7 else 1
                                                            for(i in 0..11) { drawRect(if(i<active) Color.Black else Color.Black.copy(0.15f), Offset(i*size.width/12,0f), Size(size.width/12*0.65f, size.height)) }
                                                        }
                                                        Spacer(Modifier.width(3.dp))
                                                        Text("-8dB", fontSize=7.sp, fontFamily=FontFamily.Monospace)
                                                    }
                                                } else if (engineType==1) {
                                                    Text("DEXED FM • BRAUN", fontSize=10.sp, color=Color.Black, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                    Spacer(Modifier.height(5.dp))
                                                    Text("ALGO $selectedAlgo FB ${(feedback*100).toInt()}%", fontSize=8.sp, fontFamily=FontFamily.Monospace, color=Color.DarkGray)
                                                    Spacer(Modifier.height(6.dp))
                                                    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                                                        for (i in 0..5) {
                                                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                                                val h = 8 + i*2
                                                                Box(Modifier.width(14.dp).height(22.dp).clip(RoundedCornerShape(2.dp)).background(Color.Black.copy(0.08f))) { Box(Modifier.fillMaxWidth().height(h.dp).background(Color(0xFFFF5A1F)).align(Alignment.BottomCenter)) }
                                                                Text("OP${i+1}", fontSize=6.sp, fontFamily=FontFamily.Monospace)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Text("SURGE • SUB", fontSize=10.sp, color=Color.Black, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }
                                }
                                Box(Modifier.size(142.dp), contentAlignment=Alignment.Center) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        drawCircle(Color(0xFFE8E6E1), radius=size.minDimension/2)
                                        drawCircle(Color.White, radius=size.minDimension/2, style=Stroke(1.dp.toPx()))
                                        drawCircle(Color.White, radius=size.minDimension*0.28f)
                                        drawCircle(Color.Black.copy(0.08f), radius=size.minDimension*0.28f, style=Stroke(1.dp.toPx()))
                                    }
                                    Text("^", Modifier.align(Alignment.TopCenter).padding(top=8.dp).clickable { playheadPos=0f }, fontSize=14.sp, fontWeight=FontWeight.Bold)
                                    Text("v", Modifier.align(Alignment.BottomCenter).padding(bottom=8.dp).clickable { playheadPos=1f }, fontSize=14.sp, fontWeight=FontWeight.Bold)
                                    Text("<<", Modifier.align(Alignment.CenterStart).padding(start=10.dp).clickable { playheadPos=(playheadPos-0.05f).coerceAtLeast(0f) }, fontSize=11.sp, fontWeight=FontWeight.Bold)
                                    Text(">>", Modifier.align(Alignment.CenterEnd).padding(end=10.dp).clickable { playheadPos=(playheadPos+0.05f).coerceAtMost(1f) }, fontSize=11.sp, fontWeight=FontWeight.Bold)
                                    Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.Black.copy(0.1f), CircleShape).clickable { isPlaying = !isPlaying; if(isPlaying) audioEngine.noteOn(60,100) else audioEngine.noteOff(60) }, contentAlignment=Alignment.Center) { Text(if(isPlaying) "❚❚" else "▶", fontSize=15.sp, color=Color.Black) }
                                }
                                Column(verticalArrangement=Arrangement.spacedBy(16.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                        Box(Modifier.size(50.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color(0xFFF0EEE8)).border(1.dp, Color.Black.copy(0.1f), CircleShape).clickable { isPlaying=false; isRecording=false; activeNotes.forEach { audioEngine.noteOff(it) }; activeNotes=emptySet() }, contentAlignment=Alignment.Center) { Box(Modifier.size(13.dp).background(Color.Black)) }
                                        Spacer(Modifier.height(5.dp))
                                        Text("STOP", fontSize=9.sp, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                        Box(Modifier.size(50.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(if(isRecording) Color(0xFFFFE8E0) else Color(0xFFF0EEE8)).border(if(isRecording)2.dp else 1.dp, if(isRecording) Color(0xFFFF5A1F) else Color.Black.copy(0.1f), CircleShape).clickable { isRecording = !isRecording }, contentAlignment=Alignment.Center) { Box(Modifier.size(13.dp).clip(CircleShape).background(Color(0xFFD92D2D).copy(alpha=if(isRecording) recPulse else 1f))) }
                                        Spacer(Modifier.height(5.dp))
                                        Text("REC/PAUSE", fontSize=8.sp, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(7.dp), verticalAlignment=Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFFE8E6E1)).border(1.dp, Color.Black.copy(0.08f), RoundedCornerShape(9.dp)).clickable {}, contentAlignment=Alignment.Center) { Text("⌕", fontSize=15.sp) }
                                listOf("HOME","BACK","DIVIDE","OPTIONS").forEachIndexed { idx, label ->
                                    val sel = when(idx){0->engineType==0;2->engineType==1;3->engineType==2;else->false}
                                    Box(Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(if(sel) Color.Black else Color(0xFFE8E6E1)).border(1.dp, Color.Black.copy(0.08f), RoundedCornerShape(16.dp)).clickable { when(label){"HOME"->engineType=0;"DIVIDE"->engineType=1;"OPTIONS"->engineType=2} }.padding(horizontal=16.dp), contentAlignment=Alignment.Center) { Text(label, fontSize=9.sp, fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace, color=if(sel) Color.White else Color.Black) }
                                }
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFF5A1F)))
                            }
                            if (engineType!=0) {
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFE8E6E1)).padding(5.dp), horizontalArrangement=Arrangement.spacedBy(2.dp)) {
                                    val notes = listOf(60,62,64,65,67,69,71,72)
                                    val noteNames = listOf("C","D","E","F","G","A","B","C")
                                    notes.forEachIndexed { index, midi ->
                                        val active = midi in activeNotes
                                        Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(6.dp)).background(if(active) Color(0xFFFF5A1F) else Color.White).border(1.dp, Color.Black.copy(0.12f), RoundedCornerShape(6.dp)).clickable { if(midi in activeNotes){activeNotes=activeNotes-midi;audioEngine.noteOff(midi)} else {activeNotes=activeNotes+midi;audioEngine.noteOn(midi,100)} }, contentAlignment=Alignment.BottomCenter) { Text(noteNames[index], fontSize=7.sp, fontFamily=FontFamily.Monospace, color=if(active) Color.White else Color.Gray, modifier=Modifier.padding(bottom=3.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() { audioEngine.stop(); super.onDestroy() }
}
