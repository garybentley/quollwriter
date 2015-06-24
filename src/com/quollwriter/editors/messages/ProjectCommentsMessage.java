package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

/**
 */
public class ProjectCommentsMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "project-comments";
        
    private Set<Note> comments = null;
    private String generalComment = null;
    private ProjectVersion projVer = null;
    
    public ProjectCommentsMessage ()
    {
                        
    }
    
    public ProjectCommentsMessage (Project        project,
                                   String         genComment,
                                   Set<Note>      comments,
                                   ProjectVersion pv,
                                   EditorEditor   editor)
    {
                
        if ((comments == null)
            ||
            (comments.size () == 0)
           )
        {
            
            throw new IllegalArgumentException ("Expected at least 1 note.");
                
        }
        
        if (pv == null)
        {
            
            throw new IllegalArgumentException ("Expected a project version.");
            
        }
        
        if (pv.getId () == null)
        {
            
            throw new IllegalArgumentException ("Project version must have an id.");
            
        }

        this.projVer = pv;
        
        this.setEditor (editor);
        this.setForProjectName (project.getName ());
        this.setForProjectId (project.getId ());
        this.comments = comments;
        this.generalComment = genComment;
        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "versionName",
                                    this.projVer.getName ());
        this.addToStringProperties (props,
                                    "versionId",
                                    this.projVer.getId ());
 
        this.addToStringProperties (props,
                                    "generalComment",
                                    this.generalComment);
        this.addToStringProperties (props,
                                    "comments",
                                    this.comments.size ());
                
    }
                
    public ProjectVersion getProjectVersion ()
    {
        
        return this.projVer;
                
    }
    
    public String getGeneralComment ()
    {
        
        return this.generalComment;        
                
    }
    
    public Set<Chapter> getChapters ()
    {
        
        Set<Chapter> chaps = new LinkedHashSet ();
        
        for (Note n : this.comments)
        {
            
            Chapter c = n.getChapter ();
            
            if (chaps.contains (c))
            {
                
                continue;
                
            }
            
            chaps.add (c);
            
        }
        
        return chaps;
        
    }

    public Set<Note> getComments ()
    {
        
        return this.comments;
            
    }
    
    protected void fillMap (Map data)
                     throws GeneralException
    {
        
        if (this.getForProjectId () == null)
        {
            
            throw new GeneralException ("No project id set");
            
        }
            
        if (this.generalComment != null)
        {
        
            data.put (MessageFieldNames.generalcomment,
                      this.generalComment);
            
        }
        
        if ((this.comments == null)
            ||
            (this.comments.size () == 0)
           )
        {
            
            throw new GeneralException ("No notes set");
                                
        }
        
        data.put (MessageFieldNames.versionid,
                  this.projVer.getId ());
        
        if (this.projVer.getName () != null)
        {
            
            data.put (MessageFieldNames.versionname,
                      this.projVer.getName ());
            
        }
        
        List clist = new ArrayList ();
        
        data.put (MessageFieldNames.comments,
                  clist);
        
        for (Note n : this.comments)
        {
            
            clist.add (TypeEncoder.encode (n));
            
        }        
        
    }
        
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
                                
        this.generalComment = this.getString (MessageFieldNames.generalcomment,
                                              data,
                                              false);
                                
        String projVerId = this.getString (MessageFieldNames.versionid,
                                           data);
        String projVerName = this.getString (MessageFieldNames.versionname,
                                             data,
                                             false);
        
        // Get the project version.
        ProjectVersion pv = new ProjectVersion ();
        pv.setId (projVerId);
        pv.setName (projVerName);
        
        this.projVer = pv;
                                
        Object cms = this.checkTypeAndNotNull (MessageFieldNames.comments,
                                               data,
                                               List.class);
        
        List commsL= (List) cms;
        
        if (commsL.size () == 0)
        {
            
            throw new GeneralException ("Expected to find at least 1 comment.");
            
        }
        
        Set<Note> comments = new LinkedHashSet ();
        
        for (int i = 0; i < commsL.size (); i++)
        {
            
            Object co = commsL.get (i);
            
            if (!(co instanceof Map))
            {
                
                throw new GeneralException ("Expected comment data for index: " +
                                            i +
                                            " to by a map, has type: " +
                                            co.getClass ().getName ());
                
            }
            
            Map cm = (Map) co;

            Note n = TypeEncoder.decodeToNote (cm);
            
            comments.add (n);
            
        }
        
        this.comments = comments;
        
    }
        
    public String getMessage ()
                       throws GeneralException
    {
        
        // Return a summary, should be json.
        Map m = new HashMap ();
        m.put (MessageFieldNames.projectid,
               this.getForProjectId ());
        
        if (this.generalComment != null)
        {
            
            m.put (MessageFieldNames.generalcomment,
                   this.generalComment);
            
        }
        
        m.put (MessageFieldNames.versionid,
               this.projVer.getId ());
        
        if (this.projVer.getName () != null)
        {
            
            m.put (MessageFieldNames.versionname,
                   this.projVer.getName ());
            
        }

        List cms = new ArrayList ();
        
        m.put (MessageFieldNames.comments,
               cms);
        
        for (Note n : this.comments)
        {
            
            cms.add (TypeEncoder.encode (n));
            
        }
                        
        try
        {
        
            return JSONEncoder.encode (m);
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to json encode message: " +
                                        m,
                                        e);
            
        }
        
    }

    public void setMessage (String m)
                     throws GeneralException
    {
        
        // This is a summary of what was sent/received.
        Map data = (Map) JSONDecoder.decode (m);
        
        this.setForProjectId (this.getString (MessageFieldNames.projectid,
                                              data));
        
        ProjectVersion pv = new ProjectVersion ();
        pv.setId (this.getString (MessageFieldNames.versionid,
                                  data));
        pv.setName (this.getString (MessageFieldNames.versionname,
                                    data,
                                    false));

        this.projVer = pv;
                                    
        this.generalComment = this.getString (MessageFieldNames.generalcomment,
                                              data,
                                              false);
        
        Set<Note> comms = null;
        
        List cms = (List) data.get (MessageFieldNames.comments);
        
        if (cms != null)
        {
                
            comms = new LinkedHashSet ();
                
            for (int i = 0; i < cms.size (); i++)
            {
            
                Map cm = (Map) cms.get (i);

                Note n = TypeEncoder.decodeToNote (cm);
                
                comms.add (n);
                                              
            }
            
        }
        
        this.comments = comms;
                        
    }
    
    public boolean isEncrypted ()
    {
        
        return true;
        
    }
    
    public String getMessageType ()
    {
        
        return MESSAGE_TYPE;
        
    }
    
    
}