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

public class ChatMessageAccordionItem extends MessageAccordionItem
{
                
    public ChatMessageAccordionItem (AbstractProjectViewer pv,
                                     Date                  d,
                                     Set<EditorMessage>    messages)
    {
        
        super (pv,
               d,
               messages);
        
    }
        
    private JComponent createAvatar (Image  im,
                                     String message)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        
        JLabel ic = new JLabel (new ImageIcon (im));
        ic.setAlignmentX (Component.RIGHT_ALIGNMENT);
        ic.setBorder (UIUtils.createLineBorder ());
        
        b.add (ic);
        
        b.add (Box.createVerticalStrut (0));
        
        JLabel l = UIUtils.createInformationLabel (message);
        l.setHorizontalTextPosition (JLabel.RIGHT);
        l.setAlignmentX (Component.RIGHT_ALIGNMENT);        
        l.setForeground (UIUtils.getColor ("#aaaaaa"));
        
        l.setFont (l.getFont ().deriveFont ((float) 10));
        b.add (l);
        
        b.setMaximumSize (b.getPreferredSize ());
        b.setAlignmentY (Component.TOP_ALIGNMENT);
        
        return b;
        
    }
           
    @Override 
    public JComponent getMessageBox (EditorMessage m)
    {
        
        EditorChatMessage cm = (EditorChatMessage) m;
        EditorEditor ed = cm.getEditor ();
        
        Box b = new Box (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        JComponent message = UIUtils.createHelpTextPane (cm.getMessage (),
                                                         this.projectViewer);
        message.setAlignmentY (Component.TOP_ALIGNMENT);
        message.setSize (new Dimension (300,
                                        500));
        
        message.setBorder (null);
        message.setOpaque (false);
        message.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));
        
        String name = m.getEditor ().getMainName ();
                
        if (cm.isSentByMe ())
        {
            
            BufferedImage av = EditorsEnvironment.getUserAccount ().getAvatar ();

            if (av != null)
            {
                
                av = UIUtils.getScaledImage (av,
                                             28);
                
                if (av.getHeight () > 28)
                {
                    
                    av = UIUtils.getScaledImage (av,
                                                 28,
                                                 28);
                    
                }
                
            }

            message.setAlignmentX (Component.RIGHT_ALIGNMENT);
            b.add (message);

            JComponent avl = this.createAvatar (av,
                                                (av == null ? "Me,<br />" : "") + Environment.formatTime (cm.getWhen ()));
            avl.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            b.add (avl);
                        
        } else {
        
            message.setAlignmentX (Component.LEFT_ALIGNMENT);
            BufferedImage av = ed.getDisplayAvatar ();
            
            if (av == null)
            {
                
                av = Environment.getNoEditorAvatarImage ();
                
            }
            
            av = UIUtils.getScaledImage (av,
                                         28);
            
            if (av.getHeight () > 28)
            {
                
                av = UIUtils.getScaledImage (av,
                                             28,
                                             28);
                
            }
                
            JComponent avl = this.createAvatar (av,
                                                Environment.formatTime (cm.getWhen ()));
            avl.setBorder (UIUtils.createPadding (0, 0, 0, 5));
            b.add (avl);
            b.add (message);
            
        }        
        
        b.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getColor ("#eeeeee")),
                                         UIUtils.createPadding (7, 5, 3, 5)));
        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         b.getPreferredSize ().height));
                                
        return b;
        
    }
                            
}