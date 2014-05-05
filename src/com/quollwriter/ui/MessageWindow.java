package com.quollwriter.ui;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import java.text.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public class MessageWindow extends PopupWindow
{

    private String message = null;
    private String title = null;

    public MessageWindow (AbstractProjectViewer v,
                          String                title,
                          String                message)
    {

        super (v,
               Component.LEFT_ALIGNMENT);
        
        this.message = message;
        this.title = (title != null ? title : "Just so you know...");

    }

    public String getWindowTitle ()
    {

        return this.title;

    }

    public String getHeaderTitle ()
    {

        return this.title;

    }

    public String getHeaderIconType ()
    {

        return Constants.INFO_ICON_NAME;

    }

    public String getHelpText ()
    {

        return this.message;

    }

    public void init ()
    {

        super.init ();

    }

    public void setVisible (boolean v)
    {
        
        super.setVisible (v);
        
        if (v)
        {
            
            this.toFront ();
            
        }
        
    }
    
    public JComponent getContentPanel ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        return b;

    }

    public JButton[] getButtons ()
    {

        final MessageWindow _this = this;

        JButton closeBut = new JButton ();
        closeBut.setText ("Close");

        closeBut.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.close ();

            }

        });
        
        JButton[] buts = new JButton[1];
        buts[0] = closeBut;

        return buts;

    }

}
