package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.adapter.SingleChoiceAdapter;
import com.jld.torsun.modle.TrouTeam;

import java.util.List;

/**
 * Created by lz on 2016/5/25.
 */
public class CustomChoiceDialog extends Dialog {

    public CustomChoiceDialog(Context context) {
        super(context);
    }

    public CustomChoiceDialog(Context context,int theme) {
        super(context,theme);
    }

    public static class Builder{

        private Context context;
        private String title;
        private String negativeBtnText;
        private List<TrouTeam> data;
        private int icon;
        protected ListView mListView;
        private OnClickListener negativeBtnOnClickListener;
        private OnItemClickListener onItemClickListener;
        private SingleChoiceAdapter adapter;
        private int currIndex;
        private boolean canceledOnTouchOutside = false;
        private boolean cancelable = false;

        public Builder(Context context){
            this.context = context;
        }

        public Builder setItems(List<TrouTeam> data ,int index){
            this.data = data;
            currIndex = index;
            adapter = new SingleChoiceAdapter(context,data);
            return this;
        }

        public Builder setItems(List<TrouTeam> data){
            this.data = data;
            adapter = new SingleChoiceAdapter(context,data);
            return this;
        }

        public int getSelectedIndex(){
            return currIndex;
        }

        public Builder setTitle(int title){
            this.title  = (String) context.getText(title);
            return this;
        }

        public Builder setTitle(String title){
            this.title  = title;
            return this;
        }

        public Builder setIcon(int iconId){
            this.icon = iconId;
            return this;
        }

        public Builder setCancelable(boolean cancelable){
            this.cancelable = cancelable;
            return this;
        }

        public Builder setcanceledOnTouchOutside(boolean canceledOnTouchOutside){
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder setMyOnItemClickListener(OnItemClickListener listener){
            this.onItemClickListener = listener;
            return this;
        }

        public Builder setNegativeBtn(int negativeBtnText,OnClickListener listener){
            this.negativeBtnText = (String)context.getText(negativeBtnText);
            this.negativeBtnOnClickListener = listener;
            return this;
        }

        public Builder setNegativeBtn(String negativeBtnText,OnClickListener listener){
            this.negativeBtnText = negativeBtnText;
            this.negativeBtnOnClickListener = listener;
            return this;
        }

        public CustomChoiceDialog create(){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomChoiceDialog dialog = new CustomChoiceDialog(context,R.style.CustomDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            dialog.setCancelable(cancelable);
            View layout = inflater.inflate(R.layout.custom_choice_dialog_layout,null);
//            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
//            layout.setMinimumWidth(dm.widthPixels - 80);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (icon != 0){
                ((ImageView) layout.findViewById(R.id.custom_choice_dialog_icon)).setImageResource(icon);
            }else {
                layout.findViewById(R.id.custom_choice_dialog_icon).setVisibility(View.INVISIBLE);
            }
            if (title != null){
                ((TextView) layout.findViewById(R.id.custom_choice_dialog_title)).setText(title);
            }else {
                layout.findViewById(R.id.custom_choice_dialog_title).setVisibility(View.GONE);
            }
            if (negativeBtnText != null){
                ((Button) layout.findViewById(R.id.custom_choice_dialog_negativeButton)).setText(negativeBtnText);
                if (negativeBtnOnClickListener != null){
                    ((Button) layout.findViewById(R.id.custom_choice_dialog_negativeButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            negativeBtnOnClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            }else {
                layout.findViewById(R.id.custom_choice_dialog_negativeButton).setVisibility(View.INVISIBLE);
            }
            if (data != null){
                mListView = (ListView) layout.findViewById(R.id.custom_choice_dialog_listView);
                mListView.setAdapter(adapter);
                mListView.setItemChecked(0, true);
                mListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        currIndex = position;
                        onItemClickListener.onItemClick(parent, view, position, id);
                        mListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        },1);
                    }
                });
            }
            dialog.setContentView(layout);
            if (context instanceof Activity){
                Window window = dialog.getWindow();
                WindowManager m = ((Activity)context).getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams p = window.getAttributes();
                p.width = (int)(d.getWidth() * (85.0 / 108.0));
                p.height = (int)(d.getHeight() * (5.0 / 8.0));
                window.setAttributes(p);
            }


            return dialog;
        }
    }

}
