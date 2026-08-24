# MCPE Texture Studio

Android 离线 Minecraft Bedrock 材质包制作器。

## 目标
- 无账号、无登录
- 本地创建/编辑/导入/导出 `.mcpack`
- 目标版本从 1.26.1 起，可扩展到更新版本
- 支持任意资源包文件结构，不把应用限制在固定的贴图列表

## 当前工程
这是完整版架构的第一套可编译基础工程，已经包含：
- Compose UI
- 项目/资源/编辑器/设置四个工作区
- MCPE 版本选择器
- manifest.json 生成
- `.mcpack` ZIP 打包核心
- Android Storage Access Framework 接口
- 完全离线设计

## 后续正式版
1. 完整 PNG 像素编辑器
2. `.mcpack` 自动解包、识别、重打包
3. 资源路径数据库
4. 版本兼容检测
5. 子包（subpacks）
6. 动画纹理 `.mcmeta`/相关 Bedrock 配置
7. UI/字体/模型/粒子/音效资源编辑
8. 导出前自动修复 manifest/UUID/路径


## A 版：完整像素编辑器基础实现
新增 `PixelEditor.kt`：
- 16×16 默认像素画布
- 画笔
- 橡皮
- 填充
- 吸管
- 拖动连续绘制
- 预设颜色
- 画布缩放
- PNG 导出接口（由上层接 Android Bitmap 编码器）


## B 版：.mcpack 导入/导出
新增 `PackImporter.kt` 并把主界面的 SAF 操作接通：
- OpenDocument 导入 `.mcpack/.zip`
- 自动解压全部文件到内存项目
- 读取 `manifest.json`
- 检查 `pack_icon.png`
- 忽略路径穿越（..）
- CreateDocument 导出标准 ZIP 并保存为 `.mcpack`
- 全程本地处理，不需要账号或服务器


## C 版：A+B 整合
新增：
- `ManifestTools.kt`：manifest 检查、基本修复
- `PngCodec.kt`：Android Bitmap ⇄ PNG
- `VersionDatabase.kt`：离线 Bedrock 目标版本数据库
- 导入、编辑、导出可以共用同一个本地项目模型

### 正式版设计原则
- 不要求 Microsoft/Xbox 登录
- 不依赖云端账号
- 文件通过 Android Storage Access Framework 管理
- 导入时保留未知资源文件，避免只支持“预设贴图”
- 导出前检查 manifest、UUID、版本与资源路径


## 3：一键交给 Minecraft
增加 Android Intent 流程：
- 选择已有 `.mcpack`
- 通过 `ACTION_VIEW` + MIME type 交给系统
- Android 会选择 Minecraft（如果已安装且系统允许处理该文件）
- 不需要 Microsoft 登录到本制作器
- 制作器不直接修改 Minecraft 私有目录，避免依赖 root 或特殊权限
- 对于系统无法识别 `.mcpack` 的设备，提示用户先安装 Minecraft 或使用系统“打开方式/分享”
