package com.strangerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerpro.audio.AudioEngine
import com.strangerpro.ui.Knob
import com.strangerpro.ui.PianoKeyboard

class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.init(48000)
        audioEngine.start()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0A0A0A),
                    surface = Color(0xFF1A1A1A)
                )
            ) {
                var engineType by remember { mutableStateOf(0) } // 0 dexed, 1 surge
                var algorithm by remember { mutableStateOf(5) }
                var feedback by remember { mutableStateOf(0.6f) }
                var opLevels by remember { mutableStateOf(listOf(1f,1f,1f,1f,1f,0.8f)) }
                var cutoff by remember { mutableStateOf(0.6f) }
                var resonance by remember { mutableStateOf(0.3f) }
                var waveType by remember { mutableStateOf(0) }

                LaunchedEffect(engineType) {
                    audioEngine.setEngineType(engineType)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F0F0F), Color(0xFF1E1E1E), Color(0xFF121212))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("STRANGER PRO", color = Color.White, fontSize = 20.sp, letterSpacing = 2.sp)
                            Row {
                                FilterChip(
                                    selected = engineType==0,
                                    onClick = { engineType=0 },
                                    label = { Text("DEXED FM") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E5FF))
                                )
                                Spacer(Modifier.width(8.dp))
                                FilterChip(
                                    selected = engineType==1,
                                    onClick = { engineType=1 },
                                    label = { Text("SURGE") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF4081))
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Controls
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (engineType==0) {
                                // DEXED CONTROLS
                                Column(Modifier.padding(12.dp)) {
                                    Text("DEXED FM ENGINE - 6 Operators", color = Color(0xFF00E5FF), fontSize = 12.sp)
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        item {
                                            Knob("ALGO", algorithm/31f, { v ->
                                                algorithm = (v*31).toInt()
                                                audioEngine.setAlgorithm(algorithm)
                                            })
                                        }
                                        item {
                                            Knob("FEEDBK", feedback, { v ->
                                                feedback = v
                                                audioEngine.setFeedback(v*0.9f)
                                            })
                                        }
                                        items(6) { idx ->
                                            Knob("OP${idx+1}", opLevels[idx], { v ->
                                                val newList = opLevels.toMutableList()
                                                newList[idx]=v
                                                opLevels = newList
                                                audioEngine.setParam(10+idx, v)
                                            })
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Algorithm ${algorithm} - ${getAlgoDesc(algorithm)}", color = Color.Gray, fontSize = 10.sp)
                                }
                            } else {
                                // SURGE CONTROLS
                                Column(Modifier.padding(12.dp)) {
                                    Text("SURGE SUBTRACTIVE ENGINE", color = Color(0xFFFF4081), fontSize = 12.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Knob("CUTOFF", cutoff, { v ->
                                            cutoff=v
                                            audioEngine.setParam(1, v)
                                        })
                                        Knob("RESO", resonance, { v ->
                                            resonance=v
                                            audioEngine.setParam(2, v)
                                        })
                                        Knob("WAVE", waveType/3f, { v ->
                                            waveType = (v*3).toInt()
                                            audioEngine.setParam(3, waveType.toFloat())
                                        })
                                        Column {
                                            Text("Wave: ${listOf("SAW","SQUARE","SINE","WT")[waveType]}", color=Color.White, fontSize=11.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Text("Surge XT inspired\nLowpass + Drive", color=Color.Gray, fontSize=10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Keyboard
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Text("KEYBOARD - Touch to play (poly 12 voices)", color = Color.LightGray, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                PianoKeyboard(
                                    modifier = Modifier.fillMaxWidth(),
                                    octaves = 2,
                                    startOctave = 4,
                                    onNoteOn = { note ->
                                        audioEngine.noteOn(note, 100)
                                    },
                                    onNoteOff = { note ->
                                        audioEngine.noteOff(note)
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Stranger Pro v1.0 - Dexed + Surge engines - Oboe low-latency - Ready for SYSEX import", color=Color.DarkGray, fontSize=9.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        audioEngine.stop()
        super.onDestroy()
    }

    private fun getAlgoDesc(a: Int): String {
        return when(a%4) {
            0 -> "Chain 1>2>3>4>5>6 (Classic DX Piano)"
            1 -> "Dual Stack (Pad)"
            2 -> "Parallel Carriers (Organ/Additive)"
            else -> "Modulators > Carrier (Brass)"
        }
    }
}
