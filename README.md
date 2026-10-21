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
| `:datasharingapplication` | Java | `ShoppingChannelActivity` | 数据存储：SharedPreferences、SQLite 建库与 `SQLiteOpenHelper` 增删改查、**Room 图书 CRUD**、Application 全局数据、内部 / 外部私有 / 外部公共三类目录读写与图片落盘；**商城 demo（商品列表 → 详情 → 购物车）**：公共顶部栏组件、`ListView + ViewHolder`、购物车增删改查 |

> 各模块 `applicationId` 互不相同（均为 `com.zzz.*`），因此可以**同时安装到一台设备上**，方便横向对照。
>
> `:datasharingapplication` 的页面最多，launcher 目前挂在商城列表页 `ShoppingChannelActivity`，其余 Activity（`BookEntryActivity`、SharedPreferences / SQLite / 存储相关页面）在清单里以注释形式保留 —— 想看哪个就取消对应注释或临时改 launcher。

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

本模块把 Android 侧最常用的数据存放方式都走了一遍：SharedPreferences → 手写 SQLite → `SQLiteOpenHelper` → Room，外加 Application 内存态与文件存储。

**5.1 SharedPreferences 与 SQLite**

| 文件 | 演示要点 |
|---|---|
| `SharedPreferencesActivity` | `getSharedPreferences("config", MODE_PRIVATE)`；进页面 `reload()` 把已存数据回填到控件，点确认用 `Editor` + `apply()` 保存 |
| `SqliteActivity` | 直接用 `openOrCreateDatabase` / `deleteDatabase`，并把"应用文件目录"与"数据库实际路径"打印到界面 |
| `SqliteHelperActivity` | `SQLiteOpenHelper` 子类 + 增 / 删 / 改 / 查 / **批量插入**五个按钮，含非空校验、`NumberFormatException` 捕获、结果格式化输出 |
| `database/UserDBHelper` | 单例 `getInstance`、读写连接分离（`openReadLink` / `openWriteLink`）、`closeLink`、建表 SQL、`insert` / `delete` / `update` / `queryById` / `queryAll`、`Cursor` → 对象映射；另有 `insertBatchWithTransaction` 演示**事务 + 失败回滚**，`onUpgrade` 里留了加字段的迁移写法 |
| `pojo/User` | 数据实体：`id` / `name` / `age` / `height(long)` / `weight(float)` / `married(boolean)` |

**5.2 Room —— 图书录入 CRUD**

| 文件 | 演示要点 |
|---|---|
| `pojo/BookInfo` | `@Entity(tableName = "book_info")` + `@PrimaryKey(autoGenerate = true)`；书名 / 作者 / 出版社 / 价格(double) / 页数(int) |
| `dao/BookDao` | 四类注解全用上：`@Insert`（可变长参数 / 单条返回 rowId / `List` 批量）、`@Update`、`@Delete`、`@Query`（全量、`LIKE '%' \|\| :keyword \|\| '%'` 模糊查询、按 id 查、`DELETE FROM` 清空） |
| `database/BookDataBase` | `@Database(entities = {BookInfo.class}, version = 1, exportSchema = true)`，只暴露 `bookDao()`；`BookDataBase_Impl` 由注解处理器生成，不手写 |
| `application/BookApplication` | `Room.databaseBuilder(...).allowMainThreadQueries().build()` 单例持库；`allowMainThreadQueries()` 仅为演示方便，正式项目应放子线程 |
| `BookEntryActivity` | 表单校验 → 单线程 `Executor` 落库 → `runOnUiThread` 回主线程刷新；`SimpleAdapter` + `Map` 承载行数据；另有清空输入、一键删除全部、已录入计数 |
| `schemas/` | 注解处理器导出的建表 DDL 快照（`BookDataBase/1.json`），版本间 diff 与迁移测试的依据，**必须纳入版本控制**（不是构建产物） |

> 本模块是纯 Java 模块，Room 注解处理器用 `annotationProcessor` 接入，**不需要 kapt / KSP**（那两个是 Kotlin 模块的方案）；导出位置写在 `defaultConfig.javaCompileOptions.annotationProcessorOptions` 里。

**5.3 Application 全局数据**

| 文件 | 演示要点 |
|---|---|
| `MyApplication` | `Application` 子类持有 `List<UserInfo>`，进程存活期间全局可读；含 `onCreate` / `onTerminate` / `onLowMemory` / `onTrimMemory` 四个回调的日志与"为什么 `onTerminate` 不可靠"的说明 |
| `ApplicationActivity` | 表单提交后 `MyApplication.getInstance().addUserInfo(...)`，数据不随 Activity 销毁而丢失 |
| `ShowActivity` | 读取全局列表并逐条拼接展示 |

**5.4 文件与图片存储**

| 文件 | 演示要点 |
|---|---|
| `ExternalStorageActivity` | 一次演示三类目录：内部 `getFilesDir()`、外部私有 `getExternalFilesDir(null)`、外部公共 `getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)`；Android 6~9 动态申请存储权限，Android 10+ 走分区存储，`requestLegacyExternalStorage` 留给旧系统 |
| `ImageStorageActivity` | `Bitmap.compress(PNG, 100, fos)` 落盘 + `BitmapFactory.decodeFile` 读回；重点处理 `getExternalFilesDir` 返回 `null`（存储卡未挂载）的情况 |
| `util/FileUtil` | 文本与图片读写封装：`saveText` / `openText` / `saveImage` / `openImage` |
| `util/ToastUtil` | 统一 Toast 封装 |

**生命周期与数据库连接的配合**：`onStart()` 打开连接、`onStop()` 关闭连接（而不是在 `onCreate` 里开、`onDestroy` 里关），这样页面切到后台就释放数据库资源。

**5.5 商城 demo —— 商品列表 → 详情 → 购物车**

本模块的 launcher，把前面几节串成一条完整业务链路：建表 → 列表渲染 → Intent 传参 → 详情回显 → 购物车增删改查 → 角标同步。

| 文件 | 演示要点 |
|---|---|
| `layout/layout_top_bar.xml` | **公共顶部栏组件**：返回键 / 标题 / 购物车图标 + 角标。其他页面一行 `<include layout="@layout/layout_top_bar"/>` 引入，**组件内的 id 在宿主里照样能 `findViewById`**；标题 `tv_title` 由各页面 `setText` 各自决定 |
| `ShoppingChannelActivity` | 列表页：`ScrollView + GridLayout` 两列商品卡；`queryAllGoods()` 逐条 inflate `item_goods`；点图片 / 名称 / 描述都进详情页（三处行为相同，**共用一个 `OnClickListener`**，不复制三份 Intent） |
| `ShoppingGoodDetailActivity` | 详情页：`getIntent().getIntExtra(EXTRA_GOODS_ID, -1)` 取参数 → `queryGoodById()` → 绑定大图 / 价格 / 名称 / 描述；`EXTRA_GOODS_ID` 抽成 public 常量，让"发"和"收"共用同一个 key |
| `ShoppingCartActivity` | 购物车页：`ListView + BaseAdapter + ViewHolder`；`+ / −` 改数量（**数量为 1 时再点减号 = 删行**）；清空与结算都走 `AlertDialog` 二次确认；空车用 `setEmptyView()` 自动显示"哎呀，购物车空空如也" |
| `layout/item_cart.xml` | 表格式行：图片 56dp / 名称 `weight=1` / 单价 60dp / 数量 72dp / 小计 68dp，**列宽必须与 `activity_shopping_cart.xml` 的表头完全一致**，否则"表"会对不齐 |
| `database/ShoppingDBHelper` | `goods_info` / `cart_info` 两张表；`queryAllCart()` 用 **LEFT JOIN** 一次带出商品图片路径（避免在适配器里逐行查库的 N+1）；另有 `updateCartQuantity` / `deleteCartInfo` / `clearCartInfo` / `queryCartTotalPrice`（`SUM(quantity*price)`） |
| `application/ShopApplication` | 启动时"模拟下载"商品图到外部私有目录再写库；是否初始化用 **`firstOpen` 标记 + `COUNT(*)` 双判据**（标志位可能被误写，数据才是权威事实） |
| `pojo/Goods`、`pojo/Cart` | 实体类；`Goods.imageResIdOf(name)` 用于图片文件读不到时按商品名反查 drawable |

**数据库连接归谁管**：`ShoppingDBHelper` 是进程级单例，读写连接由它自己**懒加载 + 自愈**（`dbRead()` / `dbWrite()` 每次先判 `isOpen()`，被关掉就重开）。业务代码**不要**调用 `closeLink()` —— 曾经在 `onResume` 里关过一次，把列表页正在用的连接一起置成 `null`，导致点"加入购物车"直接空指针闪退，Database Inspector 里也一直显示 `closed`。

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
| Room | 2.8.5（`:datasharingapplication` 以 `annotationProcessor` 接入，`schemaLocation` 指向模块内 `schemas/`） |
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
├── datasharingapplication/       # SharedPreferences / SQLite / Room / Application 全局数据 / 文件存储 / 商城 demo
│   └── schemas/                  # Room 导出的建表 DDL 快照（随代码一起提交）
├── docs/android-blog/            # 源码解读博客的写作工作区（结构分析 / 章节规划 / 草稿）
├── img/                          # 界面截图等素材
├── gradle/libs.versions.toml     # 依赖版本目录
├── build.gradle.kts
├── settings.gradle.kts           # include 了以上 5 个模块
└── README.md
```

所有模块的 Java 包名均为 `com.zzz.<模块名>`，手写代码集中在包根目录下（`utils` / `util` / `pojo` / `database` / `dao` / `application` 为辅助子包）。

---

## 🚀 如何运行

1. 用 **Android Studio** 直接 `Open` 本目录（根工程名为 `MyFirstApp`）；
2. 首次打开等待 Gradle Sync 完成（需联网下载依赖）；
3. 在顶部运行配置下拉框里**选择要看的模块**，然后点 Run。例如：
   - 想看布局与计算器 → 选 `mytextapplication`
   - 想看 Intent / Bundle → 选 `myactivityapplication`
   - 想看登录流程与控件 → 选 `intermediatecontrols`
   - 想看数据库与商城 demo → 选 `datasharingapplication`（launcher 是商城列表页 `ShoppingChannelActivity`；Room 图书页与 SharedPreferences / SQLite / 存储页面需先取消清单里的注释）
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
| 2026-10-19 | `c9098f4` | Room 图书管理、Application 全局数据、多存储目录读写与图片存储 |
| 2026-10-21 | `a0ff09f` | 商城 demo：公共顶部栏、商品列表 / 详情 / 购物车、购物车增删改查与角标同步 |

---

## ⚠️ 踩坑记录

### Material3 主题下的两条

本仓库主题是 `Theme.Material3.DayNight.NoActionBar`，这个主题下有两个非常容易踩的坑：

| 坑 | 表现 | 解法 |
|---|---|---|
| **`<Button>` 被替换成 `MaterialButton`** | 布局里写的 `android:background` 被吞掉，自定义背景图**完全不显示**（渲染成 M3 紫色填充按钮） | 改写成 `androidx.appcompat.widget.AppCompatButton`（写全限定类名可绕过替换） |
| **checkbox / radio 的 `buttonTint`** | 自定义 `android:button` 图被**整体染色成纯色块**（`SRC_IN` 只保留 alpha，图形细节被抹平） | `android:buttonTint="@null"` + `app:buttonTint="@null"`（根标签需加 `xmlns:app`） |

**通用判断法**：Material 主题下自定义图片"消失"或"变了样"，先怀疑 tint；**渲染成纯色块 = 被 `SRC_IN` 染色的典型特征**，不代表图没加载。

两处的完整原因分析与修复注释见：
- `intermediatecontrols/src/main/res/layout/activity_drawable_state.xml`（不只是文字说明，还用同类控件做了对照组）
- `intermediatecontrols/src/main/res/layout/activity_check_box.xml`

### 一个 `NullPointerException` 查了两轮

商城 demo 里连续踩了两个"代码看着没问题、一跑就闪退"的坑，都记在这里：

| 坑 | 表现 | 根因 | 排查方法 |
|---|---|---|---|
| **`setContentView` 用错布局** | 点商品进详情页立刻 `NullPointerException: ... ImageView.setImageBitmap(...) on a null object reference`，堆栈指向 `loadGoodsImage()`，**不是**创建控件的那一行 | 详情页写成了 `setContentView(R.layout.activity_shopping_cart)`（购物车页布局），于是 `iv_ProductImage` / `tv_Price` / `tv_Description` / `btn_AddToCart` 全部 `findViewById` 到 `null`。**`findViewById` 找不到 id 不报错、只返回 null**，所以要等到"用"它时才炸 | ① 先核对 `setContentView` 的文件名；② 在 `bindViews()` 里给每个 `findViewById` 加 null 检查，让错误在赋值那一刻暴露，而不是几十行之后 |
| **共享单例的连接被别处关掉** | 点"加入购物车"闪退，Database Inspector 里数据库一直显示 `closed` | `ShopApplication.refreshCartCount()` 里调了 `helper.closeLink()`，而 `closeLink()` 会把单例里的 `mRDB` / `mWDB` 一起置 `null`；Activity 里还留着一份"连接已打开"的假设 | 单例 + 可变连接 = 定时炸弹。谁负责"保证可用"，谁才有权关闭；读写方法内部先判 `isOpen()`（自愈），业务代码只"用"不"关" |

> 这两个坑还有个共同点：购物车页和详情页都 include 了同一个顶部栏，所以 `iv_back` / `tv_title` 这类 id 能正常取到 —— **这个"碰巧能取到"反而让布局用错更难被发现**。

### 构建与架构上的三条

| 坑 | 表现 | 解法 |
|---|---|---|
| **纯 Java 模块照抄 Kotlin 教程加 `kapt` / `ksp`** | 工程里没有 Kotlin 插件，`kapt(...)` 直接报 DSL 找不到方法；为此再加 Kotlin 插件属于为注解处理器引入整套 Kotlin 编译链 | 纯 Java 模块用 `annotationProcessor("androidx.room:room-compiler:x.y.z")`；`kapt` / `ksp` 只适用于 Kotlin 模块 |
| **不配 `room.schemaLocation`** | 不报错，只打一条 warning「Schema export directory is not provided…」，等到要写 `Migration` 时已无历史 DDL 可比对 | 在 `defaultConfig` 里配 `javaCompileOptions.annotationProcessorOptions.arguments["room.schemaLocation"] = "$projectDir/schemas"`，并把 `schemas/` 提交进版本库 |
| **一个 APK 只能注册一个 `Application`** | 清单 `android:name` 只有一个；另一个 `Application` 子类的 `onCreate` 永不执行，其单例只能靠 `new` 手动创建，拿到的实例并未 attach `Context` | 全局状态统一放进已注册的那个 Application，不要再写第二套单例；确有两个都要用时**用继承**（本仓库 `ShopApplication extends BookApplication`，`super.onCreate()` 里照样完成 Room 建库），而不是把两份 `onCreate` 复制一遍 |

---

## 📝 代码注释约定

仓库中的手写代码与资源文件统一遵循以下注释规范：

- **Java**：类头 Javadoc 写明「演示要点 / 对应布局 / 涉及 API」；方法 Javadoc 用 `@param` 逐个标注形参名与含义；方法体内用行内 `//` 解释每一步逻辑与调用的 API；lambda 与匿名内部类的形参也逐条说明；
- **XML**：根布局写页面结构总览，每个控件标注用途与引用关系，**关键属性在控件上方的注释块内逐条说明**；
- 现有注释尽量保留，只做补充 —— 目的是让代码本身能当讲义读。

> 注：`:datasharingapplication` 模块的 **25 个 Java 文件**已全部按上述规范补齐（类头 Javadoc + 方法 `@param` + 行内注释）；
> 布局文件里 `activity_book_entry.xml`、`item_book_list.xml`、`activity_shopping_*` 等尚未逐条注释，属于待办。

---

## 📄 说明

- 仓库内的代码以**学习演示**为目的，多处刻意做了简化（如登录密码写死在代码里、验证码直接弹窗展示、数据库表结构简单），**不可直接用于生产**；
- 部分页面在 `AndroidManifest.xml` 中的注册被注释掉了（例如两个 `MainActivity`），想看对应页面时取消注释或改一下 launcher 即可；
- 个别已知但不影响运行的小问题（如误导入 `org.w3c.dom.Text`、某个 `textSize` 误用了 `dp`、`MainActivity` 未在清单中注册）已在对应文件的注释中说明。
- `database/UserDBHelper#insertBatchWithTransaction` 在 `i == 1` 时故意执行 `10 / 0` 触发异常，用来演示事务回滚，因此该方法**当前必然返回 `false`** —— 是教学代码，不是可直接复用的接口。
- `ShowActivity` 原有的重复 import（`android.os.Bundle`、`AppCompatActivity` 各两次）已在补注释时一并清理。
- 清单注册的 `Application` 是 `ShopApplication`（商城 demo 用），它**继承** `BookApplication`，因此 Room 的建库逻辑仍然执行、`BookEntryActivity` 里的 `BookApplication.getBookDao()` 依然可用；`MyApplication` 仍然不会被执行（一个 APK 只能注册一个），它的全局数据演示目前只是代码保留，要看效果需把它并入已注册的那个 Application。
- `res/drawable/goods_1.png` ~ `goods_5.png` 五个文件的内容完全相同（md5 一致），所以列表里五个商品显示的是同一张图 —— 换素材即可，不是代码问题。
