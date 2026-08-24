package com.mcpetexture.studio

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PackBuilder {
    fun manifest(project: PackProject): String {
        val v = project.packVersion.joinToString(", ")
        val engine = project.targetVersion.removePrefix("最新版").ifBlank { "26.35" }
        return """
        {
          "format_version": 2,
          "header": {
            "description": ${json(project.description)},
            "name": ${json(project.name)},
            "uuid": "${project.uuid}",
            "version": [$v],
            "min_engine_version": [${engine.split(".").joinToString(", ")}]
          },
          "modules": [
            {
              "description": ${json(project.description)},
              "type": "resources",
              "uuid": "${project.moduleUuid}",
              "version": [$v]
            }
          ],
          "metadata": {
            "authors": [${json(project.author)}],
            "generated_with": {
              "mcpe_texture_studio": ["1.0.0"]
            }
          }
        }
        """.trimIndent()
    }

    fun build(project: PackProject): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            val entries = LinkedHashMap<String, ByteArray>()
            entries["manifest.json"] = manifest(project).toByteArray(Charsets.UTF_8)
            entries.putAll(project.files)
            for ((path, data) in entries) {
                val clean = path.trimStart('/').replace("\\", "/")
                zip.putNextEntry(ZipEntry(clean))
                zip.write(data)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun json(s: String): String =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
