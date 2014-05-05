package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;

public class FirstProject extends PopupWindow
{
    
    private NewProjectPanel newProjPanel = null;
    private ActionListener createProjectAction = null;
    
    public JComponent getContentPanel ()
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        b.add (Box.createVerticalStrut (5));
        
        Header header = UIUtils.createHeader ("Your first {Project}",
                                              Constants.POPUP_WINDOW_TITLE,
                                              null,
                                              null);
        header.setAlignmentX (Component.LEFT_ALIGNMENT);        
        b.add (header);
        
        JTextPane help = UIUtils.createHelpTextPane ("Let's get started.  To create your first {project} just enter a name in the box below or use the boring one provided.  Don't worry you can change it later.",
                                                     null);
        help.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     help.getPreferredSize ().height + 10));
        b.add (help);
        
        this.newProjPanel = new NewProjectPanel ();
        
        final FirstProject _this = this;
                
        JComponent cp = this.newProjPanel.createPanel (this,
                                                       null,
                                                       true,
                                                       null,
                                                       true);
        
        cp.setBorder (new EmptyBorder (10, 10, 5, 10));
        
        b.add (cp);
        
        JTextField tf = this.newProjPanel.getNameField ();

        tf.setText (Environment.replaceObjectNames ("My First {Project}"));
        tf.selectAll ();
                        
        return b;
        
    }
    
    public void init ()
    {
        
        super.init ();
        
        this.getHeader ().setBorder (new CompoundBorder (new MatteBorder (0,
                                                                  0,
                                                                  1,
                                                                  0,
                                                                  new Color (127,
                                                                             127,
                                                                             127)),
                                                 new EmptyBorder (0,
                                                                  0,
                                                                  3,
                                                                  0)));
        
        
    }
    
    public String getHeaderIconType ()
    {
        
        return null;
        
    }
    
    public void setVisible (boolean v)
    {
        
        super.setVisible (v);
        
        this.newProjPanel.getNameField ().grabFocus ();
        
    }
    
    public String getWindowTitle ()
    {
        
        return this.getHeaderTitle ();
        
    }
    
    public String getHeaderTitle ()
    {
        
        return "Welcome to " + Constants.QUOLL_WRITER_NAME;
        
    }
    
    public String getHelpText ()
    {
        
        return "Hello there and welcome to " + Constants.QUOLL_WRITER_NAME + ".  Since this is your first time using " + Constants.QUOLL_WRITER_NAME + " I'd recommend taking a peek at the <a href='help:main-window/getting-started'>getting started guide</a>, it will only take a minute.<br /><br />If you have any questions then please <a href='qw:contact'>don't hesitate to let me know</a>, all feature requests are welcome and if you need help, just ask.<br /><br />Enjoy and happy writing!";
        
    }
    
    public JButton[] getButtons ()
    {
        
        return null;
        
    }
    
    public FirstProject ()
    {
        
    }
    
}
