package com.quollwriter.editors.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
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

public class EditorPanel extends Box
{
    
    private EditorsSideBar sideBar = null;
    private EditorChatBox chatBox = null;
    //private JTextArea message = null;
    private boolean typed = false;    
    private EditorEditor editor = null;
    private Map<Date, MessageAccordionItem> history = new TreeMap ();
    private Box historyBox = null;
    private JTextField find = null;
    private Box findBox = null;
    private Box findResults = null;
    private JScrollPane scrollPane = null;
    private EditorMessageFilter filter = null;
    private Set<EditorMessage> messages = null;
    
    private boolean showChatBox = true;

    public EditorPanel (EditorsSideBar     sb,
                        EditorEditor       ed,
                        Set<EditorMessage> messages)
    {

        super (BoxLayout.Y_AXIS);
                
        this.sideBar = sb;
        this.editor = ed;
        this.messages = messages;

        this.filter = new DefaultEditorMessageFilter (null,
                                                      EditorChatMessage.MESSAGE_TYPE,
                                                      EditorInfoMessage.MESSAGE_TYPE,
                                                      NewProjectMessage.MESSAGE_TYPE,
                                                      NewProjectResponseMessage.MESSAGE_TYPE,
                                                      ProjectCommentsMessage.MESSAGE_TYPE,
                                                      InviteResponseMessage.MESSAGE_TYPE,
                                                      ProjectEditStopMessage.MESSAGE_TYPE,
                                                      UpdateProjectMessage.MESSAGE_TYPE,
                                                      EditorRemovedMessage.MESSAGE_TYPE);        
            
    }
    
    public EditorPanel (EditorsSideBar sb,
                        EditorEditor   ed)
    {
                
        this (sb,
              ed,
              ed.getMessages ());
                
    }
 
    public void handleEditorMessageEvent (EditorMessageEvent ev)
    {

        if (!this.filter.accept (ev.getMessage ()))
        {
            
            return;
            
        }
    
        if (ev.getType () == EditorMessageEvent.MESSAGE_ADDED)
        {
            
            this.addMessage (ev.getMessage ());

            return;
            
        }

        if (ev.getType () == EditorMessageEvent.MESSAGE_CHANGED)
        {

            // Get the message box.
            for (MessageAccordionItem acc : this.history.values ())
            {

                MessageBox mb = acc.getMessageBoxForMessage (ev.getMessage ());

                if (mb != null)
                {

                    mb.update ();
                    
                    this.validate ();
                    this.repaint ();
                    
                    return;
                    
                }
                
            }            
            
            return;
            
        }
        
    }
 
    public void addMessage (EditorMessage mess)
    {
        
        Date w = this.zeroTimeFields (mess.getWhen ());
                
        // See if we have a "today" history box.
        MessageAccordionItem it = this.history.get (w);
        
        Set<EditorMessage> messages = null;
        
        if (it == null)
        {
            
            messages = new LinkedHashSet ();
            messages.add (mess);
                        
            // Add one.
            it = this.createMessages (w,
                                      messages);
            it.init ();
            it.setBorder (UIUtils.createPadding (0, 0, 0, 5));            
                                      
            this.historyBox.add (it);
            
            history.put (w,
                         it);
                                      
        } else {
            
            it.addMessage (mess);
            
        }
        
        final EditorPanel _this = this;
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                JScrollBar vertical = _this.scrollPane.getVerticalScrollBar ();
                vertical.setValue (vertical.getMaximum ());        

            }
            
        });
        
    }
 
    public EditorEditor getEditor ()
    {
        
        return this.editor;
        
    }
 
    private Map<Date, Set<EditorMessage>> sortMessages (Set<EditorMessage> messages)
    {
        
        Map<Date, Set<EditorMessage>> ret = new TreeMap ();
        
        if (messages == null)
        {
            
            return ret;
            
        }
        
        for (EditorMessage m : messages)
        {
            
            Date w = this.zeroTimeFields (m.getWhen ());
            
            Set<EditorMessage> mess = ret.get (w);
            
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

    private Date zeroTimeFields (Date d)
    {
        
        GregorianCalendar gc = new GregorianCalendar ();
        gc.setTime (d);
        
        gc.set (Calendar.HOUR_OF_DAY,
                0);
        gc.set (Calendar.MINUTE,
                0);
        gc.set (Calendar.SECOND,
                0);
        gc.set (Calendar.MILLISECOND,
                0);        
        
        return gc.getTime ();
        
    }
    
/*        
        // Our buckets.
        //   * Today
        //   * Yesterday
        //   * This week (minus today/yesterday)
        //   * This month
        //   * Previously
        GregorianCalendar gc = new GregorianCalendar ();

        gc.set (Calendar.HOUR_OF_DAY,
                0);
        gc.set (Calendar.MINUTE,
                0);
        gc.set (Calendar.SECOND,
                0);
        gc.set (Calendar.MILLISECOND,
                0);        
        
        Date date = gc.getTime ();
        
        DateRange today = new DateRange (date,
                                         date);

        // Go back to yesterday.
        gc.add (Calendar.DAY_OF_MONTH,
                -1);
        
        date = gc.getTime ();
        
        DateRange yesterday = new DateRange (date,
                                             date);
        
        // Check to see if there are any more days left in the week.
        // If not then do a "last week".
        if ((gc.get (Calendar.DAY_OF_WEEK) == 1)
            &&
            (gc.get (Calendar.DAY_OF_MONTH) > 1)
           )
        {
            
            gc.add (Calendar.DAY_OF_WEEK);
            
            Date pDate = gc.getTime ();
            
            lastWeek = new DateRange (date,
                                      pDate);
            
            date = pDate;
            
        }
        
        DateRange thisWeek = this.getDate (-7, -3);
        
        GregorianCalendar gc = new GregorianCalendar ();
        
        int md = gc.get (Calendar.DAY_OF_MONTH);

        // Need to work out how many days there have been.
        DateRange thisMonth = this.getDate ();
        
        DateRange lastMonth = null;
        
        if ((md - 7) < 0)
        {
            
            gc.add (Calendar.MONTH);
            
            // Less than a week since start of this month, so show a last month
            lastMonth = this.getDateRange (-1 * md,
                                           -1 * md - gc.getActualMaximum (Calendar.DAY_OF_MONTH));
            
        }
                
        DateRange previously = this.getDate ();
        
        List<ChatMessage> todayM = new ArrayList ();
        
        for (ChatMessage m : messages)
        {
            
            Date when = m.getWhen ();
            
            if (today.contains (when))
            {
                
                todayM.add (m);
                
            }
            
        }
        
        return ret;
        
    }
 */
    private MessageAccordionItem createMessages (Date                d,
                                                 Set<EditorMessage> messages)
    {

        MessageAccordionItem it = new MessageAccordionItem (this.sideBar.getProjectViewer (),
                                                            d,
                                                            messages);

        this.history.put (d,
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
 
    private void createHistoryBox ()
    {
        
        final EditorPanel _this = this;

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
        
        this.add (this.findBox);
        
        this.historyBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.historyBox.setAlignmentX (Component.LEFT_ALIGNMENT);
       
        MessageAccordionItem hist = null;
                        
        // Waaaay too much type information here...
        // Sort the messages, if present, into date buckets.
        
        Set<EditorMessage> edMessages = this.messages;
        
        Set<EditorMessage> fmessages = new LinkedHashSet ();
        
        int undealtWithCount = 0;

        for (EditorMessage m : edMessages)
        {

            if (this.filter.accept (m))
            {

                fmessages.add (m);
                /*
                if (!m.isDealtWith ())
                {
                    
                    undealtWithCount++;
                    
                }
*/
            }
            
        }        
             /*   
        String s = "";
        
        if (undealtWithCount > 1)
        {
            
            s = "s";
            
        }
        
        if (undealtWithCount > 0)
        {
            
            this.add (UIUtils.createClickableLabel (Environment.formatNumber (undealtWithCount) + " message" + s + " require your attention.  Click to view them.",
                                                    Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                                         Constants.ICON_TAB_HEADER),
                                                    new ActionListener ()
                                                    {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                        }
                                                        
                                                    }));
            
        }
        */
                     
        Map<Date, Set<EditorMessage>> messages = this.sortMessages (fmessages);
        
        Set<Map.Entry<Date, Set<EditorMessage>>> entries = messages.entrySet ();
        
        long dontShowBefore = System.currentTimeMillis () - (7 * 24 * 60 * 60 *1000);
        
        for (Map.Entry<Date, Set<EditorMessage>> en : entries)
        {
            
            hist = this.createMessages (en.getKey (),
                                        en.getValue ());

            hist.setAlignmentX (Component.LEFT_ALIGNMENT);
            hist.init ();
            hist.setBorder (UIUtils.createPadding (0, 0, 0, 5));            
            this.historyBox.add (hist);
            
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
                
        this.scrollPane = UIUtils.createScrollPane (this.historyBox);
        
        this.scrollPane.setBorder (null);
             
        this.add (this.scrollPane);

        this.add (Box.createVerticalStrut (5));
        
    }
 
    private void createAboutBox ()
    {
        
        final EditorPanel _this = this;

        final EditorInfoBox infBox = new EditorInfoBox (this.editor,
                                                        this.sideBar.getProjectViewer ());
        
        infBox.setToolTipText ("Right click to see the menu");
        
        infBox.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                
                infBox.addDeleteAllMessagesMenuItem (m);
                        
                infBox.addSendMessageMenuItem (m);
                    
                infBox.addSendOrUpdateProjectMenuItem (m);
                                        
                infBox.addShowCommentsMenuItem (m);
/*
                infBox.addSearchMessagesMenuItem (m,
                                                  _this);
  */              
                    
                infBox.addUpdateEditorInfoMenuItem (m);
                
                infBox.addRemoveEditorMenuItem (m);                
                
            }
            
        });        
        
        infBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 5));
        
        infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                              infBox.getPreferredSize ().height));
        infBox.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 2),
                                              infBox.getBorder ()));
        this.add (infBox);        
        
    }
 
    private void createChatBox ()
    {
        
        final EditorPanel _this = this;
        
        this.chatBox = new EditorChatBox (this.editor,
                                          this.sideBar.getProjectViewer ()).init ();
        this.chatBox.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                    UIUtils.createLineBorder ()));
        this.chatBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    this.chatBox.getPreferredSize ().height + 7));
        
        this.add (this.chatBox);

        if (this.editor.isPrevious ())
        {
            
            this.chatBox.setVisible (false);
            
        }
        
    }

    public void showChatBox ()
    {
        
        this.chatBox.grabFocus ();
        
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
    {
                       
        this.createAboutBox ();
        
        this.createHistoryBox ();        
        
        if (this.showChatBox)
        {
                    
            this.createChatBox ();
            
        }
        
        final EditorPanel _this = this;
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                JScrollBar vertical = _this.scrollPane.getVerticalScrollBar ();
                vertical.setValue (vertical.getMaximum ());        

            }
            
        });
        
    }
    
}