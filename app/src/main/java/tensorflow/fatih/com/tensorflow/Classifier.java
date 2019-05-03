package tensorflow.fatih.com.tensorflow;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;
import java.util.Objects;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {
  /**
   * An immutable result returned by a Classifier describing what was recognized.
   */
  public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final String baslik;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float hassasiyet;

    /** Optional konum within the source image for the konum of the recognized object. */
    private RectF konum;

    public Recognition(
            final String id, final String baslik, final Float hassasiyet, final RectF konum) {
      this.id = id;
      this.baslik = baslik;
      this.hassasiyet = hassasiyet;
      this.konum = konum;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return baslik;
    }

    public Float getConfidence() {
      return hassasiyet;
    }

    public RectF getLocation() {
      return new RectF(konum);
    }

    public void setLocation(RectF konum) {
      this.konum = konum;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (baslik != null) {
        resultString += baslik + " ";
      }

      if (hassasiyet != null) {
        resultString += String.format("(%.1f%%) ", hassasiyet * 100.0f);
      }

      if (konum != null) {
        resultString += konum + " ";
      }

      return resultString.trim();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Recognition that = (Recognition) o;
      return Objects.equals(baslik, that.baslik);
    }

    @Override
    public int hashCode() {
      return Objects.hash(baslik);
    }
  }

  List<Recognition> recognizeImage(Bitmap bitmap);


  void close();
}
