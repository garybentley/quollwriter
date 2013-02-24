package com.quollwriter.ui;

import java.awt.Color;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class CharacterDetailsEditPanel extends DetailsEditPanel
{

    private JTextArea aliasesEdit = null;

    public CharacterDetailsEditPanel(Asset         a,
                                     ProjectViewer pv)
    {

        super (a,
               pv);

        this.aliasesEdit = UIUtils.createTextArea (2);
        
    }

    public String getEditHelpText ()
    {

        return "Use semi-colons, commas or new lines to separate aliases.";

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

        this.aliasesEdit.setText (character.getAliases ());

    }

    public boolean canSave ()
    {

        return true;

    }

    public List<FormItem> getExtraEditItems ()
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Aliases",
                                 this.aliasesEdit));

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
