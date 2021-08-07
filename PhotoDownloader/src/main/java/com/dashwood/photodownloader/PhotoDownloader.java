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
import com.dashwood.photodownloader.data.Data;
import com.dashwood.photodownloader.extra.Values;
import com.dashwood.photodownloader.handler.HandlerCheckValue;
import com.dashwood.photodownloader.handler.HandlerReturnValue;
import com.dashwood.photodownloader.listener.CallBackDownloader;
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
    private int timeAsMin = TIME_CLEAR_IMAGE_DEFAULT;
    public static final int TIME_CLEAR_IMAGE_DEFAULT = -1;
    public static final int TIME_OF_CLEAR_IMAGE_INFINITE = -2;
    private File filePath = null;
    private Animation animation;
    private CallBackDownloader callBackDownloader;
    private String saveDate;

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
        this.saveDate = Data.readPreferencesString(context,
                context.getString(R.string.PREFERENCE_HOME_DATE), "", context.getString(R.string.PREFERENCE_KEY_DATE));
        if (HandlerCheckValue.checkEmptyOrNullValue(saveDate)) {
            saveDate = HandlerReturnValue.getNowDate();
            Data.saveToPreferenceString(context, context.getString(R.string.PREFERENCE_HOME_DATE),
                    saveDate, context.getString(R.string.PREFERENCE_KEY_DATE));
        }

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

    public PhotoDownloader setCallBackDownloader(CallBackDownloader callBackDownloader) {
        this.callBackDownloader = callBackDownloader;
        return this;
    }

    /**
     * This function help you for delete files
     *
     * @param timeAsMin you can set TIME_CLEAR_IMAGE_DEFAULT mean delete files every hour or TIME_OF_CLEAR_IMAGE_INFINITE never delete files
     * @return
     */
    public PhotoDownloader setTimeForDeleteImage(int timeAsMin) {
        this.timeAsMin = timeAsMin;
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
            Log.i("LOG_DashWood", "Directory created");
        }
        String fileName = HandlerReturnValue.getFileName(url);
        File imgFile = new File(file.getAbsolutePath() + "/" + fileName);
        if (timeAsMin == TIME_CLEAR_IMAGE_DEFAULT) {
            timeAsMin = 60;
        }
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
                    Log.i("LOG_DashWood", "clear image from storage");
                }
            } else if (timeAsMin == TIME_OF_CLEAR_IMAGE_INFINITE) {
                showImage(imgFile);
                return;
            } else {
                if (HandlerReturnValue.checkTimeForClear(saveDate, timeAsMin)) {
                    if (HandlerReturnValue.removeDirectory(file)) {
                        Log.i("LOG_DashWood", "clear directory from storage");
                        Data.saveToPreferenceString(context, context.getString(R.string.PREFERENCE_HOME_DATE),
                                "", context.getString(R.string.PREFERENCE_KEY_DATE));
                        if (file.mkdirs()) {
                            Log.i("LOG_DashWood", "Directory created");
                        }
                    } else {
                        Log.i("LOG_DashWood", "FILE NOT DELETE");
                    }
                } else {
                    showImage(imgFile);
                    return;
                }
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
                                showImage(imgFile);
                            });
                            if (callBackDownloader != null) {
                                callBackDownloader.onSuccess(response);
                            }
                        } else {
                            setImageUnloaded();
                        }
                    } catch (Exception e) {
                        if (callBackDownloader != null) {
                            callBackDownloader.onError(e);
                        }
                        e.printStackTrace();
                        setImageUnloaded();
                    }
                }, error -> {
            setImageUnloaded();
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse == null) {
                Log.e("Error_DashWood", "your error not in system, network response is Null");
                return;
            }
            if (callBackDownloader != null) {
                callBackDownloader.onError(new Exception(new String(networkResponse.data)));
            }
            Log.e("Error_DashWood", new String(networkResponse.data));
        });
        requestQueue.add(downloadHttpService);
    }

    private void checkQuality(File file, Bitmap bitmap) {
        if (quality != 0) {
            setImageQuality(file, bitmap);
        }
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

    private void showImage(File file) {
        imageView.clearAnimation();
        Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (quality != 0) {
            setImageQuality(file, photoBitmap);
        }
        if (width != 0 && height != 0) {
            photoBitmap = HandlerReturnValue.getResizedBitmap(photoBitmap, width, height);
        }
        imageView.setImageBitmap(photoBitmap);
    }
}
