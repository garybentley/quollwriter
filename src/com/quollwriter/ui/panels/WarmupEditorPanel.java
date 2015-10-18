package com.quollwriter.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QTextEditor;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public class WarmupEditorPanel extends AbstractEditableEditorPanel
{

     public static final String CONVERT_TO_PROJECT_ACTION_NAME = "convert-to-project";
     public static final String DELETE_WARMUP_ACTION_NAME = "delete-warmup";
     
    protected WarmupsViewer projectViewer = null;
    private Warmup          warmup = null;

    public WarmupEditorPanel(WarmupsViewer pv,
                             Warmup        w)
                      throws GeneralException
    {

        super (pv,
               w.getChapter ());

        this.projectViewer = pv;
        this.warmup = w;

        this.actions.put (DELETE_WARMUP_ACTION_NAME,
                          new DeleteWarmupActionHandler ((Chapter) this.obj,
                                                         this.projectViewer));
        
        this.actions.put (CONVERT_TO_PROJECT_ACTION_NAME,
                          this.projectViewer.getAction (WarmupsViewer.CONVERT_TO_PROJECT_ACTION,
                                                        this.warmup.getChapter ()));
        
     }

    public Warmup getWarmup ()
    {
      
         return this.warmup;
      
    }

    public JComponent getEditorWrapper (final QTextEditor q)
    {

        FormLayout fl = new FormLayout ("fill:200px:grow",
                                        "fill:p:grow");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (q,
                     cc.xy (1,
                            1));

        JPanel p = builder.getPanel ();
        p.setBorder (null);

        return p;

    }

    public void doFillToolsPopupMenu (ActionEvent ev,
                                      JPopupMenu  p)
    {

    }

    @Override
    public void fillToolBar (JToolBar      acts,
                             final boolean fullScreen)
    {

        final AbstractEditableEditorPanel _this = this;

        acts.add (this.createToolbarButton (Constants.SAVE_ICON_NAME,
                                            "Click to save the {Warmup} text",
                                            SAVE_ACTION_NAME));

        this.doFillToolBar (acts);

        acts.add (this.createToolbarButton (Constants.WORDCOUNT_ICON_NAME,
                                            "Click to view the word counts and readability indices",
                                            TOGGLE_WORDCOUNTS_ACTION_NAME));

        String type = (this.projectViewer.isSpellCheckingEnabled () ? "off" : "on");

        acts.add (this.createToolbarButton ("spellchecker-turn-" + type,
                                            "Click to turn the spell checker " + type,
                                            TOGGLE_SPELLCHECK_ACTION_NAME));

        acts.add (this.createToolbarButton (Constants.DELETE_ICON_NAME,
                                            "Click to delete this {Warmup}",
                                            DELETE_WARMUP_ACTION_NAME));

        // Add a tools menu.
        final JButton b = UIUtils.createToolBarButton ("tools",
                                                       "Click to view the tools menu",
                                                       "tools",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                _this.doFillToolsPopupMenu (ev,
                                            m);

                JMenuItem mi = null;

                m.add (_this.createMenuItem ("Edit Text Properties",
                                             Constants.EDIT_PROPERTIES_ICON_NAME,
                                             EDIT_TEXT_PROPERTIES_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                                                     ActionEvent.CTRL_MASK)));
                        
                m.add (_this.createMenuItem ("Find",
                                             Constants.FIND_ICON_NAME,
                                             Constants.SHOW_FIND_ACTION,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                                     ActionEvent.CTRL_MASK)));

                m.add (_this.createMenuItem ("Print {Warmup}",
                                             Constants.PRINT_ICON_NAME,
                                             QTextEditor.PRINT_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                                     ActionEvent.CTRL_MASK)));
                                                                       
                m.show (b,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);

    }
    
    @Override
    public void doFillToolBar (JToolBar acts)
    {

        final WarmupEditorPanel _this = this;
    
        acts.add (UIUtils.createToolBarButton (Constants.NEW_ICON_NAME,
                                               "Click to start a new {warmup}",
                                               "addwarmup",
                                               new ActionAdapter ()
                                               {
                                                  
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                                       
                                                       _this.projectViewer.showWarmupPromptSelect ();
                                                                                                              
                                                   }
                                                  
                                               }));
    
        acts.add (UIUtils.createToolBarButton (Constants.CONVERT_ICON_NAME,
                                               "Click to convert this {warmup} to a " + Environment.getObjectTypeName (Project.OBJECT_TYPE),
                                               "convert",
                                               this.projectViewer.getAction (WarmupsViewer.CONVERT_TO_PROJECT_ACTION,
                                                                             this.warmup.getChapter ())));

    }

    public void doFillPopupMenu (final MouseEvent ev,
                                 final JPopupMenu popup,
                                       boolean    compress)
    {

        final WarmupEditorPanel _this = this;
    
        if (compress)
        {
                         
            List<JComponent> buts = new ArrayList ();

            buts.add (this.createButton (Constants.SAVE_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Save {Warmup}",
                                         SAVE_ACTION_NAME));

            buts.add (this.createButton (Constants.CONVERT_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Convert to a {Project}",
                                         CONVERT_TO_PROJECT_ACTION_NAME));
            
            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find",
                                         Constants.SHOW_FIND_ACTION));
                                                                                        
            popup.add (UIUtils.createPopupMenuButtonBar (Environment.replaceObjectNames ("{Warmup}"),
                                                         popup,
                                                         buts));
                                            
        } else {    

          JMenuItem mi = null;
        
          mi = this.createMenuItem ("Save {Warmup}",
                                    Constants.SAVE_ICON_NAME,
                                    SAVE_ACTION_NAME,
                                    KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                                            ActionEvent.CTRL_MASK));
          mi.setMnemonic (KeyEvent.VK_S);

          popup.add (mi);
            
          popup.add (this.createMenuItem ("Convert to a {Project}",
                                          Constants.CONVERT_ICON_NAME,
                                          CONVERT_TO_PROJECT_ACTION_NAME,
                                          null));
  
        }
          
    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.editor);

        return l;

    }

    @Override
    public void close ()
    {

    }

    @Override
    public void init ()
               throws GeneralException
     {

          super.init ();
        
        this.setReadyForUse (true);                  
                  
    }

    public void startWarmup ()
    {
    
        this.projectViewer.getWordCountTimer ().start (this.warmup.getMins (),
                                                       this.warmup.getWords ());

    }

}
