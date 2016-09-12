package com.quollwriter.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.components.*;

public class ObjectDetailsEditPanel extends DetailsEditPanel
{

    public static final String DEFAULT_TYPE = "Enter a new Type";

    private JComboBox types = null;

    public ObjectDetailsEditPanel (Asset         a,
                                   ProjectViewer pv)
    {

        super (a,
               pv);

        this.types = new JComboBox ();
        this.types.setEditable (true);

        this.types.setMaximumSize (this.types.getPreferredSize ());
        this.types.setToolTipText ("Add a new Type by entering a value in the field.");

    }

    @Override
    public boolean hasChanges ()
    {

        QObject o = (QObject) this.object;

        String t = o.getType ();

        if (t == null)
        {

            t = "";

        }

        return !this.types.getEditor ().getItem ().toString ().equals (t);


    }

    public Set<String> getObjectChangeEventTypes ()
    {

        Set<String> types = new HashSet ();
        types.add (QObject.TYPE);

        return types;

    }

    public String getEditHelpText ()
    {

        return "Add a new item type by entering a value in the Type field.";

    }

    public void fillForSave ()
    {

        QObject o = (QObject) this.object;

        String t = null;

        if (this.types.getEditor ().getItem () != null)
        {

            t = this.types.getEditor ().getItem ().toString ();

        } else
        {

            t = this.types.getSelectedItem ().toString ();

        }

        t.trim ();

        if (!t.equals (ObjectDetailsEditPanel.DEFAULT_TYPE))
        {

            o.setType (t);

            // Add the type.
            Environment.getUserPropertyHandler (Constants.OBJECT_TYPES_PROPERTY_NAME).addType (t,
                                                                                               true);

            Map nTypes = new HashMap ();

            StringBuilder sb = new StringBuilder ();

            for (int i = 0; i < this.types.getItemCount (); i++)
            {

                String v = this.types.getItemAt (i).toString ().trim ();

                if (v.equals (ObjectDetailsEditPanel.DEFAULT_TYPE))
                {

                    continue;

                }

                if (v.equals (""))
                {

                    continue;

                }

                if (!nTypes.containsKey (v.toLowerCase ()))
                {

                    if (sb.length () > 0)
                    {

                        sb.append ("|");

                    }

                    sb.append (v);

                    nTypes.put (v.toLowerCase (),
                                "");

                }

            }

            String item = this.types.getEditor ().getItem ().toString ().trim ();

            if ((item.length () > 0) &&
                (!nTypes.containsKey (item.toLowerCase ())))
            {

                sb.append ("|" + item);

            }

            UserProperties.set (Constants.OBJECT_TYPES_PROPERTY_NAME,
                                sb.toString ());

        }

    }

    public void fillForEdit ()
    {

        // Get the pre-defined types, they are stored in the user prefs.
        String nt = Environment.getProperty (Constants.OBJECT_TYPES_PROPERTY_NAME);

        Vector ts = new Vector ();

        if (nt != null)
        {

            StringTokenizer t = new StringTokenizer (nt,
                                                     "|");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                if (!ts.contains (tok))
                {

                    ts.add (tok);

                }

            }

        }

        Collections.sort (ts);

        if (ts.size () == 0)
        {

            ts.add (ObjectDetailsEditPanel.DEFAULT_TYPE);

        }

        this.types.setModel (new DefaultComboBoxModel (ts));

        QObject o = (QObject) this.object;

        if (o.getType () != null)
        {

            this.types.setSelectedItem (o.getType ());

        }

    }

    public boolean canSave ()
    {

        return true;

    }

    public List<FormItem> getExtraEditItems (ActionListener onSave)
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Type",
                                 this.types));

        return items;

    }

    public List<FormItem> getExtraViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

    public String getViewDescription ()
    {

        String s = super.getViewDescription ();

        QObject o = (QObject) this.object;

        String type = o.getType ();

        if ((type != null)
            &&
            (type.trim ().length () > 0)
           )
        {

            s = "<b>Type: </b>" + type + "<br /><br />" + s;

        }

        if (s == null)
        {

            s = "<i>No description.</i>";

        }

        return s;

    }

}
