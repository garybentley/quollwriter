package com.quollwriter.data;

import java.util.*;

import javafx.collections.*;

import com.quollwriter.*;

import org.jdom.*;

import org.josql.utils.*;

import com.quollwriter.data.comparators.*;

public class IdeaType extends NamedObject
{

    public static final String OBJECT_TYPE = "ideatype";

    public static final String SORT_BY_RATING = "rating";
    public static final String SORT_BY_DATE = "lastModified";
    public static final String SORT_BY_TEXT = "description";

    private String     sortBy = null;
    private String     iconType = null;
    private ObservableSet<Idea> ideas = FXCollections.observableSet (new LinkedHashSet<> ());
    private Project    proj = null;

    public IdeaType()
    {

        super (IdeaType.OBJECT_TYPE);

    }

    public void setProject (Project p)
    {

        this.proj = p;

        this.setParent (p);

    }

    public String getSortBy ()
    {

        return this.sortBy;

    }

    public void setSortBy (String s)
    {

        if (s == null)
        {

            this.sortBy = IdeaType.SORT_BY_RATING;

        }

        this.sortBy = s;

    }

    public String getIconType ()
    {

        return this.iconType;

    }

    public void setIconType (String it)
    {

        this.iconType = it;

    }

    public void addIdea (Idea i)
    {

        if (this.ideas.contains (i))
        {

            return;

        }

        i.setType (this);

        this.ideas.add (i);

    }

    public void removeIdea (Idea i)
    {

        this.ideas.remove (i);

    }

    public int getIdeaCount ()
    {

        return this.ideas.size ();

    }

    public ObservableSet<Idea> getIdeas ()
    {

        return this.ideas;

    }

    /*
    public void setIdeas (List<Idea> ideas)
    {

        this.ideas = ideas;

    }
     */
    public String toString ()
    {

        return this.getObjectType () + "(" + this.getName () + ", " + this.getKey () + ")";

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet (this.ideas);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {


    }


}
