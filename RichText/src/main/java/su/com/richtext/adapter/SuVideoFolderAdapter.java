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
import su.com.richtext.utils.SuVideoImageLoader;

public class SuVideoFolderAdapter extends RecyclerView.Adapter<SuVideoFolderAdapter.Holder> implements View.OnClickListener{

    private Context context;
    private List<SuFolder> videoFolders;

    public interface SuVideoFolderSelectCallback {
        void onFolderSelect(SuFolder folder);
    }

    private SuVideoFolderSelectCallback suVideoFolderSelectCallback;

    public SuVideoFolderAdapter(Context context, List<SuFolder> videoFolders, SuVideoFolderSelectCallback suVideoFolderSelectCallback) {
        this.context = context;
        this.videoFolders = videoFolders;
        this.suVideoFolderSelectCallback = suVideoFolderSelectCallback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(context).inflate(R.layout.image_folder_item,parent,false);
        return new Holder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            ImageView img=holder.itemView.findViewById(R.id.img);
            TextView name=holder.itemView.findViewById(R.id.name);
            TextView num=holder.itemView.findViewById(R.id.num);
            name.setText(videoFolders.get(position).getFolderName());
            num.setText(videoFolders.get(position).getNum()+"");
            SuVideoImageLoader.getInstance(context).loadVideoImage(videoFolders.get(position).getFirstFilePath(),img,null);
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return videoFolders.size();
    }

    @Override
    public void onClick(View v) {
        int position= (int) v.getTag();
        if(suVideoFolderSelectCallback !=null)
            suVideoFolderSelectCallback.onFolderSelect(videoFolders.get(position));
    }

    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
