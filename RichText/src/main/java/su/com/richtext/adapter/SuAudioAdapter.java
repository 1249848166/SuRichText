package su.com.richtext.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import su.com.richtext.R;

public class SuAudioAdapter extends RecyclerView.Adapter<SuAudioAdapter.Holder> implements View.OnClickListener {

    private Context context;
    private List<String> audioPaths;
    private List<String> selectedList = new ArrayList<>();
    int maxSize;

    public interface SuAudioSelectCallback {
        void onImageSelected(String path, int size, int maxSize);
    }

    SuAudioSelectCallback suAudioSelectCallback;

    public List<String> getSelectedList() {
        return selectedList;
    }

    public SuAudioAdapter(Context context, List<String> audioPaths,int maxSize, SuAudioSelectCallback suAudioSelectCallback) {
        this.suAudioSelectCallback = suAudioSelectCallback;
        this.context = context;
        this.audioPaths = audioPaths;
        this.maxSize=maxSize;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview=LayoutInflater.from(context).inflate(R.layout.audio_item, parent, false);
        return new Holder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        try {
            TextView name=holder.itemView.findViewById(R.id.name);
            String path=audioPaths.get(position);
            name.setText(path.substring(path.lastIndexOf("/")+1,path.length()));
            holder.itemView.setTag(path);
            holder.itemView.setOnClickListener(this);
            final CheckBox checkBox=holder.itemView.findViewById(R.id.check);
            checkBox.setTag(path);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path= (String) v.getTag();
                    if(checkBox.isChecked()){
                        if(selectedList.size()<maxSize) {
                            if (!selectedList.contains(path)) {
                                selectedList.add(path);
                                if(suAudioSelectCallback !=null)
                                    suAudioSelectCallback.onImageSelected(path,selectedList.size(),maxSize);
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
            if(selectedList.contains(audioPaths.get(position))){
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
        return audioPaths.size();
    }

    @Override
    public void onClick(View v) {
        String path= (String) v.getTag();
        previewAudio(path);
    }

    private void previewAudio(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(path);
        intent.setDataAndType(uri, "audio_white/mp3");
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
