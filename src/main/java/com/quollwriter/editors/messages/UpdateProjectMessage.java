package com.quollwriter.editors.messages;

import java.util.*;
import java.awt.image.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

/**
 * Note for performance reasons the chapter text is not stored when {@link getMessage()} is called.
 * To get the chapter text call : {@link getChaptersWithText()} which will do a lookup of the original
 * blob, decode the message and create chapters from there.  The performance hit is greater but lower
 * in the longer term since the chapter text is only needed occasionally.
 */
public class UpdateProjectMessage extends AbstractProjectMessage
{
    
    public static final String MESSAGE_TYPE = "project-update";
        
    private boolean accepted = false;
    private String responseMessage = null;
    private Date responseDate = null;
    
    public UpdateProjectMessage ()
    {
                        
    }
    
    public UpdateProjectMessage (Project        project,
                                 Set<Chapter>   chapters,
                                 ProjectVersion pv,
                                 EditorEditor   editor)
    {
                
        super (project,
               chapters,
               pv,
               editor);
        /*
                
        if ((chapters == null)
            ||
            (chapters.size () == 0)
           )
        {
            
            throw new IllegalArgumentException ("Expected at least 1 chapter.");
                
        }
        
        this.setEditor (editor);
        this.setForProjectName (project.getName ());
        this.setForProjectId (project.getId ());
        this.versionName = versionName;
        this.chapters = chapters;
        this.notes = notes;
        this.dueBy = dueBy;
        
        int wc = 0;
        
        for (Chapter c : chapters)
        {
        
            wc += TextUtilities.getWordCount (c.getText ());
        
        }
        
        this.wordCount = wc;
          */      
    }
        
    private boolean hasChapterText ()
    {
                
        for (Chapter c : this.chapters)
        {
            
            if (c.getText () != null)
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
        
    /**
     * Creates a new Project object from the data held within the message.
     */
    @Override
    public Project createProject ()
                           throws Exception
    {
        
        Set<Chapter> chaps = this.chapters;
        
        /*
        if (!this.hasChapterText ())
        {
            
            chaps = this.getChaptersWithText ();
            
        }
        */        
        chaps = this.getChaptersWithText ();

        Project proj = new Project ();
        proj.setType (Project.EDITOR_PROJECT_TYPE);
        proj.setId (this.getForProjectId ());
        proj.setName (this.getForProjectName ());
        proj.setForEditor (this.getEditor ().getEmail ());
       
        proj.setProjectVersion (this.getProjectVersion ());
       
        if (this.responseMessage != null)
        {
            
            proj.setEditResponseMessage (this.responseMessage);
       
        }
       
        Book b = new Book (proj,
                           proj.getName ());
        
        proj.addBook (b);
        
        for (Chapter c : chaps)
        {
            
            b.addChapter (c);
            
        }
       
        return proj; 
        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        if (this.responseDate != null)
        {
        
            this.addToStringProperties (props,
                                        "accepted",
                                        this.accepted);
            this.addToStringProperties (props,
                                        "responseMessage",
                                        this.responseMessage);
            this.addToStringProperties (props,
                                        "responseDate",
                                        this.responseDate);
        
        }
        
    }
                
    public boolean isAccepted ()
    {
        
        return this.accepted;        
        
    }
    
    public void setAccepted (boolean v)
    {
        
        this.accepted = v;
        this.responseDate = new Date ();
        
    }
        
    public Date getResponseDate ()
    {
        
        return this.responseDate;
        
    }
    
    public void setResponseMessage (String m)
    {
        
        this.responseMessage = m;
        
    }
    
    public String getResponseMessage ()
    {
        
        return this.responseMessage;
        
    }
/*        
    protected void fillMap (Map data)
                     throws Exception
    {
        
        if (this.getForProjectId () == null)
        {
            
            throw new GeneralException ("No project id set");
            
        }
            
        if ((this.chapters == null)
            ||
            (this.chapters.size () == 0)
           )
        {
            
            throw new GeneralException ("No chapters set");
                                
        }
        
        data.put (MessageFieldNames.name,
                  this.getForProjectName ());
        
        if ((this.notes != null)
            &&
            (this.notes.trim ().length () > 0)
           )
        {
        
            data.put (MessageFieldNames.notes,
                      this.notes.trim ());
            
        }
        
        if (this.dueBy != null)
        {
            
            data.put (MessageFieldNames.dueby,
                      this.dueBy.getTime ());
            
        }
        
        if (this.versionName != null)
        {
            
            data.put (MessageFieldNames.versionname,
                      this.versionName);
            
        }
        
        List clist = new ArrayList ();
        
        data.put (MessageFieldNames.chapters,
                  clist);
        
        // Add the chapters.
        for (Chapter c : this.chapters)
        {
            
            clist.add (TypeEncoder.encode (c));
            
        }        
                
    }
*/
/*
    private boolean hasChapterText ()
    {
                
        for (Chapter c : this.chapters)
        {
            
            if (c.getText () != null)
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
  */  
    /**
     * Creates a new Project object from the data held within the message.
     */
    /*
    public Project createProject ()
                           throws Exception
    {
        
        Set<Chapter> chaps = this.chapters;
        
        if (!this.hasChapterText ())
        {
            
            chaps = this.getChaptersWithText ();
            
        }
        
        Project proj = new Project ();
        proj.setType (Project.EDITOR_PROJECT_TYPE);
        proj.setId (this.getForProjectId ());
        proj.setName (this.getForProjectName ());
        proj.setDescription (this.notes);
        proj.setForEditor (this.getEditor ());
       
        if (this.dueBy != null)
        {
            
            // Set a property for the project.
            proj.setEditDueDate (this.dueBy);
            
        }
       
        if (this.responseMessage != null)
        {
            
            proj.setEditResponseMessage (this.responseMessage);
       
        }
       
        Book b = new Book (proj,
                           proj.getName ());
        
        proj.addBook (b);
        
        for (Chapter c : chaps)
        {
            
            b.addChapter (c);
            
        }
       
        return proj; 
        
    }
    */
    
    /*
    @Override
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {

        super.doInit (data,
                      from);
    
    }
    */
    /*
    public String getMessage ()
                       throws GeneralException
    {
        
        // Return a summary, should be json.
        Map m = new HashMap ();
        m.put (MessageFieldNames.name,
               this.getForProjectName ());
        m.put (MessageFieldNames.projectid,
               this.getForProjectId ());
        m.put (MessageFieldNames.chaptercount,
               this.chapters.size ());
        m.put (MessageFieldNames.notes,
               this.notes);
        m.put (MessageFieldNames.wordcount,
               this.wordCount);
               
        if (this.versionName != null)
        {
            
            m.put (MessageFieldNames.versionname,
                   this.versionName);
               
        }
               
        if (this.responseDate != null)
        {
            
            m.put (MessageFieldNames.accepted,
                   this.accepted);
            m.put (MessageFieldNames.responsedate,
                   this.responseDate.getTime ());
            
            if (this.responseMessage != null)
            {
                
                m.put (MessageFieldNames.responsemessage,
                       this.responseMessage);
                
            }

        }
        
        // Get a summary of each chapter.
        List chs = new ArrayList ();
        
        for (Chapter c : this.chapters)
        {
            
            Map cm = new HashMap ();
            chs.add (cm);
            
            cm.put (MessageFieldNames.name,
                    c.getName ());
            cm.put (MessageFieldNames.chapterid,
                    c.getId ());
            cm.put (MessageFieldNames.version,
                    c.getVersion ());
            
        }
                
        m.put (MessageFieldNames.chapters,
               chs);
                
        if (this.dueBy != null)
        {
        
            m.put (MessageFieldNames.dueby,
                   this.dueBy.getTime ());
            
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
    */
    @Override
    protected void fillMessageMap (Map<String, Object> m)
                            throws GeneralException    
    {
        
        if (this.responseDate != null)
        {
            
            m.put (MessageFieldNames.accepted,
                   this.accepted);
            m.put (MessageFieldNames.responsedate,
                   this.responseDate.getTime ());
            
            if (this.responseMessage != null)
            {
                
                m.put (MessageFieldNames.responsemessage,
                       this.responseMessage);
                
            }

        }                
        
    }
    
    @Override
    protected void fillFromMessageMap (Map data)
                                throws GeneralException    
    {
        
        this.responseDate = this.getDate (MessageFieldNames.responsedate,
                                          data,
                                          false);

        if (this.responseDate != null)
        {
            
            this.accepted = this.getBoolean (MessageFieldNames.accepted,
                                             data);
            
            this.responseMessage = this.getString (MessageFieldNames.responsemessage,
                                                   data,
                                                   false);

        }
        
    }
    /*
    public void setMessage (String m)
                     throws GeneralException
    {
        
        // This is a summary of what was sent/received.
        Map data = (Map) JSONDecoder.decode (m);
        
        this.setForProjectName (this.getString (MessageFieldNames.name,
                                                data));
        this.setForProjectId (this.getString (MessageFieldNames.projectid,
                                              data));
        this.notes = this.getString (MessageFieldNames.notes,
                                     data,
                                     false);
        this.versionName = this.getString (MessageFieldNames.versionname,
                                           data,
                                           false);
        this.wordCount = this.getInt (MessageFieldNames.wordcount,
                                      data);
               
        this.responseDate = this.getDate (MessageFieldNames.responsedate,
                                          data,
                                          false);

        if (this.responseDate != null)
        {
            
            this.accepted = this.getBoolean (MessageFieldNames.accepted,
                                             data);
            
            this.responseMessage = this.getString (MessageFieldNames.responsemessage,
                                                   data,
                                                   false);

        }
        
        Set<Chapter> chaps = null;
        
        List chs = (List) data.get (MessageFieldNames.chapters);
        
        if (chs != null)
        {
                
            chaps = new LinkedHashSet ();
                
            for (int i = 0; i < chs.size (); i++)
            {
            
                Map cm = (Map) chs.get (i);

                Chapter c = TypeEncoder.decodeToChapter (cm);
                
                chaps.add (c);
                                              
            }
            
        }
        
        this.chapters = chaps;
                
        this.dueBy = this.getDate (MessageFieldNames.dueby,
                                   data,
                                   false);
        
    }
    */    
    public String getMessageType ()
    {
        
        return MESSAGE_TYPE;
        
    }
    
    
}