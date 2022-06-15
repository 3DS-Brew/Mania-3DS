package com.decomp.rsdkv5;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class RSDKSurface extends SurfaceView implements SurfaceHolder.Callback,
        View.OnKeyListener, View.OnTouchListener {

    public int width;
    public int height;

    public RSDKSurface(Context context) {
        super(context);
        getHolder().addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
    }

    public void handleResume() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        nativeSetSurface(surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        width = w;
        height = h;
        nativeSetSurface(surfaceHolder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        nativeSetSurface(null);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // this is mostly stolen from sdl LOL
        final int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        int i = -1;

        switch(action) {
            case MotionEvent.ACTION_MOVE:
                for (i = 0; i < pointerCount; i++) {
                    int finger = event.getPointerId(i);
                    float x = event.getX(i) / width;
                    float y = event.getY(i) / height;

                    nativeOnTouch(finger, action, x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
                // Primary pointer up/down, the index is always zero
                i = 0;
                /* fallthrough */
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // Non primary pointer up/down
                if (i == -1) {
                    i = event.getActionIndex();
                }

                int finger = event.getPointerId(i);
                float x = event.getX(i) / width;
                float y = event.getY(i) / height;

                nativeOnTouch(finger, action, x, y);
            }
                break;

            case MotionEvent.ACTION_CANCEL:
                for (i = 0; i < pointerCount; i++) {
                    int finger = event.getPointerId(i);
                    float x = event.getX(i) / width;
                    float y = event.getY(i) / height;

                    nativeOnTouch(finger, MotionEvent.ACTION_UP, x, y);
                }
                break;

            default:
                break;
        }

        return true;
    }

    // NATIVE START
    static {
        System.loadLibrary("RetroEngine");
    }

    public static native void nativeSetSurface(Surface surface);
    public static native void nativeOnTouch(int fingerID, int action, float x, float y);
}
