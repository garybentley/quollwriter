package com.quollwriter.data;

import java.io.*;
import java.util.*;

import javafx.beans.property.*;

import org.jdom.*;

import com.quollwriter.events.*;
import com.quollwriter.*;
import com.quollwriter.data.editors.*;

public class ProjectInfo extends NamedObject implements PropertyChangedListener
{

    public enum Statistic
    {

        wordCount ("wordCount"),
        chapterCount ("chapterCount"),
        gunningFogIndex ("gunningFogIndex"),
        fleschReadingEase ("fleschReadingEase"),
        fleschKincaidGradeLevel ("fleschKincaidGradeLevel"),
        editedWordCount ("editedWordCount");

        private String type = null;

        Statistic (String type)
        {

            this.type = type;

        }

        public String getType ()
        {

            return this.type;

        }

    }

    public static final String OBJECT_TYPE = "projectinfo";

    private EditorEditor forEditor = null;
    private File               projectDirectory = null;
    private Date               lastEdited = null;
    private boolean            noCredentials = false;
    private String             type = Project.NORMAL_PROJECT_TYPE;
    private String             status = null;
    private File               icon = null;
    private File               backupDirectory = null;
    private File               filesDirectory = null;
    private Map<Statistic, Object> statistics = new HashMap ();
    private boolean            encrypted = false;
    private Project            project = null;
    private String             filePassword = null;
	private boolean opening = false;

    private SimpleStringProperty nameProp = new SimpleStringProperty ();

    public ProjectInfo ()
    {

        super (OBJECT_TYPE);

    }

    public ProjectInfo (Project from)
    {

        this ();

        this.type = from.getType ();

        this.project = from;

        this.encrypted = from.isEncrypted ();
        this.noCredentials = from.isNoCredentials ();
        this.lastEdited = from.getLastEdited ();
        this.setId (from.getId ());
        this.forEditor = from.getForEditor ();
        this.filePassword = from.getFilePassword ();

        this.nameProp.setValue (this.project.getName ());

        this.update ();

        this.project.addPropertyChangedListener (this);

    }

    public StringProperty nameProperty ()
    {

        return this.nameProp;

    }

    public synchronized void setOpening (boolean v)
    {

        this.opening = v;

    }

    public synchronized boolean isOpening ()
    {

        if (this.opening)
        {

            if (Environment.getProjectViewer (this.project) != null)
            {

                this.opening = false;

            }

        }

        return this.opening;

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "type",
                                    this.type);
        this.addToStringProperties (props,
                                    "status",
                                    this.status);
        this.addToStringProperties (props,
                                    "icon",
                                    (this.icon != null ? this.icon.getPath () : "Not set"));

        this.addToStringProperties (props,
                                    "projectDir",
                                    (this.projectDirectory != null ? this.projectDirectory.getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "backupDir",
                                    (this.backupDirectory != null ? this.backupDirectory.getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "filesDir",
                                    (this.filesDirectory != null ? this.filesDirectory.getPath () : "Not set"));
        this.addToStringProperties (props,
                                    "lastEdited",
                                    this.lastEdited);
        this.addToStringProperties (props,
                                    "encrypted",
                                    this.encrypted);
        this.addToStringProperties (props,
                                    "noCredentials",
                                    this.noCredentials);

        if (this.forEditor != null)
        {

            this.addToStringProperties (props,
                                        "forEditor",
                                        this.forEditor.getEmail ());

        }

        this.addToStringProperties (props,
                                    "statistics",
                                    this.statistics);

    }

    public Map<Statistic, Object> getStatistics ()
    {

        return this.statistics;

    }

    public void setStatistics (Map<Statistic, Object> stats)
    {

        this.statistics = stats;

    }

    @Override
    public void propertyChanged (PropertyChangedEvent ev)
    {

        this.update ();

        try
        {

          Environment.updateProjectInfo (this);

        } catch (Exception e) {

          Environment.logError ("Unable to update project info for project: " +
                                this.project,
                                e);

        }

    }

    public void setProject (Project p)
    {

        if (!p.getId ().equals (this.getId ()))
        {

            throw new IllegalArgumentException ("Project does not have same id as this project info.");

        }

        if (this.project != null)
        {

            this.project.removePropertyChangedListener (this);

        }

        this.project = p;

        this.filePassword = p.getFilePassword ();

        this.update ();

        this.project.addPropertyChangedListener (this);

    }

    public String getFilePassword ()
    {

        return this.filePassword;

    }

    public void setFilePassword (String p)
    {

        this.filePassword = p;

    }

    private void update ()
    {

        this.setProjectDirectory (this.project.getProjectDirectory ());
        this.setBackupDirectory (this.project.getBackupDirectory ());
        this.setFilesDirectory (this.project.getFilesDirectory ());
        this.setName (this.project.getName ());
        this.setLastEdited (this.project.getLastEdited ());

        ReadabilityIndices ri = this.project.getAllProjectReadabilityIndices ();

        this.addStatistic (Statistic.chapterCount,
                           this.project.getChapterCount ());
        this.addStatistic (Statistic.wordCount,
                           ri.getWordCount ());

        this.addStatistic (Statistic.gunningFogIndex,
                           ri.getGunningFogIndex ());
        this.addStatistic (Statistic.fleschReadingEase,
                           ri.getFleschReadingEase ());
        this.addStatistic (Statistic.fleschKincaidGradeLevel,
                           ri.getFleschKincaidGradeLevel ());
        this.addStatistic (Statistic.editedWordCount,
                           this.project.getEditedWordCount ());

    }

    public void addStatistic (Statistic s,
                              Object    value)
    {

        this.statistics.put (s,
                             value);

    }

    private int getNumberStatistic (Statistic s)
    {

        Object o = this.statistics.get (s);

        if (o == null)
        {

            return 0;

        }

        if (!(o instanceof Number))
        {

            return 0;

        }

        return ((Number) o).intValue ();

    }

    public int getEditedWordCount ()
    {

        return this.getNumberStatistic (Statistic.editedWordCount);

    }

    public int getFleschKincaidGradeLevel ()
    {

        return this.getNumberStatistic (Statistic.fleschKincaidGradeLevel);

    }

    public int getFleschReadingEase ()
    {

        return this.getNumberStatistic (Statistic.fleschReadingEase);

    }

    public int getGunningFogIndex ()
    {

        return this.getNumberStatistic (Statistic.gunningFogIndex);

    }

    public int getChapterCount ()
    {

        return this.getNumberStatistic (Statistic.chapterCount);

    }

    public int getWordCount ()
    {

        return this.getNumberStatistic (Statistic.wordCount);

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new TreeSet ();

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public String getStatus ()
    {

        return this.status;

    }

    public void setStatus (String s)
    {

        this.status = s;

    }

    public void setIcon (File ic)
    {

        this.icon = ic;

    }

    public File getIcon ()
    {

        return this.icon;

    }

    public boolean isNoCredentials ()
    {

        return this.noCredentials;

    }

    public void setNoCredentials (boolean v)
    {

        this.noCredentials = v;

    }

    public boolean isEncrypted ()
    {

        return this.encrypted;

    }

    public void setEncrypted (boolean v)
    {

        this.encrypted = v;

    }

    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        this.type = t;

    }

    public void setLastEdited (Date d)
    {

        this.lastEdited = d;

    }

    public Date getLastEdited ()
    {

        return this.lastEdited;

    }

    public void setProjectDirectory (File f)
    {

        this.projectDirectory = f;

    }

    public File getProjectDirectory ()
    {

        return this.projectDirectory;

    }

    public File getBackupDirectory ()
    {

        return this.backupDirectory;

    }

    public void setBackupDirectory (File f)
    {

        this.backupDirectory = f;

    }

    public File getFilesDirectory ()
    {

        return this.filesDirectory;

    }

    public void setFilesDirectory (File f)
    {

        this.filesDirectory = f;

    }

    public void setForEditor (EditorEditor ed)
    {

        this.forEditor = ed;

    }

    public EditorEditor getForEditor ()
    {

        return this.forEditor;

    }

    public boolean isEditorProject ()
    {

        return this.type.equals (Project.EDITOR_PROJECT_TYPE);

    }

    public boolean isWarmupsProject ()
    {

        return this.type.equals (Project.WARMUPS_PROJECT_TYPE);

    }

    public boolean isNormalProject ()
    {

        return this.type.equals (Project.NORMAL_PROJECT_TYPE);

    }

}
