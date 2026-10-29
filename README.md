# Android_Study

> 一个从零起步的 Android 入门练习仓库：**10 个互相独立的模块**，把 Android 基础知识点拆开逐个练一遍。
> 所有手写代码与资源文件均带**行级中文注释**（讲逻辑、讲调用的 API、标注每个形参的含义）。

---

## 📌 这是什么

这是一个**学习记录型仓库**，不是产品项目。特点是：

- **每个知识点一个 Activity**，命名即用途（如 `MainTextSizeActivity` 看字号单位、`MainViewMarginPaddingActivity` 看边距区别），需要看哪个效果就运行哪个页面；
- **10 个模块各自是独立 application**，各有自己的 launcher，互不依赖 —— 好处是每个模块都能单独安装、单独调试，不会互相干扰；
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
| `:server_application` | Java | `MainActivity` | **ContentProvider 服务端**：SQLite `user_info` 表 + 自定义 `UserInfoContentProvider`；`UriMatcher` 区分整表 `.../user` 与单行 `.../user/#`，增删改后 `notifyChange` 广播变更 |
| `:client_application` | Java | `ProviderMmsActivity` | **ContentProvider 客户端 + 权限专题**：`ContentResolver` 跨应用读写服务端数据、懒汉式 / 饿汉式动态权限对照、联系人增删查（`ContentProviderOperation` + `applyBatch`）、`ContentObserver` 监听短信、MediaStore 选图 + FileProvider 分享彩信与安装 APK |
| `:advancedcontrolsapplication` | Java | `ListViewActivity` | **高级控件（AdapterView 家族）**：Spinner 的 dropdown / dialog 两种展开形态、SimpleAdapter 与自建 BaseAdapter 对照、ListView 的 divider / dividerHeight / listSelector 运行期切换、列表项焦点抢占（`descendantFocusability`）、GridView 四种 `stretchMode`、**ViewPager + PagerAdapter 轮播图（无限翻页 + PagerTabStrip）**、**Fragment 静态/动态加载与 FragmentPagerAdapter**、**两版启动引导页** |
| `:broadcastapplication` | Java | `SystemNetworkActivity` | **广播与页面状态**：标准广播 / 有序广播的发送与拦截、静态注册（显式 `ComponentName` 绕开 8.0 隐式广播限制）、动态注册、`ACTION_TIME_TICK` 分钟广播、网络变更广播与 `getSubtype` 数字映射、`AlarmManager` + `PendingIntent` 延迟广播、**全生命周期时间线观察**、横竖屏 `configChanges` 与方向锁定、**画中画（Picture in Picture）** |
| `:serviceapplication` | Java | `MessengerActivity` | **四大组件之 Service**：`startService` / `bindService` 两条生命周期的实测对照、`onStartCommand` 的三个参数与三种返回值、扩展 Binder（同进程）/ Messenger（`android:process=":remote"` 跨进程）/ 前台服务 + `NotificationChannel`、**启动与绑定组合时的销毁时机**、IntentService（API 30 已废弃）单线程排队验证、隐式启动被禁与现代修复方案、双服务互拉保活及其失效原因 |

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

### 6. `:server_application` —— ContentProvider 服务端

这个模块没有交互界面，作用是把自己 SQLite 里的数据通过 ContentProvider 暴露给 `:client_application` 访问。

| 文件 | 演示要点 |
|---|---|
| `database/UserDBHelper` | `user_info` 表（主键列叫 `id`，不是 `_id`），建表与增删改查封装 |
| `provider/UserInfoContentProvider` | 继承 `ContentProvider` 实现 6 个抽象方法；`UriMatcher` 注册 `user`（整表）与 `user/#`（单行）两种匹配，`#` 表示数字 id |
| `AndroidManifest.xml` | `android:exported="true"` 才能被别的应用访问；`android:authorities` 全局唯一，且必须与客户端约定一致 |

**authority 一共出现在 4 个地方**，改包名或改 authority 时必须同步：① 服务端清单 `android:authorities` ② provider 里的 `AUTHORITY` 常量（`UriMatcher` 用）③ 客户端契约类 `UserInfoContent.AUTHORITIES` ④ 客户端清单 `<queries><provider android:authorities>`。只改清单不改常量，请求能进 provider 但 `UriMatcher` 匹配不到，报「未知 Uri」。

### 7. `:client_application` —— ContentProvider 客户端 + 权限专题

launcher 目前挂在 `ProviderMmsActivity`，其余页面在清单里以 `exported="false"` 保留，可加按钮跳转或临时改 launcher。

| Activity | 演示要点 |
|---|---|
| `ContentWriteActivity` | 通过 `ContentResolver` 跨应用 insert / query / update / delete；`UserInfoContent` 契约类统一 authority 与列名；Android 11+ 需用 `<queries>` 声明包可见性，否则看不到服务端 provider |
| `PermissionLazyActivity` / `PermissionHungryActivity` | 动态权限两种时机对照：**懒汉式**（点按钮才申请）/ **饿汉式**（进页面就申请）。饿汉式一次申请 4 个权限会按权限组连弹 2 个框，但只回调一次 |
| `util/PermissionUtil` | `checkPermission`（检查 + 必要时申请）、`checkGrant`（判读 `grantResults`）、`hasPermission`（纯检查不申请） |
| `ContactAddActivity` | `ContentProviderOperation` + `applyBatch` 批量写联系人：先插 `RawContacts` 拿 id，再用 `withValueBackReference` 关联到 `Data` 表的姓名 / 电话 / 邮箱三行；另一条路径演示按 `raw_contact_id` 查回并展示 |
| `MonitorSmsActivity` | `registerContentObserver(content://sms, true, observer)` 监听收件箱；`onChange` 拿到的是**单行 uri**，直接 query 就能读到新短信 |
| `ProviderMmsActivity` | MediaStore 查图片 → `GridLayout` 三列网格 → 点击多选（`LinkedHashSet` 去重保序）→ 「发送」按钮走 `ACTION_SEND` / `ACTION_SEND_MULTIPLE` |
| `ProviderAPKActivity` | 读取 Download 下的 apk → `FileProvider.getUriForFile` 转 `content://` → `ACTION_VIEW` + APK MIME 调起系统安装器 |
| `res/xml/file_paths.xml` | FileProvider 的可访问路径白名单，决定哪些目录的文件能被翻译成 `content://` Uri |

**两个 FileProvider 高频坑**：① 清单里 `android:authorities` 若误写成 `@strings/`（多一个 s），**编译不报错**但 authority 解析失败、provider 注册成包名，运行时 `getUriForFile` 抛 `Couldn't find meta-data`；② `file_paths.xml` 只配了 `Download` 子目录时，根目录的图片会被 `FileUtil.checkFileUri` 全部过滤掉，列表直接空白。
### 8. `:advancedcontrolsapplication` —— 高级控件、翻页与碎片

`Spinner` / `ListView` / `GridView` 都是 `AdapterView` 的子类（`ListView`、`GridView` 又同为 `AbsListView` 的兄弟），所以「数据靠 Adapter 喂、点击靠 `OnItemClickListener` 收」是同一套。本模块按「数据源 → 适配器 → 行样式 → 焦点 → 排版」的顺序把它们拆开看。

**8.1 三种适配器，一条演进线**

| 适配器 | 数据集 | 行布局 | 代价 / 限制 |
|---|---|---|---|
| `ArrayAdapter` | `List<T>` 或 `string-array` | 只能是一个 `TextView` | 一行字，做不了图文 |
| `SimpleAdapter` | `List<Map<String, Object>>` | 任意，但靠 `from` / `to` 两个数组**逐位对齐** | 静态格子；**key 写错不报错，那一格静默空白** |
| 自建 `BaseAdapter` | 任意实体类（如 `List<Planet>`） | 任意，`getView` 里自己填 | 什么都能做，代价是四个方法都得自己写 |

对应页面：`SpinnerDropdownActivity` / `SimpleAdapterActivity` / `BaseAdapterActivity`。

**8.2 `BaseAdapter` 的四件套**

| 方法 | 职责 | 本仓库的写法要点 |
|---|---|---|
| `getCount()` | 行数，返回 0 列表空白 | 数据为 null 时返回 0，而不是抛 NPE |
| `getItem(int)` | 取数据 | 返回值收窄成 `Planet`（协变返回类型），点击回调里不必强转 |
| `getItemId(int)` | 行 id | 直接 return position，够用 |
| `getView(...)` | 造/复用行视图 | `convertView == null` 才 inflate；ViewHolder 用 `setTag` 挂在行上；**无论新旧行都要整体重填数据** |

`getView` 的三个固定动作：拿/复用行视图 → 按 position 取数据 → 塞进控件。`inflate(layout, parent, false)` 第三个参数必须 `false`，否则 `IllegalStateException`。

**8.3 ListView 的两类样式**

| 页面 | 演示要点 |
|---|---|
| `ListViewActivity` | 两个 CheckBox 切换 `divider` / `dividerHeight` 与 `listSelector`。**「未选中 = 默认状态」用快照还原**：`onCreate` 里先 `getDivider()` / `getDividerHeight()` / `getSelector()` 存下来，取消勾选时原样塞回去，而不是抄一份"看起来像默认"的写死颜色。另注意 `setDividerHeight` 单位是 **px** |
| `ListFocusActivity` | 行里放 Button 后行点击失灵，用 `descendantFocusability="blocksDescendants"` 修复。三种取值：`FOCUS_BEFORE_DESCENDANTS`（默认）/ `FOCUS_AFTER_DESCENDANTS` / `FOCUS_BLOCK_DESCENDANTS`。**该属性由行布局根节点设置，不是 ListView**；Java 版要在 ViewHolder 里多缓存一个 `ViewGroup itemRoot` 再 `setDescendantFocusability(...)` |

**8.4 GridView 的五个属性与四种 stretchMode**

`numColumns` / `columnWidth` / `horizontalSpacing` / `verticalSpacing` / `stretchMode`，Java 侧对应 `setNumColumns` / `setColumnWidth` / `setHorizontalSpacing` / `setVerticalSpacing` / `setStretchMode`（后四个单位都是 **px**）。用下拉列表切换拉伸模式，实测（Pixel_7，density 2.625，3 列、列宽 90dp、间距 8dp）：

| stretchMode | 格子宽度 | 列间距 | 左侧留白 | 剩余空间去了哪 |
|---|---|---|---|---|
| `none` | 236px | 21px | 0 | 全留在右边 |
| `columnWidth` | **318px** | 21px | 0 | 撑宽格子，间距不变 |
| `spacingWidth` | 236px | **144px** | 0 | 均分到列与列之间 |
| `spacingWidthUniform` | 236px | 82px | **82px** | 连两侧边缘一起均分 |

一句话：**`columnWidth` 改格子，另两种改缝隙，只有 `Uniform` 会在左右两边也留白。**

**8.5 ViewPager 与 PagerAdapter**

ViewPager（`androidx.viewpager.widget`，**不是** ViewPager2）只管三件事：横向滑动、预加载左右各一页、回调滑动状态；页面长什么样全交给 `PagerAdapter`。

| 方法 | 是抽象方法吗 | 职责 |
|---|---|---|
| `getCount()` | ✅ 抽象 | 一共几页 |
| `isViewFromObject()` | ✅ 抽象 | `return view == object`，写成 false 会**静默白屏** |
| `instantiateItem()` | ❌ 有默认实现，但必须重写 | inflate 布局 → 赋值 → `addView` → 返回页面 |
| `destroyItem()` | ❌ 有默认实现，但必须重写 | `container.removeView(...)` |
| `getPageTitle()` | ❌ 可选 | 配 `PagerTabStrip` 时才需要 |

无限轮播的做法：`getCount()` 报 `Integer.MAX_VALUE / 2`，`instantiateItem` 里用 `position % 真实张数` 折回真实数据下标。

> ⚠️ **起始位置必须取中点再对齐**。写成 `(getCount() / size) * size` 算出来是贴着末尾的位置（1073741820，上限 1073741822），只能再往前翻两页就到头了。正确写法是先 `getCount() / 2` 取中点，再减去余数对齐。

对应页面：`ViewPagerActivity`（轮播图）、`PaperTagStripActivity`（标签栏）。

**8.6 Fragment 的两种加载方式**

| 方式 | 声明位置 | 数据怎么传 | 对应类 |
|---|---|---|---|
| 静态 | 布局里的 `<fragment android:name="...">` | 无法传参，只能读自身 XML | `StaticFragment` |
| 动态 | 代码里 `FragmentTransaction.add()`，或交给 Adapter 创建 | `newInstance(参数...)` → `setArguments(Bundle)` → `onCreateView` 里 `getArguments()` 取 | `DynamicFragment` + `UserPageAdapter` |

生命周期实测顺序（Pixel_7 / API 33）：

```
创建：onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
销毁：onDestroyView → onDestroy → onDetach
```

`onActivityCreated` 在 AndroidX Fragment 1.3 起已废弃，对应逻辑应写到 `onViewCreated`。

**8.7 启动引导页（两版对照）**

| 版本 | 适配器 | 单页布局 |
|---|---|---|
| ViewPager + 纯 View | `LaunchSimpleAdapter extends PagerAdapter` | `item_launch.xml` |
| ViewPager + Fragment | `LaunchSimpleImproveAdapter extends FragmentPagerAdapter` | `fragment_launch.xml` + `LaunchFragment` |

两版都把「圆点指示器」放在**每页布局内部**，所以滑动过渡时屏幕底部会同时出现两组圆点。标准做法是把 `RadioGroup` 提到 Activity 布局里只留一份，在 `onPageSelected` 里切换选中项。

引导图 `guide_1~4.png` 是 1080×2400 的整屏图，放在 **`drawable-nodpi`** 而不是 `drawable`——`res/drawable` 等价于 mdpi，420dpi 设备会把它放大 2.625 倍到 2835×6300，单张解码 71MB，ViewPager 预载三页直接 OOM。

---

### 9. `:broadcastapplication` —— 广播、生命周期与画中画

模块入口是 `SystemNetworkActivity`，其余页面在清单里以 `exported="false"` 注册（`adb am start` 拉不起来，需要 `adb root` 或从应用内跳转）。

**9.1 广播的两种注册方式**

| 方式 | 生命周期 | 8.0+ 的限制 | 本模块示例 |
|---|---|---|---|
| 静态注册 | 清单里 `<receiver>`，随应用常驻 | **收不到隐式广播**，必须用 `setComponent(new ComponentName(this, XxxReceiver.class))` 把它变成显式广播 | `ShockReceiver` + `BroadcastStaticActivity` |
| 动态注册 | `registerReceiver` / `unregisterReceiver` 成对出现 | `targetSdk 34+` 必须声明 `RECEIVER_EXPORTED` / `RECEIVER_NOT_EXPORTED` | `AlarmReceiver`、`NetworkReceiver` |

`ComponentName` 的第二个参数是**完整类名**，写成包名（`com.zzz.xxx.receiver`）会指向一个不存在的组件——广播发出去没有任何接收者，**且不报任何错**。

**9.2 静态广播收不到的两个原因**

| 原因 | 现象 | 解法 |
|---|---|---|
| `ComponentName` 传了包名 | 日志一片空白 | 传 `XxxReceiver.class` |
| 8.0+ 隐式广播限制 | 同上 | `intent.setComponent(...)` 改成显式 |

**9.3 闹钟：`AlarmManager` + `PendingIntent`**

| 坑 | 现象 | 解法 |
|---|---|---|
| `new IntentFilter()` 空 filter | 收不到任何广播（filter 里没有 action） | `new IntentFilter(ALARM_ACTION)` |
| `RECEIVER_NOT_EXPORTED` | 闹钟触发了（`dumpsys alarm` 有 wakeups）但接收器一声不响 | 用 **`RECEIVER_EXPORTED`** |
| `triggerAtMillis` 写裸 `1000` | 表示 1970 年的时间戳，属于过去，会立刻触发 | `System.currentTimeMillis() + delay` |

第二条的原因是：**AlarmManager 是「代替」应用发广播，实际发送方是系统进程（uid 1000）**，对接收器来说属于外部来源，`NOT_EXPORTED` 会直接丢弃。口诀：`sendBroadcast()` 自己发的用 `NOT_EXPORTED`；`AlarmManager` / `Notification` 代发的必须 `EXPORTED`。

**9.4 `getSubtype()` 返回的是数字**

`NetworkInfo.getSubtype()` 返回 `TelephonyManager.NETWORK_TYPE_*` 的 int（LTE = 13）。想调 `TelephonyManager.getNetworkTypeName(int)` **不行**——那是隐藏 API，运行时报 `NoSuchMethodError`。只能自己 switch 映射（见 `NetworkReceiver#getSubtypeText`，输出形如 `LTE（4G）`）。

> `NETWORK_TYPE_LTE_CA` 标了 `@SystemApi`，公开 SDK 里没有这个常量，编译报「找不到符号」，只能写 `case 19:` 字面量。

**9.5 生命周期与配置变更**

| 页面 | 清单配置 | 旋转时的表现 |
|---|---|---|
| `LifeCycleTestActivity` | 不配 `configChanges` | 销毁重建，界面上的 `hashCode` 会变 |
| `ChangeDirectionActivity` | `configChanges="orientation\|screenLayout\|screenSize\|keyboard"` | 不重建，只回调 `onConfigurationChanged` |
| `LockDirectionActivity` | `screenOrientation="portrait"` | 方向锁死，旋转手机本页不动 |

旋转实测顺序：

```
旧实例：onPause → onStop → onSaveInstanceState → onDestroy
新实例：onCreate → onStart → onRestoreInstanceState → onResume
```

> ⚠️ **`onSaveInstanceState` 在 `onStop` 之后**，不是教材常写的「onPause 之后、onStop 之前」。Android 9（API 28）起顺序改过。

> ⚠️ 光写 `orientation` 不够：Android 3.2（API 13）起横竖屏切换同时会改变 `screenSize`，漏声明照样重建。

**9.6 画中画（Picture in Picture）**

进入画中画有三条路，模块里都写了：

| 路径 | 说明 |
|---|---|
| `ACTION_CLOSE_SYSTEM_DIALOGS` 广播 + `reason` | 教材做法。`reason=homekey` 回到桌面，`recentapps` 打开任务列表 |
| `onUserLeaveHint()` | **Android 12+ 推荐做法**，用户主动离开页面时由系统回调 |
| 按钮手动调用 | 不依赖系统事件，方便验证画中画本身有没有配好 |

清单必须两行：`android:supportsPictureInPicture="true"` + `configChanges` 里带上 `screenSize|smallestScreenSize`，否则进画中画时 Activity 会重建。

> ⚠️ `ACTION_CLOSE_SYSTEM_DIALOGS` 按官方说明从 Android 12 起不再发给非系统应用。实测 API 33 模拟器**仍能收到**（userdebug 镜像没严格执行），但真机大概率收不到——所以 `onUserLeaveHint()` 才是可靠路径。两条路会同时触发，靠 `!isInPictureInPictureMode()` 防止重复进入。

---

### 10. `:serviceapplication` —— Service 的生命周期、跨进程与保活

模块入口是 `MessengerActivity`，它同时充当导航页；其余演示页都注册为 `exported="false"`，只能从首页跳转。所有 demo 统一用 `ZZZ` 作为日志 TAG，一条命令即可观察全部输出：

```bash
adb logcat -s ZZZ
```

**10.1 两条生命周期路径**

| 操作 | 回调顺序 | 实测现象 |
|---|---|---|
| 首次 `startService()` | `onCreate → onStartCommand` | `onCreate` 打印 `thread=main`，服务跑在主线程 |
| 再次 `startService()` | **只有** `onStartCommand` | `startId` 递增为 2，`onCreate` 不再出现 |
| `stopService()` | `onDestroy` | 服务销毁后重新 start，`onCreate` 会**再次**执行 |
| 首次 `bindService()` | `onCreate → onBind → onServiceConnected` | 客户端的 `onServiceConnected` 总在服务端之后 |
| 再次 `bindService()` | **什么都不回调** | `onBind` 全程只走一次 |
| `unbindService()` | `onUnbind → onDestroy` | 不需要 `stopService()`，全部解绑即销毁 |

> ⚠️ `startId` 是「当前实例内」的计数。服务销毁重建后会从 1 重新开始，不能用它统计累计启动次数。

**10.2 `onStartCommand` 的三个参数与三种返回值**

`intent` 是启动方传来的数据（服务被系统重建时可能为 **null**）；`flags` 区分本次是正常启动、重投递（`START_FLAG_REDELIVERY`）还是重试（`START_FLAG_RETRY`）；`startId` 配合 `stopSelfResult(int)` 可以做到「最后一个启动请求完成时才停服务」。

返回值决定服务被 kill 之后怎么办：

| 返回值 | 被杀后 | 重建时 `intent` | 适用场景 |
|---|---|---|---|
| `START_STICKY` | 内存空闲时重建 | **null** | 长期待命、不执行具体命令（音乐播放器） |
| `START_NOT_STICKY` | 不重建 | — | 最安全，等下次 `startService()` |
| `START_REDELIVER_INTENT` | 重建并重投递最后一个非空 Intent | 有值 | 必须立即恢复的作业（下载） |

**10.3 三种绑定方式怎么选**

| 方式 | 范围 | 代价 | 本模块 |
|---|---|---|---|
| 扩展 Binder 类 | 同应用**同进程** | 跨进程强转会直接崩 | `LocalService` |
| Messenger | 可跨进程 | 单线程串行，并发请求会排队 | `MessengerService` |
| AIDL | 可跨进程 | 需自行处理多线程与线程安全，要写 `.aidl` | 未实现（绝大多数应用用不到） |

`MessengerService` 在清单里加了 `android:process=":remote"`，实测客户端与服务端是两个 PID（7149 / 7304），`Message` 的 `what` 与 `Bundle` 能跨进程，`obj` 字段只对同进程有用。

> ⚠️ `onServiceDisconnected()` **只在连接意外中断时**回调（服务崩溃、进程被系统杀死）。客户端主动 `unbindService()` 时系统**绝对不会**调用它——把清理逻辑写在那里等于永远不执行。

**10.4 启动 + 绑定组合：存活条件模型**

这是整个模块最值得记的一条。四按钮操作台（`HybridActivity`）可以自由组合 start / bind / stop / unbind：

> **服务存活 = 「曾被 `startService()` 启动过」 或 「还有客户端绑着」。**
> `stopService()` 只清掉前一个条件，`unbindService()` 只清掉后一个条件。

两种顺序下「看起来该销毁、实际没有」的那一步正好互换：

| 组合 | 无效的那一步 | `dumpsys` 服务记录数 | 真正销毁 |
|---|---|---|---|
| start → bind → stop → unbind | `stopService()`，**零回调** | 3 → 8 → **8** → 0 | 解绑之后 |
| bind → start → unbind → stop | `unbindService()`，服务仍在 | 8 → 8 → **4** → 0 | 停止之后 |

这就是「调了 `stopService()` 服务没死、通知还挂着」的成因。

**10.5 前台服务：`stopForeground()` 不停止服务**

`startForeground(id, notification)` 要求 id **不能为 0**；`stopForeground(true)` 只撤销前台状态并移除通知，**服务本身继续运行**：

```
dumpsys activity services → isForeground=true foregroundId=1   ← 启动后
点「停止前台服务」后      → ServiceRecord 仍在，isForeground 字段消失，通知计数归 0
```

原文写于 2016 年，直接照抄会撞三处版本变化：

| 原写法 | 现在 | 改法 |
|---|---|---|
| 清单不声明 `foregroundServiceType` | API 34+ 抛 `MissingForegroundServiceTypeException` | 声明 `dataSync` + `FOREGROUND_SERVICE_DATA_SYNC` 权限 |
| `new NotificationCompat.Builder(this)` | 单参数构造已废弃，通知无渠道 | 建 `NotificationChannel(IMPORTANCE_LOW)` |
| `startService()` 启动前台服务 | 应用在后台时抛 `IllegalStateException` | `startForegroundService()`，且 5 秒内必须 `startForeground()` |

> ⚠️ 停止前台状态**不能**走 `startForegroundService()`：它要求 5 秒内调用 `startForeground()`，而撤销操作恰好不调用，会被系统判违规。本模块的「停止」按钮刻意保留 `startService()`。

> ⚠️ 大小图标的要求正好相反：`setLargeIcon()` 要**位图**（`BitmapFactory` 解不了 VectorDrawable，传 vector 返回 null，大图标静默不显示）；`setSmallIcon()` 要**纯色**（系统只取 alpha 当遮罩，彩色图或 adaptive icon 会糊成一团白块）。

**10.6 IntentService：已废弃，但值得看一眼**

连发 3 个任务，线程名全是 `IntentService[my-intent-service]`，且首尾严格相接：

```
任务 1 开始 → 任务 1 结束（04.888）
任务 2 开始（04.889） → 任务 2 结束
任务 3 开始 → 任务 3 结束 → onDestroy    ← 全部跑完自动 stopSelf()
```

单 worker 线程串行排队，决定了它只适合「一次一个」的任务。Android 11（API 30）起 `android.app.IntentService` 被标记 `@Deprecated`，替代方案是 WorkManager（需要保证执行）或 Service + 自行管理的线程/协程。`MyIntentService` 里保留完整类头说明，仅作历史观察。

**10.7 隐式启动：从警告到异常**

`RemoteService` 声明了 `exported="true"` 与 `intent-filter`，用来复现这个问题。裸隐式启动（只设 action、不设 package 和 component）在 5.0+ 直接抛异常：

```
IllegalArgumentException: Service Intent must be explicit: Intent { act=com.zzz.service.ACTION_REMOTE_START }
    at android.app.ContextImpl.validateServiceIntent(ContextImpl.java:1851)
```

Android 4.4 及以前同样条件下只打一条 `Log.w("Implicit intents with startService are not safe")` 就放行——差异就在 `ContextImpl.validateServiceIntent()` 里的版本判断。两种修复：

| 方案 | 做法 | 适用 |
|---|---|---|
| `setPackage()` | 补一个包名，component 仍为空 | 知道目标应用，一行搞定 |
| `queryIntentServices()` 转 ComponentName | 查出匹配项，要求**唯一**，再拼 `ComponentName` | 不确定由哪个应用响应 |

**10.8 保活：文章那套方案现在基本失效**

双服务互拉的做法是：`onDestroy()` 里发一条自定义广播，另一个服务收到后把死掉的那个重新 `startService()` 起来。实测结果分两种情况：

| 场景 | 结果 |
|---|---|
| 应用在前台，`stopService(A)` | 有效：A 发死讯 → B 收到 → 3 秒内 A 被拉回 |
| 用户「强行停止」（`am force-stop`） | **完全失效**：日志一条都没有，`onDestroy` 不回调，广播发不出去，进程直接消失 |

再叠加 Android 8.0 起「应用在后台不允许 `startService()`」的限制，这套写法只在应用处于前台时跑得通。真正的长期后台任务应该用前台服务 + 通知，或交给 WorkManager 这类由系统调度的机制。

> ⚠️ 互拉的 `startService(对方)` **只能写在 `onCreate()`**。写在 `onStartCommand()` 里会形成 A→B→A 无限循环——`onCreate` 只执行一次，而 `onStartCommand` 每次启动都回调。第一版代码就踩了这个坑，注释里留了反面示例。

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
├── server_application/           # ContentProvider 服务端：SQLite `user_info` + 自定义 provider
├── client_application/           # ContentProvider 客户端 + 权限专题：联系人 / 短信 / MediaStore / FileProvider
├── advancedcontrolsapplication/  # 高级控件：Spinner / ListView / GridView，ViewPager 轮播、Fragment、引导页
├── broadcastapplication/         # 广播：静态/动态注册、系统广播、AlarmManager、生命周期与画中画
├── serviceapplication/           # Service：启动/绑定/组合生命周期、Messenger 跨进程、前台服务、IntentService、隐式启动、双服务保活
├── docs/android-blog/            # 源码解读博客的写作工作区（结构分析 / 章节规划 / 草稿）
├── img/                          # 界面截图等素材
│   └── activities/               # 各模块 Activity 实机截图（按模块分目录，INDEX.md 为索引）
├── gradle/libs.versions.toml     # 依赖版本目录
├── build.gradle.kts
├── settings.gradle.kts           # include 了以上 10 个模块
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
| 2026-10-21 | `0d6c3c6` | `:datasharingapplication` 全部 25 个 Java 文件补齐注释（类头 Javadoc + 方法 `@param` + 行内注释） |
| 2026-10-23 | `caf56a8` | ContentProvider 跨应用通信（新增 `:server_application` / `:client_application` 两个模块）、动态权限懒汉式与饿汉式对照、联系人读写、`ContentObserver` 监听短信 |
| 2026-10-26 | `1d51255` | MediaStore 图片网格选图（多选 + `ACTION_SEND`）、FileProvider 发送彩信与安装本地 APK、权限适配（`READ_MEDIA_IMAGES` / `MANAGE_EXTERNAL_STORAGE`） |
| 2026-10-27 | `cd71599` | 新增 `:advancedcontrolsapplication` 高级控件模块：Spinner 的 dropdown / dialog 两种形态、SimpleAdapter 与自建 BaseAdapter 对照、ListView 分隔线与按压背景、列表项焦点抢占（`descendantFocusability`）、GridView 四种 `stretchMode`；另补 8 个模块共 77 张 Activity 实机截图与索引 |
| 2026-10-28 | `a7d5693` | 高级控件模块补齐翻页与碎片：ViewPager + PagerAdapter 轮播图（无限翻页、自动播放、`PagerTabStrip` 标签栏）、Fragment 静态与动态加载、两版启动引导页（纯 View / Fragment）、4 张 1080×2400 整屏引导图（`drawable-nodpi`） |
| 2026-10-28 | `ca9da9f` | 新增 `:broadcastapplication` 广播模块：标准广播与有序广播、静态注册（显式 `ComponentName` 绕开 8.0 隐式限制）、动态注册、`ACTION_TIME_TICK` 与网络变更广播、`AlarmManager` + `PendingIntent` 延迟广播、全生命周期时间线观察、横竖屏 `configChanges` 与方向锁定、画中画 |
| 2026-10-29 | `4410898` | 新增 `:serviceapplication` Service 模块：启动态 / 绑定态两条生命周期的实测对照、`onStartCommand` 三参数与三种返回值、扩展 Binder（同进程）与 Messenger（`:remote` 跨进程）、前台服务 + `NotificationChannel`（适配 API 34 `foregroundServiceType`）、**启动与绑定组合时的存活条件模型**、IntentService 单线程排队验证（API 30 已废弃）、隐式启动被禁与两种现代修复、双服务互拉保活及强行停止不回调 `onDestroy` 的现状 |

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

### AdapterView 这一族的三条

| 坑 | 表现 | 解法 |
|---|---|---|
| **行里有 `Button` / `CheckBox` 时 `setOnItemClickListener` 失灵** | 点行的文字区**完全没反应**（按住连按压高亮都没有），但点行内按钮却正常。实测确认：改行根布局的焦点属性就能修好，说明事件一直送到了 ListView，失效发生在焦点层 —— 行内有 `focusable` 子控件时，整行被视为"可聚焦的行"，ListView 不走行点击逻辑 | 行布局根节点加 `android:descendantFocusability="blocksDescendants"`；或在 ViewHolder 里多缓存一个 `ViewGroup itemRoot`，调 `setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)`。**注意是 `ViewGroup` 的方法，声明成 `View` 编译不过** |
| **Spinner `spinnerMode="dialog"` 并不是"自带 radio 的单选弹窗"** | 实测 API 33 + Material3 下，弹窗里是纯文字行，没有单选圆点，当前项也没有勾选标记 | 想要那种观感，得自己写一份含 `CheckedTextView` / `RadioButton` 的布局传给 `setDropDownViewResource()`。`android:prompt` 只在 dialog 模式下作为对话框标题可见 |
| **切 `stretchMode` 后立刻量尺寸，拿到的是旧值** | `setStretchMode()` 之后马上 `getChildAt(0).getWidth()`，读到的还是上一帧的列宽 | 列宽在 `onMeasure` 里重算，必须用一次性的 `OnGlobalLayoutListener` 等这一帧布局结束再读，读完 `removeOnGlobalLayoutListener(this)`。另外 `setStretchMode` 的值没变时不会触发重排，需要时补一句 `requestLayout()` |

### 广播与页面状态的四条

| 坑 | 表现 | 解法 |
|---|---|---|
| **`ComponentName` 传了包名** | 静态注册的接收器收不到广播，**日志一片空白且不报任何错** | 第二个参数必须是完整类名：`new ComponentName(this, ShockReceiver.class)`；用 `.class` 写错编译期就报错 |
| **`new IntentFilter()` 空 filter** | 接收器注册成功，但永远收不到任何广播 | filter 里必须 `addAction(...)` 或构造时传入 action —— 没有 action 就匹配不上任何 Intent |
| **`RECEIVER_NOT_EXPORTED` 拦掉 AlarmManager 广播** | `dumpsys alarm` 里能看到闹钟已触发（有 wakeups 计数），但接收器一声不响 | 用 `RECEIVER_EXPORTED`。原因：闹钟是 AlarmManager **代替**应用发的，实际发送方是系统进程（uid 1000），对接收器来说属于外部来源 |
| **`ACTION_CLOSE_SYSTEM_DIALOGS` 检测 Home 键** | Android 12 起官方不再发给非系统应用，真机上收不到 | 改用 `onUserLeaveHint()` —— 用户主动离开页面时由系统回调，是目前唯一可靠的方式 |

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
