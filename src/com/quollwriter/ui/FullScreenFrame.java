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
import com.quollwriter.editors.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.achievements.ui.*;

public class FullScreenFrame extends JFrame implements PopupsSupported, SideBarListener
{

    public static final float DEFAULT_X_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_Y_BORDER_WIDTH = (7f / 100f);
    public static final float DEFAULT_BORDER_OPACITY = 0.7f;

    public static final float MAX_BORDER_WIDTH = 0.35f;
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
    private ActionAdapter       closeAction = null;
    private ActionAdapter       showHeaderAction = null;
    private ActionAdapter       showPropertiesAction = null;
    //private ActionListener       findAction = null;
    private Header               title = null;
    private JComponent           header = null;
    private Timer                clockTimer = null;
    private JLabel               clockLabel = null;
    private SimpleDateFormat     clockFormat = null;
    private JComponent           bgImagePanel = null;
    private JComponent           sideBar = null;
    private JComponent           sideBarInner = null;
    private JButton              distModeButton = null;
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
    private boolean              hideIconColumnForEditors = false;
    private boolean              distractionFreeModeEnabled = false;
    private Timer achievementsHideTimer = null;
    private QPopup                achievementsPopup = null;
    
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

    public void showSideBar ()
    {
        
        this.showSideBar (Constants.LEFT);
        
    }
    
    public void showBlankPanel ()
    {
        
        BlankQuollPanel bqp = new BlankQuollPanel (this.projectViewer);
        
        bqp.init ();
        
        FullScreenQuollPanel bp = new FullScreenQuollPanel (bqp);
        
        this.showSideBar (Constants.LEFT);
        
        this.switchTo (bp);
        
    }
            
    public void sideBarHidden (SideBarEvent ev)
    {
        
        // TODO
        
            
    }
    
    public void sideBarShown (SideBarEvent ev)
    {
        
        if (!this.projectViewer.isMainSideBarName (ev.getSideBar ().getName ()))
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

        this.projectViewer.doForPanels (QuollEditorPanel.class,
        new DefaultQuollPanelAction ()
        {
                            
            public void doAction (QuollPanel qp)
            {
                                    
                ((QuollEditorPanel) qp).showIconColumn (true);
                                    
            }
                            
        });                

        this.setVisible (false);
        this.dispose ();
        
        this.projectViewer.fullScreenClosed (this.panel.getChild ());

        EditorsEnvironment.fullScreenExited ();
        
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

    public QPopup getPopupByName (String name)
    {
        
        if (name == null)
        {
            
            return null;
            
        }
        
        Component[] children = this.getLayeredPane ().getComponentsInLayer (JLayeredPane.POPUP_LAYER);
        
        if (children == null)
        {
            
            return null;
            
        }
        
        for (int i = 0; i < children.length; i++)
        {
            
            Component c = children[i];
            
            if (name.equals (c.getName ()))
            {
                
                if (c instanceof QPopup)
                {
                    
                    return (QPopup) c;
                    
                }
                
            }
                        
        }
        
        return null;
        
    }    
    
    @Override
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

    @Override
    public void showPopupAt (Component popup,
                             Component showAt,
                             boolean   hideOnParentClick)
    {

        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po,
                          hideOnParentClick);


    }

    @Override
    public void showPopupAt (Component c,
                             Point     p,
                             boolean   hideOnParentClick)
    {

        Insets ins = this.panel.getChild ().getInsets (); // this.getInsets ();

        p.x += ins.left;
        p.y += ins.top;
        
        try
        {

            if (c.getParent () == null)
            {

                this.addPopup (c,
                               hideOnParentClick,
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
                
        if (child instanceof AbstractEditorPanel)
        {
        
            Project proj = this.projectViewer.getProject ();
    
            AbstractEditorPanel edPanel = (AbstractEditorPanel) child;
    
            //QTextEditor ed = edPanel.getEditor ();//editor;
            
            edPanel.setIgnoreDocumentChanges (true);
    
            edPanel.initEditor (this.normalTextProperties);
            
            this.normalTextProperties = null;

            edPanel.restoreBackgroundColor ();
    
            edPanel.restoreFontColor ();
    
            //edPanel.reflowText ();
    
            edPanel.setIgnoreDocumentChanges (false);

            edPanel.setUseTypewriterScrolling (false);

        }

        if (child instanceof QuollEditorPanel)
        {
            
            QuollEditorPanel p = (QuollEditorPanel) child;
            
            p.showIconColumn (true);            
    
        }

        // Restore to the original parent.
        this.panel.restore ();

        this.projectViewer.restoreFromFullScreen (this.panel);
        
        this.getLayeredPane ().remove (child);
        
        if (child instanceof AbstractEditorPanel)
        {
            
            AbstractEditorPanel edPanel = (AbstractEditorPanel) child;
            
            edPanel.scrollCaretIntoView ();

        }
        
    }

    public boolean isDistractionFreeModeEnabled ()
    {
        
        return this.distractionFreeModeEnabled;
        
    }
    
    public void setDistractionFreeModeEnabled (boolean v)
    {
        
        this.distractionFreeModeEnabled = v;

        this.setDistractionFreeModeEnabledForChildPanel (v);
        
        this.distModeButton.setIcon (Environment.getIcon ((this.distractionFreeModeEnabled ? Constants.DISTRACTION_FREE_EXIT_ICON_NAME : Constants.DISTRACTION_FREE_ICON_NAME),
                                     Constants.ICON_TITLE_ACTION));
        
        this.distModeButton.setToolTipText ((this.distractionFreeModeEnabled ? "Click to exit distraction free mode" : "Click to enter distraction free mode"));
        
        // See if they have been in this mode before, if not then show the help popup.
        this.showFirstTimeInDistractionFreeModeInfoPopup ();
        
        if (v)
        {
            
            this.projectViewer.fireProjectEvent (ProjectEvent.DISTRACTION_FREE,
                                                 ProjectEvent.ENTER);            
            
        }
        
    }
    
    private void showFirstTimeInDistractionFreeModeInfoPopup ()
    {
        
        String propName = Constants.SEEN_FIRST_TIME_IN_DISTRACTION_FREE_MODE_INFO_POPUP_PROPERTY_NAME;
        
        if ((this.distractionFreeModeEnabled)
            &&
            (this.panel.getChild () instanceof AbstractEditorPanel)
            &&
            (!this.projectViewer.getProject ().getPropertyAsBoolean (propName))
           )
        {
            
            this.projectViewer.showNotificationPopup ("Welcome to Distraction Free Mode",
                                                      "Since this is your first time using <i>Distraction Free Mode</i> it is recommended you spend a bit of time reading about how it works.  It may work in different ways to how you expect and/or how other writing applications implement similar modes.<br /><br /><a href='help:full-screen-mode/distraction-free-mode'>Click here to find out more</a>",
                                                      -1);

            try
            {
        
                Environment.setUserProperty (propName,
                                             new BooleanProperty (propName,
                                                                  true));
        
            } catch (Exception e)
            {
        
                Environment.logError ("Unable to save user properties",
                                      e);
                
            }            
                                                      
            return;            
            
        }        
        
    }
    
    private void setDistractionFreeModeEnabledForChildPanel (boolean v)
    {
   
        final QuollPanel child = this.panel.getChild ();
   
        if (child instanceof AbstractEditorPanel)
        {
            
            AbstractEditorPanel p = (AbstractEditorPanel) child;
                        
            p.setUseTypewriterScrolling (v);
                        
        }
        
        if (child instanceof QuollEditorPanel)
        {
            
            QuollEditorPanel p = (QuollEditorPanel) child;
            
            p.showIconColumn (!v);            
    
        }
        
        this.setChildBorder ();
        
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
               throws GeneralException
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

            this.noImageBackground = bgImage;
            
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
/*
        this.findAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Show the sidebar.  Then show the find.
                _this.projectViewer.showFind (null);
            
            }

        };
*/
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

        JComponent child = new JPanel ();
        
        this.getLayeredPane ().add (child,
                                    0,
                                    0);                                    
        
        JComponent parent = (JComponent) child.getParent ();

        this.getLayeredPane ().remove (child);
                                    
        InputMap im = parent.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        ActionMap am = parent.getActionMap ();
        
        this.initKeyMappings (im);
        
        this.initActionMappings (am);
        
        this.mouseListener = new MouseEventHandler ()
        {
                
            @Override
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
            
            @Override
            public void mousePressed (MouseEvent ev)
            {
                
                this.handlePopup (ev);
                
            }
            
            @Override
            public void mouseReleased (MouseEvent ev)
            {

                _this.lastX = -1;
                _this.lastY = -1;

                this.handlePopup (ev);
                
                _this.validate ();
                _this.repaint ();
                
            }

            @Override
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

            @Override
            public void mouseExited (MouseEvent ev)
            {
                
                QuollPanel child = _this.panel.getChild ();

                child.setCursor (Cursor.getDefaultCursor ());
                    
            }
            
            @Override
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

                if (x < showThreshold)
                {                
                
                    _this.showSideBar (Constants.LEFT);

                    return;
    
                }
                    /*
                     *Annoying as hell on multiple monitors
                if (x > (d.width - showThreshold))
                {
                    
                    _this.showSideBar (Constants.RIGHT);
                    
                    return;

                }                
                */
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

        this.setDistractionFreeModeEnabled (this.projectViewer.getProject ().getPropertyAsBoolean (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME));
        
        this.initPanel ();

        ((JComponent) this.getLayeredPane ()).setDoubleBuffered (true);

        Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();        
        
        this.setSize (d);
        
        // Setting the location here ensures that we don't get shifted over by the taskbar (on windows).
        this.setLocation (0, 0);
        this.validate ();
        this.repaint ();
        this.setVisible (true);

        EditorsEnvironment.fullScreenEntered ();
        
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

            this.fullScreenTextProperties.setOn (aep,
                                                 false);
            
            this.properties.setTextProperties (this.fullScreenTextProperties);
            
        }
        
    }

    private void createSideBar ()
    {
        
        final FullScreenFrame _this = this;
        
        this.sideBar = new Box (BoxLayout.Y_AXIS);

        this.sideBarInner = new Box (BoxLayout.Y_AXIS);
        
        Box cp = new Box (BoxLayout.Y_AXIS);
        
        this.sideBarInner.setOpaque (false);
        
        cp.setOpaque (true);
        cp.setBackground (UIUtils.getComponentColor ());
        
        this.sideBarInner.add (cp);
                
        cp.add (this.projectViewer.getSideBarForFullScreen ());
        
        this.sideBar.add (this.sideBarInner);        
                                           
        MouseAdapter mouseListener = new MouseAdapter ()
        {

            private int lastX = 0;
            private int lastY = 0;
            private boolean dragInProgress = false;
        
            private Timer exitCheck = new Timer (750,
                                                 new ActionAdapter ()
                                                 {
                                                    
                                                     public void actionPerformed (ActionEvent ev)
                                                     {
                                                        
                                                        if (!_this.sideBar.getBounds ().contains (MouseInfo.getPointerInfo ().getLocation ()))
                                                        {
                                                            
                                                            _this.sideBar.setVisible (false);
                                                            
                                                            ((JComponent) _this.sideBar.getParent ()).revalidate ();
                                            
                                                            _this.sideBar.getParent ().repaint ();
                                                            
                                                        }
                                                        
                                                     }
                                                    
                                                 });
        
            public void mouseEntered (MouseEvent ev)
            {
                
                this.exitCheck.stop ();
                
            }
        
            public void mouseReleased (MouseEvent ev)
            {

                this.dragInProgress = false;
                this.lastX = 0;
                this.lastY = 0;

                _this.sideBarInner.setCursor (Cursor.getDefaultCursor ());                
                
            }

            public void mouseMoved (MouseEvent ev)
            {

                int x = ev.getX ();
                int y = ev.getY ();            
            
                Rectangle bounds = _this.sideBarInner.getBounds ();

                _this.sideBarInner.setCursor (Cursor.getDefaultCursor ());
  
                String loc = Constants.LEFT;
                
                if (bounds.x > bounds.width)
                {
                    
                    loc = Constants.RIGHT;
                    
                }
        
                if (loc.equals (Constants.LEFT))
                {

                    if ((x <= bounds.width) &&
                        (x >= bounds.width - 20))
                    {
    
                        _this.sideBarInner.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));
    
                        return;
    
                    }
                    
                } else {

                    if ((x > 0) &&
                        (x <= 20))
                    {

                        _this.sideBarInner.setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR));

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
                
                String loc = Constants.LEFT;
                
                if (bounds.x > bounds.width)
                {
                    
                    loc = Constants.RIGHT;
                    
                }

                if (loc.equals (Constants.LEFT))
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

                this.exitCheck.setRepeats (false);
                this.exitCheck.stop ();
            
                if (this.dragInProgress)
                {
                    
                    return;
                    
                }

                if (_this.noHideSideBar)
                {
                    
                    return;
                       
                }

                _this.sideBar.setCursor (Cursor.getDefaultCursor ());
                _this.sideBarInner.setCursor (Cursor.getDefaultCursor ());

                if (_this.sideBar.getBounds ().contains (ev.getLocationOnScreen ()))
                {

                    this.exitCheck.restart ();
         
                    return;

                }

                _this.sideBar.setVisible (false);

                ((JComponent) _this.sideBar.getParent ()).revalidate ();

                _this.sideBar.getParent ().repaint ();
            }

        };

        this.sideBarInner.addMouseListener (mouseListener);
        this.sideBarInner.addMouseMotionListener (mouseListener);

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
        
        // Small thing, if the current project viewer sidebar is the text properties then show the full screen
        // properties instead.
        if (this.projectViewer.isCurrentSideBarTextProperties ())
        {
            
            this.showProperties ();
            
        }
        
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
        this.clockLabel.setFont (this.clockLabel.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (18)).deriveFont (Font.PLAIN));        
                
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

        this.distModeButton = UIUtils.createButton (Constants.DISTRACTION_FREE_ICON_NAME,
                                                    Constants.ICON_TITLE_ACTION,
                                                    "Click to enter distraction free mode",
                                                    new ActionAdapter ()
        {
              
            public void actionPerformed (ActionEvent ev)
            {
 
                _this.setDistractionFreeModeEnabled (!_this.isDistractionFreeModeEnabled ());

                try
                {
            
                    Environment.setUserProperty (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME,
                                                 new BooleanProperty (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME,
                                                                      _this.isDistractionFreeModeEnabled ()));
            
                } catch (Exception e)
                {
            
                    Environment.logError ("Unable to save user properties",
                                          e);
                    
                }

            }
              
        });
        
        titleC.add (this.distModeButton);
        
        this.panel.projectViewer.fillFullScreenTitleToolbar (titleC);

        if (EditorsEnvironment.isEditorsServiceAvailable ())
        {
        
            String toolTip = (EditorsEnvironment.hasRegistered () ? "Click to show the {editors}" : "Click to register for the Editors Service.");
        
            titleC.add (UIUtils.createButton (Constants.EDITORS_ICON_NAME,
                                               Constants.ICON_TITLE_ACTION,
                                               toolTip,
                                               new ActionAdapter ()
                                               {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        try
                                                        {
                                                        
                                                            _this.panel.getProjectViewer ().viewEditors ();
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to view editors",
                                                                                  e);
                                                            
                                                            UIUtils.showErrorMessage (_this,
                                                                                      "Unable to show the {editors}.");
                                                            
                                                        }

                                                    }
                                                    
                                               }));

        }
        
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
                          showAt,
                          true);

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

            if (b.equals ("none"))
            {
                
                this.setFullScreenBackground (this.noImageBackground);
    
                this.backgroundObject = null;
                
                this.setFullScreenBackgroundProperty ("none");
                
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
        
        final FullScreenFrame _this = this;
        
        this.background = new TexturePaint (im,
                                            new Rectangle (0,
                                                           0,
                                                           width,
                                                           height));
                
        SwingUtilities.invokeLater (new Runnable ()
        {
            
            public void run ()
            {                

                _this.validate ();
                _this.repaint ();
                
            }
            
        });
        
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
        
        if (o instanceof String)
        {
            
            if (o != null)
            {
            
                v = o.toString ();
                
            }
            
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

    public void showAchievement (AchievementRule ar)
    {

        if (this.isDistractionFreeModeEnabled ())
        {
            
            return;
            
        }
    
        final FullScreenFrame _this = this;
    
        try
        {
            
            Box b = null;
            
            if (this.achievementsPopup == null)
            {
    
                b = new Box (BoxLayout.Y_AXIS);
                b.setBackground (UIUtils.getComponentColor ());
                b.setOpaque (true);            
                
                this.achievementsPopup = UIUtils.createPopup ("You've got an Achievement",
                                                              Constants.ACHIEVEMENT_ICON_NAME,
                                                              b,
                                                              true,
                                                              null);
                
                this.achievementsPopup.getHeader ().setPreferredSize (new Dimension (250,
                                                                      this.achievementsPopup.getHeader ().getPreferredSize ().height));
    
                final Box content = b;
        
                this.achievementsPopup.getHeader ().addMouseListener (new MouseAdapter ()
                {
        
                    public void mouseReleased (MouseEvent ev)
                    {
        
                        _this.achievementsPopup.setVisible (false);
        
                        content.removeAll ();                    
        
                    }
        
                });
    
                this.achievementsPopup.addMouseListener (new ComponentShowHide (new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsHideTimer.stop ();
                        
                    }
                    
                },
                new ActionAdapter ()
                {
    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsHideTimer.start ();
                        
                    }
                    
                }));
    
            } else {
                
                b = (Box) this.achievementsPopup.getContent ();
                            
            }
    
            JComponent arBox = new AchievementBox (ar,
                                                   false,
                                                   true);
    
            if (b.getComponentCount () > 0)
            {
                
                arBox.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getBorderColor ()),
                                                     arBox.getBorder ()));
                
            }
    
            b.add (arBox,
                   0);
    
            if (this.achievementsPopup.getParent () != null)
            {
                
                this.achievementsPopup.getParent ().remove (this.achievementsPopup);
                
            }
    
            this.showPopupAt (this.achievementsPopup,
                              new Point (10, 10),
                              true);
    
            this.achievementsPopup.setVisible (true);
            
            final Box content = b;
            
            if (this.achievementsHideTimer == null)
            {
            
                this.achievementsHideTimer = new Timer (10000,
                                                        new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsPopup.setVisible (false);
        
                        content.removeAll ();
                                        
                    }
                    
                });
    
                this.achievementsHideTimer.setRepeats (false);
    
            }
    
            this.achievementsHideTimer.stop ();
                    
            this.achievementsHideTimer.start ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to display achievement: " +
                                  ar,
                                  e);
            
        }

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

    public void showProperties ()
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
        /*
        if (this.sideBar.isVisible ())
        {

            // Ensure that it is the preferred size for the content.
        
            return;

        }
*/
        
        if (showWhere == null)
        {
            
            showWhere = Constants.LEFT;
            
        }
        
        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        int x = 0;
                        
        Border b = null;
        
        Border sb = null;
        
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

            sb = new MatteBorder (0, 0, 0, 30, new Color (0, 0, 0, 0));
                                                                     
        } 
                        /*                                          
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
        
            sb = new MatteBorder (0, 30, 0, 0, new Color (0, 0, 0, 0));
        
        }
        */
        this.sideBarInner.setBorder (b);
        
        this.sideBar.setBorder (sb);
        
        int w = this.sideBar.getPreferredSize ().width;
                
        this.sideBar.setBounds (x,
                                (int) (d.height * 0.1),
                                w + 30 + 13,
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
        
        this.setChildBorder ();

        final QuollPanel child = this.panel.getChild ();

        if (child.getProjectViewer () instanceof ProjectViewer)
        {
        
            ProjectViewer.addAssetActionMappings (child,
                                                  child.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW),
                                                  child.getActionMap ());        

        }
        
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

            this.showFirstTimeInDistractionFreeModeInfoPopup ();
                        
        }
                
        this.getLayeredPane ().add (child,
                                    0,
                                    0);                                    
                                    
        child.setVisible (true);

        this.setDistractionFreeModeEnabledForChildPanel (this.distractionFreeModeEnabled);            
        
        if (child instanceof AbstractEditorPanel)
        {       
        
            AbstractEditorPanel edPanel = (AbstractEditorPanel) child;
            
            //edPanel.reflowText ();
                        
            // Ensure the caret is visible.
            edPanel.scrollCaretIntoView ();

            edPanel.updateViewportPositionForTypewriterScrolling ();            
            
        }
                    
    }

    private void initActionMappings (ActionMap am)
    {
        
        this.projectViewer.initActionMappings (am);
        
        am.put ("fullscreen-close",
                this.closeAction);
        am.put ("fullscreen-show-header",
                this.showHeaderAction);
        
    }
    
    private void initKeyMappings (InputMap im)
    {
        
        this.projectViewer.initKeyMappings (im);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "fullscreen-close");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F9,
                                        0),
                "fullscreen-close");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F6,
                                        0),
                "fullscreen-show-header");
                
        // Don't allow tab closing in full screen.
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F4,
                                        0),
                null);
        
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

        Border innerBorder = null;
        
        boolean showBorder = true;
        
        if ((this.distractionFreeModeEnabled)
            &&
            ((child instanceof AbstractEditorPanel))
           )
        {
        
            showBorder = false;
        
        }
        
        if (showBorder)
        {

            innerBorder = new LineBorder (Environment.getBorderColor (),
                                          1,
                                          true);

        }

        child.setBorder (new CompoundBorder (new MatteBorder ((int) (d.height * this.yBorderWidth),
                                                              (int) (d.width * this.xBorderWidth),
                                                              (int) (d.height * this.yBorderWidth),
                                                              (int) (d.width * this.xBorderWidth),
                                                              new Color (0,
                                                                         0,
                                                                         0,
                                                                         this.borderOpacity)),
                                             innerBorder));

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
