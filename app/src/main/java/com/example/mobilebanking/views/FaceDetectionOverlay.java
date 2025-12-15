package com.example.mobilebanking.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom overlay view to display face detection rectangle and guidance
 */
public class FaceDetectionOverlay extends View {
    private Rect faceRect;
    private float faceSizePercentage = 0f;
    private boolean isFaceDetected = false;
    private boolean isReadyToCapture = false;
    private int countdown = 0; // Countdown seconds (3, 2, 1)
    private Paint borderPaint;
    private Paint guidePaint;
    private Paint textPaint;
    private Paint countdownPaint;
    private RectF guideRect;
    
    // Colors
    private static final int COLOR_DETECTING = Color.parseColor("#FF9800"); // Orange
    private static final int COLOR_READY = Color.parseColor("#4CAF50"); // Green
    private static final int COLOR_ERROR = Color.parseColor("#F44336"); // Red
    
    public FaceDetectionOverlay(Context context) {
        super(context);
        init();
    }
    
    public FaceDetectionOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public FaceDetectionOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Border paint for face rectangle
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(COLOR_DETECTING);
        
        // Guide paint for guidance rectangle
        guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(2f);
        guidePaint.setColor(Color.WHITE);
        guidePaint.setAlpha(128);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // Countdown paint (large, bold)
        countdownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        countdownPaint.setColor(COLOR_READY);
        countdownPaint.setTextSize(120f);
        countdownPaint.setTextAlign(Paint.Align.CENTER);
        countdownPaint.setStyle(Paint.Style.FILL);
        countdownPaint.setFakeBoldText(true);
        
        guideRect = new RectF();
    }
    
    public void setCountdown(int seconds) {
        this.countdown = seconds;
        postInvalidate();
    }
    
    public void updateFaceRect(Rect rect, float sizePercentage, boolean ready) {
        this.faceRect = rect;
        this.faceSizePercentage = sizePercentage;
        this.isFaceDetected = rect != null;
        this.isReadyToCapture = ready;
        
        // Update border color based on state
        if (isReadyToCapture) {
            borderPaint.setColor(COLOR_READY);
        } else if (isFaceDetected) {
            borderPaint.setColor(COLOR_DETECTING);
        } else {
            borderPaint.setColor(COLOR_ERROR);
        }
        
        postInvalidate();
    }
    
    public void clearFaceRect() {
        this.faceRect = null;
        this.isFaceDetected = false;
        this.isReadyToCapture = false;
        this.countdown = 0;
        postInvalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw guidance oval (center of screen, 3:4 ratio)
        // Calculate oval size: 70% of screen width, maintain 3:4 ratio (width:height)
        // For face detection, we want an oval that's taller than wide (portrait orientation)
        float guideWidth = width * 0.7f;
        float guideHeight = guideWidth * (4f / 3f); // 3:4 ratio (width:height) - height is 4/3 of width
        
        // Ensure it fits on screen
        if (guideHeight > height * 0.75f) {
            guideHeight = height * 0.75f;
            guideWidth = guideHeight * (3f / 4f); // Maintain 3:4 ratio
        }
        
        // Center the oval
        float guideLeft = (width - guideWidth) / 2f;
        float guideTop = (height - guideHeight) / 2f;
        guideRect.set(guideLeft, guideTop, guideLeft + guideWidth, guideTop + guideHeight);
        
        // Draw guide oval (dashed style) - this is the target area for face
        guidePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{20f, 10f}, 0f));
        guidePaint.setColor(Color.WHITE);
        guidePaint.setAlpha(180);
        canvas.drawOval(guideRect, guidePaint);
        
        // Draw inner border for better visibility
        Paint innerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(1f);
        innerBorderPaint.setColor(Color.WHITE);
        innerBorderPaint.setAlpha(100);
        RectF innerRect = new RectF(guideRect.left + 5, guideRect.top + 5, 
                                    guideRect.right - 5, guideRect.bottom - 5);
        canvas.drawOval(innerRect, innerBorderPaint);
        
        guidePaint.setPathEffect(null); // Reset
        
        // Draw face detection rectangle if face is detected
        if (isFaceDetected && faceRect != null) {
            // Convert face rect to screen coordinates if needed
            RectF faceRectF = new RectF(faceRect);
            canvas.drawRoundRect(faceRectF, 8f, 8f, borderPaint);
            
            // Draw percentage text above rectangle
            String percentageText = String.format("%.0f%%", faceSizePercentage);
            float textY = faceRectF.top - 20f;
            if (textY < 50f) {
                textY = faceRectF.bottom + 60f;
            }
            canvas.drawText(percentageText, faceRectF.centerX(), textY, textPaint);
        }
        
        // Draw countdown if ready to capture
        if (isReadyToCapture && countdown > 0) {
            String countdownText = String.valueOf(countdown);
            float countdownY = guideRect.centerY();
            // Draw background circle for countdown
            Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setColor(Color.WHITE);
            circlePaint.setAlpha(200);
            canvas.drawCircle(width / 2f, countdownY, 80f, circlePaint);
            
            // Draw countdown number
            countdownPaint.setColor(COLOR_READY);
            canvas.drawText(countdownText, width / 2f, countdownY + 40f, countdownPaint);
        }
        
        // Draw instruction text at bottom
        String instruction;
        if (isReadyToCapture) {
            if (countdown > 0) {
                instruction = "Giữ nguyên vị trí... " + countdown + " giây";
            } else {
                instruction = "Giữ nguyên vị trí...";
            }
            textPaint.setColor(COLOR_READY);
        } else if (isFaceDetected) {
            instruction = "Di chuyển gần hơn";
            textPaint.setColor(COLOR_DETECTING);
        } else {
            instruction = "Đặt khuôn mặt vào khung";
            textPaint.setColor(Color.WHITE);
        }
        
        canvas.drawText(instruction, width / 2f, height - 100f, textPaint);
    }
}

