package com.facelabel;

import com.facelabel.contacts.ContactAdapter;
import com.facelabel.contacts.group.GroupActivity;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactFragment extends Fragment {

	private ListView contactList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setGUI();
		
		setListener();
	}
	
	private void setGUI() {
 
    	this.contactList = (ListView)getActivity().findViewById(R.id.fragment_contact_listView);
    	ContactAdapter adapter = new ContactAdapter(getActivity());
    	this.contactList.setAdapter(adapter);
    }
	
	private void setListener() {

    	this.contactList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View view, int position, long id) {
				Intent intent = new Intent(getActivity(),GroupActivity.class);
				intent.putExtra("groupPosition", position);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				getActivity().finish();
			}
    		
    	});
    }

}
