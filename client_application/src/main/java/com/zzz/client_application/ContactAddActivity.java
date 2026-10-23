package com.zzz.client_application;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.pojo.Contact;
import com.zzz.client_application.util.PermissionUtil;
import com.zzz.client_application.util.ToastUtil;

import java.util.ArrayList;

/**
 * 添加联系人页：本类目前只做"控件绑定 + 事件绑定"，业务逻辑留空待实现
 *
 * <p>写联系人需要 {@code Manifest.permission.WRITE_CONTACTS}：
 * 清单里已经声明（{@code <uses-permission>} 只是"我想要"），
 * 运行时还得动态申请一次，否则 {@code ContentResolver.insert} 会抛 SecurityException
 * —— 申请方式参考 {@link PermissionLazyActivity}。
 *
 * <p>对应布局：{@code activity_contact_add.xml}
 */
public class ContactAddActivity extends AppCompatActivity {

    /** 日志 TAG：adb logcat -s zzz 只看本页输出 */
    private static final String TAG = "zzz";

    /**
     * 通讯录组
     */
    private static final String[] PERMISSION_CONTACTS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    /**
     * 单独申请通讯录（重测按钮）
     */
    public static final int REQUEST_CODE_CONTACTS = 1;

    /**
     * 姓名输入框
     */
    private EditText etName;
    /**
     * 电话输入框
     */
    private EditText etPhone;
    /**
     * 邮箱输入框
     */
    private EditText etEmail;
    /**
     * 保存按钮
     */
    private Button btnSave;
    /**
     * 查询按钮
     */
    private Button btnQuery;
    /**
     * 查询结果展示区
     */
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== 绑定控件：id 必须与 activity_contact_add.xml 里逐个对应 =====
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        btnQuery = findViewById(R.id.btn_query);
        tvResult = findViewById(R.id.tv_result);

        PermissionUtil.checkPermission(this, PERMISSION_CONTACTS, REQUEST_CODE_CONTACTS);

        // ===== 绑定事件 =====
        // ⚠️ 每个按钮都要单独 setOnClickListener。
        // 之前只给 btnSave 绑了监听器，却指望在同一个回调里用 view.getId() 区分出 btn_query ——
        // 这不可能成立：那个回调只挂在 btnSave 上，view 永远等于 btnSave，查询分支是死代码。
        btnSave.setOnClickListener(this::onSaveClick);
        btnQuery.setOnClickListener(this::onQueryClick);
    }

    /**
     * 保存按钮的点击回调（业务逻辑待实现）
     *
     * <p>实现思路，仅供参考：
     * 1. 取值：{@code etName.getText().toString().trim()}，姓名做非空校验；
     * 2. 权限：先确认 {@code WRITE_CONTACTS} 已授予（可复用 {@code PermissionUtil}）；
     * 3. 写入：联系人不是"一行记录"，要分两步 insert ——
     * 先往 {@code ContactsContract.RawContacts} 插一条拿到 rawContactId，
     * 再往 {@code ContactsContract.Data} 按 mimetype 分别插姓名 / 电话 / 邮箱
     * （StructuredName / Phone / Email），每行都要带上同一个 rawContactId。
     *
     * @param view 被点击的按钮
     */
    private void onSaveClick(View view) {
        // 这个方法只挂给 btn_save，不需要再用 view.getId() 判断是谁点的
        Contact contact = new Contact();
        contact.setName(etName.getText().toString().trim());
        contact.setPhone(etPhone.getText().toString().trim());
        contact.setEmail(etEmail.getText().toString().trim());

        // 方式一：使用ContentResolver多次写入，每次一个字段
        // addContacts(getContentResolver(), contact);

        // 方式二：批处理方式
        // 每一次操作都是一个 ContentProviderOperation,构建一个操作集合，然后一次性执行
        // 好处是，要么全部成功，要么全部失败，保证了事务的一致性。
        addFullContacts(getContentResolver(), contact);
    }

    /**
     * 查询按钮的点击回调：读出系统通讯录并显示到 tv_result
     *
     * @param view 被点击的按钮
     */
    private void onQueryClick(View view) {
        readPhoneContacts(getContentResolver());
    }

    // 查询通讯录信息：raw_contacts 拿 id → data 表按 raw_contact_id 取姓名/电话/邮箱
    private void readPhoneContacts(ContentResolver contentResolver) {
        StringBuilder result = new StringBuilder();
        int count = 0;

        // try-with-resources：Cursor 用完自动关，避免泄漏
        try (Cursor rawCursor = contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                null,
                null,
                null)) {

            if (rawCursor == null) {
                tvResult.setText("查询失败：provider 返回 null");
                return;
            }

            while (rawCursor.moveToNext()) {
                // raw_contact_id 是 long，用 getLong 取（getInt 在 id 很大时会溢出）
                long rawContactId = rawCursor.getLong(
                        rawCursor.getColumnIndexOrThrow(ContactsContract.RawContacts._ID));

                Contact contact = new Contact();
                try (Cursor dataCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        // projection 只取真正要读的两列，别用 null 拉全部
                        new String[]{ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1},
                        ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[]{String.valueOf(rawContactId)},
                        ContactsContract.Data._ID + " ASC")) {

                    if (dataCursor == null) {
                        continue;
                    }
                    while (dataCursor.moveToNext()) {
                        String mimeType = dataCursor.getString(
                                dataCursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE));
                        String value = dataCursor.getString(
                                dataCursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1));

                        // 常量放前面：mimeType 万一为 null 也不会 NPE
                        if (ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            contact.setName(value);
                        } else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            contact.setPhone(value);
                        } else if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            contact.setEmail(value);
                        }
                    }
                }

                // raw_contacts 里的行不一定有对应的 data（可能是空壳），三个字段全空就跳过。
                // 注意不能只判断 name：只有电话没有姓名的联系人也是有效数据，那样会被漏掉。
                if (contact.getName() == null && contact.getPhone() == null && contact.getEmail() == null) {
                    continue;
                }

                count++;
                Log.d(TAG, contact.toString());
                result.append(count).append(". ").append(contact).append("\n");
            }
        }

        tvResult.setText(count == 0 ? "没有查到联系人" : result.toString());
    }

    private void addFullContacts(ContentResolver contentResolver, Contact contact) {
        // 创建一个插入联系人主记录的内容操作器
        ContentProviderOperation op_raw_contact = ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build();

        // 创建一个插入联系人姓名记录的内容操作器
        ContentProviderOperation op_name = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                // 将第0个操作的id，即 raw_contacts 的 id 作为 data 表中的 raw_contact_id
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Contacts.Data.DATA2, contact.getName())
                .build();

        ContentProviderOperation op_phone = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                // 将第0个操作的id，即 raw_contacts 的 id 作为 data 表中的 raw_contact_id
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Contacts.Data.DATA1, contact.getPhone())
                .withValue(ContactsContract.Contacts.Data.DATA2, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build();

        ContentProviderOperation op_email = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                // 将第0个操作的id，即 raw_contacts 的 id 作为 data 表中的 raw_contact_id
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Contacts.Data.DATA1, contact.getEmail())
                .withValue(ContactsContract.Contacts.Data.DATA2, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(op_raw_contact);
        operations.add(op_name);
        operations.add(op_phone);
        operations.add(op_email);

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
            ToastUtil.show(this, "添加成功");
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    // 往手机通讯录添加一个联系人信息（包括姓名、电话号码、电子邮箱）
    private void addContacts(ContentResolver contentResolver, Contact contact) {
        ContentValues values = new ContentValues();
        // 往 raw_contacts 添加联系人记录，并获取添加后的联系人编号
        Uri uri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(uri);

        ContentValues name = new ContentValues();
        // 关联联系人编号
        name.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        // “名”数据类型
        name.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // “联系人名”
        name.put(ContactsContract.Contacts.Data.DATA2, contact.getName());
        // 往提供器添加联系人的名记录
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, name);

        ContentValues phone = new ContentValues();
        // 关联联系人编号
        phone.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        // “电话号码”数据类型
        phone.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // “联系人电话号码”
        phone.put(ContactsContract.Contacts.Data.DATA1, contact.getPhone());
        // 联系类型。1表示家庭，2表示工作
        phone.put(ContactsContract.Contacts.Data.DATA2, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 往提供器添加联系人的电话号码记录
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phone);

        ContentValues email = new ContentValues();
        // 关联联系人编号
        email.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        // “邮箱”数据类型
        email.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        // “联系人邮箱”
        email.put(ContactsContract.Contacts.Data.DATA1, contact.getEmail());
        // 邮箱类型。1表示家庭，2表示工作
        email.put(ContactsContract.Contacts.Data.DATA2, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        // 往提供器添加联系人的邮箱记录
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, email);

        ToastUtil.show(this, "添加成功");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACTS) {
            if (PermissionUtil.checkGrant(grantResults)) {
                onContactsGranted();
            } else {
                handleDenied("通讯录", PERMISSION_CONTACTS);
            }
        }
    }

    /**
     * 通讯录权限到手后才真正能做的事：查一次系统通讯录，顺便验证权限确实生效
     *
     * <p>Cursor 必须关闭，这里用 try-with-resources 自动关。
     */
    private void onContactsGranted() {
        int count = 0;
        try (Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
            if (cursor != null) {
                count = cursor.getCount();
            }
        }
    }

    /**
     * 单组被拒绝：区分「这次拒绝」和「勾了不再询问」
     *
     * <p>只用于两个单组重测按钮；批量申请的分支因为要给两组分别判断，逻辑写在回调里。
     *
     * @param label       功能名，只用于拼日志和状态文本
     * @param permissions 本次申请的权限组
     */
    private void handleDenied(String label, String[] permissions) {
        if (isPermanentlyDenied(permissions)) {
            // 系统不会再弹窗，只能去设置页手动开
            ToastUtil.show(this, label + "永久拒绝，需到设置里开");
            jumpToSettings();
        }
    }

    /**
     * 是否已被"永久拒绝"（勾了不再询问）
     *
     * <p>判据：{@code shouldShowRequestPermissionRationale} 返回 false。
     * 注意它在"首次申请之前"也返回 false，所以只能在收到拒绝回调后调用。
     */
    private boolean isPermanentlyDenied(String[] permissions) {
        if (PermissionUtil.hasPermission(this, permissions)) {
            return false;
        }
        for (String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED;
            if (!granted && ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 还有能再弹窗的权限，就不算永久拒绝
                return false;
            }
        }
        return true;
    }

    /**
     * 跳到本应用详情页（权限被永久拒绝后唯一的手动开关入口）
     */
    private void jumpToSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        // 必须带 package: 的 data，否则打开的是列表页而不是本应用详情
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }


}
