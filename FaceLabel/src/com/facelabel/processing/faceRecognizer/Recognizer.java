package com.facelabel.processing.faceRecognizer;

import java.io.File;
import java.io.FileOutputStream;

import org.bytedeco.javacpp.opencv_contrib;
import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgproc;

import android.graphics.Bitmap;
import android.os.Environment;

public class Recognizer {
	
	private static Recognizer instance;
	
	private static FaceRecognizer fr;
	
	private static String fileStoragePath = Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel";
	
	private Recognizer() {
		fr = opencv_contrib.createLBPHFaceRecognizer(1,8,8,8,100);
	}
	
	public static Recognizer getInstance() {
		if (instance == null) {
			instance = new Recognizer();
		}
        return instance;
	}
	
	private void load(File xml) {

		if (xml.exists()) {
			fr.load(xml.getAbsolutePath());
		}
	}

	public void train_examples(int label) {
		
		File tempFileStorageDir = new File(fileStoragePath + "/Temp");
	    MatVector trainingImages = new MatVector(5);
	    int[] labels = new int[5];
	    
	    for (int i=0;i<5;i++) {
	    	Mat mRgb = opencv_highgui.imread(tempFileStorageDir.getAbsolutePath()+"/"+i+".png");
	    	Mat mGray = new Mat();
	    	opencv_imgproc.cvtColor(mRgb, mGray, opencv_imgproc.COLOR_BGR2GRAY);
	    	trainingImages.put(i, mGray);
	    	labels[i] = label;
	    }
	    Mat labelsM = new Mat(labels);
	    
	    fr.train(trainingImages, labelsM);
	}

	public void save(long id) {
		
		File xmlDir = new File(fileStoragePath+"/Model");
		if (! xmlDir.exists()){
	        if (! xmlDir.mkdirs()){
	            return;
	        }
	    }

		FileStorage fs = new FileStorage();
		fs.open(fileStoragePath+"/Model/"+id+".xml", FileStorage.WRITE);
		fr.save(fs);
		fs.release();
	}
	
	public int predict(Bitmap bmp) {
		if (bmp != null) {

			FileOutputStream os;
			try {
				os = new FileOutputStream(fileStoragePath+"/temp.png",true);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Mat mRgb = opencv_highgui.imread(fileStoragePath+"/temp.png");
			
			new File(fileStoragePath+"/temp.png").delete();
			
			Mat mGray = new Mat();
			opencv_imgproc.cvtColor(mRgb,mGray,opencv_imgproc.COLOR_BGR2GRAY);
			
			int[] ids = new int[1];
			double[] prob = new double[1];
			
			
			File xmlDir = new File(fileStoragePath+"/Model");
			if (! xmlDir.exists()){
		       return -1;
		    }
			else {
				
				int id = -1;
				double prob_best = -1;
				
				String[] children = xmlDir.list();
		        for (int i = 0; i < children.length; i++) {
		        	load(new File(xmlDir, children[i]));
		        	fr.predict(mGray,ids,prob);
		        	
		        	if (id == -1) {
		        		id = ids[0];
		        		prob_best = prob[0];
		        	}
		        	else {
		        		if (prob[0] > prob_best) {
		        			id = ids[0];
			        		prob_best = prob[0];
		        		}
		        	}
		        }
		        
		        return id;
			}

		}
		else {
			return -1;
		}
	}
}
