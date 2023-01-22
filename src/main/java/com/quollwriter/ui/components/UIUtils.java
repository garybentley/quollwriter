package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class UIUtils
{

    public static final DropShadowBorderX internalPanelDropShadow = new DropShadowBorderX (com.quollwriter.ui.UIUtils.getColor ("#cccccc"),
                                                                                           1,
                                                                                           8);

    public static final DropShadowBorderX popupPanelDropShadow = new DropShadowBorderX (com.quollwriter.ui.UIUtils.getColor ("#cccccc"), //UIManager.getColor ("Control"),
                                                                                        1,
                                                                                        12);

    public static Font cloneFont (Font f)
    {

        return new Font (f.getFamily (),
                         f.getStyle (),
                         f.getSize ());

    }

    public static Color cloneColor (Color c)
    {

        return new Color (c.getRed (),
                          c.getGreen (),
                          c.getBlue (),
                          c.getAlpha ());

    }

    public static Color getColor (String hexCode)
    {

        if (hexCode.startsWith ("#"))
        {

            hexCode = hexCode.substring (1);

        }

        hexCode = hexCode.toUpperCase ();

        return new Color (Integer.parseInt (hexCode.substring (0,
                                                               2),
                                            16),
                          Integer.parseInt (hexCode.substring (2,
                                                               4),
                                            16),
                          Integer.parseInt (hexCode.substring (4),
                                            16));
    }

    public static void setAsButton2 (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        if (c instanceof AbstractButton)
        {

            final AbstractButton b = (AbstractButton) c;

            b.setContentAreaFilled (false);
            b.setRolloverEnabled (true);

            // b.setMargin (new Insets (2, 2, 2, 2));
            b.addMouseListener (new MouseAdapter ()
                {

                    public void mouseEntered (MouseEvent ev)
                    {

                        b.setContentAreaFilled (true);

                    }

                    public void mouseExited (MouseEvent ev)
                    {

                        b.setContentAreaFilled (false);

                    }

                });

        }

    }

    public static void setAsButton (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static JComponent createButtonBar (java.util.List<? extends JComponent> buttons)
    {

        JToolBar tb = new JToolBar ();
        tb.setOpaque (false);
        tb.setFloatable (false);
        tb.setRollover (true);

        for (int i = 0; i < buttons.size (); i++)
        {

            JComponent b = buttons.get (i);

            tb.add (b);

        }

        return tb;

    }

    public static JButton createButton (ImageIcon      icon,
                                        String         toolTipText,
                                        ActionListener action)
    {
        
        JButton b = new JButton (icon);
        UIUtils.setAsButton (b);
        b.setToolTipText (toolTipText);
        b.setOpaque (false);
        
        if (action != null)
        {
        
            b.addActionListener (action);
            
        }
        
        return b;
        
    }

}
