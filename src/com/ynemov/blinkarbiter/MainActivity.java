package com.ynemov.blinkarbiter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Currency;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "com_ynemov_blinkarbiter";
	private static final String mResFaceCascade = "haarcascade_frontalface_alt.xml";
	private static final String mResEyesCascade = "haarcascade_eye_tree_eyeglasses.xml";

	private String[] mRawRes = {mResFaceCascade, mResEyesCascade};

	private CameraBridgeViewBase mOpenCvCameraView;
	private CascadeClassifier mFaceCascade;// = new CascadeClassifier();
	private CascadeClassifier mEyesCascade;
	private Mat mGrayscaleImage;
	private int mAbsoluteFaceSize;
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    }
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
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
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.disableView();

		(new AsyncTask<String, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(String... params) {

				try {
					// TODO remove if success
					//					InputStream ims1 = getAssets().open(params[0]);
					{
						// Copy the resource into a temp file so OpenCV can load it
						InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						File mCascadeFile = new File(cascadeDir, params[0]);
						FileOutputStream os = new FileOutputStream(mCascadeFile);


						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

						Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
						Log.i(TAG, "PATH=" + mCascadeFile.getAbsolutePath());
						// Load the cascade classifier
						mFaceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
						Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
					}

					//					InputStream ims2 = getAssets().open(params[1]);
					{
						// Copy the resource into a temp file so OpenCV can load it
						InputStream is = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						File mCascadeFile = new File(cascadeDir, params[1]);
						FileOutputStream os = new FileOutputStream(mCascadeFile);


						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

						// Load the cascade classifier
						Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
						Log.i(TAG, "PATH=" + mCascadeFile.getAbsolutePath());
						mEyesCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
						Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
					}

					//					if(!mFaceCascade.load(params[0]) || !mEyesCascade.load(params[1])) {
					//						return false;
					//					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean isSuccess) {
				if(isSuccess) {
					Toast toast = Toast.makeText(getApplicationContext(), "TWO SUCCESS LOADS", Toast.LENGTH_SHORT);
					toast.show();
					mOpenCvCameraView.enableView();
				}
				else {
					Toast toast = Toast.makeText(getApplicationContext(), "SMTHNG ARE GOING WRONG", Toast.LENGTH_SHORT);
					toast.show();
				}
				super.onPostExecute(isSuccess);
			}

		}).execute(mRawRes);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGrayscaleImage = new Mat(height, width, CvType.CV_8UC4);

		// The faces will be a 30% of the height of the screen
		mAbsoluteFaceSize = (int) (height * 0.3);
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		Mat inputMat = inputFrame.rgba();
		// Create a grayscale image
		Imgproc.cvtColor(inputMat, mGrayscaleImage, Imgproc.COLOR_RGBA2RGB);

		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		MatOfRect faces = new MatOfRect();

		// Use the classifier to detect faces
		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		if (mFaceCascade != null) {
			Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
			mFaceCascade.detectMultiScale(mGrayscaleImage, faces, 1.1, 2, 2,
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		}


		// If there are any faces found, draw a rectangle around it
		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		Rect[] facesArray = faces.toArray();
		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		for (int i = 0; i <facesArray.length; i++) {
			Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
			Core.rectangle(inputMat, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
		}

		Log.d(TAG, "Faces detected: " + facesArray.length + " (" + System.currentTimeMillis() + ")"); 
		Log.i(TAG, "L=" + Thread.currentThread().getStackTrace()[2].getLineNumber());
		return inputMat;
		//		return inputFrame.rgba();
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.main, menu);
	//		return true;
	//	}
	//
	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		// Handle action bar item clicks here. The action bar will
	//		// automatically handle clicks on the Home/Up button, so long
	//		// as you specify a parent activity in AndroidManifest.xml.
	//		int id = item.getItemId();
	//		if (id == R.id.action_settings) {
	//			return true;
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}
}