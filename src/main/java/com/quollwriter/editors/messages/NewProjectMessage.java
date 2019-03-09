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
public class NewProjectMessage extends AbstractProjectMessage
{
    
    public static final String MESSAGE_TYPE = "project-new";
        
    //private String projectName = null;
    //private Set<Chapter> chapters = null;
    //private String notes = null;
    //private Date dueBy = null;
    //private int wordCount = 0;
    private boolean accepted = false;
    private String responseMessage = null;
    private Date responseDate = null;
    private String editorName = null;
    private BufferedImage editorAvatar = null;
    //private String versionName = null;
    
    public NewProjectMessage ()
    {
                        
    }
    
    public NewProjectMessage (Project        project,
                              Set<Chapter>   chapters,
                              ProjectVersion pv,
                              EditorEditor   editor,
                              EditorAccount  acc)
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
        if (acc != null)
        {
            
            this.editorName = acc.getName ();
            this.editorAvatar = acc.getAvatar ();
            
        }
        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "editorName",
                                    this.editorName);
        this.addToStringProperties (props,
                                    "hasEditorAvatar",
                                    (this.editorAvatar != null));
                
    }
        
/*            
    public Set<Chapter> getChaptersWithText ()
                                      throws Exception
    {
        
        Map data = EditorsEnvironment.getOriginalMessageAsMap (this);

        Object chaps = this.checkTypeAndNotNull (MessageFieldNames.chapters,
                                                 data,
                                                 List.class);
        
        List chapsL = (List) chaps;
        
        if (chapsL.size () == 0)
        {
            
            throw new GeneralException ("Expected to find at least 1 chapter.");
            
        }
        
        int wc = 0;
        
        Set<Chapter> chapsS = new LinkedHashSet ();
        
        for (int i = 0; i < chapsL.size (); i++)
        {
            
            Object co = chapsL.get (i);
            
            if (!(co instanceof Map))
            {
                
                throw new GeneralException ("Expected chapter data for index: " +
                                            i +
                                            " to by a map, has type: " +
                                            co.getClass ().getName ());
                
            }
            
            Map cm = (Map) co;

            Chapter c = TypeEncoder.decodeToChapter (cm);
            
            chapsS.add (c);
            
        }
        
        return chapsS;

    }
    */
/*
    public Set<Chapter> getChapters ()
    {
        
        return this.chapters;
            
    }
  */  
    public boolean isAccepted ()
    {
        
        return this.accepted;        
        
    }
    
    public void setAccepted (boolean v)
    {
        
        this.accepted = v;
        this.responseDate = new Date ();
        
    }
    
    public BufferedImage getEditorAvatar ()
    {
        
        return this.editorAvatar;
        
    }
    
    public String getEditorName ()
    {
        
        return this.editorName;
        
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
    public Date getDueBy ()
    {
        
        return this.dueBy;
        
    }
    
    public int getWordCount ()
    {
        
        return this.wordCount;
        
    }
    
    public String getVersionName ()
    {
        
        return this.versionName;
        
    }
    
    public String getNotes ()
    {
        
        return this.notes;
        
    }
    */
    
    @Override
    protected void fillMap (Map data)
                     throws Exception
    {
        
        super.fillMap (data);
        
/*        
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
                
        if (this.versionName != null)
        {
            
            data.put (MessageFieldNames.versionname,
                      this.versionName);
            
        }
        
        if (this.dueBy != null)
        {
            
            data.put (MessageFieldNames.dueby,
                      this.dueBy.getTime ());
            
        }
        
        List clist = new ArrayList ();
        
        data.put (MessageFieldNames.chapters,
                  clist);
        
        // Add the chapters.
        for (Chapter c : this.chapters)
        {
            
            clist.add (TypeEncoder.encode (c));
            
        }        
        */
        Map edInf = new HashMap ();
        
        data.put (MessageFieldNames.editorinformation,
                  edInf);
        
        if (this.editorName != null)
        {
            
            edInf.put (MessageFieldNames.name,
                       this.editorName);
            
        }
        
        if (this.editorAvatar != null)
        {
            
            edInf.put (MessageFieldNames.avatar,
                       EditorsUtils.getImageAsBase64EncodedString (this.editorAvatar));
            
        }
        
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
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {
        
        super.doInit (data,
                      from);
        
        Map edInf = (Map) data.get (MessageFieldNames.editorinformation);
        
        if (edInf != null)
        {
            
            String n = this.getString (MessageFieldNames.name,
                                       edInf,
                                       false);
            
            if (n != null)
            {
                
                this.editorName = n;
                
            }
            
            String avatar = this.getString (MessageFieldNames.avatar,
                                            edInf,
                                            false);
            
            if (avatar != null)
            {
                
                this.editorAvatar = EditorsUtils.getImageFromBase64EncodedString (avatar);
                
            }
            
        }        
             /*           
        this.setForProjectName (this.getString (MessageFieldNames.name,
                                                data));
                
        String notes = this.getString (MessageFieldNames.notes,
                                       data,
                                       false);
        
        if (notes != null)
        {
            
            notes = notes.trim ();
            
            if (notes.length () > 5000)
            {
                
                throw new GeneralException ("Notes is too long, max length is: 5000, is: " + notes.length ());
                
            }
            
            this.notes = notes;
            
        }
        
        this.versionName = this.getString (MessageFieldNames.versionname,
                                           data,    
                                           false);

        this.dueBy = this.getDate (MessageFieldNames.dueby,
                                   data,
                                   false);
        
        Object chaps = this.checkTypeAndNotNull (MessageFieldNames.chapters,
                                                 data,
                                                 List.class);
        
        List chapsL = (List) chaps;
        
        if (chapsL.size () == 0)
        {
            
            throw new GeneralException ("Expected to find at least 1 chapter.");
            
        }
        
        int wc = 0;
        
        Set<Chapter> chapsS = new LinkedHashSet ();
        
        for (int i = 0; i < chapsL.size (); i++)
        {
            
            Object co = chapsL.get (i);
            
            if (!(co instanceof Map))
            {
                
                throw new GeneralException ("Expected chapter data for index: " +
                                            i +
                                            " to by a map, has type: " +
                                            co.getClass ().getName ());
                
            }
            
            Map cm = (Map) co;

            Chapter c = TypeEncoder.decodeToChapter (cm);

            wc += TextUtilities.getWordCount (c.getText ());
            
            chapsS.add (c);
            
        }
        
        this.wordCount = wc;
        this.chapters = chapsS;
        */
    }
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
/*
    public boolean isEncrypted ()
    {
        
        return true;
        
    }
  */  
    public String getMessageType ()
    {
        
        return MESSAGE_TYPE;
        
    }
    
    
}