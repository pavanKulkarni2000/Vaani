package com.vaani.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class EmptyItemDecoration extends RecyclerView.ItemDecoration {
    private final Rect r = new Rect();
    Paint paint;

    public EmptyItemDecoration() {
        paint = new Paint();
        paint.setTextSize(100);
        paint.setAlpha(100);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (Objects.requireNonNull(parent.getAdapter()).getItemCount() == 0)
            drawCenter(c, paint, "No files");
    }

    private void drawCenter(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

}