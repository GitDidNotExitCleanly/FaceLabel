package com.facelabel.contacts.member;

import java.io.File;

import com.facelabel.BitmapLoader;
import com.facelabel.R;
import com.facelabel.contacts.group.GroupActivity;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MemberEditFragment extends Fragment {

	private int groupPosition;
	private int memberPosition;
	
	private ImageView photo;
	private EditText name;
	private EditText phone;
	private EditText email;
	
	private Button delete;
	
	private ProgressDialog progressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_member_edit, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		groupPosition = getActivity().getIntent().getExtras().getInt("groupPosition");
		memberPosition = getActivity().getIntent().getExtras().getInt("memberPosition");
		
		setGUI();
		
		setDeleteButton();
	}

	private void setGUI() {
		MemberInfo info = ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition);
		this.photo = (ImageView)getActivity().findViewById(R.id.activity_member_photo_edit);
    	this.photo.setImageBitmap(BitmapLoader.decodeBitmapFromFile(info.getPhoto(), 100, 100));
		this.name = (EditText)getActivity().findViewById(R.id.activity_member_name_edit);
    	this.name.setText(info.getName());
    	this.phone = (EditText)getActivity().findViewById(R.id.activity_member_phone_edit);
    	this.phone.setText(info.getPhone());
    	this.email = (EditText)getActivity().findViewById(R.id.activity_member_email_edit);
    	this.email.setText(info.getEmail());
	}
	
	private void setDeleteButton() {
		this.delete = (Button)getActivity().findViewById(R.id.activity_member_delete);
		
		this.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Delete Member");
				builder.setMessage("Are you sure you want to delete this member?");
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				
				builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {        
				    	
				    	new DeleteFile().execute();

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
	
	private class DeleteFile extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "", "Deleting ...", true, false);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			DatabaseHelper.getInstance(getActivity()).deleteMember(String.valueOf(ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).getId()));
			new File(ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).getPhoto()).delete();
			ContactsData.getContacts().get(groupPosition).getGroupMembers().remove(memberPosition);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {

			Intent intent = new Intent(getActivity(),GroupActivity.class);
			intent.putExtra("groupPosition", groupPosition);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			getActivity().finish();
			
			progressDialog.dismiss();
	    }
	}
}
