package su.com.richtext.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import su.com.richtext.R;
import su.com.richtext.model.SuFolder;
import su.com.richtext.utils.SuImageLoader;

public class SuAudioFolderAdapter extends RecyclerView.Adapter<SuAudioFolderAdapter.Holder> implements View.OnClickListener{

    private Context context;
    private List<SuFolder> audioFolders;

    public interface SuAudioFolderSelectCallback {
        void onFolderSelect(SuFolder folder);
    }

    private SuAudioFolderSelectCallback suAudioFolderSelectCallback;

    public SuAudioFolderAdapter(Context context, List<SuFolder> audioFolders, SuAudioFolderSelectCallback suAudioFolderSelectCallback) {
        this.context = context;
        this.audioFolders = audioFolders;
        this.suAudioFolderSelectCallback = suAudioFolderSelectCallback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(context).inflate(R.layout.audio_folder_item,parent,false);
        return new Holder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            TextView name=holder.itemView.findViewById(R.id.name);
            TextView num=holder.itemView.findViewById(R.id.num);
            name.setText(audioFolders.get(position).getFolderName());
            num.setText(audioFolders.get(position).getNum()+"");
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return audioFolders.size();
    }

    @Override
    public void onClick(View v) {
        int position= (int) v.getTag();
        if(suAudioFolderSelectCallback !=null)
            suAudioFolderSelectCallback.onFolderSelect(audioFolders.get(position));
    }

    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
