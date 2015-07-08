package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.components.ActionAdapter;

public class EditorChatBox extends Box implements EditorInteractionListener
{
    
    private EditorEditor editor = null;
    private JTextArea message = null;
    private boolean typed = false;
    private AbstractProjectViewer projectViewer = null;
    private boolean typingStartedSent = false;
    private JLabel notification = null;
    
    public EditorChatBox (EditorEditor          ed,
                          AbstractProjectViewer viewer)
    {

        super (BoxLayout.Y_AXIS);
    
        this.editor = ed;
        this.projectViewer = viewer;
    
    }
    
    public EditorChatBox init ()
    {
    
        this.message = UIUtils.createTextArea (5);
    
        final JScrollPane sp = new JScrollPane (this.message);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.getViewport ().setOpaque (false);
        sp.setBorder (null);
        sp.setPreferredSize (this.message.getPreferredSize ());
                
        final EditorChatBox _this = this;
        
        this.add (sp);

        this.notification = UIUtils.createLoadingLabel ("Sending message...");
                        
        final ActionListener sendMessageAction = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                String text = _this.message.getText ().trim ();
                System.out.println ("GOT: " + text);
                if (text.length () == 0)
                {
                    
                    return;
                    
                }
            
                _this.showSending ();
            
                _this.notification.setVisible (true);
                
                _this.validate ();
                _this.repaint ();

                // Add the message to the today list.
                final Date when = new Date ();
                
                final Date w = Utils.zeroTimeFields (when);
        
                final EditorChatMessage m = new EditorChatMessage (text,
                                                                   true,
                                                                   _this.editor,
                                                                   when);
                
                m.setDealtWith (true);
                
                EditorsEnvironment.sendMessageToEditor (m,
                                                        new ActionListener ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.hideNotification ();
                                                
                        _this.message.setText ("");
                        _this.message.grabFocus ();
                        
                        _this.validate ();
                        _this.repaint ();
                        
                    }
                    
                },
                new ActionListener ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.hideNotification ();

                        _this.validate ();
                        _this.repaint ();
                        
                    }
                    
                },
                null);
                
            }
                
        };
        
        JButton save = UIUtils.createButton ("send-message",
                                             Constants.ICON_MENU,
                                             "Click to send the message",
                                             sendMessageAction);

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
        buttons.add (Box.createHorizontalStrut (3));
        buttons.add (this.notification);
        buttons.add (Box.createHorizontalGlue ());
        buttons.add (tb);

        buttons.setBorder (new CompoundBorder (new MatteBorder (1,
                                                                0,
                                                                0,
                                                                0,
                                                                UIUtils.getColor ("#dddddd")),
                                               new EmptyBorder (2,
                                                                2,
                                                                2,
                                                                2)));
                                                                
                                                                
        this.add (buttons);
        
        final ActionListener initText = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.message.setForeground (UIUtils.getHintTextColor ());
                
                String help = "Enter your message here...\n\nTo send press Ctrl+Enter or use the button below.";
                            
                _this.message.setText (help);
        
                _this.message.getCaret ().setDot (0);
        
                _this.message.grabFocus ();

            }
            
        };        
        
        this.message.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                _this.message.grabFocus ();

            }
            
            public void mousePressed (MouseEvent ev)
            {
                
                if ((!_this.message.getText ().equals (""))
                    &&
                    (_this.message.getForeground () == Color.BLACK)
                   )
                {
                    
                    return;
                    
                }
                
                _this.message.setText ("");
                _this.message.setForeground (Color.BLACK);                
                
            }

            public void mouseExited (MouseEvent ev)
            {
                
                if (!_this.typed)
                {
                    
                    initText.actionPerformed (new ActionEvent ("init", 1, "init"));
                    
                }
                
            }
            
        });

        final Timer typingStop = new Timer (1500,
                                            new ActionListener ()
        {
                
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.typingStartedSent = false;
                
                // Send stopped message.
                _this.sendStoppedTyping ();
                
            }
            
        });
        
        typingStop.setRepeats (false);
        
        final Timer typingStart = new Timer (750,
                                             new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                typingStop.stop ();
                
                if (!_this.typingStartedSent)
                {
                    
                    _this.typingStartedSent = true;
                    
                    // Send the message.
                    _this.sendStartedTyping ();
                                        
                }
                
                typingStop.restart ();
                
            }
            
        });
        
        typingStart.setCoalesce (true);
        typingStart.setRepeats (false);
        
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
                
                typingStop.stop ();
                typingStart.start ();
                
                if ((ev.getKeyCode () == KeyEvent.VK_ENTER) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                    _this.sendStoppedTyping ();
                
                    sendMessageAction.actionPerformed (new ActionEvent (message, 1, "sending"));
                
                }

                if ((ev.getKeyCode () == KeyEvent.VK_BACK_SPACE) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                    _this.message.setText ("");
                
                }

            }

        });        
                     
        
                
        this.message.setBorder (new EmptyBorder (3,
                                                 3,
                                                 3,
                                                 3));

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            this.getPreferredSize ().height));
        this.setMinimumSize (this.getPreferredSize ());
        
        initText.actionPerformed (new ActionEvent ("init", 1, "init"));
        
        EditorsEnvironment.addEditorInteractionListener (this);
                
        return this;
        
    }
    
    @Override
    public void handleInteraction (EditorInteractionEvent ev)
    {
        
        if (this.editor != ev.getEditor ())
        {
         
            return;
            
        }
        
        if (ev.getAction () == InteractionMessage.Action.typing)
        {
            
            this.showTyping ();
            
        }
        
        if (ev.getAction () == InteractionMessage.Action.normal)
        {
            
            this.hideNotification ();
            
        }

    }
    
    public void grabFocus ()
    {
        
        this.message.grabFocus ();
        
    }
    
    private void hideNotification ()
    {
        
        this.notification.setVisible (false);
        
    }
    
    private void showTyping ()
    {

        this.notification.setText (this.editor.getShortName () + " is typing...");
        this.notification.setIcon (Environment.getTypingIcon ());
        
        this.notification.setVisible (true);
        
    }
    
    private void showSending ()
    {
        
        this.notification.setText ("Sending message...");
        this.notification.setIcon (Environment.getLoadingIcon ());
        
        this.notification.setVisible (true);
        
    }
    
    private void sendStartedTyping ()
    {

        EditorsEnvironment.sendInteractionMessageToEditor (InteractionMessage.Action.typing,
                                                           this.editor,
                                                           null);
        
    }
    
    private void sendStoppedTyping ()
    {

        EditorsEnvironment.sendInteractionMessageToEditor (InteractionMessage.Action.normal,
                                                      this.editor,
                                                      null);
        
    }
    
}