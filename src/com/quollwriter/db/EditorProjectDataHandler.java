package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

public class EditorProjectDataHandler implements DataHandler<EditorProject, NamedObject>
{

    private ObjectManager objectManager = null;

    public EditorProjectDataHandler (ObjectManager om)
    {

        this.objectManager = om;

    }

    private String getAsString (Set<String> items)
    {
        
        if ((items == null)
            ||
            (items.size () == 0)
           )
        {
            
            return null;
            
        }
        
        StringBuilder b = new StringBuilder ();
        
        for (String g : items)
        {
            
            if (b.length () > 0)
            {
                
                b.append (",");
                
            }
            
            b.append (g);
            
        }
        
        return b.toString ();
        
    }
    
    @Override
    public void createObject (EditorProject p,
                              Connection    conn)
                       throws GeneralException
    {
        
        List params = new ArrayList ();
        params.add (p.getKey ());
        params.add (p.getId ());
        params.add (this.getAsString (p.getGenres ()));
        params.add (p.getExpectations ());
        params.add (p.getWordCountLength ().getType ());
        
        this.objectManager.executeStatement ("INSERT INTO editorproject (dbkey, id, genres, expectations, wordcounttypelength) VALUES (?, ?, ?, ?, ?)",
                                             params,
                                             conn);        
        
    }

    @Override
    public void deleteObject (EditorProject d,
                              boolean       deleteChildObjects,
                              Connection    conn)
                       throws GeneralException
    {
    
        List params = new ArrayList ();
        params.add (d.getKey ());
    
        this.objectManager.executeStatement ("DELETE FROM editorproject WHERE dbkey = ?",
                                             params,
                                             conn);
        
    }

    @Override
    public void updateObject (EditorProject p,
                              Connection    conn)
                       throws GeneralException
    {
        
        List params = new ArrayList ();
        params.add (p.getId ());
        params.add (this.getAsString (p.getGenres ()));
        params.add (p.getExpectations ());
        params.add (p.getWordCountLength ().getType ());
        params.add (p.getKey ());
        
        this.objectManager.executeStatement ("UPDATE editorproject SET id = ?, genres = ?, expectations = ?, wordcounttypelength = ? WHERE dbkey = ?",
                                             params,
                                             conn);
        
    }

    @Override
    public List<EditorProject> getObjects (NamedObject parent,
                                           Connection  conn,
                                           boolean     loadChildObjects)
                                    throws GeneralException
    {
        
        throw new UnsupportedOperationException ("Not supported");        
        
    }

    private Set<String> split (String s,
                               String sep)
    {
        
        Set<String> ret = new LinkedHashSet ();
        
        StringTokenizer t = new StringTokenizer (s,
                                                 sep);
        
        while (t.hasMoreTokens ())
        {
            
            ret.add (t.nextToken ().trim ());
            
        }
        
        return ret;
        
    }
    
    private EditorProject getEditorProject (ResultSet rs)
                               throws GeneralException
    {

        try
        {

            int ind = 1;

            long key = rs.getLong (ind++);

            EditorProject p = new EditorProject ();

            p.setKey (key);
            p.setId (rs.getString (ind++));
            Environment.logMessage ("GOT PROJ ID AT LOAD: " + p.getId ());
            p.setName (rs.getString (ind++));
            
            String genres = rs.getString (ind++);
            
            p.setGenres (this.split (genres,
                                     ","));
            
            p.setExpectations (rs.getString (ind++));
            
            String wcLength = rs.getString (ind++);
            
            p.setWordCountLength (EditorProject.WordCountLength.getWordCountLengthByType (wcLength));
            
            p.setDescription (new StringWithMarkup (rs.getString (ind++)));

            p.setLastModified (rs.getTimestamp (ind++));
            p.setDateCreated (rs.getTimestamp (ind++));
            p.setPropertiesAsString (rs.getString (ind++));

            return p;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load editor project",
                                        e);

        }
            
    }
    
    @Override
    public EditorProject getObjectByKey (int        key,
                                         NamedObject parent,
                                         Connection conn,
                                         boolean    loadChildObjects)
                                  throws GeneralException
    {
        
        ResultSet rs = null;

        try
        {
        
            rs = this.objectManager.executeQuery ("SELECT dbkey, id, name, genres, expectations, wordcounttypelength, description, lastmodified, datecreated, properties FROM editorproject_v",
                                                  null,
                                                  conn);

            if (rs.next ())
            {

                EditorProject p = this.getEditorProject (rs);

                return p;

            }

            return null;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load editor project",
                                        e);

        } finally
        {

            try
            {

                if (rs != null)
                {

                    rs.close ();

                }

            } catch (Exception e)
            {

            }

        }
        
    }

}
