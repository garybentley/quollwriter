package com.quollwriter.ui;

import java.awt.Color;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class CharacterDetailsEditPanel extends DetailsEditPanel
{

    private TextArea aliasesEdit = null;

    public CharacterDetailsEditPanel (Asset         a,
                                      ProjectViewer pv)
    {

        super (a,
               pv);

        this.aliasesEdit = new TextArea (this.getEditHelpText (),
                                         2,
                                         -1);
        this.aliasesEdit.setSpellCheckEnabled (false);
        this.aliasesEdit.setAutoGrabFocus (false);

    }

    @Override
    public boolean hasChanges ()
    {

        QCharacter character = (QCharacter) this.object;

        String as = character.getAliases ();

        if (as == null)
        {

            as = "";

        }

        return !this.aliasesEdit.getText ().equals (as);

    }

    public Set<String> getObjectChangeEventTypes ()
    {

        Set<String> types = new HashSet ();
        types.add (NamedObject.ALIASES);

        return types;

    }

    public String getEditHelpText ()
    {

        return "Use semicolons, commas or new lines to separate aliases.";

    }

    public void fillForSave ()
    {

        QCharacter character = (QCharacter) this.object;

        String al = this.aliasesEdit.getText ();

        if ((al != null)
            &&
            (al.trim ().length () > 0)
           )
        {

            // Split the aliases.
            StringTokenizer t = new StringTokenizer (al,
                                                     ",;" + String.valueOf ('\n'));

            StringBuilder b = new StringBuilder ();

            while (t.hasMoreTokens ())
            {

                if (b.length () > 0)
                {

                    b.append (String.valueOf ('\n'));

                }

                String tok = t.nextToken ().trim ();

                b.append (tok);

            }

            al = b.toString ();

        } else {

            al = null;

        }

        // Save the aliases.
        character.setAliases (al);

    }

    public void fillForEdit ()
    {

        QCharacter character = (QCharacter) this.object;

        String aliases = character.getAliases ();

        if (aliases == null)
        {

            aliases = "";

        }

        this.aliasesEdit.setTextWithMarkup (new StringWithMarkup (aliases));

    }

    @Override
    public boolean canSave ()
    {

        return true;

    }

    public List<FormItem> getExtraEditItems (ActionListener onSave)
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Aliases",
                                 this.aliasesEdit));

        UIUtils.addDoActionOnReturnPressed (this.aliasesEdit,
                                            onSave);

        return items;

    }

    public String getViewDescription ()
    {

        String s = super.getViewDescription ();

        QCharacter c = (QCharacter) this.object;

        if (c.getAliases () != null)
        {

            List l = c.getAliasesAsList ();

            StringBuilder b = new StringBuilder ();

            if (l != null)
            {

                for (int i = 0; i < l.size (); i++)
                {

                    if (b.length () > 0)
                    {

                        b.append (", ");

                    }

                    b.append (l.get (i));

                }

            }

            s = "<b>Aliases: </b>" + b + "<br /><br />" + s;

        }

        if (s == null)
        {

            s = "<i>No description.</i>";

        }

        return s;

    }

    public List<FormItem> getExtraViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

}
