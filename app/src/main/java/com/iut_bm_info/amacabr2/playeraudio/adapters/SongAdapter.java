package com.iut_bm_info.amacabr2.playeraudio.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iut_bm_info.amacabr2.playeraudio.R;
import com.iut_bm_info.amacabr2.playeraudio.models.Song;
import com.iut_bm_info.amacabr2.playeraudio.utils.ConvertDuration;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by amacabr2 on 23/11/17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;

    private List<Song> songs;

    private RecyclerItemClickListener listener;

    private int selectedPosition;

    public SongAdapter(Context context, List<Song> songs, RecyclerItemClickListener listener) {
        this.context = context;
        this.songs = songs;
        this.listener = listener;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = songs.get(position);

        if (song != null) {

            if (selectedPosition == position) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                holder.icPlayActive.setVisibility(VISIBLE);
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                holder.icPlayActive.setVisibility(INVISIBLE);
            }

            String duration = ConvertDuration.milliToMinuteAnsSeconde(song.getDuration());
            holder.tvTitle.setText(song.getTitle());
            holder.tvArtist.setText(song.getArtist());
            holder.tvDuration.setText(duration);
            Picasso.with(context).load(song.getArtworkUrl()).placeholder(R.drawable.music_placeholder).into(holder.ivArtWork);
            holder.bind(song, listener);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvArtist, tvDuration;

        private ImageView ivArtWork, icPlayActive;

        public SongViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.songRow_title);
            tvArtist = itemView.findViewById(R.id.songRow_artiste);
            tvDuration = itemView.findViewById(R.id.songRow_duration);
            ivArtWork = itemView.findViewById(R.id.songRow_image);
            icPlayActive = itemView.findViewById(R.id.songRow_playActive);
        }

        public void bind(final Song song, final RecyclerItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        listener.onClickListener(song, getLayoutPosition());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public interface RecyclerItemClickListener {

        void  onClickListener(Song song, int position) throws IOException;
    }
}
