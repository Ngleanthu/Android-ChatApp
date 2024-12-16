package com.example.chatapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activities.FullScreenMediaActivity;

public class MediaUtil {

    @OptIn(markerClass = UnstableApi.class)
    public static void addVideoToLayout(LinearLayout parentLayout, String videoUrl, String fileName, Context context, boolean showFullScreen) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Log.e("ExoPlayer", "Invalid video URL.");
            return;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int width;
        int height;

        if (showFullScreen) {
            width = FrameLayout.LayoutParams.MATCH_PARENT;
            height = FrameLayout.LayoutParams.MATCH_PARENT;
        } else {
            width = (int) (screenWidth * 0.7);
            height = (int) (screenHeight * 0.4);
        }

        // Tạo PlayerView
        PlayerView playerView = new PlayerView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        playerView.setLayoutParams(layoutParams);


        // Cấu hình ExoPlayer
        ExoPlayer player = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(player);

        playerView.post(new Runnable() {
            @Override
            public void run() {
                View previousButton = playerView.findViewById(androidx.media3.ui.R.id.exo_prev);
                View nextButton = playerView.findViewById(androidx.media3.ui.R.id.exo_next);
                if(previousButton != null){
                    previousButton.setVisibility(View.GONE);
                }
                if(previousButton != null){
                    nextButton.setVisibility(View.GONE);
                }
                View fullScreenButton = playerView.findViewById(androidx.media3.ui.R.id.exo_fullscreen);
                if (fullScreenButton != null && fullScreenButton instanceof ImageButton) {
                    ImageButton fullScreenImageButton = (ImageButton) fullScreenButton;

                    if (showFullScreen) {
                        fullScreenImageButton.setImageResource(R.drawable.baseline_fullscreen_exit_24);
                    } else {
                        fullScreenImageButton.setImageResource(R.drawable.baseline_fullscreen_24);
                    }

                    fullScreenButton.setOnClickListener(v -> {
                        if (showFullScreen) {
                            // Thoát toàn màn hình
                            if (context instanceof Activity) {
                                Activity activity = (Activity) context;
                                activity.setResult(Activity.RESULT_OK);
                                activity.finish();
                            }
                        } else {
                            // Chuyển sang toàn màn hình
                            Intent intent = new Intent(context, FullScreenMediaActivity.class);
                            intent.putExtra("VIDEO_URL", videoUrl);
                            intent.putExtra("FILE_NAME", fileName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });

                    // Đảm bảo nút fullscreen hiển thị
                    fullScreenButton.setVisibility(View.VISIBLE);
                }
            }
        });


        // Tạo MediaItem và thiết lập video URL
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        // Chuẩn bị và phát video
        player.prepare();

        // Tạo một FrameLayout để chứa PlayerView và nút thu phóng
        FrameLayout videoContainer = new FrameLayout(context);
        videoContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        videoContainer.addView(playerView);

        ImageButton downLoadButton = new ImageButton(context);
        downLoadButton.setImageResource(R.drawable.baseline_download_24);
        downLoadButton.setBackgroundColor(0); // Xóa nền
        downLoadButton.setContentDescription("Down video");

        // Thiết lập LayoutParams cho nút
        FrameLayout.LayoutParams paramsButtonDown = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        paramsButtonDown.gravity = Gravity.END | Gravity.TOP;
        paramsButtonDown.setMargins(16, 20, 20, 10);
        downLoadButton.setLayoutParams(paramsButtonDown);

        if(showFullScreen){
            videoContainer.addView(downLoadButton);
        }

        downLoadButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Download File")
                    .setMessage("Do you want to download this file?")
                    .setPositiveButton("Download", (dialog, which) -> FileHelper.downloadFile(videoUrl, fileName, context))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();

        });
        parentLayout.addView(videoContainer);

    }



    public static void addImageToLayout(LinearLayout parentLayout, String imageUrl, String fileName,  Context context, boolean showFullScreen) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int width;
        int height;

        if (showFullScreen) {
            width = FrameLayout.LayoutParams.MATCH_PARENT;
            height = FrameLayout.LayoutParams.MATCH_PARENT;
        } else {
            width = (int) (screenWidth * 0.7);
            height = (int) (screenHeight * 0.3);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width,
                height
        );
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.e("ImageView", "Invalid image URL.");
            return;
        }

        // Tạo một ImageView mới
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.background_input)
                .error(R.drawable.background_icon)
                .into(imageView);
        FrameLayout imageContainer = new FrameLayout(context);
        imageContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        imageContainer.addView(imageView);

        if(showFullScreen){
            // Thêm button X để thoát
            ImageButton closeButton = new ImageButton(context);
            closeButton.setImageResource(R.drawable.baseline_close_24);
            closeButton.setBackgroundColor(0);
            closeButton.setColorFilter(ContextCompat.getColor(context, R.color.primary_dark));
            FrameLayout.LayoutParams closeButtonParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            closeButtonParams.gravity = Gravity.START | Gravity.TOP;
            closeButtonParams.setMargins(20, 20, 20, 10);
            closeButton.setLayoutParams(closeButtonParams);
            closeButton.setOnClickListener(v -> {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                }
            });

            // Thêm button Download
            ImageButton downloadButton = new ImageButton(context);
            downloadButton.setImageResource(R.drawable.baseline_download_24);
            downloadButton.setBackgroundColor(0);
            downloadButton.setColorFilter(ContextCompat.getColor(context, R.color.primary_dark));
            FrameLayout.LayoutParams downloadButtonParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            downloadButtonParams.gravity = Gravity.END | Gravity.TOP;
            downloadButtonParams.setMargins(16, 20, 20, 10);
            downloadButton.setLayoutParams(downloadButtonParams);
            downloadButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Download File")
                        .setMessage("Do you want to download this file?")
                        .setPositiveButton("Download", (dialog, which) -> FileHelper.downloadFile(imageUrl, fileName, context))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();

            });

            // Thêm button vào FrameLayout
            imageContainer.addView(closeButton);
            imageContainer.addView(downloadButton);
        }

        imageContainer.setOnClickListener(v -> {
            if(!showFullScreen){
                // Chuyển sang toàn màn hình
                Intent intent = new Intent(context, FullScreenMediaActivity.class);
                intent.putExtra("IMAGE_URL", imageUrl);
                intent.putExtra("FILE_NAME", fileName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        parentLayout.addView(imageContainer);
    }
}