package com.xperiencelabs.armenu

import Dashboard.DashBoard
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.ar.core.Config
import com.xperiencelabs.armenu.ui.theme.ARMenuTheme
import com.xperiencelabs.armenu.ui.theme.Translucent
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ARMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                  Box(modifier = Modifier.fillMaxSize()){
                      if (DashBoard.foodtype == "Mandazi"){
                          val currentModel = remember {
                              mutableStateOf("donut")
                          }
                          ARScreen(currentModel.value)
                          Menu(modifier = Modifier.align(Alignment.BottomCenter)){
                              currentModel.value = it
                          }
                      }else if(DashBoard.foodtype == "Tambi Nyama"){
                          val currentModel = remember {
                              mutableStateOf("ramen")
                          }
                          ARScreen(currentModel.value)
                          Menu(modifier = Modifier.align(Alignment.BottomCenter)){
                              currentModel.value = it
                          }
                      }
                      else if(DashBoard.foodtype == "wali maharage"){
                          val currentModel = remember {
                              mutableStateOf("rice")
                          }
                          ARScreen(currentModel.value)
                          Menu(modifier = Modifier.align(Alignment.BottomCenter)){
                              currentModel.value = it
                          }
                      }
                      else {
                          val currentModel = remember {
                              mutableStateOf("burger")
                          }
                          ARScreen(currentModel.value)
                          Menu(modifier = Modifier.align(Alignment.BottomCenter)){
                              currentModel.value = it
                          }
                      }


                  }
                }
            }
        }
    }
}



@Composable
fun Menu(modifier: Modifier,onClick:(String)->Unit) {

    var currentIndex by remember {
        mutableStateOf(0)
    }
    if (DashBoard.foodtype == "Mandazi"){
        val itemsList = listOf(
            Food("ramen",R.drawable.pizza),
        )

    }else{
        val itemsList = listOf(
            Food("pizza",R.drawable.pizza),

        )

    }

    fun updateIndex(offset:Int){
//        currentIndex = (currentIndex+offset + itemsList.size) % itemsList.size
//        onClick(itemsList[currentIndex].name)
    }
    Row(modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
    }

}

@Composable
fun CircularImage(
    modifier: Modifier=Modifier,
    imageId: Int
) {
    Box(modifier = modifier
        .size(140.dp)
        .clip(CircleShape)
        .border(width = 3.dp, Translucent, CircleShape)
    ){
        Image(painter = painterResource(id = imageId), contentDescription = null, modifier = Modifier.size(140.dp), contentScale = ContentScale.FillBounds)
    }
}

@Composable
fun ARScreen(model:String) {
    val nodes = remember {
        mutableListOf<ArNode>()
    }
    val modelNode = remember {
        mutableStateOf<ArModelNode?>(null)
    }
    val placeModelButton = remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize()){
        val foodtype=DashBoard.foodtype;
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = {arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false
                modelNode.value = ArModelNode(arSceneView.engine,PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = 0.8f
                    ){

                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = {node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }

                }
                nodes.add(modelNode.value!!)
            },
            onSessionCreate = {
                planeRenderer.isVisible = false
            }
        )
        if(placeModelButton.value){
            Button(onClick = {
                modelNode.value?.anchor()
            }, modifier = Modifier.align(Alignment.Center)) {
                Text(text = "Place It")
            }
        }
    }


LaunchedEffect(key1 = model){
    modelNode.value?.loadModelGlbAsync(
        glbFileLocation = "models/${model}.glb",
        scaleToUnits = 0.8f
    )
    Log.e("errorloading","ERROR LOADING MODEL")
}

}


data class Food(var name:String,var imageId:Int)






