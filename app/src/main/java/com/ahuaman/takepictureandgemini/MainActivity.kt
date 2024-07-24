package com.ahuaman.takepictureandgemini

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.ahuaman.takepictureandgemini.ui.theme.TakePictureAndGeminiTheme
import com.ahuaman.takepictureandgemini.utils.createImageFile
import com.ahuaman.takepictureandgemini.utils.uriToBitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TakePictureAndGeminiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    var textGenerated by remember {
        mutableStateOf<String>("")
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedImageUri = uri
        } else {
            capturedImageUri = Uri.EMPTY
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission is granted
            cameraLauncher.launch(uri)
        } else {
            // Permission is denied
        }
    }


    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Button(onClick = {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }) {
            Text(text = "Tomar foto")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        capturedImageUri?.let { uri_file ->

            if(uri_file == Uri.EMPTY) return@let

            Image(
                painter = rememberAsyncImagePainter(model = uri_file),
                contentDescription = "Captured Image",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(text = textGenerated, modifier = Modifier.padding(8.dp))

            val model = GenerativeModel(
                modelName = "gemini-1.5-pro",
                apiKey = "AIzaSyChe9FtZRzWmupVkBtsz89YBnpOeZrpFyc",
            )

            GlobalScope.launch {
                val response = model.generateContent(
                    content {
                        context.uriToBitmap(uri_file)?.let {
                            image(it)
                        }
                        text("Puedes describir los detalles de la imagen")
                    }
                )

                // Use the response
                response?.let {
                    // Do something with the response
                    textGenerated = it.text.toString()
                    println("Response: ${it.text}")
                }
            }
        }
    }
}