package com.quollwriter.ui;

import java.util.*;

import com.gentlyweb.properties.StringProperty;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.db.*;


public class ItemTypeHandler implements TypesHandler<QObject>
{

    private AbstractProjectViewer projectViewer = null;
    private Set<String>  types = new TreeSet ();
    private TypesEditor typesEditor = null;
    private ObjectProvider<QObject> objectProvider = null;

    public ItemTypeHandler (AbstractProjectViewer    pv,
                            ObjectProvider<QObject>  objProv)

    {

        this.projectViewer = pv;
        this.objectProvider = objProv;
        
        String nt = Environment.getProperty (Constants.OBJECT_TYPES_PROPERTY_NAME);

        if (nt != null)
        {

            StringTokenizer t = new StringTokenizer (nt,
                                                     "|");
    
            while (t.hasMoreTokens ())
            {
    
                String tok = t.nextToken ().trim ();
                
                this.types.add (tok);
    
            }

        }

    }

    public boolean typesEditable ()
    {
        
        return true;
        
    }
    
    public void setTypesEditor (TypesEditor ed)
    {
        
        this.typesEditor = ed;
        
    }

    public Set<QObject> getObjectsForType (String    t)
    {
        
        Set<QObject> objs = this.objectProvider.getAll ();
        
        Set<QObject> ret = new LinkedHashSet ();

        for (QObject o : objs)
        {

            if (o.getType ().equals (t))
            {

                ret.add (o);

            }

        }

        return ret;
        
    }        
    
    public Map<String, Set<QObject>> getObjectsAgainstTypes ()
    {
 
        // The implementation here is pretty inefficient but we can get away with it due to the generally
        // low number of types and qobjects.
        
        // Might be worthwhile putting a josql wrapper around this for the grouping.
 
        Map<String, Set<QObject>> ret = new LinkedHashMap ();
 
        Set<QObject> objs = this.objectProvider.getAll ();

        Set<String> types = this.getTypesFromObjects ();
        
        for (String type : types)
        {

            for (QObject o : objs)
            {

                String t = o.getType ();
                
                if (t.equals (type))
                {
                    
                    Set<QObject> retObjs = ret.get (t);
                    
                    if (retObjs == null)
                    {
                        
                        retObjs = new LinkedHashSet ();
                        
                        ret.put (t,
                                 retObjs);
                        
                    }
                    
                    retObjs.add (o);
                    
                }

            }        

        }
        
        return ret;
        
    }

    public Set<String> getTypesFromObjects ()
    {
        
        Set<QObject> objs = this.objectProvider.getAll ();

        Set<String> types = new TreeSet ();
        
        for (QObject o : objs)
        {

            types.add (o.getType ());
            
        }
        
        return types;
        
    }
    
    public int getUsedInCount (String type)
    {
        
        int c = 0;
        /*
        Set<Note> notes = this.projectViewer.getAllNotes ();

        for (Note nn : notes)
        {

            if (nn.getType ().equals (type))
            {

                c++;

            }

        }        
        */
        return c;
        
    }

    public boolean removeType (String  type,
                               boolean reload)
    {

        this.types.remove (type);

        this.saveTypes ();

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

            return;

        }

        this.types.add (t);

        this.saveTypes ();

        if (this.typesEditor != null)
        {
            
            this.typesEditor.reloadTypes ();
            
        }

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

        StringProperty p = new StringProperty (Constants.OBJECT_TYPES_PROPERTY_NAME,
                                               sb.toString ());
        p.setDescription ("N/A");

        props.setProperty (Constants.OBJECT_TYPES_PROPERTY_NAME,
                           p);

        try
        {

            Environment.saveUserProperties (props);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user properties for: " +
                                  Constants.OBJECT_TYPES_PROPERTY_NAME +
                                  " property with value: " +
                                  sb.toString (),
                                  e);

        }

    }

    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload)
    {

        List<QObject> toSave = new ArrayList ();

        // Change the type for all notes with the old type.
        List<QObject> objs = this.projectViewer.getProject ().getQObjects ();

        for (QObject o : objs)
        {

            if (o.getType ().equals (oldType))
            {

                o.setType (newType);

                toSave.add (o);

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

                Environment.logError ("Unable to save qobjects: " +
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

        return true;

    }

}
