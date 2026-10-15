# Android_Study

> 一个从零起步的 Android 入门练习仓库：**5 个互相独立的模块**，把 Android 基础知识点拆开逐个练一遍。
> 所有手写代码与资源文件均带**行级中文注释**（讲逻辑、讲调用的 API、标注每个形参的含义）。

---

## 📌 这是什么

这是一个**学习记录型仓库**，不是产品项目。特点是：

- **每个知识点一个 Activity**，命名即用途（如 `MainTextSizeActivity` 看字号单位、`MainViewMarginPaddingActivity` 看边距区别），需要看哪个效果就运行哪个页面；
- **5 个模块各自是独立 application**，各有自己的 launcher，互不依赖 —— 好处是每个模块都能单独安装、单独调试，不会互相干扰；
- **能对照的尽量做成对照组**。例如同一个"按钮点击事件"用三种写法实现（匿名内部类 / 实现接口 / lambda）并排放在一起；同一个 `scaleType` 用同一张图排开 8 行对比；
- **踩过的坑写在代码注释里**。Material3 主题下"自定义背景图不显示""勾选框变成纯色块"这两个经典坑，在对应布局文件里都有完整的原因分析与修复说明。

---

## 🗂 模块总览

| 模块 | 语言 | 入口 Activity（launcher） | 演示内容 |
|---|---|---|---|
| `:app` | Kotlin + Compose | `MainActivity` | Jetpack Compose 入门：组合函数、状态、AlertDialog、页面跳转 |
| `:mytextapplication` | Java | `CalculatorActivity` | 文本 / 字号 / 颜色、四大布局（Linear / Relative / Grid / Scroll）、边距与对齐、按钮点击的三种写法、ImageView 的 8 种 scaleType、简易计算器 |
| `:myactivityapplication` | Java | `ActStartActivity` | Activity 跳转与 `finish`、显式 / 隐式 Intent、Bundle 传值与结果回传、启动模式与 Intent Flag、读取资源、桌面快捷方式 |
| `:intermediatecontrols` | Java | `LoginMainActivity` | 登录 / 忘记密码流程、CheckBox / Switch / RadioButton、EditText 校验与焦点监听、AlertDialog、DatePicker / TimePicker、Drawable（shape / selector / 点九图） |
| `:datasharingapplication` | Java | `SqliteHelperActivity` | 数据存储：SharedPreferences 读写、SQLite 建库删库、`SQLiteOpenHelper` 封装下的增删改查 |

> 各模块 `applicationId` 互不相同（均为 `com.zzz.*`），因此可以**同时安装到一台设备上**，方便横向对照。

---

## ✨ 各模块演示内容详解

### 1. `:app` —— Jetpack Compose 入门

目前是 Compose 的最小可运行示例，保留用来和后面四个 Java 模块做"声明式 UI vs 传统 View"的对照。

- `@Composable` 函数 + `Modifier` 链式修饰
- 用 `remember { mutableStateOf(false) }` 保存状态，状态一变 UI 自动重组
- `Scaffold` + `AlertDialog` + `Button` 等 Material3 组件
- `Intent` 跳转到第二个 Compose 页面（`SecondActivity`）

对应布局资源：`res/layout/activity_second.xml`；主题在 `ui/theme/`（`Color.kt` / `Theme.kt` / `Type.kt`）。

### 2. `:mytextapplication` —— 文本与布局基础

**文本三件套**

| Activity | 演示要点 |
|---|---|
| `MainTextActivity` | 代码里 `setText(R.string.hello)` 设置文字（与布局里写 `android:text` 等价） |
| `MainTextSizeActivity` | `30px` / `30dp` / `30sp` 三种单位并排对比 —— 结论：**文字大小一律用 `sp`** |
| `MainTextColorActivity` | 文字色 / 背景色，`@color` 资源引用 vs `0xAARRGGBB` 直接写死色值 |

**四大布局**

| Activity | 演示要点 |
|---|---|
| `MainLinearLayoutActivity` | 横向与纵向的 `layout_weight` 等分（用权重的那一维必须写 `0dp`） |
| `MainRelativeLayoutActivity` | 十几个 TextView 逐个演示 `alignParent*` / `centerInParent` / `toLeftOf` / `above` / `alignLeft` 等对齐属性 |
| `MainGridLayoutActivity` | `columnCount` / `rowCount` + `layout_columnWeight`（权重 1 与 2 的宽度对比） |
| `MainScrollViewActivity` | `HorizontalScrollView` 左右滚动 + `ScrollView` 上下滚动（两者都只能有一个直接子控件） |

**视图尺寸与对齐**

- `MainViewBorderActivity`：三种定宽方式 —— `wrap_content` / `match_parent` / 写死 dp，以及**用代码改宽高**（`getLayoutParams → 改 width/height → setLayoutParams`，需 `dp2px` 换算）
- `MainViewMarginPaddingActivity`：`margin`（外边距）与 `padding`（内边距）的区别，用色块加间距直观呈现
- `MainViewGravityActivity`：`gravity`（内容对齐）与 `layout_gravity`（控件在父容器中的对齐）的差别

**按钮点击事件的四种写法**（本仓库最值得对照阅读的一组）

| Activity | 写法 |
|---|---|
| `MainButtonClickActivity` | ① 自定义 `OnClickListener` 实现类（构造参数传入目标 TextView）② Activity 实现接口 + `setOnClickListener(this)` ③ lambda 写 `setOnLongClickListener` 并 `return true` 消费事件 |
| `MainButtonStyleActivity` | ④ **布局里写 `android:onClick="doClick"`**，由反射调用 —— 方法必须 `public` + 单个 `View` 形参 + 返回 `void` |
| `MainButtonEnabledActivity` | `setEnabled(true/false)`：禁用后按钮**连点击回调都不会触发**，配合 `setTextColor` 变灰 |

**图片显示**

- `MainImageViewActivity`：用**同一张图**排开 8 种 `scaleType`（`fitCenter` / `centerCrop` / `center` / `centerInside` / `fitXY` / `fitStart` / `fitEnd` / `matrix`），另含 `ImageButton` 与 `Button` 的 `android:drawableStart` 图文混排

**案例：简易计算器**（本模块最完整的逻辑，368 行）

- `CalculatorActivity`：加减乘除、开平方、求倒数、`C`（全清）与 `CE`（清当前数）
- 三个字段驱动全部逻辑：`firstNumber` / `operator` / `newNumber`（"下一次输入是否另起一个新数"）
- 含除零、负数开方、重复小数点的边界校验与 `Error` 提示

**工具类**：`utils/Utils.dp2px()`、`utils/DateUtils.getNowTime()`

**顺便演示了跨应用隐式跳转**：本模块 launcher `CalculatorActivity` 除 `MAIN/LAUNCHER` 外，还额外声明了一个自定义 action 的 `intent-filter`：

```xml
<intent-filter>
    <action android:name="android.intent.action.ZZZ" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

它正是 `:myactivityapplication` 里 `IntentActivity`「跳转到我的页面」按钮的目标 —— 点击后会启动**另一个 App（本模块）**的页面。隐式 Intent 的匹配范围是全系统已安装应用，而不只是自己这个 App；因此这个演示需要两个模块都装在同一台设备上。

**多套资源限定符**（同一份 dimen / theme 在不同设备环境下自动择一）

```
values/           默认
values-land/      横屏
values-night/     深色模式
values-v23/       API ≥ 23（边到边：系统栏透明 + windowLightStatusBar）
values-w600dp/    最小宽度 ≥ 600dp（平板 / 折叠屏展开）
values-w1240dp/   最小宽度 ≥ 1240dp（超大屏）
```

### 3. `:myactivityapplication` —— Activity 与 Intent

| Activity | 演示要点 |
|---|---|
| `ActStartActivity` → `ActFinishActivity` | 本模块 launcher；最基础的显式跳转与 `finish()` 返回上一页（页面上还演示了"顶部返回箭头 + 底部完成按钮做同一件事"） |
| `IntentActivity` | 显式 Intent（`ComponentName` 指定组件）与三种隐式 Intent：`ACTION_DIAL` + `tel:` 打开拨号盘、`ACTION_SENDTO` + `smsto:` 打开短信、自定义 action |
| `BundleSendActivity` ↔ `BundleReceiveActivity` | `Bundle` 键值对传值；用 `registerForActivityResult` + `setResult(RESULT_OK, intent)` + `finish()` 把数据**带回**上一页 |
| `StartupmodeActivity` ↔ `StartupmodefinishActivity` | 两个结构对称的页面互跳，观察 Intent Flag 对返回栈的影响：`FLAG_ACTIVITY_REORDER_TO_FRONT` / `CLEAR_TOP` / `CLEAR_TASK`（代码里保留了可切换的实验写法与长注释说明） |
| `ReadResourceActivity` | 读 `strings.xml`（`getString`）与读 `AndroidManifest` 里的 `<meta-data>`（`PackageManager.getActivityInfo` + `GET_META_DATA`） |

**桌面快捷方式**：`ActStartActivity` 注册了 `res/xml/shortcuts.xml`，长按桌面图标可看到三个快捷入口（"启停活动" / "来回跳转" / "Zzz"），分别直达三个演示页。

### 4. `:intermediatecontrols` —— 常用控件与 Drawable

**登录 / 忘记密码**（本模块 launcher 是最贴近真实业务的界面）

- `LoginMainActivity`：手机号 + 密码 / 验证码**两种登录方式**用 `RadioGroup` 切换（切换时联动修改标签文字、输入框 hint、记住密码勾选框的显隐与按钮文字）
- 手机号正则校验 `^1[3-9]\d{9}$`、`setError` 报错、登录成功时用 `SharedPreferences` 记住手机号与密码
- `ForgetPasswordActivity`：生成随机六位数验证码 → `AlertDialog` 展示 → 确认后自动填入并把光标移到末尾；校验"两次密码一致"后用 `setResult` 把新密码回传给登录页

**基础控件**

| Activity | 演示要点 |
|---|---|
| `CheckBoxActivity` | 系统样式 vs 自定义勾选图（`selector`），两个复选框共用一个 `OnCheckedChangeListener` |
| `SwitchActivity` | `CompoundButton.OnCheckedChangeListener` 的通用签名（CheckBox / RadioButton / Switch 共用） |
| `RadioButtonActivity` | 互斥由 `RadioGroup` 提供，监听器**挂在组上**，回调只给被选中项的 id |
| `EditTextActivity` | 四种输入框外观对比：默认 / 无边框（`@null`）/ 自定义 selector 边框 / 密码框 |
| `EditTextFocusActivity` | 焦点变化监听 + **只在失焦时**校验手机号，失败时 `setError` 与 `Toast` 双重提示 |
| `EditHideActivity` | 自定义 `TextWatcher`：输入位数凑满（11 位手机号 / 6 位密码）自动收起软键盘 |
| `AlertDialogActivity` | `AlertDialog.Builder` 的标题 / 内容 / 肯定按钮 / 否定按钮与回调 |
| `DatePickerActivity` | 页面内嵌 `DatePicker`（spinner 模式）**与**弹出的 `DatePickerDialog` 两种用法；注意 `getMonth()` 是 0 基、而 `LocalDate` 的 `Month` 是 1 基 |
| `TimePickerActivity` | 内嵌 `TimePicker` 与 `TimePickerDialog`；回调只给时分，秒用成员变量跨回调传递 |

**Drawable 三种形态**

| 文件 | 演示要点 |
|---|---|
| `shape_rectangle.xml` / `shape_oval.xml` | `<shape>` 纯 XML 画图：`solid` / `stroke` / `corners` / `gradient` / `padding`，任意尺寸不糊 |
| `checkbox_selector.xml` / `edit_text.xml` / `btn_nine_selector.xml` | `<selector>` 状态图：从上到下命中第一个成立的 `state_*` 条目，所以**兜底条目必须放最后** |
| `nine_origin.9.png` + `origin.png` | 点九图（九宫格）与普通 PNG 做对照 —— 点九图拉伸不变形、边角不会糊 |

### 5. `:datasharingapplication` —— 数据存储

| 文件 | 演示要点 |
|---|---|
| `SharedPreferencesActivity` | `getSharedPreferences("config", MODE_PRIVATE)`；进页面 `reload()` 把已存数据回填到控件，点确认用 `Editor` + `apply()` 保存 |
| `SqliteActivity` | 直接用 `openOrCreateDatabase` / `deleteDatabase`，并把"应用文件目录"与"数据库实际路径"打印到界面 |
| `SqliteHelperActivity` | 项目里封装最完整的一处：`SQLiteOpenHelper` 子类 + 增 / 删 / 改 / 查四个按钮，含非空校验、`NumberFormatException` 捕获、结果格式化输出 |
| `database/UserDBHelper` | 单例 `getInstance`、读写连接分离（`openReadLink` / `openWriteLink`）、`closeLink`、建表 SQL、`insert` / `delete` / `update` / `queryById` / `queryAll`、`Cursor` → 对象映射 |
| `pojo/User` | 数据实体：`id` / `name` / `age` / `height(long)` / `weight(float)` / `married(boolean)` |

**生命周期与数据库连接的配合**：`onStart()` 打开连接、`onStop()` 关闭连接（而不是在 `onCreate` 里开、`onDestroy` 里关），这样页面切到后台就释放数据库资源。

---

## 🧰 技术栈与版本

| 项 | 版本 |
|---|---|
| Android Gradle Plugin | 9.3.2 |
| Kotlin | 2.2.10（仅 `:app` 使用） |
| compileSdk / targetSdk | `release(37)` / 37 |
| minSdk | 28（`:app` 为 26） |
| Java 兼容级别 | 11 |
| Compose BOM | 2026.02.01 |
| AppCompat | 1.6.1 |
| Material Components | 1.10.0 |
| ConstraintLayout | 2.1.4 |
| Activity KTX | 1.13.0 |
| Gradle 依赖集中管理 | `gradle/libs.versions.toml`（版本目录 / Version Catalog） |

主题统一为 `Theme.Material3.DayNight.NoActionBar`（定义在各模块 `res/values/themes.xml`，深色版在 `values-night/themes.xml`）。

---

## 📁 目录结构

```
Android_Study/
├── app/                          # Kotlin + Compose 入门（minSdk 26）
├── mytextapplication/            # 文本 / 布局 / 按钮 / 图片 / 计算器
│   └── src/main/res/             # 含 land / night / v23 / w600dp / w1240dp 多套限定符
├── myactivityapplication/        # Activity 与 Intent / Bundle / 启动模式
├── intermediatecontrols/         # 登录流程 + 常用控件 + Drawable
├── datasharingapplication/       # SharedPreferences + SQLite
├── gradle/libs.versions.toml     # 依赖版本目录
├── build.gradle.kts
├── settings.gradle.kts           # include 了以上 5 个模块
└── README.md
```

所有模块的 Java 包名均为 `com.zzz.<模块名>`，手写代码集中在包根目录下（`utils` / `pojo` / `database` / `util` 为辅助子包）。

---

## 🚀 如何运行

1. 用 **Android Studio** 直接 `Open` 本目录（根工程名为 `MyFirstApp`）；
2. 首次打开等待 Gradle Sync 完成（需联网下载依赖）；
3. 在顶部运行配置下拉框里**选择要看的模块**，然后点 Run。例如：
   - 想看布局与计算器 → 选 `mytextapplication`
   - 想看 Intent / Bundle → 选 `myactivityapplication`
   - 想看登录流程与控件 → 选 `intermediatecontrols`
   - 想看数据库 → 选 `datasharingapplication`
   - 想看 Compose → 选 `app`
4. 命令行编译（不需要连设备）：

```bash
./gradlew :mytextapplication:assembleDebug
./gradlew :myactivityapplication:assembleDebug
./gradlew :intermediatecontrols:assembleDebug
./gradlew :datasharingapplication:assembleDebug
```

> `local.properties`（内含本机 SDK 路径）未纳入版本控制，首次打开由 Android Studio 自动生成，或手动写入 `sdk.dir=...`。

---

## 🧭 学习路线（对应提交历史）

按提交顺序看，可以还原出这个仓库的学习轨迹：

| 日期 | 提交 | 内容 |
|---|---|---|
| 2026-09-14 | `a5272b0` `450e4d2` | 初始提交、Layout 起步 |
| 2026-09-20 | `78d75c1` `8606196` | 四大布局与 Button |
| 2026-09-21 | `fc48006` | 计算器与 Activity 生命周期 |
| 2026-09-28 | `12a485e` | 数据传输与桌面快捷方式 |
| 2026-10-08 | `e6271cd` | 图形 Drawable（shape / selector / 点九图） |
| 2026-10-13 | `b8cc622` | 复选框、开关按钮、单选按钮、编辑框、焦点变更监听器、文本变化监听器、提醒对话框、日期对话框 |
| 2026-10-14 | `8e282a1` | 时间选择器、日期选择对话框、Activity 注册整理 |
| 2026-10-14 | `b7a674b` | 为三个 Java 模块的手写代码与资源**补全行级注释**（104 个文件） |
| 2026-10-15 | `be3ef31` | 数据共享：SharedPreferences 与 SQLite |

---

## ⚠️ 踩坑记录（Material3 主题下最值得记的两条）

本仓库主题是 `Theme.Material3.DayNight.NoActionBar`，在这个主题下有两个非常容易踩的坑：

| 坑 | 表现 | 解法 |
|---|---|---|
| **`<Button>` 被替换成 `MaterialButton`** | 布局里写的 `android:background` 被吞掉，自定义背景图**完全不显示**（渲染成 M3 紫色填充按钮） | 改写成 `androidx.appcompat.widget.AppCompatButton`（写全限定类名可绕过替换） |
| **checkbox / radio 的 `buttonTint`** | 自定义 `android:button` 图被**整体染色成纯色块**（`SRC_IN` 只保留 alpha，图形细节被抹平） | `android:buttonTint="@null"` + `app:buttonTint="@null"`（根标签需加 `xmlns:app`） |

**通用判断法**：Material 主题下自定义图片"消失"或"变了样"，先怀疑 tint；**渲染成纯色块 = 被 `SRC_IN` 染色的典型特征**，不代表图没加载。

两处的完整原因分析与修复注释见：
- `intermediatecontrols/src/main/res/layout/activity_drawable_state.xml`（不只是文字说明，还用同类控件做了对照组）
- `intermediatecontrols/src/main/res/layout/activity_check_box.xml`

---

## 📝 代码注释约定

仓库中的手写代码与资源文件统一遵循以下注释规范：

- **Java**：类头 Javadoc 写明「演示要点 / 对应布局 / 涉及 API」；方法 Javadoc 用 `@param` 逐个标注形参名与含义；方法体内用行内 `//` 解释每一步逻辑与调用的 API；lambda 与匿名内部类的形参也逐条说明；
- **XML**：根布局写页面结构总览，每个控件标注用途与引用关系，**关键属性在控件上方的注释块内逐条说明**；
- 现有注释尽量保留，只做补充 —— 目的是让代码本身能当讲义读。

> 注：`:datasharingapplication` 模块（2026-10-15 新增）目前尚未按上述规范补全注释，属于待办。

---

## 📄 说明

- 仓库内的代码以**学习演示**为目的，多处刻意做了简化（如登录密码写死在代码里、验证码直接弹窗展示、数据库表结构简单），**不可直接用于生产**；
- 部分页面在 `AndroidManifest.xml` 中的注册被注释掉了（例如两个 `MainActivity`），想看对应页面时取消注释或改一下 launcher 即可；
- 个别已知但不影响运行的小问题（如误导入 `org.w3c.dom.Text`、某个 `textSize` 误用了 `dp`、`MainActivity` 未在清单中注册）已在对应文件的注释中说明。
