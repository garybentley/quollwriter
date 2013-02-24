package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


public class Accordion extends Box
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

                if ((above.content.getHeight () == 0) &&
                    (diffY < 0))
                {

                    above = this.above.getPreviousVisibleContent ();

                }

                if (((below.content == null) ||
                     (below.content.getHeight () == 0)) &&
                    (diffY > 0))
                {

                    below = this.below.getNextVisibleContent ();

                }

                if ((above.content == null) ||
                    (below.content == null))
                {

                    return;

                }

                Dimension aPref = above.content.getSize ();
                Dimension bPref = below.content.getSize ();

                aNewHeight = aPref.height + diffY;

                if (diffY != 0)
                {

                    if (aNewHeight > 0)
                    {

                        above.content.setPreferredSize (new Dimension (aPref.width,
                                                                       aNewHeight));

                        above.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             Short.MAX_VALUE));

                        above.content.setVisible (true);

                    } else
                    {

                        above.content.setVisible (false);
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

                        below.content.setPreferredSize (new Dimension (bPref.width,
                                                                       bNewHeight));

                        below.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             Short.MAX_VALUE));

                        below.content.setVisible (true);

                    } else
                    {

                        below.content.setVisible (false);
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

    public class ItemHandler extends MouseAdapter
    {

        private Item      item = null;
        private Accordion acc = null;
        private Dimension contentOldSize = null;

        public ItemHandler(Item      item,
                           Accordion acc)
        {

            this.item = item;
            this.acc = acc;

            if (this.item.content != null)
            {

                this.item.control.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

                this.item.control.addMouseListener (this);

            }

        }

        public void mouseReleased (MouseEvent ev)
        {

/*
            if ((this.item.content.isVisible ())
                &&
                (this.acc.getVisibleCount () == 1)
               )
            {

                return;

            }
*/

            if (ev.isPopupTrigger ())
            {
                
                return;
                
            }

            if (this.item.content.isVisible ())
            {

                this.contentOldSize = this.item.content.getPreferredSize ();

            }

            this.item.content.setVisible (!this.item.content.isVisible ());

            if (this.item.content.isVisible ())
            {

                this.item.content.setPreferredSize (this.contentOldSize);
                this.item.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                         Short.MAX_VALUE));

            } else
            {

                this.item.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                         this.item.getPreferredSize ().height));

            }

            /*
            } else {

            this.contentOldSize = this.item.content.getPreferredSize ();

            }
             */
            this.item.revalidate ();
            this.item.repaint ();

            this.acc.revalidate ();
            this.acc.repaint ();
            /*
            Container c = this.acc.getParent ();

            if (c != null)
            {

            c.repaint ();

            }
             */
        }

    }

    public class Item extends Box
    {

        public Component header = null;
        public Component content = null;
        public Component control = null;
        public Item      nextItem = null;
        public Item      previousItem = null;

        public Item(Component header,
                    Component control,
                    Component content)
        {

            super (BoxLayout.PAGE_AXIS);

            if (header != null)
            {

                this.add (header);

            }

            if (content != null)
            {

                this.add (content);

            }

            this.header = header;
            this.control = control;
            this.content = content;

        }

        public Item getPreviousVisibleContent ()
        {

            if (this.content != null)
            {

                if (this.content.isVisible ())
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

            if (this.content != null)
            {

                if (this.content.isVisible ())
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

    private java.util.List items = new ArrayList ();
    private Border         itemBorder = null;
    private int            dividerSize = 7;
    private HeaderFactory  headerFactory = null;

    public Accordion(int axis)
    {

        super (axis);

        this.setAutoscrolls (true);
        this.applyComponentOrientation (ComponentOrientation.LEFT_TO_RIGHT);

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

                    if (it.content != null)
                    {

                        it.content.setVisible (vis);

                        Dimension pref = it.content.getPreferredSize ();

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

                        it.content.setPreferredSize (pref);

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
            b.append (((it.content != null) ? it.content.isVisible () : false));
            b.append (",");
            b.append (it.content.getWidth ());
            b.append (",");
            b.append (it.content.getHeight ());

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

            if (((Item) items.get (i)).content == null)
            {

                continue;

            }

            if (((Item) items.get (i)).content.isVisible ())
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
        h.setFont (h.getFont ().deriveFont ((float) com.quollwriter.ui.UIUtils.scaleToScreenSize (14d)).deriveFont (Font.PLAIN));
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
    public void add (Component header,
                     Component control,
                     Component content)
    {

        if (control == null)
        {

            control = header;

        }

        Item ai = new Item (header,
                            control,
                            content);

        // ai.setDoubleBuffered (true);
        ai.setBorder (this.itemBorder);

        if (this.items.size () > 0)
        {

            Item last = (Item) this.items.get (this.items.size () - 1);

            last.nextItem = ai;
            ai.previousItem = last;

            // Create a strut.
            JComponent s = (JComponent) Box.createVerticalStrut (this.dividerSize);

            s.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));
            s.setOpaque (false);

            new DividerHandler (last,
                                ai,
                                s,
                                this);

            this.add (s);

        }

        this.items.add (ai);

        this.add (ai);

        this.validate ();

        // Left align everything.
        if (content != null)
        {

            ((JComponent) content).setAlignmentX (Component.LEFT_ALIGNMENT);

        }

        if (header != null)
        {

            ((JComponent) header).setAlignmentX (Component.LEFT_ALIGNMENT);

        }

        new ItemHandler (ai,
                         this);

    }

}
