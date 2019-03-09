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

public class RenameTagActionHandler extends TextInputActionHandler<AbstractProjectViewer>
{

    private Tag tag = null;

    public RenameTagActionHandler (Tag                   tag,
                                   AbstractProjectViewer pv)
    {

        super (pv);

        this.tag = tag;

    }

    public String getIcon ()
    {

        return Constants.EDIT_ICON_NAME;

    }

    public String getTitle ()
    {

        return getUIString (tags,actions,rename,title);
        //"Rename Tag";

    }

    public String getHelp ()
    {

        return getUIString (tags,actions,rename,text);
        //"Enter the new tag below.";

    }

    public String getConfirmButtonLabel ()
    {

        return getUIString (tags,actions,rename,buttons,confirm);
        //"Change";

    }

    public String getInitialValue ()
    {

        return this.tag.getName ();

    }

    public String isValid (String v)
    {

        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {

            return getUIString (tags,actions,rename,errors,novalue);
            //"Please enter a new tag.";

        }

        try
        {

            Tag ot = Environment.getTagByName (v.trim ());

            // See if we have another tag with that name.
            if ((ot != null)
                &&
                (ot != this.tag)
               )
            {

                return String.format (getUIString (tags,actions,rename,errors,valueexists),
                                    //"Already have a tag called <b>%s</b>.",
                                      ot.getName ());

            }

        } catch (Exception e) {

            Environment.logError ("Unable to get tag for name: " + v.trim (),
                                  e);

            return getUIString (tags,actions,rename,errors,general);
            //"Unable to check tags.";

        }

        return null;

    }

    @Override
    public boolean onConfirm (String v)
                              throws Exception
    {

        try
        {

            this.tag.setName (v.trim ());

            Environment.saveTag (this.tag);

        } catch (Exception e) {

            Environment.logError ("Unable to update tag: " +
                                  this.tag,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (tags,actions,rename,actionerror));
                                      //"Unable to update tag.");

        }

        return true;

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
