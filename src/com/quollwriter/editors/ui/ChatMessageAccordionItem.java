package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.renderers.*;

public class ChatMessageAccordionItem extends MessageAccordionItem<EditorChatMessage>
{
                
    public ChatMessageAccordionItem (AbstractProjectViewer  pv,
                                     Date                   d,
                                     Set<EditorChatMessage> messages)
    {
        
        super (pv,
               d,
               messages);
        
    }

    @Override 
    public JComponent getMessageBox (EditorChatMessage m)
    {

        ChatMessageBox cmb = new ChatMessageBox (m,
                                                 this.projectViewer);

        try
        {                                                 
        
            cmb.init ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init chat message box for message: " +
                                  m,
                                  e);            
                                                 
        }
        
        Box b = new Box (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (cmb);
        
        b.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getColor ("#eeeeee")),
                                         UIUtils.createPadding (7, 5, 3, 5)));
        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         b.getPreferredSize ().height));
                
        return b;
        
    }
                            
}