package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.Image;
import java.awt.image.*;
import java.awt.Dimension;
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
        
        //final JComponent b = this.getMessageQuoteComponent (this.message.getMessage ());
            
        //this.add (b);                
            
        Box b = new Box (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);            
        
        this.add (b);
                
        JComponent message = UIUtils.createHelpTextPane (this.message.getMessage (),
                                                         this.projectViewer);
        message.setAlignmentY (Component.TOP_ALIGNMENT);
        message.setSize (new Dimension (300,
                                        500));
        
        message.setBorder (null);
        message.setOpaque (false);
        message.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));
        
        String name = this.message.getEditor ().getMainName ();
                
        if (this.message.isSentByMe ())
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
                                                (av == null ? "Me,<br />" : "") + Environment.formatTime (this.message.getWhen ()));
            avl.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            b.add (avl);
                        
        } else {
        
            message.setAlignmentX (Component.LEFT_ALIGNMENT);
            BufferedImage av = this.message.getEditor ().getDisplayAvatar ();
            
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
                                                Environment.formatTime (this.message.getWhen ()));
            avl.setBorder (UIUtils.createPadding (0, 0, 0, 5));
            b.add (avl);
            b.add (message);
            
        }
        
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
    
}