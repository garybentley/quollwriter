package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

public class EditorProjectSideBar extends ProjectSideBar
{
    
    public EditorProjectSideBar (AbstractProjectViewer v)
    {
        
        super (v,
               null);
        
    }
                 
    public void init ()
    {
        
        super.init ();

        this.addAccordionItem (new EditorChaptersAccordionItem (this.projectViewer));
        
    }
     
}