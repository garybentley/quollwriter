package com.quollwriter.data.editors;

import java.util.*;
import javafx.beans.property.*;

import com.quollwriter.data.*;

/**
 * Models an editor for a project.
 */
public class ProjectEditor extends DataObject implements Comparable<ProjectEditor>
{

    public static final String OBJECT_TYPE = "projecteditor";

    public enum Status
    {

        invited ("invited"),
        accepted ("accepted");

        private String type = null;

        Status (String type)
        {

            this.type = type;

        }

        public String getType ()
        {

            return this.type;

        }

    }

    protected EditorEditor editor = null;
    private String forProjectId = null;
    private String forProjectName = null;
    private ObjectProperty<Status> statusProp = new SimpleObjectProperty ();
    private StringProperty statusMessageProp = new SimpleStringProperty ();
    private Date from = null;
    private Date to = null;
    private BooleanProperty currentProp = new SimpleBooleanProperty (false);

    public ProjectEditor ()
    {

        super (OBJECT_TYPE);

    }

    public ProjectEditor (Project      proj,
                          EditorEditor editor)
    {

        super (OBJECT_TYPE);

        this.forProjectId = proj.getId ();
        this.forProjectName = proj.getName ();
        this.editor = editor;

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "forProjectId",
                                    this.forProjectId);
        this.addToStringProperties (props,
                                    "forProjectName",
                                    this.forProjectName);
        this.addToStringProperties (props,
                                    "editor",
                                    this.editor);
        this.addToStringProperties (props,
                                    "current",
                                    this.currentProp.getValue ());
        this.addToStringProperties (props,
                                    "statusMessage",
                                    this.getStatusMessage ());
        this.addToStringProperties (props,
                                    "from",
                                    this.from);
        this.addToStringProperties (props,
                                    "to",
                                    this.to);

    }

    public int compareTo (ProjectEditor pe)
    {

        return this.editor.getMainName ().compareTo (pe.editor.getMainName ());

    }

    public boolean isPrevious ()
    {

        return this.to != null;

    }

    public boolean isInvited ()
    {

        if (this.statusProp.getValue () == null)
        {

            return true;

        }

        return this.statusProp.getValue () == Status.invited;

    }

    public Status getStatus ()
    {

        return this.statusProp.getValue ();

    }

    public void setStatus (Status s)
    {

        this.statusProp.setValue (s);

    }

    public ObjectProperty<Status> statueProperty ()
    {

        return this.statusProp;

    }

    public void setForProjectName (String n)
    {

        this.forProjectName = n;

    }

    public String getForProjectName ()
    {

        return this.forProjectName;

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        return null;

    }

    public void setForProjectId (String id)
    {

        this.forProjectId = id;

    }

    public String getForProjectId ()
    {

        return this.forProjectId;

    }

    public void setCurrent (boolean v)
    {

        this.currentProp.setValue (v);

    }

    public boolean isCurrent ()
    {

        return this.currentProp.getValue ();

    }

    public BooleanProperty currentProperty ()
    {

        return this.currentProp;

    }

    public void setEditorTo (Date d)
    {

        this.to = d;
        this.currentProp.setValue (false);

    }

    public Date getEditorTo ()
    {

        return this.to;

    }

    public void setEditorFrom (Date d)
    {

        this.from = d;

    }

    public Date getEditorFrom ()
    {

        return this.from;

    }

    public StringProperty statusMessageProperty ()
    {

        return this.statusMessageProp;

    }

    public void setStatusMessage (String s)
    {

        this.statusMessageProp.setValue (s);

    }

    public String getStatusMessage ()
    {

        return this.statusMessageProp.getValue ();

    }

    public void setEditor (EditorEditor ed)
    {

        this.editor = ed;

    }

    public EditorEditor getEditor ()
    {

        return this.editor;

    }

}
