package com.facelabel.processing;

import com.facelabel.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

public class DesignFragment_Popup extends PopupWindow {

	private Button btn_recognize, btn_take_photo, btn_pick_photo, btn_cancel;
	private View mMenuView;
	
	public DesignFragment_Popup(Activity context, OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.fragment_design_popup, null);
		
		btn_recognize = (Button) mMenuView.findViewById(R.id.btn_recoginize_face);
		btn_take_photo = (Button) mMenuView.findViewById(R.id.btn_take_photo);
		btn_pick_photo = (Button) mMenuView.findViewById(R.id.btn_pick_photo);
		btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);

		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		btn_recognize.setOnClickListener(itemsOnClick);
		btn_pick_photo.setOnClickListener(itemsOnClick);
		btn_take_photo.setOnClickListener(itemsOnClick);
		
		this.setContentView(mMenuView);	
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);	
		this.setFocusable(true);		
		this.setAnimationStyle(R.style.PopupMenuAnimation);
		
		mMenuView.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
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


