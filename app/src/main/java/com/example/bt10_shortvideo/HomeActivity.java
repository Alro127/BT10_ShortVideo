package com.example.bt10_shortvideo;

import static com.example.bt10_shortvideo.LoginActivity.userEmail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.bt10_shortvideo.adapter.ShortVideoAdapter;
import com.example.bt10_shortvideo.databinding.ActivityHomeBinding;
import com.example.bt10_shortvideo.models.ShortVideo;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    ShortVideoAdapter shortVideoAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setImage();
        getVideos();
        switchActivity();
    }

    private void switchActivity() {
        binding.imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void getVideos() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("shortvideo");
        FirebaseRecyclerOptions<ShortVideo> options = new FirebaseRecyclerOptions.Builder<ShortVideo>()
                .setQuery(mDatabase, ShortVideo.class).build();

        shortVideoAdapter = new ShortVideoAdapter(options);
        binding.vpager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        binding.vpager.setAdapter(shortVideoAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        shortVideoAdapter.startListening();
    }
    private void setImage() {
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
                        .into(binding.imgProfile);

                Log.d("AVATAR_URL", "URL: " + imageUrl);
            }
        });
    }
}
