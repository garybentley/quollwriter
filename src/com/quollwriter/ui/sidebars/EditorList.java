package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.*;
import java.awt.event.*;

import java.io.*;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.activation.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.*;

import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.ActionAdapter;

public class EditorList extends Box//ScrollableBox
{
    
    private EditorsSideBar edSideBar = null;
    
    public EditorList (EditorsSideBar sb)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.edSideBar = sb;
        this.setOpaque (true);
        this.setBackground (UIUtils.getComponentColor ());

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
        
        List<EditorEditor> eds = new ArrayList ();

        eds.add (new EditorEditor ("Susan Stephen",
                             "Suze",
                             new FileDataSource (new File ("D:/development/quollwriter/main/imgs/bgs/thumbs/" + "bamboo-3.jpg")),
                             EditorEditor.Status.busy,
                             EditorEditor.EditorStatus.pending));
                             
        AccordionItem ai = new AccordionItem ("Pending Editors",
                                              null,
                                              this.edSideBar.createEditorsList (eds));
        
        ai.init ();

        this.add (ai);
        
        eds = new ArrayList ();
        
        eds.add (new EditorEditor ("John Watson",
                             "Johnno",
                             new FileDataSource (new File ("D:/development/quollwriter/main/imgs/bgs/thumbs/" + "clouds-3.jpg")),
                             EditorEditor.Status.online,
                             EditorEditor.EditorStatus.current));
        
        eds.add (new EditorEditor ("Sherlock Holmes",
                             "Sherly",
                             new FileDataSource (new File ("D:/development/quollwriter/main/imgs/bgs/thumbs/" + "orange-flowers-2.jpg")),
                             EditorEditor.Status.offline,
                             EditorEditor.EditorStatus.current));

        eds.add (new EditorEditor ("Juliette Morris",
                             "Julie",
                             new FileDataSource (new File ("D:/development/quollwriter/main/imgs/bgs/thumbs/" + "pink-squares-2.gif")),
                             EditorEditor.Status.busy,
                             EditorEditor.EditorStatus.current));

        ai = new AccordionItem ("Current Editors",
                                null,
                                this.edSideBar.createEditorsList (eds));
        ai.init ();

        this.add (ai);

        eds = new ArrayList ();
        
        eds.add (new EditorEditor ("Michael Caine",
                             "Mike",
                             new FileDataSource (new File ("D:/development/quollwriter/main/imgs/bgs/thumbs/" + "blue-snowflakes-1.jpg")),
                             null,
                             EditorEditor.EditorStatus.previous));
        
        ai = new AccordionItem ("Previous Editors",
                                null,
                                this.edSideBar.createEditorsList (eds));
        ai.init ();

        this.add (ai);
        
        this.add (UIUtils.createOpaqueGlue (BoxLayout.Y_AXIS));

    }
        
    public void init ()
    {
        
        
        
    }
    
}