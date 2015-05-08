package com.facelabel.processing.faceRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import com.facelabel.MainPanelActivity;
import com.facelabel.MyExceptionHandler;
import com.facelabel.R;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FaceRecognizerActivity extends Activity implements CvCameraViewListener2 {
	
	private ProgressDialog progressDialog;
	
	private RelativeLayout rectangle;
	private ImageView result;
	
	private TextView btnCancel;
	private TextView btnDetect;
	
	private int windowWidth;
	private int windowHeight;
	
	private int mMaxDetectWidth;
	private int mMaxDetectHeight;
	
	private Mat lastValidRect;
	private Mat currentRect;
	
    private static final String    TAG                 = "FaceRecognizerActivity";
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
                    Log.i(TAG, "OpenCV loaded successfully");

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
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
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

    public FaceRecognizerActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_face_recognizer);
        
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        
    	Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		this.windowWidth = size.x;
		this.windowHeight = size.y;
		this.mMaxDetectWidth = size.x/2;
		this.mMaxDetectHeight = size.y/2;
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        setGUIAndListener();
        
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }
    
    private void setGUIAndListener() {
    	
    	this.rectangle = (RelativeLayout)findViewById(R.id.activity_face_recognizer_rectangle);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mMaxDetectWidth,mMaxDetectWidth);
    	params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
    	this.rectangle.setLayoutParams(params);
    	this.result = (ImageView)findViewById(R.id.activity_face_recognizer_result);
    	
    	this.btnCancel = (TextView)findViewById(R.id.activity_face_recognizer_cancel);
    	this.btnDetect = (TextView)findViewById(R.id.activity_face_recognizer_detect);
    	
    	this.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(FaceRecognizerActivity.this,MainPanelActivity.class);
				intent.putExtra("fragment", 1);
				startActivity(intent);
				finish();
			}
    		
    	});
    	
    	this.btnDetect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (currentRect != null) {
					if (mOpenCvCameraView != null) {
			            mOpenCvCameraView.disableView();
					}
					
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			    	params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
			    	rectangle.setLayoutParams(params);
			    	rectangle.setBackgroundColor(Color.BLACK);
			    	
			    	Bitmap bmp = Bitmap.createBitmap(lastValidRect.cols(),lastValidRect.rows(),Config.RGB_565);
			    	Utils.matToBitmap(lastValidRect, bmp);
			    	result.setImageBitmap(bmp);
					
			    	new RecognizeFace().execute(bmp);
				}
				else {
					Toast toast = Toast.makeText(FaceRecognizerActivity.this, "Please wiat until getting a stable image.", 200);
					toast.show();
				}
			}
    		
    	});
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
	public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
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
        else {
            Log.e(TAG, "Detection method is not selected!");
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
    
	private class RecognizeFace extends AsyncTask<Bitmap, Void, Integer> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(FaceRecognizerActivity.this, "", "Analyzing ...", true, false);
		}
		
		@Override
		protected Integer doInBackground(Bitmap... src) {

			Recognizer faceRecognizer = Recognizer.getInstance();
			faceRecognizer.train();
			faceRecognizer.save();
			
			return faceRecognizer.predict(src[0]);
		}
		
		@Override
		protected void onPostExecute(Integer memberID) {
			
			Log.e("HELLO_WORLD", String.valueOf(memberID));
			if (memberID != -1) {
				for (int i=0;i<ContactsData.getContacts().size();i++) {
					for (int j=0;i<ContactsData.getContacts().get(i).getGroupMembers().size();j++) {
						if (ContactsData.getContacts().get(i).getGroupMembers().get(j).getId() == memberID) {
							
							Intent intent = new Intent(FaceRecognizerActivity.this, MemberActivity.class);
							intent.putExtra("groupPosition", i);
							intent.putExtra("memberPosition", j);
							startActivity(intent);
							finish();
							
						}
					}
				}
			}
			else {
				new AlertDialog.Builder(FaceRecognizerActivity.this)
							.setTitle("Exception")
							.setMessage("No Match Found !")
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						        @Override
								public void onClick(DialogInterface dialog, int which) { 
						        	
						        	Intent intent = new Intent(FaceRecognizerActivity.this,MainPanelActivity.class);
									intent.putExtra("fragment", 1);
									startActivity(intent);
									finish();
									
						        }
						     })
						    .setIcon(android.R.drawable.ic_dialog_alert)
						    .show();
			}
			
			progressDialog.dismiss();
	    }

	}
}