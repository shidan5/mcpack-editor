package com.mcpetexture.studio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream


private fun openMcpackWithMinecraft(context: android.content.Context, uri: Uri): String {
    return try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/octet-stream")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
        "已尝试交给 Minecraft 打开 .mcpack"
    } catch (_: ActivityNotFoundException) {
        "没有找到可处理 .mcpack 的应用；请先安装 Minecraft"
    } catch (e: Exception) {
        "打开失败：${e.message ?: "未知错误"}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioApp() {
    var project by remember { mutableStateOf(PackProject()) }
    var tab by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var showVersions by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<ByteArray?>(null) }
    val context = LocalContext.current
    val installPack = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            message = openMcpackWithMinecraft(context, uri)
        }
    }

    val importPack = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val imported = PackImporter.import(LocalContext.current, uri)
                project = project.copy(files = imported.files)
                message = "导入完成：${imported.files.size} 个文件" +
                    if (imported.warnings.isNotEmpty()) "；${imported.warnings.joinToString("；")}" else ""
            } catch (e: Exception) {
                message = "导入失败：${e.message ?: "未知错误"}"
            }
        }
    }

    val exportPack = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = pendingExport
                requireNotNull(bytes)
                LocalContext.current.contentResolver.openOutputStream(uri).use { out ->
                    requireNotNull(out)
                    out.write(bytes)
                }
                message = "已导出 .mcpack"
            } catch (e: Exception) {
                message = "导出失败：${e.message ?: "未知错误"}"
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MCPE 材质包工作室") },
                    actions = {
                        IconButton(onClick = { importPack.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) }) {
                            Icon(Icons.Default.FolderOpen, "导入")
                        }
                        IconButton(onClick = {
                            pendingExport = PackExporter.toMcpack(project)
                        exportPack.launch("${project.name}.mcpack")
                        }) {
                            Icon(Icons.Default.FileDownload, "导出")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf("项目", "资源", "编辑器", "设置").forEachIndexed { i, title ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = {
                                Icon(
                                    when (i) {
                                        0 -> Icons.Default.Home
                                        1 -> Icons.Default.Folder
                                        2 -> Icons.Default.Edit
                                        else -> Icons.Default.Settings
                                    }, title
                                )
                            },
                            label = { Text(title) }
                        )
                    }
                }
            }
        ) { pad ->
            LazyColumn(
                modifier = Modifier.padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    when (tab) {
                        0 -> ProjectScreen(project, { project = it }, { showVersions = true }, { pendingExport = PackExporter.toMcpack(project); exportPack.launch("${project.name}.mcpack") }, { installPack.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) })
                        1 -> ResourceScreen(project)
                        2 -> EditorScreen()
                        else -> SettingsScreen()
                    }
                }
                if (message.isNotBlank()) item { Text(message, style = MaterialTheme.typography.bodySmall) }
            }
        }

        if (showVersions) {
            AlertDialog(
                onDismissRequest = { showVersions = false },
                title = { Text("目标 MCPE 版本") },
                text = {
                    Column {
                        McpeVersions.all.forEach { v ->
                            TextButton(onClick = {
                                project = project.copy(targetVersion = v)
                                showVersions = false
                            }, modifier = Modifier.fillMaxWidth()) { Text(v) }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun ProjectScreen(
    p: PackProject,
    onChange: (PackProject) -> Unit,
    pickVersion: () -> Unit,
    export: () -> Unit,
    install: () -> Unit
) {
    Text("项目", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(p.name, { onChange(p.copy(name = it)) }, label = { Text("材质包名称") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(p.description, { onChange(p.copy(description = it)) }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(p.author, { onChange(p.copy(author = it)) }, label = { Text("作者（可选）") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = pickVersion, modifier = Modifier.fillMaxWidth()) {
        Text("目标版本：${p.targetVersion}")
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = export, modifier = Modifier.weight(1f)) {
            Text("导出 .mcpack")
        }
        Button(onClick = install, modifier = Modifier.weight(1f)) {
            Text("安装到 Minecraft")
        }
    }
    Text("UUID：${p.uuid}", style = MaterialTheme.typography.bodySmall)
    Text("无需登录、无需服务器；项目数据可完全在本机处理。")
}

@Composable
fun ResourceScreen(p: PackProject) {
    Text("资源浏览器", style = MaterialTheme.typography.headlineSmall)
    val groups = listOf(
        "textures/blocks" to "方块纹理",
        "textures/items" to "物品纹理",
        "textures/entity" to "生物/实体",
        "textures/gui" to "GUI",
        "textures/environment" to "环境",
        "textures/particle" to "粒子",
        "sounds" to "音效",
        "texts" to "语言",
        "font" to "字体",
        "models" to "模型",
        "render_controllers" to "渲染控制器",
        "subpacks" to "子包"
    )
    groups.forEach { (path, name) ->
        Card(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(name) },
                supportingContent = { Text(path) },
                leadingContent = { Icon(Icons.Default.Folder, null) }
            )
        }
    }
}

@Composable
fun EditorScreen() {
    Text("像素编辑器", style = MaterialTheme.typography.headlineSmall)
    Text("支持 16×16、32×32、64×64、128×128、256×256、512×512 以及自定义尺寸。")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text("画笔") })
        AssistChip(onClick = {}, label = { Text("橡皮") })
        AssistChip(onClick = {}, label = { Text("吸管") })
        AssistChip(onClick = {}, label = { Text("填充") })
    }
    Text("正式版这里将加入缩放像素画布、图层、撤销/重做、镜像、旋转、调色板和 PNG 导入。")
}

@Composable
fun SettingsScreen() {
    Text("设置", style = MaterialTheme.typography.headlineSmall)
    ListItem(
        headlineContent = { Text("离线模式") },
        supportingContent = { Text("不要求 Microsoft/Xbox 登录") },
        trailingContent = { Switch(checked = true, onCheckedChange = {}) }
    )
    ListItem(
        headlineContent = { Text("导出前校验") },
        supportingContent = { Text("检查 manifest、UUID、PNG 和路径") },
        trailingContent = { Switch(checked = true, onCheckedChange = {}) }
    )
}