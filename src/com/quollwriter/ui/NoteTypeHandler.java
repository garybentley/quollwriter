package com.quollwriter.ui;

import java.util.*;

import com.gentlyweb.properties.StringProperty;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.db.*;
import com.quollwriter.data.comparators.*;

public class NoteTypeHandler implements TypesHandler<Note>
{

    private AbstractProjectViewer projectViewer = null;
    private Set<String>  types = new TreeSet ();
    private EditNoteTypes typesEditor = null;
    private ObjectProvider<Note> objectProvider = null;
    
    public NoteTypeHandler (AbstractProjectViewer pv,
                            ObjectProvider<Note>  objProv)
    {

        this.projectViewer = pv;
        this.objectProvider = objProv;
        
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

    public boolean typesEditable ()
    {
        
        return true;
        
    }
    
    public void setTypesEditor (EditNoteTypes ed)
    {
        
        this.typesEditor = ed;
        
    }

    public int getUsedInCount (String type)
    {
        
        int c = 0;
        
        Set<Note> notes = this.objectProvider.getAll ();

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

            this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

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

                this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

            }

            return;

        }

        this.types.add (t);

        this.saveTypes ();

        if (reload)
        {

            this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        }

        if (this.typesEditor != null)
        {
            
            this.typesEditor.reloadTypes ();
            
        }

    }
/*
    public Set<Note> getNotesForType (String t)
    {
        
        Set<Note> notes = this.objectProvider.getAll ();
        
        Set<Note> ret = new TreeSet (new ChapterItemSorter ());

        for (Note n : notes)
        {

            if (n.getType ().equals (t))
            {

                ret.add (n);

            }

        }

        return ret;
        
    }    
  */  
    public Set<Note> getObjectsForType (String    t)
    {
        
        Set<Note> notes = this.objectProvider.getAll ();
        
        Set<Note> ret = new TreeSet (new ChapterItemSorter ());

        for (Note n : notes)
        {

            if (n.getType ().equals (t))
            {

                ret.add (n);

            }

        }

        return ret;
        
    }    
/*
    public Set<String> getTypesFromNotes ()
    {
        
        Set<String> types = new TreeSet ();
        
        Set<Note> notes = this.objectProvider.getAll ();

        for (Note nn : notes)
        {

            types.add (nn.getType ());
            
        }
        
        return types;
        
    }
*/
    public Set<String> getTypesFromObjects ()
    {
        
        Set<Note> notes = this.objectProvider.getAll ();

        Set<String> types = new TreeSet ();
        
        for (Note nn : notes)
        {

            types.add (nn.getType ());
            
        }
        
        return types;
        
    }

    public Map<String, Set<Note>> getObjectsAgainstTypes ()
    {
 
        // The implementation here is pretty inefficient but we can get away with it due to the generally
        // low number of types and notes.
        
        // Might be worthwhile putting a josql wrapper around this for the grouping.
 
        Map<String, Set<Note>> ret = new LinkedHashMap ();
 
        Set<Note> notes = this.objectProvider.getAll ();

        Set<String> types = this.getTypesFromObjects ();
        
        for (String type : types)
        {

            for (Note n : notes)
            {

                String t = n.getType ();
                
                if (t.equals (type))
                {
                    
                    Set<Note> retNotes = ret.get (t);
                    
                    if (retNotes == null)
                    {
                        
                        retNotes = new TreeSet (new ChapterItemSorter ());
                        
                        ret.put (t,
                                 retNotes);
                        
                    }
                    
                    retNotes.add (n);
                    
                }

            }        

        }
        
        return ret;
        
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
        Set<Note> notes = this.objectProvider.getAll ();

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

                this.objectProvider.saveAll (toSave);

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

            this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        }

        return true;

    }

}
