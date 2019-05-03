package tensorflow.fatih.com.tensorflow;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import tensorflow.fatih.com.tensorflow.env.YaziSinir;
import tensorflow.fatih.com.tensorflow.env.ResimAraclari;
import tensorflow.fatih.com.tensorflow.tracking.MultiBoxTracker;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {

  private static final int Giris_Size = 300;
  private static final String Model_Dosya = "file:///android_asset/traffic5.pb";
  private static final String Etiket_Dosya = "file:///android_asset/coco_labels_list.txt";

  // Minimum detection confidence to track a detection.
  private static final float Hassasiyet = 0.6f;

  private static final Size OnizlemeBoyutu = new Size(640, 480);

  private static final float Yazi_Boyut = 10;

  private Integer Sensor_Yonu;

  private Classifier Detoktor;

  private Bitmap rgbCerceve = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean BilgiAlgilama = false;

  private long ZamanDalga = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker Takip;

  private byte[] luminanceCopy;

  private YaziSinir Yazi;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, Yazi_Boyut, getResources().getDisplayMetrics());
    Yazi = new YaziSinir(textSizePx);
    Yazi.YaziTipi(Typeface.MONOSPACE);

    Takip = new MultiBoxTracker(this);

    int cropSize = Giris_Size;

    try {
      Detoktor = TensorFlowObjectDetectionAPIModel.create(
          getAssets(), Model_Dosya, Etiket_Dosya, Giris_Size);
      cropSize = Giris_Size;
    } catch (final IOException e) {
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    Sensor_Yonu = rotation - getScreenOrientation();
    rgbCerceve = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
            ResimAraclari.MatrixTransformasyonu(
            previewWidth, previewHeight,
            cropSize, cropSize,
            Sensor_Yonu, false);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new OverlayView.DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            Takip.draw(canvas);
          }
        });
  }

  OverlayView trackingOverlay;

  @Override
  protected void processImage() {
    ++ZamanDalga;
    final long currTimestamp = ZamanDalga;
    byte[] originalLuminance = getLuminance();
    Takip.onFrame(
        previewWidth,
        previewHeight,
        Sensor_Yonu);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (BilgiAlgilama) {
      readyForNextImage();
      return;
    }
    BilgiAlgilama = true;
    rgbCerceve.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbCerceve, frameToCropTransform, null);

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            final List<Classifier.Recognition> results = Detoktor.recognizeImage(croppedBitmap);
            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

              final List<Classifier.Recognition> mappedRecognitions =
                    new LinkedList<>();

            for (final Classifier.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= Hassasiyet) {
                canvas.drawRect(location, paint);
                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                mappedRecognitions.add(result);
              }
            }

            Takip.trackResults(mappedRecognitions);
            toSpeech(mappedRecognitions);
            trackingOverlay.postInvalidate();

            BilgiAlgilama = false;
          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return OnizlemeBoyutu;
  }

}
