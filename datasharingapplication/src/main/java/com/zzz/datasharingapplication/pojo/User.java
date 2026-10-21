package com.zzz.datasharingapplication.pojo;

/**
 * 用户实体类（POJO）：对应 SQLite 里 student 表的一行记录
 *
 * <p>演示要点：
 * 1. 字段全部 {@code public}，页面里可以直接 {@code user.name = "张三"} 赋值，
 *    适合演示；正式项目应改为 private + getter/setter，避免外部随意改状态；
 * 2. 字段类型刻意混用 int / long / float / boolean，用来演示
 *    {@code Cursor.getInt / getLong / getFloat / getInt(0/1)} 与建表 SQL 里
 *    {@code INTEGER / REAL} 的对应关系（SQLite 没有 boolean，婚否用 0/1 存）；
 * 3. 提供两个构造方法：无参（先 new 再逐个 set，适合"由 Cursor 逐列填充"）与全参（一次给全）。
 *
 * <p>相关文件：{@code database/UserDBHelper}（建表与增删改查）、{@code SqliteHelperActivity}（增删改查界面）
 */
public class User {

    public int id;              // 序号（对应表主键 _id，自增）
    public String name;         // 姓名
    public int age;             // 年龄
    public long height;         // 身高（单位 mm，用 long 演示大整数列）
    public float weight;        // 体重（单位 kg，用 float 演示单精度浮点列）
    public boolean married;     // 婚否（SQLite 里存 0/1）

    /**
     * 全参构造：一次把所有字段填好
     *
     * @param id      序号（新增时一般传 0，交给数据库自增）
     * @param name    姓名
     * @param age     年龄
     * @param height  身高
     * @param weight  体重
     * @param married 婚否
     */
    public User(int id, String name, int age, long height, float weight, boolean married) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.married = married;
    }

    /**
     * 无参构造：给"先建空对象、再逐字段赋值"的场景用（例如从 Cursor 里读一行）
     */
    public User() {
    }

    /** @return 序号 */
    public int getId() {
        return id;
    }

    /** @param id 序号 */
    public void setId(int id) {
        this.id = id;
    }

    /** @return 姓名 */
    public String getName() {
        return name;
    }

    /** @param name 姓名 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 年龄 */
    public int getAge() {
        return age;
    }

    /** @param age 年龄 */
    public void setAge(int age) {
        this.age = age;
    }

    /** @return 身高 */
    public long getHeight() {
        return height;
    }

    /** @param height 身高 */
    public void setHeight(long height) {
        this.height = height;
    }

    /** @return 体重 */
    public float getWeight() {
        return weight;
    }

    /** @param weight 体重 */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /** @return 是否已婚 */
    public boolean isMarried() {
        return married;
    }

    /** @param married 是否已婚 */
    public void setMarried(boolean married) {
        this.married = married;
    }

    /**
     * 把对象转成可读字符串（调试与界面直接输出都用它）
     *
     * @return 形如 {@code User{id=1, name='张三', age=25, ...}} 的文本
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", weight=" + weight +
                ", married=" + married +
                '}';
    }
}
