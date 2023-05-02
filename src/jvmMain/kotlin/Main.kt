import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.application
import com.aspose.threed.Scene
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun FrameWindowScope.App() {

    var filePicker by remember { mutableStateOf(false) }

    var modelFile by remember { mutableStateOf<File?>(null) }
    val extensions = ModelOutputTypes.values().map { it.extension }

    //TODO: Add multiple file converter

    if (filePicker) {
        FileDialog(
            FileDialogMode.Load,
            title = "Choose a file",
            block = {
                setFilenameFilter { _, f -> f.split(".").lastOrNull() in extensions }
            }
        ) { file -> modelFile = file?.let { File(it) } }
    }

    var outputType by remember { mutableStateOf(ModelOutputTypes.Stl) }
    var dragState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        window.dropTarget = DropTarget().apply {
            addDropTargetListener(object : DropTargetAdapter() {
                override fun dragEnter(dtde: DropTargetDragEvent?) {
                    super.dragEnter(dtde)
                    dragState = true
                }

                override fun drop(event: DropTargetDropEvent) {
                    event.acceptDrop(DnDConstants.ACTION_COPY)
                    val draggedFileName = event.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                    println(draggedFileName)
                    when (draggedFileName) {
                        is List<*> -> {
                            draggedFileName.firstOrNull()?.toString()?.let {
                                val f = File(it)
                                if (f.extension in extensions) {
                                    modelFile = f
                                }
                            }
                        }
                    }
                    event.dropComplete(true)
                    dragState = false
                }

                override fun dragExit(dte: DropTargetEvent?) {
                    super.dragExit(dte)
                    dragState = false
                }
            })
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                floatingActionButton = {
                    AnimatedVisibility(
                        modelFile != null,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        ExtendedFloatingActionButton(
                            text = { Text("Convert") },
                            icon = { Icon(Icons.Default.Transform, null) },
                            onClick = {
                                runCatching {
                                    val path = modelFile!!.parent
                                    val name = modelFile!!.nameWithoutExtension
                                    println(path)
                                    println(name)
                                    Scene.fromFile(modelFile.toString())
                                        .save("$path${File.separator}$name.${outputType.extension}")
                                }
                                    .onSuccess { modelFile = null }
                            }
                        )
                    }
                },
                actions = {
                    Text(modelFile?.extension.orEmpty())
                    Text("To")
                    Text(outputType.extension)
                }
            )
        }
    ) { padding ->
        Crossfade(modelFile) { target ->
            when {
                target != null -> {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.padding(padding)
                    ) {
                        Column {
                            Text(modelFile.toString())

                            Spinner(
                                selected = outputType,
                                list = ModelOutputTypes.values().toList(),
                                onSelectionChanged = { outputType = it },
                                selectedToString = { it.extension },
                            )
                        }

                        /*WebView(
                            """
                                <html>
                                    <script type="text/javascript" src="~/Downloads/o3dv/o3dv.min.js"></script>
                                    <div class="online_3d_viewer" model="${modelFile.toString()}">
                                    </div>
                                </html>
                            """.trimIndent(),
                        )*/
                    }
                }

                else -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Button(
                            onClick = { filePicker = true }
                        ) { Text("Load File") }
                    }
                }
            }
        }

        AnimatedVisibility(
            dragState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .padding(padding)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) { Text("Drag and Drop 3D Model") }
            }
        }
    }
}

fun main() = application {
    WindowWithBar(
        onCloseRequest = ::exitApplication,
        windowTitle = "3D Model Converter"
    ) {
        App()
    }
}

enum class FileDialogMode(internal val id: Int) { Load(FileDialog.LOAD), Save(FileDialog.SAVE) }

@Composable
private fun FileDialog(
    mode: FileDialogMode,
    title: String = "Choose a file",
    parent: Frame? = null,
    block: FileDialog.() -> Unit = {},
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, title, mode.id) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory + File.separator + file)
                }
            }
        }.apply(block)
    },
    dispose = FileDialog::dispose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Spinner(
    selected: T,
    list: List<T>,
    onSelectionChanged: (myData: T) -> Unit,
    selectedToString: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) } // initial value

    OutlinedCard(
        onClick = { expanded = !expanded },
        modifier = modifier
    ) {
        ListItem(
            headlineText = { Text(text = selectedToString(selected)) },
            trailingContent = { Icon(Icons.Outlined.ArrowDropDown, null) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // delete this modifier and use .wrapContentWidth() if you would like to wrap the dropdown menu around the content
            modifier = Modifier.fillMaxWidth()
        ) {
            list.forEach { listEntry ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectionChanged(listEntry)
                    },
                    text = {
                        Text(
                            text = selectedToString(listEntry),
                            modifier = Modifier
                                //.wrapContentWidth()  //optional instad of fillMaxWidth
                                .fillMaxWidth()
                                .align(Alignment.Start)
                        )
                    },
                )
            }
        }
    }
}

enum class ModelOutputTypes(val extension: String) {
    Stl("stl"),
    ThreeMF("3mf"),
    ThreeDS("3ds"),
    Amf("amf"),
    Rvm("rvm"),
    Gltf("gltf"),
    Glb("glb"),
    Pdf("pdf"),
    Html("html"),
    Drc("drc"),
    Dae("dae"),
    Fbx("fbx"),
    Obj("obj"),
    U3d("u3d"),
    Ply("ply"),
    Usd("usd"),
}

@Composable
fun WebView(
    content: String,
    modifier: Modifier = Modifier
) {
    SwingPanel(
        factory = { JFXWebView(content) },
        modifier = modifier,
    )
}

class JFXWebView(private val content: String) : JFXPanel() {
    init {
        Platform.runLater(::initialiseJavaFXScene)
    }

    private fun initialiseJavaFXScene() {
        val webView = javafx.scene.web.WebView()
        val webEngine = webView.engine
        webEngine.loadContent(content)
        val scene = javafx.scene.Scene(webView)
        setScene(scene)
    }
}