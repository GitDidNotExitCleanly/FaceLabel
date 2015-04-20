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

import com.facelabel.database.ContactsData;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class Recognizer {
	
	private static Recognizer instance;
	
	private static FaceRecognizer fr;
	
	private static String fileStoragePath = Environment.getExternalStorageDirectory()+"/Android/data/com.facelabel";
	
	private Recognizer() {
		fr = opencv_contrib.createLBPHFaceRecognizer(1,8,8,8,100);
        load();
	}
	
	public static Recognizer getInstance() {
		if (instance == null) {
			instance = new Recognizer();
		}
        return instance;
	}
	
	private void load() {
		
		File xml = new File(fileStoragePath+"/Model/FaceModel.xml");
		if (xml.exists()) {
			fr.load(fileStoragePath+"/Model/FaceModel.xml");
		}
	}
	
	public void train() {
		
		int totalMembers = 0;
		for (int i=0;i<ContactsData.getContacts().size();i++) {
			totalMembers += ContactsData.getContacts().get(i).getGroupMembers().size();
		}
		
		if (totalMembers > 0) {
			MatVector trainingImages = new MatVector(totalMembers);
			int[] labels = new int[totalMembers];	
			
			int count = 0;
			for (int i=0;i<ContactsData.getContacts().size();i++) {
				for (int j=0;j<ContactsData.getContacts().get(i).getGroupMembers().size();j++) {
					long memberId = ContactsData.getContacts().get(i).getGroupMembers().get(j).getId();
					Mat mRgb = opencv_highgui.imread(ContactsData.getContacts().get(i).getGroupMembers().get(j).getPhoto());
					Mat mGray = new Mat();
					opencv_imgproc.cvtColor(mRgb, mGray, opencv_imgproc.COLOR_BGR2GRAY);
					trainingImages.put(count, mGray);
					labels[count] = (int) memberId;
					count++;
				}
			}
			Mat labelsM = new Mat(labels);
	 
			fr.train(trainingImages, labelsM);
		}
	}
	
	public void save() {
		
		File xmlDir = new File(fileStoragePath+"/Model");
		if (! xmlDir.exists()){
	        if (! xmlDir.mkdirs()){
	            return;
	        }
	    }

		FileStorage fs = new FileStorage();
		fs.open(fileStoragePath+"/Model/FaceModel.xml", FileStorage.WRITE);
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
			
			fr.predict(mGray,ids,prob);
			
			System.out.println("probability: "+prob[0]);
			
			return ids[0];
		}
		else {
			return -1;
		}
	}
}
