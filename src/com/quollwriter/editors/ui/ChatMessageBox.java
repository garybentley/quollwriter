package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Point;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

public class ChatMessageBox extends MessageBox<EditorChatMessage>
{
        
    public ChatMessageBox (EditorChatMessage     mess,
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
        
        final JComponent b = this.getMessageQuoteComponent (this.message.getMessage ());
            
        this.add (b);                
                            
    }
    
}