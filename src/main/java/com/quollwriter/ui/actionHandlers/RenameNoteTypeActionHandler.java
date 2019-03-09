package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.events.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class RenameNoteTypeActionHandler extends TextInputActionHandler<AbstractProjectViewer>
{

    private String type = null;
    private UserPropertyHandler handler = null;

    public RenameNoteTypeActionHandler (String                type,
                                        AbstractProjectViewer pv)
    {

        super (pv);

        this.type = type;
        this.handler = Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME);

    }

    public String getIcon ()
    {

        return Constants.EDIT_ICON_NAME;

    }

    public String getTitle ()
    {

        return getUIString (notetypes,actions,rename,title);
        //"Rename {Note} Type";

    }

    public String getHelp ()
    {

        return getUIString (notetypes,actions,rename,text);
        //"Enter the new type below.";

    }

    public String getConfirmButtonLabel ()
    {

        return getUIString (notetypes,actions,rename,buttons,confirm);
        //"Change";

    }

    public String getInitialValue ()
    {

        return this.type;

    }

    public String isValid (String v)
    {

        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {

            return getUIString (notetypes,actions,rename,errors,novalue);
            //"Please enter a new type.";

        }

        return null;

    }

    @Override
    public boolean onConfirm (String v)
                              throws Exception
    {

        try
        {

            if (this.handler.renameType (this.type,
                                         v,
                                         true))
            {

                return false;

            }

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of note type: " +
                                  this.type +
                                  " to: " +
                                  v,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (notetypes,actions,rename,actionerror));
                                      //"Unable to change type.");

        }

        return false;

    }

    public boolean onCancel ()
                             throws Exception
    {

        return true;

    }

    public Point getShowAt ()
    {

        return null;

    }

}
