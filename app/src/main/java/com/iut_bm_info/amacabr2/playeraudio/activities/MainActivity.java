package com.iut_bm_info.amacabr2.playeraudio.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.iut_bm_info.amacabr2.playeraudio.R;
import com.iut_bm_info.amacabr2.playeraudio.adapters.SongAdapter;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.SoundCloudApiRequest;
import com.iut_bm_info.amacabr2.playeraudio.api_connect.VolleySingleton;
import com.iut_bm_info.amacabr2.playeraudio.config.Config;
import com.iut_bm_info.amacabr2.playeraudio.models.Song;
import com.iut_bm_info.amacabr2.playeraudio.utils.ConvertDuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements
        SongAdapter.RecyclerItemClickListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private RecyclerView recycler;

    private SongAdapter adapter;

    private List<Song> songs;

    private int currentIndex;

    private TextView tvTitle, tvDuration;

    private ImageView ivPlay, ivNext, ivPrevious;

    private ProgressBar pbLoaderMain, pbLoaderToolbar;

    private SeekBar sbMusic;

    private MediaPlayer mediaPlayer;

    private long currentSongLenth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        setContentView(R.layout.activity_main);
        getSongs();

        songs = new ArrayList<>();

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new SongAdapter(getApplicationContext(), songs, this);
        recycler.setAdapter(adapter);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        handleSeekbar();
    }

    public void getSongs() {
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundCloudApiRequest request = new SoundCloudApiRequest(queue);

        request.getSongs(new SoundCloudApiRequest.SoundCloudInterface() {
            @Override
            public void onSuccess(List<Song> newSongs) {
                currentIndex = 0;
                songs.addAll(newSongs);
                adapter.notifyDataSetChanged();
                adapter.setSelectedPosition(0);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClickListener(Song song, int position) throws IOException {
        changeSelectedSong(position);
        prepareSong(song);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        togglePlay(mediaPlayer);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (currentIndex + 1 < songs.size()) {
            changeMusic(currentIndex + 1);
        } else {
            changeMusic(0);
        }
    }

    private void changeMusic(int i) {
        Song next = songs.get(i);
        changeSelectedSong(i);
        try {
            prepareSong(next);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        recycler = findViewById(R.id.mainActivity_recycler);
        pbLoaderMain = findViewById(R.id.mainActivity_progressBar);
        tvTitle = findViewById(R.id.toolbar_title);
        tvDuration = findViewById(R.id.toolbar_time);
        ivPlay = findViewById(R.id.toolbar_play);
        ivNext = findViewById(R.id.toolbar_next);
        ivPrevious = findViewById(R.id.toolbar_previous);
        pbLoaderToolbar = findViewById(R.id.toolbar_loader);
        sbMusic = findViewById(R.id.toolbar_seekbar);
    }

    private void togglePlay(final MediaPlayer mediaPlayer) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } else {
            pbLoaderToolbar.setVisibility(GONE);
            tvTitle.setVisibility(VISIBLE);
            mediaPlayer.start();
            ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause));

            final Handler handler = new Handler();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sbMusic.setMax((int) currentSongLenth / 1000);
                    sbMusic.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                    tvDuration.setText(ConvertDuration.milliToMinuteAnsSeconde((long)mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private void changeSelectedSong(int index) {
        adapter.notifyItemChanged(adapter.getSelectedPosition());
        currentIndex = index;
        adapter.setSelectedPosition(currentIndex);
        adapter.notifyItemChanged(currentIndex);
    }

    private void prepareSong(Song song) throws IOException {
        currentSongLenth = song.getDuration();
        pbLoaderToolbar.setVisibility(VISIBLE);
        tvTitle.setVisibility(GONE);
        ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play));
        tvTitle.setText(song.getTitle());
        tvDuration.setText(ConvertDuration.milliToMinuteAnsSeconde(song.getDuration()));
        String stream = song.getStreamUrl() + "?client_id="+ Config.CLIENT_ID;
        mediaPlayer.reset();
        mediaPlayer.setDataSource(stream);
        mediaPlayer.prepareAsync();
    }

    private void handleSeekbar() {
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
