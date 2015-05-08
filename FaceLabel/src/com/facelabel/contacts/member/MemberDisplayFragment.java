package com.facelabel.contacts.member;

import com.facelabel.BitmapLoader;
import com.facelabel.R;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberDisplayFragment extends Fragment {

	private int groupPosition;
	private int memberPosition;
	
	private ImageView photo;
	private TextView name;
	private TextView email;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_member_display, container,
				false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		groupPosition = getActivity().getIntent().getExtras().getInt("groupPosition");
		memberPosition = getActivity().getIntent().getExtras().getInt("memberPosition");
		
		setGUI();
	}
	
    private void setGUI() { 	
    	MemberInfo info = ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition);
    	this.photo = (ImageView)getActivity().findViewById(R.id.activity_member_photo);
    	this.photo.setImageBitmap(BitmapLoader.decodeBitmapFromFile(info.getPhoto(), 100, 100));
    	this.name = (TextView)getActivity().findViewById(R.id.activity_member_name);
    	this.name.setText(info.getName());
    	this.email = (TextView)getActivity().findViewById(R.id.activity_member_email);
    	this.email.setText(info.getEmail());
    }
}