# 提示词 ①　项目分析（建立源码地图）

> **用途**：项目有重大变化（新增模块、大改结构）时重新执行。
> **频率**：低。首次已执行完毕，产出 [PROJECT_MAP.md](../PROJECT_MAP.md) 与 [SOURCE_INDEX.md](../SOURCE_INDEX.md)。
> **注意**：本阶段**只建立认知，不写博客正文**。

---

## 提示词正文（复制以下内容给 Agent）

```text
你是一名 Android 技术导师兼技术文档作者。

我在维护一个 Android 入门自学项目，技术路线是 Java + XML View，
使用 Android SDK / AndroidX，不使用 Kotlin / Compose / MVVM。

项目根目录：D:/Code/Android

你的任务是：基于真实源码，更新 docs/android-blog/PROJECT_MAP.md 与
docs/android-blog/SOURCE_INDEX.md 两份项目分析文档。
不要编写博客正文。

═══════════════════════════════════════════
一、分析原则（强制）
═══════════════════════════════════════════

1. 必须以真实文件内容为依据，逐行读取后再下结论。
2. 禁止根据目录名、文件名或"常见 Android 项目模板"推断功能。
3. 必须区分三类信息，并在文档中分别标注：
   - 【已确认】直接从源码读到的事实
   - 【合理解释】基于源码的推断
   - 【待核实】需要运行或查官方文档才能确认的内容
4. 不修改、不重构、不格式化任何源码与构建配置。
5. 不输出密钥、签名、本机绝对路径等敏感信息。

═══════════════════════════════════════════
二、必须读取的范围
═══════════════════════════════════════════

构建层：
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml
  .gitignore
  各模块 build.gradle.kts（app / mytextapplication /
                        myactivityapplication / intermediatecontrols）

清单层：
  4 份 AndroidManifest.xml（逐条核对每个 <activity> 的注册状态，
  包括被 <!-- --> 注释掉的，以及写了但从未注册的）

源码层：
  三个 Java 模块下 src/main/java 的全部 .java 文件

资源层：
  各模块 src/main/res/ 下的 layout/*.xml、drawable/*、
  values/*.xml、xml/*.xml

═══════════════════════════════════════════
三、本项目已知事实（请核实，不要推翻也不要盲信）
═══════════════════════════════════════════

- rootProject 名 MyFirstApp，remote 为 GitHub 上的 Android_Study
- 四个模块中 :app 是 Compose + Kotlin 模板，**不属于博客范围**，
  但可作为"同一仓库并存两种范式"的对照出现
- 三个 Java 模块各自是独立的 application（各自有 launcher），
  不是 library + app 结构
- 主题为 Theme.Material3.DayNight.NoActionBar
- viewBinding 仅在 :mytextapplication 开启为 true，但需核实代码是否真的用了
- compileSdk = release(37)，minSdk = 28，targetSdk = 37，Java 11
- 大部分 Activity 含一段 EdgeToEdge + ViewCompat.setOnApplyWindowInsetsListener 样板

以上每一条都要在源码中找到出处；若与源码不符，以源码为准并明确指出差异。

═══════════════════════════════════════════
四、输出要求
═══════════════════════════════════════════

A. 更新 docs/android-blog/PROJECT_MAP.md，必须包含：
   1. 项目身份表（仓库、rootProject、分支、构建系统、包名前缀）
   2. 模块清单表：模块 / namespace / 角色 / launcher Activity /
      Activity 数 / Layout 数
   3. 技术栈实况表：每一项都要注明"来源文件"
   4. 模块依赖对比表
   5. 应用启动流程（含 Mermaid 流程图）
   6. **Activity ↔ Layout ↔ Manifest 注册状态 三方映射表**（三个模块分列），
      未注册或被注释的必须显著标注
   7. 资源与配置的引用关系（尤其 selector / shape / .9.png 的引用链）
   8. 代码约定（命名前缀、重复定义的工具类、样板代码）
   9. "真实困惑点"清单：从代码本身发现的、适合作为章节引入问题的现象
  10. 待核实清单（写成 - [ ] 形式）

B. 更新 docs/android-blog/SOURCE_INDEX.md，必须包含：
   每个源码文件一行，列为：
   文件路径 / 职责与关键内容 / 对应知识点 / 建议章节 /
   状态（✅已逐行读取 / 🔍确认存在但未细读 / ⚪未涉及）
   并单列一节「明确不存在的知识点」，列出项目中查无实据的技术点，
   防止后续写作时将它们写成"项目已实现"。

═══════════════════════════════════════════
五、完成后总结（在对话中回复，不要写进文件）
═══════════════════════════════════════════

1. 实际读取了哪些文件（按目录分组列出）
2. 哪些文件存在但尚未细读
3. 新发现了哪些可独立成章的技术主题
4. 哪些主题应合并，哪些缺乏源码依据
5. 与已有文档相比，本次有哪些需要修正的地方

本阶段不要生成任何博客正文。
```

---

## 使用说明

- 在**能访问项目工作区的环境**中执行（本仓库根目录 `D:/Code/Android`）。
  仅靠上传源码片段，无法保证覆盖率，也无法核对 Gradle 与 Manifest 的联动关系。
- 若只改了某一个模块，可以把"必须读取的范围"缩小到该模块，加快速度。
- 产出必须人工抽查：**随机挑 3 个"【已确认】"的结论，自己打开文件核对**。
  抽查不通过，说明 Agent 在推断，需要重做。
