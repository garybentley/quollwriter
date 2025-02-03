package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

public class ErrorMessage extends EditorMessage
{

    public static final String MESSAGE_TYPE = "error";

    public enum ErrorType
    {
        projectnotexists ("projectnotexists"),
        invalidstate ("invalidstate");

        private final String type;

        ErrorType (String t)
        {

            this.type = t;

        }

        public String getType ()
        {

            return this.type;

        }

    }

    private String errorMessageId = null;
    private ErrorType errorType = null;
    private String reason = null;

    public ErrorMessage ()
    {

    }

    public ErrorMessage (EditorMessage mess,
                         ErrorType     errorType,
                         String        reason)
    {

        if (errorType == null)
        {

            throw new IllegalArgumentException ("Error type must be specified.");

        }

        if (mess == null)
        {

            throw new IllegalArgumentException ("Message must be specified.");

        }

        this.setEditor (mess.getEditor ());

        this.errorMessageId = mess.getMessageId ();
        this.errorType = errorType;
        this.reason = reason;

    }

    public String toString ()
    {

        return MESSAGE_TYPE + "(errorType:" + this.errorType.getType () + ", messageId: " + this.errorMessageId + ", reason: " + this.reason + ")";

    }

    public String getErrorMessageId ()
    {

        return this.errorMessageId;

    }

    public ErrorType getErrorType ()
    {

        return this.errorType;

    }

    public String getReason ()
    {

        return this.reason;

    }

    protected void fillMap (Map data)
                     throws Exception
    {

        data.put (MessageFieldNames.messageid,
                  this.errorMessageId);

        data.put (MessageFieldNames.errortype,
                  this.errorType.getType ());

        if (this.reason != null)
        {

            data.put (MessageFieldNames.reason,
                      this.reason);

        }

    }

    public String getMessage ()
    {

        return null;

    }

    public void setMessage (String s)
    {

        // Nothing to do.  No need to construct.

    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {

        this.errorMessageId = this.getString (MessageFieldNames.messageid,
                                              data);
        this.errorType = ErrorType.valueOf (this.getString (MessageFieldNames.errortype,
                                                            data));
        this.reason = this.getString (MessageFieldNames.reason,
                                      data,
                                      false);

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
