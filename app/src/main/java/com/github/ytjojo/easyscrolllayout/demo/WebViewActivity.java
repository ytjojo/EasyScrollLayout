package com.github.ytjojo.easyscrolllayout.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.ytjojo.easyscrolllayout.BaseRefreshIndicator;
import com.github.ytjojo.easyscrolllayout.ContentWraperView;
import com.github.ytjojo.easyscrolllayout.EasyScrollLayout;

/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
//        webView.loadUrl("https://github.com/jeasonlzy/okhttp-OkGo");
        webView.loadUrl("http://www.jianshu.com/p/7caa5f4f49bd");
        EasyScrollLayout easyScrollLayout = (EasyScrollLayout) findViewById(R.id.easyScrolllayout);
        final ContentWraperView contentWraperView = (ContentWraperView) findViewById(R.id.contentWraperview);
        contentWraperView.setTopHeaderOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(),"refresh",Toast.LENGTH_SHORT).show();

                contentWraperView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //                contentWraperView.setLoadComplete();
                        contentWraperView.setTopHeaderLoadComplete();
                    }
                },3000);
            }
        });
        contentWraperView.setBottomFooterOnStartLoadCallback(new BaseRefreshIndicator.OnStartLoadCallback() {
            @Override
            public void onStartLoad() {
                Toast.makeText(getApplicationContext(),"loadmore",Toast.LENGTH_SHORT).show();
                contentWraperView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentWraperView.setLoadComplete();
//                contentWraperView.setBottomFooterLoadComplete();
                    }
                },3000);

            }
        });


    }
}
