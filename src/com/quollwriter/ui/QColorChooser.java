package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ActionAdapter;


public class QColorChooser extends Box
{

    public static final String   SWATCH_TYPE = "swatch";
    public static final String   RGB_TYPE = "rgb";
    private List<ChangeListener> changeListeners = new ArrayList ();
    private JPanel               rgbPanel = null;
    private JLabel               advLink = null;
    private JSlider              rs = null;
    private JSlider              gs = null;
    private JSlider              bs = null;

    public QColorChooser(String colors,
                         Color  initial)
    {

        super (BoxLayout.Y_AXIS);

        this.rs = new JSlider (0,
                               255);
        this.rs.setValue (initial.getRed ());
        this.gs = new JSlider (0,
                               255);
        this.gs.setValue (initial.getGreen ());
        this.bs = new JSlider (0,
                               255);
        this.bs.setValue (initial.getBlue ());

        Box swatches = new Box (BoxLayout.Y_AXIS);
        swatches.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        StringTokenizer t = new StringTokenizer (colors,
                                                 ",");

        List<Color> cols = new ArrayList ();

        while (t.hasMoreTokens ())
        {

            cols.add (UIUtils.getColor (t.nextToken ().trim ()));


        }

        Box srow = new Box (BoxLayout.X_AXIS);
        srow.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        srow.add (this._getSwatch (Color.white));

        srow.add (Box.createHorizontalStrut (6));

        for (int i = 0; i < cols.size (); i++)
        {

            if (i == (cols.size () / 2))
            {

                swatches.add (srow);

                swatches.add (Box.createVerticalStrut (6));

                srow = new Box (BoxLayout.X_AXIS);
                srow.setAlignmentX (JComponent.LEFT_ALIGNMENT);

                srow.add (this._getSwatch (Color.black));

                srow.add (Box.createHorizontalStrut (6));

            } else
            {

                if (i > 0)
                {

                    srow.add (Box.createHorizontalStrut (6));

                }

            }

            srow.add (this._getSwatch (cols.get (i)));

        }

        swatches.add (srow);

        this.add (swatches);

        this.add (Box.createVerticalStrut (6));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (Box.createHorizontalGlue ());
        b.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        final QColorChooser _this = this;

        this.advLink = UIUtils.createClickableLabel ("Advanced",
                                                     null);

        this.advLink.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.advLink.setToolTipText ("Click to set the color via RGB");

        this.advLink.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    _this.rgbPanel.setVisible (true);

                    _this.rgbPanel.revalidate ();

                    _this.rgbPanel.repaint ();

                    _this.advLink.setVisible (false);

                }

            });

        b.add (this.advLink);

        this.add (Box.createVerticalStrut (6));

        this.add (b);

        FormLayout rgbfl = new FormLayout ("right:p, 6px, 150px, 6px, 5dlu",
                                           "p, 6px, p, 6px, p");

        PanelBuilder rgbbuilder = new PanelBuilder (rgbfl);

        CellConstraints cc = new CellConstraints ();

        rgbbuilder.addLabel ("Red",
                             cc.xy (1,
                                    1));

        rs.setOpaque (false);
        rgbbuilder.add (rs,
                        cc.xy (3,
                               1));

        ChangeListener cl = new ChangeAdapter ()
        {

            public void stateChanged (ChangeEvent ev)
            {

                // Get the new color, then fire a state change.
                Color c = new Color (_this.rs.getValue (),
                                     _this.gs.getValue (),
                                     _this.bs.getValue ());

                _this.fireChangeEvent (c,
                                       RGB_TYPE);

            }

        };

        this.rs.addChangeListener (cl);
        this.gs.addChangeListener (cl);
        this.bs.addChangeListener (cl);

        rgbbuilder.addLabel ("Green",
                             cc.xy (1,
                                    3));

        gs.setOpaque (false);
        rgbbuilder.add (gs,
                        cc.xy (3,
                               3));

        rgbbuilder.addLabel ("Blue",
                             cc.xy (1,
                                    5));

        bs.setOpaque (false);

        rgbbuilder.add (bs,
                        cc.xy (3,
                               5));

        this.rgbPanel = rgbbuilder.getPanel ();
        this.rgbPanel.setOpaque (false);
        this.rgbPanel.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        this.rgbPanel.setVisible (false);

        this.add (this.rgbPanel);
        this.setOpaque (false);
        this.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        if (initial != null)
        {

            this.fireChangeEvent (initial,
                                  SWATCH_TYPE);

        }

    }

    public static JPanel getSwatch (final Color col)
    {

        Border ccBorder = new MatteBorder (1,
                                           1,
                                           1,
                                           1,
                                           Environment.getBorderColor ());

        JPanel c = new JPanel ();
        c.setSize (20,
                   20);
        c.setMinimumSize (new Dimension (20,
                                         20));
        c.setMaximumSize (new Dimension (20,
                                         20));
        c.setPreferredSize (c.getMinimumSize ());
        c.setBorder (ccBorder);
        c.setBackground (col);

        return c;

    }

    public void hideAdvanced ()
    {

        this.rgbPanel.setVisible (false);

        this.advLink.setVisible (true);

        this.rgbPanel.revalidate ();

        this.rgbPanel.repaint ();

    }

    public JLabel getAdvancedLink ()
    {

        return this.advLink;

    }

    private JPanel _getSwatch (final Color col)
    {

        JPanel c = QColorChooser.getSwatch (col);

        final QColorChooser _this = this;

        c.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    // Inform the change listeners.
                    _this.fireChangeEvent (col,
                                           SWATCH_TYPE);

                }

            });

        return c;

    }

    public void removeChangeListener (ChangeListener l)
    {

        this.changeListeners.remove (l);

    }

    public void addChangeListener (ChangeListener l)
    {

        this.changeListeners.add (l);

    }

    protected void fireChangeEvent (Color  c,
                                    String type)
    {

        NewColorStateChangeEvent ev = new NewColorStateChangeEvent (c,
                                                                    type);

        for (ChangeListener cl : this.changeListeners)
        {

            cl.stateChanged (ev);

        }

        // Sneaky :)
        this.rs.setValue (c.getRed ());
        this.gs.setValue (c.getGreen ());
        this.bs.setValue (c.getBlue ());

    }

    public static QPopup getColorChooserPopup (String         colors,
                                               Color          initialColor,
                                               ChangeListener cl,
                                               ActionListener closeListener)
    {

        final QPopup qp = new QPopup ("Select a Color",
                                      null,
                                      null);

        final QColorChooser cc = new QColorChooser (colors,
                                                    initialColor);

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "Click to close",
                                              closeListener);
        
        List<JButton> buts = new ArrayList ();
        buts.add (close);
        
        qp.getHeader ().setControls (UIUtils.createButtonBar (buts));
                                                    
        close.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.setVisible (false);

                cc.hideAdvanced ();

            }

        });

        cc.setBorder (new EmptyBorder (10,
                                       10,
                                       10,
                                       10));

        if (cl != null)
        {

            cc.addChangeListener (cl);

        }

        cc.addChangeListener (new ChangeAdapter ()
            {

                public void stateChanged (ChangeEvent ev)
                {

                    NewColorStateChangeEvent nev = (NewColorStateChangeEvent) ev;

                    if (nev.getType ().equals (QColorChooser.SWATCH_TYPE))
                    {

                        qp.setVisible (false);

                        cc.hideAdvanced ();

                    }

                }

            });

        cc.getAdvancedLink ().addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    // Rebound.
                    qp.setBounds (qp.getLocation ().x,
                                  qp.getLocation ().y,
                                  qp.getPreferredSize ().width,
                                  qp.getPreferredSize ().height);

                }

            });

        qp.setContent (cc);

        return qp;

    }

}
