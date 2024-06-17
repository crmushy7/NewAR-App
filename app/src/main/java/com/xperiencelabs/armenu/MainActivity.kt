package com.xperiencelabs.armenu

import Dashboard.DashBoard
import android.content.Context
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
import androidx.compose.ui.graphics.Color
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
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import java.text.DateFormat
import java.util.Calendar
import kotlin.math.PI

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
                            "Tambi Nyama" -> currentModel.value = "tambi"
                            "wali maharage" -> currentModel.value = "rice"
                            "Wali nyama" -> currentModel.value = "walikuku"
                            else -> currentModel.value = "pizza"
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
    val rotationX = remember { mutableStateOf(0f) }
    val rotationY = remember { mutableStateOf(0f) }
    val rotationZ = remember { mutableStateOf(0f) }
    val translationX = remember { mutableStateOf(0f) }
    val translationY = remember { mutableStateOf(0f) }
    val context = LocalContext.current

    // State for text visibility
    var isTextVisible by remember { mutableStateOf(false) }

    // Sample food ingredients text
    val foodIngredients = when (DashBoard.foodtype) {
        "Mandazi" -> "Flour, Sugar, Coconut Milk, Cardamom"
        "Tambi Nyama" -> "Beef, Spaghetti, Tomatoes, Onions, Spices"
        "wali maharage" -> "Rice, Beans, Onions, Coconut Milk"
        "Wali nyama" -> "Rice, Chicken, Tomatoes, Spices"
        else -> "Ingredient 1, Ingredient 2, Ingredient 3"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, rotationChange ->
                            scale.value *= zoom
                            val degreesChange = rotationChange * (180 / PI.toFloat())

                            // Apply rotation to Y axis by default
                            rotationY.value += degreesChange
                            translationX.value += pan.x
                            translationY.value += pan.y

                            // Log transformation values including intermediate rotation change
                            Log.d("ARScreen", "Scale: ${scale.value}, RotationY (Degrees Change): $degreesChange, Total RotationY: ${rotationY.value}, TranslationX: ${translationX.value}, TranslationY: ${translationY.value}")
                        }
                    )
                },
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer?.apply {
                    isShadowReceiver = false
                    isVisible = true
                }

                modelNode.value = ArModelNode(arSceneView.engine).apply {
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
            // Additional debug logs before applying transformation
            Log.d("ARScreen", "Before applying - Scale: ${scale.value}, RotationX: ${rotationX.value}, RotationY: ${rotationY.value}, RotationZ: ${rotationZ.value}, TranslationX: ${translationX.value}, TranslationY: ${translationY.value}")
            node.scale = Scale(scale.value, scale.value, scale.value)
            node.rotation = Rotation(rotationX.value, rotationY.value, rotationZ.value)
            node.position = Position(translationX.value, translationY.value, node.position.z)

            // Log applied transformations
            Log.d("ARScreen", "Applied - Scale: ${node.scale}, Rotation: ${node.rotation}, Position: ${node.position}")
        }

        // Place model button
        if (placeModelButton.value) {
            Button(
                onClick = {
                    modelNode.value?.anchor()
                },
                modifier = Modifier.align(Alignment.Center),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6106)) // Custom color
            ) {
                Text(text = "Place It")
            }
        }

        // Rotation and flipping buttons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomButton(onClick = { rotationX.value -= 10f }, text = "Rotate X-", iconId = R.drawable.ic_rotate_left)
                    CustomButton(onClick = { rotationX.value += 10f }, text = "Rotate X+", iconId = R.drawable.ic_rotate_right)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomButton(onClick = { rotationY.value -= 10f }, text = "Rotate Y-", iconId = R.drawable.ic_rotate_down)
                    CustomButton(onClick = { rotationY.value += 10f }, text = "Rotate Y+", iconId = R.drawable.ic_rotate_up)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomButton(onClick = { rotationZ.value -= 10f }, text = "Rotate Z-", iconId = R.drawable.ic_rotate_left_bottom)
                    CustomButton(onClick = { rotationZ.value += 10f }, text = "Rotate Z+", iconId = R.drawable.ic_rotate_right_top)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomButton(onClick = { scale.value -= 0.1f }, text = "Scale -", iconId = R.drawable.ic_zoom_out)
                    CustomButton(onClick = { scale.value += 0.1f }, text = "Scale +", iconId = R.drawable.ic_zoom_in)
                }
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
                    saveOrderToFirebase(FirebaseDatabase.getInstance().reference, model, context)
                },
                modifier = Modifier.align(Alignment.TopEnd),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6106)) // Custom color
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
                modifier = Modifier.align(Alignment.TopStart),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6106)) // Custom color
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

@Composable
fun CustomButton(onClick: () -> Unit, text: String, iconId: Int) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6106)) // Custom color
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

data class Rotation(val x: Float, val y: Float, val z: Float)

fun saveOrderToFirebase(database: DatabaseReference, model: String, context: Context) {
    if (DashBoard.tableStatus == null) {
        Toast.makeText(context, "Table number unspecified", Toast.LENGTH_SHORT).show()
        return
    }

    val calendar = Calendar.getInstance()
    val currentdate = DateFormat.getInstance().format(calendar.time)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1 // Adding 1 because January is represented as 0
    val year = calendar.get(Calendar.YEAR)
    val dateOnly = "$day-$month-$year"

    val placeOrderRef = database.child("Tables")
        .child(dateOnly)
        .child(DashBoard.tableStatus)
        .push()

    val orderID = placeOrderRef.key?.trim()
    val orderDetails = mapOf(
        "FoodName" to DashBoard.menuname,
        "FoodPrice" to DashBoard.menuprice,
        "Status" to "Not served",
        "Date" to "$currentdate Hrs",
        "orderID" to orderID,
        "tableNumber" to DashBoard.tableStatus
    )

    placeOrderRef.setValue(orderDetails).addOnSuccessListener {
        val historyRef = database.child("History").child(dateOnly).child(orderID ?: "")
        historyRef.setValue(orderDetails).addOnSuccessListener {
            Toast.makeText(context, "Order placed! Please wait a few minutes and it will be served to you!", Toast.LENGTH_LONG).show()
        }
    }
}

data class Food(var name: String, var imageId: Int)
