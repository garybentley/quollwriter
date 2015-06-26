package com.quollwriter.editors.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.CardLayout;
import java.awt.image.*;

import java.awt.event.*;

import java.io.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.border.*;

import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.Header;

public class EditorPanel extends Box
{
    
    private EditorsSideBar sideBar = null;
    private EditorChatBox chatBox = null;
    //private JTextArea message = null;
    private boolean typed = false;    
    private EditorEditor editor = null;
    private Map<Date, ChatMessageAccordionItem> chatHistory = new TreeMap ();
    private Box chatHistoryBox = null;
    private JTextField find = null;
    private Box findBox = null;
    private Box findResults = null;
    private JScrollPane chatHistoryScrollPane = null;
    
    private EditorMessageFilter importantMessageFilter = null;
    private Set<EditorMessage> messages = null;
    private Timer dateLabelsUpdate = null;
    private JPanel cards = null;
    private Project project = null;
    private ProjectEditor projectEditor = null;
    
    private boolean showChatBox = true;

    
    
    public EditorPanel (EditorsSideBar     sb,
                        EditorEditor       ed,
                        Set<EditorMessage> messages)
                 throws GeneralException
    {

        super (BoxLayout.Y_AXIS);
                
        this.sideBar = sb;
        this.editor = ed;
        this.messages = messages;
        this.project = this.sideBar.getProjectViewer ().getProject ();
        this.projectEditor = this.project.getProjectEditor (this.editor);
        
        Project np = null;
        
        this.importantMessageFilter = new EditorMessageFilter ()
        {
          
            @Override
            public boolean accept (EditorMessage m)
            {
           
                if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (m))
                {
                  
                    return false;
                  
                }
              
                if (m.isDealtWith ())
                {
                  
                    return false;
                  
                }
              
                if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                {
                  
                    return false;
                  
                }
                
                return true;
              
            }
          
        };     
        
        this.createAboutBox ();

        this.cards = new JPanel ();
        this.cards.setLayout (new CardLayout ());
        this.cards.setOpaque (false);
        this.cards.setAlignmentX (Component.LEFT_ALIGNMENT);        
                
        this.add (this.cards);
        
        this.cards.add (this.createChatHistoryCard (),
                        "chathistory");

    }
    
    public EditorPanel (EditorsSideBar sb,
                        EditorEditor   ed)
                 throws GeneralException
    {
                
        this (sb,
              ed,
              ed.getMessages ());
                
    }
 
    public void handleEditorMessageEvent (EditorMessageEvent ev)
    {
    
        if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (ev.getMessage ()))
        {
            
            return;
            
        }
    
        if (ev.getType () == EditorMessageEvent.MESSAGE_ADDED)
        {
            
            this.addMessage (ev.getMessage ());

            return;
            
        }
        
    }
 
    public void addMessage (EditorMessage mess)
    {
        
        if (mess instanceof EditorChatMessage)
        {
        
            EditorChatMessage cmess = (EditorChatMessage) mess;

            if (this.isShowing ())
            {
                
                cmess.setDealtWith (true);
                
                try
                {
                
                    EditorsEnvironment.updateMessage (cmess);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update message: " +
                                          cmess,
                                          e);
                    
                }
                
            }
        
            Date w = Utils.zeroTimeFields (cmess.getWhen ());
                    
            // See if we have a "today" history box.
            ChatMessageAccordionItem it = this.chatHistory.get (w);
            
            Set<EditorChatMessage> messages = null;
            
            if (it == null)
            {
                
                messages = new LinkedHashSet ();
                messages.add (cmess);
                
                it = new ChatMessageAccordionItem (this.sideBar.getProjectViewer (),
                                                   w,
                                                   messages);
                                            
                it.init ();
                it.setBorder (UIUtils.createPadding (0, 0, 0, 5));            
                                          
                this.chatHistoryBox.add (it);
                                
                this.chatHistory.put (w,
                                      it);
                                          
            } else {
                
                it.addMessage (cmess);
                
            }
            
            final EditorPanel _this = this;
            
            UIUtils.doLater (new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
    
                    JScrollBar vertical = _this.chatHistoryScrollPane.getVerticalScrollBar ();
                    vertical.setValue (vertical.getMaximum ());        
    
                }
                
            });
            
        }
        
    }
 
    public EditorEditor getEditor ()
    {
        
        return this.editor;
        
    }
 
    private Map<Date, Set<EditorChatMessage>> sortChatMessages (Set<EditorChatMessage> messages)
    {
        
        Map<Date, Set<EditorChatMessage>> ret = new TreeMap ();
        
        if (messages == null)
        {
            
            return ret;
            
        }
        
        for (EditorChatMessage m : messages)
        {
            
            Date w = Utils.zeroTimeFields (m.getWhen ());
            
            Set<EditorChatMessage> mess = ret.get (w);
            
            if (mess == null)
            {
                
                mess = new LinkedHashSet ();
                ret.put (w,
                         mess);
                
            }
            
            mess.add (m);
            
        }
        
        return ret;
        
    }
    
    private ChatMessageAccordionItem createChatMessages (Date                   d,
                                                         Set<EditorChatMessage> messages)
    {

        ChatMessageAccordionItem it = new ChatMessageAccordionItem (this.sideBar.getProjectViewer (),
                                                                    d,
                                                                    messages);

        this.chatHistory.put (d,
                              it);
        
        return it;
        
    }
 
    public void showSearch ()
    {
        
        // TODO: Not sure about the best way for this since each message is a container around the actual text.
        
        if (true)
        {
            
            return;
            
        }
        
        this.findBox.setVisible (true);
        
        this.find.selectAll ();
        
        this.find.grabFocus ();
        
    }
 
    private void search ()
    {

        String f = this.find.getText ().trim ();
        
        // Ask all messages.
        Set<EditorMessage> matches = this.editor.getMessages (new EditorMessageFilter ()
                                                              {
                                                                
                                                                  public boolean accept (EditorMessage m)
                                                                  {
                                                                    
                                                                      return true;
                                                                    
                                                                  }
                                                                
                                                              });
                
        
    }
 
    private JComponent createChatHistoryCard ()
    {
        
        final EditorPanel _this = this;

        Box card = new Box (BoxLayout.Y_AXIS);
        card.setAlignmentX (Component.LEFT_ALIGNMENT);
        card.setOpaque (false);
        
        this.find = UIUtils.createSearchBox (750,
                                            new ActionListener ()
                                            {

                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                   _this.search ();

                                               }

                                             });

        this.find.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 this.find.getPreferredSize ().height));
        
        this.findBox = new Box (BoxLayout.Y_AXIS);
        this.findBox.add (this.find);
        
        this.findResults = new ScrollableBox (BoxLayout.Y_AXIS);
        
        this.findBox.add (this.findResults);
        
        this.findBox.setVisible (false);
        
        //card.add (this.findBox);
        
        this.chatHistoryBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.chatHistoryBox.setAlignmentX (Component.LEFT_ALIGNMENT);
       
        ChatMessageAccordionItem hist = null;
                        
        // Waaaay too much type information here...
        // Sort the messages, if present, into date buckets.
                
        Set<EditorChatMessage> fmessages = new LinkedHashSet ();
        
        int undealtWithCount = 0;

        Project np = null;
        
        EditorMessageFilter filter = new DefaultEditorMessageFilter (np,
                                                                     EditorChatMessage.MESSAGE_TYPE);
        
        for (EditorMessage m : this.messages)
        {

            if (filter.accept (m))
            {

                fmessages.add ((EditorChatMessage) m);

            }
            
        }        
                     
        Map<Date, Set<EditorChatMessage>> messages = this.sortChatMessages (fmessages);
        
        Set<Map.Entry<Date, Set<EditorChatMessage>>> entries = messages.entrySet ();
        
        long dontShowBefore = System.currentTimeMillis () - (7 * 24 * 60 * 60 *1000);
        
        for (Map.Entry<Date, Set<EditorChatMessage>> en : entries)
        {
            
            hist = this.createChatMessages (en.getKey (),
                                            en.getValue ());

            hist.setAlignmentX (Component.LEFT_ALIGNMENT);
            hist.init ();
            hist.setBorder (UIUtils.createPadding (0, 0, 0, 5));            
            this.chatHistoryBox.add (hist);
            
            if (en.getKey ().getTime () < (dontShowBefore))
            {
                
                hist.setContentVisible (false);
                
            }

        }

        // Last one should always be visible regardless of age.
        if (hist != null)
        {
            
            hist.setContentVisible (true);
            
        }
                
        this.chatHistoryScrollPane = UIUtils.createScrollPane (this.chatHistoryBox);
        
        this.chatHistoryScrollPane.setBorder (null);
             
        card.add (this.chatHistoryScrollPane);

        if (this.showChatBox)
        {
                    
            card.add (Box.createVerticalStrut (5));

            card.add (this.createChatBox ());
            
        }
        
        return card;
        
    }
  
    private void createAboutBox ()
                          throws GeneralException
    {
        
        final EditorPanel _this = this;

        final EditorInfoBox infBox = new EditorInfoBox (this.editor,
                                                        this.sideBar.getProjectViewer (),
                                                        true);
        
        infBox.setToolTipText ("Right click to see the menu");
        
        infBox.addFullPopupListener ();

        infBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 5));
        
        infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                              infBox.getPreferredSize ().height));
        infBox.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 2),
                                              infBox.getBorder ()));
        
        infBox.init ();
        
        this.add (infBox);        
        
    }
 
    private EditorChatBox createChatBox ()
    {
        
        final EditorPanel _this = this;
        
        this.chatBox = new EditorChatBox (this.editor,
                                          this.sideBar.getProjectViewer ()).init ();
        this.chatBox.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                    UIUtils.createLineBorder ()));
        this.chatBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    this.chatBox.getPreferredSize ().height + 7));
        
        if (this.editor.isPrevious ())
        {
            
            this.chatBox.setVisible (false);
            
        }
        
        return this.chatBox;
        
    }

    public void showChatHistory ()
    {
        
        ((CardLayout) this.cards.getLayout ()).show (this.cards,
                                                     "chathistory");
        
    }
    
    public void showChatBox ()
    {
        
        this.showChatHistory ();
        
        this.chatBox.grabFocus ();
        
    }
        
    private void showMessagesInCard (String             cardId,
                                     String             title,
                                     String             iconType,
                                     String             help,
                                     Set<EditorMessage> messages,
                                     boolean            showAttentionBorder)
    {
        
        final EditorPanel _this = this;
        
        try
        {
        
            // Sort the messages in descending when order or newest first.
            Query q = new Query ();
            q.parse (String.format ("SELECT * FROM %s ORDER BY when DESC",
                                    EditorMessage.class.getName ()));
            
            QueryResults qr = q.execute (messages);
            
            messages = new LinkedHashSet (qr.getResults ());

        } catch (Exception e) {
            
            Environment.logError ("Unable to sort messages",
                                  e);
            
        }
        
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        for (EditorMessage m : messages)
        {
            
            MessageBox mb = null;
            
            try
            {
            
                mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                              this.sideBar.getProjectViewer ());
                //mb.setShowAttentionBorder (showAttentionBorder);
    
                mb.init ();
    
            } catch (Exception e) {
                
                Environment.logError ("Unable to get message box for message: " +
                                      m,
                                      e);
                                
            }
            
            mb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            Box wb = new Box (BoxLayout.Y_AXIS);
            wb.setAlignmentX (Component.LEFT_ALIGNMENT);
            wb.setBorder (UIUtils.createPadding (0, 0, 10, 0));
            wb.add (mb);
            
            b.add (wb);
            
        }

        final JScrollPane sp = UIUtils.createScrollPane (b);
        
        sp.setBorder (null);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (1, 0, 0, 0));

        Box wrapper = new Box (BoxLayout.Y_AXIS);
        wrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
                        
        Header h = new Header (title,
                               Environment.getIcon (iconType,
                                                    Constants.ICON_SIDEBAR),
                               null); 

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "Click to close",
                                              new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.showChatHistory ();
                
            }
            
        });
        
        List<JButton> buts = new ArrayList ();
        buts.add (close);
        
        h.setControls (UIUtils.createButtonBar (buts));
        
        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (14d)).deriveFont (java.awt.Font.PLAIN));
        h.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        wrapper.add (h);
        
        if (help != null)
        {
        
            JLabel info = UIUtils.createInformationLabel (help);
            info.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            
            wrapper.add (info);

        }
        
        wrapper.add (sp);
        
        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (sp.getVerticalScrollBar ().getValue () > 0)
                {
                
                    sp.setBorder (new MatteBorder (1, 0, 0, 0,
                                                   UIUtils.getInnerBorderColor ()));

                } else {
                    
                    sp.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });        
                
        this.cards.add (wrapper,
                        cardId);
        
        ((CardLayout) this.cards.getLayout ()).show (this.cards,
                                                     cardId);        
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getVerticalScrollBar ().setValue (0);        
                
            }
            
        });
        
    }
        
    public boolean isShowChatBox ()
    {
        
        return this.showChatBox;
        
    }
    
    public void setShowChatBox (boolean v)
    {
    
        this.showChatBox = v;
        
    }
    
    public void init ()
               throws GeneralException
    {
                               
        final EditorPanel _this = this;
                
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                JScrollBar vertical = _this.chatHistoryScrollPane.getVerticalScrollBar ();
                vertical.setValue (vertical.getMaximum ());        

            }
            
        });
        
        // Fire a timer once a minute that updates the history boxes date labels.
        // TODO: Schedule so that it only fires just after midnight.
        this.dateLabelsUpdate = new Timer (60 * 1000,
                                           new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                for (Date d : _this.chatHistory.keySet ())
                {
                    
                    _this.chatHistory.get (d).updateHeaderTitle ();
                    
                }
                
            }
            
        });
        
        this.dateLabelsUpdate.start ();
        
    }
    
}