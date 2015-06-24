package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class Header extends Box
{

    // orig: public static Color defaultPaintLeftColor = new Color (112, 149, 226);
    public static Color defaultPaintLeftColor = UIUtils.getColor ("#516CA3");

    // public static Color defaultPaintLeftColor = new Color (77, 122, 244);
    public static Color  defaultPaintRightColor = null;
    public static Insets defaultPadding = new Insets (3,
                                                      3,
                                                      3,
                                                      3);

    /*
    public static Font defaultFont = new Font ("Tahoma",
                                               Font.BOLD,
                                               12);
     */
    public static Color defaultTitleColor = Color.WHITE;

    private JLabel        label = null;
    private PaintProvider paintProvider = null;
    private Component     controls = null;

    public Header(String    title,
                  Icon      icon,
                  Component controls)
    {

        this ();

        this.setTitle (title);
        this.setIcon (icon);

        this.setControls (controls);

    }

    public Header()
    {

        super (BoxLayout.LINE_AXIS);

        this.label = new JLabel ();

        this.label.setAlignmentY (Component.TOP_ALIGNMENT);
        this.label.setOpaque (false);
        //this.label.setVerticalAlignment (SwingConstants.TOP);
        
        //this.label.setVerticalTextPosition (SwingConstants.TOP);        
        
        this.add (this.label);
        this.add (Box.createHorizontalGlue ());

        this.paintProvider = new GradientPainter (Header.defaultPaintLeftColor,
                                                  Header.defaultPaintRightColor);

        this.setPadding (Header.defaultPadding);

        Font f = this.getFont ().deriveFont (Font.BOLD,
                                             14);

        this.setFont (f);
        this.setTitleColor (Header.defaultTitleColor);

    }

    public void setLabel (JLabel l)
    {

        this.label = l;
        this.label.setOpaque (false);
        
        this.remove (0);

        this.add (this.label,
                  0);

        this.validate ();
        this.repaint ();

    }

    public Component getControls ()
    {
        
        return this.controls;
        
    }
    
    public void setControls (Component c)
    {

        if (c == null)
        {

            return;

        }

        if (c instanceof JComponent)
        {
        
            ((JComponent) c).setAlignmentY (Component.TOP_ALIGNMENT);
        
        }
        
        int ind = -1;

        if (this.controls != null)
        {

            for (int i = 0; i < this.getComponentCount (); i++)
            {

                if (this.getComponent (i) == this.controls)
                {

                    ind = i;

                    break;

                }

            }

        }

        if (ind > -1)
        {

            this.remove (ind);

            this.add (c,
                      ind);

        } else
        {

            this.add (c);

        }

        this.controls = c;

        this.validate ();
        this.repaint ();

    }

    public JLabel getLabel ()
    {

        return this.label;

    }

    public void setPadding (Insets i)
    {

        if (i == null)
        {
            
            this.setBorder (null);
            
            return;
            
        }
    
        this.setBorder (new EmptyBorder (i));

    }

    public Header getClone (String    title,
                            Icon      icon,
                            Component controls)
    {

        Header n = new Header (title,
                               icon,
                               controls);

        n.setTitleColor (this.label.getForeground ());
        n.setPaintProvider (this.getPaintProvider ().getClone ());
        n.setFont (this.label.getFont ());

        return n;

    }

    public Dimension getMaximumSize ()
    {

        return new Dimension (Short.MAX_VALUE,
                              this.getPreferredSize ().height);

    }

    public Dimension getMinimumSize ()
    {

        return new Dimension (this.label.getPreferredSize ().width + 5 + ((this.controls != null) ? this.controls.getMinimumSize ().width : 0),
                              this.getPreferredSize ().height);

    }

    public PaintProvider getPaintProvider ()
    {

        return this.paintProvider;

    }

    public void setPaintProvider (PaintProvider p)
    {

        this.paintProvider = p;

    }

    public String getTitle ()
    {

        return this.label.getText ();

    }

    public Icon getIcon ()
    {

        return this.label.getIcon ();

    }

    public void setIcon (Icon i)
    {

        this.label.setIcon (i);

    }

    public void setTitleColor (Color c)
    {

        this.label.setForeground (c);

    }

    public void setTitle (String t)
    {

        this.label.setText (String.format ("<html>%s</html>",
                                           com.quollwriter.Environment.replaceObjectNames (t)));

    }

    public Font getFont ()
    {

        return this.label.getFont ();

    }

    public void setFont (Font f)
    {

        this.label.setFont (f);

    }

    public void setFontSize (float i)
    {

        this.label.setFont (this.label.getFont ().deriveFont (i));

    }

    public void setBold (boolean b)
    {

        this.label.setFont (this.label.getFont ().deriveFont ((b ? Font.BOLD : Font.PLAIN)));

    }

    protected void paintComponent (Graphics g)
    {

        if (this.paintProvider != null)
        {

            Paint p = this.paintProvider.getPaint (this);

            Graphics2D g2d = (Graphics2D) g;

            int w = this.getWidth ();
            int h = this.getHeight ();

            g2d.setPaint (p);
            g2d.fillRect (0,
                          0,
                          w,
                          h);

            this.setOpaque (false);

        }

        super.paintComponent (g);

        //this.setOpaque (true);

    }

    public static Header createBoldSubHeader (String    title,
                                              ImageIcon icon)
    {

        Header h = new Header (title,
                               ((icon == null) ? null : icon),
                               null);

        h.setFont (h.getFont ().deriveFont (Font.BOLD,
                                            12f));
        // h.setTitleColor (Color.BLACK);
        // h.setOpaque (false);
        // h.setBackground (new Color (0, 0, 0, 0));
        // h.setPaintProvider (null);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setBorder (new EmptyBorder (3,
                                      3,
                                      3,
                                      3));

        return h;

    }

}
