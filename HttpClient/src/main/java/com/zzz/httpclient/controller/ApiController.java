package com.zzz.httpclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OkHttp 演示用服务端接口。
 *
 * <p>对应客户端：D:\Code\Android\okhttpapplication（OkHttpActivity）。
 * <p>统一走 /api 前缀，接口与客户端按钮一一对应：
 * <ul>
 *   <li>GET  /api/get    —— 查询参数，客户端演示同步（execute）/异步（enqueue）</li>
 *   <li>GET  /api/path/{category} —— 路径变量，对应 Retrofit 的 @Path</li>
 *   <li>GET  /api/header —— 自定义请求头，对应 Retrofit 的 @Header</li>
 *   <li>POST /api/post   —— JSON 请求体，演示 RequestBody</li>
 *   <li>POST /api/form   —— 表单请求体，演示 FormBody</li>
 *   <li>POST /api/upload —— multipart/form-data，演示文件上传</li>
 * </ul>
 *
 * <p>返回值故意用 Map 而不是自定义实体类：字段名即所见，方便和客户端日志对照，
 * 也免得为演示再写一堆 getter/setter。
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    /** 时间只取时分秒，日志里看起来够用了 */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 上传文件的落盘根目录：D:/Code/Android/HttpClient/upload。
     *
     * <p>写绝对路径而不是项目内的相对路径：从 IDEA 启动和命令行 java -jar 启动的「工作目录」不同，
     * 相对路径会分别落到两个地方，事后根本找不到文件。
     *
     * <p>目录不存在会自动创建（{@link Files#createDirectories}），不用手动先建。
     */
    private static final Path UPLOAD_ROOT = Paths.get("D:/Code/Android/HttpClient/upload");

    /**
     * 按天分的子目录名，例如 upload/2026-11-04/。
     *
     * <p>日期是**每次请求时**取的（不是启动时算一次），所以在服务器不重启的情况下跨过零点，
     * 新文件也会自动落到第二天的目录里。
     */
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * GET 请求：参数从 URL 的查询串里取，例如 /api/get?name=张三&age=18
     *
     * @param name 姓名，URL 里没带就用默认值（defaultValue 让参数变成可选）
     * @param age  年龄，Spring 会自动把字符串 "18" 转成 int
     * @return 统一响应体，data 里回显收到的那两个参数
     */
    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam(defaultValue = "张三") String name,
                                  @RequestParam(defaultValue = "18") int age) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("age", age);
        return ok("GET 成功：参数从 URL 查询串解析", data);
    }

    /**
     * GET 路径变量：参数写在 URL 路径里（/api/path/服装），不是查询串。
     *
     * <p>对应 Retrofit 的 {@code @Path}。中文会被客户端编码成 %E5%BC%A0...，
     * Spring 收到后自动解码，所以这里拿到的是原文，不用自己 URLDecoder。
     *
     * @param category 路径里那一段，@PathVariable 按名字匹配（方法和占位符同名时可省略）
     * @return 统一响应体
     */
    @GetMapping("/path/{category}")
    public Map<String, Object> path(@PathVariable String category) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("category", category);
        data.put("from", "URL 路径段，不是查询串");
        return ok("GET 成功：路径变量已解码", data);
    }

    /**
     * GET 自定义请求头：客户端用 Retrofit 的 {@code @Header} 加，服务端用 @RequestHeader 接。
     *
     * <p>头名大小写不敏感，但写错名字不会报错，只会读到 defaultValue——比查询参数更难查。
     *
     * @param token   自定义头 token；没传时给个占位说明，便于在客户端日志里看出来
     * @param traceId 自定义头 X-Trace-Id，required=false 表示可以不传，不传就是 null
     * @return 统一响应体，data 里回显读到的头
     */
    @GetMapping("/header")
    public Map<String, Object> header(
            @RequestHeader(value = "token", defaultValue = "（没有传 token）") String token,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("traceId", traceId);
        return ok("GET 成功：从请求头读到自定义字段", data);
    }

    /**
     * POST 请求：请求体是 JSON，靠 @RequestBody 反序列化成 Map。
     * 客户端必须发 Content-Type: application/json，否则这里会报 415（不支持的媒体类型）。
     *
     * @param body 请求体反序列化后的键值对，原样回显
     * @return 统一响应体
     */
    @PostMapping("/post")
    public Map<String, Object> post(@RequestBody Map<String, Object> body) {
        return ok("POST 成功：@RequestBody 解析 JSON", body);
    }

    /**
     * POST 表单：Content-Type 是 application/x-www-form-urlencoded，
     * 参数不在 URL 上，而在请求体里（name=李四&amp;age=20 这种形式）。
     *
     * @param form 表单字段，用 Map 一次性接住所有键值，省得一个个写 @RequestParam
     * @return 统一响应体
     */
    @PostMapping("/form")
    public Map<String, Object> form(@RequestParam Map<String, String> form) {
        return ok("表单接收成功：application/x-www-form-urlencoded", form);
    }

    /**
     * POST 文件上传：Content-Type 是 multipart/form-data。
     * 客户端用 MultipartBody 拼装，每个 part 对应这里的一个 @RequestParam。
     *
     * <p>落盘位置：{@code D:/Code/Android/HttpClient/upload/<yyyy-MM-dd>/<毫秒时间戳>_<原文件名>}。
     *
     * @param file 文件字段，字段名必须是 file（和客户端 addFormDataPart 的第一个参数一致）
     * @param desc 附带的普通文本字段，演示「文件 + 文本」混在同一个表单里
     * @return 统一响应体，data 里给出文件名、大小、类型和落盘路径
     * @throws IOException 写磁盘失败时抛出
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "desc", defaultValue = "") String desc)
            throws IOException {
        if (file.isEmpty()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("originalFilename", file.getOriginalFilename());
            data.put("size", 0);
            return fail(400, "上传的文件是空的", data);
        }

        // 今天的目录：upload/2026-11-04，不存在就建（第一次上传、或跨过零点时走到这里）
        Path dayDir = UPLOAD_ROOT.resolve(LocalDate.now().format(DAY_FORMAT));
        Files.createDirectories(dayDir);
        // 原始文件名可能重复，前面拼毫秒时间戳避免覆盖
        String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String savedName = System.currentTimeMillis() + "_" + originalName;
        Path savedPath = dayDir.resolve(savedName);
        // transferTo 会把临时文件（Spring 存磁盘的临时副本）搬到目标位置
        file.transferTo(savedPath);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("originalFilename", originalName);
        data.put("contentType", file.getContentType());
        data.put("size", file.getSize());
        data.put("desc", desc);
        data.put("savedTo", savedPath.toString());
        return ok("文件接收成功，已保存到 " + savedPath, data);
    }

    /**
     * 成功响应的统一外壳。
     *
     * @param msg  给人看的提示
     * @param data 业务数据，可以是 Map / List / 任何能序列化成 JSON 的对象
     * @return code=200 的响应体
     */
    private Map<String, Object> ok(String msg, Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("msg", msg);
        result.put("data", data);
        // 打上服务端时间和线程名：客户端连的是不是这台机器、请求有没有并发，一眼能看出来
        result.put("serverTime", LocalTime.now().format(TIME_FORMAT));
        result.put("serverThread", Thread.currentThread().getName());
        return result;
    }

    /**
     * 失败响应，结构保持和成功一致，客户端解析逻辑不用分支。
     *
     * @param code 业务错误码（这里直接复用 HTTP 语义的 400）
     * @param msg  错误说明
     * @param data 出错时的上下文
     * @return code=400 的响应体
     */
    private Map<String, Object> fail(int code, String msg, Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", data);
        result.put("serverTime", LocalTime.now().format(TIME_FORMAT));
        result.put("serverThread", Thread.currentThread().getName());
        return result;
    }
}
