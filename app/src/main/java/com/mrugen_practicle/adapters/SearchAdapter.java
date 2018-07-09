package com.mrugen_practicle.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mrugen_practicle.R;
import com.mrugen_practicle.models.Datum;
import com.mrugen_practicle.models.VideoData;
import com.mrugen_practicle.models.VideoData_;
import com.mrugen_practicle.ui.detail.DetailActivity;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {

    List<Datum> searchList;
    Context context;
    Box<VideoData> videoDataBox;

    public SearchAdapter(List<Datum> movieList, Context context, Box<VideoData> videoDataBox) {
        this.searchList = movieList;
        this.context = context;
        this.videoDataBox = videoDataBox;
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_search, parent, false);
        SearchHolder mh = new SearchHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, final int position) {

        getVideoData(searchList.get(position).getId());
        holder.tvTitle.setText(searchList.get(position).getTitle());
        try {
            Glide.with(context)
                    .load(searchList.get(position).getImages().getFixedHeightSmall().getUrl())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivMovie);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        holder.itemView.setOnClickListener(view -> context.startActivity(new Intent(context, DetailActivity.class).putExtra("URL", searchList.get(position).getImages().getOriginalMp4().getMp4())
                .putExtra("ID", searchList.get(position).getId())
                .putExtra("IDS", searchList.get(position).getId())));
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public class SearchHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.ivMovie)
        ImageView ivMovie;

        public SearchHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public void getVideoData(String ids) {
        try {
            VideoData videoData = getVideoDataById(ids);
            if (videoData == null) {
//                Add first time or create new data
                videoDataBox.put(new VideoData(0, true, 0, true, ids));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VideoData getVideoDataById(String id) {
        return videoDataBox.query().equal(VideoData_.videoId, id).build().findUnique();
    }

}
