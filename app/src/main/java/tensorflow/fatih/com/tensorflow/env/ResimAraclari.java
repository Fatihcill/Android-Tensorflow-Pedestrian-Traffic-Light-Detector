package tensorflow.fatih.com.tensorflow.env;

import android.graphics.Matrix;

public class ResimAraclari {

  static final int MaksimumKanal = 262143;

  public static void convertYUV420SPToARGB8888(
      byte[] giris,
      int genislik,
      int yukseklik,
      int[] cikis) {

    // Java implementation of YUV420SP to ARGB8888 converting
    final int cerceveBoyut = genislik * yukseklik;
    for (int j = 0, yp = 0; j < yukseklik; j++) {
      int uvp = cerceveBoyut + (j >> 1) * genislik;
      int u = 0;
      int v = 0;

      for (int i = 0; i < genislik; i++, yp++) {
        int y = 0xff & giris[yp];
        if ((i & 1) == 0) {
          v = 0xff & giris[uvp++];
          u = 0xff & giris[uvp++];
        }

        cikis[yp] = YUV2RGB(y, u, v);
      }
    }
  }

  private static int YUV2RGB(int y, int u, int v) {
    // Adjust and check YUV values
    y = (y - 16) < 0 ? 0 : (y - 16);
    u -= 128;
    v -= 128;

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);
    int y1192 = 1192 * y;
    int r = (y1192 + 1634 * v);
    int g = (y1192 - 833 * v - 400 * u);
    int b = (y1192 + 2066 * u);

    // Clipping RGB values to be inside boundaries [ 0 , MaksimumKanal ]
    r = r > MaksimumKanal ? MaksimumKanal : (r < 0 ? 0 : r);
    g = g > MaksimumKanal ? MaksimumKanal : (g < 0 ? 0 : g);
    b = b > MaksimumKanal ? MaksimumKanal : (b < 0 ? 0 : b);

    return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
  }


  public static void YUV420DonARGB8888(
      byte[] yData,
      byte[] uData,
      byte[] vData,
      int genislik,
      int yukseklik,
      int yDizi,
      int uvDizi,
      int uvPikselDizisi,
      int[] out) {
    int yp = 0;
    for (int j = 0; j < yukseklik; j++) {
      int pY = yDizi * j;
      int pUV = uvDizi * (j >> 1);

      for (int i = 0; i < genislik; i++) {
        int uv_offset = pUV + (i >> 1) * uvPikselDizisi;

        out[yp++] = YUV2RGB(
            0xff & yData[pY + i],
            0xff & uData[uv_offset],
            0xff & vData[uv_offset]);
      }
    }
  }

  /**
   * Returns a transformation matrix from one reference frame into another.
   * Handles cropping (if maintaining aspect ratio is desired) and rotation.
   *
   * @param srcGenisligi Width of source frame.
   * @param srcYuksekligi Height of source frame.
   * @param dstGenisligi Width of destination frame.
   * @param dstYuksekligi Height of destination frame.
   * @param Rotasyon Amount of rotation to apply from one frame to another.
   *  Must be a multiple of 90.
   * @param EnboyOran If true, will ensure that scaling in x and y remains constant,
   * cropping the image if necessary.
   * @return The transformation fulfilling the desired requirements.
   */
  public static Matrix MatrixTransformasyonu(
      final int srcGenisligi,
      final int srcYuksekligi,
      final int dstGenisligi,
      final int dstYuksekligi,
      final int Rotasyon,
      final boolean EnboyOran) {
    final Matrix matrix = new Matrix();

    if (Rotasyon != 0) {
      if (Rotasyon % 90 != 0) { }

      // Translate so center of image is at origin.
      matrix.postTranslate(-srcGenisligi / 2.0f, -srcYuksekligi / 2.0f);

      // Rotate around origin.
      matrix.postRotate(Rotasyon);
    }

    final boolean transpose = (Math.abs(Rotasyon) + 90) % 180 == 0;

    final int Genislikici = transpose ? srcYuksekligi : srcGenisligi;
    final int Yukseklikici = transpose ? srcGenisligi : srcYuksekligi;

    // Apply scaling if necessary.
    if (Genislikici != dstGenisligi || Yukseklikici != dstYuksekligi) {
      final float OlcekFaktoruX = dstGenisligi / (float) Genislikici;
      final float OlcekFaktoruY = dstYuksekligi / (float) Yukseklikici;

      if (EnboyOran) {
        // Scale by minimum factor so that dst is filled completely while
        // maintaining the aspect ratio. Some image may fall off the edge.
        final float OlcekFaktoru = Math.max(OlcekFaktoruX, OlcekFaktoruY);
        matrix.postScale(OlcekFaktoru, OlcekFaktoru);
      } else {
        // Scale exactly to fill dst from src.
        matrix.postScale(OlcekFaktoruX, OlcekFaktoruY);
      }
    }

    if (Rotasyon != 0) {
      // Translate back from origin centered reference to destination frame.
      matrix.postTranslate(dstGenisligi / 2.0f, dstYuksekligi / 2.0f);
    }

    return matrix;
  }
}
