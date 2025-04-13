package com.example.bt10_shortvideo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.bt10_shortvideo.R;
import com.example.bt10_shortvideo.databinding.ActivityHomeBinding;
import com.example.bt10_shortvideo.models.ShortVideo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.checkerframework.checker.units.qual.N;

import java.util.List;

public class ShortVideoAdapter extends FirebaseRecyclerAdapter<ShortVideo, ShortVideoAdapter.MyHolder> {
    private boolean isFav = false;
    private boolean isNotFav = false;

    public ShortVideoAdapter(@NonNull FirebaseRecyclerOptions<ShortVideo> options) {
        super(options);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private VideoView videoView;
        private ProgressBar progressBar;
        private TextView tvEmail, tvTitle, tvDescription, tvFavorite, tvNotFavorite;
        private ImageView imgShare, imgMore;
        public MyHolder(@NonNull View view) {
            super(view);
            videoView = view.findViewById(R.id.videoView);
            progressBar = view.findViewById(R.id.videoProgressBar);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvFavorite = view.findViewById(R.id.tvFavorite);
            tvNotFavorite = view.findViewById(R.id.tvNotFavorite);
            imgMore = view.findViewById(R.id.imgMore);
            imgShare = view.findViewById(R.id.imgShare);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull ShortVideoAdapter.MyHolder holder, int position, @NonNull ShortVideo model) {
        holder.tvEmail.setText(model.getEmail());
        loadAvatarForEmail(model.getEmail(), holder.tvEmail, holder.itemView.getContext());
        holder.tvTitle.setText(model.getTitle());
        holder.tvDescription.setText(model.getDesc());
        holder.tvFavorite.setText(String.valueOf(model.getFavNum()));
        holder.tvNotFavorite.setText(String.valueOf(model.getNotFavNum()));

        holder.videoView.setVideoURI(Uri.parse(model.getUrl()));
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.progressBar.setVisibility(View.GONE);
                mp.start();
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
                float scale = videoRatio / screenRatio;
                if (scale>1f)
                    holder.videoView.setScaleX(scale);
                else
                    holder.videoView.setScaleY(1f/scale);
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        holder.tvFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFav) {
                    holder.tvFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_red_favorite_24, 0, 0);
                    isFav = true;
                    model.increaseFavNum();
                    if (isNotFav) {
                        holder.tvNotFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_heart_broken_24,  0, 0);
                        isNotFav = false;
                        model.decreaseNotFavNum();
                    }
                }
                else {
                    holder.tvFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_favorite_24, 0, 0);
                    isFav = false;
                    model.decreaseFavNum();
                }

                updateFirebase(model);
            }
        });

        holder.tvNotFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNotFav) {
                    holder.tvNotFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_red_heart_broken_24,  0, 0);
                    isNotFav = true;
                    model.increaseNotFavNum();
                    if (isFav) {
                        holder.tvFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_favorite_24, 0, 0);
                        isFav = false;
                        model.decreaseFavNum();
                    }
                }
                else {
                    holder.tvNotFavorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_heart_broken_24, 0, 0);
                    isNotFav = false;
                    model.decreaseNotFavNum();
                }

                updateFirebase(model);
            }
        });

    }

    private void loadAvatarForEmail(String email, TextView textView, Context context) {
        String emailKey = email.replace(".", "_");
        DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                .getReference("avatar")
                .child(emailKey)
                .child("url");

        avatarRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String imageUrl = snapshot.getValue(String.class);

                // Dùng Glide để tải ảnh và set vào drawableLeft
                Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .circleCrop()
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                                drawable.setBounds(0, 0, 72, 72); // Đặt kích thước avatar
                                textView.setCompoundDrawablesRelative(drawable, null, null, null);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                setDefaultAvatar(textView, context);
                            }
                        });

            } else {
                // Không có URL → đặt avatar mặc định
                setDefaultAvatar(textView, context);
            }
        }).addOnFailureListener(e -> {
            Log.e("AVATAR", "Lỗi lấy avatar: ", e);
            setDefaultAvatar(textView, context);
        });
    }
    private void setDefaultAvatar(TextView textView, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.avatar);
        if (drawable != null) {
            drawable.setBounds(0, 0, 72, 72);
            textView.setCompoundDrawablesRelative(drawable, null, null, null);
        }
    }



    private void updateFirebase(ShortVideo model) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference videoRef = database.child("shortvideo").child(String.valueOf(model.getId()));

        // Cập nhật lượt thích và không thích
        videoRef.child("favNum").setValue(model.getFavNum());
        videoRef.child("notFavNum").setValue(model.getNotFavNum());
    }

    @NonNull
    @Override
    public ShortVideoAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        return new MyHolder(view);
    }
}
