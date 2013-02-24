package com.quollwriter.ui;

import java.util.*;

import com.quollwriter.data.*;

public class ProjectEvent extends EventObject
{
    
    public static final String FULL_SCREEN = "fullscreen";
    public static final String TYPE_WRITER_SOUND = "typewritersound";
    public static final String WORD_COUNTS = "wordcounts";
    public static final String WORD_COUNT_HISTORY = "wordcounthistory";
    public static final String AUTO_SAVE = "autosave";
    public static final String SPELL_CHECK = "spellcheck";
    public static final String TOOLBAR = "toolbar";
    public static final String SIDEBAR = "sidebar";
    public static final String FIND = "find";
    public static final String SYNONYM = "synonym";
    public static final String READABILITY = "readability";
    public static final String ABOUT = "about";
    public static final String PERSONAL_DICTIONARY = "personaldictionary";
    public static final String HELP = "help";
    public static final String BUG_REPORT = "bugreport";
    public static final String EXPORT = "export";
    public static final String IMPORT = "import";
    public static final String PROBLEM_FINDER = "problemfinder";
    public static final String ITEM_TYPES = "itemtypes";
    public static final String IDEA_BOARD = "ideaboard";
    public static final String ACHIEVEMENTS = "achievements";
    public static final String TIPS = "tips";
    public static final String TABS = "tabs";
    
    public static final String CHANGE_BACKGROUND = "changebackground";
    public static final String CHANGE_LANGUAGE = "changelanguage";
    public static final String OPEN = "open";
    public static final String SHOW = "show";
    public static final String CLOSE = "close";
    public static final String ANY = "*";
    public static final String NEW = "new";
    public static final String EDIT = "edit";
    public static final String RENAME = "rename";
    public static final String DELETE = "delete";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String MOVE = "move";
    public static final String RATE = "rate";
    public static final String NEWRULE = "newrule";
    public static final String EDITRULE = "editrule";
    public static final String SUBMIT = "submit";
    public static final String ENTER = "enter";
    public static final String EXIT = "exit";
    public static final String REPLACE = "replace";
    public static final String CHANGED_DIRECTORY = "changeddirectory";
    public static final String ADD_WORD = "addword";
    public static final String IGNORE = "ignore";
    public static final String UNIGNORE = "unignore";
    public static final String NEW_RULE = "newrule";
    public static final String EDIT_RULE = "editrule";
    public static final String SORT = "sort";
    public static final String TIME_REACHED = "timereached";
    public static final String WORD_COUNT_REACHED = "wordcountreached";
    public static final String CONVERT_TO_PROJECT = "converttoproject";
    public static final String WARMUP_ON_STARTUP = "warmuponstartup";
    public static final String TIMER_RESTART = "timerrestart";
    public static final String TIMER_STARTED = "timerstarted";
    public static final String CREATE_OWN_PROMPT = "createownprompt";
    public static final String CHANGE_BORDER_SIZE = "changebordersize";
    public static final String CHANGE_BG_COLOR = "changebgcolor";
    public static final String CHANGE_LINE_SPACING = "changelinespacing";
    public static final String CHANGE_ALIGNMENT = "changealignment";
    public static final String CHANGE_FONT_COLOR = "changefontcolor";
    public static final String CHANGE_FONT_SIZE = "changefontsize";
    public static final String CHANGE_FONT = "changefont";
    public static final String CHANGE_BG_IMAGE = "changebgimage";
    public static final String CHANGE_BG_OPACITY = "changebgopacity";
    public static final String CHANGE_LINE_INDENT = "changelineindent";
    
    private AbstractProjectViewer viewer = null;
    private String type = null;
    private String action = null;
    private Object contextObject = null;
    
    public ProjectEvent (AbstractProjectViewer viewer,
                         String                type,
                         String                action)
    {
        
        super (viewer);
        
        this.viewer = viewer;
        this.type = type;
        this.action = action;
        
    }

    public ProjectEvent (AbstractProjectViewer viewer,
                         String                type,
                         String                action,
                         Object                contextObject)
    {
        
        this (viewer,
              type,
              action);
        
        this.contextObject = contextObject;
                
    }
    
    public ProjectEvent (AbstractProjectViewer viewer,
                         String                type)
    {
        
        this (viewer,
              type,
              "*");
                        
    }

    public ProjectEvent (AbstractProjectViewer viewer,
                         String                type,
                         Object                contextObject)
    {
        
        this (viewer,
              type,
              "*",
              contextObject);
                        
    }

    public ProjectEvent (AbstractProjectViewer viewer,
                         DataObject            contextObject,
                         String                action)
    {
        
        this (viewer,
              contextObject.getObjectType (),
              action,
              contextObject);
                        
    }
    
    public Object getContextObject ()
    {
        
        return this.contextObject;
        
    }
    
    public String getAction ()
    {
        
        return this.action;
        
    }
    
    public String getType ()
    {
        
        return this.type;
        
    }

    public String toString ()
    {
        
        return this.getEventId ();
        
    }

    public String getEventId ()
    {
        
        return this.type + (this.action != null ? this.action : "");
        
    }
    
    public AbstractProjectViewer getProjectViewer ()
    {
        
        return this.viewer;
        
    }
    
}