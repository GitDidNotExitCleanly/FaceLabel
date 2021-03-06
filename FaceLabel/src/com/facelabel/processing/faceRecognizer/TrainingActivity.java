package com.facelabel.processing.faceRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.contacts.member.MemberActivity;
import com.facelabel.database.ContactsData;
import com.facelabel.database.DatabaseHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class TrainingActivity extends Activity implements CvCameraViewListener2 {

	private ProgressDialog progressDialog;
	
	private ImageButton takePhoto;
	private ImageButton train;
	private Button cancel;
	
	private int photoSize = 0;
	private ImageView[] trainingPhotos;
	
	private int windowWidth;
	private int windowHeight;
	
	private int mMaxDetectWidth;
	private int mMaxDetectHeight;
	
	private Mat lastValidRect;
	private Mat currentRect;
	
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;

    private Mat                    mRgba;
    private Mat                    mGray;   
    private CascadeClassifier      mJavaDetector;
    
    private CascadeClassifier 	   eyesDetector;
    private CascadeClassifier 	   mouthDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;

    private float                  mRelativeFaceSize   = 0.45f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                	
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
                            mJavaDetector = null;
                        }

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_mcs_eyepair_big);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "haarcascade_mcs_eyepair_big.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        eyesDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (eyesDetector.empty()) {
                        	eyesDetector = null;
                        }
                        
                        // load cascade file from application resources
                        is = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
                        mCascadeFile = new File(cascadeDir, "haarcascade_mcs_mouth.xml");
                        os = new FileOutputStream(mCascadeFile);

                        buffer = new byte[4096];
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mouthDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mouthDetector.empty()) {
                        	mouthDetector = null;
                        }

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                   
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.activity_training);
		
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		
    	Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		this.windowWidth = size.x;
		this.windowHeight = size.y;
		this.mMaxDetectWidth = size.x/2;
		this.mMaxDetectHeight = size.y/2;
		
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.training_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
		
		setTrainingPhotoList();
		
		setButtons();
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
	}

	private void setTrainingPhotoList() {
		this.trainingPhotos = new ImageView[5];
		this.trainingPhotos[0] = (ImageView)findViewById(R.id.trainingImage1);
		this.trainingPhotos[1] = (ImageView)findViewById(R.id.trainingImage2);
		this.trainingPhotos[2] = (ImageView)findViewById(R.id.trainingImage3);
		this.trainingPhotos[3] = (ImageView)findViewById(R.id.trainingImage4);
		this.trainingPhotos[4] = (ImageView)findViewById(R.id.trainingImage5);
	}
	
	private void setButtons() {
		this.takePhoto = (ImageButton)findViewById(R.id.training_take);
		this.train = (ImageButton)findViewById(R.id.training_train);
		this.cancel = (Button)findViewById(R.id.training_cancel);
		
		this.takePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (currentRect != null && photoSize < 5) {
					if (mOpenCvCameraView != null) {
			            mOpenCvCameraView.disableView();
					}
					
			    	new CaptureFace().execute();
				}
				else {
					if (photoSize >= 5) {
						Toast toast = Toast.makeText(TrainingActivity.this, "Maximum photo size", 200);
						toast.show();
					}
					else {
						Toast toast = Toast.makeText(TrainingActivity.this, "Please wait until getting a stable image", 200);
						toast.show();
					}
				}
			}
			
		});
		
		this.train.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (photoSize < 5) {
					Toast toast = Toast.makeText(TrainingActivity.this, "Not enough training examples", 200);
					toast.show();
				}
				else {
					int groupPosition = getIntent().getExtras().getInt("groupPosition");
					int memberPosition = getIntent().getExtras().getInt("memberPosition");
					long memberID = ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).getId();
					
					new Train().execute(memberID);
				}
			}
			
		});
		
		this.cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TrainingActivity.this,MemberActivity.class);
				intent.putExtra("groupPosition", getIntent().getExtras().getInt("groupPosition"));
				intent.putExtra("memberPosition", getIntent().getExtras().getInt("memberPosition"));
				startActivity(intent);
				finish();
			}
			
		});
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
        mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null) 	
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 5, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }

        Rect[] facesArray = faces.toArray();
        int maxSize = 0;
        int index = -1;
        for (int i = 0; i < facesArray.length; i++) {
        	if ((facesArray[i].width*facesArray[i].height) > maxSize && (int)(facesArray[i].tl().x+facesArray[i].br().x)/2 >= (windowWidth-mMaxDetectWidth)/2 && (int)(facesArray[i].tl().x+facesArray[i].br().x)/2 <= windowWidth-((windowWidth-mMaxDetectWidth)/2) && (int)(facesArray[i].tl().y+facesArray[i].br().y)/2 >= (windowHeight-mMaxDetectHeight)/2 && (int)(facesArray[i].tl().y+facesArray[i].br().y)/2 <= windowHeight-((windowHeight-mMaxDetectHeight)/2)) {
        		maxSize = facesArray[i].width*facesArray[i].height;
        		index = i;
        	}
        }
        if (index != -1) {
        	Core.rectangle(mRgba, facesArray[index].tl(), facesArray[index].br(), FACE_RECT_COLOR, 3);
        	this.currentRect = mRgba.submat(facesArray[index]);
        	this.lastValidRect = mRgba.submat(facesArray[index]);
        }
        else {
        	this.currentRect = null;
        }

        return mRgba;
	}

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    @Override
	public void onDestroy() {
        super.onDestroy();
		new ClearCache().execute();
        mOpenCvCameraView.disableView();
    }
    
	private class CaptureFace extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(TrainingActivity.this, "", "Capturing ...", true, false);
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			
			Bitmap bmp = Bitmap.createBitmap(lastValidRect.cols(),lastValidRect.rows(),Config.RGB_565);
			Utils.matToBitmap(lastValidRect, bmp);
			
			// align face
			Mat gray = new Mat();
			Imgproc.cvtColor(lastValidRect, gray, Imgproc.COLOR_BGR2GRAY);
			
			// eyespair
			MatOfRect eyepairs = new MatOfRect();
			eyesDetector.detectMultiScale(gray, eyepairs, 1.1, 5, 2, new Size(), gray.size());
			Rect[] eyesArray = eyepairs.toArray();
	        int maxSize = 0;
	        int index = -1;
	        for (int i = 0; i < eyesArray.length; i++) {
	        	if (index == -1) {
	        		maxSize = eyesArray[i].width*eyesArray[i].height;
	        		index = i;
	        	}
	        	else {
	        		int size = eyesArray[i].width*eyesArray[i].height;
	        		if (size > maxSize) {
	        			maxSize = size;
		        		index = i;
	        		}
	        	}
	        }
	        Rect detectedEyePair = null;
	        if (index != -1) {
	        	detectedEyePair = eyesArray[index];
	        }

			// mouth
			MatOfRect mouths = new MatOfRect();
			eyesDetector.detectMultiScale(gray, mouths, 1.1, 5, 2, new Size(), gray.size());
			Rect[] mouthArray = mouths.toArray();
			maxSize = 0;
	        index = -1;
	        for (int i = 0; i < mouthArray.length; i++) {
	        	if (index == -1) {
	        		maxSize = mouthArray[i].width*mouthArray[i].height;
	        		index = i;
	        	}
	        	else {
	        		int size = mouthArray[i].width*mouthArray[i].height;
	        		if (size > maxSize) {
	        			maxSize = size;
		        		index = i;
	        		}
	        	}
	        } 
	        Rect detectedMouth = null;
	        if (index != -1) {
	        	detectedMouth = mouthArray[index]; 
	        }
			
	        if (detectedEyePair == null && detectedMouth == null) {
	        	Log.e("ERROR", "very bad training example");
	        }
	        else {
	        	if (detectedEyePair == null) {
	        		Log.e("ERROR", "bad training example: no eyes");
		        }
		        else if (detectedMouth == null) {
		        	Log.e("ERROR", "bad training example: no mouth");
		        }
		        else {
					int crop_x = detectedEyePair.x;
					int crop_y = detectedEyePair.y;
					int width = detectedEyePair.width;
					int height = detectedMouth.y + detectedMouth.height - detectedEyePair.y;
					width = Math.min(bmp.getWidth()-crop_x, Math.max(width, height));
					height = Math.min(bmp.getHeight()-crop_y, Math.max(width, height));
					
					bmp = Bitmap.createBitmap(bmp, crop_x, crop_y, width, height, null, false);
		        }
	        }
	        Bitmap.createScaledBitmap(bmp, 250, 250, false);
			
			// save as image file
			String fileStoragePath = Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel";
		    File tempFileStorageDir = new File(fileStoragePath + "/Temp");
		    
		    // Create the storage directory if it does not exist
		    if (! tempFileStorageDir.exists()){
		        if (! tempFileStorageDir.mkdirs()){
		            return null;
		        }
		    }
			
			FileOutputStream os;
			try {
				os = new FileOutputStream(tempFileStorageDir.getAbsolutePath()+"/"+photoSize+".png",true);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			return Bitmap.createScaledBitmap(bmp, 90, 90, false);
		}
		
		@Override
		protected void onPostExecute(Bitmap bmp) {

			trainingPhotos[photoSize].setImageBitmap(bmp);		
			photoSize++;
			
			mOpenCvCameraView.enableView();
			
			progressDialog.dismiss();
	    }

	}
	
	private class ClearCache extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
		    
			String fileStoragePath = Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel";
			File tempFileStorageDir = new File(fileStoragePath + "/Temp");
		    
		    if (tempFileStorageDir.exists()){
		    	if (tempFileStorageDir.isDirectory()) {
		            String[] children = tempFileStorageDir.list();
		            for (int i = 0; i < children.length; i++) {
		                new File(tempFileStorageDir, children[i]).delete();
		            }
		        }
		    }
		    
			return null;
		}
		
	}
	
	private class Train extends AsyncTask<Long, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(TrainingActivity.this, "", "Training ...", true, false);
		}
		
		@Override
		protected Void doInBackground(Long... params) {
		    
			int label =  Integer.parseInt(String.valueOf(params[0]));
			
			Recognizer.getInstance().train_examples(label);
			Recognizer.getInstance().save(params[0]);
		    
			// change state
			int groupPosition = getIntent().getExtras().getInt("groupPosition");
			int memberPosition = getIntent().getExtras().getInt("memberPosition");
			ContactsData.getContacts().get(groupPosition).getGroupMembers().get(memberPosition).finishTraining();
			DatabaseHelper.getInstance(TrainingActivity.this).finishTraining(params[0]);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param) {
			
			Intent intent = new Intent(TrainingActivity.this,MemberActivity.class);
			intent.putExtra("groupPosition", getIntent().getExtras().getInt("groupPosition"));
			intent.putExtra("memberPosition", getIntent().getExtras().getInt("memberPosition"));
			startActivity(intent);
			finish();
			
			progressDialog.dismiss();
		}
	}
}
