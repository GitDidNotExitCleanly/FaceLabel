package com.facelabel.contacts.group;

import com.facelabel.BitmapLoader;
import com.facelabel.R;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private int position;
	
	public GroupAdapter(Context ctx) {
		this.inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return ContactsData.getContacts().get(position).getGroupMembers().size();
	}

	@Override
	public Object getItem(int index) {
		return ContactsData.getContacts().get(position).getGroupMembers().get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.activity_group_member_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView)convertView.findViewById(R.id.activity_group_member_name);
			viewHolder.photo = (ImageView)convertView.findViewById(R.id.activity_group_member_photo);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder)convertView.getTag();
		}

		MemberInfo info = ContactsData.getContacts().get(this.position).getGroupMembers().get(position);
		
		viewHolder.name.setText(info.getName());
		viewHolder.photo.setImageBitmap(BitmapLoader.decodeBitmapFromFile(info.getPhoto(),80,80));
		
		return convertView;
	}

	public void  setGroup(int position) {
		this.position = position;
		notifyDataSetChanged();
	}
	
	private class ViewHolder {
		TextView name;
		ImageView photo;
	}
}
