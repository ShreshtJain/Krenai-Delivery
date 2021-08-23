package com.delivery.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.delivery.R;


public class SiteShotOverlayView extends LinearLayout {
    private Bitmap bitmap;

    public SiteShotOverlayView(Context context) {
        super(context);
    }

    public SiteShotOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SiteShotOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SiteShotOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.actionButton));
        paint.setStrokeWidth(4);


        Paint innerPaint = new Paint();
        innerPaint.setARGB(0, 0, 0, 0);
        innerPaint.setAlpha(0);
        innerPaint.setStyle(Paint.Style.STROKE);

        int x0 = canvas.getWidth() / 2;
        int y0 = canvas.getHeight() / 2;
        int dx = canvas.getHeight() / 3;
        int dy = canvas.getHeight() / 3;
        //draw guide box
        canvas.drawRect(x0 - 300, y0 - dy, x0 + 300, y0 + dy, paint);
        canvas.drawRect(x0 - 300, y0 - dy, x0 + 300, y0 + dy, innerPaint);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }
}