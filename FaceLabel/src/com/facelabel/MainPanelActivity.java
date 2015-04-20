package com.facelabel;

import com.facelabel.data_model.MenuItem;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainPanelActivity extends Activity {

	private ActionBar actionBar;

	private Fragment[] mFragments;

	private MenuItem[] menuItems;
	private OnClickListener menuListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_panel);

		 Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		int fragmentIndex = getIntent().getExtras().getInt("fragment");
		if (fragmentIndex == 0) {
			setActionBar(0);			
			setFragment(0);			
			setSplitActionBar(0);
		}
		else if (fragmentIndex == 1) {
			setActionBar(1);			
			setFragment(1);		
			setSplitActionBar(1);
		}
		else {
			setActionBar(2);			
			setFragment(2);		
			setSplitActionBar(2);
		}
	}

	private void setActionBar(int fragmentIndex) {
		actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		if (fragmentIndex == 0) {
			actionBar.setCustomView(R.layout.fragment_contact_action_bar);
		}
		else if (fragmentIndex == 1) {
			actionBar.setCustomView(R.layout.fragment_design_action_bar);
		}
		else {
			actionBar.setCustomView(R.layout.fragment_setting_action_bar);
		}
	}

	private void setFragment(int fragmentIndex) {
		// manage all fragments
		mFragments = new Fragment[3];

		mFragments[0] = getFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[1] = getFragmentManager().findFragmentById(R.id.fragment_design);
		mFragments[2] = getFragmentManager().findFragmentById(R.id.fragment_setting);

		// display the default fragment
		getFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).commit();
		getFragmentManager().beginTransaction().show(mFragments[fragmentIndex]).commit();	
	}

	private void setSplitActionBar(int fragmentIndex) {

		menuListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = String.valueOf(v.getTag());
				if (tag.equals("viewGroups")) {
					if (!menuItems[0].isSelected()) {
						// initialize all options
						getFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).commit();
						// select the one
						actionBar.setCustomView(R.layout.fragment_contact_action_bar);
						getFragmentManager().beginTransaction().show(mFragments[0]).commit();				

						// initialize all options
						for (int i=0;i<menuItems.length;i++) {
							menuItems[i].setSelected(false);
						}
						// select the one
						menuItems[0].setSelected(true);
					}
				}
				else if (tag.equals("settings")) {
					// settings
					if (!menuItems[2].isSelected()) {
						// initialize all options
						getFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).commit();
						// select the one
						actionBar.setCustomView(R.layout.fragment_setting_action_bar);
						getFragmentManager().beginTransaction().show(mFragments[2]).commit();

						// initialize all options
						for (int i=0;i<menuItems.length;i++) {
							menuItems[i].setSelected(false);
						}
						// select the one
						menuItems[2].setSelected(true);
					}
				}
				else {
					// design
					if (!menuItems[1].isSelected()) {
						// initialize all options
						getFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).commit();
						// select the one
						actionBar.setCustomView(R.layout.fragment_design_action_bar);
						getFragmentManager().beginTransaction().show(mFragments[1]).commit();

						// initialize all options
						for (int i=0;i<menuItems.length;i++) {
							menuItems[i].setSelected(false);
						}
						// select the one
						menuItems[1].setSelected(true);
					}
				}
			}
		};

		// group relevant views and set up listeners
		menuItems = new MenuItem[3];

		menuItems[0] = new MenuItem((LinearLayout)findViewById(R.id.activity_main_groups_menu),(ImageView)findViewById(R.id.activity_main_groups_icon));
		menuItems[0].setOnClickListener(menuListener);

		menuItems[1] = new MenuItem((LinearLayout)findViewById(R.id.activity_main_add_menu),(ImageView)findViewById(R.id.activity_main_add_icon));
		menuItems[1].setOnClickListener(menuListener);

		menuItems[2] = new MenuItem((LinearLayout)findViewById(R.id.activity_main_settings_menu),(ImageView)findViewById(R.id.activity_main_settings_icon));
		menuItems[2].setOnClickListener(menuListener);

		if (fragmentIndex == 0) {
			menuItems[0].setSelected(true);
		}
		else if (fragmentIndex == 1) {
			menuItems[1].setSelected(true);
		}
		else {
			menuItems[2].setSelected(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent i = new Intent(Intent.ACTION_MAIN); 
			i.addCategory(Intent.CATEGORY_HOME); 
			startActivity(i);
		}
		return super.onKeyDown(keyCode, event);
	}
}
