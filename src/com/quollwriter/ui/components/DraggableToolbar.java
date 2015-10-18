package com.quollwriter.ui.components;

import java.awt.event.*;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.Point;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.image.*;

public class DraggableToolbar extends JToolBar
{
    
    private DragSource ds = null;
    private boolean editable = false;
    private Component currentDrag = null;
    private DraggableToolbar connectTo = null;
    
    public DraggableToolbar ()
    {

        this.setDropTarget (new DropTarget (this,
                                         new DropTargetAdapter ()
        {
            
            @Override
            public void dragEnter (DropTargetDragEvent e)
            {
                                
                e.acceptDrag (e.getDropAction ());
                
            }
            
            @Override
            public void drop (DropTargetDropEvent e)
            {
                
                System.out.println ("DROP TB");
                
            }
            
        }));
    
    }
    
    public void setEditable (boolean v)
    {
        
        this.editable = v;
        
    }
    
    public void setConnectTo (DraggableToolbar tb)
    {
        
        this.connectTo = tb;
        
    }
    
    @Override
    public Component add (final Component c)
    {
        
        final DraggableToolbar _this = this;
        
        super.add (c);
        
        if (c.getDropTarget () != null)
        {
            
            return c;
            
        }
        
        DragSource.getDefaultDragSource ().createDefaultDragGestureRecognizer (c,
                                                                               DnDConstants.ACTION_MOVE,
                                                                               new DragGestureListener ()
        {
        
            @Override
            public void dragGestureRecognized (DragGestureEvent e)
            {
                
                if (!_this.editable)
                {
                    
                    return;
                    
                }
                
                BufferedImage im = com.quollwriter.ui.UIUtils.getImageOfComponent (c,
                                                                                   0,
                                                                                   0);

                // Determine which we are trying to drag.
                Point p = ((MouseEvent) e.getTriggerEvent ()).getPoint ();
                
                c.setEnabled (false);
                
                // Get the component.
                System.out.println ("HERE2");
                
                e.startDrag (Cursor.getPredefinedCursor (Cursor.MOVE_CURSOR),
                             im,
                             new Point (-10, -10),
                             new TransferableComponent (c),
                             new DragSourceAdapter ()
                {
        
                    @Override
                    public void dragExit (DragSourceEvent e)
                    {
                        
                        System.out.println ("EXIT");

                        Point l = e.getLocation ();
                        Container p = c.getParent ();
                        
                        SwingUtilities.convertPointFromScreen (l,
                                                               p);                        
                        
                        if (l.x < 0)
                        {
                            
                            p.add (c,
                                   0);
                            
                        c.setVisible (true);
                        
                        p.validate ();
                        p.repaint ();
return;
                        }
                        
                        if (l.x > p.getBounds ().width)
                        {
                            
                            p.add (c);

                        c.setVisible (true);
                        
                        p.validate ();
                        p.repaint ();
return;
                            
                        }

                        System.out.println ("EXIT");
                                                
                        //c.setVisible (false);
                        
                        p.validate ();
                        p.repaint ();
                        
                    }
        
                    @Override
                    public void dragEnter (DragSourceDragEvent e)
                    {
                    
                        // Get the component we are over.
                        Point l = e.getLocation ();
                        
                        System.out.println ("DD: " + c.getName ());
                        Component s = e.getDragSourceContext ().getComponent ();
                        System.out.println ("OTHER: " + s.getParent ().hashCode ());
                        // Do we have the same parent?
                        
                        Container p = c.getParent ();
                        
                        SwingUtilities.convertPointFromScreen (l,
                                                               p);
                        
                        if (!SwingUtilities.getLocalBounds (p).contains (l))
                        {
                            System.out.println ("HERE1");
                            p = null;
                            
                            if (_this.connectTo != null)
                            {
                                
                                l = e.getLocation ();
                                System.out.println ("HERE2: " + l);
                                SwingUtilities.convertPointFromScreen (l,
                                                                       _this.connectTo);
                                System.out.println ("HERE2.1: " + l);
                                System.out.println ("HERE2.2: " + SwingUtilities.getLocalBounds (_this.connectTo));
                                if (SwingUtilities.getLocalBounds (_this.connectTo).contains (l))
                                {
                                System.out.println ("HERE3");    
                                    p = _this.connectTo;
                                    
                                }
                                
                            }
                            
                        }
                        
                        if (p == null)
                        {
                            
                            return;
                            
                        }
                        
                        Component cc = p.getComponentAt (l);
                        System.out.println ("ENTERED: " + cc);
                        if (cc == c)
                        {
                            System.out.println ("RET");
                        c.getParent ().validate ();
                        c.getParent ().repaint ();
                        
                        if (_this.connectTo != null)
                        {
                            
                            _this.connectTo.validate ();
                            _this.connectTo.repaint ();
                            
                        }

                            return;
                            
                        }
                        
                        Container origParent = c.getParent ();
                                                            
                        for (int i = 0; i < p.getComponentCount (); i++)
                        {
                            
                            if (p.getComponent (i) == cc)
                            {
                                
                                s.setVisible (true);
                                
                                p.add (s,
                                       i);
                                
                                break;
                                
                            }
                            
                        }
                        System.out.println ("REPAINT: " + c.getParent ().hashCode ());
                        c.getParent ().validate ();
                        c.getParent ().repaint ();
                        
                        origParent.validate ();
                        origParent.repaint ();
                        System.out.println ("REPAINT3: " + origParent.hashCode ());
                        if (_this.connectTo != null)
                        {
                            System.out.println ("REPAINT2: " + _this.connectTo.hashCode ());
                            _this.connectTo.validate ();
                            _this.connectTo.repaint ();
                            
                        }
                        
                    }
                    
                    @Override
                    public void dragOver (DragSourceDragEvent e)
                    {
                    
                        // Get the component we are over.
                        Point l = e.getLocation ();
                        
                        Container p = c.getParent ();

                        if (p == null)
                        {
                            
                            return;
                            
                        }
                        
                        SwingUtilities.convertPointFromScreen (l,
                                                               p);
                        
                                                Component cc = p.getComponentAt (l);
                        
                        if (cc == c)
                        {
                            
                            return;
                            
                        }
                                    
                        for (int i = 0; i < p.getComponentCount (); i++)
                        {
                            
                            if (p.getComponent (i) == cc)
                            {
                                System.out.println ("ADD TO: " + i + ", " + c.getName ());
                                
                                c.setVisible (true);
                                
                                p.add (c,
                                       i);
                                
                                break;
                                
                            }
                            
                        }
                        
                        c.getParent ().validate ();
                        c.getParent ().repaint ();
                        
                        if (_this.connectTo != null)
                        {
                            
                            _this.connectTo.validate ();
                            _this.connectTo.repaint ();
                            
                        }
                        
                    }

                    @Override
                    public void dragMouseMoved (DragSourceDragEvent e)
                    {
                        System.out.println ("MOVE");
                    }
        
                    @Override
                    public void dragDropEnd (DragSourceDropEvent e)
                    {
                        
                        c.setVisible (true);
                        
                        c.setEnabled (true);

                        c.getParent ().validate ();
                        c.getParent ().repaint ();
                        
                        if (_this.connectTo != null)
                        {
                            
                            _this.connectTo.validate ();
                            _this.connectTo.repaint ();
                            
                        }
                        
                    }
                    
                });
                
            }
            
        });
        
        c.setDropTarget (new DropTarget (c,
                                         new DropTargetAdapter ()
        {
            
            @Override
            public void dragEnter (DropTargetDragEvent e)
            {
                                
                e.acceptDrag (e.getDropAction ());
                
            }
            
            @Override
            public void drop (DropTargetDropEvent e)
            {
                
                System.out.println ("DROP");
                
            }
            
        }));

        return c;
        
    }
    
    private class TransferableComponent implements Transferable, java.io.Serializable
    {
        
        private Component c = null;
        
        public TransferableComponent (Component c)
        {
            
            this.c = c;
            
        }
        
        public Component getComponent ()
        {
            
            return this.c;
            
        }
        
        public DataFlavor[] getTransferDataFlavors ()
        {
            
            return new DataFlavor[] { new DataFlavor (String.class, "Component") };
            
        }
        
        public Object getTransferData (DataFlavor f)
        {
            
            return this.c.getName ();
            
        }
        
        public boolean isDataFlavorSupported (DataFlavor f)
        {
            
            return true;
            
        }
        
    }
    
}