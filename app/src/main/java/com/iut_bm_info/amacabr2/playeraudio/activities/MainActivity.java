package com.iut_bm_info.amacabr2.playeraudio.activities;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
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
        MediaPlayer.OnCompletionListener,
        View.OnClickListener {

    public static final int CLICK_IV_PLAY = 0;

    public static final int CLICK_IV_PREVIOUS = 1;

    public static final int CLICK_IV_NEXT = 2;

    public static final int CLICK_BTN_SEARCH = 3;

    private RecyclerView recycler;

    private SongAdapter adapter;

    private List<Song> songs;

    private TextView tvTitle, tvDuration;

    private ImageView ivPlay, ivNext, ivPrevious;

    private ProgressBar pbLoaderMain, pbLoaderToolbar;

    private FloatingActionButton fabSearch;

    private SeekBar sbMusic;

    private MediaPlayer mediaPlayer;

    private long currentSongLenth;

    private int currentIndex;

    private boolean firstLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        setContentView(R.layout.activity_main);
        getSongs("");

        songs = new ArrayList<>();
        firstLaunch = true;

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new SongAdapter(getApplicationContext(), songs, this);
        recycler.setAdapter(adapter);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        handleSeekbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void getSongs(String query) {
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundCloudApiRequest request = new SoundCloudApiRequest(queue);
        pbLoaderMain.setVisibility(VISIBLE);

        request.getSongs(query, new SoundCloudApiRequest.SoundCloudInterface() {
            @Override
            public void onSuccess(List<Song> newSongs) {
                pbLoaderMain.setVisibility(GONE);
                currentIndex = 0;
                songs.clear();
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
        firstLaunch = false;
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
            playNewSong(currentIndex + 1);
        } else {
            playNewSong(0);
        }
    }

    @Override
    public void onClick(View view) {
       switch ((int)view.getTag()) {
           case CLICK_IV_PLAY:
               pushPlay();
               break;
           case CLICK_IV_PREVIOUS:
               pushPrevious();
               break;
           case CLICK_IV_NEXT:
               pushNext();
               break;
           case CLICK_BTN_SEARCH:
               pushSearch();
               break;
       }
    }

    private void playNewSong(int i) {
        Song nextOrPrevious = songs.get(i);
        changeSelectedSong(i);
        try {
            prepareSong(nextOrPrevious);
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
        fabSearch = findViewById(R.id.mainActivity_btnSearch);

        ivPlay.setTag(CLICK_IV_PLAY);
        ivPrevious.setTag(CLICK_IV_PREVIOUS);
        ivNext.setTag(CLICK_IV_NEXT);
        fabSearch.setTag(CLICK_BTN_SEARCH);

        ivPlay.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        fabSearch.setOnClickListener(this);
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

    private void pushPlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play));
                mediaPlayer.pause();
            } else {
                if (firstLaunch) {
                    playNewSong(0);
                } else {
                    mediaPlayer.start();
                    firstLaunch = false;
                }
            }

            ivPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause));
        }
    }

    private void pushPrevious() {
        if (mediaPlayer != null) {
            firstLaunch = false;
            int index;

            if ((index = currentIndex - 1) >= 0) {
                playNewSong(index);
            } else {
                playNewSong(songs.size() - 1);
            }
        }
    }

    private void pushNext() {
        if (mediaPlayer != null) {
            firstLaunch = false;
            int index;

            if ((index = currentIndex + 1) < songs.size()) {
                playNewSong(index);
            } else {
                playNewSong(0);
            }
        }
    }

    private void pushSearch() {
        createDialog();
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        builder.setTitle(R.string.rechercher);
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText etSearch = view.findViewById(R.id.dialogSearch);
                String search = etSearch.getText().toString().trim();

                if (search.length() > 0) {
                    getSongs(search);
                } else {
                    Toast.makeText(MainActivity.this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.create().show();
    }
}
