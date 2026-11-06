package com.zzz.retrofitapplication.data.model;

/**
 * 业务实体，同时充当「POST 请求体」和「响应里的 data」。
 *
 * <p>它被 {@code @Body} 标注的参数使用时，GsonConverterFactory 会把它序列化成
 * {"name":"王五","age":22}；作为响应类型时又会被反序列化回来。
 *
 * <p>注意 Gson 反序列化**不需要无参构造**：它绕过构造器直接给字段赋值（Unsafe 方式），
 * 所以字段名必须和 JSON 的 key 一致，否则拿到 null。
 */
public class User {

    private String name;
    private int age;

    /**
     * @param name 姓名
     * @param age  年龄
     */
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', age=" + age + "}";
    }
}
