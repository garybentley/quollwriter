package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


public class Accordion extends ScrollableBox
{

    public class DividerHandler extends MouseAdapter
    {

        private Item      above = null;
        private Item      below = null;
        private Component divider = null;
        private Accordion acc = null;
        private Point     last = null;

        public DividerHandler(Item      above,
                              Item      below,
                              Component divider,
                              Accordion acc)
        {

            this.above = above;
            this.below = below;
            this.divider = divider;
            this.acc = acc;

            this.divider.addMouseMotionListener (this);
            this.divider.addMouseListener (this);

        }

        public void mouseReleased (MouseEvent ev)
        {

            this.last = null;

        }

        public void mouseDragged (MouseEvent ev)
        {

            ev = SwingUtilities.convertMouseEvent (this.divider,
                                                   ev,
                                                   this.acc);

            int aNewHeight = 0;
            int bNewHeight = 0;

            if (this.last != null)
            {

                Item above = this.above;
                Item below = this.below;

                int diffY = ev.getPoint ().y - this.last.y;

                if ((above.openContent.getHeight () == 0) &&
                    (diffY < 0))
                {

                    above = this.above.getPreviousVisibleContent ();

                }

                if (((below.openContent == null) ||
                     (below.openContent.getHeight () == 0)) &&
                    (diffY > 0))
                {

                    below = this.below.getNextVisibleContent ();

                }

                if ((above.openContent == null) ||
                    (below.openContent == null))
                {

                    return;

                }

                Dimension aPref = above.openContent.getSize ();
                Dimension bPref = below.openContent.getSize ();

                aNewHeight = aPref.height + diffY;

                if (diffY != 0)
                {

                    if (aNewHeight > 0)
                    {

                        above.openContent.setPreferredSize (new Dimension (aPref.width,
                                                                           aNewHeight));

                        above.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             Short.MAX_VALUE));

                        above.openContent.setVisible (true);

                    } else
                    {

                        above.openContent.setVisible (false);
                        above.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             above.header.getPreferredSize ().height));

                        diffY = 0;

                    }

                }

                bNewHeight = bPref.height - diffY;

                if (diffY != 0)
                {

                    if (bNewHeight > 0)
                    {

                        below.openContent.setPreferredSize (new Dimension (bPref.width,
                                                                           bNewHeight));

                        below.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             Short.MAX_VALUE));

                        below.openContent.setVisible (true);

                    } else
                    {

                        below.openContent.setVisible (false);
                        below.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             below.header.getPreferredSize ().height));

                    }

                }

                above.revalidate ();
                above.repaint ();

                below.revalidate ();
                below.repaint ();

                this.acc.revalidate ();
                this.acc.repaint ();

            }

            this.last = ev.getPoint ();

        }

    }

    public class Item extends Box
    {

        public Component header = null;
        public Component openContent = null;
        public Component closedContent = null;
        public Component control = null;
        public Item      nextItem = null;
        public Item      previousItem = null;
        private Dimension openContentOldSize = null;
        private Dimension closedContentOldSize = null;

        public Item(Component header,
                    Component control,
                    Component openContent,
                    Component closedContent)
        {

            super (BoxLayout.PAGE_AXIS);

            if (header != null)
            {

                this.add (header);

                if (header instanceof JComponent)
                {
                    
                    ((JComponent) header).setAlignmentX (Component.LEFT_ALIGNMENT);
                    
                }

            }

            if (openContent != null)
            {

                if (openContent instanceof JComponent)
                {
                    
                    ((JComponent) openContent).setAlignmentX (Component.LEFT_ALIGNMENT);
                    
                }

                this.add (openContent);

            }

            if (closedContent != null)
            {

                if (closedContent instanceof JComponent)
                {
                    
                    ((JComponent) closedContent).setAlignmentX (Component.LEFT_ALIGNMENT);
                    
                }

                this.add (closedContent);

                closedContent.setVisible (false);
                
            }

            this.header = header;
            this.control = control;
            this.openContent = openContent;
            this.closedContent = closedContent;
            
            if ((this.openContent != null)
                ||
                (this.closedContent != null)
               )
            {
                
                final Item _this = this;
                
                this.control.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

                this.control.addMouseListener (new MouseAdapter ()
                {
                
                    public void mouseReleased (MouseEvent ev)
                    {
            
                        if (ev.isPopupTrigger ())
                        {
                            
                            return;
                            
                        }

                        _this.setOpenContentVisible (!_this.openContent.isVisible ());

                    }
                    
                });
                
            }

        }
        
        public void setOpenContentVisible (boolean v)
        {
            
            if (this.openContent.isVisible ())
            {

                this.openContentOldSize = this.openContent.getPreferredSize ();

            } else {
                
                if (this.closedContent != null)
                {
                    
                    this.closedContentOldSize = this.closedContent.getPreferredSize ();
                    
                }
                
            }

            this.openContent.setVisible (v);
            
            if (this.closedContent != null)
            {
                
                this.closedContent.setVisible (!this.openContent.isVisible ());
                
            }
            /*
            this.setPreferredSize (null);
            
            if (this.openContent.isVisible ())
            {

                this.openContent.setPreferredSize (this.openContentOldSize);

            } else
            {

                if (this.closedContent != null)
                {

                    this.closedContent.setSize (this.closedContent.getPreferredSize ());
                    
                }

            }
            */
/*
            this.setMaximumSize (new Dimension (this.getPreferredSize ().width,
                                                this.getPreferredSize ().height));
*/
            this.revalidate ();
            this.repaint ();

            this.getParent ().validate ();
            this.getParent ().repaint ();
            
        }
        
        public Item getPreviousVisibleContent ()
        {

            if (this.openContent != null)
            {

                if (this.openContent.isVisible ())
                {

                    return this;

                }

            }

            if (this.previousItem == null)
            {

                return this;

            }

            return this.previousItem.getPreviousVisibleContent ();

        }

        public Item getNextVisibleContent ()
        {

            if (this.openContent != null)
            {

                if (this.openContent.isVisible ())
                {

                    return this;

                }

            }

            if (this.nextItem == null)
            {

                return this;

            }

            return this.nextItem.getNextVisibleContent ();

        }

    }

    private java.util.List<Item> items = new ArrayList ();
    private Border         itemBorder = null;
    private int            dividerSize = 7;
    private HeaderFactory  headerFactory = null;

    public Accordion(int axis)
    {

        super (axis);

        this.setAutoscrolls (true);
        this.applyComponentOrientation (ComponentOrientation.LEFT_TO_RIGHT);

    }

    public void setAllSectionsOpen (boolean v)
    {
        
        for (Item i : this.items)
        {
            
            i.setOpenContentVisible (v);
            
        }
        
    }
    
    /**
     * Expects a format: 0,<true|false>,<width>,<height>|1,<true/false>,<width>,<height>...
     * The numeric index indicates the item in the accoridion.
     *
     * @param s The format.
     */
    public void setState (String s)
    {

        if (s == null)
        {

            return;

        }

        StringTokenizer t = new StringTokenizer (s,
                                                 "|");

        while (t.hasMoreTokens ())
        {

            StringTokenizer tt = new StringTokenizer (t.nextToken ().trim ().toLowerCase (),
                                                      ",");

            if (tt.countTokens () > 1)
            {

                try
                {

                    int ind = Integer.parseInt (tt.nextToken ());

                    Item it = (Item) this.items.get (ind);

                    if (it == null)
                    {

                        continue;

                    }

                    boolean vis = Boolean.parseBoolean (tt.nextToken ());

                    if (it.openContent != null)
                    {

                        it.setOpenContentVisible (vis);

                        Dimension pref = it.openContent.getPreferredSize ();

                        if (tt.hasMoreTokens ())
                        {

                            try
                            {

                                pref.width = Integer.parseInt (tt.nextToken ());

                            } catch (Exception e)
                            {

                            }

                        }

                        if (tt.hasMoreTokens ())
                        {

                            try
                            {

                                pref.height = Integer.parseInt (tt.nextToken ());

                            } catch (Exception e)
                            {

                            }

                        }

                        //it.openContent.setPreferredSize (pref);

                        this.revalidate ();
                        this.repaint ();

                    }

                } catch (Exception e)
                {

                    // Just ignore it.

                }

            }

        }

    }

    /**
     * Returns a format: 0,<true|false>,<width>,<height>|1,<true/false>,<width>,<height>...
     * The numeric index indicates the item in the accoridion.
     *
     * @returns The format.
     */
    public String getState ()
    {

        StringBuffer b = new StringBuffer ();

        for (int i = 0; i < this.items.size (); i++)
        {

            if (b.length () > 0)
            {

                b.append ("|");

            }

            Item it = (Item) this.items.get (i);

            b.append (i);
            b.append (",");
            b.append (((it.openContent != null) ? it.openContent.isVisible () : false));
            b.append (",");
            b.append (it.openContent.getWidth ());
            b.append (",");
            b.append (it.openContent.getHeight ());

        }

        return b.toString ();

    }

    public void setItemBorder (Border b)
    {

        this.itemBorder = b;

    }

    public int getVisibleCount ()
    {

        int c = 0;

        for (int i = 0; i < items.size (); i++)
        {

            if (((Item) items.get (i)).openContent == null)
            {

                continue;

            }

            if (((Item) items.get (i)).openContent.isVisible ())
            {

                c++;

            }

        }

        return c;

    }

    public HeaderFactory getHeaderFactory ()
    {

        return this.headerFactory;

    }

    public void setHeaderFactory (HeaderFactory hf)
    {

        this.headerFactory = hf;

    }

    public Header addHeader (String    title,
                             Icon      icon,
                             Component content)
    {

        Header h = null;

        if (this.headerFactory != null)
        {

            h = this.headerFactory.getHeader (title,
                                              icon);

        } else
        {

            h = new Header (title,
                            icon,
                            null);

                                    // new
        h.setFont (h.getFont ().deriveFont ((float) com.quollwriter.ui.UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
        h.setPaintProvider (null);
        h.setTitleColor (UIUtils.getColor ("#333333"));
        h.setIcon (null);
        //h.setPadding (null);
        h.setPadding (new Insets (5, 5, 0, 5));
        h.setPaintProvider (new GradientPainter (Color.WHITE,
                                                 Color.WHITE));
        
        // end new

        }

        this.add (h,
                  null,
                  content);

        return h;

    }
/*
    public void add (EditPanel ep)
    {

        ep.init ();

        this.add (ep.getHeader (),
                  null,
                  ep.getPanel ());

    }
*/
    public Item add (Component header,
                     Component control,
                     Component openContent)
    {

        return this.add (header,
                         control,
                         openContent,
                         null);
    
    }
    
    public Item add (Component header,
                     Component control,
                     Component openContent,
                     Component closedContent)                     
    {

        if (control == null)
        {

            control = header;

        }

        Item ai = new Item (header,
                            control,
                            openContent,
                            closedContent);

        // ai.setDoubleBuffered (true);
        ai.setBorder (this.itemBorder);

        if (this.items.size () > 0)
        {

            Item last = (Item) this.items.get (this.items.size () - 1);

            last.nextItem = ai;
            ai.previousItem = last;

            // Create a strut.
            JComponent s = (JComponent) Box.createVerticalStrut (this.dividerSize);

            //s.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));
            s.setOpaque (false);
/*
            new DividerHandler (last,
                                ai,
                                s,
                                this);
*/
            this.add (s);

        }

        this.items.add (ai);

        this.add (ai);

        this.validate ();

        return ai;
        
    }

}
