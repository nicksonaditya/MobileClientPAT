package com.example.mobileclientpat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HighscoresAdapter extends RecyclerView.Adapter<HighscoresAdapter.HighscoreViewHolder> {

    private List<Highscore> highscoreList;

    public HighscoresAdapter(List<Highscore> highscoreList) {
        this.highscoreList = highscoreList;
    }

    @NonNull
    @Override
    public HighscoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_highscore, parent, false);
        return new HighscoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HighscoreViewHolder holder, int position) {
        Highscore highscore = highscoreList.get(position);
        holder.rankTextView.setText(String.valueOf(highscore.getRank()));
        holder.usernameTextView.setText(highscore.getUsername());
        holder.scoreTextView.setText(String.valueOf(highscore.getScore()));
    }

    @Override
    public int getItemCount() {
        return highscoreList.size();
    }

    class HighscoreViewHolder extends RecyclerView.ViewHolder {

        TextView rankTextView;
        TextView usernameTextView;
        TextView scoreTextView;

        HighscoreViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
        }
    }
}
