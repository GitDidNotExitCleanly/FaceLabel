package com.facelabel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.facelabel.processing.DesignFragment_Popup;
import com.facelabel.processing.ImageSelectionActivity;
import com.facelabel.processing.faceRecognizer.FaceRecognizerActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;

public class DesignFragment extends Fragment {

	private Button startDesign;
	
	private RelativeLayout popupMenuBackground;
	private PopupWindow popupMenu;
	
	private final int CAMERA_REQUEST = 0;
	private String imageStoragePath;

	private final int ALBUM_REQUEST = 1;
	private String selectedImagePath;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_design, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setGUI();
		
		setListener();
	}
	
	private void setGUI() {
		
		this.startDesign = (Button)getActivity().findViewById(R.id.fragment_design_start);
	}
	
	private void setListener() {
		
		this.startDesign.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (popupMenu == null) {
					popupMenuBackground = (RelativeLayout)getActivity().findViewById(R.id.fragment_design_popup_background);
					popupMenu = new DesignFragment_Popup(getActivity(), popupMenuListener);
					popupMenu.setOnDismissListener(popupMenuDismissListener);
				}
				
				popupMenu.showAtLocation(popupMenuBackground, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
				popupMenuBackground.setBackgroundColor(0xb0000000);
				getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(0xb0000000));
			}
		});
		
	}
	
	private OnDismissListener popupMenuDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss() {
			popupMenuBackground.setBackgroundColor(Color.TRANSPARENT);
			getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}
	};
	
	private OnClickListener popupMenuListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			popupMenu.dismiss();

			Intent intent;
			switch (v.getId()) {
			case R.id.btn_take_photo:

				imageStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".png";
				File imageFile = new File(imageStoragePath);
				Uri imageFileUri = Uri.fromFile(imageFile);

				intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
				startActivityForResult(intent, CAMERA_REQUEST);	

				break;
			case R.id.btn_pick_photo:

				intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, ALBUM_REQUEST);
				
				break;
			case R.id.btn_recoginize_face:
				
				intent = new Intent(getActivity(),FaceRecognizerActivity.class);
				startActivity(intent);
				getActivity().finish();
				
				break;
			}
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_REQUEST) {
			getActivity();
			if (resultCode == Activity.RESULT_OK) {

				Intent i = new Intent(getActivity(),ImageSelectionActivity.class);
				i.putExtra("method", "camera");
				i.putExtra("imgPath", imageStoragePath);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				getActivity().finish();
			}
		}
		else if (requestCode == ALBUM_REQUEST) {
			getActivity();
			if (resultCode == Activity.RESULT_OK) {

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaColumns.DATA };
				Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);    

				selectedImagePath = cursor.getString(columnIndex);
				cursor.close();

				Intent i = new Intent(getActivity(),ImageSelectionActivity.class);
				i.putExtra("method", "album");
				i.putExtra("imgPath", selectedImagePath);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				getActivity().finish();
			}
		}
		else {}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (popupMenu != null) {
			popupMenu.dismiss();
			popupMenuBackground.setBackgroundColor(Color.TRANSPARENT);
			getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}
	}
}
