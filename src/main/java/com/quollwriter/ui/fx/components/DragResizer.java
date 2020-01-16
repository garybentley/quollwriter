package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.geometry.*;

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

    private double startX;
    private double startWidth;
    private double startY;
    private double startHeight;
    private double lastX;
    private double lastY;

    private boolean dragging;

    private DoubleProperty newHeightProp = new SimpleDoubleProperty (-1);
    private DoubleProperty newWidthProp = new SimpleDoubleProperty (-1);
    private BooleanProperty draggingProp = new SimpleBooleanProperty (false);
    private DoubleProperty xProp = new SimpleDoubleProperty (-1);
    private DoubleProperty yProp = new SimpleDoubleProperty (-1);
    private Set<Side> sides = new HashSet<> ();
    private Side dragZone = null;
    private Runnable onDragLeft = null;
    private Runnable onDragRight = null;
    private Runnable onDragUp = null;
    private Runnable onDragDown = null;
    private Consumer<Side> onDoubleClick = null;
    private int resizeMargin = RESIZE_MARGIN;

    private DragResizer(Region  aRegion,
                        Side... ss)
    {

        this (aRegion,
              RESIZE_MARGIN,
              ss);

    }

    private DragResizer(Region  aRegion,
                        int     resizeMargin,
                        Side... ss)
    {
        region = aRegion;
        this.resizeMargin = resizeMargin;

        this.sides.addAll (Arrays.asList (ss));

    }

    public static DragResizer makeResizable(Region region,
                                            Side... sides)
    {

        return DragResizer.makeResizable (region,
                                          RESIZE_MARGIN,
                                          sides);

    }

    public static DragResizer makeResizable(Region region,
                                            int    resizeMargin,
                                            Side... sides)
    {
        final DragResizer resizer = new DragResizer(region,
                                                    resizeMargin,
                                                    sides);

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
        region.setOnMouseClicked (ev ->
        {

            resizer.mouseClicked (ev);

        });

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

    public void mouseClicked (MouseEvent ev)
    {

        if (ev.isPopupTrigger ())
        {

            return;

        }

        if (ev.getClickCount () == 2)
        {

            if (this.onDoubleClick != null)
            {

                this.onDoubleClick.accept (this.getDragZone (ev));

            }

        }

    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        this.startX = -1;
        this.startWidth = 0;
        this.startY = -1;
        this.lastX = -1;
        this.lastY = -1;
        this.startHeight = 0;
        this.dragZone = null;
        this.draggingProp.setValue (false);
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {

        region.setCursor (Cursor.DEFAULT);

        Side z = this.getDragZone (event);

        if ((z == null)
            ||
            (this.dragging)
           )
        {

            return;

        }

        if ((z == Side.TOP)
            &&
            (this.sides.contains (Side.TOP))
           )
        {

            region.setCursor (Cursor.N_RESIZE);
            return;

        }

        if ((z == Side.BOTTOM)
            &&
            (this.sides.contains (Side.BOTTOM))
           )
        {

            region.setCursor (Cursor.S_RESIZE);
            return;

        }

        if ((z == Side.LEFT)
            &&
            (this.sides.contains (Side.LEFT))
           )
        {

            region.setCursor (Cursor.W_RESIZE);
            return;

        }

        if ((z == Side.RIGHT)
            &&
            (this.sides.contains (Side.RIGHT))
           )
        {

            region.setCursor (Cursor.E_RESIZE);
            return;

        }

    }

    protected boolean isInDraggableZone (MouseEvent ev)
    {

        if (this.sides.contains (Side.TOP))
        {

            return ev.getY () <= this.resizeMargin;

        }

        if (this.sides.contains (Side.BOTTOM))
        {

            return ev.getY () >= (region.getHeight () - this.resizeMargin);

        }

        if (this.sides.contains (Side.LEFT))
        {

            return ev.getX () <= this.resizeMargin;

        }

        if (this.sides.contains (Side.RIGHT))
        {

            return ev.getX() >= (region.getWidth () - this.resizeMargin);

        }

        return false;

    }

    protected Side getDragZone (MouseEvent ev)
    {

        if ((ev.getY () <= this.resizeMargin)
            &&
            (ev.getY () >= 0)
           )
        {

            return Side.TOP;

        }

        if ((ev.getY () >= (region.getHeight () - this.resizeMargin))
            &&
            (ev.getY () <= region.getHeight ())
           )
        {

            return Side.BOTTOM;

        }

        if ((ev.getX () <= this.resizeMargin)
            &&
            (ev.getX () >= 0)
           )
        {

            return Side.LEFT;

        }

        if ((ev.getX () >= (region.getWidth () - this.resizeMargin))
            &&
            (ev.getX () <= region.getWidth ())
           )
        {

            return Side.RIGHT;

        }

        return null;

    }

    public void setOnDraggingLeft (Runnable r)
    {

        this.onDragLeft = r;

    }

    public void setOnDraggingRight (Runnable r)
    {

        this.onDragRight = r;

    }

    public void setOnDraggingUp (Runnable r)
    {

        this.onDragUp = r;

    }

    public void setOnDraggingDown (Runnable r)
    {

        this.onDragDown = r;

    }

    public void setOnDoubleClick (Consumer<Side> r)
    {

        this.onDoubleClick = r;

    }

    protected void mouseDragged(MouseEvent event) {
        if(!dragging) {
            return;
        }

        double mousex = event.getScreenX ();

        double newWidth = this.startWidth + (mousex - this.startX);

        if ((mousex < this.lastX)
            &&
            (this.onDragLeft != null)
           )
        {

            this.onDragLeft.run ();

        }

        if (mousex > this.lastX)
        {

            if (this.onDragRight != null)
            {

                this.onDragRight.run ();

            }

        }

        double mousey = event.getScreenY ();

        double newHeight = this.startHeight + (mousey - this.startY);

        if ((mousey < this.lastY)
            &&
            (this.onDragUp != null)
           )
        {

            this.onDragUp.run ();

        }

        if (mousey > this.lastY)
        {

            if (this.onDragDown != null)
            {

                this.onDragDown.run ();

            }

        }

        this.newWidthProp.setValue (newWidth);
        this.newHeightProp.setValue (newHeight);
        this.xProp.setValue (mousex - this.startX);
        this.yProp.setValue (mousey - this.startY);
        this.lastX = mousex;
        this.lastY = mousey;
        event.consume ();
/*
        double min = this.region.getMinWidth ();

        if (min < 0)
        {

            min = this.region.minWidth (this.region.getHeight ());

        }

        if (newWidth < min)
        {

            return;

        }

        region.setPrefWidth(newWidth);

        //y = mousey;
*/
    }

    public DoubleProperty yProperty ()
    {

        return this.yProp;

    }

    public DoubleProperty xProperty ()
    {

        return this.xProp;

    }

    public DoubleProperty newWidthProperty ()
    {

        return this.newWidthProp;

    }

    public DoubleProperty newHeightProperty ()
    {

        return this.newHeightProp;

    }

    public Side getDragZone ()
    {

        return this.dragZone;

    }

    protected void mousePressed(MouseEvent event) {

        Side z = this.getDragZone (event);

        if ((z == null)
            ||
            (!this.sides.contains (z))
           )
        {

            return;

        }

        this.dragZone = z;

        dragging = true;
        this.draggingProp.setValue (true);

        this.startX = event.getScreenX();
        this.startWidth = this.region.getWidth ();
        this.startY = event.getScreenY();
        this.startHeight = this.region.getHeight ();
        this.lastX = this.startX;
        this.lastY = this.startY;
    }
}
