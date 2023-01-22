package com.quollwriter.data.editors;

import java.util.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.data.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

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
    private StringProperty statusMessageProp = null;
    private Date from = null;
    private Date to = null;
    private BooleanProperty currentProp = new SimpleBooleanProperty (false);
    private ObjectProperty<List<String>> statusString = null;
    private ObjectProperty<List<String>> statusStringParms = null;

    public ProjectEditor ()
    {

        super (OBJECT_TYPE);

        this.statusString = new SimpleObjectProperty (new ArrayList<> ());
        this.statusStringParms = new SimpleObjectProperty (new ArrayList<> ());

        this.statusMessageProp = new SimpleStringProperty ();
        this.statusMessageProp.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            if ((this.statusString.getValue () == null)
                ||
                (this.statusString.getValue ().size () == 0)
               )
            {

                return "";

            }

            return getUILanguageStringProperty (this.statusString.getValue (),
                                                (this.statusStringParms.getValue () != null ? this.statusStringParms.getValue ().toArray () : null)).getValue ();

        },
        this.statusString));

    }

    public ProjectEditor (Project      proj,
                          EditorEditor editor)
    {

        this ();

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
                                    this.statusMessageProp.getValue ());
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

    public ObjectProperty<Status> statusProperty ()
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

    public void setStatusMessage (List<String> str,
                                  List<String> parms)
    {

        this.statusStringParms.setValue (parms);
        this.statusString.setValue (str);

    }

    public void setStatusMessage (String s)
    {

        if (s == null)
        {

            return;

        }

        // Eeek!
        if (s.startsWith ("{"))
        {

            try
            {

                Map m = (Map) JSONDecoder.decode (s);
                Object str = m.get ("str");

                Object p = m.get ("parms");

                if ((p != null)
                    &&
                    (p instanceof List)
                   )
                {

                    this.statusStringParms.setValue ((List<String>) p);

                }

                if (str instanceof List)
                {

                    this.statusString.setValue ((List<String>) str);

                }

            } catch (Exception e) {

                this.statusString.setValue (Arrays.asList ("<Error>"));

            }

        } else {

            // Legacy, pre v3.
            this.statusString.setValue (Arrays.asList (s));

        }

    }

    public String getStatusMessage ()
                             throws GeneralException
    {

        Map m = new HashMap ();
        m.put ("str",
               this.statusString.getValue ());
        m.put ("parms",
               this.statusStringParms.getValue ());

        return JSONEncoder.encode (m);

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
