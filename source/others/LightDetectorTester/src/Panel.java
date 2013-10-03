import java.awt.peer.LightweightPeer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.example.digitallighterserver.lightdetector.*;
import com.example.lightdetector.LightDetector;


//import org.opencv.samples.tutorial1.LightDetector;
//import org.opencv.samples.tutorial1.R;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;  

import javax.swing.*; 

public class Panel{
	static String res_folder = "/home/tom/workspace/LightDetectorTester/res/drawable/";
	
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        LightDetector detector = new LightDetector();
        
        for (int i = 0; i < 1; ++i) {
        	String input = res_folder + i + ".jpg";
        	String output = res_folder + "out/" + i + ".jpg";
        	
        	Mat imageIn = Highgui.imread(input);

        	System.out.println("Processing: " + input);
        	
        	Scalar color = new Scalar(171, 255, 255);
        	
        	ArrayList<Point> mobiles =  detector.getBlobCoords(imageIn, color);
        	for (Point center: mobiles) {
        		System.out.println("pes");
        		Core.circle(imageIn, center, 5, new Scalar(255, 0, 0));
        	}
        	
        	//Mat imageProcessed = detector.detect(imageIn, new Scalar(255, 255, 255, 255));
        	//Mat imageOut = mergeImages(imageIn, imageProcessed);
        	
        	Core.rectangle(imageIn, new Point(0, 0), new Point(10, 10), color);
        	Highgui.imwrite(output, imageIn);
        }
    }
    
    public static Mat mergeImages(Mat img1, Mat img2)
    {
    	Mat imgResult = new Mat(img1.rows(),2*img1.cols(),img1.type()); // Your final image
    	//if (img2.channels() == 1)
    	//	img2 = LightDetector.colorize(img2);	
    	
    	//img2.convertTo(img2, img1.type());
    	Mat roiImgResult_Left = imgResult.submat(new Rect(0,0,img1.cols(),img1.rows())); //Img1 will be on the left part
    	Mat roiImgResult_Right = imgResult.submat(new Rect(img1.cols(),0,img2.cols(),img2.rows())); //Img2 will be on the right part, we shift the roi of img1.cols on the right

    	Mat roiImg1 = img1.submat(new Rect(0,0,img1.cols(),img1.rows()));
    	Mat roiImg2 = img2.submat(new Rect(0,0,img2.cols(),img2.rows()));

    	roiImg1.copyTo(roiImgResult_Left); //Img1 will be on the left of imgResult
    	roiImg2.copyTo(roiImgResult_Right); //Img2 will be on the right of imgResult
    	
    	return imgResult;
    }
}