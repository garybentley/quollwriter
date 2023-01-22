package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.events.*;

public class AddNewTagActionHandler extends TextInputActionHandler<AbstractProjectViewer>
{

    private NamedObject obj = null;

    public AddNewTagActionHandler (NamedObject           obj,
                                   AbstractProjectViewer pv)
    {

        super (pv);

        this.obj = obj;

    }

    public String getIcon ()
    {

        return Constants.ADD_ICON_NAME;

    }

    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.tags,
                                        LanguageStrings.actions,
                                        LanguageStrings.newtag,
                                        LanguageStrings.title);
        //"Add New Tag(s)";

    }

    public String getHelp ()
    {

        return String.format (Environment.getUIString (LanguageStrings.tags,
                                                       LanguageStrings.actions,
                                                       LanguageStrings.newtag,
                                                       LanguageStrings.text),
                              //"Enter the new tag(s) below.  Separate the tags with commas or semi-colons.<br /><br />The tags will be added to <b>%s</b>.",
                              this.obj.getName ());

    }

    public String getConfirmButtonLabel ()
    {

        return Environment.getUIString (LanguageStrings.tags,
                                        LanguageStrings.actions,
                                        LanguageStrings.newtag,
                                        LanguageStrings.confirm);
        //"Add";

    }

    public String getInitialValue ()
    {

        return null;

    }

    public String isValid (String v)
    {

        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {

            return Environment.getUIString (LanguageStrings.tags,
                                            LanguageStrings.actions,
                                            LanguageStrings.newtag,
                                            LanguageStrings.errors,
                                            LanguageStrings.novalue);
            //"Please enter a new tag.";

        }

        Set<String> ntags = this.getTags (v);

        if (ntags.size () == 0)
        {

            return Environment.getUIString (LanguageStrings.tags,
                                            LanguageStrings.actions,
                                            LanguageStrings.newtag,
                                            LanguageStrings.errors,
                                            LanguageStrings.novalue);
            //"Please enter a new tag.";

        }

        return null;

    }

    private Set<String> getTags (String v)
    {
        
        Set<String> ret = new LinkedHashSet<> ();

        StringTokenizer t = new StringTokenizer (v.trim (),
                                                 ";,");

        while (t.hasMoreTokens ())
        {

            ret.add (t.nextToken ().trim ());

        }

        return ret;

    }

    @Override
    public boolean onConfirm (String v)
                              throws Exception
    {

        Set<String> tags = this.getTags (v);

        for (String s : tags)
        {

            Tag ot = null;

            try
            {

                ot = Environment.getTagByName (s);

            } catch (Exception e) {

                Environment.logError ("Unable to get tag for name: " +
                                      s,
                                      e);

                continue;

            }

            if (ot != null)
            {

                continue;

            }

            Tag tag = new Tag ();
            tag.setName (s);

            try
            {

                Environment.saveTag (tag);

            } catch (Exception e) {

                Environment.logError ("Unable to add tag: " +
                                      tag,
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          Environment.getUIString (LanguageStrings.tags,
                                                                   LanguageStrings.actions,
                                                                   LanguageStrings.newtag,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to add tag.");

                return false;

            }

            this.obj.addTag (tag);

        }

        try
        {

            this.viewer.saveObject (this.obj,
                                    false);

        } catch (Exception e) {

            Environment.logError ("Unable to update object: " +
                                  this.obj,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.tags,
                                                               LanguageStrings.actions,
                                                               LanguageStrings.newtag,
                                                               LanguageStrings.actionerror));
                                      //"Unable to add tags.");

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
