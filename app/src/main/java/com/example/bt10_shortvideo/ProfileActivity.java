package com.example.bt10_shortvideo;

import static android.content.ContentValues.TAG;

import static com.example.bt10_shortvideo.LoginActivity.userEmail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bt10_shortvideo.databinding.ActivityProfileBinding;
import com.example.bt10_shortvideo.models.ShortVideo;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUp();
        countUserVideos();
        binding.btnPostNewVideo.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadActivity.class));
            finish();
        });
        binding.profileImage.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadAvatarActivity.class));
            finish();
        });
    }

    private void setUp() {
        binding.tvEmail.setText(userEmail);
        String emailKey = userEmail.replace(".","_");
        DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                .getReference("avatar")
                .child(emailKey)
                .child("url");

        avatarRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String imageUrl = snapshot.getValue(String.class);

                // Gán URL vào ImageView bằng Glide
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(binding.profileImage);

                Log.d("AVATAR_URL", "URL: " + imageUrl);
            }
        });
    }

    private void countUserVideos() {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shortvideo");

        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    ShortVideo video = snapshot.getValue(ShortVideo.class);
                    if (video != null && userEmail != null && userEmail.equals(video.getEmail())) {
                        count++;
                    }
                }

                Log.d("VIDEO_COUNT", "Số video của user " + userEmail + " là: " + count);
                binding.videoNum.setText(String.valueOf(count));

            } else {
                Log.e("VIDEO_COUNT", "Lỗi khi truy vấn Firebase", task.getException());
            }
        });
    }

}
