package com.mcpetexture.studio

import org.json.JSONObject
import java.util.UUID

data class ManifestCheck(
    val ok: Boolean,
    val name: String,
    val uuid: String?,
    val version: String,
    val minEngineVersion: String?,
    val warnings: List<String>
)

object ManifestTools {
    fun inspect(json: String?): ManifestCheck {
        if (json.isNullOrBlank()) {
            return ManifestCheck(false, "", null, "", null, listOf("缺少 manifest.json"))
        }
        return try {
            val root = JSONObject(json)
            val header = root.optJSONObject("header")
            val modules = root.optJSONArray("modules")
            val warnings = mutableListOf<String>()
            if (header == null) warnings += "缺少 header"
            if (modules == null || modules.length() == 0) warnings += "缺少 modules"

            val name = header?.optString("name", "") ?: ""
            val uuid = header?.optString("uuid", null)
            val version = header?.optJSONArray("version")?.let { a ->
                (0 until a.length()).joinToString(".") { a.optInt(it) }
            } ?: ""
            val engine = header?.optJSONArray("min_engine_version")?.let { a ->
                (0 until a.length()).joinToString(".") { a.optInt(it) }
            }

            if (name.isBlank()) warnings += "header.name 为空"
            if (uuid.isNullOrBlank()) warnings += "header.uuid 为空或缺失"
            if (version.isBlank()) warnings += "header.version 为空"
            ManifestCheck(warnings.isEmpty(), name, uuid, version, engine, warnings)
        } catch (e: Exception) {
            ManifestCheck(false, "", null, "", null, listOf("manifest.json JSON 格式错误：${e.message}"))
        }
    }

    fun repair(project: PackProject): String {
        return PackBuilder.manifest(
            project.copy(
                uuid = runCatching { UUID.fromString(project.uuid) }.getOrElse { UUID.randomUUID() }.toString(),
                moduleUuid = runCatching { UUID.fromString(project.moduleUuid) }.getOrElse { UUID.randomUUID() }.toString()
            )
        )
    }
}
