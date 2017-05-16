package com.jld.torsun.modle;

import android.graphics.Bitmap;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class Message_Photo_Item {

    public Bitmap bitmap;
    public Boolean isSetBitmap = false;

    public Message_Photo_Item() {
    }

    public Message_Photo_Item(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Message_Photo_Item(Bitmap bitmap, Boolean isSetBitmap) {
        this.bitmap = bitmap;
        this.isSetBitmap = isSetBitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Boolean getIsSetBitmap() {
        return isSetBitmap;
    }

    public void setIsSetBitmap(Boolean isSetBitmap) {
        this.isSetBitmap = isSetBitmap;
    }
}
