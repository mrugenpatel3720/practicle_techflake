package com.mrugen_practicle.ui.detail;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.mrugen_practicle.MyApplication;
import com.mrugen_practicle.R;
import com.mrugen_practicle.models.VideoData;
import com.mrugen_practicle.models.VideoData_;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class DetailActivity extends AppCompatActivity implements AdaptiveMediaSourceEventListener {

    @BindView(R.id.sv_player)
    SurfaceView svPlayer;
    @BindView(R.id.prev)
    ImageButton prev;
    @BindView(R.id.rew)
    ImageButton rew;
    @BindView(R.id.btnPlay)
    ImageButton btnPlay;
    @BindView(R.id.ffwd)
    ImageButton ffwd;
    @BindView(R.id.next)
    ImageButton next;
    @BindView(R.id.time_current)
    TextView timeCurrent;
    @BindView(R.id.mediacontroller_progress)
    SeekBar mediacontrollerProgress;
    @BindView(R.id.player_end_time)
    TextView playerEndTime;
    @BindView(R.id.fullscreen)
    ImageButton fullscreen;
    @BindView(R.id.lin_media_controller)
    LinearLayout linMediaController;
    @BindView(R.id.player_frame_layout)
    FrameLayout playerFrameLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.txtCount)
    AppCompatTextView txtCount;
    @BindView(R.id.imgLike)
    AppCompatImageView imgLike;
    @BindView(R.id.imgDislike)
    AppCompatImageView imgDislike;

    private SimpleExoPlayer exoPlayer;
    private boolean bAutoplay = true;
    private boolean bIsPlaying = false;
    private boolean bControlsActive = true;

    private Handler handler;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private DataSource.Factory dataSourceFactory;

    private String dash = "http://www.youtube.com/api/manifest/dash/id/3aa39fa2cc27967f/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=A2716F75795F5D2AF0E88962FFCD10DB79384F29.84308FF04844498CE6FBCE4731507882B8307798&key=ik0";

    private String userAgent =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:40.0) Gecko/20100101 Firefox/40.0";

    private BoxStore boxStore;
    private Box<VideoData> videoDataBox;
    private long ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler = new Handler();
        initDataSource();
        initDashPlayer(dash);

        boxStore = ((MyApplication) getApplication()).getBoxStore();
        videoDataBox = boxStore.boxFor(VideoData.class);

        toolbar.setTitle("Video Player");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        });

        getVideoData(getIntent().getStringExtra("IDS"));

        initMp4Player(getIntent().getStringExtra("URL"));

        if (bAutoplay) {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(true);
                bIsPlaying = true;
                setProgress();
            }
        }

        imgLike.setOnClickListener(view -> {
            imgLike.setImageResource(R.drawable.ic_like);
            imgDislike.setImageResource(R.drawable.ic_dislike_white);
            long totalCount = Long.parseLong(txtCount.getText().toString().trim()) + 1;
            txtCount.setText(totalCount + "");
            updateVideoData(ID, true, getIntent().getStringExtra("IDS"));
        });

        imgDislike.setOnClickListener(view -> {
            imgDislike.setImageResource(R.drawable.ic_dislike);
            imgLike.setImageResource(R.drawable.ic_like_white);
            long totalCount = Long.parseLong(txtCount.getText().toString().trim()) + 1;
            txtCount.setText(totalCount + "");
            updateVideoData(ID, false, getIntent().getStringExtra("IDS"));
        });
    }

    private void initDataSource() {
        dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"),
                        new DefaultBandwidthMeter());
    }

    private void initMediaControls() {
        initSurfaceView();
        initPlayButton();
        initSeekBar();
        initFwd();
        initPrev();
        initRew();
        initNext();
    }

    private void initNext() {
        next.requestFocus();
        next.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getDuration()));
    }

    private void initRew() {
        rew.requestFocus();
        rew.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000));
    }

    private void initPrev() {
        prev.requestFocus();
        prev.setOnClickListener(view -> exoPlayer.seekTo(0));
    }

    private void initFwd() {
        ffwd.requestFocus();
        ffwd.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000));
    }


    private void initSurfaceView() {
        svPlayer.setOnClickListener(view -> toggleMediaControls());
    }

    private String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setProgress() {
        mediacontrollerProgress.setProgress(0);
        mediacontrollerProgress.setMax(0);
        mediacontrollerProgress.setMax((int) exoPlayer.getDuration() / 1000);

        handler = new Handler();
        //Make sure you update Seekbar on UI thread
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (exoPlayer != null && bIsPlaying) {
                    mediacontrollerProgress.setMax(0);
                    mediacontrollerProgress.setMax((int) exoPlayer.getDuration() / 1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    mediacontrollerProgress.setProgress(mCurrentPosition);
                    timeCurrent.setText(stringForTime((int) exoPlayer.getCurrentPosition()));
                    playerEndTime.setText(stringForTime((int) exoPlayer.getDuration()));

                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void initSeekBar() {
        mediacontrollerProgress.requestFocus();

        mediacontrollerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                exoPlayer.seekTo(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediacontrollerProgress.setMax(0);
        mediacontrollerProgress.setMax((int) exoPlayer.getDuration() / 1000);
    }

    private void toggleMediaControls() {

        if (bControlsActive) {
            hideMediaController();
            bControlsActive = false;
        } else {
            showController();
            bControlsActive = true;
            setProgress();
        }
    }

    private void showController() {
        linMediaController.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void hideMediaController() {
        linMediaController.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initPlayButton() {
        btnPlay.requestFocus();
        btnPlay.setOnClickListener(view -> {
            if (bIsPlaying) {
                exoPlayer.setPlayWhenReady(false);
                bIsPlaying = false;
            } else {
                exoPlayer.setPlayWhenReady(true);
                bIsPlaying = true;
                setProgress();
            }
        });
    }

    private void initMp4Player(String mp4URL) {

        MediaSource sampleSource =
                new ExtractorMediaSource(Uri.parse(mp4URL), dataSourceFactory, new DefaultExtractorsFactory(),
                        handler, error -> {

                        });


        initExoPlayer(sampleSource);
    }


    private void initDashPlayer(String dashUrl) {


        MediaSource sampleSource =
                new DashMediaSource(Uri.parse(dashUrl), new DefaultDataSourceFactory(this, userAgent),
                        new DefaultDashChunkSource.Factory(dataSourceFactory), handler,
                        this);

        initExoPlayer(sampleSource);
    }

    private void initExoPlayer(MediaSource sampleSource) {
        if (exoPlayer == null) {
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            // 2. Create the player
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        }

        exoPlayer.prepare(sampleSource);

        exoPlayer.setVideoSurfaceView(svPlayer);

        exoPlayer.setPlayWhenReady(true);

        initMediaControls();
    }

    private void initHLSPlayer(String dashUrl) {

        MediaSource sampleSource = new HlsMediaSource(Uri.parse(dashUrl), dataSourceFactory, handler,
                this);


        initExoPlayer(sampleSource);
    }

    @Override
    public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
                              int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
                              long mediaEndTimeMs, long elapsedRealtimeMs) {

    }

    @Override
    public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
                                int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
                                long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
                               int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
                               long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
                            int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
                            long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded,
                            IOException error, boolean wasCanceled) {

    }

    @Override
    public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason,
                                          Object trackSelectionData, long mediaTimeMs) {

    }

    public void getVideoData(String ids) {
        try {
            VideoData videoData = getVideoDataById(ids);
            if (videoData == null) {
//                Add first time
//                videoDataBox.put(new VideoData(id, true, 0, true, ids));
                txtCount.setText("0");
            } else {
//                get Data
                ID = videoData.getId();
                if (videoData.isDefolt() == true) {

                } else {
                    if (videoData.isLike() == true) {
                        imgLike.setImageResource(R.drawable.ic_like);
                        imgDislike.setImageResource(R.drawable.ic_dislike_white);
                    } else {
                        imgDislike.setImageResource(R.drawable.ic_dislike);
                        imgLike.setImageResource(R.drawable.ic_like_white);
                    }
                }
                txtCount.setText(videoData.getTotalCount() + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateVideoData(long id, boolean isLike, String ids) {
        try {

            VideoData videoData = getVideoDataById(ids);
            if (videoData != null) {
//                Update
                videoDataBox.put(new VideoData(id, isLike, Long.parseLong(txtCount.getText().toString().trim()), false, ids));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VideoData getVideoDataById(String id) {
        return videoDataBox.query().equal(VideoData_.videoId, id).build().findUnique();
    }
}