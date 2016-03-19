package com.quollwriter.db;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.imageio.*;
import java.awt.image.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.bc.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;

public class ProjectInfoObjectManager extends ObjectManager
{

    public ProjectInfoObjectManager ()
    {
        
        this.handlers.put (ProjectInfo.class,
                           new ProjectInfoDataHandler (this));
        
    }

    public void init (File   dir,
                      String username,
                      String password,
                      String filePassword,
                      int    newSchemaVersion)
               throws GeneralException
    {

        super.init (dir,
                    username,
                    password,
                    filePassword,
                    newSchemaVersion);

    }

    public void updateLinks (NamedObject d,
                             Set<Link>   newLinks)
    {
        
    }

    public void deleteLinks (NamedObject n,
                             Connection  conn)
    {
        
    }
    
    public void getLinks (NamedObject d,
                          Project     p,
                          Connection  conn)
    {
        
    }
    
    public void updateSchemaVersion (int        newVersion,
                                     Connection conn)
                              throws Exception
    {

        List params = new ArrayList ();
        params.add (newVersion);

        this.executeStatement ("UPDATE info SET schema_version = ?",
                               params,
                               conn);
        
    }
    
    public int getSchemaVersion ()
                          throws GeneralException    
    {
        
        Connection c = null;

        try
        {

            c = this.getConnection ();

            PreparedStatement ps = c.prepareStatement ("SELECT schema_version FROM info");

            ResultSet rs = ps.executeQuery ();

            if (rs.next ())
            {

                return rs.getInt (1);

            }
            
        } catch (Exception e)
        {

            this.throwException (c,
                                 "Unable to get schema version",
                                 e);
                
        } finally
        {

            this.releaseConnection (c);

        }
            
        return -1;
        
        
    }
            
    public String getSchemaFile (String file)
    {
        
        return Constants.PROJECT_INFO_SCHEMA_DIR + file;
        
    }
    
    public String getCreateViewsFile ()
    {
        
        return Constants.PROJECT_INFO_UPDATE_SCRIPTS_DIR + "/create-views.xml";
        
    }
    
    public String getUpgradeScriptFile (int oldVersion,
                                        int newVersion)
    {
        
        return Constants.PROJECT_INFO_UPDATE_SCRIPTS_DIR + "/" + oldVersion + "-" + newVersion + ".xml";
        
    }
 
    public void addSession (Session s)
                     throws GeneralException
    {
    
        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }

        try
        {
        
            List params = new ArrayList ();
            params.add (s.getStart ());
            params.add (s.getEnd ());
            params.add (s.getWordCount ());
    
            this.executeStatement ("INSERT INTO session (start, end, wordcount) VALUES (?, ?, ?)",
                                   params,
                                   conn);

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to save session: " +
                                 s,
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }
        
    }
 
    public List<Session> getSessions (int     daysPast)
                               throws GeneralException
    {

        Connection conn = null;
    
        try
        {

            conn = this.getConnection ();

        } catch (Exception e) {
            
            this.throwException (null,
                                 "Unable to get connection",
                                 e);
            
        }
                        
        try
        {
            
            List params = new ArrayList ();

            String whereDays = "";

            // 0 means today.
            // -1 means all time
            // 1 means yesterday
            if (daysPast > -1)
            {

                whereDays = " AND start >= DATEADD ('DAY', ?, CURRENT_DATE) ";
                params.add (-1 * daysPast);

            }

            ResultSet rs = this.executeQuery (String.format ("SELECT start, end, wordcount FROM session WHERE 1 = 1 %s ORDER BY start",
                                                             whereDays),
                                              params,
                                              conn);

            List<Session> ret = new ArrayList ();

            Session last = null;
            
            while (rs.next ())
            {

                int ind = 1;

                Session s = new Session (rs.getTimestamp (ind++), // start
                                         rs.getTimestamp (ind++), // end
                                         rs.getInt (ind++)); // word count

                if (last != null)
                {
                    
                    // If the time difference less than 2s?
                    if ((s.getStart ().getTime () - last.getEnd ().getTime ()) < 2 * Constants.SEC_IN_MILLIS)
                    {
                        
                        Session _s = new Session (last.getStart (),
                                                  s.getEnd (),
                                                  s.getWordCount () + last.getWordCount ());
                        
                        // Merge the two together.
                        ret.remove (last);

                        s = _s;
                                                                 
                    }
                    
                }
                
                last = s;
                                         
                ret.add (s);

            }

            return ret;

        } catch (Exception e)
        {

            this.throwException (conn,
                                 "Unable to load sessions",
                                 e);

        } finally {
            
            this.releaseConnection (conn);
            
        }
        
        return null;

    }
    
}