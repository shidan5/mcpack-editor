package com.mcpetexture.studio

data class BedrockVersion(
    val label: String,
    val engine: List<Int>,
    val notes: String = ""
)

object VersionDatabase {
    // 可离线扩展；“最新版”在没有网络时使用应用内最后已知版本。
    val versions = listOf(
        BedrockVersion("1.26.1", listOf(1,26,1)),
        BedrockVersion("1.26.2", listOf(1,26,2)),
        BedrockVersion("1.26.3", listOf(1,26,3)),
        BedrockVersion("1.26.4", listOf(1,26,4)),
        BedrockVersion("26.20", listOf(26,20)),
        BedrockVersion("26.21", listOf(26,21)),
        BedrockVersion("26.22", listOf(26,22)),
        BedrockVersion("26.30", listOf(26,30)),
        BedrockVersion("26.31", listOf(26,31)),
        BedrockVersion("26.32", listOf(26,32)),
        BedrockVersion("26.33", listOf(26,33)),
        BedrockVersion("26.34", listOf(26,34)),
        BedrockVersion("26.35", listOf(26,35))
    )

    val latestKnown: BedrockVersion get() = versions.last()
}
