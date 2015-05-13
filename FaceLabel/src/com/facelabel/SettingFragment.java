package com.facelabel;

import java.io.File;

import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;
import com.facelabel.settings.DetailedSettingActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class SettingFragment extends Fragment {
	
	// functions
	private RelativeLayout clear_data;
	
	// others
	private RelativeLayout about;
	private RelativeLayout author;
	
	private ProgressDialog progressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setting, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setGUI();
		
		setListener();
	}
	
	private void setGUI() {
		// functions
		this.clear_data = (RelativeLayout)getActivity().findViewById(R.id.fragment_setting_functions_clear_data);
		
		// others
		this.about = (RelativeLayout)getActivity().findViewById(R.id.fragment_setting_others_about);
		this.author = (RelativeLayout)getActivity().findViewById(R.id.fragment_setting_others_author);
	}
	
	private void setListener() {
		
		// functions
		this.clear_data.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new  AlertDialog.Builder(getActivity());
				builder.setTitle("Delete All Content")
						.setMessage("Are you sure you want to delete all content?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        @Override
							public void onClick(DialogInterface dialog, int which) { 
					            new DeleteAllContent().execute();
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        @Override
							public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     })
					    .setIcon(android.R.drawable.ic_dialog_alert)
					    .show();
			}
			
		});
		
		// others
		this.about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),DetailedSettingActivity.class);
				intent.putExtra("category", "others");
				intent.putExtra("option", "About");
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				getActivity().finish();
			}
			
		});
		
		this.author.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),DetailedSettingActivity.class);
				intent.putExtra("category", "others");
				intent.putExtra("option", "Author");
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				getActivity().finish();
			}
			
		});
	}
	
    private class DeleteAllContent extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "", "Deleting ...", true, false);
		}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			DatabaseHelper.getInstance(getActivity());
			
			File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel/Groups");
			String[] children = mediaStorageDir.list();
	        for (int i = 0; i < children.length; i++) {
	            new File(mediaStorageDir, children[i]).delete();
	        }
	        
			File xmlDir = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel/Model");
			if (xmlDir.exists()) {
				String[] xmlChildren = xmlDir.list();
				for (int i = 0; i < xmlChildren.length; i++) {
		            new File(xmlDir, xmlChildren[i]).delete();
		        }
			}
	        
	        ContactsData.getContacts().clear();
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {
			progressDialog.dismiss();
	    }
	}
}
