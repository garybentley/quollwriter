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
import com.quollwriter.events.*;
import com.quollwriter.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.components.ActionAdapter;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class EditorChatBox extends Box implements EditorInteractionListener
{

    private EditorEditor editor = null;
    private TextArea message = null;
    private AbstractViewer viewer = null;
    private boolean typingStartedSent = false;
    private JLabel notification = null;

    public EditorChatBox (EditorEditor   ed,
                          AbstractViewer viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.editor = ed;
        this.viewer = viewer;

    }

    public EditorChatBox init ()
    {

        this.message = UIUtils.createTextArea (getUIString (editors,LanguageStrings.editor,sendchat,box,tooltip),
                                                //"Enter your message here...\n\nTo send press Ctrl+Enter or use the button below.",
                                               5,
                                               -1);

        this.message.setBorder (null);

        final EditorChatBox _this = this;

        this.add (this.message);

        this.notification = UIUtils.createLoadingLabel (getUIString (editors,LanguageStrings.editor,sendchat,sending));
        //"Sending message...");

        final ActionListener sendMessageAction = new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                String text = _this.message.getText ().trim ();

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
                                             getUIString (editors,LanguageStrings.editor,sendchat,box,buttons,send,tooltip),
                                             //"Click to send the message",
                                             sendMessageAction);

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               getUIString (editors,LanguageStrings.editor,sendchat,box,buttons,LanguageStrings.cancel,tooltip),
                                               //"Click to cancel",
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

        this.message.setBorder (null);

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            this.getPreferredSize ().height));
        this.setMinimumSize (this.getPreferredSize ());

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

        this.notification.setText (String.format (getUIString (editors,LanguageStrings.editor,sendchat,contactistyping),
                                                  this.editor.getShortName ()));
                                                 //this.editor.getShortName () + " is typing...");
        this.notification.setIcon (Environment.getTypingIcon ());

        this.notification.setVisible (true);

    }

    private void showSending ()
    {

        this.notification.setText (getUIString (editors,LanguageStrings.editor,sendchat,sending));
        //"Sending message...");
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
