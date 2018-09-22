package su.com.richtext.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import su.com.richtext.R;
import su.com.richtext.utils.SuVideoImageLoader;

public class SuVideoAdapter extends RecyclerView.Adapter<SuVideoAdapter.Holder> implements View.OnClickListener {

    private Context context;
    private List<String> imagePaths;
    private List<String> selectedList = new ArrayList<>();
    int maxSize;
    private int width;

    public interface SuVideoSelectCallback{
        void onVideoSelect(String path,int size,int maxSize);
    }

    SuVideoSelectCallback suVideoSelectCallback;

    public List<String> getSelectedList() {
        return selectedList;
    }

    public SuVideoAdapter(Context context, List<String> imagePaths, int space, int span, int maxSize,SuVideoSelectCallback suVideoSelectCallback) {
        this.suVideoSelectCallback=suVideoSelectCallback;
        this.context = context;
        this.imagePaths = imagePaths;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        width = (int) ((float) (screenWidth - space * (span + 1)) / span);
        this.maxSize=maxSize;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview=LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new Holder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        try {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.width = width;
            params.height = width;
            holder.itemView.setLayoutParams(params);
            ImageView imageView=holder.itemView.findViewById(R.id.img);
            ViewGroup.LayoutParams params1=imageView.getLayoutParams();
            params1.width=width;
            params1.height=width;
            imageView.setLayoutParams(params1);
            imageView.setImageResource(R.drawable.round_corner_gray);
            SuVideoImageLoader.getInstance(context).loadVideoImage(imagePaths.get(position), imageView, null);
            holder.itemView.setTag(imagePaths.get(position));
            holder.itemView.setOnClickListener(this);
            final CheckBox checkBox=holder.itemView.findViewById(R.id.check);
            checkBox.setTag(imagePaths.get(position));
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path= (String) v.getTag();
                    if(checkBox.isChecked()){
                        if(selectedList.size()<maxSize) {
                            if (!selectedList.contains(path)) {
                                selectedList.add(path);
                                if(suVideoSelectCallback!=null)
                                    suVideoSelectCallback.onVideoSelect(path,selectedList.size(),maxSize);
                            }
                        }else{
                            ((CheckBox)v).setChecked(false);
                            Toast.makeText(context, "最多选择"+maxSize+"个", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if(selectedList.contains(path)){
                            selectedList.remove(path);
                        }
                    }
                }
            });
            if(selectedList.contains(imagePaths.get(position))){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    @Override
    public void onClick(View v) {
        String path= (String) v.getTag();
        previewVideo(path);
    }

    private void previewVideo(String path) {
        Uri uri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/mp4");
        context.startActivity(intent);
    }

    class Holder extends RecyclerView.ViewHolder {

        View itemView;

        Holder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
