package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.io.*;
import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

//import com.jhlabs.image.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.MultiImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.TextStylable;
import com.quollwriter.ui.components.TextProperties;
import com.quollwriter.ui.components.Runner;

public class FullScreenFrame extends JFrame implements PopupsSupported, SideBarListener
{

    public static final float DEFAULT_X_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_Y_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_BORDER_OPACITY = 0.7f;

    public static final float MAX_BORDER_WIDTH = 0.25f;
    public static final float MIN_BORDER_WIDTH = 0.015f;

    private FullScreenQuollPanel panel = null;
    private Border               childBorder = null;
    private float                xBorderWidth = -1f;
    private float                yBorderWidth = -1f;
    private float                borderOpacity = -1f;
    private int                  lastX = -1;
    private int                  lastY = -1;
    private int                  expandBorderWidth = 20;
    private MouseAdapter         mouseListener = null;
    private JSlider              backgroundOpacity = null;
    private int                  sizeBoxMaxWidth = 150;
    private ImagePanel           sizeBox = null;
    private ActionListener       closeAction = null;
    private ActionListener       showHeaderAction = null;
    private ActionListener       showPropertiesAction = null;
    private ActionListener       findAction = null;
    private Header               title = null;
    private JComponent           header = null;
    private Timer                clockTimer = null;
    private JLabel               clockLabel = null;
    private SimpleDateFormat     clockFormat = null;
    private JComponent           bgImagePanel = null;
    private JComponent           sideBar = null;
    //private JComponent           properties = null;
    private int                  minimumSideBarWidth = 0;
    private boolean              noHideSideBar = false;
    private TextProperties       normalTextProperties = null;
    private FullScreenTextProperties       fullScreenTextProperties = null;
    private Map<String, QPopup>  popups = new HashMap ();
    private Timer                headerHideTimer = null;
    private Paint                background = null;
    private QPopup               backgroundSelectorPopup = null;
    private Object               backgroundObject = null;
    private FullScreenPropertiesSideBar properties = null;
    private AbstractProjectViewer projectViewer = null;
    private BufferedImage         noImageBackground = null;
    
    public FullScreenFrame (FullScreenQuollPanel qp)
    {

        this.panel = qp;

        this.projectViewer = this.panel.getProjectViewer ();
        
        //com.gentlyweb.properties.Properties props = Environment.getUserProperties ();
        
        Project proj = this.projectViewer.getProject ();

        //this.projectViewer.setVisible (false);
        
        this.fullScreenTextProperties = new FullScreenTextProperties (this);
        
        this.xBorderWidth = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME);

        if (this.xBorderWidth <= 0)
        {

            this.xBorderWidth = DEFAULT_X_BORDER_WIDTH;

        }

        this.yBorderWidth = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME);

        if (this.yBorderWidth <= 0)
        {

            this.yBorderWidth = DEFAULT_Y_BORDER_WIDTH;

        }

        this.borderOpacity = proj.getPropertyAsFloat (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME);

        if (this.borderOpacity <= 0)
        {

            this.borderOpacity = DEFAULT_BORDER_OPACITY;

        }

        final FullScreenFrame _this = this;

        this.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    _this.close ();

                }

            });

        this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);

        this.setResizable (false);
        this.setUndecorated (true);
        
    }

    public void showBlankPanel ()
    {
        
        BlankQuollPanel bqp = new BlankQuollPanel (this.projectViewer);
        
        bqp.init ();
        
        FullScreenQuollPanel bp = new FullScreenQuollPanel (bqp);
        
        this.showSideBar (Constants.LEFT);
        
        this.switchTo (bp);
        
    }
    
    public void sideBarShown (SideBarEvent ev)
    {
        
        if (!this.projectViewer.isMainSideBarName (ev.getName ()))
        {
            
            //this.noHideSideBar = true;
        
            this.showSideBar (Constants.LEFT);
            
        } else {
            
            //this.noHideSideBar = false;
            
            this.hideSideBar ();
            
        }
                
    }
        
    public void close ()
    {

        // Set the properties.
        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();

        try
        {

            FloatProperty fp = new FloatProperty (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME,
                                                  this.getXBorderWidth ());
            fp.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME,
                               fp);

            fp = new FloatProperty (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME,
                                    this.getYBorderWidth ());
            fp.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME,
                               fp);

            fp = new FloatProperty (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME,
                                    this.getBorderOpacity ());
            fp.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME,
                               fp);

                               
            /*              
            try
            {

                IntegerProperty sizeP = new IntegerProperty (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                                                             this.fullScreenTextProperties.getFontSize ());
                sizeP.setDescription ("N/A");

                props.setProperty (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                                   sizeP);

            } catch (Exception e)
            {

                // Ignore.

            }
            
            StringProperty alignP = new StringProperty (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                                                        this.fullScreenTextProperties.getAlignment ());
            alignP.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                               alignP);

            StringProperty fontP = new StringProperty (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                                                       this.fullScreenTextProperties.getFontFamily ());
            fontP.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                               fontP);

            try
            {

                FloatProperty lspaceP = new FloatProperty (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                                                           this.fullScreenTextProperties.getLineSpacing ());
                lspaceP.setDescription ("N/A");

                props.setProperty (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                                   lspaceP);

            } catch (Exception e)
            {

                // Ignore.

            }

            StringProperty fcp = new StringProperty (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                                                     UIUtils.colorToHex (this.fullScreenTextProperties.getTextColor ()));

            fcp.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                               fcp);

            StringProperty bcp = new StringProperty (Constants.FULL_SCREEN_EDITOR_FONT_BGCOLOR_PROPERTY_NAME,
                                                     UIUtils.colorToHex (this.fullScreenTextProperties.getBackgroundColor ()));
            bcp.setDescription ("N/A");

            props.setProperty (Constants.FULL_SCREEN_EDITOR_FONT_BGCOLOR_PROPERTY_NAME,
                               bcp);
*/
            Environment.saveUserProperties (props);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user properties",
                                  e);

        }            
    
        try
        {

            this.projectViewer.getProject ().getProperties ().setProperty (Constants.FULL_SCREEN_SIDEBAR_WIDTH_PROPERTY_NAME,
                                                                           new IntegerProperty (Constants.FULL_SCREEN_SIDEBAR_WIDTH_PROPERTY_NAME,
                                                                                                this.sideBar.getPreferredSize ().width));

            this.projectViewer.saveProject ();
                                                                                                            
        } catch (Exception e)
        {

            // Ignore.

        }
    
        this.projectViewer.removeMainPanelListener (this.properties);
    
        this.projectViewer.removeSideBarListener (this);
    
        this.projectViewer.removeSideBar (this.properties);
    
        this.clockTimer.stop ();
        
        this.restorePanel ();
 
        this.setVisible (false);
        this.dispose ();
        
        this.projectViewer.fullScreenClosed (this.panel.getChild ());

        this.projectViewer.fireProjectEventLater (ProjectEvent.FULL_SCREEN,
                                                  ProjectEvent.EXIT);
                                                  
    }

    public void removePopup (Component c)
    {

        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

    }

    public void addPopup (Component c,
                          boolean   hideOnClick)
    {

        this.addPopup (c,
                       hideOnClick,
                       false);

    }

    public void addPopup (final Component c,
                          boolean         hideOnClick,
                          boolean         hideViaVisibility)
    {

        this.getLayeredPane ().add (c,
                                    JLayeredPane.POPUP_LAYER);

        this.getLayeredPane ().moveToFront (c);

/*
        this.content.getLayeredPane ().add (c,
                             JLayeredPane.POPUP_LAYER);

        this.content.getLayeredPane ().moveToFront (c);
*/
        if (hideOnClick)
        {

            final FullScreenFrame _this = this;
            final boolean         hideVia = hideViaVisibility;

            // Need to do it this way because mouse events aren't being forwarded/delivered.
            MouseAdapter m = new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    c.setVisible (false);

                    if (hideVia)
                    {

                        c.setVisible (false);

                    } else
                    {

                        _this.removePopup (c);

                        java.util.List<Component> comps = _this.getTopLevelComponents ();

                        for (int i = 0; i < comps.size (); i++)
                        {

                            comps.get (i).removeMouseListener (this);

                        }

                    }

                }

            };

            java.util.List<Component> comps = this.getTopLevelComponents ();

            for (int i = 0; i < comps.size (); i++)
            {

                comps.get (i).addMouseListener (m);

            }

        }

    }

    private java.util.List getTopLevelComponents ()
    {

        java.util.List l = new ArrayList ();
        l.add (this.header);
        l.add (this);

        return l;

    }

    public void showPopupAt (Component popup,
                             Component showAt)
    {

        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po);


    }

    public void showPopupAt (Component c,
                             Point     p)
    {

        Insets ins = this.getInsets ();

        // p.x -= ins.right;
        // p.y -= ins.top;
        try
        {

            if (c.getParent () == null)
            {

                this.addPopup (c,
                               true,
                               false);

            }
        } catch (Exception e)
        {

            return;
        }

        Dimension cp = c.getPreferredSize ();

        if ((p.y + cp.height) > (this.getBounds ().height - ins.top - ins.bottom))
        {

            p = new Point (p.x,
                           p.y);

            p.y = p.y - cp.height;

        }

        if (p.y < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.y = 10;

        }

        if ((p.x + cp.width) > (this.getBounds ().width - ins.left - ins.right))
        {

            p = new Point (p.x,
                           p.y);

            p.x = p.x - cp.width;

        }

        if (p.x < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.x = 10;

        }

/*
        Dimension s = c.getPreferredSize ();

        if (p.y < 0)
        {

            p.y = p.y + s.height;

        }

        Dimension t = this.getPreferredSize ();

        if ((p.x + s.width) > (t.width))
        {

            p.x = t.width - 20 - s.width;

        }

        if (p.x < 0)
        {

            p.x = 20;

        }
*/

        c.setBounds (p.x,
                     p.y,
                     c.getPreferredSize ().width,
                     c.getPreferredSize ().height);

        c.setVisible (true);
        this.validate ();
        this.repaint ();

    }

    public void restorePanel ()
    {

        QuollPanel child = this.panel.getChild ();

        child.removeMouseWheelListener (this.mouseListener);
        child.removeMouseListener (this.mouseListener);
        child.removeMouseMotionListener (this.mouseListener);
        
        java.util.List<Component> cs = this.panel.getTopLevelComponents ();

        if (cs != null)
        {
            
            cs = new java.util.ArrayList (cs);
            
        } else {
            
            cs = new java.util.ArrayList ();
            
        }
        
        InputMap im = null;

        for (Component c : cs)
        {

            im = ((JComponent) c).getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

            im.remove (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                               0));

            im.remove (KeyStroke.getKeyStroke (KeyEvent.VK_F9,
                                               0));

            im.remove (KeyStroke.getKeyStroke (KeyEvent.VK_F6,
                                               0));

        }

        if (child instanceof AbstractEditorPanel)
        {
        
            Project proj = this.projectViewer.getProject ();
    
            AbstractEditorPanel edPanel = (AbstractEditorPanel) child;
    
            QTextEditor ed = edPanel.getEditor ();//editor;
            
            edPanel.setIgnoreDocumentChanges (true);
    
            edPanel.initEditor (this.normalTextProperties);
            
            this.normalTextProperties = null;

            edPanel.restoreBackgroundColor ();
    
            edPanel.restoreFontColor ();
    
            edPanel.setIgnoreDocumentChanges (false);

        }
            
        // Restore to the original parent.
        this.panel.restore ();

        this.getLayeredPane ().remove (child);

        this.projectViewer.restoreFromFullScreen (this.panel);

    }

    public void switchTo (FullScreenQuollPanel fs)
    {

        this.restorePanel ();

        this.panel = fs;

        this.initPanel ();
        this.validate ();
        this.repaint ();

        this.showHeader ();
        
        final FullScreenFrame _this = this;
        
        this.headerHideTimer = new Timer (3000,
                                          new ActionAdapter ()
                                          {
                                               
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                               
                                              _this.hideHeader ();
                                              
                                            }
                                              
                                          });

        this.headerHideTimer.start ();
        
    }

    public void init ()
    {

        final FullScreenFrame _this = this;

        Robot r = null;

        try
        {

            r = new Robot ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to get robot?",
                                  e);

        }                                                            
                                                            
        if (r != null)
        {

            BufferedImage bgImage = r.createScreenCapture (new Rectangle (Toolkit.getDefaultToolkit ().getScreenSize ()));

            _this.noImageBackground = bgImage;
            
        }
                    
        this.bgImagePanel = new JPanel ()
        {
        
            public void paintComponent (Graphics g)
            {
            
                Graphics2D g2d = (Graphics2D) g;

                if (_this.background != null)
                {

                    g2d.setPaint (background);
                    g2d.fill (g2d.getClip ());

                }

            }
            
        };
            
        Properties userProps = Environment.getUserProperties ();

        Object bgObj = null;
        
        String b = userProps.getProperty (Constants.FULL_SCREEN_BG_PROPERTY_NAME);
        
        if (b == null)
        {
            
            // Legacy < v2
            File f = userProps.getPropertyAsFile (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME);
        
            if ((f != null)
                &&
                (f.exists ())
                &&
                (f.isFile ())
               )
            {
                
                bgObj = f;

            }
            
        } else {
              
            bgObj = b;
              
        }

        _this.setFullScreenBackground (bgObj);
            
        this.getContentPane ().add (this.bgImagePanel);

        this.properties = new FullScreenPropertiesSideBar (this,
                                                           this.fullScreenTextProperties);
        
        this.projectViewer.addSideBar ("fullscreenproperties",
                                       this.properties);        
                
        this.projectViewer.addSideBarListener (this);
        
        this.createHeader ();
        
        // Get the left/right panels.
        this.createSideBar ();

        this.findAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Show the sidebar.  Then show the find.
                _this.projectViewer.showFind (null);
            
            }

        };

        this.closeAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.sideBar.isVisible ())
                {
                    
                    _this.hideSideBar ();
                    
                    return;
                    
                }
            
                _this.close ();

            }

        };

        this.showHeaderAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.header.isVisible ())
                {

                    _this.header.setVisible (false);

                    return;

                }

                _this.showHeader ();

            }

        };

        this.showPropertiesAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.showProperties ();
            
            }

        };
        
        this.mouseListener = new MouseAdapter ()
        {

            public void mouseWheelMoved (MouseWheelEvent ev)
            {

                if (_this.getChildBounds ().contains (ev.getPoint ()))
                {

                    return;

                }

                int r = ev.getWheelRotation ();

                _this.setBorderOpacity (_this.borderOpacity + (-1 * r * 0.1f));

                _this.properties.backgroundOpacityChanged ();
                
                _this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                                      ProjectEvent.CHANGE_BG_OPACITY);

            }

            private void handlePopup (MouseEvent ev)
            {
                
                if (_this.getChildBounds ().contains (ev.getPoint ()))
                {

                    return;

                }

                if (ev.isPopupTrigger ())
                {
                    
                    JPopupMenu p = new JPopupMenu ();

                    String oName = Environment.getObjectTypeName (Chapter.OBJECT_TYPE);
        
                    JMenuItem mi = new JMenuItem ("Edit full screen properties",
                                                  Environment.getIcon (Constants.SETTINGS_ICON_NAME,
                                                                       Constants.ICON_MENU));

                    mi.addActionListener (_this.showPropertiesAction);
                                                                       
                    p.add (mi);
                                                                       
                    Component c = (Component) ev.getSource ();

                    Point l = ev.getLocationOnScreen ();
                    
                    p.show (c,
                            l.x,
                            l.y);
                    
                }
                
            }
            
            public void mousePressed (MouseEvent ev)
            {
                
                this.handlePopup (ev);
                
            }
            
            public void mouseReleased (MouseEvent ev)
            {

                _this.lastX = -1;
                _this.lastY = -1;

                this.handlePopup (ev);
                
            }

            public void mouseDragged (MouseEvent ev)
            {

                int x = ev.getX ();
                int y = ev.getY ();

                QuollPanel child = _this.panel.getChild ();

                Rectangle bounds = _this.getChildBounds ();

                if (_this.lastX == -1)
                {

                    if ((x <= (bounds.x)) &&
                        (x >= (bounds.x - _this.expandBorderWidth)))
                    {

                        _this.lastX = x;

                        child.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));

                    }

                    if ((x >= (bounds.x + bounds.width)) &&
                        (x <= (bounds.x + bounds.width + _this.expandBorderWidth)))
                    {

                        _this.lastX = x;

                        child.setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR));

                    }

                }

                if (_this.lastY == -1)
                {

                    if ((y <= (bounds.y)) &&
                        (y >= (bounds.y - _this.expandBorderWidth)))
                    {

                        _this.lastY = y;

                        child.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));

                    }

                    if ((y >= (bounds.y + bounds.height)) &&
                        (y <= (bounds.y + bounds.height + _this.expandBorderWidth)))
                    {

                        _this.lastY = y;

                        child.setCursor (Cursor.getPredefinedCursor (Cursor.S_RESIZE_CURSOR));

                    }

                }

                Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
                
                if (_this.lastX > 0)
                {

                    int diff = x - _this.lastX;

                    if ((diff < 0) &&
                        (diff < -5))
                    {

                        diff = -5;

                    }

                    if ((diff > 0) &&
                        (diff > 5))
                    {

                        diff = 5;

                    }

                    int bw = 0;

                    if (x > bounds.width)
                    {

                        bw = d.width - x + diff;

                    } else
                    {

                        bw = x + diff;

                    }

                    _this.xBorderWidth = (float) (((float) bw) / (float) d.width);

                    // Resize the border according to the difference.
                    _this.setChildBorder ();

                    // Not using an event, overkill since there is such high coupling between the two components.
                    _this.properties.displayAreaSizeChanged ();
                    
                    _this.lastX = x;

                    _this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                                          ProjectEvent.CHANGE_BORDER_SIZE);

                }

                if (_this.lastY > 0)
                {

                    int diff = y - _this.lastY;

                    int bh = 0;

                    if (y > bounds.height)
                    {

                        bh = d.height - y + diff;

                    } else
                    {

                        bh = y + diff;

                    }

                    _this.yBorderWidth = (float) (((float) bh) / (float) d.height);

                    // Resize the border according to the difference.
                    _this.setChildBorder ();

                    // Not using an event, overkill since there is such high coupling between the two components.
                    _this.properties.displayAreaSizeChanged ();
                                        
                    _this.lastY = y;

                    _this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                                          ProjectEvent.CHANGE_BORDER_SIZE);

                }

            }

            public void mouseMoved (MouseEvent ev)
            {

                Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();            
            
                Rectangle bounds = _this.getChildBounds ();

                QuollPanel child = _this.panel.getChild ();

                if (bounds.contains (ev.getPoint ()))
                {

                    child.setCursor (Cursor.getDefaultCursor ());

                    return;

                }

                int x = ev.getX ();
                int y = ev.getY ();

                int showThreshold = _this.expandBorderWidth;

                if ((bounds.y <= showThreshold) ||
                    ((d.height - bounds.y) <= showThreshold))
                {

                    showThreshold = 3;

                }

                if (y < showThreshold)
                {

                    _this.showHeader ();

                    return;

                }

                String sideBarLoc = Environment.getUserProperties ().getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

                if (x < showThreshold)
                {                
                
                    _this.showSideBar (Constants.LEFT);
    
                    return;
    
                }
                    
                if (x > (d.width - showThreshold))
                {
                    
                    _this.showSideBar (Constants.RIGHT);

                    return;

                }                
                
                if ((x <= (bounds.x)) &&
                    (x >= (bounds.x - 20)))
                {

                    child.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));

                    return;

                }

                if ((x >= (bounds.x + bounds.width)) &&
                    (x <= (bounds.x + bounds.width + 20)))
                {

                    child.setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR));

                    return;

                }

                if ((y <= (bounds.y)) &&
                    (y >= (bounds.y - 20)))
                {

                    child.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));

                    return;

                }

                if ((y >= (bounds.y + bounds.height)) &&
                    (y <= (bounds.y + bounds.height + 20)))
                {

                    child.setCursor (Cursor.getPredefinedCursor (Cursor.S_RESIZE_CURSOR));

                    return;

                }

                child.setCursor (Cursor.getDefaultCursor ());

            }

        };

        this.initPanel ();

        ((JComponent) this.getLayeredPane ()).setDoubleBuffered (true);

        Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();        
        
        this.setSize (d);
        this.validate ();
        this.repaint ();
        this.setVisible (true);

        this.projectViewer.fireProjectEventLater (ProjectEvent.FULL_SCREEN,
                                                  ProjectEvent.ENTER);

    }

    public void resetPropertiesToDefaults ()
    {
        
        if (this.panel.getChild () instanceof AbstractEditorPanel)
        {
            
            AbstractEditorPanel aep = (AbstractEditorPanel) this.panel.getChild ();
        
            aep.initEditor (this.normalTextProperties);
        
            aep.restoreBackgroundColor ();
                        
            this.fullScreenTextProperties = new FullScreenTextProperties (this,
                                                                          this.normalTextProperties);
            
            this.properties.setTextProperties (this.fullScreenTextProperties);
            
        }
        
    }

    private void createSideBar ()
    {
        
        final FullScreenFrame _this = this;
        
        this.sideBar = new Box (BoxLayout.Y_AXIS);

        Box cp = new Box (BoxLayout.Y_AXIS);
        
        cp.setOpaque (true);
        cp.setBackground (UIUtils.getComponentColor ());
        //cp.setBorder (new EmptyBorder (0, 0, 0, 5));
                
        cp.add (this.projectViewer.getSideBarPanel ());
        
        this.sideBar.add (cp);        
                                           
        MouseAdapter mouseListener = new MouseAdapter ()
        {

            private int lastX = 0;
            private int lastY = 0;
            private boolean dragInProgress = false;
        
            public void mouseReleased (MouseEvent ev)
            {

                this.dragInProgress = false;
                this.lastX = 0;
                this.lastY = 0;

                _this.sideBar.setCursor (Cursor.getDefaultCursor ());                
                
            }

            public void mouseMoved (MouseEvent ev)
            {

                int x = ev.getX ();
                int y = ev.getY ();            
            
                Rectangle bounds = _this.sideBar.getBounds ();

                String sideBarLoc = Environment.getUserProperties ().getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

                _this.sideBar.setCursor (Cursor.getDefaultCursor ());
        
                if (sideBarLoc.equals (Constants.LEFT))
                {

                    if ((x <= bounds.width) &&
                        (x >= bounds.width - 20))
                    {
    
                        _this.sideBar.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));
    
                        return;
    
                    }
                    
                } else {

                    if ((x > 0) &&
                        (x <= 20))
                    {

                        _this.sideBar.setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR));

                        return;

                    }
                    
                }

            }
            
            public void mouseDragged (MouseEvent ev)
            {

                int x = ev.getX ();
                int y = ev.getY ();            
            
                if (!this.dragInProgress)
                {
            
                    this.dragInProgress = true;
                    this.lastX = x;
                    this.lastY = y;
                    
                    return;
                
                }
                    
                Rectangle bounds = _this.sideBar.getBounds ();                
                
                String sideBarLoc = Environment.getUserProperties ().getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

                if (sideBarLoc.equals (Constants.LEFT))
                {
                
                    bounds.width += (x - this.lastX);
                    
                    bounds.width = Math.max (bounds.width, _this.minimumSideBarWidth);
                    
                } else {
                                        
                    int diff = this.lastX - x;

                    bounds.width += diff;
                    
                    if (bounds.width >= _this.minimumSideBarWidth)
                    {
                    
                        bounds.x += (-1 * diff);
                        
                    } else {
                        
                        bounds.width = _this.minimumSideBarWidth;
                        
                    }
                    
                    // Reset the position to prevent jitter (where the diff looks like it swings from positive to negative)                    
                    x = 0;
                                        
                }
                
                _this.sideBar.setPreferredSize (bounds.getSize ());
                
                _this.sideBar.setBounds (bounds);
                                
                ((JComponent) _this.sideBar.getParent ()).revalidate ();

                _this.sideBar.getParent ().repaint ();

                this.lastX = x;
                this.lastY = y;
                
            }        
        
            public void mouseExited (MouseEvent ev)
            {

                if (this.dragInProgress)
                {
                    
                    return;
                    
                }

                if (_this.noHideSideBar)
                {
                    
                    return;
                       
                }

                _this.sideBar.setCursor (Cursor.getDefaultCursor ());
            
                if (_this.sideBar.getBounds ().contains (ev.getLocationOnScreen ()))
                {

                    return;

                }

                _this.sideBar.setVisible (false);

                ((JComponent) _this.sideBar.getParent ()).revalidate ();

                _this.sideBar.getParent ().repaint ();
            }

        };

        this.sideBar.addMouseListener (mouseListener);
        this.sideBar.addMouseMotionListener (mouseListener);

        int width = this.sideBar.getPreferredSize ().width;
        
        this.minimumSideBarWidth = width + 20;
        
        try
        {
            
            width = this.projectViewer.getProject ().getPropertyAsInt (Constants.FULL_SCREEN_SIDEBAR_WIDTH_PROPERTY_NAME);
            
        } catch (Exception e) {
            
            // Ignore.
            
        }

        this.sideBar.setPreferredSize (new Dimension (width,
                                                      this.sideBar.getPreferredSize ().height));
        
        this.sideBar.setOpaque (false);
        
        this.sideBar.setVisible (false);
        
        this.getLayeredPane ().add (this.sideBar,
                                    1,
                                    3);                
        
    }
    
    private void createHeader ()
    {
        
        final FullScreenFrame _this = this;
        
        this.header = new Box (BoxLayout.Y_AXIS);

        Box cp = new Box (BoxLayout.Y_AXIS);
        
        cp.setOpaque (true);
        cp.setBackground (UIUtils.getComponentColor ());
        
        this.header.add (cp);        
        
        this.header.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {
             
                if (_this.headerHideTimer != null)
                {
                    
                    _this.headerHideTimer.stop ();
                    
                }
                
            }
            
            public void mouseExited (MouseEvent ev)
            {

                if (_this.header.getBounds ().contains (ev.getLocationOnScreen ()))
                {

                    return;

                }
/*
                if ((_this.wordCountTimer != null)
                    &&
                    (_this.wordCountTimer.isPopupVisible ())
                   )
                {

                    return;

                }
*/
                _this.header.setVisible (false);

                ((JComponent) _this.header.getParent ()).revalidate ();

                _this.header.getParent ().repaint ();
            }

        });

        this.header.setOpaque (false);

        this.header.setBorder (new com.quollwriter.ui.components.DropShadowBorderX (UIManager.getColor ("Control"),
                                                                                                        Color.BLACK,
                                                                                                  1,
                                                                                                  12,
                                                                                                  0.5f,
                                                                                                  12,
                                                                                                  false,
                                                                                                  true,
                                                                                                  true,
                                                                                                  true));

        this.title = UIUtils.createHeader (null,
                                           Constants.FULL_SCREEN_TITLE,
                                           null,
                                           null);
        this.title.setAlignmentX (Component.LEFT_ALIGNMENT);

        JToolBar titleC = new JToolBar ();
        titleC.setFloatable (false);
        titleC.setOpaque (false);
        titleC.setRollover (true);
        titleC.setBorderPainted (false);
                
        this.clockLabel = new JLabel ();
        this.clockLabel.setBorder (new EmptyBorder (0,
                                                    0,
                                                    3,
                                                    20));
        this.clockLabel.setFont (this.clockLabel.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (18)).deriveFont (Font.PLAIN));        
                
        titleC.add (this.clockLabel);

        titleC.add (Box.createHorizontalStrut (5));        
        
        this.clockFormat = new SimpleDateFormat ("h:mm a");

        // Start a timer.
        this.clockTimer = new Timer (500,
                                     new ActionAdapter ()
                                     {

                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             _this.clockLabel.setText (_this.clockFormat.format (new Date ()));

                                         }

                                     });

        this.clockTimer.start ();
        
        this.panel.projectViewer.fillFullScreenTitleToolbar (titleC);

        titleC.add (UIUtils.createButton ("find",
                                           Constants.ICON_FULL_SCREEN_ACTION,
                                           "Click to find some text",
                                           new ActionAdapter ()
                                           {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.panel.getProjectViewer ().showFind (null);
                                                    
                                                    _this.showSideBar (Constants.LEFT);
                                                    
                                                }
                                            
                                           }));
                                                
        JButton sb = UIUtils.createButton ("fullscreen-exit",
                                           Constants.ICON_FULL_SCREEN_ACTION,
                                           "Click to exit full screen mode",
                                           new ActionAdapter ()
                                           {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.close ();
                                                    
                                                }
                                            
                                           });
        
        titleC.add (sb);

        JButton eb = UIUtils.createButton ("edit-properties",
                                           Constants.ICON_FULL_SCREEN_ACTION,
                                           "Click to edit the full screen properties",
                                           new ActionAdapter ()
                                           {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.showProperties ();
                                                    
                                                }
                                            
                                           });
        
        titleC.add (eb);        
        
        this.title.setControls (titleC);

        cp.add (this.title);
        this.header.setVisible (false);

        this.getLayeredPane ().add (this.header,
                                    1,
                                    1);                
        
    }

    public void showBackgroundSelector (Point showAt)
    {

        final FullScreenFrame _this = this;

        if (this.backgroundSelectorPopup == null)
        {

            this.backgroundSelectorPopup = BackgroundSelector.getBackgroundSelectorPopup (new ChangeAdapter ()
            {

                public void stateChanged (ChangeEvent ev)
                {

                    BackgroundChangeEvent bce = (BackgroundChangeEvent) ev;

                    _this.setFullScreenBackground (bce.getValue ());

                }

            },
            new Dimension (75,
                           75),
            this.backgroundObject);

            this.backgroundSelectorPopup.setDraggable (this);

            this.addPopup (this.backgroundSelectorPopup,
                           true);

        } else
        {

            if (this.backgroundSelectorPopup.isVisible ())
            {

                this.backgroundSelectorPopup.setVisible (false);

                return;

            }

        }

        this.showPopupAt (this.backgroundSelectorPopup,
                          showAt);

        ((BackgroundSelector) this.backgroundSelectorPopup.getContent ()).setSelected (this.backgroundObject);

    }

    private void setFullScreenBackground (Object o)
    {

        if (o == null)
        {
            
            this.setNoFullScreenBackground ();
            
            return;
            
        }

        if (o instanceof String)
        {

            String b = o.toString ();
            
            if (b.startsWith ("bg:"))
            {
                
                this.setFullScreenBackground (new BackgroundImage (b.substring ("bg:".length ())));
                
                return;
                
            } 
            
            if (b.startsWith ("file:"))
            {
                
                this.setFullScreenBackground (new File (b.substring ("file:".length ())));
                
                return;
                
            }
            
            if (b.startsWith ("#"))
            {
                
                this.setFullScreenBackground (UIUtils.getColor (b));
                
                return;
                
            }
            
            this.setNoFullScreenBackground ();
            
            return;
            
        }
    
        if (o instanceof BackgroundImage)
        {
            
            this.setFullScreenBackground ((BackgroundImage) o);
            
            return;
            
        }

        if (o instanceof File)
        {
            
            this.setFullScreenBackground ((File) o);
            
            return;
            
        }

        if (o instanceof BufferedImage)
        {
            
            this.setFullScreenBackground ((BufferedImage) o);
            
            return;
            
        }
        
        if (o instanceof Color)
        {
            
            this.setFullScreenBackground ((Color) o);
            
            return;
            
        }
        
    }
/*
    private void setFullScreenBackground (File f)
    {

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();                        
    
        Image im = UIUtils.getImage (f);
    
        Image image = im.getImage ().getScaledInstance (d.width,
                                                        -1,
                                                        Image.SCALE_FAST);

        BufferedImage bi = new BufferedImage (im.getIconWidth (),
                                              im.getIconHeight (),
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bi.getGraphics ();
        im.paintIcon (null, g2d, 0, 0);
        g2d.dispose ();
        
        this.setFullScreenBackground (bi);        

        this.setFullScreenBackgroundProperty (f);
        
    }
*/
    private void setFullScreenBackground (BackgroundImage im)
    {
                
        BufferedImage bi = UIUtils.getBufferedImage (im.getImage (),
                                                     this);
        
        if (bi == null)
        {
            
            this.setNoFullScreenBackground ();
            
            return;
            
        }
        
        this.setFullScreenBackground (bi);

        this.backgroundObject = im;
   
        this.setFullScreenBackgroundProperty (im);
        
    }

    public Object getBackgroundObject ()
    {
        
        return this.backgroundObject;
        
    }

    private void setFullScreenBackground (BufferedImage im)
    {

        int           height = im.getHeight ();
        int           width = im.getWidth ();
        
        //xxx
                
        Graphics2D    biG2d = (Graphics2D) im.getGraphics ();
        biG2d.drawImage (im,
                         0,
                         0,
                         Color.black,
                         this);
        
        this.background = new TexturePaint (im,
                                            new Rectangle (0,
                                                           0,
                                                           width,
                                                           height));
                
        this.validate ();
        this.repaint ();
        
    }
    
    private void setNoFullScreenBackground ()
    {

        // Check to see if there is a background set, if not use the default.
        Properties userProps = Environment.getUserProperties ();

        Object bgObj = null;
        
        String b = userProps.getProperty (Constants.FULL_SCREEN_BG_PROPERTY_NAME);
        
        if (b == null)
        {
 
            BackgroundImage bimg = new BackgroundImage (Constants.DEFAULT_FULL_SCREEN_BG_IMAGE_FILE_NAME);
 
            this.setFullScreenBackground (bimg);
            
            this.backgroundObject = bimg;
            
            this.setFullScreenBackgroundProperty (bimg);

            return;
        
        }        
    
        this.setFullScreenBackground (this.noImageBackground);

        this.backgroundObject = null;
            
        this.setFullScreenBackgroundProperty (null);
                            
    }
    
    private void setFullScreenBackgroundProperty (Object o)
    {

        Properties userProps = Environment.getUserProperties ();

        // Legacy < v2
        userProps.removeProperty (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME);

        String v = null;
                
        if (o instanceof File)
        {
            
            v = "file:" + ((File) o).getPath ();
            
        }
        
        if (o instanceof Color)
        {
            
            v = UIUtils.colorToHex ((Color) o);
            
        }
        
        if (o instanceof BackgroundImage)
        {
            
            v = "bg:" + ((BackgroundImage) o).getName ();
            
        }
        
        if (v == null)
        {
            
            userProps.removeProperty (Constants.FULL_SCREEN_BG_PROPERTY_NAME);
            
        }
        
        StringProperty p = new StringProperty (Constants.FULL_SCREEN_BG_PROPERTY_NAME,
                                               v);
        p.setDescription ("Full screen background");

        userProps.setProperty (Constants.FULL_SCREEN_BG_PROPERTY_NAME,
                               p);
        
        try
        {
            
            Environment.saveUserProperties (userProps);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to save user properties",
                                  e);
            
        }
        
    }
    
    public void setFullScreenBackground (Color c)
    {
        
        this.background = c;

        this.borderOpacity = 0f;

        this.setChildBorder ();
        
        this.backgroundObject = c;
        
        this.setFullScreenBackgroundProperty (c);
        
        this.validate ();
        this.repaint ();
        
    }
    
    private void setFullScreenBackground (File f)
    {    
    
        if ((f == null)
            ||
            (!f.exists ())
            ||
            (!f.isFile ())
           )
        {
            
            this.setNoFullScreenBackground ();
            
            return;
            
        }
    
        Image im = UIUtils.getImage (f);

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();                        
        
        // Only scale up if the image is greater than 20% of the screen width
        if (im.getWidth (this) > (d.width / 5))
        {

            im = im.getScaledInstance (d.width,
                                       d.height,
                                       Image.SCALE_FAST);

        }
                                       
        BufferedImage bi = new BufferedImage (im.getWidth (this),
                                              im.getHeight (this),
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics ();
        g.drawImage (im,
                     0,
                     0,
                     this);

        g.dispose ();
        
        this.setFullScreenBackground (bi);        

        this.backgroundObject = f;
                
        this.setFullScreenBackgroundProperty (f);
        
        this.panel.getProjectViewer ().fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                                          ProjectEvent.CHANGE_BG_IMAGE);            
    
    }

    public void showAchievementsPopup (QPopup popup,
                                       Point  p)
    {

        popup.setBounds (p.x,
                         p.y,
                         popup.getPreferredSize ().width,
                         popup.getPreferredSize ().height);
        
        this.getLayeredPane ().add (popup,
                                    1,
                                    1);
        
        popup.setVisible (true);
        
    }
/*
    private void setBackgroundImage (File f)
    {
        
        if (f == null)
        {
            
            return;
            
        }

        Properties userProps = Environment.getUserProperties ();
        
        if (!f.exists ())
        {

            UIUtils.showErrorMessage (this,
                                      "Background image file no longer exists.");

            userProps.removeProperty (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME);
            
            try
            {
                
                Environment.saveUserProperties (userProps);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to save user properties",
                                      e);
                
            }

            return;
            
        }
        
        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();                    
        
        // Set the background image.
        ImageIcon im = new ImageIcon (f.getPath ());
        
        Image image = im.getImage ().getScaledInstance (d.width,
                                                        -1,
                                                        Image.SCALE_FAST);

        this.bgImagePanel.setCurrentImage (image);

        FileProperty fp = new FileProperty (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME,
                                            f,
                                            Properties.FILE,
                                            false);
        fp.setDescription ("Full screen background image");

        userProps.setProperty (Constants.FULL_SCREEN_BG_IMAGE_PROPERTY_NAME,
                               fp);
        
        try
        {
        
            Environment.saveUserProperties (userProps);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to save user properties",
                                  e);
            
        }
        
    }
*/
    private void showOverlayPanel (JComponent c)
    {
        
        c.setVisible (true);
        c.revalidate ();
        c.repaint ();

        this.getLayeredPane ().moveToFront (c);
        this.getLayeredPane ().validate ();
        this.getLayeredPane ().repaint ();        
        
    }

    private void showProperties ()
    {
        
        this.panel.getProjectViewer ().showSideBar ("fullscreenproperties");

        if (true)
        {
            
            return;
            
        }

    }
        
    private void hideSideBar ()
    {
        
        this.sideBar.setVisible (false);
        
    }
    private void showSideBar (String showWhere)
    {
        
        if (this.sideBar.isVisible ())
        {

            return;

        }

        if (showWhere == null)
        {
            
            showWhere = Constants.LEFT;
            
        }
        
        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        String sideBarLoc = Environment.getUserProperties ().getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        int x = 0;
        int w = this.sideBar.getPreferredSize ().width;

        Border b = null;
        
        if (showWhere.equals (Constants.LEFT))
        {
        
            b = new com.quollwriter.ui.components.DropShadowBorderX (UIManager.getColor ("Control"),
                                                                     Color.BLACK,
                                                                     1,
                                                                     12,
                                                                     0.5f,
                                                                     12,
                                                                     true,
                                                                     false,
                                                                     true,
                                                                     true);

        } 
                                                                     
        if (showWhere.equals (Constants.RIGHT))
        {

            x = d.width - w;
        
            b = new com.quollwriter.ui.components.DropShadowBorderX (UIManager.getColor ("Control"),
                                                                     Color.BLACK,
                                                                     1,
                                                                     12,
                                                                     0.5f,
                                                                     12,
                                                                     true,
                                                                     true,
                                                                     true,
                                                                     false);
        
        }
        
        this.sideBar.setBorder (b);
        
        this.sideBar.setBounds (x,
                                (int) (d.height * 0.1),
                                w,
                                (int) (d.height * 0.8));
        
        this.showOverlayPanel (this.sideBar);
        
    }
    
    private void hideHeader ()
    {
        
        this.header.setVisible (false);
        
        if (this.headerHideTimer != null)
        {
            
            this.headerHideTimer.stop ();
            this.headerHideTimer = null;
            
        }
        
        this.validate ();
        this.repaint ();
        
    }
    
    private void showHeader ()
    {

        if (this.headerHideTimer != null)
        {
    
            this.headerHideTimer.stop ();
            this.headerHideTimer = null;
            
        }
    
        if (this.header.isVisible ())
        {

            return;

        }

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        this.header.setBounds ((int) (d.width * 0.1),
                               0,
                               (int) (d.width * 0.8),
                               this.header.getPreferredSize ().height);

        this.showOverlayPanel (this.header);
                               
    }
    
    public FullScreenQuollPanel getPanel ()
    {

        return this.panel;

    }

    private void initPanel ()
    {

        java.util.List<Component> cs = this.panel.getTopLevelComponents ();
        
        if (cs != null)
        {
            
            cs = new java.util.ArrayList (cs);
            
        } else {
            
            cs = new java.util.ArrayList ();
            
        }

        cs.add (this.header);

        InputMap im = null;

        for (Component c : cs)
        {

            im = ((JComponent) c).getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                            Event.CTRL_MASK),
                    this.findAction);

            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                            0),
                    this.findAction);                                        
                    
            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                            0),
                    this.closeAction);
/*
            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F5,
                                            0),
                    this.closeAction);                    
  */                  
            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F9,
                                            0),
                    this.closeAction);

            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F6,
                                            0),
                    this.showHeaderAction);


        }

        this.setChildBorder ();

        final QuollPanel child = this.panel.getChild ();

        this.title.setIcon (Environment.getIcon (this.panel.getIconType (),
                                                 Constants.ICON_PANEL_MAIN));
        this.title.setTitle (this.panel.getTitle ());

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        child.setSize (d);
        child.setLocation (0,
                           0);

        child.addMouseWheelListener (this.mouseListener);
        child.addMouseListener (this.mouseListener);
        child.addMouseMotionListener (this.mouseListener);

        if (child instanceof AbstractEditorPanel)
        {
                
            AbstractEditorPanel edPanel = (AbstractEditorPanel) child;
        
            this.normalTextProperties = edPanel.getTextProperties ();
        
            this.fullScreenTextProperties.setOn (edPanel,
                                                 false);
        
            edPanel.initEditor (this.fullScreenTextProperties);
            
            //this.setFontColor (this.fullScreenTextProperties.getTextColor ());
            //this.setBackgroundColor (this.fullScreenTextProperties.getBackgroundColor ());
            
        }
        
        this.getLayeredPane ().add (child,
                                    0,
                                    0);

        child.setVisible (true);

        if (child instanceof AbstractEditorPanel)
        {       
        
            // Ensure the caret is visible.
            ((AbstractEditorPanel) child).scrollCaretIntoView ();

        }
            
    }

    public void incrementYBorderWidth (float v)
    {
        
        this.yBorderWidth += v;
        
        this.setChildBorder ();

        this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                             ProjectEvent.CHANGE_BORDER_SIZE);        
        
    }

    public void incrementXBorderWidth (float v)
    {
        
        this.xBorderWidth += v;
        
        this.setChildBorder ();

        this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                             ProjectEvent.CHANGE_BORDER_SIZE);        
        
    }

    public float getBorderOpacity ()
    {
        
        return this.borderOpacity;
        
    }

    public void setBorderOpacity (float f)
    {
        
        this.borderOpacity = f;
        
        this.setChildBorder ();

        this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                             ProjectEvent.CHANGE_BG_OPACITY);
        
    }

    public void toggleMaximizeDisplayArea ()
    {
        
        if ((this.xBorderWidth > MIN_BORDER_WIDTH) ||
            (this.yBorderWidth > MIN_BORDER_WIDTH))
        {

            this.xBorderWidth = MIN_BORDER_WIDTH;
            this.yBorderWidth = MIN_BORDER_WIDTH;

        } else
        {

            if ((this.xBorderWidth == MAX_BORDER_WIDTH) ||
                (this.yBorderWidth == MAX_BORDER_WIDTH))
            {

                this.xBorderWidth = MIN_BORDER_WIDTH;
                this.yBorderWidth = MIN_BORDER_WIDTH;

            } else
            {

                this.xBorderWidth = MAX_BORDER_WIDTH;
                this.yBorderWidth = MAX_BORDER_WIDTH;

            }

        }
        
        this.setChildBorder ();

        this.projectViewer.fireProjectEvent (ProjectEvent.FULL_SCREEN,
                                             ProjectEvent.CHANGE_BORDER_SIZE);
        
    }
        
    public float getYBorderWidth ()
    {
        
        return this.yBorderWidth;
        
    }

    public float getXBorderWidth ()
    {
        
        return this.xBorderWidth;
        
    }

    public void setChildBorder ()
    {

        if (this.borderOpacity > 1f)
        {

            this.borderOpacity = 1f;

        }

        if (this.borderOpacity < 0f)
        {

            this.borderOpacity = 0f;

        }

        this.xBorderWidth = this.limitBorderWidth (this.xBorderWidth);
        this.yBorderWidth = this.limitBorderWidth (this.yBorderWidth);

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        QuollPanel child = this.panel.getChild ();

        child.setBorder (new CompoundBorder (new MatteBorder ((int) (d.height * this.yBorderWidth),
                                                              (int) (d.width * this.xBorderWidth),
                                                              (int) (d.height * this.yBorderWidth),
                                                              (int) (d.width * this.xBorderWidth),
                                                              new Color (0,
                                                                         0,
                                                                         0,
                                                                         this.borderOpacity)),
                                             new LineBorder (Environment.getBorderColor (),
                                                             2,
                                                             true)));

        child.validate ();
        child.repaint ();

    }
/*
    public void setBackgroundColorX (Color c)
    {
        
        this.fullScreenTextProperties.setBackgroundColor (c);

        if (c.equals (this.fullScreenTextProperties.getTextColor ()))
        {

            if (c.equals (Color.black))
            {

                // Set the background to white.
                this.fullScreenTextProperties.setTextColor (Color.white);

            }

            if (c.equals (Color.white))
            {

                // Set the background to black.
                this.fullScreenTextProperties.setTextColor (Color.black);

            }

        }

    }

    public void setFontColorX (Color c)
    {

        AbstractEditorPanel aep = (AbstractEditorPanel) this.panel.getChild ();

        QTextEditor editor = aep.getEditor ();
        
        this.fullScreenTextProperties.setTextColor (c);

        if (c.equals (this.fullScreenTextProperties.getBackgroundColor ()))
        {

            if (c.equals (Color.black))
            {

                // Set the background to white.
                aep.restoreBackgroundColor ();

                this.fullScreenTextProperties.setBackgroundColor (Color.white);
                
            }

            if (c.equals (Color.white))
            {

                // Set the background to black.
                this.fullScreenTextProperties.setBackgroundColor (Color.black);

            }

        }

    }
*/
    private float limitBorderWidth (float w)
    {

        if (w > MAX_BORDER_WIDTH)
        {

            w = MAX_BORDER_WIDTH;

        }

        if (w < MIN_BORDER_WIDTH)
        {

            w = MIN_BORDER_WIDTH;

        }

        return w;

    }

    private Rectangle getExpandBounds (Rectangle r)
    {

        return new Rectangle (r.x - this.expandBorderWidth,
                              r.y - this.expandBorderWidth,
                              r.width + (this.expandBorderWidth * 2),
                              r.height + (this.expandBorderWidth * 2));

    }

    private Rectangle getChildBounds ()
    {

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        int cx = (int) (((float) d.width) * this.xBorderWidth);
        int cy = (int) (((float) d.height) * this.yBorderWidth);

        int cw = d.width - (2 * cx);
        int ch = d.height - (2 * cy);

        return new Rectangle (cx,
                              cy,
                              cw,
                              ch);

    }

    public AbstractProjectViewer getProjectViewer ()
    {
        
        return this.projectViewer;
        
    }
    
}
