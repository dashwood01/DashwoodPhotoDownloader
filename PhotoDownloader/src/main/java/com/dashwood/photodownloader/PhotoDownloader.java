package com.dashwood.photodownloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private Drawable imageUnloaded, loadingImage;
    private String url;
    private ImageView imageView;
    private boolean clearCache;
    private int quality = 0;
    private int width = 0, height = 0;
    private File filePath = null;
    private Animation animation;

    /**
     * You must set all this items, be careful all of this items can't be null
     *
     * @param context
     * @param url
     * @param imageView
     */
    public PhotoDownloader(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        this.context = context;
        this.url = url;
        this.imageView = imageView;
        if (requestQueue == null) {
            HttpsTrustManager.allowAllSSL();
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    public PhotoDownloader setImageLoading(Drawable loadingImage, Animation animation) {
        this.loadingImage = loadingImage;
        this.animation = animation;
        return this;
    }

    public PhotoDownloader setImageLoading(int loadingImage, Animation animation) {
        this.loadingImage = ContextCompat.getDrawable(context, loadingImage);
        this.animation = animation;
        return this;
    }

    public PhotoDownloader setImageLoading(int loadingImage, boolean isAnimationRotate) {
        this.loadingImage = ContextCompat.getDrawable(context, loadingImage);
        if (isAnimationRotate) {
            setAnimation();
        }
        return this;
    }

    public PhotoDownloader setImageLoading(Drawable loadingImage, boolean isAnimationRotate) {
        this.loadingImage = loadingImage;
        if (isAnimationRotate) {
            setAnimation();
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

    public PhotoDownloader clearCache() {
        this.clearCache = true;
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

    public PhotoDownloader setFilePath(File filePath) {
        this.filePath = filePath;
        return this;
    }

    public void init() {
        downloadPhoto();
    }


    private void downloadPhoto() {
        String[] urls = url.split("/");
        if (urls[urls.length - 1].equals("null")) {
            return;
        }
        File file = filePath;
        if (filePath == null) {
            file = new File(context.getExternalCacheDir() + "/" + Values.getImageDir());
        }
        if (file.mkdirs()) {
            Log.i("LOG", "Directory created");
        }
        String fileName = HandlerReturnValue.getFileName(url);
        File imgFile = new File(file.getAbsolutePath() + "/" + fileName);
        if (loadingImage == null) {
            loadingImage = ContextCompat.getDrawable(context, R.drawable.ic_loading_photo_downloader);
            setAnimation();
        }
        imageView.setImageDrawable(loadingImage);
        if (animation != null) {
            imageView.startAnimation(animation);
        }
        if (imgFile.exists()) {
            if (clearCache) {
                if (imgFile.delete()) {
                    Log.i("LOG", "clear image from storage");
                }
            } else {
                imageView.clearAnimation();
                Bitmap photoBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (quality != 0) {
                    setImageQuality(imgFile, photoBitmap);
                }
                if (width != 0 && height != 0) {
                    photoBitmap = HandlerReturnValue.getResizedBitmap(photoBitmap, width, height);
                }
                imageView.setImageBitmap(photoBitmap);
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
                                if (animation != null) {
                                    imageView.clearAnimation();
                                }
                                Bitmap photoBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                checkQuality(imgFile, photoBitmap);
                                photoBitmap = getResizeBitmap(photoBitmap);
                                imageView.setImageBitmap(photoBitmap);
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

    private void checkQuality(File file, Bitmap bitmap) {
        if (quality != 0) {
            setImageQuality(file, bitmap);
        }
    }

    private Bitmap getResizeBitmap(Bitmap bitmap) {
        if (width != 0 && height != 0) {
            return HandlerReturnValue.getResizedBitmap(bitmap, width, height);
        }
        return bitmap;
    }

    private void setImageUnloaded() {
        if (imageUnloaded == null) {
            handler.post(() -> {
                if (animation != null) {
                    imageView.clearAnimation();
                }
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_image_broken));
            });
        } else {
            handler.post(() -> {
                if (animation != null) {
                    imageView.clearAnimation();
                }
                imageView.setImageDrawable(imageUnloaded);
            });
        }
    }

    private void setAnimation() {
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animaiton_infinite);
    }

    private void setImageQuality(File file, Bitmap bitmap) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
