package com.dashwood.photodownloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dashwood.photodownloader.extra.Values;
import com.dashwood.photodownloader.handler.HandlerReturnValue;
import com.dashwood.photodownloader.service.DownloadHttpService;
import com.dashwood.photodownloader.service.HttpsTrustManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoDownloader {
    private RequestQueue requestQueue;
    private final Handler handler = new Handler();
    private final Context context;
    private Drawable imageUnloaded;
    private String url;
    private ImageView imageView;
    private boolean clearCache;
    private int quality = 0;
    private int width = 0, height = 0;

    public PhotoDownloader(Context context, String url, ImageView imageView) {
        this.context = context;
        this.url = url;
        this.imageView = imageView;
        if (requestQueue == null) {
            HttpsTrustManager.allowAllSSL();
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    public PhotoDownloader setImageLoading(Drawable loadingImage, boolean isAnimationRotate) {
        imageView.setImageDrawable(loadingImage);
        if (isAnimationRotate) {
            setAnimationStart();
        }
        return this;
    }

    public PhotoDownloader setImageLoading(int loadingImage, boolean isAnimationRotate) {
        imageView.setImageDrawable(ContextCompat.getDrawable(context, loadingImage));
        if (isAnimationRotate) {
            setAnimationStart();
        }
        return this;
    }

    public PhotoDownloader setUnloadedImage(Drawable imageUnloaded) {
        this.imageUnloaded = imageUnloaded;
        return this;
    }

    public PhotoDownloader setUnloadedImage(int imageUnloaded) {
        this.imageUnloaded = ContextCompat.getDrawable(context, imageUnloaded);
        return this;
    }

    public PhotoDownloader setClearCache(boolean clearCache) {
        this.clearCache = clearCache;
        return this;
    }

    public PhotoDownloader setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public PhotoDownloader setWidthAndHeight(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PhotoDownloader init() {
        downloadPhoto();
        return this;
    }

    private void setAnimationStart() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animaiton_infinite);
        imageView.startAnimation(animation);
    }

    private void setImageQuality(File file, Bitmap bitmap) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) this.width) / width;
        float scaleHeight = ((float) this.height) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void downloadPhoto() {
        String[] urls = url.split("/");
        if (urls[urls.length - 1].equals("null")) {
            return;
        }
        File file = context.getExternalFilesDir(Values.getImageDir());
        file.mkdirs();
        String fileName = HandlerReturnValue.getFileName(url);
        File imgFile = new File(file.getAbsolutePath() + "/" + fileName);
        if (imgFile.exists()) {
            if (clearCache) {
                if (imgFile.delete()) {
                    Log.i("LOG", "clear image from storage");
                }
            } else {
                imageView.clearAnimation();
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (quality != 0) {
                    setImageQuality(imgFile, myBitmap);
                }
                if (width != 0 && height != 0) {
                    myBitmap = getResizedBitmap(myBitmap);
                }
                imageView.setImageBitmap(myBitmap);
                return;
            }
        }
        DownloadHttpService downloadHttpService = new DownloadHttpService(url,
                response -> {
                    try {
                        if (response != null) {
                            FileOutputStream fileStream = new FileOutputStream(imgFile);
                            fileStream.write(response);
                            fileStream.close();
                            handler.post(() -> {
                                imageView.clearAnimation();
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                if (quality != 0) {
                                    setImageQuality(imgFile, myBitmap);
                                }
                                if (width != 0 && height != 0) {
                                    myBitmap = getResizedBitmap(myBitmap);
                                }
                                imageView.setImageBitmap(myBitmap);
                            });
                        } else {
                            setImageUnloaded();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setImageUnloaded();
                    }
                }, error -> {
            setImageUnloaded();
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse == null) {
                Log.e("Error", "your error not in system, network response is Null");
                return;
            }
            Log.e("Error", new String(networkResponse.data));
        });
        requestQueue.add(downloadHttpService);
    }

    private void setImageUnloaded() {
        if (imageUnloaded == null) {
            handler.post(() -> {
                imageView.clearAnimation();
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_image_broken));
            });
        } else {
            handler.post(() -> {
                imageView.clearAnimation();
                imageView.setImageDrawable(imageUnloaded);
            });
        }
    }
}
