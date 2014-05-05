package com.quollwriter.ui.sidebars;

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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ScrollableBox;

public class EditorPanel extends Box
{
    
    private EditorsSideBar sideBar = null;
    private JTextArea message = null;
    private boolean typed = false;    
    private EditorEditor editor = null;
    private Map<Date, ChatHistoryAccordionItem> history = new TreeMap ();
    private Box historyBox = null;
    private JTextField find = null;
    private Box findBox = null;
    private Box findResults = null;
    
    public EditorPanel (EditorsSideBar sb,
                        EditorEditor   ed)
    {
        
        super (BoxLayout.Y_AXIS);
                
        this.sideBar = sb;
        this.editor = ed;

        this.createAboutBox ();
        
        this.createHistoryBox ();
        
        this.createChatBox ();
        
    }
 
    public EditorEditor getEditor ()
    {
        
        return this.editor;
        
    }
 
    private Map<Date, List<ChatMessage>> sortMessages (List<ChatMessage> messages)
    {
        
        Map<Date, List<ChatMessage>> ret = new TreeMap ();
        
        if (messages == null)
        {
            
            return ret;
            
        }
        
        for (ChatMessage m : messages)
        {
            
            Date w = this.zeroTimeFields (m.getWhen ());
            
            List<ChatMessage> mess = ret.get (w);
            
            if (mess == null)
            {
                
                mess = new ArrayList ();
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
    private ChatHistoryAccordionItem createMessages (Date              d,
                                                     List<ChatMessage> messages)
    {

        return new ChatHistoryAccordionItem ((ProjectViewer) this.sideBar.getProjectViewer (),
                                             d,
                                             messages);
        
    }
 
    private void search ()
    {
        
    }
 
    private void createHistoryBox ()
    {
        
        final EditorPanel _this = this;

        this.find = UIUtils.createTextField ();
        this.find.setBorder (new CompoundBorder (new EmptyBorder (5, 10, 5, 10),
                                                 this.find.getBorder ()));
        
        KeyAdapter vis = new KeyAdapter ()
        {

            private Timer searchT = new Timer (750,
                                               new ActionAdapter ()
                                               {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      _this.search ();

                                                  }

                                                });

            public void keyPressed (KeyEvent ev)
            {

                this.searchT.setRepeats (false);
                this.searchT.stop ();

                // If enter was pressed then search, don't start the timer.
                if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                {
                    
                    _this.search ();
                    return;
                    
                }

                this.searchT.start ();

            }

        };

        this.find.addKeyListener (vis);
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
        
        ChatHistoryAccordionItem hist = null;
        
        // Waaaay too much type information here...
        // Sort the messages, if present, into date buckets.
        
        Map<Date, List<ChatMessage>> messages = this.sortMessages (this.editor.getMessages ());
        
        Set<Map.Entry<Date, List<ChatMessage>>> entries = messages.entrySet ();
        
        for (Map.Entry<Date, List<ChatMessage>> en : entries)
        {
            
            hist = this.createMessages (en.getKey (),
                                        en.getValue ());

            hist.setAlignmentX (Component.LEFT_ALIGNMENT);
            hist.init ();
            
            this.historyBox.add (hist);
            
            //b.add (Box.createVerticalStrut (10));

        }
 /*       
        messages.add (new ChatMessage ("How are you today?",
                                       "You",
                                       new Date ()));
        messages.add (new ChatMessage ("Not so bad, how are you getting on?",
                                       "John",
                                       new Date ()));
        messages.add (new ChatMessage ("Oh ok.  Did you see that film last night?  The one about the giant zombie martians from Pluto.  Bit weird wasn't it.",
                                       "You",
                                       new Date ()));
        
        hist = new ChatHistoryAccordionItem ((ProjectViewer) this.sideBar.getProjectViewer (),
                                                                      Environment.parseDate ("02 Apr 2013"),
                                                                      this.editor.getMessages ());
        hist.setAlignmentX (Component.LEFT_ALIGNMENT);
        hist.init ();
        
        b.add (hist);
        
        b.add (Box.createVerticalStrut (10));
   */     
/*
        messages.add (new ChatMessage ("Me again.  What you been up to?",
                                       "You",
                                       new Date ()));
        messages.add (new ChatMessage ("Nothing much, this and that why what's up?",
                                       "John",
                                       new Date ()));
        messages.add (new ChatMessage ("Nawt, saw another film the other night.  Resevoir frogs, freaky french film.",
                                       "You",
                                       new Date ()));
        messages.add (new ChatMessage ("Sounds right up your alley",
                                       "John",
                                       new Date ()));
        
        hist = new ChatHistoryAccordionItem ((ProjectViewer) this.sideBar.getProjectViewer (),
                                             new Date (),
                                             messages);
        hist.setAlignmentX (Component.LEFT_ALIGNMENT);
        hist.init ();
        
        b.add (hist);

        b.add (Box.createVerticalStrut (10));
*/
        JScrollPane sp = new JScrollPane (this.historyBox);
        
        sp.setOpaque (false);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        //sp.setBorder (new EmptyBorder (0, 5, 0, 0));
        sp.getViewport ().setOpaque (false);
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        sp.setBorder (null);
                
        this.add (sp);

        this.add (Box.createVerticalStrut (10));
        
    }
 
    private void createAboutBox ()
    {
        
        final EditorPanel _this = this;

        EditorInfoBox infBox = new EditorInfoBox (this.editor);
        infBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        infBox.setBorder (new EmptyBorder (5, 0, 10, 0));

        infBox.setBorder (new CompoundBorder (new MatteBorder (0,
                                                          0,
                                                          1,
                                                          0,
                                                          UIUtils.getBorderColor ()),
                                         new EmptyBorder (5, 0, 5, 0)));
        
        infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                              infBox.getPreferredSize ().height));
        
        this.add (infBox);        
        
    }
 
    private void createChatBox ()
    {
        
        final EditorPanel _this = this;
        
        this.message = UIUtils.createTextArea (5);

        JScrollPane sp = new JScrollPane (this.message);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.getViewport ().setOpaque (false);
        sp.setBorder (null);
        
        Box messageBox = new Box (BoxLayout.Y_AXIS);
        messageBox.add (sp);
                
        JButton save = UIUtils.createButton ("send-message",
                                             Constants.ICON_MENU,
                                             "Click to send the message",
                                             new ActionAdapter ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.sendMessage ();
                                                    
                                                }
                                                
                                             });

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to cancel",
                                               new ActionAdapter ()
                                               {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                    
                                                     _this.message.setText ("");

                                                  }
                                                  
                                               });

        List<JButton> buts = new ArrayList ();

        buts.add (save);
        buts.add (cancel);

        JToolBar tb = UIUtils.createButtonBar (buts);

        tb.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        Box buttons = new Box (BoxLayout.X_AXIS);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        buttons.add (Box.createHorizontalGlue ());
        buttons.add (tb);

        buttons.setBorder (new CompoundBorder (new MatteBorder (1,
                                                                0,
                                                                0,
                                                                0,
                                                                UIUtils.getColor ("#dddddd")),
                                               new EmptyBorder (3,
                                                                3,
                                                                3,
                                                                3)));
                                                                
                                                                
        messageBox.add (buttons);
        
        this.message.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                _this.message.grabFocus ();

            }
            
            public void mousePressed (MouseEvent ev)
            {
                
                _this.message.setText ("");
                _this.message.setForeground (Color.BLACK);                
                
            }

            public void mouseExited (MouseEvent ev)
            {
                
                if (!_this.typed)
                {
                    
                    _this.initEditText ();
                    
                }
                
            }
            
        });

        this.message.addKeyListener (new KeyAdapter ()
        {

            private boolean typed = false;
        
            public void keyPressed (KeyEvent ev)
            {
                        
                if (!_this.typed)
                {                        
 
                    _this.typed = true;
                
                    _this.message.setText ("");
                    _this.message.setForeground (Color.BLACK);
                            
                }
                
                if ((ev.getKeyCode () == KeyEvent.VK_ENTER) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                    _this.sendMessage ();
                
                }

                if ((ev.getKeyCode () == KeyEvent.VK_BACK_SPACE) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                    _this.message.setText ("");
                
                }

            }

        });        
                
        messageBox.setBorder (UIUtils.createLineBorder ());
        message.setBorder (new EmptyBorder (3,
                                            3,
                                            3,
                                            3));

        //sp.getViewport ().setPreferredSize (message.getPreferredSize ());

        messageBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  messageBox.getPreferredSize ().height));
        
        this.add (Box.createVerticalGlue ());
        this.add (messageBox);
        
    }

    private void sendMessage ()
    {
        
        // Add the message to the today list.
        Date when = new Date ();
        
        Date w = this.zeroTimeFields (when);

        ChatMessage m = new ChatMessage (this.message.getText ().trim (),
                                         "You",
                                         when);
        
        // See if we have a "today" history box.
        ChatHistoryAccordionItem it = this.history.get (w);
        
        List<ChatMessage> messages = null;
        
        if (it == null)
        {
            
            messages = new ArrayList ();
            messages.add (m);
                        
            // Add one.
            it = this.createMessages (w,
                                      messages);
            it.init ();
                                      
            this.historyBox.add (it);
            
            history.put (w,
                         it);
                                      
        } else {
            
            it.addMessage (m);
            
        }
        
        this.message.setText ("");
        this.message.grabFocus ();
        
    }
    
    private void initEditText ()
    {
        
        final Color lightGrey = UIUtils.getColor ("#aaaaaa");

        this.message.setForeground (lightGrey);
        
        String help = "Enter message...\n\nTo save press Ctrl+Enter or use the buttons below.";
                    
        this.message.setText (help);

        this.message.getCaret ().setDot (0);

        this.message.grabFocus ();
        
    }
    
    public void showMessageBox ()
    {
        
        this.message.grabFocus ();
        
    }
    
    public void init ()
    {
        
        this.initEditText ();
        
    }
    
}