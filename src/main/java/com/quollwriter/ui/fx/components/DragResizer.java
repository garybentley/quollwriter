package com.quollwriter.ui.fx.components;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Copied from: https://stackoverflow.com/questions/16925612/how-to-resize-component-with-mouse-drag-in-javafx by Andy Till.
 * {@link DragResizer} can be used to add mouse listeners to a {@link Region}
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 * <p>
 * Only height resizing is currently implemented. Usage: <pre>DragResizer.makeResizable(myAnchorPane);</pre>
 *
 * @author atill
 *
 */
public class DragResizer {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 7;

    private final Region region;

    private double y;

    private boolean initMinHeight;

    private boolean dragging;

    private BooleanProperty draggingProp = new SimpleBooleanProperty (false);

    private DragResizer(Region aRegion) {
        region = aRegion;
    }

    public static DragResizer makeResizable(Region region) {
        final DragResizer resizer = new DragResizer(region);

        region.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mousePressed(event);
            }});
        region.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseDragged(event);
            }});
        region.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseOver(event);
            }});
        region.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseReleased(event);
            }});

        return resizer;
    }

    public BooleanProperty draggingProperty ()
    {

        return this.draggingProp;

    }

    public boolean isDragging ()
    {

        return this.dragging;

    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        this.draggingProp.setValue (false);
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
        if(isInDraggableZone(event) || dragging) {
            region.setCursor(Cursor.E_RESIZE);
        }
        else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    protected boolean isInDraggableZone(MouseEvent event) {
        return event.getX() >= (region.getWidth () - RESIZE_MARGIN);
    }

    protected void mouseDragged(MouseEvent event) {
        if(!dragging) {
            return;
        }

        double mousey = event.getX();

        double newHeight = region.getWidth () + (mousey - y);

        region.setPrefWidth(newHeight);

        y = mousey;
    }

    protected void mousePressed(MouseEvent event) {

        // ignore clicks outside of the draggable margin
        if(!isInDraggableZone(event)) {
            return;
        }

        dragging = true;
        this.draggingProp.setValue (true);

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinHeight) {
            //region.setMinWidth(region.getWidth());
            initMinHeight = true;
        }

        y = event.getX();
    }
}
