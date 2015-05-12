package com.facelabel;

import java.util.ArrayList;

import com.facelabel.data_model.GroupInfo;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;
import com.facelabel.processing.faceRecognizer.Recognizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

public class LoadingActivity extends Activity {

	private class LoadContent extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			Recognizer.getInstance();
			
			SQLiteDatabase db = DatabaseHelper.getInstance(LoadingActivity.this).getReadableDatabase();

			Cursor cursor = db.query(DatabaseHelper.TABLE_GROUP, null, null, null, null, null, null);
			
			ContactsData.getContacts().clear();
			while (cursor.moveToNext()) {
				int groupIdIndex = cursor.getColumnIndex("id");
				int groupNameIndex = cursor.getColumnIndex(DatabaseHelper.GROUP_NAME);
				int groupPhotoIndex = cursor.getColumnIndex(DatabaseHelper.GROUP_PHOTO);
				long groupId = cursor.getLong(groupIdIndex);
				String groupName = cursor.getString(groupNameIndex);
				String groupPhoto = cursor.getString(groupPhotoIndex);
				
				ArrayList<MemberInfo> members = new ArrayList<MemberInfo>();
				Cursor memberCursor = db.query(DatabaseHelper.TABLE_MEMBERS, new String[]{"id",DatabaseHelper.MEMBER_NAME,DatabaseHelper.MEMBER_PHOTO,DatabaseHelper.MEMBER_PHONE,DatabaseHelper.MEMBER_EMAIL}, DatabaseHelper.GROUP_ID+"=?", new String[]{String.valueOf(groupId)}, null, null, null);
				while (memberCursor.moveToNext()) {
					int memberIdIndex = memberCursor.getColumnIndex("id");
					int memberNameIndex = memberCursor.getColumnIndex(DatabaseHelper.MEMBER_NAME);
					int memberPhotoIndex = memberCursor.getColumnIndex(DatabaseHelper.MEMBER_PHOTO);
					int memberPhoneIndex = memberCursor.getColumnIndex(DatabaseHelper.MEMBER_PHONE);
					int memberEmailIndex = memberCursor.getColumnIndex(DatabaseHelper.MEMBER_EMAIL);
					long id = memberCursor.getLong(memberIdIndex);
					String name = memberCursor.getString(memberNameIndex);
					String photo = memberCursor.getString(memberPhotoIndex);
					String phone = memberCursor.getString(memberPhoneIndex);
					String email = memberCursor.getString(memberEmailIndex);
					
					MemberInfo member = new MemberInfo(groupId,id,name,photo,phone,email);
					members.add(member);
				}
				memberCursor.close();
				
				GroupInfo group = new GroupInfo(groupId,groupName,groupPhoto,members);
				ContactsData.getContacts().add(group);
			}
			
			cursor.close();
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean isFinished) {
			if (isFinished) {
				Intent intent = new Intent(LoadingActivity.this,MainPanelActivity.class);
				intent.putExtra("fragment", 0);
				
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		
		new LoadContent().execute();
	}
}
