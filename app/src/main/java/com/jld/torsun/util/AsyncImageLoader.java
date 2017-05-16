package com.jld.torsun.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by lz on 2016/2/24.
 */
public class AsyncImageLoader {

    //SoftReference是软引用，是为了更好的为了系统回收变量
    //private HashMap<String, SoftReference<Drawable>> imageCache;
    private HashMap<String, SoftReference<Bitmap>> imageCache;

    public AsyncImageLoader() {
        //imageCache = new HashMap<String, SoftReference<Drawable>>();
        imageCache = new HashMap<String, SoftReference<Bitmap>>();
    }

    //public Drawable loadDrawable(final String imageUrl,final ImageView imageView, final ImageCallback imageCallback){
    public Bitmap loadDrawable(final String imageUrl,final ImageView imageView, final ImageCallback imageCallback){
        if (imageCache.containsKey(imageUrl)) {
            //从缓存中获取
            //SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
            //Drawable drawable = softReference.get();
            Bitmap bitmap = softReference.get();
//            if (drawable != null) {
//                return drawable;
//            }
            if (bitmap != null){
                return bitmap;
            }
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                //imageCallback.imageLoaded((Drawable) message.obj, imageView,imageUrl);
                imageCallback.imageLoaded((Bitmap) message.obj, imageView,imageUrl);
            }
        };
        //建立新一个新的线程下载图片
        new Thread() {
            @Override
            public void run() {
                //Drawable drawable = loadImageFromUrl(imageUrl);
                Bitmap bitmap = loadImageFromUrl(imageUrl);
//                imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
//                Message message = handler.obtainMessage(0, drawable);
                imageCache.put(imageUrl, new SoftReference<Bitmap>(bitmap));
                Message message = handler.obtainMessage(0, bitmap);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    //public static Drawable loadImageFromUrl(String url){
    public static Bitmap loadImageFromUrl(String url){
        URL m;
        InputStream i = null;
        try {
            m = new URL(url);
            i = (InputStream) m.getContent();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //对获取到的数据流转成bitmap再模糊处理
        Bitmap b = BitmapFactory.decodeStream(i);
        //ImageUtil.fastblur(this,b,25);
       // i=Bitmap2IS(b);
       // Drawable d = Drawable.createFromStream(i, "src");
        return b;
    }

    public static InputStream Bitmap2IS(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
        return sbs;
    }

    //回调接口
    public interface ImageCallback {
        //public void imageLoaded(Drawable imageDrawable,ImageView imageView, String imageUrl);
        public void imageLoaded(Bitmap imageBitmap, ImageView imageView, String imageUrl);
    }
}
