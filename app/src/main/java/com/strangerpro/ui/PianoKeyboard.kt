package com.strangerpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class PianoKey(val midiNote: Int, val isBlack: Boolean, val rect: Rect = Rect.Zero)

@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    octaves: Int = 2,
    startOctave: Int = 4,
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit
) {
    var activeNotes by remember { mutableStateOf(setOf<Int>()) }
    var keys by remember { mutableStateOf(listOf<PianoKey>()) }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(140.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    // find key at offset
                    val key = keys.firstOrNull { it.rect.contains(offset) }
                    if (key != null) {
                        activeNotes = activeNotes + key.midiNote
                        onNoteOn(key.midiNote)
                        tryAwaitRelease()
                        activeNotes = activeNotes - key.midiNote
                        onNoteOff(key.midiNote)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    val key = keys.firstOrNull { it.rect.contains(offset) }
                    if (key != null && key.midiNote !in activeNotes) {
                        activeNotes = activeNotes + key.midiNote
                        onNoteOn(key.midiNote)
                    }
                },
                onDrag = { change, _ ->
                    val key = keys.firstOrNull { it.rect.contains(change.position) }
                    // simple monophonic drag for now
                    if (key != null && key.midiNote !in activeNotes) {
                        // release previous
                        activeNotes.forEach { onNoteOff(it) }
                        activeNotes = setOf(key.midiNote)
                        onNoteOn(key.midiNote)
                    }
                },
                onDragEnd = {
                    activeNotes.forEach { onNoteOff(it) }
                    activeNotes = emptySet()
                }
            )
        }
    ) {
        val whiteKeyCount = octaves * 7
        val whiteKeyWidth = size.width / whiteKeyCount
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = size.height * 0.6f

        val newKeys = mutableListOf<PianoKey>()
        var whiteIndex = 0
        // isBlack pattern for one octave C to B: C, C#, D, D#, E, F, F#, G, G#, A, A#, B
        val pattern = listOf(false, true, false, true, false, false, true, false, true, false, true, false)
        val noteOffsets = listOf(0,1,2,3,4,5,6,7,8,9,10,11)

        for (oct in 0 until octaves) {
            for (i in 0 until 12) {
                val midi = (startOctave + oct) * 12 + i
                val isBlack = pattern[i]
                if (!isBlack) {
                    val left = whiteIndex * whiteKeyWidth
                    val rect = Rect(Offset(left, 0f), androidx.compose.ui.geometry.Size(whiteKeyWidth, size.height))
                    newKeys.add(PianoKey(midi, false, rect))
                    val isActive = midi in activeNotes
                    drawRect(
                        color = if (isActive) Color(0xFF00E5FF) else Color.White,
                        topLeft = Offset(left, 0f),
                        size = androidx.compose.ui.geometry.Size(whiteKeyWidth, size.height)
                    )
                    // border
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(left, 0f),
                        size = androidx.compose.ui.geometry.Size(whiteKeyWidth, size.height),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                    whiteIndex++
                }
            }
        }
        // draw black keys on top
        whiteIndex = 0
        for (oct in 0 until octaves) {
            for (i in 0 until 12) {
                val midi = (startOctave + oct) * 12 + i
                val isBlack = pattern[i]
                if (!isBlack) {
                    whiteIndex++
                } else {
                    // black key is between white keys
                    val left = whiteIndex * whiteKeyWidth - blackKeyWidth/2
                    val rect = Rect(Offset(left, 0f), androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight))
                    newKeys.add(PianoKey(midi, true, rect))
                    val isActive = midi in activeNotes
                    drawRect(
                        color = if (isActive) Color(0xFFFF4081) else Color(0xFF212121),
                        topLeft = Offset(left, 0f),
                        size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight)
                    )
                }
            }
        }
        keys = newKeys
    }
}
