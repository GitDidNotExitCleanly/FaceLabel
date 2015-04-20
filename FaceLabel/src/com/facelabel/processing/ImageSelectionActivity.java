package com.facelabel.processing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.facelabel.BitmapLoader;
import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageSelectionActivity extends Activity {

	private final int CAMERA_REQUEST = 0;
	private String imageStoragePath;
	
	private final int ALBUM_REQUEST = 1;
	private String selectedImagePath;
	
	private TextView btnSelect;
	private TextView btnCancel;
	
	private ImageView img;
	private Bitmap bitmap;
	
	private String method;
	private String imgPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_selection);
		
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		method = getIntent().getExtras().getString("method");
		imgPath = getIntent().getExtras().getString("imgPath");
		
		setGUI();
		
		setListener();
	}

	private void setGUI() {
		
		this.btnSelect = (TextView)findViewById(R.id.activity_image_selection_select);
		this.btnCancel = (TextView)findViewById(R.id.activity_image_selection_cancel);
		this.img = (ImageView)findViewById(R.id.activity_image_selection_image);
		
		if (method.equals("camera")) {
			this.btnCancel.setText("Retake");
		}
		else if (method.equals("album")) {
			this.btnCancel.setText("Others");
		}
		else {}
		
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		this.bitmap = BitmapLoader.decodeBitmapFromFile(imgPath, size.x, size.y);
		this.img.setImageBitmap(bitmap);
	}
	
	private void setListener() {

		this.btnSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View item) {
				Intent intent = new Intent(ImageSelectionActivity.this,ImageProcessingActivity.class);
				intent.putExtra("imgPath", imgPath);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
    	});
		
		this.btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View item) {
				
				if (method.equals("camera")) {

					imageStoragePath = Environment.getExternalStorageDirectory()
			                .getAbsolutePath() + "/DCIM/Camera/"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".png";
			        File imageFile = new File(imageStoragePath);
			        Uri imageFileUri = Uri.fromFile(imageFile);
			 
			        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
			        startActivityForResult(intent, CAMERA_REQUEST);
				}
				else if (method.equals("album")) {
					
					Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, ALBUM_REQUEST);
				}
				else {}
				
			}
    	});
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
            	Point size = new Point();
        		getWindowManager().getDefaultDisplay().getSize(size);
        		int width = size.x;
        		int height = size.y;
        		this.bitmap = BitmapLoader.decodeBitmapFromFile(imageStoragePath, width, height);
        		this.img.setImageBitmap(bitmap);
        		
        		imgPath = imageStoragePath;
            }
        }
        else if (requestCode == ALBUM_REQUEST) {
        	 if (resultCode == RESULT_OK) {
        		Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaColumns.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);    
        	     
                selectedImagePath = cursor.getString(columnIndex);
        	    cursor.close();
        	     
        	    Point size = new Point();
         		getWindowManager().getDefaultDisplay().getSize(size);
         		int width = size.x;
         		int height = size.y;
         		this.bitmap = BitmapLoader.decodeBitmapFromFile(selectedImagePath, width, height);
         		this.img.setImageBitmap(bitmap);
         		
         		imgPath = selectedImagePath;
        	 }
        }
        else {}
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(ImageSelectionActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 1);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
