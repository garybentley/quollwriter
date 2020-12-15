package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.text.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.DocumentAdapter;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class QColorChooser extends Box implements UserPropertyListener
{

    public static final String   SWATCH_TYPE = "swatch";
    public static final String   RGB_TYPE = "rgb";
    private List<ChangeListener> changeListeners = new ArrayList ();
    private JPanel               rgbPanel = null;
    private JSlider              rs = null;
    private JSlider              gs = null;
    private JSlider              bs = null;
    private JSpinner             rsp = null;
    private JSpinner             gsp = null;
    private JSpinner             bsp = null;
    private JTextField  rgb = null;
    private Color currentColor = null;
    private Color initialColor = null;
    private boolean updating = false;
    private ActionListener onSelect = null;
    private ActionListener onCancel = null;
    private JList<Color> swatches = null;
    private JScrollPane swatchesScrollPane = null;
    private int showRows = 2;
    private int horizGap = 3;
    private int vertGap = 3;
    private Dimension swatchSize = new Dimension (20, 20);
    private int borderWidth = 1;
    private String userPropName = null;

    public QColorChooser (Color  initial)
    {

        super (BoxLayout.Y_AXIS);

        final QColorChooser _this = this;

        this.rs = new JSlider (0,
                               255);

        this.gs = new JSlider (0,
                               255);

        this.bs = new JSlider (0,
                               255);

        this.initialColor = initial;

        Set<Color> cols = this.getSwatchColors ();

        this.swatchesScrollPane = new JScrollPane ();

        this.setSwatches (cols);

        final Dimension sSize = new Dimension (this.swatchSize.width + (2 * this.borderWidth) + (2 * this.horizGap),
                                               this.swatchSize.height + (2 * this.borderWidth) + (2 * this.vertGap));

        this.swatchesScrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.swatchesScrollPane.getVerticalScrollBar ().setUnitIncrement (this.swatchSize.height + (2 * this.borderWidth) + (this.vertGap * 2));
        this.swatchesScrollPane.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        this.swatchesScrollPane.setOpaque (false);

        this.swatchesScrollPane.getViewport ().setPreferredSize (new Dimension (8 * sSize.width,
                                                                                this.showRows * sSize.height));

        this.swatchesScrollPane.setBorder (null);

        List<String> prefix = Arrays.asList (colorchooser,labels);

        FormLayout rgbfl = new FormLayout ("right:p, 6px, 150px:grow, 3px, 48px",
                                           "p, 6px, p, 6px, p, 6px, p, 10px, top:p");

        int rr = 1;
        int gr = 3;
        int br = 5;
        int hr = 7;
        int cr = 9;
        int butr = 11;

        PanelBuilder rgbbuilder = new PanelBuilder (rgbfl);

        CellConstraints cc = new CellConstraints ();

        rgbbuilder.addLabel (getUIString (prefix,hex),
                             //"HEX",
                             cc.xy (1,
                                    hr));

        this.rgb = new JTextField ();

        this.rgb.setPreferredSize (new Dimension (80, this.rgb.getPreferredSize ().height));
        this.rgb.setMaximumSize (this.rgb.getPreferredSize ());

        this.rgb.getDocument ().addDocumentListener (new DocumentAdapter ()
        {

            @Override
            public void insertUpdate (DocumentEvent ev)
            {

                String t = rgb.getText ().trim ();

                if (!t.startsWith ("#"))
                {

                    t = "#" + t;

                }

                if (t.length () < 7)
                {

                    return;

                }

                t = t.toUpperCase ();

                Color c = null;

                try
                {

                    c = Color.decode (t);

                } catch (Exception e) {

                    return;

                }

                _this.update (c,
                              false);

            }

        });

        Box brgb = new Box (BoxLayout.X_AXIS);
        brgb.add (this.rgb);
        brgb.add (Box.createHorizontalGlue ());

        rgbbuilder.add (brgb,
                        cc.xy (3,
                               hr));

        rgbbuilder.addLabel (Environment.getUIString (prefix,
                                                      LanguageStrings.red),
                             //"Red",
                             cc.xy (1,
                                    rr));

        rs.setOpaque (false);
        rgbbuilder.add (rs,
                        cc.xy (3,
                               rr));

        SpinnerNumberModel rm = new SpinnerNumberModel (initial.getRed (),
                                                         0,
                                                         255,
                                                         1);

        this.rsp = new JSpinner (rm);

        rgbbuilder.add (this.rsp,
                        cc.xy (5, rr));

        rgbbuilder.addLabel (Environment.getUIString (prefix,
                                                      LanguageStrings.green),
                                                      //"Green",
                             cc.xy (1,
                                    gr));

        this.gs.setOpaque (false);
        rgbbuilder.add (this.gs,
                        cc.xy (3,
                               gr));

        SpinnerNumberModel gm = new SpinnerNumberModel (initial.getGreen (),
                                                         0,
                                                         255,
                                                         1);

        this.gsp = new JSpinner (gm);

        rgbbuilder.add (this.gsp,
                        cc.xy (5, gr));

        rgbbuilder.addLabel (Environment.getUIString (prefix,
                                                      LanguageStrings.blue),
                             //"Blue",
                             cc.xy (1,
                                    br));

        bs.setOpaque (false);

        rgbbuilder.add (bs,
                        cc.xy (3,
                               br));

        SpinnerNumberModel bm = new SpinnerNumberModel (initial.getBlue (),
                                                         0,
                                                         255,
                                                         1);

        this.bsp = new JSpinner (bm);

        rgbbuilder.add (this.bsp,
                        cc.xy (5, br));

        rgbbuilder.addLabel (Environment.getUIString (prefix,
                                                      LanguageStrings.color),
                             //"Color",
                             cc.xy (1,
                                    cr));

        rgbbuilder.add (this.swatchesScrollPane,
                        cc.xywh (3,
                                 cr,
                                 3,
                                 1));

        ChangeListener vcl = new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                Color c = new Color (((Number) _this.rsp.getValue ()).intValue (),
                                     ((Number) _this.gsp.getValue ()).intValue (),
                                     ((Number) _this.bsp.getValue ()).intValue ());

                _this.update (c,
                              true);

            }

        };

        this.rsp.addChangeListener (vcl);
        this.gsp.addChangeListener (vcl);
        this.bsp.addChangeListener (vcl);

        ChangeListener cl = new ChangeAdapter ()
        {

            public void stateChanged (ChangeEvent ev)
            {

                // Get the new color, then fire a state change.

                Color c = new Color (_this.rs.getValue (),
                                     _this.gs.getValue (),
                                     _this.bs.getValue ());

                _this.update (c,
                              true);
/*
                _this.fireChangeEvent (c,
                                       RGB_TYPE);
*/
            }

        };

        this.rs.addChangeListener (cl);
        this.gs.addChangeListener (cl);
        this.bs.addChangeListener (cl);

        this.setColor (initial);

        this.rgbPanel = rgbbuilder.getPanel ();
        this.rgbPanel.setOpaque (false);
        this.rgbPanel.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.add (this.rgbPanel);

        this.setOpaque (false);
        this.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        if (initial != null)
        {

            this.fireChangeEvent (initial,
                                  SWATCH_TYPE);

        }

    }

    public void setUserPropertyName (String n)
    {

        this.userPropName = n;

        if (n != null)
        {

            UserProperties.addListener (this);

        } else {

            UserProperties.removeListener (this);

        }

    }

    @Override
    public void propertyChanged (UserPropertyEvent ev)
    {

        if (ev.getName ().equals (this.userPropName))
        {

            try
            {

                this.updating = true;

                Color c = UIUtils.getColor (UserProperties.get (this.userPropName));

                this.currentColor = c;

                this.rs.setValue (this.currentColor.getRed ());
                this.gs.setValue (this.currentColor.getGreen ());
                this.bs.setValue (this.currentColor.getBlue ());
                this.rsp.setValue (this.currentColor.getRed ());
                this.gsp.setValue (this.currentColor.getGreen ());
                this.bsp.setValue (this.currentColor.getBlue ());

                this.rgb.setText (UIUtils.colorToHex (this.currentColor).toUpperCase ());

            } finally {

                this.updating = false;

            }

        }

    }

    public Color getCurrentColor ()
    {

        return this.currentColor;

    }

    public void setShowRows (int r)
    {

        this.showRows = r;

    }

    public void setHorizontalGap (int g)
    {

        this.horizGap = g;

    }

    public void setVerticalGap (int g)
    {

        this.vertGap = g;

    }

    public void setSwatchSize (Dimension d)
    {

        this.swatchSize = d;

    }

    public void setOnSelect (ActionListener l)
    {

        this.onSelect = l;

    }

    public void setOnCancel (ActionListener l)
    {

        this.onCancel = l;

    }

    public static JPanel getSwatch (Color     col)
    {

        return QColorChooser.getSwatch (col,
                                        new Dimension (25, 25),
                                        0,
                                        0);

    }

    public static JPanel getSwatch (Color     col,
                                    Dimension swatchSize,
                                    int       horizGap,
                                    int       vertGap)
    {

        Border ccBorder = new MatteBorder (1,
                                           1,
                                           1,
                                           1,
                                           UIUtils.getBorderColor ());



            ccBorder = new CompoundBorder (new MatteBorder (vertGap,
                                                            horizGap,
                                                            vertGap,
                                                            horizGap,
                                                            UIUtils.getComponentColor ()),
                                           UIUtils.createLineBorder ());

        Dimension s = new Dimension (swatchSize.width + (horizGap * 2) + 2,
                                     swatchSize.height + (vertGap * 2) + 2);

        JPanel c = new JPanel ();
        c.setSize (s.width,
                   s.height);
        c.setMinimumSize (s);
        c.setMaximumSize (s);
        c.setPreferredSize (c.getMinimumSize ());
        c.setBorder (ccBorder);
        c.setBackground (col);
        //UIUtils.setAsButton (c);

        return c;

    }

    public void resetToInitial ()
    {

        this.setColor (this.initialColor);

    }

    // TODO: Use a better lock.
    private synchronized void update (Color   c,
                                      boolean updateRGB)
    {

        if (this.updating)
        {

            return;

        }

        this.updating = true;

        this.currentColor = c;

        this.rs.setValue (this.currentColor.getRed ());
        this.gs.setValue (this.currentColor.getGreen ());
        this.bs.setValue (this.currentColor.getBlue ());
        this.rsp.setValue (this.currentColor.getRed ());
        this.gsp.setValue (this.currentColor.getGreen ());
        this.bsp.setValue (this.currentColor.getBlue ());

        if (updateRGB)
        {

            this.rgb.setText (UIUtils.colorToHex (this.currentColor).toUpperCase ());

        }

        this.fireChangeEvent (c,
                              RGB_TYPE);

        this.updating = false;

    }

    public void setColor (final Color   c)
    {

        this.update (c,
                     true);

    }

    public Color getColor ()
    {

        return this.currentColor;

    }

    public void removeChangeListener (ChangeListener l)
    {

        this.changeListeners.remove (l);

    }

    public void addChangeListener (ChangeListener l)
    {

        this.changeListeners.add (l);

    }

    public void setSwatches (Set<Color> colors)
    {

        final QColorChooser _this = this;

        DefaultListModel<Color> m = new DefaultListModel ();

        for (Color c : colors)
        {

            m.addElement (c);

        }

        this.swatches = new JList ();
        this.swatches.setModel (m);
        this.swatches.setLayoutOrientation (JList.HORIZONTAL_WRAP);
        this.swatches.setVisibleRowCount (0);
        this.swatches.setOpaque (true);
        this.swatches.setBackground (UIUtils.getComponentColor ());

        UIUtils.setAsButton (this.swatches);

        this.swatches.setCellRenderer (new DefaultListCellRenderer ()
        {

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                return _this.getSwatch ((Color) value,
                                        swatchSize,
                                        horizGap,
                                        vertGap);

            }

        });

        this.swatches.addListSelectionListener (new ListSelectionListener ()
        {

            @Override
            public void valueChanged (ListSelectionEvent ev)
            {

                if (ev.getValueIsAdjusting ())
                {

                    return;

                }

                Color c = (Color) _this.swatches.getSelectedValue ();

                _this.update (c,
                              true);

            }

        });

        this.swatches.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.swatches.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                final int ind = _this.swatches.locationToIndex (ev.getPoint ());

                if (ind >= 0)
                {

                    Color c = _this.swatches.getModel ().getElementAt (ind);

                    if ((c != Color.white)
                        &&
                        (c != Color.black)
                       )
                    {

                        JMenuItem mi = null;

                        mi = new JMenuItem (getUIString (colorchooser,swatch,popupmenu,items,remove),
                                            //"Remove",
                                            Environment.getIcon (Constants.DELETE_ICON_NAME,
                                                                 Constants.ICON_MENU));
                        mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                // Remove the file.
                                DefaultListModel<Color> model = (DefaultListModel<Color>) _this.swatches.getModel ();
                                Object o = model.remove (ind);

                                Set<Color> ncols = new LinkedHashSet (Collections.list (model.elements ()));

                                _this.setSwatchColors (ncols);

                            }

                        });

                        m.add (mi);

                    }

                }

            }

        });

        this.swatchesScrollPane.setViewportView (this.swatches);

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

    public static QPopup getColorChooserPopup (final String         title,
                                               final Color          initialColor,
                                               final String         userPropName,
                                               final ChangeListener cl,
                                               final ActionListener closeListener)
    {

        final QColorChooser cc = new QColorChooser (initialColor);

        cc.setUserPropertyName (userPropName);

        final QPopup qp = UIUtils.createClosablePopup ((title != null ? title : getUIString (colorchooser,popup,title)),
                                                        //"Select a color"),
                                                       null,
                                                       closeListener);

        cc.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        if (cl != null)
        {

            cc.addChangeListener (cl);

        }

        Box content = new Box (BoxLayout.Y_AXIS);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        cc.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (cc);

        JButton f = UIUtils.createButton (getUIString (buttons,finish),
                                          new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                cc.addSwatchColor (cc.getCurrentColor ());

                qp.removeFromParent ();

            }

        });

        JButton r = UIUtils.createButton (getUIString (colorchooser,popup,reset,text),
                                          //"Reset",
                                          new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                cc.resetToInitial ();

            }

        });

        r.setToolTipText (getUIString (colorchooser,popup,reset,tooltip));
                          //"Reset to the initial color.");

        JButton[] _buts = { f, r };

        JComponent bs = UIUtils.createButtonBar2 (_buts,
                                                  Component.CENTER_ALIGNMENT);

        bs.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (bs);

        qp.setContent (content);

        return qp;

    }

    public void addSwatchColor (Color c)
    {

        if (!this.getSwatchColors ().contains (c))
        {

            DefaultListModel<Color> model = (DefaultListModel<Color>) this.swatches.getModel ();

            if (model.getSize () < 2)
            {

                model.addElement (c);

            } else {

                Enumeration<Color> els = model.elements ();

                int ind = 0;
                int bind = -1;
                int wind = -1;
                int s = model.getSize ();

                while (els.hasMoreElements ())
                {

                    Color _c = els.nextElement ();

                    if (_c == Color.black)
                    {

                        bind = ind;

                    }

                    if (_c == Color.white)
                    {

                        wind = ind;

                    }

                    ind++;

                }

                if (bind > 0)
                {

                    if (s > bind)
                    {

                        model.add (bind + 1,
                                   c);

                    } else {

                        model.addElement (c);

                    }

                }

                if ((bind < 0)
                    &&
                    (wind > 0)
                   )
                {

                    if (s > wind)
                    {

                        model.add (wind + 1,
                                   c);

                    } else {

                        model.addElement (c);

                    }

                }

                if ((bind < 0)
                    &&
                    (wind < 0)
                   )
                {

                    model.add (0,
                               c);

                }

            }

            Set<Color> ncols = new LinkedHashSet (Collections.list (model.elements ()));

            this.setSwatchColors (ncols);

        }

    }

    public Set<Color> getSwatchColors ()
    {

        String colors = UserProperties.get (Constants.COLOR_SWATCHES_PROPERTY_NAME);

        StringTokenizer t = new StringTokenizer (colors,
                                                 ",");

        Set<Color> cols = new LinkedHashSet ();

        cols.add (Color.white);
        cols.add (Color.black);

        while (t.hasMoreTokens ())
        {

            cols.add (UIUtils.getColor (t.nextToken ().trim ()));

        }

        return cols;

    }

    private void setSwatchColors (Set<Color> colors)
    {

        StringBuilder b = new StringBuilder ();

        if (colors == null)
        {

            return;

        }

        Iterator<Color> iter = colors.iterator ();

        while (iter.hasNext ())
        {

            Color col = iter.next ();

            String c = UIUtils.colorToHex (col).toUpperCase ();

            if ((c.equals ("#000000"))
                ||
                (c.equals ("#FFFFFF"))
               )
            {

                continue;

            }

            b.append (c);

            if (iter.hasNext ())
            {

                b.append (",");

            }

        }

        UserProperties.set (Constants.COLOR_SWATCHES_PROPERTY_NAME,
                            b.toString ());

    }

}
