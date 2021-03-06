package com.facelabel.contacts.group;

import java.io.File;

import com.facelabel.BitmapLoader;
import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.contacts.member.MemberActivity;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;
import com.facelabel.processing.crop.AddMissingMemberActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GroupActivity extends Activity {
	
	private int groupPosition;
	
	private ImageView btnBack;
	private TextView groupName;
	private ImageView setting;
	
	private PopupWindow dropdownMenu;
	
	private ImageView groupPhoto;
	private GridView memberList;
	private Button delete;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
	
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		groupPosition = getIntent().getExtras().getInt("groupPosition");
		
		setActionBar();
		
		setGUI();
		
		setListener();
	}
	
    private void setActionBar() {
    	ActionBar actionBar = getActionBar();
    	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    	actionBar.setCustomView(R.layout.activity_group_action_bar);
    }
    
    private void setGUI() {
    	
    	this.btnBack = (ImageView)findViewById(R.id.activity_group_button_back);
    	this.groupName = (TextView)findViewById(R.id.activity_group_groupName);
    	this.groupName.setText(ContactsData.getContacts().get(groupPosition).getGroupName());
    	this.setting = (ImageView)findViewById(R.id.activity_group_setting);
    	
    	this.groupPhoto = (ImageView)findViewById(R.id.activity_group_groupPhoto);
    	this.groupPhoto.setImageBitmap(BitmapLoader.decodeBitmapFromFile(ContactsData.getContacts().get(groupPosition).getGroupPhoto(), 300, 300));
    	
    	this.memberList = (GridView)findViewById(R.id.activity_group_memberList);
    	GroupAdapter adapter = new GroupAdapter(this);
    	adapter.setGroup(groupPosition);
    	this.memberList.setAdapter(adapter);
    	
    	this.delete = (Button)findViewById(R.id.activity_group_delete);
    }
    
    private void setListener() {

    	this.btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupActivity.this,MainPanelActivity.class);
				intent.putExtra("fragment", 0);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				finish();
			}
    		
    	});
    	
    	this.setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (dropdownMenu == null) {
					dropdownMenu = new GroupActivity_Dropdown(GroupActivity.this,itemsOnClick);
				}
				
				dropdownMenu.showAsDropDown(setting, -240, 20);
			}
    		
    	});
    	
    	this.memberList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View view, int position, long id) {
				Intent intent = new Intent(GroupActivity.this,MemberActivity.class);
				intent.putExtra("groupPosition", groupPosition);
				intent.putExtra("memberPosition", position);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
    		
    	});
    	
    	this.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
				builder.setTitle("Delete Group");
				builder.setMessage("Are you sure you want to delete this group?");
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				
				builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {    
				    
						new DeleteFiles().execute();
				    		
				    }
				});
				
				builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});
				
				builder.create().show();
			}
    		
    	});
    }
    
    private OnClickListener itemsOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			dropdownMenu.dismiss();
			
			int id = v.getId();
			if (id == R.id.activity_group_setting_changeName) {		
				AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
				builder.setTitle("Enter new name : ");
				
				final EditText input = new EditText(GroupActivity.this);
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				input.setText(groupName.getText());
				
				InputFilter[] fArray = new InputFilter[1];
				fArray[0] = new InputFilter.LengthFilter(15);
				input.setFilters(fArray);
				
				builder.setView(input);
				
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        String newName = input.getText().toString();
				        if (!newName.equals(groupName.getText())) {
				        	DatabaseHelper.getInstance(GroupActivity.this).changeGroupName(String.valueOf(ContactsData.getContacts().get(groupPosition).getGroupId()), newName);
				        	groupName.setText(newName);
							ContactsData.getContacts().get(groupPosition).setGroupName(newName);
				        }
				    }
				});
				
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});
				
				builder.create().show();
			}
			else if (id == R.id.activity_group_setting_addMissingMember) {
				Intent intent = new Intent(GroupActivity.this,AddMissingMemberActivity.class);
				intent.putExtra("groupPosition", groupPosition);
				startActivity(intent);
				finish();
			}
			else {}
			
		}

    };
    
    private class DeleteFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(GroupActivity.this, "", "Deleting ...", true, false);
		}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			DatabaseHelper.getInstance(GroupActivity.this).deleteGroup(String.valueOf(ContactsData.getContacts().get(groupPosition).getGroupId()));
			
			File dir = new File(ContactsData.getContacts().get(groupPosition).getGroupPhoto()).getParentFile();
			String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	            new File(dir, children[i]).delete();
	        }
	        dir.delete();
	        
			File xmlDir = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel/Model");
			if (xmlDir.exists()) {
				for (int i=0;i<ContactsData.getContacts().get(groupPosition).getGroupMembers().size();i++) {
					long member = ContactsData.getContacts().get(groupPosition).getGroupMembers().get(i).getId();
					new File(xmlDir,member+".xml").delete();
				}
			}
	        
	        ContactsData.getContacts().remove(groupPosition);
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {

			Intent intent = new Intent(GroupActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 0);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			finish();
			
			progressDialog.dismiss();
	    }
	}
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(GroupActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 0);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
