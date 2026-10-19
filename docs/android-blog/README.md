# Android 入门自学技术博客

> 一个基于**真实源码**的 Android 入门学习记录。
> 仓库：<https://github.com/2002312bugisme/Android_Study>
> 技术路线：**Java + XML View + Android SDK / AndroidX**（不含 Kotlin、Compose、MVVM）

---

## 这套博客和普通教程的区别

| 普通教程 | 这套博客 |
|---|---|
| 用"标准示例"讲通用知识 | 每一段代码都能在你的仓库里 `Ctrl+F` 找到 |
| 只讲"怎么写" | 讲"为什么这样写""运行时发生了什么""为什么另一种写法不行" |
| 声称实验结果 | 标注"已运行验证"或"仅静态分析" |
| 一次性生成整套 | 一章一 commit，可回溯、可增量维护 |

**三条底线**：

1. **源码是事实依据** —— 所有关于本项目的描述都能追溯到具体文件。
2. **官方文档是技术依据** —— 涉及框架内部行为，核对与 `targetSdk 37` 相适配的官方文档。
3. **博客是对两者的解释** —— 不得为了行文流畅而编造类、方法、功能或实验结果。

---

## 目录导航

| 文件 | 作用 |
|---|---|
| [PROJECT_MAP.md](PROJECT_MAP.md) | 项目真实结构、技术栈实况、Activity↔Layout 映射、引用关系 |
| [SOURCE_INDEX.md](SOURCE_INDEX.md) | 源码文件 ↔ 知识点映射，写作时用来定位示例 |
| [LEARNING_ROADMAP.md](LEARNING_ROADMAP.md) | 14 章整体规划、前置关系、篇幅预算、素材密度评估 |
| [prompts/](prompts/) | 四个阶段的提示词（分析 / 规划 / 写作 / 审查） |
| [tools/count_chars.py](tools/count_chars.py) | 字数统计脚本，用于验收篇幅 |
| [chapters/](chapters/) | 每章一份独立 Markdown |
| [assets/](assets/) | 截图等辅助素材（流程图优先用 Mermaid，GitHub 原生渲染） |

---

## 阅读顺序

```
第 1 部分  项目与运行机制   01 → 02
第 2 部分  XML View 界面    03 → 04 → 05 → 06 → 07 → 08
第 3 部分  交互与数据传递   09 → 10 → 11 → 12 → 13
第 4 部分  综合实践         14
```

章节之间用相对路径互链，例如 `[第 03 章](03-xml-layout-and-view-tree.md)`。

---

## 篇幅口径（全项目统一，不得中途更换）

**目标：每章 6000 ～ 7000 字。**

"字数"在这里有歧义，所以统一定义三个口径，写作与验收都以此为准：

| 口径 | 定义 | 用途 |
|---|---|---|
| **A. 汉字数** | 仅统计 CJK 汉字（`\u4e00-\u9fff`、`\u3400-\u4dbf`） | 衡量"中文写作量" |
| **B. 正文可见字符** ⭐ | 汉字 + 中英文可见字符，**排除**代码围栏内容、行内代码、Markdown 标记、空白 | **主验收口径** |
| **C. 全文可见字符** | 在 B 的基础上**包含**真实代码块 | 衡量阅读量 |

- 标题、表格、列表中的可见文字**计入**。
- 链接只计锚文本，不计 URL。
- **不要相信 Agent 自报的字数**，一律用 `tools/count_chars.py` 复核：

```bash
python docs/android-blog/tools/count_chars.py docs/android-blog/chapters/03-xml-layout-and-view-tree.md
```

> **重要提醒**：篇幅是"结果"，不是"目标"。如果某章素材只够写 4000 字，
> 正确做法是**合并章节或降低该章篇幅预期**，而不是重复概念把它灌到 7000 字。
> 详见 [LEARNING_ROADMAP.md](LEARNING_ROADMAP.md) 各章的"素材密度"列。

---

## 章节状态

图例：⬜ 待写 ｜ 🟨 草稿 ｜ 🟦 已自检 ｜ ✅ 已审查

| 章 | 标题 | 状态 |
|---|---|---|
| 01 | 项目结构、Gradle 构建与多模块 | ⬜ |
| 02 | AndroidManifest 与应用启动流程 | ⬜ |
| 03 | XML 布局与 View 树 | ⬜ |
| 04 | 尺寸单位、padding/margin/gravity 与 LayoutParams | ⬜ |
| 05 | TextView、颜色与资源引用机制 | ⬜ |
| 06 | Button 与四种点击事件写法 | ⬜ |
| 07 | Drawable：shape / selector / 九宫格 | 🟨 |
| 08 | ImageView、scaleType 与图片资源 | ⬜ |
| 09 | CheckBox / Switch / RadioButton 与 CompoundButton | ⬜ |
| 10 | EditText：焦点、校验、TextWatcher 与软键盘 | ⬜ |
| 11 | Intent：显式与隐式跳转 | ⬜ |
| 12 | Bundle 传值与 Activity Result API | ⬜ |
| 13 | 生命周期、返回栈与启动模式 | ⬜ |
| 14 | 综合案例：简易计算器 | ⬜ |

每完成一章，同时更新本表。

---

## 仓库维护约定

1. **一章一 commit**，message 形如 `docs(blog): 第 03 章 XML 布局与 View 树`。
2. 每章提交前 `git status` / `git diff --stat` 确认**只动了预期的 Markdown**，不碰源码与构建配置。
3. 博客与源码同仓库（`docs/`），不单独开仓库 —— 保证"代码-文章"永远同步。
4. **不要把 `build/`、`.gradle/`、`.workbuddy/` 作为写作素材或提交内容**。
5. 涉及源码运行结果时，优先实机运行（AVD 内存上限 2048 MB）；不能运行就标注"未运行验证"。
6. 文章中不得出现签名文件、API 密钥、本机绝对路径等敏感信息。
7. 所有 Git 写操作由本人执行，AI 仅做只读核查与命令建议。

---

## 怎么用这套流程（日常操作）

```text
新开一章      →  用 prompts/03-chapter-writing.md，指定章节号
写完自检      →  python tools/count_chars.py <该章文件>
独立审查      →  用 prompts/04-chapter-review.md（重新读源码，不是润色）
修复问题      →  明确说"按报告修复第 X 章"，Agent 才动文件
```

**不要一次让 Agent 生成多章。** 一章一章来，前一章的风格和深度确认了，再进入下一章。
