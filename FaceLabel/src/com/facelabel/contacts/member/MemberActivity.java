package com.facelabel.contacts.member;

import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.contacts.group.GroupActivity;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberActivity extends Activity {

	private int groupPosition;
	private int memberPosition;

	private ActionBar actionBar;
	
	private Fragment display;
	private Fragment edit;

	private boolean isEditable = false;	
	private ImageView leftButton;
	private ImageView rightButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member);
	
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		groupPosition = getIntent().getExtras().getInt("groupPosition");
		memberPosition = getIntent().getExtras().getInt("memberPosition");
		
		setFragment();
		
		setActionBar();
	}
	
	private void setFragment() {
		this.display = getFragmentManager().findFragmentById(R.id.fragment_member_display);
		this.edit = getFragmentManager().findFragmentById(R.id.fragment_member_edit);
		
		getFragmentManager().beginTransaction().hide(this.edit).hide(this.display).commit();
		getFragmentManager().beginTransaction().show(this.display).commit();
	}
	
    private void setActionBar() {
    	actionBar = getActionBar();
    	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    	actionBar.setCustomView(R.layout.activity_member_action_bar);
    	
    	this.leftButton = (ImageView)findViewById(R.id.activity_member_left_button);
    	this.rightButton = (ImageView)findViewById(R.id.activity_member_right_button);
    	
    	leftButton.setImageResource(R.drawable.back_button_effect);
		rightButton.setImageResource(R.drawable.edit_button_effect);

    	this.leftButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isEditable) {
					// edit cancel
					isEditable = false;
					
					leftButton.setImageResource(R.drawable.back_button_effect);
					rightButton.setImageResource(R.drawable.edit_button_effect);
					
					getFragmentManager().beginTransaction().hide(display).hide(edit).commit();
					getFragmentManager().beginTransaction().show(display).commit();
				}
				else {
					// button back
					Intent intent = new Intent(MemberActivity.this,GroupActivity.class);
					intent.putExtra("groupPosition", groupPosition);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					finish();
				}
			}
    		
    	});
    	
    	this.rightButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isEditable) {
					// edit OK
					EditText nameEdit = (EditText)findViewById(R.id.activity_member_name_edit);
					EditText phoneEdit = (EditText)findViewById(R.id.activity_member_phone_edit);
					EditText emailEdit = (EditText)findViewById(R.id.activity_member_email_edit);
					
					TextView nameDisplay = (TextView)findViewById(R.id.activity_member_name);
					TextView phoneDisplay = (TextView)findViewById(R.id.activity_member_phone);
					TextView emailDisplay = (TextView)findViewById(R.id.activity_member_email);
					
					boolean isModified = false;
					MemberInfo info = ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition);
					if (!nameEdit.getText().toString().equals(info.getName())) {
						isModified = true;
					}
					if (!phoneEdit.getText().toString().equals(info.getPhone())) {
						isModified = true;
					}
					if (!emailEdit.getText().toString().equals(info.getEmail())) {
						isModified = true;
					}
					if (isModified) {
						String newName = nameEdit.getText().toString();
						String newPhone = phoneEdit.getText().toString();
						String newEmail = emailEdit.getText().toString();
						DatabaseHelper.getInstance(MemberActivity.this).changePersonalInfo(String.valueOf(ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).getId()),newName,newPhone,newEmail);	
						nameDisplay.setText(newName);
						phoneDisplay.setText(newPhone);
						emailDisplay.setText(newEmail);
						ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).updateInfo(newName,newPhone,newEmail);
					}
					
					isEditable = false;
					
					leftButton.setImageResource(R.drawable.back_button_effect);
					rightButton.setImageResource(R.drawable.edit_button_effect);
					
					getFragmentManager().beginTransaction().hide(display).hide(edit).commit();
					getFragmentManager().beginTransaction().show(display).commit();
				}
				else {
					// edit
					isEditable = true;
					
					leftButton.setImageResource(R.drawable.edit_cancel_button_effect);
					rightButton.setImageResource(R.drawable.edit_done_button_effect);
					
					getFragmentManager().beginTransaction().hide(display).hide(edit).commit();
					getFragmentManager().beginTransaction().show(edit).commit();
				}
			}
    		
    	});
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(MemberActivity.this,GroupActivity.class);
			intent.putExtra("groupPosition", groupPosition);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
