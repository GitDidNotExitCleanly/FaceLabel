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
import org.opencv.objdetect.CascadeClassifier;

import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
import com.facelabel.R.id;
import com.facelabel.R.layout;
import com.facelabel.contacts.group.GroupActivity;
import com.facelabel.contacts.member.MemberActivity;
import com.facelabel.database.ContactsData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;

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
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
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
                        } else

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
						Toast toast = Toast.makeText(TrainingActivity.this, "Please wiat until getting a stable image", 200);
						toast.show();
					}
				}
			}
			
		});
		
		this.train.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
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
}
