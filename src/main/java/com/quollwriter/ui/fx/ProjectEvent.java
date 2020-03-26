package com.quollwriter.ui.fx;

import java.util.*;

import com.quollwriter.data.*;

public class ProjectEvent extends EventObject
{

    // Don't you just love compilers...
    public enum Type
    {
        any ("any"),
        fullscreen ("fullScreen"),
        distractionfree ("distractionfree"),
        typewritersound ("typewritersound"),
        wordcounts ("wordcounts"),
        autosave ("autosave"),
        spellcheck ("spellcheck"),
        toolbar ("toolbar"),
        layout ("layout"),
        sidebar ("sidebar"),
        find ("find"),
        synonym ("synonym"),
        readability ("readabilty"),
        about ("about"),
        personaldictionary ("personaldictionary"),
        help ("help"),
        bugreport ("bugreport"),
        contact ("contact"),
        _export ("export"),
        _import ("import"),
        problemfinder ("problemfinder"),
        problemfinderruleconfig ("problemfinderruleconfig"),
        itemtypes ("itemtypes"),
        notetypes ("notetypes"),
        ideaboard ("ideaboard"),
        idea ("idea"),
        ideatype ("ideatype"),
        achievements ("achievements"),
        tips ("tips"),
        tabs ("tabs"),
        whatsnew ("whatsnew"),
        statistics ("statistics"),
        backups ("backups"),
        textproperties ("textproperties"),
        userobjecttype ("userobjecttype"),
        userobjecttypefield ("userobjecttypefield"),
        tags ("tags"),
        // TODO Change achievements to handle this...
        projectobject ("projectobject"),
        scene ("scene"),
        note ("note"),
        outlineitem ("outlineitem"),
        chapter ("chapter"),
        warmup ("warmup"),
        asset ("asset"),
        project ("project"),
        tag ("tag");

        final String type;

        Type (String t)
        {

            this.type = t;

        }

    }

    public enum Action
    {
        changebackground ("changebackground"),
        changelanguage ("changelanguage"),
        open ("open"),
        show ("show"),
        close ("close"),
        any ("*"),
        _new ("new"),
        edit ("edit"),
        rename ("rename"),
        restore ("restore"),
        delete ("delete"),
        on ("on"),
        off ("off"),
        move ("move"),
        rate ("rate"),
        submit ("submit"),
        changed ("changed"),
        enter ("enter"),
        exit ("exit"),
        replace ("replace"),
        changeddirectory ("changeddirectory"),
        addword ("addword"),
        removeword ("removeword"),
        ignore ("ignore"),
        unignore ("unignore"),
        newrule ("newrule"),
        editrule ("editrule"),
        removerule ("removerule"),
        sort ("sort"),
        timereached ("timereached"),
        showchart ("showchart"),
        wordcountreached ("wordcountreached"),
        contverttoproject ("converttoproject"),
        warmuponstartup ("warmuponstartup"),
        timerrestart ("timerrestart"),
        timerstarted ("timerstarted"),
        createownprompt ("createownprompt"),
        changebordersize ("changebordersize"),
        changerbgcolor ("changebgcolor"),
        changelinespacing ("changelinespacing"),
        changealignment ("changealignment"),
        changefontcolor ("changefontcolor"),
        changefontsize ("changefontsize"),
        changefont ("changefont"),
        changebgimage ("changebgimage"),
        changebgopacity ("changebgopacity"),
        changelineindent ("changelineindent"),
        changehighlightwritingline ("changehighlightwritingline"),
        changetextborder ("changetextborder");

        final String action;

        Action (String a)
        {

            this.action = a;

        }

    }

    private Type type = null;
    private Action action = null;
    private Object contextObject = null;

    public ProjectEvent (Object source,
                         Type   type,
                         Action action)
    {

        super (source);

        this.type = type;
        this.action = action;

    }

    public ProjectEvent (Object source,
                         Type   type,
                         Action action,
                         Object contextObject)
    {

        this (source,
              type,
              action);

        this.contextObject = contextObject;

    }

    public ProjectEvent (Object source,
                         Type   type)
    {

        this (source,
              type,
              Action.any);

    }

    public ProjectEvent (Object source,
                         Type   type,
                         Object contextObject)
    {

        this (source,
              type,
              Action.any,
              contextObject);

    }

    public Object getContextObject ()
    {

        return this.contextObject;

    }

    public Action getAction ()
    {

        return this.action;

    }

    public Type getType ()
    {

        return this.type;

    }

    public String toString ()
    {

        return this.getEventId ();

    }

    public String getEventId ()
    {

        return this.type + (this.action != null ? "." + this.action : "");

    }

}
