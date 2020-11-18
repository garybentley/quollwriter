package com.quollwriter.editors.messages;

import java.util.*;

import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

//@Message(type=InviteResponseMessage.MESSAGE_TYPE)
public class InviteResponseMessage extends EditorMessage
{

    public static final String MESSAGE_TYPE = "invite-response";

    private boolean accepted = false;
    private String editorName = null;
    private Image editorAvatar = null;

    public InviteResponseMessage ()
    {

    }

    public InviteResponseMessage (boolean       accepted,
                                  EditorAccount acc)
    {

        this.accepted = accepted;

        if (acc == null)
        {

            throw new NullPointerException ("Expected account to be provided.");

        }

        if (this.accepted)
        {

            this.editorName = acc.getName ();
            this.editorAvatar = acc.getAvatar ();

        }

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "accepted",
                                    this.accepted);

    }

    protected void fillMap (Map data)
                     throws Exception
    {

        data.put (MessageFieldNames.accepted,
                  this.accepted);

        Map edInf = new HashMap ();

        data.put (MessageFieldNames.editorinformation,
                  edInf);

        if (this.editorName != null)
        {

            edInf.put (MessageFieldNames.name,
                       this.editorName);

        }

        if (this.editorAvatar != null)
        {

            edInf.put (MessageFieldNames.avatar,
                       EditorsUtils.getImageAsBase64EncodedString (this.editorAvatar));

        }

    }

    public boolean isAccepted ()
    {

        return this.accepted;

    }

    public Image getEditorAvatar ()
    {

        return this.editorAvatar;

    }

    public String getEditorName ()
    {

        return this.editorName;

    }

    public String getMessage ()
                       throws GeneralException
    {

        Map data = new HashMap ();

        data.put (MessageFieldNames.accepted,
                  this.accepted);

        Map edInf = new HashMap ();

        data.put (MessageFieldNames.editorinformation,
                  edInf);

        if (this.editorName != null)
        {

            edInf.put (MessageFieldNames.name,
                       this.editorName);

        }

        if (this.editorAvatar != null)
        {

            try
            {

                edInf.put (MessageFieldNames.avatar,
                           EditorsUtils.getImageAsBase64EncodedString (this.editorAvatar));

            } catch (Exception e) {

                throw new GeneralException ("Unable to base64 encode editor avatar.",
                                            e);

            }

        }

        try
        {

            return JSONEncoder.encode (data);

        } catch (Exception e) {

            throw new GeneralException ("Unable to json encode data: " +
                                        data,
                                        e);

        }

    }

    public void setMessage (String s)
                     throws GeneralException
    {

        // This is a summary of what was sent/received.
        Map data = (Map) JSONDecoder.decode (s);

        this.doInit (data,
                     this.getEditor ());

    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {

        this.accepted = this.getBoolean (MessageFieldNames.accepted,
                                         data);

        Map edInf = (Map) data.get (MessageFieldNames.editorinformation);

        if (edInf != null)
        {

            String n = this.getString (MessageFieldNames.name,
                                       edInf,
                                       false);

            if (n != null)
            {

                this.editorName = n;

            }

            String avatar = this.getString (MessageFieldNames.avatar,
                                            edInf,
                                            false);

            if (avatar != null)
            {

                try
                {

                    this.editorAvatar = EditorsUtils.getImageFromBase64EncodedString (avatar);

                } catch (Exception e) {

                    throw new GeneralException ("Unable to get avatar image from base64 encoded string.",
                                                e);

                }

            }

        }

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
