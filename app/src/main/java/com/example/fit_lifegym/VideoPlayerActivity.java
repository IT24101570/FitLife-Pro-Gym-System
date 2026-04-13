package com.example.fit_lifegym;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoPlayerActivity extends AppCompatActivity {

    private YouTubePlayerView youtubePlayerView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnOpenYouTube;
    private String videoUrl;
    private String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        videoTitle = getIntent().getStringExtra("VIDEO_TITLE");

        TextView tvTitle = findViewById(R.id.tvVideoTitle);
        tvTitle.setText(videoTitle);

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        errorLayout = findViewById(R.id.errorLayout);
        btnOpenYouTube = findViewById(R.id.btnOpenYouTube);
        btnOpenYouTube.setOnClickListener(v -> openInYouTubeApp());

        youtubePlayerView = findViewById(R.id.youtube_player_view);
        progressBar = findViewById(R.id.progressBar);

        getLifecycle().addObserver(youtubePlayerView);

        String videoId = extractVideoId(videoUrl);
        Log.d("VideoPlayer", "Extracted Video ID: " + videoId + " from URL: " + videoUrl);

        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            return;
        }

        // Simplified options - sometimes explicit origin causes UNKNOWN errors
        IFramePlayerOptions options = new IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .build();

        youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youtubePlayer) {
                progressBar.setVisibility(View.GONE);
                // Use loadVideo directly as it's more reliable for standard videos
                youtubePlayer.loadVideo(videoId, 0f);
                Log.d("VideoPlayer", "Player ready, loading video: " + videoId);
            }

            @Override
            public void onError(@NonNull YouTubePlayer youtubePlayer, @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                Log.e("VideoPlayer", "Player Error: " + error.name());
                progressBar.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                
                TextView tvErrorMessage = findViewById(R.id.tvErrorMessage);
                if (error == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError.UNKNOWN) {
                    tvErrorMessage.setText("This video has playback restrictions.\nPlease try watching it directly on YouTube.");
                } else {
                    tvErrorMessage.setText("Error playing video: " + error.name());
                }
            }
        }, options);
    }

    private void openInYouTubeApp() {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open YouTube", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractVideoId(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        url = url.trim();

        try {
            if (url.contains("v=")) {
                String id = url.split("v=")[1];
                int ampersandIndex = id.indexOf("&");
                if (ampersandIndex != -1) {
                    id = id.substring(0, ampersandIndex);
                }
                return id;
            } else if (url.contains("youtu.be/")) {
                String id = url.substring(url.lastIndexOf("/") + 1);
                int questionMarkIndex = id.indexOf("?");
                if (questionMarkIndex != -1) {
                    id = id.substring(0, questionMarkIndex);
                }
                return id;
            } else if (url.contains("embed/")) {
                String id = url.substring(url.lastIndexOf("/") + 1);
                int questionMarkIndex = id.indexOf("?");
                if (questionMarkIndex != -1) {
                    id = id.substring(0, questionMarkIndex);
                }
                return id;
            }
        } catch (Exception e) {
            Log.e("VideoPlayer", "Error extracting video ID", e);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }
}
