package com.github.ytjojo.scrollmaster.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.Toast;

import com.github.ytjojo.scrollmaster.BaseRefreshIndicator;
import com.github.ytjojo.scrollmaster.ContentWraperView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/14 0014.
 */

public class ViewPagerEachWithRefreshLoadMoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_withinnertop);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<Class<? extends Fragment>> fragments= new ArrayList<>();
        fragments.add(RecyclerViewFragment.class);
        fragments.add(ListFragment.class);
        fragments.add(ScrollViewFragment.class);
        fragments.add(WebViewFragment.class);
        viewPager.setAdapter(new ViewpagerFragmentAdapter(getSupportFragmentManager(),fragments));
    }
   public static class RecyclerViewFragment extends Fragment{
       ContentWraperView mContentWraperView;

       @Nullable
       @Override
       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
           return inflater.inflate(R.layout.fragment_contentwrapper_recyclervie,container,false);
       }

       @Override
       public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
           super.onViewCreated(view, savedInstanceState);
           mContentWraperView = (ContentWraperView) view;
           RecyclerView recyclerView = (RecyclerView) mContentWraperView.findViewById(R.id.recylerview);
           recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
           recyclerView.setItemAnimator(new DefaultItemAnimator());
           recyclerView.setAdapter(new BaseRecyclerViewAdapter(R.layout.item_simple,7));
           mContentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
               @Override
               public void onStartLoad() {
                   Toast.makeText(getContext(),"refresh",Toast.LENGTH_SHORT).show();

                   mContentWraperView.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           //                contentWraperView.setLoadComplete();
                           mContentWraperView.setTopHeaderLoadComplete();
                       }
                   },3000);
               }
           });
           mContentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
               @Override
               public void onStartLoad() {
                   Toast.makeText(getContext(),"loadmore",Toast.LENGTH_SHORT).show();
                   mContentWraperView.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           mContentWraperView.setLoadComplete();
//                contentWraperView.setBottomFooterLoadComplete();
                       }
                   },3000);

               }
           });
       }
   }
    public static class ListFragment extends Fragment{
        ContentWraperView mContentWraperView;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_contentwrapper_listview,container,false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mContentWraperView = (ContentWraperView) view;
            ListView listView = (ListView) mContentWraperView.findViewById(R.id.listview);
            ArrayList<String> stringArrayList = new ArrayList<>();
            for (int i = 0; i <5 ; i++) {
                stringArrayList.add(""+i);
            }
            listView.setAdapter(new BaseListViewAdapter<String>(stringArrayList,R.layout.item_list_simple) {
                @Override
                public void bindData2View(int position, String model, View convertView, ViewGroup parent) {

                }
            });

            mContentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"refresh",Toast.LENGTH_SHORT).show();

                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //                contentWraperView.setLoadComplete();
                            mContentWraperView.setTopHeaderLoadComplete();
                        }
                    },3000);
                }
            });
            mContentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"loadmore",Toast.LENGTH_SHORT).show();
                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mContentWraperView.setLoadComplete();
//                contentWraperView.setBottomFooterLoadComplete();
                        }
                    },3000);

                }
            });
        }
    }
    public static class WebViewFragment extends Fragment{
        ContentWraperView mContentWraperView;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_contentwrapper_webview,container,false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mContentWraperView = (ContentWraperView) view;
            WebView webView = (WebView) mContentWraperView.findViewById(R.id.webview);
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
//        webView.loadUrl("https://github.com/jeasonlzy/okhttp-OkGo");
            webView.loadUrl("http://www.jianshu.com/p/7caa5f4f49bd");
            mContentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"refresh",Toast.LENGTH_SHORT).show();

                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //                contentWraperView.setLoadComplete();
                            mContentWraperView.setTopHeaderLoadComplete();
                        }
                    },3000);
                }
            });
            mContentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"loadmore",Toast.LENGTH_SHORT).show();
                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mContentWraperView.setLoadComplete();
//                contentWraperView.setBottomFooterLoadComplete();
                        }
                    },3000);

                }
            });
        }
    }
    public static class ScrollViewFragment extends Fragment{
        ContentWraperView mContentWraperView;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_contentwrapper_scrollview,container,false);

        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mContentWraperView = (ContentWraperView) view;
            mContentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"refresh",Toast.LENGTH_SHORT).show();

                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //                contentWraperView.setLoadComplete();
                            mContentWraperView.setTopHeaderLoadComplete();
                        }
                    },3000);
                }
            });
            mContentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
                @Override
                public void onStartLoad() {
                    Toast.makeText(getContext(),"loadmore",Toast.LENGTH_SHORT).show();
                    mContentWraperView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mContentWraperView.setLoadComplete();
//                contentWraperView.setBottomFooterLoadComplete();
                        }
                    },3000);

                }
            });

        }
    }
}
