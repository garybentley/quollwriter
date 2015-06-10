package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;

// Use an annotation?
//@MessageBox(messageClass=InviteResponseMessage)
public class InviteResponseMessageBox extends MessageBox<InviteResponseMessage>
{
        
    public InviteResponseMessageBox (InviteResponseMessage mess,
                                     AbstractProjectViewer     viewer)
    {
        
        super (mess,
               viewer);
        
    }
        
    public boolean isAutoDealtWith ()
    {
        
        return true;
        
    }
        
    public void doUpdate ()
    {
        
    }
    
    public void doInit ()
    {
        
        boolean accepted = this.message.isAccepted ();
        String iconName = (accepted ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME);
        
        String message = "Accepted invitation to be {an editor}";
            
        if (!accepted)
        {

            message = "Rejected invitation to be {an editor}";
            
        }

        JComponent h = UIUtils.createBoldSubHeader (message,
                                                    iconName);        
        
        this.add (h);
        
    }
    
}