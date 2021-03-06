package com.facelabel.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.facelabel.BitmapLoader;
import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.data_model.GroupInfo;
import com.facelabel.data_model.MemberInfo;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageProcessingActivity extends Activity {

	private ImageView btnBack;
	private TextView btnUse;
	
	private ImageView img;
	private Bitmap bitmap;
	
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_processing);
		
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
		setGUI();
		
		setListener();
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, ImageProcessingActivity.this, mLoaderCallback);
	}

	private void setGUI() {
		this.btnBack = (ImageView)findViewById(R.id.activity_image_processing_button_back);
		this.btnUse = (TextView)findViewById(R.id.activity_image_processing_use);
		this.img = (ImageView)findViewById(R.id.activity_image_processing_image);
		
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		this.bitmap = BitmapLoader.decodeBitmapFromFile(getIntent().getExtras().getString("imgPath"), size.x, size.y);
		this.img.setImageBitmap(bitmap);
	}
	
	private void setListener() {

		this.btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ImageProcessingActivity.this,MainPanelActivity.class);
				intent.putExtra("fragment", 1);
				startActivity(intent);
				finish();
			}
			
		});
		
		this.btnUse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View item) {
				if (mJavaDetector != null) {
					
					btnBack.setEnabled(false);
					btnUse.setEnabled(false);
					
					new DetectFaces().execute(bitmap);
				}
			}   		
    	});
	}

	private CascadeClassifier mJavaDetector;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
					Log.i("OpenCVManager", "Connection Succeed");
					
					try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        
                        if (mJavaDetector.empty()) {
                            Log.e("OpenCVManager", "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i("OpenCVManager", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mCascadeFile.delete();
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("OpenCVManager", "Failed to load cascade. Exception thrown: " + e);
                    }
					
					break;
				default:
					super.onManagerConnected(status);
					Log.i("OpenCVManager", "Connection Fail");
					break;
			}
		}
	};
	
	private class DetectFaces extends AsyncTask<Bitmap, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(ImageProcessingActivity.this, "", "Generating ...", true, false);
		}
		
		@Override
		protected Void doInBackground(Bitmap... src) {

			Mat rgbMat = new Mat();
			Mat grayMat = new Mat();
			Utils.bitmapToMat(src[0], rgbMat);
			Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
			
			MatOfRect faces = new MatOfRect();
			if (mJavaDetector != null) {
				mJavaDetector.detectMultiScale(grayMat, faces, 1.1, 5, 2, new Size(20, 20), new Size());
			}
			
			org.opencv.core.Rect[] facesArray = faces.toArray();
			
			Bitmap[] groupPhotos = new Bitmap[facesArray.length+1];
			groupPhotos[0] = bitmap;
			if (facesArray.length != 0) {
				for (int i=0;i<facesArray.length;i++) {
					Mat faceMat = rgbMat.submat(facesArray[i]);
					groupPhotos[i+1] = Bitmap.createBitmap(faceMat.cols(), faceMat.rows(), Config.RGB_565);
					Utils.matToBitmap(faceMat, groupPhotos[i+1]);
				}
			}

			long lastGroupId = DatabaseHelper.getInstance(ImageProcessingActivity.this).getLastInsertedID();
			long lastMemberId = DatabaseHelper.getInstance(ImageProcessingActivity.this).getLastInsertedMemberID();
			
			File[] imageFiles = getOutputMediaFile(groupPhotos,lastGroupId,lastMemberId);
			
			storeImages(groupPhotos, imageFiles);
			
			// group member list
			ArrayList<MemberInfo> groupMembers = new ArrayList<MemberInfo>();
			for (int i=0;i<imageFiles.length-1;i++) {
				String name = "Member"+i;
				String photoPath = imageFiles[i+1].getAbsolutePath();
				String phone = "";
				String email = "";
				groupMembers.add(new MemberInfo(lastGroupId+1,lastMemberId+i+1,name,photoPath,phone,email,"false"));
			}
			
			String groupName = "Undefined";
			String groupPhotoPath = imageFiles[0].getAbsolutePath();
			GroupInfo newGroup = new GroupInfo(lastGroupId+1,groupName,groupPhotoPath,groupMembers);
			
			DatabaseHelper.getInstance(ImageProcessingActivity.this).addGroup(newGroup);
			ContactsData.getContacts().add(newGroup);

			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {
			
			Intent intent = new Intent(ImageProcessingActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 0);
			startActivity(intent);
			finish();
			
			progressDialog.dismiss();
	    }
		
		private  File[] getOutputMediaFile(Bitmap[] images,long lastGroupId,long lastMemberId){
		    // To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this. 
		    File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
		            + "/Android/data/"
		            + getApplicationContext().getPackageName()
		            + "/Groups");
		    
		    // Create the storage directory if it does not exist
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		            return null;
		        }
		    }
		    
		    File groupDir = new File(mediaStorageDir,String.valueOf(lastGroupId+1));
		    if (! groupDir.exists()){
		        if (! groupDir.mkdirs()){
		            return null;
		        }
		    }
		    
		    // Create a media file name
		    File[] mediaFiles = new File[images.length];
		    mediaFiles[0] = new File(groupDir.getPath() + File.separator + "Group.png");
		    for (int i=1;i<images.length;i++) {	    	
			    String mImageName="Member"+ (lastMemberId+i) +".png";
			    mediaFiles[i] = new File(groupDir.getPath() + File.separator + mImageName);
		    }
		    return mediaFiles;
		} 
		
		private void storeImages(Bitmap[] images, File[] pictureFiles) {
		    try {
		    	for (int i=0;i<images.length;i++) {
		    		FileOutputStream fos = new FileOutputStream(pictureFiles[i]);
		    		images[i].compress(Bitmap.CompressFormat.PNG, 100, fos);
				    fos.close();
		    	}
		    } catch (FileNotFoundException e) {
		        Log.d("File not found", e.getMessage());
		    } catch (IOException e) {
		        Log.d("Error accessing file", e.getMessage());
		    }  
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Intent intent = new Intent(ImageProcessingActivity.this,MainPanelActivity.class);
			intent.putExtra("fragment", 1);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
