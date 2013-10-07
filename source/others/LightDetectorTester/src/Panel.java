//package src;

import java.awt.peer.LightweightPeer;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.example.lightdetector.*;

//import org.opencv.samples.tutorial1.LightDetector;
//import org.opencv.samples.tutorial1.R;
import org.opencv.highgui.Highgui;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.HashMap;

public class Panel {
	static String res_folder = "./res/video/seq/";
	static private Mat image;

	static int tilesX = 4;
	static int tilesY = 4;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		ArrayList<String> screenColors = new ArrayList<String>();
		screenColors.add(ColorManager.KEY_BLUE);
		screenColors.add(ColorManager.KEY_GREEN);
		screenColors.add(ColorManager.KEY_RED);
		//screenColors.add(ColorManager.KEY_WHITE);
		
		for (int i = 1; i < 4800; i++) {
			image = Highgui.imread(res_folder + "frame" + i + ".jpg");

			image = drawTilesGrid(image, tilesY, tilesY);

			PointCollector collector = new PointCollector(tilesX, tilesY,
					new PointCollectorListener() {

						@Override
						public void onPointCollectorUpdate(
								HashMap<String, ArrayList<Point>> update) {

							for (String colorItem : update.keySet()) {
								for (Point tile : update.get(colorItem)) {
									System.out.println("Blob: " + colorItem + " " + tile.x + " " + tile.y);
									image = drawTile(image, (int) tile.x,
											(int) tile.y,
											ColorManager.getCvColor(colorItem));
								}
							}

						}
					});
			collector.collectOffline(image, screenColors);
			Highgui.imwrite(res_folder + "out/frame" + i + ".jpg",
					image);

		}

		/*
		 * LightDetector detector = new LightDetector(); for (int i = 17; i <
		 * 18; ++i) {
		 * 
		 * String input = res_folder + i + ".jpg"; String output = res_folder +
		 * "out/" + i + ".jpg";
		 * 
		 * Mat imageIn = Highgui.imread(input);
		 * 
		 * System.out.println("Processing: " + input);
		 * 
		 * // HSV color // Scalar colorHSV = new Scalar(171, 255, 255);
		 * 
		 * // RGB color Scalar color = new Scalar(0, 0, 255); // Scalar colorRGB
		 * = new Scalar(0, 0, 255); // Scalar colorHSV; // colorHSV =
		 * detector.scalarRgba2Hsv(colorRGB);
		 * 
		 * // System.out.println("" + colorHSV.val[0] + " " + colorHSV.val[1] +
		 * " " + colorHSV.val[2]);
		 * 
		 * // debug// // // Mat img = new Mat(new Size(300, 300),
		 * CvType.CV_8UC3, colorHSV); // Highgui.imwrite(outDebug, img);
		 * 
		 * ArrayList<Point> mobiles = detector.getBlobCoords(imageIn, color);
		 * for (Point center: mobiles) { System.out.println("found blob");
		 * Core.circle(imageIn, center, 5, new Scalar(255, 0, 0)); }
		 * 
		 * //Mat imageProcessed = detector.detect(imageIn, new Scalar(255, 255,
		 * 255, 255)); //Mat imageOut = mergeImages(imageIn, imageProcessed);
		 * 
		 * // Core.rectangle(imageIn, new Point(0, 0), new Point(10, 10),
		 * color); if(Highgui.imwrite(output, imageIn))
		 * System.out.println("Upis");
		 * 
		 * }
		 */
	}

	private static Mat drawTile(Mat input, int x, int y, Scalar color) {
		Mat output = new Mat(input.height(), input.width(), input.type(),
				new Scalar(0, 0, 0));
		input.copyTo(output);

		int unitX = output.width() / tilesX;
		int unitY = output.height() / tilesY;
		Core.rectangle(output, new Point(unitX * x, unitY * y), new Point(unitX
				* (x + 1), unitY * (y + 1)), color, 5);

		// Core.addWeighted(input, 1.0, output, 0.5, 0, output);
		return output;
	}

	public static Mat mergeImages(Mat img1, Mat img2) {
		Mat imgResult = new Mat(img1.rows(), 2 * img1.cols(), img1.type()); // Your
																			// final
																			// image
		// if (img2.channels() == 1)
		// img2 = LightDetector.colorize(img2);

		// img2.convertTo(img2, img1.type());
		Mat roiImgResult_Left = imgResult.submat(new Rect(0, 0, img1.cols(),
				img1.rows())); // Img1 will be on the left part
		Mat roiImgResult_Right = imgResult.submat(new Rect(img1.cols(), 0, img2
				.cols(), img2.rows())); // Img2 will be on the right part, we
										// shift the roi of img1.cols on the
										// right

		Mat roiImg1 = img1.submat(new Rect(0, 0, img1.cols(), img1.rows()));
		Mat roiImg2 = img2.submat(new Rect(0, 0, img2.cols(), img2.rows()));

		roiImg1.copyTo(roiImgResult_Left); // Img1 will be on the left of
											// imgResult
		roiImg2.copyTo(roiImgResult_Right); // Img2 will be on the right of
											// imgResult

		return imgResult;
	}

	public static Mat drawTilesGrid(Mat input, int tilesX, int tilesY) {
		Mat output = new Mat();
		input.copyTo(output);

		int unit = output.width() / tilesX;
		for (int i = 0; i < tilesX; ++i)
			Core.line(output, new Point(i * unit, 0), new Point(i * unit,
					output.height()), new Scalar(255, 0, 0, 255));

		unit = output.height() / tilesY;
		for (int i = 0; i < tilesY; ++i)
			Core.line(output, new Point(0, i * unit), new Point(output.width(),
					i * unit), new Scalar(255, 0, 0, 255));

		return output;
	}

}
