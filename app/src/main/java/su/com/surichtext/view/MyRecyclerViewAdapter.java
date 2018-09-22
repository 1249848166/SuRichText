package su.com.surichtext.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import su.com.surichtext.activity.ReadDetailActivity;
import su.com.surichtext.model.Content;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.Holder> implements View.OnClickListener{

    Context context;
    List<Content> contents;

    public MyRecyclerViewAdapter(Context context, List<Content> contents) {
        this.context = context;
        this.contents = contents;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv=new TextView(context);
        return new Holder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            holder.itemView.setLayoutParams(params);
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.itemView.setPadding(10, 10, 10, 10);
            holder.itemView.setTextSize(20);
            holder.itemView.setText(contents.get(position).getTitle());
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public void onClick(View view) {
        try {
            int index = (int) view.getTag();
            String content = contents.get(index).getContent();
            Intent intent = new Intent(context, ReadDetailActivity.class);
            intent.putExtra("html", content);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView itemView;

        Holder(TextView itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
