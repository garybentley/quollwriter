package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.renderers.*;

public class ChatHistoryAccordionItem extends AccordionItem
{
        
    private ProjectViewer projectViewer = null;
    private Box content = null;
    private List<ChatMessage> messages = null;
    private Date date = null;
    private ChatMessage lastMessage = null;
        
    public ChatHistoryAccordionItem (ProjectViewer     pv,
                                     Date              d,
                                     List<ChatMessage> messages)
    {
        
        super ("",
               null);
        
        int c = 0;
        
        if (messages != null)
        {
            
            c = messages.size ();
            
        } else {
            
            messages = new ArrayList ();
            
        }
        
        this.date = d;
        this.messages = messages;
        
        this.setTitle (Environment.formatDate (d) + " (" + c + ")");
        this.setIconType (null);

        this.projectViewer = pv;

        final ChatHistoryAccordionItem _this = this;
        
        this.content = new Box (BoxLayout.Y_AXIS);
                
        Header h = this.getHeader ();
                      
        h.setTitleColor (UIUtils.getColor ("#aaaaaa"));
        h.setFontSize (12);
                                        
        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getColor ("#dddddd")),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));

        //this.content.add ();
        this.content.setBorder (new EmptyBorder (5, 0, 10, 0));
        
        if (messages != null)
        {
            
            for (int i = 0; i < messages.size (); i++)
            {
                
                ChatMessage m = messages.get (i);
                
                JComponent mb = this.getMessageBox (m);
                
                if ((c > 1)
                    &&
                    (d.getTime () < (System.currentTimeMillis () - (7 * 24 * 60 * 60 *1000)))
                   )
                {
                    
                    if (i == (messages.size () - 1))
                    {

                        mb.setVisible (true);
                    
                    } else {
                        
                        mb.setVisible (false);
                        
                    }
                    
                }
                
                this.content.add (mb);
                
                this.lastMessage = m;
                
            }
            
        }
        
        if ((c > 1)
            &&
            (d.getTime () < (System.currentTimeMillis () - (7 * 24 * 60 * 60 *1000)))
           )
        {
            
            JLabel l = UIUtils.createClickableLabel ("Show other " + (c - 1) + " messages",
                                                     null);
            
            l.setBorder (new EmptyBorder (0, 5, 0, 0));
            
            this.content.add (l);
            //l.setVisible (false);
                        
        }
        
    }
    
    private JLabel createLabel (String m)
    {
        
        JLabel l = new JLabel (m);
        l.setForeground (UIUtils.getColor ("#aaaaaa"));
        
        return l;
        
    }
    
    public void addMessage (ChatMessage m)
    {
        
        JComponent mb = this.getMessageBox (m);
        
        this.content.add (mb);
        
        this.messages.add (m);
        
        int c = this.messages.size ();
        
        this.setTitle (Environment.formatDate (this.date) + " (" + c + ")");        
        
        this.validate ();
        this.repaint ();
        
    }
    
    private JComponent getMessageBox (ChatMessage m)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box details = new Box (BoxLayout.X_AXIS);
        details.setAlignmentX (Component.LEFT_ALIGNMENT);
        details.add (this.createLabel (m.getFrom ()));
        details.add (Box.createHorizontalGlue ());
        details.add (this.createLabel (Environment.formatTime (m.getWhen ())));
        
        b.add (details);
        
        b.add (Box.createVerticalStrut (5));
        
        JTextPane p = UIUtils.createHelpTextPane (m.getMessage (),
                                                  this.projectViewer);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setMaximumSize (null);
        p.setBorder (new EmptyBorder (0, 5, 0, 0));
        b.add (p);
        
        b.setBorder (new EmptyBorder (0, 5, 8, 5));

        return b;
        
    }
    
    public JComponent getContent ()
    {
        
        return this.content;
                
    }
        
    public void init ()
    {
        
        super.init ();
                
    }
    
}