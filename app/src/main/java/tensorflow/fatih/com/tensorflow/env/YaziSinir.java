package tensorflow.fatih.com.tensorflow.env;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.Vector;

/**
 * A class that encapsulates the tedious bits of rendering legible, bordered Yazi onto a canvas.
 */
public class YaziSinir {
  private final Paint IcBoya;
  private final Paint DisBoya;

  private final float YaziBoyut;


  public YaziSinir(final float YaziBoyut) {
    this(Color.WHITE, Color.BLACK, YaziBoyut);
  }

  public YaziSinir(final int IcRenk, final int DisRenk, final float YaziBoyut) {
    IcBoya = new Paint();
    IcBoya.setTextSize(YaziBoyut);
    IcBoya.setColor(IcRenk);
    IcBoya.setStyle(Style.FILL);
    IcBoya.setAntiAlias(false);
    IcBoya.setAlpha(255);

    DisBoya = new Paint();
    DisBoya.setTextSize(YaziBoyut);
    DisBoya.setColor(DisRenk);
    DisBoya.setStyle(Style.FILL_AND_STROKE);
    DisBoya.setStrokeWidth(YaziBoyut / 8);
    DisBoya.setAntiAlias(false);
    DisBoya.setAlpha(255);

    this.YaziBoyut = YaziBoyut;
  }

  public void YaziTipi(Typeface typeface) {
    IcBoya.setTypeface(typeface);
    DisBoya.setTypeface(typeface);
  }

  public void MetinYaz(final Canvas canvas, final float pozX, final float pozY, final String Yazi) {
    canvas.drawText(Yazi, pozX, pozY, DisBoya);
    canvas.drawText(Yazi, pozX, pozY, IcBoya);
  }

}
