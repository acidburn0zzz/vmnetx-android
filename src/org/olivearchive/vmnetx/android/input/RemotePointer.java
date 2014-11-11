package org.olivearchive.vmnetx.android.input;

import org.olivearchive.vmnetx.android.RemoteCanvas;
import org.olivearchive.vmnetx.android.SpiceCommunicator;

import android.view.MotionEvent;

public class RemotePointer {
    public static final int MOUSE_BUTTON_MOVE		= 0;
    public static final int MOUSE_BUTTON_LEFT		= 1;
    public static final int MOUSE_BUTTON_MIDDLE		= 2;
    public static final int MOUSE_BUTTON_RIGHT		= 3;
    public static final int MOUSE_BUTTON_SCROLL_UP	= 4;
    public static final int MOUSE_BUTTON_SCROLL_DOWN	= 5;

    public static final int PTRFLAGS_DOWN             = 0x8000;
    
    /**
     * Current and previous state of "mouse" buttons
     */
    private int pointerMask = 0;
    private int prevPointerMask = 0;

    private RemoteCanvas canvas;
    private SpiceCommunicator spice;

    /**
     * Indicates where the mouse pointer is located.
     */
    private int mouseX, mouseY;

    public RemotePointer (SpiceCommunicator s, RemoteCanvas c) {
        spice = s;
        mouseX = spice.framebufferWidth()/2;
        mouseY = spice.framebufferHeight()/2;
        canvas = c;
    }
    
    public int getX() {
        return mouseX;
    }

    public int getY() {
        return mouseY;
    }

    public void setX(int newX) {
        mouseX = newX;
    }

    public void setY(int newY) {
        mouseY = newY;
    }

    /**
     * Convert a motion event to a format suitable for sending over the wire
     * @param evt motion event; x and y must already have been converted from screen coordinates
     * to remote frame buffer coordinates.
     * @param downEvent True if "mouse button" (touch or trackball button) is down when this happens
     * @return true if event was actually sent
     */
    public boolean processPointerEvent(MotionEvent evt, boolean downEvent)
    {
        return processPointerEvent(evt, downEvent, false);
    }

    /**
     *  Overloaded processPointerEvent method which supports mouse scroll button.
     * @param evt motion event; x and y must already have been converted from screen coordinates
     * to remote frame buffer coordinates.
     * @param downEvent True if "mouse button" (touch or trackball button) is down when this happens
     * @param useRightButton If true, event is interpreted as happening with right mouse button
     * @param useMiddleButton If true, event is interpreted as click happening with middle mouse button
     * @param useScrollButton If true, event is interpreted as click happening with mouse scroll button
     * @param direction Indicates the direction of the scroll event: 0 for up, 1 for down, 2 for left, 3 for right.
     * @return true if event was actually sent
     */
    public boolean processPointerEvent(MotionEvent evt, boolean downEvent, 
                                       boolean useRightButton, boolean useMiddleButton, boolean useScrollButton, int direction) {
        return processPointerEvent((int)evt.getX(),(int)evt.getY(), evt.getActionMasked(), 
                                    evt.getMetaState(), downEvent, useRightButton, useMiddleButton, useScrollButton, direction);
    }
    
    /**
     *  Overloaded processPointerEvent method which supports middle mouse button.
     * @param evt motion event; x and y must already have been converted from screen coordinates
     * to remote frame buffer coordinates.
     * @param downEvent True if "mouse button" (touch or trackball button) is down when this happens
     * @param useRightButton If true, event is interpreted as happening with right mouse button
     * @param useMiddleButton If true, event is interpreted as click happening with middle mouse button
     * @return true if event was actually sent
     */
    public boolean processPointerEvent(MotionEvent evt, boolean downEvent, 
                                       boolean useRightButton, boolean useMiddleButton) {
        return processPointerEvent((int)evt.getX(),(int)evt.getY(), evt.getActionMasked(), 
                                    evt.getMetaState(), downEvent, useRightButton, useMiddleButton, false, -1);
    }

    /**
     * Convert a motion event to a format suitable for sending over the wire
     * @param evt motion event; x and y must already have been converted from screen coordinates
     * to remote frame buffer coordinates.
     * @param downEvent True if "mouse button" (touch or trackball button) is down when this happens
     * @param useRightButton If true, event is interpreted as happening with right mouse button
     * @return true if event was actually sent
     */
    public boolean processPointerEvent(MotionEvent evt, boolean downEvent, boolean useRightButton) {
        return processPointerEvent((int)evt.getX(),(int)evt.getY(), evt.getAction(), 
                                    evt.getMetaState(), downEvent, useRightButton, false, false, -1);
    }

    /**
     * Overloaded processPointerEvent method which supports right mouse button.
     * @param evt motion event; x and y must already have been converted from screen coordinates
     * to remote frame buffer coordinates.
     * @param downEvent True if "mouse button" (touch or trackball button) is down when this happens
     * @param useRightButton If true, event is interpreted as happening with right mouse button
     * @return true if event was actually sent
     */
    public boolean processPointerEvent(int x, int y, int action, int modifiers, boolean mouseIsDown, boolean useRightButton) {
        return processPointerEvent(x, y, action, modifiers, mouseIsDown, useRightButton, false, false, -1);
    }
    
    public boolean processPointerEvent(int x, int y, int action, int modifiers, boolean mouseIsDown, boolean useRightButton,
                                        boolean useMiddleButton, boolean useScrollButton, int direction) {
        
        if (spice != null && spice.isInNormalProtocol()) {
            if (useRightButton) {
                //android.util.Log.e("", "Mouse button right");
                pointerMask = MOUSE_BUTTON_RIGHT;
            } else if (useMiddleButton) {
                //android.util.Log.e("", "Mouse button middle");
                pointerMask = MOUSE_BUTTON_MIDDLE;
            } else if (action == MotionEvent.ACTION_DOWN) {
                //android.util.Log.e("", "Mouse button left");
                pointerMask = MOUSE_BUTTON_LEFT;
            } else if (useScrollButton) {
                if        ( direction == 0 ) {
                    //android.util.Log.e("", "Scrolling up");
                    pointerMask = MOUSE_BUTTON_SCROLL_UP;
                } else if ( direction == 1 ) {
                    //android.util.Log.e("", "Scrolling down");
                    pointerMask = MOUSE_BUTTON_SCROLL_DOWN;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                //android.util.Log.e("", "Mouse moving");
                pointerMask = MOUSE_BUTTON_MOVE;
            } else {
                //android.util.Log.e("", "Setting previous mouse action with mouse not down.");
                // If none of the conditions are satisfied, then set the pointer mask to
                // the previous mask so we can unpress any pressed buttons.
                pointerMask = prevPointerMask;
            }
            
            // Save the previous pointer mask other than action_move, so we can
            // send it with the pointer flag "not down" to clear the action.
            if (pointerMask != MOUSE_BUTTON_MOVE) {
                // If this is a new mouse down event, release previous button pressed to avoid confusing the remote OS.
                if (prevPointerMask != 0 && prevPointerMask != pointerMask) {
                    spice.writePointerEvent(mouseX, mouseY, modifiers|canvas.getKeyboard().getMetaState(), prevPointerMask & ~PTRFLAGS_DOWN);
                }
                prevPointerMask = pointerMask;
            }
            
            if (mouseIsDown /*&& pointerMask != MOUSE_BUTTON_MOVE*/) {
                //android.util.Log.e("", "Mouse pointer is down");
                pointerMask = pointerMask | PTRFLAGS_DOWN;
            } else {
                //android.util.Log.e("", "Mouse pointer is up");
                prevPointerMask = 0;
            }
                        
            canvas.invalidateMousePosition();
            mouseX = x;
            mouseY = y;
            if ( mouseX < 0) mouseX=0;
            else if ( mouseX >= spice.framebufferWidth())  mouseX = spice.framebufferWidth()  - 1;
            if ( mouseY < 0) mouseY=0;
            else if ( mouseY >= spice.framebufferHeight()) mouseY = spice.framebufferHeight() - 1;
            canvas.invalidateMousePosition();
            
            spice.writePointerEvent(mouseX, mouseY, modifiers|canvas.getKeyboard().getMetaState(), pointerMask);
            return true;
        }
        return false;
    }
}
