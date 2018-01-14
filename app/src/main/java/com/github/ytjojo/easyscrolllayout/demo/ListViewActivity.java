package com.github.ytjojo.easyscrolllayout.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ytjojo.easyscrolllayout.BaseRefreshIndicator;
import com.github.ytjojo.easyscrolllayout.ContentWraperView;
import com.github.ytjojo.easyscrolllayout.EasyScrollLayout;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class ListViewActivity extends AppCompatActivity {
    ListView mListView;


    private String[] name = { "剑萧舞蝶", "张三", "hello", "诗情画意","总是单位" };

    private String[] desc = { "魔域玩家", "百家执行", "高级的富一代", "妹子请过来.","一个善于跑妹子的" };
    View mFirstView;
    int mLastTop;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickylayout);
        TextView tvHeader = (TextView) findViewById(R.id.tvHeader);
        tvHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"click header",Toast.LENGTH_SHORT).show();
            }
        });
        tvHeader.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(),"Long click header",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mListView = (ListView) findViewById(R.id.listview);
        List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < name.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head",R.mipmap.ic_launcher_round);
            listem.put("name", name[i]);
            listem.put("desc", desc[i]);
            listems.add(listem);
        }
        for (int i = 0; i < name.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head", R.mipmap.ic_launcher_round);
            listem.put("name", name[i]);
            listem.put("desc", desc[i]);
            listems.add(listem);
        }
        for (int i = 0; i < name.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head", R.mipmap.ic_launcher_round);
            listem.put("name", name[i]);
            listem.put("desc", desc[i]);
            listems.add(listem);
        }
        for (int i = 0; i < name.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head", R.mipmap.ic_launcher_round);
            listem.put("name", name[i]);
            listem.put("desc", desc[i]);
            listems.add(listem);
        }
        for (int i = 0; i < name.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("head", R.mipmap.ic_launcher_round);
            listem.put("name", name[i]);
            listem.put("desc", desc[i]);
            listems.add(listem);
        }
        SimpleAdapter simpleadapter = new SimpleAdapter(this, listems,
                R.layout.item_list_simple, new String[] { "name", "head", "desc" },
                new int[] {R.id.name,R.id.head,R.id.desc});
        mListView.setAdapter(simpleadapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(mFirstView !=null){
                    int dy = mFirstView.getTop() - mLastTop;
                    if(dy != 0){
                        Logger.e("mListView onScroll   : " + dy);

                    }
                    mFirstView = view.getChildAt(0);
                    mLastTop = mFirstView.getTop();

                }else {
                    mFirstView = view.getChildAt(0);
                    if(mFirstView!=null){
                        mLastTop = mFirstView.getTop();
                    }
                }

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"p"+position,Toast.LENGTH_SHORT).show();
            }
        });
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(),"Long"+position,Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

        final ContentWraperView contentWraperView = (ContentWraperView) findViewById(R.id.contentWraperview);
        final EasyScrollLayout easyScrollLayout = (EasyScrollLayout) findViewById(R.id.easyScrolllayout);
        easyScrollLayout.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                contentWraperView.setCanBottomFooterLoad(false);
                easyScrollLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        easyScrollLayout.setTopHeaderLoadComplete();
                        contentWraperView.setCanBottomFooterLoad(true);
                    }
                },3000);

            }
        });
        contentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                easyScrollLayout.setCanTopHeaderLoad(false);
                contentWraperView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentWraperView.setLoadComplete();
                        easyScrollLayout.setCanTopHeaderLoad(true);
                    }
                }, 3000);
            }
        });

    }
}
