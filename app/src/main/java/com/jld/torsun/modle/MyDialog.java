package com.jld.torsun.modle;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jld.torsun.R;

/**
 * 项目名称：branches
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class MyDialog {

    private boolean isSingle = false;//是否是单个按钮的dialog
    private boolean isTouchCancel = false;//点击屏幕外部不消失
    private String btn_text;//单个按钮dialog按钮文字
    private String cancel_text;
    private String confirm_text;
    private String content_text;
    private String title_text;
    private Activity activity;
    private Dialog dialog;
    private boolean isCancelable = true;

    public boolean isShowClose() {
        return isShowClose;
    }

    public void setShowClose(boolean showClose) {
        isShowClose = showClose;
    }

    private boolean isShowClose;

    public boolean isCancelable() {
        return isCancelable;
    }

    public void setCancelable(boolean cancelable) {
        isCancelable = cancelable;
    }

    public MyDialog(boolean isSingle, Activity activity) {
        this.isSingle = isSingle;
        this.activity = activity;
    }

    public boolean isTouchCancel() {
        return isTouchCancel;
    }

    public void setIsTouchCancel(boolean isTouchCancel) {
        this.isTouchCancel = isTouchCancel;
    }

    public String getBtnText() {
        return btn_text;
    }

    public void setBtnText(String btnText) {
        this.btn_text = btnText;
    }

    public String getCancel() {
        return cancel_text;
    }

    public void setCancel(String cancel) {
        this.cancel_text = cancel;
    }

    public String getConfirm() {
        return confirm_text;
    }

    public void setConfirm(String confirm) {
        this.confirm_text = confirm;
    }

    public String getContent() {
        return content_text;
    }

    public void setContent(String content) {
        this.content_text = content;
    }

    public String getTitle() {
        return title_text;
    }

    public void setTitle(String title) {
        this.title_text = title;
    }

    /**
     * 两个按钮的dialog
     */
    public void showTwo(final TwoOnclick twoOnclick) {

        // 获取布局
        View view = activity.getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(activity, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        dialog.setCancelable(isCancelable);
        // 获取子控件
        Button cancel = (Button) view
                .findViewById(R.id.bt_select_dialog_cancel);
        Button confirm = (Button) view
                .findViewById(R.id.bt_select_dialog_confirm);

        TextView title = (TextView) view
                .findViewById(R.id.tv_select_dialog_title);
        TextView message = (TextView) view
                .findViewById(R.id.tv_select_dialog_message);
        message.setPadding(0, 0, 0, activity.getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);
        if (!isShowClose)
            close.setVisibility(View.GONE);

        message.setGravity(Gravity.CENTER);
        // 初始化控件
        title.setText(title_text);
        message.setText(content_text);
        confirm.setText(confirm_text);
        cancel.setText(cancel_text);

        confirm.setOnClickListener(new View.OnClickListener() {// 清除缓存
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                twoOnclick.confirm_method();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {// 取消
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                twoOnclick.cancel_method();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {// 关闭
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                twoOnclick.close_method();
            }
        });
        dialog.setCancelable(isTouchCancel);

        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /**
     * 一个按钮的dialog
     */
    public void showOne(final OneOnclick oneOnclick) {

        // 获取布局
        View view = activity.getLayoutInflater().inflate(
                R.layout.dialog_update_prompt, null);

        // 设置dialog样式
        dialog = new Dialog(activity, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        dialog.setCancelable(isCancelable);
        // 获取子控件
        Button confirm = (Button) view
                .findViewById(R.id.bt_update_dialog_confirm);
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_updete_dialog_close);
        TextView update_dialog_message = (TextView) view
                .findViewById(R.id.tv_update_dialog_message);
        update_dialog_message.setPadding(0, 0, 0, activity.getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));

        update_dialog_message.setText(content_text);
        confirm.setText(btn_text);

        if (!isShowClose)
            close.setVisibility(View.GONE);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                oneOnclick.confirm_method();
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(isTouchCancel);

        // dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }



    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }

    public interface OneOnclick {
        public void confirm_method();
    }

    public interface TwoOnclick {
        public void cancel_method();

        public void confirm_method();

        public void close_method();
    }
}
