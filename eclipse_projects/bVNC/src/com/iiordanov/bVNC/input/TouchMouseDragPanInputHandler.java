package com.iiordanov.bVNC.input;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.iiordanov.bVNC.RemoteCanvas;
import com.iiordanov.bVNC.RemoteCanvasActivity;
import com.iiordanov.bVNC.Utils;

public class TouchMouseDragPanInputHandler extends AbstractGestureInputHandler {
    static final String TAG = "TouchMouseDragPanInputHandler";
    public static final String TOUCH_ZOOM_MODE_DRAG_PAN = "TOUCH_ZOOM_MODE_DRAG_PAN";

    /**
     * Divide stated fling velocity by this amount to get initial velocity
     * per pan interval
     */
    static final float FLING_FACTOR = 8;
    
    /**
     * @param c
     */
    public TouchMouseDragPanInputHandler(RemoteCanvasActivity va, RemoteCanvas v) {
        super(va, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iiordanov.bVNC.AbstractInputHandler#getName()
     */
    @Override
    public String getName() {
        return TOUCH_ZOOM_MODE_DRAG_PAN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iiordanov.bVNC.VncCanvasActivity.ZoomInputHandler#onKeyDown(int,
     *      android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent evt) {
        return keyHandler.onKeyDown(keyCode, evt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iiordanov.bVNC.VncCanvasActivity.ZoomInputHandler#onKeyUp(int,
     *      android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent evt) {
        return keyHandler.onKeyUp(keyCode, evt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iiordanov.bVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTrackballEvent(MotionEvent evt) {
        return trackballMouse(evt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
     */
    @Override
    public void onLongPress(MotionEvent e) {

        // If we've performed a right/middle-click and the gesture is not over yet, do not start drag mode.
        if (secondPointerWasDown || thirdPointerWasDown)
            return;
        
        Utils.performLongPressHaptic(canvas);
        canvas.displayShortToastMessage("Panning");
        endDragModesAndScrolling();
        panMode = true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent,
     *      android.view.MotionEvent, float, float)
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        RemotePointer p = canvas.getPointer();

        // onScroll called while scaling/swiping gesture is in effect. We ignore the event and pretend it was
        // consumed. This prevents the mouse pointer from flailing around while we are scaling.
        // Also, if one releases one finger slightly earlier than the other when scaling, it causes Android 
        // to stick a spiteful onScroll with a MASSIVE delta here. 
        // This would cause the mouse pointer to jump to another place suddenly.
        // Hence, we ignore onScroll after scaling until we lift all pointers up.
        boolean twoFingers = false;
        if (e1 != null)
            twoFingers = (e1.getPointerCount() > 1);
        if (e2 != null)
            twoFingers = twoFingers || (e2.getPointerCount() > 1);

        if (twoFingers||inSwiping||inScaling||scalingJustFinished)
            return true;

        activity.showKeyboardControls(false);
        
        if (!dragMode) {
            dragMode = true;
            p.processPointerEvent(getX(e1), getY(e1), e1.getActionMasked(), e1.getMetaState(), true, false, false, false, 0);
        } else {
            p.processPointerEvent(getX(e2), getY(e2), e2.getActionMasked(), e2.getMetaState(), true, false, false, false, 0);
        }
        canvas.panToMouse();
        return true;
    }
}

