package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.factories.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public abstract class PopupWindow extends JFrame
{

    private boolean          inited = false;
    protected AbstractViewer viewer = null;
    private Header           header = null;
    private float            buttonAlignment = Component.LEFT_ALIGNMENT;
    private Box              content = null;
    private Point            showAt = null;
    private JTextPane        helpP = null;
    
    public PopupWindow()
    {

    }

    public PopupWindow(AbstractViewer pv,
                       float          buttonAlignment)
    {

        this (pv);
        
        this.viewer = pv;
        
        this.buttonAlignment = buttonAlignment;
                
    }
    
    public PopupWindow(AbstractViewer pv)
    {

        super ();
        
        this.setModalExclusionType (Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        
        this.viewer = pv;

        if (pv != null)
        {

            final PopupWindow _this = this;

            pv.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    _this.close ();

                }

            });

        }

    }

    public void setShowAt (Point p)
    {
        
        this.showAt = p;
        
        if ((this.inited)
            &&
            (this.showAt != null)
           )
        {
            
            this.setLocation (this.showAt.x,
                              this.showAt.y);
                        
        }
    
    }
    
    public Point getShowAt ()
    {
        
        return this.showAt;
        
    }
    
    public AbstractViewer getViewer ()
    {
        
        return this.viewer;
        
    }

    public ActionListener getCloseAction ()
    {
        
        final PopupWindow _this = this;
        
        return new ActionListener ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.close ();
                
            }
            
        };
        
    }
        
    public void init ()
    {

        if (this.inited)
        {

            this.toFront ();

            this.setVisible (true);

            return;

        }

        UIUtils.setFrameTitle (this,
                               this.getWindowTitle ());

        this.setIconImage (Environment.getWindowIcon ().getImage ());

        final PopupWindow _this = this;

        this.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
                                                               
        this.content = new Box (BoxLayout.PAGE_AXIS);

        this.content.setOpaque (true);
        this.content.setBackground (UIUtils.getComponentColor ());
        this.content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                                                 
        String headerTitle = (this.getHeaderTitle () != null) ? this.getHeaderTitle () : this.getWindowTitle ();
        
        if (headerTitle != null)
        {
            
            this.header = UIUtils.createHeader (headerTitle,
                                                Constants.POPUP_WINDOW_TITLE);
            
            this.content.add (this.header);

        }

        Box helpWrapper = new Box (BoxLayout.Y_AXIS);
        helpWrapper.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        helpWrapper.setBorder (UIUtils.createPadding (5, 5, 0, 0));
        
        this.helpP = UIUtils.createHelpTextPane (this.viewer);
        this.helpP.setBorder (null);
        this.helpP.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                           Short.MAX_VALUE));
        
        helpWrapper.add (this.helpP);
        
        this.content.add (helpWrapper);
        this.setHelpText (this.getHelpText ());

        JComponent c = this.getContentPanel ();

        if (c == null)
        {

            this.close ();

            return;

        }

        c.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        c.setOpaque (false);

        this.content.add (c);

        JButton[] buts = this.getButtons ();

        if ((buts != null) &&
            (buts.length > 0))
        {

            this.content.add (Box.createVerticalStrut (10));

            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  this.buttonAlignment); //ButtonBarFactory.buildRightAlignedBar (buts);
            bp.setOpaque (false);
            bp.setBorder (new EmptyBorder (0,
                                           0,
                                           0,
                                           0));
            bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            this.content.add (bp);

        }

        //this.content.add (Box.createVerticalStrut (5));

        this.getContentPane ().add (this.content);

        this.setResizable (false);
        
        this.pack ();
        
        this.resize ();
               
        if (this.showAt == null)
        {
        
            //UIUtils.setCenterOfScreenLocation (this);
            
            // Let the windows manager decide where to show.
            this.setLocationByPlatform (true);

        } else {
            
            this.setLocation (this.showAt.x,
                              this.showAt.y);
            
        }
        
        this.inited = true;
                
        this.setVisible (true);
                
        this.toFront ();
        this.pack ();
        
    }

    public void resize ()
    {

        this.getContentPane ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                                this.content.getPreferredSize ().height));
            
        this.validate ();
        this.repaint ();
        
        final PopupWindow _this = this;
        
        UIUtils.doLater (new ActionListener ()
        {
        
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.pack ();
                                                 
            }
            
        });
        
    }
    
    public void setButtonAlignment (float v)
    {
        
        this.buttonAlignment = v;
        
    }
    
    public void setVisible (boolean v)
    {

        if (!this.inited)
        {

            this.init ();

        }

        super.setVisible (v);

        if (v)
        {
        
            this.resize ();
            
        }
        
    }

    public void close ()
    {

        this.setVisible (false);
        this.dispose ();

    }

    public void setHeaderTitle (String t)
    {

        this.header.setTitle (t);

    }

    public Header getHeader ()
    {
        
        return this.header;
        
    }
    
    public void setHelpText (String t)
    {

        if ((t == null) ||
            (t.trim ().equals ("")))
        {

            this.helpP.setVisible (false);

        } else
        {

            this.helpP.setText (t);

            this.helpP.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                               Short.MAX_VALUE));
                        
            //this.helpP.setText (UIUtils.formatTextForHelpPane (t));

            this.helpP.setVisible (true);

        }
        
        this.resize ();
        this.resize ();
        
    }

    public abstract String getWindowTitle ();

    public abstract String getHeaderTitle ();

    public abstract String getHeaderIconType ();

    public abstract String getHelpText ();

    public abstract JComponent getContentPanel ();

    public abstract JButton[] getButtons ();

/*
    public static JTextPane createHelpText (Component parent,
                                            String    text)
    {

        JTextPane t = UIUtils.createHelpTextPane (parent,
                                                  text);
        t.setBorder (new EmptyBorder (10, 5, 10, 5));

        return t;

    }
*/
}
