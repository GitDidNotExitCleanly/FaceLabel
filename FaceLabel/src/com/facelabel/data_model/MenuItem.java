package com.facelabel.data_model;

import com.facelabel.R;

import android.graphics.Color;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

/*
 * Custom data structure for the menu item
 * 
 * @author Sheng Wang (psysw1@nottingham.ac.uk)
 * */

public class MenuItem {
	
	private LinearLayout container;
	
	private ImageView icon;
	
	private boolean isSelected;
	
	public MenuItem(LinearLayout container,ImageView icon) {
		this.container = container;	
		this.container.setBackgroundColor(Color.alpha(0));
		this.icon = icon;
		this.isSelected = false;
	}

	public LinearLayout getContainer() {
		return this.container;
	}
	
	public ImageView getIcon() {
		return this.icon;
	}
	
	// if it is selected, change text color and background alpha
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		if (isSelected) {
			this.container.setBackgroundColor(Color.argb(30, 255, 255, 255));
			if (String.valueOf(this.icon.getTag()).equals("viewGroups")) {
				this.icon.setImageResource(R.drawable.contact_selected);
			}
			if (String.valueOf(this.icon.getTag()).equals("addGroup")) {
				this.icon.setImageResource(R.drawable.face_detection_selected);
			}
			if (String.valueOf(this.icon.getTag()).equals("settings")) {
				this.icon.setImageResource(R.drawable.system_setting_selected);
			}
		}
		else {
			this.container.setBackgroundColor(Color.alpha(0));
			if (String.valueOf(this.icon.getTag()).equals("viewGroups")) {
				this.icon.setImageResource(R.drawable.contact);
			}
			if (String.valueOf(this.icon.getTag()).equals("addGroup")) {
				this.icon.setImageResource(R.drawable.face_detection);
			}
			if (String.valueOf(this.icon.getTag()).equals("settings")) {
				this.icon.setImageResource(R.drawable.system_setting);
			}
		}
	}
	
	public boolean isSelected() {
		return this.isSelected;
	}

	// set listener for the container (linearLayout)
	public void setOnClickListener(OnClickListener listener) {
		this.container.setOnClickListener(listener);
	}
}
