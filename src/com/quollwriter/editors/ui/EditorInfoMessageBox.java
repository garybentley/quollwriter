package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;

public class EditorInfoMessageBox extends MessageBox<EditorInfoMessage>
{
        
    public EditorInfoMessageBox (EditorInfoMessage     mess,
                                 AbstractProjectViewer viewer)
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
        
        this.add (this.getMessageComponent ("Name/avatar " + (this.message.isSentByMe () ? "sent" : "received"),
                                            Constants.INFO_ICON_NAME));
                                    
    }
    
}