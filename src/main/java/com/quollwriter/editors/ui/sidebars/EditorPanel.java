package com.quollwriter.editors.ui.sidebars;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;

import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorPanel extends VBox
{

    private AbstractViewer viewer = null;
    private EditorChatBox chatBox = null;
    //private JTextArea message = null;
    private boolean typed = false;
    private EditorEditor editor = null;
    private Map<Date, ChatMessageAccordionItem> chatHistory = new TreeMap<> ();
    private VBox chatHistoryBox = null;
    /*
    private JTextField find = null;
    private Box findBox = null;
    private Box findResults = null;
    */
    private ScrollPane chatHistoryScrollPane = null;

    private ScheduledFuture dateLabelsUpdate = null;
    private Project project = null;
    private ProjectEditor projectEditor = null;

    //private boolean showChatBox = true;

    public EditorPanel (AbstractViewer     viewer,
                        EditorEditor       ed,
                        IPropertyBinder    binder)
                 throws GeneralException
    {

        if (!ed.messagesLoaded ())
        {

            EditorsEnvironment.loadMessagesForEditor (ed);

        }

        binder.addSetChangeListener (ed.getMessages (),
                                     ev ->
        {

            if (ev.wasRemoved ())
            {

                UIUtils.runLater (() ->
                {

                    this.removeMessage (ev.getElementRemoved ());

                });

            }

            if (ev.wasAdded ())
            {

                if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (ev.getElementAdded ()))
                {

                    return;

                }

                UIUtils.runLater (() ->
                {

                    this.addMessage (ev.getElementAdded ());

                });

                return;

            }

        });

        this.dateLabelsUpdate = Environment.schedule (() ->
        {

            UIUtils.runLater (() ->
            {

                for (Date d : this.chatHistory.keySet ())
                {

                    this.chatHistory.get (d).updateHeaderTitle ();

                }

            });

        },
        60 * Constants.MIN_IN_MILLIS,
        60 * Constants.MIN_IN_MILLIS);

        this.viewer = viewer;
        this.editor = ed;

        if  (viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) viewer;
            this.project = pv.getProject ();
            this.projectEditor = this.project.getProjectEditor (this.editor);

        }

        Project np = null;

        EditorInfoBox infBox = new EditorInfoBox (this.editor,
                                                  this.viewer,
                                                  false,
                                                  binder);

        UIUtils.setTooltip (infBox,
                            getUILanguageStringProperty (editors,LanguageStrings.editor,view,info,tooltip,general));
        infBox.addFullPopupListener ();

        this.chatBox = new EditorChatBox (this.editor,
                                          this.viewer);

        this.chatBox.managedProperty ().bind (this.chatBox.visibleProperty ());
        this.chatBox.setVisible (!this.editor.isPrevious ());

        VBox chatBoxWrapper = new VBox ();
        chatBoxWrapper.getStyleClass ().add ("chatbox-wrapper");
        chatBoxWrapper.getChildren ().add (this.chatBox);

        this.getChildren ().addAll (infBox,
                                    this.createChatHistory (),
                                    chatBoxWrapper);

        UIUtils.forceRunLater (() ->
        {

            this.chatHistoryScrollPane.setVvalue (this.chatHistoryScrollPane.getVmax ());

        });

    }

    public void removeMessage (EditorMessage mess)
    {

        // Needed?

    }

    public void addMessage (EditorMessage mess)
    {

        if (mess instanceof EditorChatMessage)
        {

            EditorChatMessage cmess = (EditorChatMessage) mess;
/*
TODO?
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
*/
            Date w = Utils.zeroTimeFields (cmess.getWhen ());

            // See if we have a "today" history box.
            ChatMessageAccordionItem it = this.chatHistory.get (w);

            Set<EditorChatMessage> messages = null;

            if (it == null)
            {

                messages = new LinkedHashSet<> ();
                messages.add (cmess);

                it = new ChatMessageAccordionItem (this.viewer,
                                                   w,
                                                   messages);

                this.chatHistoryBox.getChildren ().add (it.getAccordionItem ());

                this.chatHistory.put (w,
                                      it);

            } else {

                it.addMessage (cmess);

            }

            UIUtils.forceRunLater (() ->
            {

                this.chatHistoryScrollPane.setVvalue (this.chatHistoryScrollPane.getVmax ());

            });

        }

    }

    public EditorEditor getEditor ()
    {

        return this.editor;

    }

    private Map<Date, Set<EditorChatMessage>> sortChatMessages (Set<EditorChatMessage> messages)
    {

        Map<Date, Set<EditorChatMessage>> ret = new TreeMap<> ();

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

                mess = new LinkedHashSet<> ();
                ret.put (w,
                         mess);

            }

            mess.add (m);

        }

        return ret;

    }
/*
    private ChatMessageAccordionItem createChatMessages (Date                   d,
                                                         Set<EditorChatMessage> messages)
    {

        ChatMessageAccordionItem it = new ChatMessageAccordionItem (this.viewer,
                                                                    d,
                                                                    messages);

        this.chatHistory.put (d,
                              it);

        return it;

    }
*/
/*
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
*/
/*
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
*/
    private Node createChatHistory ()
    {

        this.chatHistoryBox = new VBox ();
/*
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
*/

        ChatMessageAccordionItem hist = null;

        // Waaaay too much type information here...
        // Sort the messages, if present, into date buckets.

        Set<EditorChatMessage> fmessages = new LinkedHashSet<> ();

        int undealtWithCount = 0;

        Project np = null;

        EditorMessageFilter filter = new DefaultEditorMessageFilter (np,
                                                                     EditorChatMessage.MESSAGE_TYPE);

        for (EditorMessage m : this.editor.getMessages ())
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

            hist = new ChatMessageAccordionItem (this.viewer,
                                                 en.getKey (),
                                                 en.getValue ());

            this.chatHistory.put (en.getKey (),
                                  hist);

            this.chatHistoryBox.getChildren ().add (hist.getAccordionItem ());

            if (en.getKey ().getTime () < (dontShowBefore))
            {

                hist.getAccordionItem ().setContentVisible (false);

            }

        }

        // Last one should always be visible regardless of age.
        if (hist != null)
        {

            hist.getAccordionItem ().setContentVisible (true);

        }

        this.chatHistoryScrollPane = new ScrollPane (this.chatHistoryBox);
        VBox.setVgrow (this.chatHistoryScrollPane,
                       Priority.ALWAYS);

        return this.chatHistoryScrollPane;

    }

/*
REMOVE
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
                                                              this.viewer);
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
                                              getUIString (actions,clicktoclose),
                                              //"Click to close",
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

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (java.awt.Font.PLAIN));
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
    */
/*
    public boolean isShowChatBox ()
    {

        return this.showChatBox;

    }

    public void setShowChatBox (boolean v)
    {

        this.showChatBox = v;

    }
*/

}
