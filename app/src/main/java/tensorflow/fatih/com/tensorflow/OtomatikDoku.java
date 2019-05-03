package tensorflow.fatih.com.tensorflow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class OtomatikDoku extends TextureView {
  private int GenislikOrani = 0;
  private int YukseklikOrani = 0;

  public OtomatikDoku(final Context context) {
    this(context, null);
  }

  public OtomatikDoku(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public OtomatikDoku(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  public void EnBoyOrani(final int genislik, final int yukseklik) {
    if (genislik < 0 || yukseklik < 0) {
      throw new IllegalArgumentException("Boyut negatif olamaz");
    }
    GenislikOrani = genislik;
    YukseklikOrani = yukseklik;
    requestLayout();
  }

  @Override
  protected void onMeasure(final int genislikOlcme, final int yukseklikOlcme) {
    super.onMeasure(genislikOlcme, yukseklikOlcme);
    final int genislik = MeasureSpec.getSize(genislikOlcme);
    final int yukseklik = MeasureSpec.getSize(yukseklikOlcme);
    if (0 == GenislikOrani || 0 == YukseklikOrani) {
      setMeasuredDimension(genislik, yukseklik);
    } else {
      if (genislik < yukseklik * GenislikOrani / YukseklikOrani) {
        setMeasuredDimension(genislik, genislik * YukseklikOrani / GenislikOrani);
      } else {
        setMeasuredDimension(yukseklik * GenislikOrani / YukseklikOrani, yukseklik);
      }
    }
  }
}
