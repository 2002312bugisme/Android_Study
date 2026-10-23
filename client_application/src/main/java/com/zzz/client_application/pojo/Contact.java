package com.zzz.client_application.pojo;

/**
 * 联系人数据对象（POJO / JavaBean）
 *
 * <p>作用：把界面上几个输入框的值打包成一个对象，避免一路传递零散字符串。
 * 字段与 {@code activity_contact_add.xml} 的三个输入框一一对应，
 * 后续写联系人时直接从对象里取字段即可。
 *
 * <p>JavaBean 的三条惯例（以后写任何实体类都适用）：
 * 1. 字段全部 {@code private}，外部只能通过方法访问 —— 以后要加校验（比如电话格式）只改一处；
 * 2. 提供 public 无参构造 —— 很多框架（Gson、Room）靠反射创建对象，没有它会失败；
 * 3. getter/setter 命名必须是 {@code getXxx} / {@code setXxx} —— 框架靠命名推断属性名。
 *
 * <p>注意：如果要在 Activity 之间用 Intent 传这个对象，还需要实现
 * {@code Parcelable} 或 {@code Serializable}，否则 {@code putExtra} 放不进去。
 */
public class Contact {

    /** 姓名 */
    private String name;

    /** 电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 无参构造：给框架（反射）用 */
    public Contact() {
    }

    /**
     * 全参构造：一行创建带值的对象
     *
     * @param name  姓名
     * @param phone 电话
     * @param email 邮箱
     */
    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 打印对象时输出内容而不是 {@code Contact@1a2b3c}
     *
     * <p>调试时 {@code Log.d(TAG, contact.toString())} 能直接看到三个字段的值。
     */
    @Override
    public String toString() {
        return "Contact{name='" + name + "', phone='" + phone + "', email='" + email + "'}";
    }
}
