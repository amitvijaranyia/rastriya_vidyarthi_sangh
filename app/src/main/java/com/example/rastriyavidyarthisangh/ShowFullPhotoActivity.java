package com.example.rastriyavidyarthisangh;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;


public class ShowFullPhotoActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_full_photo);

        Intent intent = getIntent();
        if(intent.hasExtra("photo_url")){
            String photo_url = intent.getStringExtra("photo_url");
            PhotoView photoView = findViewById(R.id.photo_view);
            Glide.with(this).load(photo_url).into(photoView);
        }
    }
}
