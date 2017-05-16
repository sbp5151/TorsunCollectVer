package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.jld.torsun.R;
import com.jld.torsun.modle.CreateSendMessageItem;
import com.jld.torsun.util.LogUtil;

import java.util.ArrayList;

//import com.android.volley.RequestQueue;

/**
 * @author liuzhi
 * @ClassName: CreateNewMessageListAdapter
 * @Description: 创建新信息界面的listAdapter
 * @date 2016-3-17
 */
public class CreateNewMessageListAdapter extends BaseAdapter {
    private Activity mActivity;
    public static final String TAG = "CreateNewMessageListAdapter";

    private Handler choosePhotoHandler;
    public ArrayList<CreateSendMessageItem> items = new ArrayList<>();
    private CreateNewMessageListAdapter_Item adapter_item;
    private String describe;

    public CreateNewMessageListAdapter(Activity activity, Handler handler) {
        mActivity = activity;
        choosePhotoHandler = handler;
    }

    @Override
    public int getCount() {
        if (null != items && items.size() > 0) {
            return items.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int postion) {
        if (null != items && items.size() > 0) {
            return items.get(postion);
        }
        return null;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }


    @Override
    public View getView(final int postion, View contentView, ViewGroup arg2) {
        final CreateSendMessageItem item = items.get(postion);
        LogUtil.d(TAG, "position:" + postion + "````item:" + item.toString());
        View ret = null;
        ViewHold hold = null;
        if (contentView == null) {
            hold = new ViewHold();
            ret = LayoutInflater.from(mActivity).inflate(R.layout.item_create_message_center_list_layout, null);
            hold.lv_view = (ListView) ret.findViewById(R.id.lv_message_photo_item);
            hold.iv_add_photo = (ImageView) ret.findViewById(R.id.iv_message_add_photo);
            hold.et_photo_describe = (EditText) ret.findViewById(R.id.et_message_photo_describe);

            //相片描述设置内容监听
            final ViewHold finalHold = hold;
            hold.et_photo_describe.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {

                    describe = s.toString();
                    int position = (int) finalHold.et_photo_describe.getTag();
                    items.get(position).setImageDescribe(describe);
                    if (position == (items.size() - 1))
                        setMessageItem(postion);
                }
            });
            ret.setTag(hold);
        } else {
            hold = (ViewHold) contentView.getTag();
            ret = contentView;
        }
        hold.et_photo_describe.setTag(postion);
        if (TextUtils.isEmpty(items.get(postion).getImageDescribe())) {
            hold.et_photo_describe.setText("");
        } else {
            hold.et_photo_describe.setText(items.get(postion).getImageDescribe());
        }
        adapter_item = new CreateNewMessageListAdapter_Item(item.getPath(), mActivity, choosePhotoHandler, postion);
        hold.lv_view.setAdapter(adapter_item);
        //设置添加图片按钮、
        if (item.getPath() != null && item.getPath().size() > 0) {
            //如果有图片则不显示添加图片按钮
            hold.iv_add_photo.setVisibility(View.GONE);
        } else {
            //如果没有图片隐藏图片按钮
            hold.iv_add_photo.setVisibility(View.VISIBLE);
        }
        hold.iv_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出选择图片对话框
                Message message = Message.obtain();
                message.what = CreateNewMessageActivity.CHOOSEPHOTO_MSG;
                message.arg1 = postion;
                choosePhotoHandler.sendMessage(message);
            }
        });
        return ret;
    }

    class ViewHold {
        EditText et_photo_describe;//图片描述
        ListView lv_view;//图片集合
        ImageView iv_add_photo;//添加按钮
    }

    /***
     * 当相片描述不为空时显示“添加图片”
     *
     * @param position
     */
    private void setMessageItem(int position) {
        //描述不为空
        if (!TextUtils.isEmpty(describe)) {
            Message message = Message.obtain();
            message.what = CreateNewMessageActivity.DESCRIBE_CHANGE;
            message.arg1 = 1;
            choosePhotoHandler.sendMessage(message);
            //描述为空
        } else {
            Message message = Message.obtain();
            message.what = CreateNewMessageActivity.DESCRIBE_CHANGE;
            message.arg1 = 2;
            choosePhotoHandler.sendMessage(message);
        }
    }

    public void addItem() {
        items.add(new CreateSendMessageItem());
        notifyDataSetChanged();
    }

    public void addList(ArrayList<CreateSendMessageItem> list) {
        items.addAll(list);
        notifyDataSetChanged();
    }

    public void addImage(String path, int position) {
        LogUtil.d(TAG, "``````````addImage" + position);
        LogUtil.d(TAG, "``````````addImage" + path);
        CreateSendMessageItem item = items.get(position);
        item.getPath().clear();
        item.getPath().add(path);
        LogUtil.d(TAG, item.getPath().size() + "");
        if (position == 0 || position == (items.size() - 1)) {//只有在封面或者最后一个才发送
            Message message = Message.obtain();
            message.what = CreateNewMessageActivity.ADD_IMAGE;
            message.arg1 = position;
            choosePhotoHandler.sendMessage(message);
        }
        notifyDataSetChanged();
    }

    public void addImages(ArrayList<String> paths, int position) {
        LogUtil.d(TAG, "``````````paths" + paths);

        CreateSendMessageItem item = items.get(position);
        item.getPath().addAll(paths);
        LogUtil.d(TAG, "`````````item.getPath():" + item.getPath());
        if (position == 0 || position == (items.size() - 1)) {//只有在封面或者最后一个才发送
            Message message = Message.obtain();
            message.what = CreateNewMessageActivity.ADD_IMAGE;
            message.arg1 = position;
            choosePhotoHandler.sendMessage(message);
        }
        notifyDataSetChanged();
    }

    public ArrayList<CreateSendMessageItem> getData() {
        return items;
    }

    private void changeViewHeigh(ImageView view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) ((float) view.getWidth() / view.getDrawable().getMinimumWidth() * view.getDrawable().getMinimumHeight());
        view.setLayoutParams(layoutParams);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, 1000);
    }
}
