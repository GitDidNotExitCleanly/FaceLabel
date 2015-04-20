package com.facelabel.contacts.group;

import com.facelabel.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class GroupActivity_Dropdown extends PopupWindow {

	private LinearLayout changeName;
	private LinearLayout addMember;
	private View mDropdownMenuView;
	
	public GroupActivity_Dropdown(Activity context, OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDropdownMenuView = inflater.inflate(R.layout.activity_group_setting_dropdown, null);
		
		this.changeName = (LinearLayout)mDropdownMenuView.findViewById(R.id.activity_group_setting_changeName);
		this.addMember = (LinearLayout)mDropdownMenuView.findViewById(R.id.activity_group_setting_addMissingMember);
		this.changeName.setOnClickListener(itemsOnClick);
		this.addMember.setOnClickListener(itemsOnClick);
		
		this.setContentView(mDropdownMenuView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		
		mDropdownMenuView.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				int height = mDropdownMenuView.findViewById(R.id.dropdown_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction() == MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
	}
	
}
