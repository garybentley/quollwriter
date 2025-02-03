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

    public void showChatBox ()
    {

        this.chatBox.requestFocus ();

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

    private Node createChatHistory ()
    {

        this.chatHistoryBox = new VBox ();
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

        this.chatHistoryScrollPane = new QScrollPane (this.chatHistoryBox);
        VBox.setVgrow (this.chatHistoryScrollPane,
                       Priority.ALWAYS);

        return this.chatHistoryScrollPane;

    }

}
