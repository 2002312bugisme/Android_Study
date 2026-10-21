package com.zzz.datasharingapplication.pojo;

public class Cart {
    private int id;          // 购物车记录ID
    private int goodsId;     // 关联商品ID
    private String goodsName;// 商品名称（冗余）
    private int quantity;    // 购买数量
    private double price;    // 单价
    // 商品图片路径：购物车表里没有这个字段，是联表查商品表时带出来的，仅内存使用
    private String imagePath;

    // 无参构造
    public Cart() {
    }

    // 全参构造（不含 id，用于新增）
    public Cart(int goodsId, String goodsName, int quantity, double price) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.price = price;
    }

    // 全参构造（含 id，用于查询回显）
    public Cart(int id, int goodsId, String goodsName, int quantity, double price) {
        this.id = id;
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * 取商品图片路径（联表带出来的，只用于展示）
     *
     * @return 图片绝对路径；可能为 null
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * 设置商品图片路径（由查询方法在解析游标时回填）
     *
     * @param imagePath 图片绝对路径
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * 小计金额 = 单价 × 数量
     *
     * @return 该行小计
     */
    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", goodsName='" + goodsName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}