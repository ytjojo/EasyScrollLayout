package com.github.ytjojo.scrollmaster.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Uri uri = getIntent().getData();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this,ListViewActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.tv_webview).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_viewpager).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ViewPagerAcitivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_viewpagerh).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,DrawerViewpagerActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_topbanner).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,DrawerBannerActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_recylerview_viewpagerheader).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,DrawerRecylerViewWithHeaderActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_horizontal_refresh_loadmore).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,HorizentalLoadActivity .class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_horizontal_refresh_loadmore_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,HorizentalLoadItemActivity .class);
				startActivity(intent);
			}
		});
		findViewById(R.id.tv_viewpager_refresh_loadmore).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ViewPagerEachWithRefreshLoadMoreActivity .class);
				startActivity(intent);
			}
		});


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 400: { // 这个400就是上面defineSettingDialog()的第二个参数。
				// 你可以在这里检查你需要的权限是否被允许，并做相应的操作。
				break;
			}
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


}
