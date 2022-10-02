package example.org.spottest;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    public static List<Track> trackList;

    public TrackAdapter(Context ctx, List<Track> trackList) {
        inflater = LayoutInflater.from(ctx);
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public TrackAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.rv_item, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackAdapter.MyViewHolder viewHolder, int i) {
        viewHolder.trackEditText.setText(trackList.get(i).getTitle());
        viewHolder.artistEditText.setText(trackList.get(i).getArtist());
        viewHolder.albumEditText.setText(trackList.get(i).getAlbum());
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        protected EditText trackEditText;
        protected EditText artistEditText;
        protected EditText albumEditText;

        public MyViewHolder(View itemView) {
            super(itemView);

            trackEditText = itemView.findViewById(R.id.track_edittext);
            artistEditText = itemView.findViewById(R.id.artist_edittext);
            albumEditText = itemView.findViewById(R.id.album_edittext);

            trackEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    trackList.get(getAdapterPosition()).setTitle(trackEditText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            artistEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    trackList.get(getAdapterPosition()).setArtist(artistEditText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            albumEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    trackList.get(getAdapterPosition()).setAlbum(albumEditText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}
