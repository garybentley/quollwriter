package com.quollwriter.ui.fx.swing;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.actionHandlers.*;

public abstract class ProjectObjectQuollPanel<E extends AbstractProjectViewer> extends QuollPanel<E>
{

    public static final int UNSAVED_CHANGES_ACTION_EVENT = 0;

    public static final int SAVED = 1;

    public static final String HAS_CHANGES_COMMAND = "hasChanges";
    public static final String NO_CHANGES_COMMAND = "noChanges";

    protected Chapter                               chapter = null;
    private java.util.List                          actionListeners = new ArrayList ();
    private java.util.List<PropertyChangedListener> propertyChangedListeners = new ArrayList ();
    private boolean                                 hasUnsavedChanges = false;

    public ProjectObjectQuollPanel (E       pv,
                                    Chapter obj)
    {

        super (pv);

        this.chapter = obj;

    }

    public abstract boolean saveUnsavedChanges ()
                                         throws Exception;

    public abstract void refresh ();

    public java.util.List<PropertyChangedListener> getObjectPropertyChangedListeners ()
    {

        return this.propertyChangedListeners;

    }

    public void removeObjectPropertyChangedListener (PropertyChangedListener l)
    {

        this.propertyChangedListeners.remove (l);

    }

    public void addObjectPropertyChangedListener (PropertyChangedListener l)
    {

        this.propertyChangedListeners.add (l);

        this.chapter.addPropertyChangedListener (l);

    }

    public void saveObject ()
                     throws Exception
    {

        this.viewer.saveObject (this.chapter,
                                true);

        this.setHasUnsavedChanges (false);

        // Fire an event to interested parties.
        this.fireActionEvent (new ActionEvent (this,
                                               SAVED,
                                               "saved"));

    }

    @Override
    public String getPanelId ()
    {

        return this.chapter.getObjectReference ().asString ();

    }

    public boolean hasUnsavedChanges ()
    {

        return this.hasUnsavedChanges;

    }

    protected void setHasUnsavedChanges (boolean hasChanges)
    {

        this.hasUnsavedChanges = hasChanges;

        this.fireActionEvent (new ActionEvent (this,
                                               UNSAVED_CHANGES_ACTION_EVENT,
                                               (hasChanges ? HAS_CHANGES_COMMAND : NO_CHANGES_COMMAND)));

    }

}
