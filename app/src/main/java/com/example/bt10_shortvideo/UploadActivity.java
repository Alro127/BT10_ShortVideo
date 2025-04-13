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

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bt10_shortvideo.databinding.ActivityUploadBinding;
import com.example.bt10_shortvideo.models.ShortVideo;
import com.example.bt10_shortvideo.utils.FileUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    ActivityUploadBinding binding;
    public static final int MY_REQUEST_CODE = 100;
    private Uri mUri;
    public static String[] storge_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO
    };
    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mProgressDialog = new ProgressDialog(UploadActivity.this);
        mProgressDialog.setMessage("Please wait upload....");

        binding.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        binding.btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

    }
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dưới Android 6.0 không cần runtime permission
            Toast.makeText(this, "Permission granted (legacy)", Toast.LENGTH_SHORT).show();
            openGallery();
            return;
        }

        // Android 13+ dùng quyền READ_MEDIA_VIDEO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_VIDEO}, MY_REQUEST_CODE);
            }
        } else {
            // Android 6.0 – 12.x dùng quyền READ_EXTERNAL_STORAGE
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Video"));
    }
    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.e(TAG, "onActivityResult");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // request code
                    Intent data = result.getData();
                    if (data == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    mUri = uri;
                    try {
                        binding.videoView.setVideoURI(mUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    );

    private void uploadVideo() {
        if (mUri == null) {
            Log.e(TAG, "No video selected");
            return;
        }

        mProgressDialog.show();

        Thread thread = new Thread(() -> {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dbwzloucf",
                        "api_key", "322178371811559",
                        "api_secret", "ZTu1_QCWmcZOz4mQ17UXuekMJc8"
                ));

                File file = FileUtils.getFile(this, mUri);

                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                        "resource_type", "video"
                ));

                String videoUrl = uploadResult.get("secure_url").toString();

                Log.d(TAG, "Video uploaded: " + videoUrl);

                // Lưu metadata vào Firebase
                saveVideoToFirebase(videoUrl);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    Log.e(TAG, "Upload failed");
                });
            }
        });
        thread.start();
    }

    private void saveVideoToFirebase(String videoUrl) {
        runOnUiThread(() -> mProgressDialog.dismiss());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("shortvideo");
        String videoKey = databaseReference.push().getKey();

        if (videoKey == null) {
            Log.e(TAG, "Failed to generate Firebase key");
            return;
        }

        // Giả định có thông tin từ người dùng nhập UI (nếu chưa có, bạn gán tạm)
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String email = userEmail;

        // Tạo model Video
        ShortVideo video = new ShortVideo(
                videoKey.hashCode(), // dùng hash code từ key để làm int id
                email,
                title,
                desc,
                videoUrl,
                0, // favNum mặc định
                0  // notFavNum mặc định
        );

        // Lưu vào Firebase
        databaseReference.child(videoKey).setValue(video)
                .addOnSuccessListener(aVoid ->
                {
                    Log.d(TAG, "Video metadata saved");
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save metadata", e));
    }
}
