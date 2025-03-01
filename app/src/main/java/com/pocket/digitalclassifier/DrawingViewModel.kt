package com.pocket.digitalclassifier

import android.graphics.Bitmap
import android.graphics.Picture
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.pocket.digitalclassifier.DigitClassifier.Companion.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DrawingState(
    val selectedColor: Color = Color.Black,
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList()
)

val allColors = listOf(
    Color.Black,
    Color.Red,
    Color.Blue,
    Color.Green,
    Color.Yellow,
    Color.Magenta,
    Color.Cyan,
)

data class PathData(
    val id: String,
    val color: Color,
    val path: List<Offset>
)

sealed interface DrawingAction {
    data object OnNewPathStart : DrawingAction
    data class OnDraw(val offset: Offset) : DrawingAction
    data object OnPathEnd : DrawingAction
    data class OnSelectColor(val color: Color) : DrawingAction
    data object OnClearCanvasClick : DrawingAction
    data class StartPictureRecording(val picture: Picture, val width: Int, val height: Int) : DrawingAction
    data class EndPictureRecording(val picture: Picture):DrawingAction
    data class PredicateText(val bitmap: Bitmap, val digitClassifier: DigitClassifier):DrawingAction
}

class DrawingViewModel : ViewModel() {

    private val _state = MutableStateFlow(DrawingState())
    val state = _state.asStateFlow()

    private val _canvasFlow = MutableStateFlow(android.graphics.Canvas())
    val canvasFlow = _canvasFlow.asStateFlow()

    private val _bitmapFlow = MutableStateFlow(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val bitmapFlow = _bitmapFlow.asStateFlow()

    private val _predicatedText = MutableStateFlow("")
    val predicatedText = _predicatedText.asStateFlow()

    fun onAction(action: DrawingAction) {
        when (action) {
            DrawingAction.OnClearCanvasClick -> onClearCanvasClick()
            is DrawingAction.OnDraw -> onDraw(action.offset)
            DrawingAction.OnNewPathStart -> onNewPathStart()
            DrawingAction.OnPathEnd -> onPathEnd()
            is DrawingAction.OnSelectColor -> onSelectColor(action.color)
            is DrawingAction.StartPictureRecording -> startPictureRecording(action.picture, action.width, action.height)
            is DrawingAction.EndPictureRecording -> endPictureRecording(action.picture)
            is DrawingAction.PredicateText -> predicateText(action.bitmap, action.digitClassifier)

        }
    }

    private fun onSelectColor(color: Color) {
        _state.update {
            it.copy(
                selectedColor = color
            )
        }
    }

    private fun onPathEnd() {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = null,
                paths = it.paths + currentPathData
            )
        }
    }

    private fun onNewPathStart() {
        _state.update {
            it.copy(
                currentPath = PathData(
                    id = System.currentTimeMillis().toString(),
                    color = it.selectedColor,
                    path = emptyList()
                )
            )
        }

    }

    private fun onDraw(offset: Offset) {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = currentPathData.copy(
                    path = currentPathData.path + offset
                )
            )
        }
    }

    private fun onClearCanvasClick() {
        _state.update {
            it.copy(
                currentPath = null,
                paths = emptyList()
            )
        }
    }

    private fun startPictureRecording(picture: Picture,width: Int, height: Int) {
         _canvasFlow.update {
            picture.beginRecording(width, height)
         }
    }

    private fun endPictureRecording(picture: Picture) {
        picture.endRecording()
    }

    private fun predicateText(bitmap: Bitmap, digitClassifier: DigitClassifier) {
        if (digitClassifier.isInitialized) {
            digitClassifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { resultText ->
                    //predictedTextView?.text = resultText
                    _predicatedText.value = resultText
                    Log.d(TAG, "predicated text: $resultText")
                }
                .addOnFailureListener { e ->
                    _predicatedText.value = e.toString()
                    Log.e(TAG, "Error classifying drawing.", e)
                }
        }
    }
}