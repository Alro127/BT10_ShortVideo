package com.example.bt10_shortvideo;

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
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bt10_shortvideo.utils.FileUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class UploadAvatarActivity extends AppCompatActivity {
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
    Button btnChoose, btnUpload;
    CircleImageView imgAvatar;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    public static final int MY_REQUEST_CODE = 100;
    public static final String TAG = UploadAvatarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_avatar);

        btnChoose = findViewById(R.id.btnChooseFile);
        btnUpload = findViewById(R.id.btnUpload);
        imgAvatar = findViewById(R.id.imgAvatar);


        mProgressDialog = new ProgressDialog(UploadAvatarActivity.this);
        mProgressDialog.setMessage("Please wait upload....");

//bắt sự kiện nút chọn ảnh
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPermission(); //checks quyền
                //chooseImage();
            }
        });

//bắt sự kiện upload ảnh
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null) {
                    uploadImage();
                }
            }
        });
    }
    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }
    private void CheckPermission() {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            openGallery();
            return;
        }
        if(checkSelfPermission (android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }else{
            requestPermissions(permissions(), MY_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
    private void uploadImage() {
        if (mUri == null) {
            Toast.makeText(this, "Chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.show();

        new Thread(() -> {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dbwzloucf",
                        "api_key", "322178371811559",
                        "api_secret", "ZTu1_QCWmcZOz4mQ17UXuekMJc8"
                ));

                File file = FileUtils.getFile(this, mUri);

                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                        "resource_type", "image"
                ));

                String imageUrl = uploadResult.get("secure_url").toString();
                saveImageToFirebase(imageUrl);

            } catch (Exception e) {
                runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
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
                            imgAvatar.setImageURI(mUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void saveImageToFirebase(String imageUrl) {
        runOnUiThread(() -> mProgressDialog.dismiss());

        String email = userEmail;
        if (email == null) {
            Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailKey = email.replace(".", "_");
        DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                .getReference("avatar")
                .child(emailKey);

        // Bước 1: Xóa avatar cũ nếu có
        avatarRef.removeValue()
                .addOnCompleteListener(task -> {
                    // Bước 2: Lưu avatar mới
                    Map<String, Object> avatarData = new HashMap<>();
                    avatarData.put("url", imageUrl);
                    avatarData.put("timestamp", System.currentTimeMillis());

                    avatarRef.setValue(avatarData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Lưu avatar thành công", Toast.LENGTH_SHORT).show();
                                startActivity( new Intent(this, ProfileActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("SAVE_AVATAR", "Lỗi khi lưu avatar mới", e);
                                Toast.makeText(this, "Lưu avatar thất bại", Toast.LENGTH_SHORT).show();
                            });
                });
    }

}
