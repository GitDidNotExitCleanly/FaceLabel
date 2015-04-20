package com.facelabel.settings;

import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailedSettingActivity extends Activity {

	private String category;
	private String option;
	
	private TextView optionInActionBar;
	
	private ImageView btnBack;
	
	// others
	private Fragment[] others_mFragments;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailed_setting);
		
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		category = getIntent().getExtras().getString("category");
		option = getIntent().getExtras().getString("option");
		
		setActionBar();
		
		setGUI();
		
		setListener();
		
		setFragment();
	}

	private void setActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_detailed_setting_action_bar);
		optionInActionBar = (TextView)findViewById(R.id.activity_detailed_setting_action_bar_option);
		optionInActionBar.setText(option);
	}
	
	private void setGUI() {	
		this.btnBack = (ImageView)findViewById(R.id.activity_detailed_setting_action_bar_btnBack);
	}
	
	private void setListener() {
		this.btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DetailedSettingActivity.this,MainPanelActivity.class);
				intent.putExtra("fragment", 2);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				finish();
			}
			
		});
	}
	
	private void setFragment() {

		// others
		others_mFragments = new Fragment[2];
		others_mFragments[0] = getFragmentManager().findFragmentById(R.id.fragment_setting_others_about);
		others_mFragments[1] = getFragmentManager().findFragmentById(R.id.fragment_setting_others_author);

		getFragmentManager().beginTransaction().hide(others_mFragments[0]).hide(others_mFragments[1]).commit();
		
		// display
		if (category.equals("functions")) {
			
		}
		else if (category.equals("others")) {
			if (option.equals("About")) {
				getFragmentManager().beginTransaction().show(others_mFragments[0]).commit();
			}
			else if (option.equals("Author")) {
				getFragmentManager().beginTransaction().show(others_mFragments[1]).commit();
			}
			else {}
		}
		else {}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(DetailedSettingActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 2);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
