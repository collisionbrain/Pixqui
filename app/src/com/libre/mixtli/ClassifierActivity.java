package com.libre.mixtli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.libre.mixtli.core.PixquiCore;
import com.libre.mixtli.env.BorderedText;
import com.libre.mixtli.env.ImageUtils;
import com.libre.mixtli.env.Logger;
import com.libre.mixtli.prefs.Pref;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    protected static final boolean SAVE_PREVIEW_BITMAP = false;

    private ResultsView resultsView;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;
    private   CameraActivity cameraActivity;
    private long lastProcessingTimeMs;

    // These are the settings for the original v1 Inception model. If you want to
    // use a model that's been produced from the TensorFlow for Poets codelab,
    // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
    // INPUT_NAME = "Mul", and OUTPUT_NAME = "final_result".
    // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
    // the ones you produced.
    //
    // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
    // model first:
    //
    // python strip_unused.py \
    // --input_graph=<retrained-pb-file> \
    // --output_graph=<your-stripped-pb-file> \
    // --input_node_names="Mul" \
    // --output_node_names="final_result" \
    // --input_binary=true
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";
    private float minimumConfidence=0.9f;

    private static final String MODEL_FILE = "file:///android_asset/new_retrained_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels.txt";


    private static final boolean MAINTAIN_ASPECT = true;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


    private Integer sensorOrientation;
    private Classifier classifier;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private BorderedText borderedText;
    private Pref prefs;
    private Context context;
    private CameraActivity activity ;

    private FirebaseStorage firebaseStorage ;
    private StorageReference storageRef ;
    private StorageReference uploadeRef;
    private String user_key;
    private SimpleDateFormat simpleDateFormat;
    private Calendar calendar ;
    private Date now ;
    private PixquiCore pixquiCore;
    private Mat screenMat;
    private  MatOfRect faces;
    private String faceFile;
    private File externalDir ;
    private String cascadeFilePath;
    private File mCascadeFile;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(null);
        context=this;
        prefs=new Pref(context);
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();
        user_key=prefs.loadData("REGISTER_USER_KEY");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        calendar = Calendar.getInstance();

        faceFile = "/pdata/xml/haarcascade_frontalcatface.xml";
        externalDir = Environment.getExternalStorageDirectory();
        cascadeFilePath=externalDir.getAbsolutePath().toString().concat(faceFile);
        mCascadeFile = new File(cascadeFilePath);

        pixquiCore = new PixquiCore(mCascadeFile.getAbsolutePath(), 0);
        screenMat=new Mat();
        faces=new MatOfRect();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    private static final float TEXT_SIZE_DIP = 10;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE,
                sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        prefs=new Pref(this);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        Utils.bitmapToMat(croppedBitmap,screenMat);
                        Mat grayImage=new Mat();
                        Imgproc.cvtColor(screenMat, grayImage, Imgproc.COLOR_BGR2GRAY);
                        Mat dst= com.libre.mixtli.prefs.Utils.rotate( grayImage, 180);
                        Bitmap fcroppedBitmap = Bitmap.createBitmap(croppedBitmap);
                        Utils.matToBitmap(dst,fcroppedBitmap);

                        Log.d("xxxxxxxxxxx", "BITMAP  "+rgbFrameBitmap.getHeight()+"x"+rgbFrameBitmap.getWidth());
                        Log.d("xxxxxxxxxxx", "MAT  >"+screenMat.cols()+"x"+screenMat.rows());

                        pixquiCore.detect(dst, faces);
                        List<Rect> list=faces.toList();
                        Log.d("xxxxxxxxxxx", "FACES  >> "+list.size());
                        screenMat.release();


                        if(list.size()>0) {
                            final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
                            for (Classifier.Recognition res : results
                                    ) {
                                if (res.getConfidence() > minimumConfidence) {
                                    user_key = prefs.loadData("REGISTER_USER_KEY");
                                    cameraActivity.setGunAllert();
                                }
                            }
                        }
                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        requestRender();
                        readyForNextImage();
                    }
                });
    }

    @Override
    public void onSetDebug(boolean debug) {
        classifier.enableStatLogging(debug);
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (classifier != null) {
                String statString = classifier.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }

}
