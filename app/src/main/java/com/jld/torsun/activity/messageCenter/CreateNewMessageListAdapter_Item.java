package com.jld.torsun.activity.messageCenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;

import java.util.ArrayList;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class CreateNewMessageListAdapter_Item extends BaseAdapter {

    public static final String TAG = "CreateNewMessageListAdapter_Item";
    public Context context;
    public ArrayList<String> images = new ArrayList<>();
    private Handler choosePhotoHandler;
    private int index;

    public CreateNewMessageListAdapter_Item(ArrayList<String> images, Context context, Handler handler, int position) {
        this.context = context;
        choosePhotoHandler = handler;
        this.images = images;
        index = position;
    }

    @Override
    public int getCount() {
        LogUtil.d(TAG, "getCount" + images.size());
        int ret = 0;
        if (images != null)
            ret = images.size();
        return ret;
    }

    @Override
    public Object getItem(int position) {
        LogUtil.d(TAG, "getItem" + position);

        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        LogUtil.d(TAG, "position" + position);

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View ret = null;
        LogUtil.d(TAG, "position" + position);
        LogUtil.d(TAG, "images" + images.size());
        final ViewHold hold;
        if (convertView == null) {
            hold = new ViewHold();
            ret = LayoutInflater.from(context).inflate(R.layout.item_create_message_center_list_layout_item, null);
            hold.iv_iamge = (ImageView) ret.findViewById(R.id.iv_item_message_center_list_img_item);
            hold.iv_delete = (ImageView) ret.findViewById(R.id.iv_delete_icon_item);
            hold.iv_iamge.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    hold.iv_delete.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.guide_like_anim);
                    hold.iv_delete.startAnimation(animation);
                    return true;
                }
            });
            hold.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hold.iv_delete.setVisibility(View.GONE);
                    Message message = Message.obtain();
                    message.what = CreateNewMessageActivity.IMAGE_DELETE;
                    message.arg1 = index;
                    message.arg2 = position;
                    choosePhotoHandler.sendMessage(message);
                }
            });
            hold.iv_iamge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (index == 0) {
                        //弹出选择图片对话框
                        Message message = Message.obtain();
                        message.what = CreateNewMessageActivity.CHOOSEPHOTO_MSG;
                        message.arg1 = index;
                        choosePhotoHandler.sendMessage(message);
                    }
                }
            });
            ret.setTag(hold);
        } else {
            ret = convertView;
            hold = (ViewHold) convertView.getTag();
        }
        if (images.get(position) != null) {
            hold.iv_iamge.setImageBitmap(compressImageFromFile(images.get(position)));
        }
        return ret;
    }

    public void addImages(String bitmap) {
        if (bitmap != null) {
            images.add(bitmap);
            notifyDataSetChanged();
        }
    }

    public void addImages(ArrayList<String> bitmaps) {
        if (bitmaps != null) {
            images.addAll(bitmaps);
            notifyDataSetChanged();
        }
    }

    public class ViewHold {
        ImageView iv_iamge;
        ImageView iv_delete;
    }

    private Bitmap compressImageFromFile(String srcPath) {
        LogUtil.d(TAG, "srcPath" + srcPath);

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;//
        float ww = 480f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置采样率
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

}
