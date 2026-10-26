package com.zzz.client_application.pojo;

import android.net.Uri;

public class ImageItem {

    /**
     * 对应 MediaStore.Images.Media._ID
     */
    private long id;

    /**
     * 对应 MediaStore.Images.Media.TITLE
     */
    private String title;

    /**
     * 对应 MediaStore.Images.Media.SIZE (单位: 字节)
     */
    private long size;

    /**
     * 对应 MediaStore.Images.Media.DATA (文件绝对路径，已废弃但兼容旧版)
     */
    private String data;

    public ImageItem() {
    }

    public ImageItem(long id, String title, long size, String data) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * 根据 ID 生成 content Uri（推荐代替 DATA 使用）
     */
    public Uri getContentUri() {
        return Uri.withAppendedPath(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                String.valueOf(id));
    }

    @Override
    public String toString() {
        return "ImageItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", size=" + size +
                ", data='" + data + '\'' +
                '}';
    }
}