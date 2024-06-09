package com.xperiencelabs.armenu

import Dashboard.DashBoard
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.ar.core.Config
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xperiencelabs.armenu.ui.theme.ARMenuTheme
import com.xperiencelabs.armenu.ui.theme.Translucent
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ARMenuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val foodType = DashBoard.foodtype
                        val currentModel = remember { mutableStateOf("") }

                        when (foodType) {
                            "Mandazi" -> currentModel.value = "donut"
                            "Tambi Nyama" -> currentModel.value = "ramen"
                            "wali maharage" -> currentModel.value = "rice"
                            else -> currentModel.value = "burger"
                        }

                        ARScreen(model = currentModel.value)
                        Menu(modifier = Modifier.align(Alignment.BottomCenter)) {
                            currentModel.value = it
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Menu(modifier: Modifier, onClick: (String) -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    val itemsList = listOf(
        Food("ramen", R.drawable.pizza),
    )

    fun updateIndex(offset: Int) {
        currentIndex = (currentIndex + offset + itemsList.size) % itemsList.size
        onClick(itemsList[currentIndex].name)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Add menu content if needed
    }
}

@Composable
fun CircularImage(
    modifier: Modifier = Modifier,
    imageId: Int
) {
    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .border(width = 3.dp, Translucent, CircleShape)
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun ARScreen(model: String) {
    val nodes = remember { mutableStateListOf<ArNode>() }
    val modelNode = remember { mutableStateOf<ArModelNode?>(null) }
    val placeModelButton = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    val rotation = remember { mutableStateOf(0f) }
    val translationX = remember { mutableStateOf(0f) }
    val translationY = remember { mutableStateOf(0f) }
    val context = LocalContext.current

    // State for text visibility
    var isTextVisible by remember { mutableStateOf(false) }

    // Sample food ingredients text
    val foodIngredients = "Ingredient 1, Ingredient 2, Ingredient 3"

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotationChange ->
                        scale.value *= zoom
                        rotation.value += rotationChange
                        translationX.value += pan.x
                        translationY.value += pan.y
                    }
                },
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer?.apply {
                    isShadowReceiver = false
                    isVisible = true
                }

                modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/$model.glb",
                        scaleToUnits = 0.8f
                    ) {
                        Log.d("ARScreen", "Model loaded successfully")
                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = { node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }
                }

                nodes.add(modelNode.value!!)
            }
        )

        // Apply transformations to the model node
        modelNode.value?.let { node ->
            node.scale = Scale(scale.value, scale.value, scale.value)
            node.rotation = Rotation(0f, rotation.value, 0f)
            node.position = Position(translationX.value, translationY.value, 0f)
        }

        // Place model button
        if (placeModelButton.value) {
            Button(
                onClick = {
                    modelNode.value?.anchor()
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = "Place It")
            }
        }

        // Place order button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    saveOrderToFirebase(FirebaseDatabase.getInstance().reference, model)
                    Toast.makeText(context, "Order Placed", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(text = "Place Order")
            }
        }

        // Toggle text visibility button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Toggle text visibility on button click
            Button(
                onClick = { isTextVisible = !isTextVisible },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(text = if (isTextVisible) "Hide Ingredients" else "Show Ingredients")
            }

            // Display food ingredients text
            if (isTextVisible) {
                Text(
                    text = foodIngredients,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 56.dp)
                )
            }
        }
    }

    LaunchedEffect(key1 = model) {
        modelNode.value?.loadModelGlbAsync(
            glbFileLocation = "models/$model.glb",
            scaleToUnits = 0.8f
        ) {
            Log.e("ARScreen", "Error loading model")
        }
    }
}

fun saveOrderToFirebase(database: DatabaseReference, model: String) {
    val order = mapOf(
        "model" to model,
        "timestamp" to System.currentTimeMillis()
    )
    database.child("Orders").push().setValue(order)
}

data class Food(var name: String, var imageId: Int)
