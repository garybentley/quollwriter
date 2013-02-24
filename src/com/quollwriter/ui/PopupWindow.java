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


public abstract class PopupWindow extends JDialog // JFrame
{

    private boolean                 inited = false;
    protected AbstractProjectViewer projectViewer = null;
    private Header                  header = null;
    private JTextPane               helpText = null;
    private Box content = null;
    
    public PopupWindow()
    {


    }

    public PopupWindow(AbstractProjectViewer pv)
    {

        super ();

        this.projectViewer = pv;

        if (pv != null)
        {

            final PopupWindow _this = this;

            pv.addWindowListener (new WindowAdapter ()
                {

                    public void windowClosing (WindowEvent ev)
                    {

                        _this.setVisible (false);
                        _this.dispose ();

                    }

                });

        }

    }

    public AbstractProjectViewer getProjectViewer ()
    {
        
        return this.projectViewer;
        
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

        this.setMinimumSize (new Dimension (500,
                                            0));

        this.setMaximumSize (new Dimension (500,
                                            10000));

        final PopupWindow _this = this;

        this.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
                                                               
        this.content = new Box (BoxLayout.PAGE_AXIS);

        this.content.setOpaque (true);
        this.content.setBackground (UIUtils.getComponentColor ());
        this.content.setBorder (new EmptyBorder (10,
                                                 10,
                                                 10,
                                                 10));
                                                 
        this.header = UIUtils.createHeader ((this.getHeaderTitle () != null) ? this.getHeaderTitle () : this.getWindowTitle (),
                                            Constants.POPUP_WINDOW_TITLE,
                                            null,
                                            null);
        
        this.content.add (this.header);

        this.helpText = UIUtils.createHelpTextPane (null);

        this.content.add (this.helpText);

        this.setHelpText (this.getHelpText ());

        this.helpText.setPreferredSize (new Dimension (500,
                                                       this.helpText.getPreferredSize ().height + 10));
        
        this.content.add (Box.createVerticalStrut (10));

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

            JPanel bp = ButtonBarFactory.buildRightAlignedBar (buts);
            bp.setOpaque (false);
            bp.setBorder (new EmptyBorder (0,
                                           10,
                                           0,
                                           0));
            bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            this.content.add (bp);

        }

        this.getContentPane ().add (this.content);

        this.setResizable (false);

        this.resize ();
        
        UIUtils.setCenterOfScreenLocation (this);

        this.inited = true;

        this.setVisible (true);

        this.toFront ();

    }

    public void resize ()
    {
                
        this.getContentPane ().setPreferredSize (new Dimension (500,
                                                                this.content.getPreferredSize ().height));
        
        this.pack ();
        
    }
    
    public void setVisible (boolean v)
    {

        if (!this.inited)
        {

            this.init ();

        }

        super.setVisible (v);

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

    public void setHelpText (String t)
    {

        if ((t == null) ||
            (t.trim ().equals ("")))
        {

            this.helpText.setVisible (false);

        } else
        {

            this.helpText.setText (UIUtils.getWithHTMLStyleSheet (this.helpText,
                                                                  Environment.replaceObjectNames (t)));

            this.helpText.setVisible (true);

        }

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
