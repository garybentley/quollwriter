package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.Set;
import java.util.LinkedHashSet;

public class Dragger extends MouseAdapter
{

    private Component within = null;
    private Component dragItem = null;
    private Component dragHandle = null;
    private Point     last = null;
    private Point     startLoc = null;
    private boolean   constrainToX = false;
    private boolean   constrainToY = false;
    private Set<DragListener> listeners = new LinkedHashSet ();
    private int dragItemLayer = -1;
    
    public Dragger(Component dragHandle,
                   Component dragItem,
                   Component within)
    {

        dragHandle.addMouseMotionListener (this);
        dragHandle.addMouseListener (this);
        this.dragItem = dragItem;
        this.within = within;
        this.dragHandle = dragHandle;

        this.dragItem.addMouseListener (this);

    }

    public void removeDragListener (DragListener l)
    {
        
        this.listeners.remove (l);
        
    }

    public void addDragListener (DragListener l)
    {
        
        this.listeners.add (l);
        
    }

    protected void fireDragEvent (int   type,
                                  Point start,
                                  Point end)
    {
        
        final Dragger _this = this;
        
        DragEvent ev = new DragEvent (dragItem,
                                      start,
                                      end);

        for (DragListener d : this.listeners)
        {
            
            if (type == DragEvent.FINISHED)
            {
                
                if (_this.within instanceof JLayeredPane)
                {
                    
                    JLayeredPane p = (JLayeredPane) _this.within;
                    
                    p.setLayer (_this.dragItem,
                                _this.dragItemLayer);
                    
                }

                d.dragFinished (ev);
                
            }
            
            if (type == DragEvent.STARTED)
            {
                
                if (_this.within instanceof JLayeredPane)
                {
                    
                    JLayeredPane p = (JLayeredPane) _this.within;
                    
                    _this.dragItemLayer = p.getLayer (_this.dragItem);
                    
                    p.setLayer (_this.dragItem,
                                JLayeredPane.DRAG_LAYER);
                    
                }
                
                d.dragStarted (ev);
                
            }
            
            if (type == DragEvent.IN_PROGRESS)
            {
                
                d.dragInProgress (ev);
                
            }
            
        }
        
    }

    public void mouseReleased (MouseEvent e)
    {

        this.fireDragEvent (DragEvent.FINISHED,
                            this.startLoc,
                            this.dragItem.getLocation ());

        this.dragHandle.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));

        this.startLoc = null;
        this.last = null;
        
    }

    public void mouseMoved (MouseEvent e)
    {

        // Ignore.

    }

    public void mouseDragged (MouseEvent e)
    {

        Component c = (Component) e.getSource ();

        e = SwingUtilities.convertMouseEvent (c,
                                              e,
                                              this.within);

        if (this.last != null)
        {

            Point l = this.dragItem.getLocation ();
            
            Point ep = e.getPoint ();

            SwingUtilities.convertPointToScreen (ep,
                                                      this.within);

            Point sl = new Point (l.x, l.y);

            if (!this.constrainToY)
            {
                
                int diffX = (ep.x - this.last.x);

                l.x += diffX;
                
            }

            if (!this.constrainToX)
            {
                
                int diffY = (ep.y - this.last.y);
                
                l.y += diffY;
                
            }

            this.dragItem.setLocation (l);

            this.last = ep;
            this.fireDragEvent (DragEvent.IN_PROGRESS,
                                sl,
                                l);

        } else {
            
            this.startLoc = this.dragItem.getLocation ();
            
            Point p = e.getPoint ();
            this.dragItem.getParent ().setComponentZOrder (this.dragItem,
                                                           0);
            SwingUtilities.convertPointToScreen (p,
                                                             this.within);
            this.last = p;
            
            this.dragHandle.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));
            
            this.fireDragEvent (DragEvent.STARTED,
                                this.startLoc,
                                null);
            
        }

    }
    
    public void constrainToX (boolean v)
    {
        
        this.constrainToX = v;
        
    }

    public void constrainToY (boolean v)
    {
        
        this.constrainToY = v;
        
    }

}
