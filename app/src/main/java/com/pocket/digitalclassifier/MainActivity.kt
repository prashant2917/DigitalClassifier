package com.pocket.digitalclassifier

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocket.digitalclassifier.DigitClassifier.Companion.TAG
import com.pocket.digitalclassifier.ui.theme.DigitalClassifierTheme
import com.pocket.digitalclassifier.util.Util

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitalClassifierTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val picture = remember { Picture() }
                   // val predicatedText = remember { ("")}
                    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
                    val viewModel = viewModel<DrawingViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    val digitalClassifier = DigitClassifier(baseContext)
                    digitalClassifier.initialize().addOnFailureListener { e -> Log.e(TAG, "Error to setting up digit classifier.", e) }
                    val predicatedSText by viewModel.predicatedText.collectAsStateWithLifecycle()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DrawingCanvas(
                            paths = state.paths,
                            currentPath = state.currentPath,
                            picture = picture,
                            onAction = viewModel::onAction,
                            digitalClassifier,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        CanvasControls(
                            selectedColor = state.selectedColor,
                            colors = allColors,
                            onSelectColor = {
                                viewModel.onAction(DrawingAction.OnSelectColor(it))
                            },
                        )

                        CanvasButton(buttonText = "Convert", onClick = {
                            imageBitmap = Util.createBitmapFromPicture(picture)
                            imageBitmap?.let {
                                Log.d(
                                    TAG,
                                    "onCreate: width ${it.width} height ${it.height} bitmap $it")
                                viewModel.onAction(DrawingAction.PredicateText(it, digitalClassifier))
                                Toast.makeText(baseContext, predicatedSText, Toast.LENGTH_LONG).show()
                            }

                        })
                        CanvasButton(
                            buttonText = "Clear Canvas",
                            onClick = { viewModel.onAction(DrawingAction.OnClearCanvasClick)

                                       imageBitmap?.recycle()
                            })

                    }
                }
            }
        }
    }
}