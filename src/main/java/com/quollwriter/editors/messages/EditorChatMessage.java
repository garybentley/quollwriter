package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;

public class EditorChatMessage extends EditorMessage
{

    public static final String MESSAGE_TYPE = "chat";

    private String text = null;

    public EditorChatMessage ()
    {

    }

    public EditorChatMessage (String       text,
                              boolean      toEditor,
                              EditorEditor editor,
                              Date         when)
    {

        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {

            throw new IllegalArgumentException ("No text provided.");

        }

        this.setEditor (editor);
        this.setSentByMe (toEditor);
        this.text = text;

        this.setWhen (when);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "message",
                                    this.text);

    }

    public void setMessage (String m)
    {

        this.text = m;

    }

    public String getMessage ()
    {

        return this.text;

    }

    protected void fillMap (Map data)
                     throws GeneralException
    {

        if (this.text == null)
        {

            throw new GeneralException ("No message set");

        }

        data.put (MessageFieldNames.text,
                  this.text);

    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {

        String m = this.getString (MessageFieldNames.text,
                                   data);

        this.text = m;

    }

    public boolean isEncrypted ()
    {

        return true;

    }

    public String getMessageType ()
    {

        return MESSAGE_TYPE;

    }

}
