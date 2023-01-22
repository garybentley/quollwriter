package com.quollwriter.editors.ui;

import java.util.*;
import java.lang.reflect.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.editors.messages.*;

/**
 * Creates a visual representation (box) of the specified message.
 * Not all messages have visual representations, most notably PublicKeyMessages in which
 * case a null will be returned.
 */
public class MessageBoxFactory
{

    private static final Map<String, Class> boxTypes = new HashMap ();

    static
    {

        Map m = MessageBoxFactory.boxTypes;

        // See the discussion of how this is handled in MessageFactory, the same arguments
        // can (and should) be made here.

        // This is a little more complex however since there are times when a user may want to
        // use their own representation of messages.
        // TODO: Make more flexible in a future release, especially to allow custom representations.

        // TODO: Move this setup to a config file that dynamically loads the classes.
        m.put (EditorChatMessage.MESSAGE_TYPE,
               ChatMessageBox.class);
        m.put (EditorInfoMessage.MESSAGE_TYPE,
               EditorInfoMessageBox.class);
        m.put (NewProjectMessage.MESSAGE_TYPE,
               NewProjectMessageBox.class);
        m.put (NewProjectResponseMessage.MESSAGE_TYPE,
               NewProjectResponseMessageBox.class);
        m.put (ProjectCommentsMessage.MESSAGE_TYPE,
               ProjectCommentsMessageBox.class);
        m.put (InviteResponseMessage.MESSAGE_TYPE,
               InviteResponseMessageBox.class);
        m.put (InviteMessage.MESSAGE_TYPE,
               InviteMessageBox.class);
        m.put (ProjectEditStopMessage.MESSAGE_TYPE,
               ProjectEditStopMessageBox.class);
        m.put (UpdateProjectMessage.MESSAGE_TYPE,
               UpdateProjectMessageBox.class);
        m.put (EditorRemovedMessage.MESSAGE_TYPE,
               EditorRemovedMessageBox.class);

    }

    /**
     * Create a new message box for the message.  May be null if the message isn't supported.
     * The message box WON'T be inited before return.
     *
     * @param mess The message.
     * @param viewer The viewer.
     * @return The message box instance or null if the message isn't supported.
     * @throws Exception The type can be various depending on the myriad of reasons it can go wrong.
     */
    public static MessageBox getMessageBoxInstance (EditorMessage  mess,
                                                    AbstractViewer viewer)
                                             throws Exception
    {

        if (mess == null)
        {

            throw new IllegalArgumentException ("No message specified.");

        }

        Class c = MessageBoxFactory.boxTypes.get (mess.getMessageType ());

        if (c == null)
        {

            return null;

        }

        // Get the constructor.
        Constructor cons = null;

        try
        {

            cons = c.getConstructor (mess.getClass (),
                                     AbstractViewer.class);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get constructor for message type: " +
                                        mess.getMessageType (),
                                        e);

        }

        if (cons == null)
        {

            throw new IllegalArgumentException ("Class: " +
                                                c.getName () +
                                                " does not have the correct constructor");

        }

        MessageBox mb = null;

        try
        {

            mb = (MessageBox) cons.newInstance (mess,
                                                viewer);

        } catch (Exception e) {

            throw new GeneralException ("Unable to create new instance of: " +
                                        c.getName (),
                                        e);

        }

        return mb;

    }

}
