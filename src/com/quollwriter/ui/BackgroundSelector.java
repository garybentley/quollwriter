package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.*;

import java.awt.image.*;

import java.util.*;

import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ActionAdapter;

public class BackgroundSelector extends Box implements ListSelectionListener
{

    private static int BORDER_WIDTH = 3;
    private static int SHOW_ROWS = 4;

    private List<ChangeListener> changeListeners = new ArrayList ();
    private JList                list = null;
    private List<File>           files = new ArrayList ();
    private File                 selectedFile = null;

    private Map<File, JComponent> imgCache = new HashMap ();
    
    private Object origSelection = null;

    public BackgroundSelector(Dimension swatchSize,
                              Object    selected)
    {

        super (BoxLayout.Y_AXIS);

        final Dimension sSize = new Dimension (swatchSize.width + (2 * BORDER_WIDTH) + 2,
                                               swatchSize.height + (2 * BORDER_WIDTH) + 2);

        if (selected != null)
        {                                               
        
            this.origSelection = selected;
            
        }

        DefaultListModel lm = new DefaultListModel ();

        this.initFiles ();
                
        Set<BackgroundImage> bgImages = null;
        
        try
        {
            
            bgImages = Environment.getBackgroundImages ();
            
        } catch (Exception e)
        {

        }

        List<BackgroundImage> _bgImages = new ArrayList (bgImages);
        
        Collections.sort (_bgImages);
        
        bgImages = new LinkedHashSet (_bgImages);

        lm.addElement ("add");
        
        lm.addElement ("none");
                
        if (this.origSelection != null)
        {

            lm.addElement (this.origSelection);

        }

        for (File f : this.files)
        {
            
            if (f.equals (this.origSelection))
            {
                
                this.selectedFile = f;
                
                continue;
                
            }
            
            lm.addElement (f);
            
        }
        
        for (BackgroundImage bi : bgImages)
        {

            if (bi.equals (this.origSelection))
            {

                continue;

            }

            lm.addElement (bi);

        }

        String colors = Environment.getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);

        colors = "#ffffff," + colors;
        colors += ",#000000";

        StringTokenizer t = new StringTokenizer (colors,
                                                 ",");

        List<Color> cols = new ArrayList ();

        while (t.hasMoreTokens ())
        {

            lm.addElement (UIUtils.getColor (t.nextToken ().trim ()));

        }

        final BackgroundSelector _this = this;

        int rows = lm.getSize () / SHOW_ROWS;

        if ((rows % SHOW_ROWS) != 0)
        {

            rows++;

        }

        this.list = new JList (lm);
        this.list.setLayoutOrientation (JList.HORIZONTAL_WRAP);
        this.list.setVisibleRowCount (rows);
        this.list.addListSelectionListener (this);
        this.list.setOpaque (true);
        this.list.setBackground (Color.white);

        this.list.addMouseListener (new MouseAdapter ()
        {
           
            private void handle (MouseEvent ev)
            {

                if (ev.isPopupTrigger ())
                {
                    
                    final int ind = _this.list.locationToIndex (ev.getPoint ());
                    
                    if (ind >= 0)
                    {
                        
                        Object o = _this.list.getModel ().getElementAt (ind);
                        
                        if (o instanceof File)
                        {
                            
                            // Show a menu.
                            JPopupMenu m = new JPopupMenu ();
                            
                            JMenuItem mi = null;
    
                            mi = new JMenuItem ("Remove this image",
                                                Environment.getIcon (Constants.DELETE_ICON_NAME,
                                                                     Constants.ICON_MENU));
                            mi.addActionListener (new ActionAdapter ()
                            {
                               
                                public void actionPerformed (ActionEvent ev)
                                {

                                    // Remove the file.                               
                                    Object o = ((DefaultListModel) _this.list.getModel ()).remove (ind);
                               
                                    _this.files.remove (o);
                               
                                    _this.updateFiles ();
                               
                                }
                                
                            });
                            
                            m.add (mi);

                            m.show ((Component) ev.getSource (),
                                    ev.getX (),
                                    ev.getY ());                            
                            
                        }
                        
                    }
                    
                }
                
            }
           
            public void mouseReleased (MouseEvent ev)
            {
                
                handle (ev);
                
            }
           
            public void mousePressed (MouseEvent ev)
            {

                handle (ev);
                
            }
            
        });
        
        this.list.setCellRenderer (new DefaultListCellRenderer ()
        {

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                return _this.getSwatch (value,
                                        sSize,
                                        isSelected);

            }

        });

        JScrollPane sp = new JScrollPane (this.list);

        // sp.getViewport ().add (this.list);
        sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.getViewport ().setPreferredSize (new Dimension ((SHOW_ROWS * sSize.width),
                                                           (SHOW_ROWS * sSize.height)));
        sp.setBorder (null);

        this.add (sp);

    }

    private void initFiles ()
    {
        
        try
        {
        
            String bgFiles = Environment.getUserProperties ().getProperty (Constants.BG_IMAGE_FILES_PROPERTY_NAME);
            
            if (bgFiles == null)
            {
                
                return;
                
            }
            
            // Will be xml.
            Element root = JDOMUtils.getStringAsElement (bgFiles);
            
            List els = JDOMUtils.getChildElements (root,
                                                   "f",
                                                   false);
            
            for (int i = 0; i < els.size (); i++)
            {
                
                Element el = (Element) els.get (i);
                
                File f = new File (JDOMUtils.getChildContent (el));
                
                if ((!f.exists ())
                    ||
                    (!f.isFile ())
                   )
                {
                    
                    continue;
                    
                }
                
                this.files.add (f);
                
            }

        } catch (Exception e) {
            
            Environment.logError ("Unable to load background image files",
                                  e);
            
        }
        
    }
    
    private void updateFiles ()
    {
        
        try
        {
        
            Element root = new Element ("files");
    
            for (File f : this.files)
            {
                
                Element el = new Element ("f");
                el.addContent (f.getPath ());
                
                root.addContent (el);
                
            }
            
            // Get as a string.
            String data = JDOMUtils.getElementAsString (root);
            
            Environment.setUserProperty (Constants.BG_IMAGE_FILES_PROPERTY_NAME,
                                         data);

        } catch (Exception e) {
            
            Environment.logError ("Unable to update background image files",
                                  e);            
                
        }
        
    }
    
    public void setSelected (Object obj)
    {

        this.list.setSelectedValue (obj,
                                    true);

        this.list.validate ();
        this.list.repaint ();
        this.validate ();
        this.repaint ();

    }

    public void valueChanged (ListSelectionEvent ev)
    {

        if (this.list.getSelectedValue ().toString ().equals ("add"))
        {
            
            JFileChooser fc = new JFileChooser ();//f);
            FileNameExtensionFilter fil = new FileNameExtensionFilter ("Image files: jpg, png, gif",
                                                                       "jpg",
                                                                       "jpeg",
                                                                       "gif",
                                                                       "png");
            fc.setFileFilter (fil);
            
            if (this.selectedFile != null)
            {
                
                fc.setSelectedFile (this.selectedFile.getParentFile ());
                
            }
            
            if (fc.showOpenDialog (this) == JFileChooser.APPROVE_OPTION)
            {
                
                File f = fc.getSelectedFile ();
                
                this.fireChangeEvent (f);
                
                ((DefaultListModel) this.list.getModel ()).add (2, f);
                
                this.files.add (f);
                
                this.updateFiles ();
                
                this.selectedFile = f;
                
            }                        

            return;
            
        }
            
    
        this.fireChangeEvent (this.list.getSelectedValue ());

    }

    public JComponent getSwatch (final Object v,
                                 Dimension    swatchSize,
                                 boolean      isSelected)
    {

        final BackgroundSelector _this = this;    
    
        JComponent c = null;
        
        if (v instanceof JComponent)
        {

            c = (JComponent) v;
        
            c.setSize (swatchSize);
            c.setMinimumSize (swatchSize);
            c.setMaximumSize (swatchSize);
            c.setPreferredSize (c.getMinimumSize ());
                                    
        }

        if (v instanceof File)
        {
            
            File f = (File) v;
            
            if (!f.exists ())
            {
                
                return null;
                
            }
            
            c = this.imgCache.get (f);
            
            if (c == null)
            {
            
                Image i = UIUtils.getImage (f);
                
                c = new ImagePanel (UIUtils.getScaledImage (i,
                                                            75,
                                                            75,
                                                            this),
                                    null);
    
                c.setToolTipText ("Click to select this background");
                                             
                this.imgCache.put (f,
                                   c);
                                             
            }
                        
        }
        
        if (v instanceof String)
        {
        
            String vv = v.toString ();
        
            if (vv.equals ("own"))
            {
    
                c = new JPanel ();
                c.setBackground (new Color (0,
                                            0,
                                            0,
                                            0));
                c.setToolTipText ("Click to select no background");
        
            }
            
            if (vv.equals ("none"))
            {
    
                JLabel l = new JLabel ("Clear",
                                Environment.getIcon (Constants.CLEAR_ICON_NAME,
                                                     Constants.ICON_BG_SWATCH),
                                SwingConstants.CENTER);

                l.setVerticalTextPosition(SwingConstants.BOTTOM);
                l.setHorizontalTextPosition(SwingConstants.CENTER);
                Box box = new Box(BoxLayout.Y_AXIS);
                box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                box.add(Box.createVerticalGlue());
                box.add(l);
                box.add(Box.createVerticalGlue());

                c = box;
                
                l.setMaximumSize (new Dimension (75, 75));
                                
                c.setToolTipText ("Click to select no background");                
        
            }

            if (vv.equals ("add"))
            {
    
                JLabel l = new JLabel ("Add Image",
                                Environment.getIcon ("add",
                                                         Constants.ICON_BG_SWATCH),
                                SwingConstants.CENTER);

                l.setVerticalTextPosition(SwingConstants.BOTTOM);
                l.setHorizontalTextPosition(SwingConstants.CENTER);

                Box box = new Box(BoxLayout.Y_AXIS);
                box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                box.add(Box.createVerticalGlue());
                box.add(l);
                box.add(Box.createVerticalGlue());

                c = box;
                
                l.setMaximumSize (new Dimension (75, 75));
                                
                c.setToolTipText ("Click to select no background");                
    
            }
            
        }
        
        if (v instanceof Color)
        {
    
            c = new JPanel ();
            c.setBackground ((Color) v);

            c.setToolTipText ("Click to select this color as the background");
    
        }
        
        if (v instanceof BackgroundImage)
        { 
    
            c = new ImagePanel (((BackgroundImage) v).getThumb (),
                                null);
    
            c.setToolTipText ("Click to select this background");
    
        }
 
        UIUtils.setAsButton (c);
        c.setOpaque (true);
        c.setSize (swatchSize);
        c.setMinimumSize (swatchSize);
        c.setMaximumSize (swatchSize);
        c.setPreferredSize (c.getMinimumSize ());

        Border ccBorder = null;

        Color col = Color.white;
        int   w = BORDER_WIDTH;

        if (isSelected)
        {

            col = UIUtils.getColor ("#D38230"); //Environment.getBorderColor (); // 
            w = BORDER_WIDTH + 1;

            ccBorder = new MatteBorder (w,
                                        w,
                                        w,
                                        w,
                                        col);

        } else
        {
            
            ccBorder = new CompoundBorder (new MatteBorder (w,
                                                            w,
                                                            w,
                                                            w,
                                                            col),
                                           UIUtils.createLineBorder ());

        }

        c.setBorder (ccBorder);

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

    protected void fireChangeEvent (Object val)
    {

        String v = null;
        
        if (val instanceof String)
        {
            
            v = val.toString ();
            
            if (!v.startsWith ("#"))
            {
                
                v = "bg:" + v;
                
            }
            
        }
    
        BackgroundChangeEvent ev = new BackgroundChangeEvent (this,
                                                              val);

        for (ChangeListener cl : this.changeListeners)
        {

            cl.stateChanged (ev);

        }

    }

    public void resetToDefault ()
    {

        if (this.origSelection != null)
        {

            this.setSelected (this.origSelection);

        }

    }

    public static QPopup getBackgroundSelectorPopup (ChangeListener cl,
                                                     Dimension      swatchSize,
                                                     Object         selected)
    {

        final QPopup qp = new QPopup ("Select a background image/color",
                                      Environment.getIcon ("bg-select",
                                                           Constants.ICON_POPUP),
                                      null);

        final BackgroundSelector bs = new BackgroundSelector (swatchSize,
                                                              selected);

        List<JComponent> buts = new ArrayList ();
/*
        JComponent undo = new JButton (Environment.getIcon ("undo",
                                                            Constants.ICON_MENU));
        // null);

        buts.add (undo);
        undo.setToolTipText ("Reset the background");
*/
        JComponent cancel = new JButton (Environment.getIcon ("cancel",
                                                              Constants.ICON_MENU));
        // null);

        buts.add (cancel);

        JToolBar controls = UIUtils.createButtonBar (buts);

        qp.getHeader ().setControls (controls);
        bs.setOpaque (false);
/*
        undo.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    bs.resetToDefault ();

                }

            });
*/
        cancel.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    qp.setVisible (false);

                }

            });

        bs.setBorder (new EmptyBorder (2,
                                       2,
                                       2,
                                       0));

        if (cl != null)
        {

            bs.addChangeListener (cl);

        }

        qp.setContent (bs);

        bs.setSelected (selected);

        return qp;

    }

}


