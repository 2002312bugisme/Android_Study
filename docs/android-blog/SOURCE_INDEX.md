# SOURCE_INDEX —— 源码文件 ↔ 知识点映射

> 用途：写某一章时，先用本表定位可引用的**真实代码位置**，再打开文件读取。
> 原则：**本表只做索引，不替代读源码**。写章节前必须实际打开文件核对。
> 状态标记：✅ 已逐行读取确认 ｜ 🔍 已确认存在，内容未逐行读 ｜ ⚪ 未涉及

---

## 一、构建与配置

| 文件 | 职责 | 知识点 | 章节 | 状态 |
|---|---|---|---|---|
| `settings.gradle.kts` | 声明 rootProject 名与 5 个模块、仓库策略 | 多模块、`FAIL_ON_PROJECT_REPOS` | 01 | ✅ |
| `build.gradle.kts`（根） | 只声明插件 `apply false` | 根/模块构建脚本的分工 | 01 | ✅ |
| `gradle/libs.versions.toml` | 版本目录：所有依赖与插件版本 | Version Catalog、`alias(libs.*)` | 01 | ✅ |
| `mytextapplication/build.gradle.kts` | 该模块 Android 配置 | `compileSdk`/`minSdk`/`targetSdk`、`viewBinding` | 01 | ✅ |
| `myactivityapplication/build.gradle.kts` | 同上 | 同上 | 01 | ✅ |
| `intermediatecontrols/build.gradle.kts` | 同上 | 同上 | 01 | ✅ |
| `.gitignore` ⚠️ | 忽略构建产物与本机配置 | **缺 `.workbuddy/`** | — | ✅ |

## 二、AndroidManifest.xml（5 份）

| 文件 | 可引用的具体内容 | 知识点 | 章节 |
|---|---|---|---|
| `mytextapplication/.../AndroidManifest.xml` | `CalculatorActivity` 的 `MAIN`/`LAUNCHER`；自定义 action `android.intent.action.ZZZ`；**两个被注释掉的 activity 声明** | intent-filter、`exported`、自定义 action、注册与代码的一致性 | 02 |
| `myactivityapplication/.../AndroidManifest.xml` | `ActStartActivity` 的 launcher + `<meta-data android:name="android.app.shortcuts">`；`ReadResourceActivity` 的 `<meta-data android:name="metadata" android:value="Hello World!"/>` | meta-data、shortcuts、`exported=true` | 02 / 11 |
| `intermediatecontrols/.../AndroidManifest.xml` | `EditHideActivity` 为 launcher；9 个 activity 全部 `exported=false` | 默认导出规则 | 02 |
| `app/.../AndroidManifest.xml` | Compose 模块的入口 | 作为对照 | 01 |
| `datasharingapplication/.../AndroidManifest.xml` | `WRITE_EXTERNAL_STORAGE`(`maxSdkVersion=28`) + `READ_EXTERNAL_STORAGE`(`maxSdkVersion=32`)；`android:requestLegacyExternalStorage="true"`；`android:name=".application.BookApplication"`；其余 7 个 activity 以注释保留 | 分区存储、`maxSdkVersion`、Application 注册唯一性 | 扩展（不在 14 章内） |

## 三、`:mytextapplication`（文本 / View / 布局 / 事件 / 图片）

| 文件 | 职责与关键内容 | 知识点 | 章节 | 状态 |
|---|---|---|---|---|
| `MainTextActivity.java` | 最短的完整例子：`setContentView` + `findViewById` + `setText(R.string.hello)`；Edge-to-Edge 被注释 | XML 与 Java 的连接、资源引用 | 05 | ✅ |
| `MainTextColorActivity.java` | 注释里保留 4 种颜色写法：`Color.GREEN`、`0x11111111`、`0x00ff00`；实际生效的是 `setBackgroundResource(R.color.black)` | **ARGB 与 alpha 坑**、颜色资源 | 05 | ✅ |
| `MainTextSizeActivity.java` | 纯 `setContentView`，尺寸全在 XML 中 | `sp` vs `dp` | 04 / 05 | ✅ |
| `MainViewBorderActivity.java` | `getLayoutParams()` → 改 `width/height` → `Utils.dp2px` → `setLayoutParams` | `LayoutParams`、单位换算、**为什么默认是 px** | 04 | ✅ |
| `MainViewMarginPaddingActivity.java` | 空壳，margin/padding 全在 XML | margin 与 padding 的区别 | 04 | ✅ |
| `MainViewGravityActivity.java` | 空壳 | `gravity` vs `layout_gravity` | 04 | ✅ |
| `MainLinearLayoutActivity.java` | 空壳 | `orientation`、`layout_weight` | 03 | ✅ |
| `MainRelativeLayoutActivity.java` | 空壳 | `layout_toRightOf` 等相对定位 | 03 | 🔍 |
| `MainGridLayoutActivity.java` | 空壳 | `columnCount`/`rowCount`/`layout_columnWeight` | 03 | ✅ |
| `MainScrollViewActivity.java` | 空壳 | 滚动容器、单子 View 限制 | 03 | 🔍 |
| `MainButtonStyleActivity.java` | **`public void doClick(View view)`** → XML `android:onClick` 写法 | 第 4 种点击绑定方式 | 06 | ✅ |
| `MainButtonClickActivity.java` | 一个文件三种写法：独立静态内部类 / Activity 实现接口 / Lambda 长按 | `setOnClickListener`、`OnLongClickListener`、`v.getId()` | 06 | ✅ |
| `MainButtonEnabledActivity.java` | `setEnabled(true/false)` + `setTextColor` | 控件状态与可用性 | 06 | ✅ |
| `MainImageViewActivity.java` | 空壳（Manifest 中已注释，**无法直接启动**） | 需先取消注释才能实验 | 08 | ✅ |
| `CalculatorActivity.java` | 205 行：`double firstNumber` / `operator` / `newNumber` 三状态机；19 个按钮统一 `implements View.OnClickListener`；`formatResult` 处理 `5.0 → 5` | 综合案例、状态机、`Double.parseDouble` | 14 | ✅ |
| `utils/Utils.java` | `dp2px(Context, float)`：`dp * density + 0.5f` | 密度换算原理 | 04 | ✅ |
| `utils/DateUtils.java` | `SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)` + `TimeZone.getTimeZone("Asia/Shanghai")` | 日期格式化（Java 侧，非 Android 特有） | 14 附录 | ✅ |
| `drawable/btn_background.xml` | **selector 内联 `<shape>`** 两种状态 | selector 写法 B | 07 | ✅ |
| `drawable/tos_cn.png` | 图片章节的被测图 | 图片资源与密度目录 | 08 | ✅ |
| `layout/activity_main_button_click.xml` | 3 组 `btn_*` + `tv_message_*`，`android:background="@color/green"` | 布局与控件的对应 | 06 | ✅ |
| `layout/activity_main_linear_layout.xml` | 嵌套 LinearLayout，`layout_width="0dp"` + `layout_weight="1"` 的横向均分与纵向均分 | `layout_weight` 的正确用法 | 03 | ✅ |
| `layout/activity_main_grid_layout.xml` | `columnCount=2`、`layout_columnWeight` 1/2/1/1 的宽度比例 | GridLayout 权重 | 03 | ✅ |
| `layout/activity_main_image_view.xml` | **8 种 `scaleType` 全量对比**（fitCenter/centerCrop/center/centerInside/fitXY/fitStart/fitEnd/matrix）+ `ImageButton` + `Button#drawableStart` | ImageView 缩放模型 | 08 | ✅ |
| 其余 `layout/activity_*.xml` | 见「待补读」 | — | — | 🔍 |

## 四、`:myactivityapplication`（Activity / Intent / Bundle）

| 文件 | 职责与关键内容 | 知识点 | 章节 | 状态 |
|---|---|---|---|---|
| `MainActivity.java` | 空壳，**未在 Manifest 注册** | 反例：未注册不可启动 | 02 | ✅ |
| `ActStartActivity.java` | `startActivity(new Intent(this, ActFinishActivity.class))`，最短的显式跳转 | 显式 Intent | 11 | ✅ |
| `ActFinishActivity.java` | 两个控件（`ImageView` 返回图 + `Button`）共用一次 `finish()`，用 `\|\|` 合并 id 判断 | `finish()`、多控件共用一个监听 | 11 | ✅ |
| `IntentActivity.java` | 四种跳转并列：`setComponent(new ComponentName(...))`、`ACTION_DIAL + tel:`、`ACTION_SENDTO + smsto:`、自定义 action `android.intent.action.ZZZ` | 显式/隐式 Intent、`Uri`、`setData` | 11 | ✅ |
| `BundleSendActivity.java` | `Bundle.putString` ×2 → `intent.putExtras` → `register.launch(intent)`；**注释掉了 `startActivity`** | **`registerForActivityResult` 新 API**、`putExtras` | 12 | ✅ |
| `BundleReceiveActivity.java` | `getIntent().getExtras()` 与 `getIntent().getStringExtra()` 两种取值并列；`setResult(RESULT_OK, intent)` + `finish()` 回传 | Bundle 取值、结果回传 | 12 | ✅ |
| `StartupmodeActivity.java` | **自带 30 行中文注释**：standard / CLEAR_TASK / CLEAR_TOP / REORDER_TO_FRONT 对返回栈的影响，含 `A → B → A → B` 推演 | 返回栈、Intent Flag | 13 | ✅ |
| `StartupmodefinishActivity.java` | 与 A 互相跳转，同样设 `FLAG_ACTIVITY_REORDER_TO_FRONT` | 与 A 对照观察 | 13 | ✅ |
| `ReadResourceActivity.java` | `getString(R.string.string_data)`；`getPackageManager().getActivityInfo(..., GET_META_DATA)` 读 Manifest 中的 `metadata` | 资源读取、`PackageManager`、异常处理 | 02 / 05 | ✅ |
| `utils/Utils.java` / `utils/DateUtils.java` | 与 `:mytextapplication` 中**内容完全相同** | 模块隔离与代码复制的代价 | 01 | ✅ |
| `res/xml/shortcuts.xml` | 静态快捷方式定义 | 桌面快捷方式 | 11（彩蛋） | 🔍 |

## 五、`:intermediatecontrols`（Drawable / 复合控件 / EditText）

| 文件 | 职责与关键内容 | 知识点 | 章节 | 状态 |
|---|---|---|---|---|
| `MainActivity.java` | 空壳，**未注册** | 反例 | — | ✅ |
| `DrawableActivity.java` | `view_content.setBackgroundResource(R.drawable.shape_rectangle / shape_oval)` 在两个 shape 间切换 | 用 Java 切换 Drawable | 07 | ✅ |
| `DrawableNineActivity.java` | 空壳，**逻辑全在 XML 的注释里** | 九宫格与普通图的对比 | 07 | ✅ |
| `DrawableStateActivity.java` | 空壳，状态切换全在 XML | selector 状态 | 07 | ✅ |
| `CheckBoxActivity.java` | `implements CompoundButton.OnCheckedChangeListener`，用 `buttonView.setText()` 回显 | `OnCheckedChangeListener` | 09 | ✅ |
| `SwitchActivity.java` | 同样的接口监听 Switch | 控件共用的接口抽象 | 09 | ✅ |
| `RadioButtonActivity.java` | `RadioGroup.OnCheckedChangeListener`，回调给的是 `checkedId` 而非控件 | 单选组、**两种回调签名对比** | 09 | ✅ |
| `EditTextActivity.java` | 空壳 | 输入类型全在 XML | 10 | ✅ |
| `EditTextFocusActivity.java` | `OnFocusChangeListener` + `phone.matches("^1[3-9]\\d{9}$")` + `setError` + `Toast` | 焦点事件、正则校验、错误提示 | 10 | ✅ |
| `EditHideActivity.java` | `private class HideTextWatcher implements TextWatcher`，长度达标即收起键盘 | `TextWatcher`、接口三个回调的分工 | 10 | ✅ |
| `utils/ViewUtil.java` | `hideOneInputMethod(Activity, View)`：`getSystemService(INPUT_METHOD_SERVICE)` → `hideSoftInputFromWindow(view.getWindowToken(), 0)` | 系统服务获取、软键盘控制 | 10 | ✅ |
| `drawable/shape_rectangle.xml` | **`<solid>` + `<stroke>` + `<corners>` + `<gradient>` 四种子标签齐全** | shape 全要素 | 07 | ✅ |
| `drawable/shape_oval.xml` | 椭圆 shape | 与 rectangle 对比 | 07 | 🔍 |
| `drawable/btn_nine_selector.xml` | selector 引用外部 `.9.png` 与普通 `png` | selector 写法 A | 07 | ✅ |
| `drawable/checkbox_selector.xml` | `state_checked` → checked/unchecked png | 状态选择器 | 09 | ✅ |
| `drawable/nine_origin.9.png` / `origin.png` | 九宫格与普通图对照 | `.9.png` 命名与拉伸规则 | 07 | ✅ |
| `layout/activity_check_box.xml` | **作者手写 13 行注释解释 `buttonTint="@null"`**；用 `AppCompatCheckBox` 而非 `CheckBox` | Material3 tint 坑（一手记录） | 09 | ✅ |
| `layout/activity_drawable_nine.xml` | **作者手写注释解释为何必须用 `AppCompatButton`**；`.9.png` 与普通图并列 | Material 主题替换 `<Button>` | 07 | ✅ |
| 其余 `layout/activity_*.xml` | 见「待补读」 | — | — | 🔍 |

---

## 六、待补读清单（写对应章节前必须打开）

| 文件 | 对应章节 |
|---|---|
| `mytextapplication/.../activity_main_relative_layout.xml` | 03 |
| `mytextapplication/.../activity_main_scroll_view.xml` | 03 |
| `mytextapplication/.../activity_main_view_gravity.xml` | 04 |
| `mytextapplication/.../activity_main_view_margin_padding.xml` | 04 |
| `mytextapplication/.../activity_main_text_size.xml` | 04 / 05 |
| `mytextapplication/.../activity_main_text_color.xml` | 05 |
| `mytextapplication/.../activity_main_text.xml` | 05 |
| `mytextapplication/.../activity_main_button_style.xml` | 06 |
| `mytextapplication/.../activity_main_button_enabled.xml` | 06 |
| `mytextapplication/.../activity_calculator.xml` | 14 |
| `myactivityapplication/.../activity_read_resource.xml` | 02 |
| `myactivityapplication/.../res/xml/shortcuts.xml` | 11 |
| `intermediatecontrols/.../activity_drawable.xml`、`activity_drawable_state.xml` | 07 |
| `intermediatecontrols/.../activity_switch.xml`、`activity_radio_button.xml` | 09 |
| `intermediatecontrols/.../activity_edit_text.xml`、`activity_edit_text_focus.xml`、`activity_edit_hide.xml` | 10 |
| `intermediatecontrols/.../drawable/edit_text.xml`、`edit_text_focus.xml`、`edit_text_normal.xml`、`shape_oval.xml` | 07 / 10 |
| 三个模块的 `values/colors.xml`、`values/themes.xml` | 01 / 05 |
| `mytextapplication/.../values/themes.xml` | 01 |

---

## 七、明确**不存在**的知识点（禁止写成"项目已实现"）

以下内容在**前四个模块**（`:app` / `:mytextapplication` / `:myactivityapplication` / `:intermediatecontrols`）的已读源码中**均未出现**。若章节需要，只能作为"扩展知识"并显式标注：

| 知识点 | 证据 |
|---|---|
| Fragment / Navigation 组件登录代码 | 仅有依赖，无 `NavHostFragment`/`NavController` 使用 |
| RecyclerView / ListView / Adapter | 前四个模块无相关类与布局（`ListView` 出现在 `:datasharingapplication`） |
| `ViewModel` / `LiveData` / `DataBinding` | 无相关导入 |
| 网络请求（OkHttp / Retrofit / HttpURLConnection） | 无依赖、Manifest 无 `INTERNET` 权限 |
| 本地存储（SharedPreferences / Room / SQLite / 文件读写） | 前四个模块无相关代码；`:datasharingapplication`（2026-10-19 新增）已完整实现，但不在 14 章规划范围内 |
| `onStart`/`onResume`/`onPause`/`onStop`/`onDestroy` 覆写 | **所有 Activity 只覆写 `onCreate`** |
| `onSaveInstanceState` / 状态恢复 | 无覆写（`savedInstanceState` 参数未被使用） |
| 运行时权限申请 | 前四个模块 Manifest 无任何 `<uses-permission>`（`:datasharingapplication` 有存储读写权限） |
| 自定义 View（继承 View 并重写 `onDraw`） | 只有自定义 Drawable，无自定义 View 类 |
| `Parcelable` 序列化 | Bundle 中只放了 `String` |
| 动画（`ObjectAnimator` / `ViewPropertyAnimator`） | 无 |
| 主题切换 / 深色模式适配代码 | 仅在 `themes.xml` 中继承 `DayNight`，无切换代码 |
| 多语言（`values-en/`） | 只有默认 `values/` |

> **这些"缺口"本身就是第 13 章和第 14 章的实验素材来源** —— 例如"自己给 Activity 加上
> `onStart`/`onResume` 的 `Log` 覆写，观察调用顺序"，是项目当前**做不到直接演示**、
> 必须动手加代码才能验证的实验。写作时按"实验任务"呈现，不要描述成已有行为。

---

## 八、附：`:datasharingapplication` 索引（2026-10-19 新增，**不在 14 章规划内**）

仅登记可用素材，若日后决定新增"数据存储"一章，可直接从这里取材。**写作前仍需实际打开文件核对。**

| 文件 | 可引用的具体内容 | 知识点 | 状态 |
|---|---|---|---|
| `pojo/BookInfo.java` | `@Entity(tableName = "book_info")` + `@PrimaryKey(autoGenerate = true)` + 5 个业务字段 | Room 实体声明 | ✅ |
| `dao/BookDao.java` | `@Insert` 三种签名（可变长 / 返回 rowId / `List` 批量）、`@Update`、`@Delete`、`@Query` 四种（全量 / `LIKE '%' \|\| :keyword \|\| '%'` / 按 id / `DELETE FROM`） | DAO 注解全集、命名参数 `:keyword` | ✅ |
| `database/BookDataBase.java` | `@Database(entities=..., version=1, exportSchema=true)`，只暴露 `bookDao()` | `_Impl` 由注解处理器生成 | ✅ |
| `application/BookApplication.java` | `Room.databaseBuilder(...).allowMainThreadQueries().build()` 单例持库 | 数据库单例；`allowMainThreadQueries` 为何只适合演示 | ✅ |
| `BookEntryActivity.java` | 表单校验 → `Executor` 单线程落库 → `runOnUiThread` 刷新 `SimpleAdapter` | 主线程不碰 IO、`ListView` + `SimpleAdapter` | ✅ |
| `schemas/…BookDataBase/1.json` | 导出的 `createSql` / `identityHash` / `setupQueries` | schema 导出与迁移比对 | ✅ |
| `database/UserDBHelper.java` | 读写连接分离、`Cursor` → 对象映射、`insertBatchWithTransaction` 事务回滚（内含故意 `10/0`） | 事务、`beginTransaction`/`setTransactionSuccessful` | ✅ |
| `ExternalStorageActivity.java` | 内部 / 外部私有 / 外部公共三目录对照；Android 6~9 动态权限、10+ 分区存储 | 存储分区模型、`maxSdkVersion` | ✅ |
| `ImageStorageActivity.java` | `Bitmap.compress` 落盘 + `decodeFile` 读回；`getExternalFilesDir` 可能返回 `null` | Bitmap 与文件 IO | ✅ |
| `MyApplication.java` | 全局 `List<UserInfo>`、四个 Application 生命周期回调 | ⚠️ 该 Application **未在清单注册**，属反例素材 | ✅ |
