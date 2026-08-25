package com.mcpetexture.studio

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

data class ImportedPack(
    val manifest: String?,
    val files: Map<String, ByteArray>,
    val warnings: List<String>
)

object PackImporter {
    /**
     * Reads .mcpack/.zip through Android's Storage Access Framework.
     * No server or login is required.
     */
    fun import(context: Context, uri: Uri): ImportedPack {
        val files = LinkedHashMap<String, ByteArray>()
        val warnings = mutableListOf<String>()

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法打开文件" }
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.isDirectory) continue
                    val path = entry.name.replace("\\", "/").trimStart('/')
                    if (path.contains("..")) {
                        warnings += "已忽略可疑路径：$path"
                        continue
                    }
                    val out = ByteArrayOutputStream()
                    zip.copyTo(out)
                    files[path] = out.toByteArray()
                }
            }
        }

        val manifest = files["manifest.json"]?.toString(Charsets.UTF_8)
        if (manifest == null) warnings += "缺少 manifest.json"
        if (!files.keys.any { it.equals("pack_icon.png", true) }) {
            warnings += "没有 pack_icon.png（可选）"
        }
        return ImportedPack(manifest, files, warnings)
    }
}

object PackExporter {
    fun toMcpack(project: PackProject): ByteArray = PackBuilder.build(project)
}
