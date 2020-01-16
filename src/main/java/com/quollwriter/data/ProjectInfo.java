package com.quollwriter.data;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.collections.*;

import org.jdom.*;

import com.quollwriter.events.*;
import com.quollwriter.*;
import com.quollwriter.data.editors.*;

public class ProjectInfo extends NamedObject implements PropertyChangedListener
{

    public static final String STATUS_PROP_NAME = "status";
    public static final String LAST_EDITED_PROP_NAME = "lastEdited";

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
    private ObjectProperty<Path> projectDirectoryProp = null;
    private boolean            noCredentials = false;
    private String             type = Project.NORMAL_PROJECT_TYPE;
    private File               icon = null;
    private Path               filesDirectory = null;
    private ObservableMap<Statistic, Object> statistics = null;
    private boolean            encrypted = false;
    private Project            project = null;
    private String             filePassword = null;
	private boolean opening = false;
    private ObjectProperty<Path> backupDirPathProp = null;
    private ObservableSet<Path> backupPaths = null;

    private StringProperty statusProp = null;
    private ObjectProperty<Date> lastEditedProp = null;
    private IPropertyBinder binder = null;

    public ProjectInfo ()
    {

        super (OBJECT_TYPE);

        this.statistics = FXCollections.observableMap (new HashMap<> ());

        final ProjectInfo _this = this;
        this.binder = new PropertyBinder ();

        this.statusProp = new SimpleStringProperty (this, STATUS_PROP_NAME);
        this.statusProp.addListener ((p, oldv, newv) ->
        {

            _this.firePropertyChangedEvent (STATUS_PROP_NAME,
                                            oldv,
                                            newv);

        });

        this.lastEditedProp = new SimpleObjectProperty<> (this, LAST_EDITED_PROP_NAME);
        this.lastEditedProp.addListener ((p, oldv, newv) ->
        {

            _this.firePropertyChangedEvent (LAST_EDITED_PROP_NAME,
                                            oldv,
                                            newv);

        });

        this.backupDirPathProp = new SimpleObjectProperty<> ();
        this.projectDirectoryProp = new SimpleObjectProperty<> ();

    }

    public ProjectInfo (Project from)
    {

        this ();

        this.setId (from.getId ());

        this.setProject (from);

        //this.project.addPropertyChangedListener (this);

    }

    public void dispose ()
    {

        this.binder.dispose ();

    }

    public ObservableSet<Path> getBackupPaths ()
                                        throws GeneralException
    {

        if (this.backupPaths == null)
        {

            this.backupPaths = FXCollections.observableSet (new TreeSet<> ((p1, p2) ->
            {

                try
                {

                    FileTime l1 = null;
                    FileTime l2 = null;

                    if (Files.exists (p1))
                    {

                        l1 = Files.getLastModifiedTime (p1);

                    }

                    if (Files.exists (p2))
                    {

                        l2 = Files.getLastModifiedTime (p2);

                    }

                    if ((l1 == null)
                        &&
                        (l2 == null)
                       )
                    {

                        return 0;

                    }

                    int ret = 0;

                    if (l1 == null)
                    {

                        ret = 1;

                    }

                    if (l2 == null)
                    {

                        ret = -1;

                    }

                    if ((l1 != null)
                        &&
                        (l2 != null)
                       )
                    {

                        ret = l1.compareTo (l2);

                    }

                    return -1 * ret;//Files.getLastModifiedTime (p1).compareTo (Files.getLastModifiedTime (p2));

                } catch (Exception e) {

                    throw new RuntimeException (e);

                }

            }));

            Path p = this.getBackupDirPath ();

            if (p != null)
            {

                try
                {

                    this.backupPaths.addAll (Files.list (p)
                        .filter (path ->
                        {

                            try
                            {

                                return Utils.isBackupFile (path);

                            } catch (Exception e) {

                                Environment.logError ("Unable to determine if path: " + path + " is a backup file.",
                                                      e);

                                return false;

                            }

                        })
                        .collect (Collectors.toList ()));

                } catch (Exception e) {

                    throw new GeneralException ("Unable to get backup paths from: " + p,
                                                e);

                }

            }

        }

        return this.backupPaths;

    }

    public void addBackupPath (Path p)
                        throws GeneralException
    {

        this.getBackupPaths ().add (p);

    }

    public void removeBackupPath (Path p)
                           throws GeneralException
    {

        this.getBackupPaths ().remove (p);

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
                                    this.statusProp.getValue ());
        this.addToStringProperties (props,
                                    "icon",
                                    (this.icon != null ? this.icon.getPath () : "Not set"));

        this.addToStringProperties (props,
                                    "projectDir",
                                    (this.projectDirectoryProp.getValue () != null ? this.projectDirectoryProp.getValue ().toString () : "Not set"));
        this.addToStringProperties (props,
                                    "backupDir",
                                    (this.backupDirPathProp.getValue () != null ? this.backupDirPathProp.getValue ().toString () : "Not set"));
        this.addToStringProperties (props,
                                    "filesDir",
                                    (this.filesDirectory != null ? this.filesDirectory.toString () : "Not set"));
        this.addToStringProperties (props,
                                    "lastEdited",
                                    this.lastEditedProp.getValue ());
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

    public ObservableMap<Statistic, Object> getStatistics ()
    {

        return this.statistics;

    }

    public void setStatistics (Map<Statistic, Object> stats)
    {

        this.statistics.clear ();
        this.statistics.putAll (stats);

    }

    private void saveInfo ()
    {

        try
        {

          Environment.updateProjectInfo (this);

        } catch (Exception e) {

          Environment.logError ("Unable to update project info for project: " +
                                this.project,
                                e);

        }

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

            //this.project.removePropertyChangedListener (this);

        }

        this.project = p;

        this.type = p.getType ();
        this.encrypted = p.isEncrypted ();
        this.noCredentials = p.isNoCredentials ();
        this.forEditor = p.getForEditor ();
        this.filePassword = p.getFilePassword ();
        this.setName (p.getName ());

        //this.project.addPropertyChangedListener (this);

        this.binder.addChangeListener (this.project.nameProperty (),
                                       (pr, oldv, newv) ->
        {

            this.setName (newv);
            this.saveInfo ();

        });

        this.setProjectDirectory (this.project.getProjectDirectory ().toPath ());

        this.binder.addChangeListener (this.project.projectDirectoryProperty (),
                                       (pr, oldv, newv) ->
        {

            this.setProjectDirectory (newv.toPath ());
            this.setFilesDirectory (this.project.getFilesDirectory ().toPath ());
            this.saveInfo ();

        });

        this.setBackupDirPath (this.project.getBackupDirectory ().toPath ());

        this.binder.addChangeListener (this.project.backupDirectoryProperty (),
                                       (pr, oldv, newv) ->
        {

            this.setBackupDirPath (newv.toPath ());
            this.saveInfo ();

        });

        this.setLastEdited (this.project.getLastEdited ());

        this.binder.addChangeListener (this.project.lastEditedProperty (),
                                       (pr, oldv, newv) ->
        {

            this.setLastEdited (newv);
            this.saveInfo ();

        });

        // TODO Need to deal with the RI.

        this.addStatistic (Statistic.chapterCount,
                           this.project.getChapterCount ());

        this.binder.addChangeListener (this.project.chapterCountProperty (),
                                       (pr, oldv, newv) ->
        {

            this.addStatistic (Statistic.chapterCount,
                               newv);
            this.saveInfo ();

        });

        /*
        It would be expensive to try and keep this up to date.
        this.binder.addChangeListener (this.project.editedWordCountProperty (),
                                       (pr, oldv, newv) ->
        {

            this.addStatistic (Statistic.editedWordCount,
                               newv);
            this.saveInfo ();

        });
*/
        this.addStatistic (Statistic.editedWordCount,
                           this.project.getEditedWordCount ());
        ReadabilityIndices ri = this.project.getAllProjectReadabilityIndices ();

        this.addStatistic (Statistic.wordCount,
                           ri.getWordCount ());
        this.addStatistic (Statistic.gunningFogIndex,
                           ri.getGunningFogIndex ());
        this.addStatistic (Statistic.fleschReadingEase,
                           ri.getFleschReadingEase ());
        this.addStatistic (Statistic.fleschKincaidGradeLevel,
                           ri.getFleschKincaidGradeLevel ());

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

        this.setProjectDirectory (this.project.getProjectDirectory ().toPath ());
        this.setBackupDirPath (this.project.getBackupDirectory ().toPath ());
        this.setFilesDirectory (this.project.getFilesDirectory ().toPath ());
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

        return this.statusProp.getValue ();

    }

    public void setStatus (String s)
    {

        this.statusProp.unbind ();

        StringProperty p = UserProperties.getProjectStatus (s);

        this.statusProp.bind (p);

    }

    public StringProperty statusProperty ()
    {

        return this.statusProp;

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

    public ObjectProperty<Date> lastEditedProperty ()
    {

        return this.lastEditedProp;

    }

    public void setLastEdited (Date d)
    {

        this.lastEditedProp.setValue (d);

    }

    public Date getLastEdited ()
    {

        return this.lastEditedProp.getValue ();

    }

    public void setProjectDirectory (Path f)
    {

        this.projectDirectoryProp.setValue (f);

    }

    public Path getProjectDirectory ()
    {

        return this.projectDirectoryProp.getValue ();

    }

    public ObjectProperty<Path> backupDirPathProperty ()
    {

        return this.backupDirPathProp;

    }

    public Path getBackupDirPath ()
    {

        return this.backupDirPathProp.getValue ();

    }

    public void setBackupDirPath (Path f)
    {

        this.backupDirPathProp.setValue (f);

    }

    public Path getFilesDirectory ()
    {

        return this.filesDirectory;

    }

    public void setFilesDirectory (Path f)
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
