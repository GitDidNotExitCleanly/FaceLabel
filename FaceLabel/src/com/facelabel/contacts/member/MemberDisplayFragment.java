package com.facelabel.contacts.member;

import com.facelabel.BitmapLoader;
import com.facelabel.R;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.processing.faceRecognizer.TrainingActivity;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberDisplayFragment extends Fragment {

	private int groupPosition;
	private int memberPosition;
	
	private ImageView photo;
	private TextView name;
	private TextView phone;
	private TextView email;
	private TextView trainingState;
	
	private ImageButton dial;
	private Button train;
	
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
    	this.phone = (TextView)getActivity().findViewById(R.id.activity_member_phone);
    	this.phone.setText(info.getPhone());
    	this.email = (TextView)getActivity().findViewById(R.id.activity_member_email);
    	this.email.setText(info.getEmail());
    	
		this.dial = (ImageButton)getActivity().findViewById(R.id.fragment_member_display_dial);
		
		this.dial.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// dial
				if (!phone.getText().equals("")) {
					Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ phone.getText().toString().trim()));
					startActivity(callIntent);
				}
			}
			
		});
		
		this.trainingState = (TextView)getActivity().findViewById(R.id.activity_member_state);
		this.train = (Button)getActivity().findViewById(R.id.activity_member_train);
    	
		this.train.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),TrainingActivity.class);
				intent.putExtra("groupPosition", groupPosition);
				intent.putExtra("memberPosition", memberPosition);
				startActivity(intent);
				getActivity().finish();
			}
			
		});
	
		if (ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).getTrainingState().equals("true")) {
    		this.trainingState.setText("Trained");
    		
    		this.train.setEnabled(false);
    	}
    }
}
