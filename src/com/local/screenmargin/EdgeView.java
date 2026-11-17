package com.local.screenmargin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Full-screen test pattern: a line on the very edge of the picture, a ruler with
 * a tick every 5 px (long every 10 px, numbered every 20 px) going in from each
 * edge, corner marks and a grid. If an edge line is hidden under the protector,
 * the margin on that side is too small.
 */
final class EdgeView extends View {
    private final Paint edge = new Paint();
    private final Paint tick = new Paint();
    private final Paint grid = new Paint();
    private final Paint num = new Paint(Paint.ANTI_ALIAS_FLAG);

    EdgeView(Context c) {
        super(c);
        setBackgroundColor(Color.BLACK);
        edge.setColor(Ui.CORAL);
        edge.setStrokeWidth(3);
        tick.setColor(Color.WHITE);
        tick.setStrokeWidth(1);
        grid.setColor(0xFF1B2036);
        grid.setStrokeWidth(1);
        num.setColor(Ui.WARN);
        num.setTextSize(22);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        // grid every 60 px
        for (int x = 60; x < w; x += 60) {
            c.drawLine(x, 0, x, h, grid);
        }
        for (int y = 60; y < h; y += 60) {
            c.drawLine(0, y, w, y, grid);
        }
        // diagonals so the corners are easy to judge
        c.drawLine(0, 0, w, h, grid);
        c.drawLine(w, 0, 0, h, grid);

        // rulers going in from each edge, 0-100 px
        for (int d = 5; d <= 100; d += 5) {
            int len = d % 10 == 0 ? 24 : 12;
            for (int p = w / 4; p < w; p += w / 4) {        // top and bottom, 3 places
                c.drawLine(p - len, d, p + len, d, tick);
                c.drawLine(p - len, h - 1 - d, p + len, h - 1 - d, tick);
            }
            for (int p = h / 6; p < h; p += h / 6) {        // left and right, 5 places
                c.drawLine(d, p - len, d, p + len, tick);
                c.drawLine(w - 1 - d, p - len, w - 1 - d, p + len, tick);
            }
            if (d % 20 == 0) {
                String s = String.valueOf(d);
                c.drawText(s, w / 4 + 30, d + 8, num);
                c.drawText(s, w / 4 + 30, h - d + 8, num);
            }
            if (d % 40 == 20) {            // side labels: 20, 60, 100 (no overlap)
                String s = String.valueOf(d);
                c.drawText(s, d - 8, h / 6 - 30, num);
                c.drawText(s, w - d - 8 - num.measureText(s) + 16, h / 6 - 30, num);
            }
        }

        // the edge itself, plus corner marks
        float e = 1.5f;
        c.drawLine(0, e, w, e, edge);
        c.drawLine(0, h - e, w, h - e, edge);
        c.drawLine(e, 0, e, h, edge);
        c.drawLine(w - e, 0, w - e, h, edge);
        int k = 60;
        Paint thick = new Paint(edge);
        thick.setStrokeWidth(8);
        c.drawLine(0, 4, k, 4, thick);
        c.drawLine(4, 0, 4, k, thick);
        c.drawLine(w - k, 4, w, 4, thick);
        c.drawLine(w - 4, 0, w - 4, k, thick);
        c.drawLine(0, h - 4, k, h - 4, thick);
        c.drawLine(4, h - k, 4, h, thick);
        c.drawLine(w - k, h - 4, w, h - 4, thick);
        c.drawLine(w - 4, h - k, w - 4, h, thick);
    }
}
