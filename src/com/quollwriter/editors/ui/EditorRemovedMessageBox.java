package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
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
import com.quollwriter.editors.*;

public class EditorRemovedMessageBox extends MessageBox<EditorRemovedMessage>
{
                
    public EditorRemovedMessageBox (EditorRemovedMessage         mess,
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
                                        
        String text = String.format ("Removed %sas {an editor}",
                                     (this.message.isSentByMe () ? "" : "you "));
                                
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.DELETE_ICON_NAME);
        
        this.add (h);
                                     
    }
        
}