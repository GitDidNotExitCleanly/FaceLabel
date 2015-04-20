package com.facelabel.processing.crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.facelabel.BitmapLoader;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.contacts.group.GroupActivity;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AddMissingMemberActivity extends Activity {

	private int groupPosition;
	
	private CropView cropView;	

	private TextView btnCancel;
	private TextView btnOK;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_missing_member);
		
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		groupPosition = getIntent().getExtras().getInt("groupPosition");
		
		setGUI();
		
		setListener();
	}

	private void setGUI() {
		
		cropView = (CropView)findViewById(R.id.activity_add_missing_member_cropview);
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		cropView.setBmp(BitmapLoader.decodeBitmapFromFile(ContactsData.getContacts().get(groupPosition).getGroupPhoto(), size.x, size.y),size.x,size.y);

		this.btnCancel = (TextView)findViewById(R.id.activity_add_missing_member_cancel);
		this.btnOK = (TextView)findViewById(R.id.activity_add_missing_member_ok);
	}
	
	private void setListener() {
		
		this.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddMissingMemberActivity.this,GroupActivity.class);
				intent.putExtra("groupPosition", groupPosition);
				startActivity(intent);
				finish();
			}
			
		});
	
		this.btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				new AddMember().execute();
			}
			
		});
	}
	
	private class AddMember extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(AddMissingMemberActivity.this, "", "Generating ...", true, false);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			long currentGroupId = ContactsData.getContacts().get(groupPosition).getGroupId();
			long currentMemberId = DatabaseHelper.getInstance(AddMissingMemberActivity.this).getLastInsertedMemberID() + 1;	
			Bitmap croppedImage = cropView.getCroppedImage();
			
			File imageFile = getOutputMediaFile(currentGroupId,currentMemberId);
			storeImage(croppedImage,imageFile);
			
			MemberInfo newMember = new MemberInfo(currentGroupId,currentMemberId,"New Member",imageFile.getAbsolutePath(),"");
			DatabaseHelper.getInstance(AddMissingMemberActivity.this).addMember(newMember);
			ContactsData.getContacts().get(groupPosition).getGroupMembers().add(newMember);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {

			Intent intent = new Intent(AddMissingMemberActivity.this,GroupActivity.class);
			intent.putExtra("groupPosition", groupPosition);
			startActivity(intent);
			finish();
			
			progressDialog.dismiss();
	    }
		
		private  File getOutputMediaFile(long groupID,long memberID){
		    // To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this. 
		    File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
		            + "/Android/data/"
		            + getApplicationContext().getPackageName()
		            + "/Groups");
		    
		    // Create the storage directory if it does not exist
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		            return null;
		        }
		    }
		    
		    File groupDir = new File(mediaStorageDir,String.valueOf(groupID));
		    if (! groupDir.exists()){
		        if (! groupDir.mkdirs()){
		            return null;
		        }
		    }
		    
		    // Create a media file name
		    String mImageName="Member"+ memberID +".png";
		    File mediaFile = new File(groupDir.getPath() + File.separator + mImageName);
		    return mediaFile;
		} 
		
		private void storeImage(Bitmap image,File mediaFile) {
		    try {
		    	FileOutputStream fos = new FileOutputStream(mediaFile);
		    	image.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
		    } catch (FileNotFoundException e) {
		        Log.d("File not found", e.getMessage());
		    } catch (IOException e) {
		        Log.d("Error accessing file", e.getMessage());
		    }  
		}	
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(AddMissingMemberActivity.this,GroupActivity.class);
			intent.putExtra("groupPosition", groupPosition);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
