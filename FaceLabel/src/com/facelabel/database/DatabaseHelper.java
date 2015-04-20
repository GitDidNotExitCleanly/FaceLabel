package com.facelabel.database;

import com.facelabel.data_model.GroupInfo;
import com.facelabel.data_model.MemberInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static DatabaseHelper singleton=null;
	
	private static final String DATABASE_NAME="faceLabel.db";
	private static final int SCHEMA = 1;
	
	public static final String TABLE_GROUP = "faceLabelGroups";
	public static final String GROUP_ID = "groupID";
	public static final String GROUP_NAME = "groupNAME";
	public static final String GROUP_PHOTO = "photo";
	
	public static final String TABLE_MEMBERS = "faceLabelMembers";
	public static final String MEMBER_NAME = "member";
	public static final String MEMBER_PHOTO = "photo";
	public static final String MEMBER_EMAIL = "email";
	
	public synchronized static DatabaseHelper getInstance(Context ctx) {
		if (singleton == null) {
			singleton=new DatabaseHelper(ctx.getApplicationContext());
		}
		return singleton;
	}
	
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			db.execSQL("CREATE TABLE faceLabelGroups (id INTEGER PRIMARY KEY AUTOINCREMENT,"+GROUP_NAME+" TEXT,"+GROUP_PHOTO+" TEXT);");
			db.execSQL("CREATE TABLE faceLabelMembers (id INTEGER PRIMARY KEY,"+GROUP_ID+" LONG,"+MEMBER_NAME+" TEXT,"+MEMBER_PHOTO+" TEXT,"+MEMBER_EMAIL+" TEXT);");			
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// update
	}
	
	public long getLastInsertedID() {
		String query = "SELECT id from "+ TABLE_GROUP +" order by id DESC limit 1";
		Cursor cursor = getReadableDatabase().rawQuery(query, null);
		if (cursor != null && cursor.moveToFirst()) 
		{
		    return cursor.getLong(0);
		}
		else {
			return 0;
		}
	}
	
	public long getLastInsertedMemberID() {
		String query = "SELECT id from "+ TABLE_MEMBERS +" order by id DESC limit 1";
		Cursor cursor = getReadableDatabase().rawQuery(query, null);
		if (cursor != null && cursor.moveToFirst()) 
		{
		    return cursor.getLong(0);
		}
		else {
			return 0;
		}
	}
	
	public void addMember(MemberInfo member) {
		new AddMember().execute(member);
	}
	
	private class AddMember extends AsyncTask<MemberInfo,Void,Void> {

		@Override
		protected Void doInBackground(MemberInfo... member) {
			
			ContentValues cv = new ContentValues();
			cv.put(GROUP_ID, member[0].getGroupId());
			cv.put(MEMBER_NAME, member[0].getName());
			cv.put(MEMBER_PHOTO, member[0].getPhoto());
			cv.put(MEMBER_EMAIL, member[0].getEmail());
			getWritableDatabase().insert(TABLE_MEMBERS, null, cv);
			return null;
		}
		
	}
	
	public void addGroup(GroupInfo group) {
		new AddGroup().execute(group);
	}
	
	private class AddGroup extends AsyncTask<GroupInfo,Void,Void> {

		@Override
		protected Void doInBackground(GroupInfo... group) {
			
			ContentValues cv = new ContentValues();
			cv.put(GROUP_NAME, group[0].getGroupName());
			cv.put(GROUP_PHOTO, group[0].getGroupPhoto());
			long groupId = getWritableDatabase().insert(TABLE_GROUP, null, cv);
			
			for (int i=0;i<group[0].getGroupMembers().size();i++) {
				MemberInfo info = group[0].getGroupMembers().get(i);
				cv = new ContentValues();
				cv.put(GROUP_ID, groupId);
				cv.put(MEMBER_NAME, info.getName());
				cv.put(MEMBER_PHOTO, info.getPhoto());
				cv.put(MEMBER_EMAIL, info.getEmail());
				getWritableDatabase().insert(TABLE_MEMBERS, null, cv);
			}
			
			return null;
		}
		
	}
	
	public void changeGroupName(String groupId, String newName) {
		new ChangeGroupName().execute(new String[]{groupId,newName});
	}
	
	private class ChangeGroupName extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			ContentValues cv = new ContentValues();
			cv.put(GROUP_NAME, params[1]);
			getWritableDatabase().update(TABLE_GROUP, cv, "id=?", new String[]{ params[0] });
			return null;
		}
		
	}
	
	public void changePersonalInfo(String memberId, String name, String email) {
		new ChangePersonalInfo().execute(memberId,name,email);
	}
	
	private class ChangePersonalInfo extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			ContentValues cv = new ContentValues();
			cv.put(MEMBER_NAME, params[1]);
			cv.put(MEMBER_EMAIL, params[2]);
			getWritableDatabase().update(TABLE_MEMBERS, cv, "id=?", new String[]{ params[0] });
			return null;
		}
		
	}
	
	public void deleteMember(String memberId) {
		new DeleteMember().execute(memberId);
	}
	
	private class DeleteMember extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			getWritableDatabase().delete(TABLE_MEMBERS, "id=?", new String[]{params[0]});
			return null;
		}
		
	}
	
	public void deleteGroup(String groupId) {
		new DeleteGroup().execute(groupId);
	}
	
	private class DeleteGroup extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			getWritableDatabase().delete(TABLE_GROUP, "id=?", new String[]{params[0]});
			return null;
		}
		
	}
	
	public void deleteAll() {
		new DeleteAll().execute();
	}
	
	private class DeleteAll extends AsyncTask<String,Void,Void> {

		@Override
		protected Void doInBackground(String... params) {
			getWritableDatabase().delete(TABLE_GROUP, null, null);
			getWritableDatabase().delete(TABLE_MEMBERS, null, null);
			return null;
		}
		
	}
}
