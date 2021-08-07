package com.dashwood.dashwoodphotodownloader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;

import com.dashwood.dashwoodphotodownloader.databinding.ActivityMainBinding;
import com.dashwood.photodownloader.PhotoDownloader;
import com.dashwood.photodownloader.listener.CallBackDownloader;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PhotoDownloader photoDownloader = new PhotoDownloader(getApplicationContext(), "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/ESC_large_ISS022_ISS022-E-11387-edit_01.JPG/1600px-ESC_large_ISS022_ISS022-E-11387-edit_01.JPG"
                , binding.imgView);
        photoDownloader.setCallBackDownloader(new CallBackDownloader() {
            @Override
            public void onSuccess(byte[] response) {
                Log.i("LOG", "SUCCESS");
            }

            @Override
            public void onError(Exception e) {
                Log.i("LOG", "ERROR");
                e.printStackTrace();
            }
        });
        photoDownloader.init();
    }
}