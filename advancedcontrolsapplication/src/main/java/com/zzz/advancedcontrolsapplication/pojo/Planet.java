package com.zzz.advancedcontrolsapplication.pojo;

import com.zzz.advancedcontrolsapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 行星数据模型（POJO，Plain Old Java Object）——BaseAdapter 版列表的「一行」。
 *
 * <p>演示要点：
 * <ol>
 *   <li>这是 {@code BaseAdapter} 与 {@code SimpleAdapter} 最本质的差别：
 *       SimpleAdapter 的行数据只能摊成 {@code Map<String, Object>}，key 靠字符串约定，
 *       写错了编译器不管、运行期也不报错，只是那一格空白；
 *       换成实体类之后，字段有名字、有类型，写错编译器当场报错。</li>
 *   <li>三个字段对应 row 布局里的三个控件（图标 / 标题 / 描述）。
 *       字段是 {@code final} 且只给 getter：数据造好之后适配器只读不改，
 *       防止 getView 里顺手改数据导致「滑出去再滑回来变了样」这种诡异现象。</li>
 *   <li>图标存的是 <b>资源 id（int）</b>，不是 Bitmap：资源 id 只是个小整数，
 *       在 List 里传几十上百个也不占内存；真正的解码发生在 ImageView 里，且由框架做缓存。</li>
 * </ol>
 *
 * <p>⚠️ POJO 里出现 {@code R} 引用并不优雅（模型层混进了资源层），真实项目一般存
 * 图片 URL / 文件名，由 View 层去加载；教学 demo 为了免除网络与异步的干扰才直接放资源 id。
 *
 * <p>涉及 API：普通 Java 实体类，无 Android 特有 API
 */
public class Planet {

    /** 行星图标的资源 id，即 {@code R.drawable.ic_planet_xxx} 这个 int */
    private final int iconResId;

    /** 行星名称，显示在行布局的主标题 */
    private final String title;

    /** 一句话简介，显示在行布局的副标题 */
    private final String description;

    /**
     * 构造一个行星对象。
     *
     * @param iconResId   图标资源 id，如 {@code R.drawable.ic_planet_earth}
     * @param title       行星名称，如 "地球 Earth"
     * @param description 一句话简介
     */
    public Planet(int iconResId, String title, String description) {
        this.iconResId = iconResId;
        this.title = title;
        this.description = description;
    }

    /**
     * @return 图标资源 id
     */
    public int getIconResId() {
        return iconResId;
    }

    /**
     * @return 行星名称
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return 行星简介
     */
    public String getDescription() {
        return description;
    }

    /**
     * 内置演示数据：把五颗行星初始化成一个 List，供适配器直接使用。
     *
     * <p>为什么放在 POJO 里而不是 Activity 里：这份数据是「模型的默认值」，
     * Activity 只负责把它交给适配器。真实项目里这一层通常是
     * 「数据库查询 / 网络请求返回的 List」，方法签名不变，只换实现。
     *
     * @return 长度为 5 的 {@link List}，顺序即列表显示顺序；每次调用都返回<b>新的</b> List，
     *         调用方可以随意增删而不影响别人
     */
    public static List<Planet> getDefaultPlanets() {
        // ArrayList 是有序、可变的实现；这里不需要线程安全，不用 Vector / CopyOnWriteArrayList
        List<Planet> planets = new ArrayList<>();
        planets.add(new Planet(R.drawable.ic_planet_mercury, "水星 Mercury",
                "离太阳最近，表面布满环形山，昼夜温差可达 600℃"));
        planets.add(new Planet(R.drawable.ic_planet_venus, "金星 Venus",
                "轨道紧邻地球，浓厚的二氧化碳大气造成极端温室效应"));
        planets.add(new Planet(R.drawable.ic_planet_earth, "地球 Earth",
                "已知唯一存在生命的行星，71% 的表面被液态水覆盖"));
        planets.add(new Planet(R.drawable.ic_planet_mars, "火星 Mars",
                "表面氧化铁使其呈红色，拥有太阳系最高的火山奥林帕斯山"));
        planets.add(new Planet(R.drawable.ic_planet_jupiter, "木星 Jupiter",
                "体积最大的行星，大红斑是持续数百年的巨型风暴"));
        return planets;
    }
}
