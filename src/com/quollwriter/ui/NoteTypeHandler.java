package com.quollwriter.ui;

import java.util.*;

import com.gentlyweb.properties.StringProperty;

import com.quollwriter.*;

import com.quollwriter.data.*;


public class NoteTypeHandler implements TypesHandler
{

    private ProjectViewer projectViewer = null;
    private Set<String>  types = new TreeSet ();
    private EditNoteTypes typesEditor = null;

    public NoteTypeHandler(ProjectViewer pv)
    {

        this.projectViewer = pv;

        String nt = Environment.getProperty (Constants.NOTE_TYPES_PROPERTY_NAME);

        StringTokenizer t = new StringTokenizer (nt,
                                                 "|");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            if (tok.equals (Note.EDIT_NEEDED_NOTE_TYPE))
            {
                
                continue;
                
            }
            
            this.types.add (tok);

        }

    }

    public void setTypesEditor (EditNoteTypes ed)
    {
        
        this.typesEditor = ed;
        
    }

    public int getUsedInCount (String type)
    {
        
        int c = 0;
        
        Set<Note> notes = this.projectViewer.getAllNotes ();

        for (Note nn : notes)
        {

            if (nn.getType ().equals (type))
            {

                c++;

            }

        }        

        return c;
        
    }

    public boolean removeType (String  type,
                               boolean reload)
    {

        List<Note> toSave = new ArrayList ();
/*
        // Change the type for all notes with the old type.
        Set<Note> notes = this.projectViewer.getAllNotes ();

        for (Note nn : notes)
        {

            if (nn.getType ().equals (type))
            {

                nn.setType (null);

                toSave.add (nn);

            }

        }

        if (toSave.size () > 0)
        {

            try
            {

                this.projectViewer.saveObjects (toSave,
                                                true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save notes: " +
                                      toSave +
                                      " for removing type: " +
                                      type,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Unable to change type");

                return false;

            }

        }
*/
        this.types.remove (type);

        this.saveTypes ();

        if (reload)
        {

            this.projectViewer.reloadNoteTree ();

        }

        if (this.typesEditor != null)
        {
            
            this.typesEditor.reloadTypes ();
            
        }

        return true;

    }

    public void addType (String  t,
                         boolean reload)
    {

        if (this.types.contains (t))
        {

            if (reload)
            {

                this.projectViewer.reloadNoteTree ();

            }

            return;

        }

        this.types.add (t);

        this.saveTypes ();

        if (reload)
        {

            this.projectViewer.reloadNoteTree ();

        }

        if (this.typesEditor != null)
        {
            
            this.typesEditor.reloadTypes ();
            
        }

    }

    public Set<String> getTypesFromNotes ()
    {
        
        Set<String> types = new TreeSet ();
        
        Set<Note> notes = this.projectViewer.getAllNotes ();

        for (Note nn : notes)
        {

            types.add (nn.getType ());
            
        }
        
        return types;
        
    }

    public Set<String> getTypes ()
    {

        return new TreeSet<String> (this.types);

    }

    private void saveTypes ()
    {

        StringBuilder sb = new StringBuilder ();

        for (String s : this.types)
        {

            if (sb.length () > 0)
            {

                sb.append ("|");

            }

            sb.append (s);

        }

        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();

        StringProperty p = new StringProperty (Constants.NOTE_TYPES_PROPERTY_NAME,
                                               sb.toString ());
        p.setDescription ("N/A");

        props.setProperty (Constants.NOTE_TYPES_PROPERTY_NAME,
                           p);

        try
        {

            Environment.saveUserProperties (props);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user properties for: " +
                                  Constants.NOTE_TYPES_PROPERTY_NAME +
                                  " property with value: " +
                                  sb.toString (),
                                  e);

        }

    }

    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload)
    {

        List<Note> toSave = new ArrayList ();

        // Change the type for all notes with the old type.
        Set<Note> notes = this.projectViewer.getAllNotes ();

        for (Note nn : notes)
        {

            if (nn.getType ().equals (oldType))
            {

                nn.setType (newType);

                toSave.add (nn);

            }

        }

        if (toSave.size () > 0)
        {

            try
            {

                this.projectViewer.saveObjects (toSave,
                                                true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save notes: " +
                                      toSave +
                                      " with new type: " +
                                      newType,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Unable to change type");

                return false;

            }

        }

        this.removeType (oldType,
                         false);
        this.addType (newType,
                      false);

        if (reload)
        {

            this.projectViewer.reloadNoteTree ();

        }

        return true;

    }

}
