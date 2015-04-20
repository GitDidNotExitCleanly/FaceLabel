package com.facelabel.contacts;

import com.facelabel.BitmapLoader;
import com.facelabel.R;
import com.facelabel.data_model.GroupInfo;
import com.facelabel.database.ContactsData;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private ViewHolder viewHolder;
	
	public ContactAdapter(Context ctx) {
		this.inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return ContactsData.getContacts().size();
	}

	@Override
	public Object getItem(int index) {
		return ContactsData.getContacts().get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		viewHolder = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.fragment_contact_group_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView)convertView.findViewById(R.id.activity_contact_group_name);
			viewHolder.size = (TextView)convertView.findViewById(R.id.activity_contact_group_size);
			viewHolder.photo = (ImageView)convertView.findViewById(R.id.activity_contact_group_photo);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder)convertView.getTag();
		}

		GroupInfo info = ContactsData.getContacts().get(position);
			
		viewHolder.name.setText(info.getGroupName());
		viewHolder.size.setText(String.valueOf(info.getGroupMembers().size()));
		viewHolder.photo.setImageBitmap(BitmapLoader.decodeBitmapFromFile(info.getGroupPhoto(), 50, 25));

		return convertView;
	}
	
	private class ViewHolder {
		TextView name;
		TextView size;
		ImageView photo;
	}
}
