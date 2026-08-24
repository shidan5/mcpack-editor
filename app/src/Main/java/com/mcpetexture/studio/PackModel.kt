package com.mcpetexture.studio

data class PackProject(
    val name: String = "我的材质包",
    val description: String = "由 MCPE 材质包工作室创建",
    val author: String = "",
    val targetVersion: String = "26.35",
    val packVersion: List<Int> = listOf(1, 0, 0),
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val moduleUuid: String = java.util.UUID.randomUUID().toString(),
    val files: Map<String, ByteArray> = emptyMap()
)

object McpeVersions {
    // 1.26.1 起的常用目标版本；可继续扩展。
    val all = listOf(
        "1.26.1", "1.26.2", "1.26.3", "1.26.4",
        "26.20", "26.21", "26.22", "26.30", "26.31",
        "26.32", "26.33", "26.34", "26.35", "最新版"
    )
}
