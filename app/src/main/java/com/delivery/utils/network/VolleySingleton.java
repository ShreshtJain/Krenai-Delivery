package com.delivery.utils.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.delivery.utils.context.ApplicationContext;

public class VolleySingleton {

    private static VolleySingleton sInstance = null;
    private RequestQueue myRequestQueue;
    private ImageLoader mImageLoader;

    private VolleySingleton() {
        myRequestQueue = Volley.newRequestQueue(ApplicationContext.getAppContext());
        mImageLoader = new ImageLoader(myRequestQueue, new ImageLoader.ImageCache() {
            private LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>((int) Runtime.getRuntime().maxMemory() / 1024 / 8);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getsInstance() {
        if (sInstance == null) {
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public RequestQueue getMyRequestQueue() {
        return myRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
