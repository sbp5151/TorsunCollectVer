package zhy.imageloader;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.jld.torsun.R;
import com.jld.torsun.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import zhy.utils.CommonAdapter;
import zhy.utils.ViewHolder;

public class MyAdapter extends CommonAdapter<String> {

    /**
     * 用户选择的图片，存储为图片的完整路径
     */
    public static List<String> mSelectedImage = new ArrayList<>();

    /**
     * 文件夹路径
     */
    private String mDirPath;
    private Handler mHandler;
    private Context context;

    public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
                     String dirPath, Handler mHandler) {
        super(context, mDatas, itemLayoutId);
        this.mDirPath = dirPath;
        this.mHandler = mHandler;
        this.context = context;
    }

    @Override
    public void convert(final ViewHolder helper, final String item) {
        //设置no_pic
        helper.setImageResource(R.id.id_item_image, R.mipmap.pictures_no);
        //设置no_selected
        helper.setImageResource(R.id.id_item_select,
                R.mipmap.picture_unselected);
        //设置图片
        helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);

        final ImageView mImageView = helper.getView(R.id.id_item_image);
        final ImageView mSelect = helper.getView(R.id.id_item_select);

        mImageView.setColorFilter(null);
        //设置ImageView的点击事件
        mImageView.setOnClickListener(new OnClickListener() {
            //选择，则将图片变暗，反之则反之
            @Override
            public void onClick(View v) {


                // 已经选择过该图片
                if (mSelectedImage.contains(mDirPath + "/" + item)) {
                    mSelectedImage.remove(mDirPath + "/" + item);
                    mSelect.setImageResource(R.mipmap.picture_unselected);
                    mImageView.setColorFilter(null);
                    Message message = mHandler.obtainMessage();
                    message.what = 0x120;
                    message.arg1 = mSelectedImage.size();
                    mHandler.sendMessage(message);
                } else if (mSelectedImage.size() >= 5) {
                    ToastUtil.showToast(context, context.getResources().getString(R.string.message_add_photo_hint), 3000);
                } else {
                    // 未选择该图片
                    mSelectedImage.add(mDirPath + "/" + item);
                    mSelect.setImageResource(R.mipmap.pictures_selected);
                    mImageView.setColorFilter(Color.parseColor("#77000000"));
                    Message message = mHandler.obtainMessage();
                    message.what = 0x120;
                    message.arg1 = mSelectedImage.size();
                    mHandler.sendMessage(message);
                }

            }
        });

        /**
         * 已经选择过的图片，显示出选择过的效果
         */
        if (mSelectedImage.contains(mDirPath + "/" + item)) {
            mSelect.setImageResource(R.mipmap.pictures_selected);
            mImageView.setColorFilter(Color.parseColor("#77000000"));
        }

    }

    public List<String> getImageList() {
        return mSelectedImage;
    }
}
