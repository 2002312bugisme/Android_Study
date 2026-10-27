# Activity 截图索引

模拟器：Pixel_7 AVD / Android 13（API 33, google_apis x86_64）/ 1080x2400 / 浅色模式

命名规则：`img/activities/<Gradle模块名>/<Activity类名>.png`，与源码类名一一对应；
带状态后缀的是同一页的额外状态截图：
`_dropdown_open` / `_dialog_open` 弹层展开；`_custom_style` / `_pressed` 样式与按压；
`_block_focus` 焦点阻止开启；`GridViewActivity_{none|columnWidth|spacingWidth|spacingWidthUniform}` 四种拉伸模式。

## advancedcontrolsapplication（15）

| 截图 | 备注 |
|---|---|
| `BaseAdapterActivity.png` | ListView + 自建 PlanetBaseAdapter（实体类数据集 + ViewHolder） |
| `GridViewActivity_columnWidth.png` | stretchMode=columnWidth：格子被撑到 318px，间距仍 21px |
| `GridViewActivity_none.png` | stretchMode=none：格子 236px、间距 21px、左侧 0，剩余空间全留右侧 |
| `GridViewActivity_spacingWidth.png` | stretchMode=spacingWidth：格子 236px 不变，间距被撑到 144px，两侧无留白 |
| `GridViewActivity_spacingWidthUniform.png` | stretchMode=spacingWidthUniform：间距 82px，且左侧也留白 82px |
| `ListFocusActivity.png` | 行内带 Button，默认 FOCUS_BEFORE_DESCENDANTS：点行文字区无反应 |
| `ListFocusActivity_block_focus.png` | 勾选 blocksDescendants 后点行：回显「行被点击 → 第 1 行」 |
| `ListViewActivity.png` | 默认态：主题默认分隔线（细浅灰），两开关未勾选 |
| `ListViewActivity_custom_style.png` | 两个开关都勾选：渐变橙色 12dp 分隔线 + 自定义 selector |
| `ListViewActivity_pressed.png` | 按住第一行时的按压背景（半透明靛蓝色块） |
| `SimpleAdapterActivity.png` |  |
| `SpinnerDialogActivity.png` |  |
| `SpinnerDialogActivity_dialog_open.png` | 模态弹窗状态，标题来自 android:prompt；实测无 radio 圆点 |
| `SpinnerDropdownActivity.png` |  |
| `SpinnerDropdownActivity_dropdown_open.png` | 下拉层展开状态（点第一个 Spinner） |

## app（2）

| 截图 | 备注 |
|---|---|
| `MainActivity.png` |  |
| `SecondActivity.png` |  |

## client_application（9）

| 截图 | 备注 |
|---|---|
| `ContactAddActivity.png` | 截图前已预授予 READ/WRITE_CONTACTS |
| `ContentWriteActivity.png` |  |
| `MainActivity.png` |  |
| `MonitorSmsActivity.png` | 截图前已预授予 READ_SMS |
| `PermissionHungryActivity.png` | 截图前已预授予全部危险权限，故直接进入已授权态 |
| `PermissionLazyActivity.png` | 懒汉式对照页，按钮点击后才申请权限 |
| `ProviderAPKActivity.png` |  |
| `ProviderMmsActivity.png` | 截图前已预授予 READ_MEDIA_IMAGES / MANAGE_EXTERNAL_STORAGE |
| `SendMessagesActivity.png` | 截图前已预授予 SEND_SMS / READ_MEDIA_IMAGES |

## datasharingapplication（11）

| 截图 | 备注 |
|---|---|
| `ApplicationActivity.png` |  |
| `BookEntryActivity.png` |  |
| `ExternalStorageActivity.png` |  |
| `ImageStorageActivity.png` |  |
| `SharedPreferencesActivity.png` |  |
| `ShoppingCartActivity.png` |  |
| `ShoppingChannelActivity.png` |  |
| `ShoppingGoodDetailActivity.png` | 必须带 --ei goods_id，否则 onCreate 里 finish() |
| `ShowActivity.png` |  |
| `SqliteActivity.png` |  |
| `SqliteHelperActivity.png` |  |

## intermediatecontrols（15）

| 截图 | 备注 |
|---|---|
| `AlertDialogActivity.png` |  |
| `CheckBoxActivity.png` |  |
| `DatePickerActivity.png` |  |
| `DrawableActivity.png` |  |
| `DrawableNineActivity.png` |  |
| `DrawableStateActivity.png` |  |
| `EditHideActivity.png` |  |
| `EditTextActivity.png` |  |
| `EditTextFocusActivity.png` |  |
| `ForgetPasswordActivity.png` |  |
| `LoginMainActivity.png` |  |
| `MainActivity.png` | AS 默认模板页（Hello World!），源码存在但生产清单未注册 |
| `RadioButtonActivity.png` |  |
| `SwitchActivity.png` |  |
| `TimePickerActivity.png` |  |

## myactivityapplication（9）

| 截图 | 备注 |
|---|---|
| `ActFinishActivity.png` |  |
| `ActStartActivity.png` |  |
| `BundleReceiveActivity.png` | 必须从上一页带 extras；直接启动会 NPE |
| `BundleSendActivity.png` |  |
| `IntentActivity.png` |  |
| `MainActivity.png` | AS 默认模板页（Hello World!），源码存在但生产清单未注册 |
| `ReadResourceActivity.png` |  |
| `StartupmodeActivity.png` |  |
| `StartupmodefinishActivity.png` |  |

## mytextapplication（15）

| 截图 | 备注 |
|---|---|
| `CalculatorActivity.png` |  |
| `MainButtonClickActivity.png` |  |
| `MainButtonEnabledActivity.png` |  |
| `MainButtonStyleActivity.png` |  |
| `MainGridLayoutActivity.png` |  |
| `MainImageViewActivity.png` | 注册曾被注释掉，截图时临时注册 |
| `MainLinearLayoutActivity.png` |  |
| `MainRelativeLayoutActivity.png` |  |
| `MainScrollViewActivity.png` |  |
| `MainTextActivity.png` | AS 默认模板页，源码存在但生产清单未注册 |
| `MainTextColorActivity.png` | 注册曾被注释掉，截图时临时注册 |
| `MainTextSizeActivity.png` | 源码存在但生产清单未注册 |
| `MainViewBorderActivity.png` |  |
| `MainViewGravityActivity.png` |  |
| `MainViewMarginPaddingActivity.png` |  |

## server_application（1）

| 截图 | 备注 |
|---|---|
| `MainActivity.png` |  |

合计 **77** 张。