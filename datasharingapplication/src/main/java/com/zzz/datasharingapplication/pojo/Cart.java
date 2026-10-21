package com.zzz.datasharingapplication.pojo;

/**
 * 购物车条目实体：对应 SQLite 表 {@code cart_info} 的一行
 *
 * <p>演示要点：
 * 1. {@code goodsName} 与 {@code price} 是<b>冗余字段</b>——商品表里已经有了，
 *    这里再存一份，好处是查购物车时不用每次再联表；代价是商品改名/改价后购物车里是旧值。
 *    真实电商系统通常在这种情况下"以商品表为准"，冗余只用于下单快照；
 * 2. {@code id} 是购物车表自己的主键（{@code _id}），<b>不是商品 id</b>；
 *    改数量、删条目都要用它，而不是 goodsId；
 * 3. {@code imagePath} 在表里<b>没有</b>对应列，是 {@code queryAllCart()} 联表查商品表时带出来的，
 *    只用于界面显示。
 *
 * <p>相关文件：{@code database/ShoppingDBHelper#queryAllCart()}、
 * {@code ShoppingCartActivity}（购物车界面）
 */
public class Cart {

    private int id;          // 购物车记录ID（cart_info._id，改删都用它）
    private int goodsId;     // 关联商品ID（cart_info.goods_id）
    private String goodsName;// 商品名称（冗余字段）
    private int quantity;    // 购买数量
    private double price;    // 单价（加购时的价格快照）

    // 商品图片路径：购物车表里没有这个字段，是联表查商品表时带出来的，仅内存使用
    private String imagePath;

    /**
     * 无参构造：给"先 new、再逐字段 set"的场景用（例如由 Cursor 逐列填充）
     */
    public Cart() {
    }

    /**
     * 全参构造（不含 id）：用于新增一条购物车记录，主键交给数据库自增
     *
     * @param goodsId   关联的商品ID
     * @param goodsName 商品名称
     * @param quantity  购买数量
     * @param price     单价
     */
    public Cart(int goodsId, String goodsName, int quantity, double price) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * 全参构造（含 id）：用于查询回显
     *
     * @param id        购物车记录主键
     * @param goodsId   关联的商品ID
     * @param goodsName 商品名称
     * @param quantity  购买数量
     * @param price     单价
     */
    public Cart(int id, int goodsId, String goodsName, int quantity, double price) {
        this.id = id;
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.price = price;
    }

    /** @return 购物车记录主键 */
    public int getId() {
        return id;
    }

    /** @param id 购物车记录主键 */
    public void setId(int id) {
        this.id = id;
    }

    /** @return 关联的商品ID */
    public int getGoodsId() {
        return goodsId;
    }

    /** @param goodsId 关联的商品ID */
    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    /** @return 商品名称（冗余字段） */
    public String getGoodsName() {
        return goodsName;
    }

    /** @param goodsName 商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** @return 购买数量 */
    public int getQuantity() {
        return quantity;
    }

    /** @param quantity 购买数量（≤0 时业务层应删行，而不是存 0） */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** @return 单价 */
    public double getPrice() {
        return price;
    }

    /** @param price 单价 */
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
     * <p>放在实体里而不是适配器里算，是为了让"金额怎么来的"只有一处定义。
     *
     * @return 该行小计（未做四舍五入，显示时再格式化）
     */
    public double getSubtotal() {
        return price * quantity;
    }

    /**
     * 输出全部字段，方便 Log 打印与调试
     *
     * @return 形如 {@code Cart{id=1, goodsId=1, goodsName='...', quantity=2, ...}} 的文本
     */
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
