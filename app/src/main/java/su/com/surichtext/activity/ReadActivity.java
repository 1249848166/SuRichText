package su.com.surichtext.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import su.com.surichtext.R;
import su.com.surichtext.model.Content;
import su.com.surichtext.view.MyRecyclerViewAdapter;
import su.com.surichtext.view.MyRecyclerViewDecoration;

public class ReadActivity extends AppCompatActivity implements OnLoadMoreListener,OnRefreshListener{

    SmartRefreshLayout refresh;
    RecyclerView recycler;
    MyRecyclerViewAdapter adapter;
    List<Content> contents;
    MyRecyclerViewDecoration decoration;

    int page=0;
    final int limit=20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        try {
            refresh = findViewById(R.id.refresh);
            refresh.setRefreshFooter(new ClassicsFooter(this));
            refresh.setRefreshHeader(new ClassicsHeader(this));
            refresh.setOnRefreshListener(this);
            refresh.setOnLoadMoreListener(this);

            recycler = findViewById(R.id.recycler);
            contents = new ArrayList<>();
            adapter = new MyRecyclerViewAdapter(this, contents);
            decoration = new MyRecyclerViewDecoration(10);
            recycler.addItemDecoration(decoration);
            recycler.setAdapter(adapter);
            recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            loadData("refresh");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.finishLoadMore(500);
        loadData("loadMore");
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.finishRefresh(500);
        loadData("refresh");
    }

    void loadData(final String tag){
        try {
            BmobQuery<Content> bmobQuery = new BmobQuery<>();
            switch (tag) {
                case "refresh":
                    page = 0;
                    break;
                case "loadMore":
                    page++;
                    break;
            }
            bmobQuery.setLimit(limit);
            bmobQuery.setSkip(limit * page);
            bmobQuery.findObjects(new FindListener<Content>() {
                @Override
                public void done(List<Content> list, BmobException e) {
                    try {
                        if (e == null) {
                            int start = contents.size();
                            switch (tag) {
                                case "refresh":
                                    contents.clear();
                                    contents.addAll(list);
                                    adapter.notifyDataSetChanged();
                                    break;
                                case "loadMore":
                                    contents.addAll(list);
                                    adapter.notifyItemRangeChanged(start, list.size());
                                    break;
                            }
                        } else {
                            Toast.makeText(ReadActivity.this, "网络错误:" + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
