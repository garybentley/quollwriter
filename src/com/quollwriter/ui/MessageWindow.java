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
    private String confirmButtonLabel = null;
    private ActionListener onConfirm = null;

    public MessageWindow (AbstractViewer v,
                          String         title,
                          String         message)
    {

        super (v,
               Component.LEFT_ALIGNMENT);
        
        this.message = message;
        this.title = (title != null ? title : Environment.getUIString (LanguageStrings.generalmessage,
                                                                       LanguageStrings.title));
        //"Just so you know...");

    }

    public MessageWindow (AbstractViewer v,
                          String         title,
                          String         message,
                          String         confirmButtonLabel,
                          ActionListener onConfirm)
    {

        super (v,
               Component.LEFT_ALIGNMENT);
        
        this.message = message;
        this.title = (title != null ? title : Environment.getUIString (LanguageStrings.generalmessage,
                                                                       LanguageStrings.title));
                      //"Just so you know...");
        this.confirmButtonLabel = confirmButtonLabel;
        this.onConfirm = onConfirm;

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

        JButton closeBut = UIUtils.createButton ((this.confirmButtonLabel != null ? this.confirmButtonLabel : Constants.CONFIRM_BUTTON_LABEL_ID),
                                                 new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (_this.onConfirm != null)
                {
                    
                    _this.onConfirm.actionPerformed (ev);
                    
                }
                    
                _this.close ();

            }

        });
        
        JButton[] buts = new JButton[1];
        buts[0] = closeBut;

        return buts;

    }

}
