package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.DocumentAdapter;
import com.quollwriter.ui.components.StyleChangeAdapter;
import com.quollwriter.ui.components.StyleChangeEvent;
import com.quollwriter.ui.components.StyleChangeListener;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.Runner;
import com.quollwriter.ui.components.TextStylable;
import com.quollwriter.ui.components.TextProperties;

public abstract class AbstractViewOnlyEditorPanel extends AbstractEditorPanel
{

    public AbstractViewOnlyEditorPanel (AbstractProjectViewer pv,
                                        Chapter               c)
                                 throws GeneralException
    {

        super (pv,
               c);

    }

    public abstract void doFillToolBar (JToolBar b);

    public abstract void doFillPopupMenu (MouseEvent eve,
                                          JPopupMenu p,
                                          boolean    compress);

    public abstract void doFillToolsPopupMenu (ActionEvent eve,
                                               JPopupMenu  p);

    @Override
    public boolean saveUnsavedChanges ()
    {

        return true;

    }

    @Override
    public void close ()
    {

        super.close ();

    }

    @Override
    public void init ()
               throws GeneralException
    {

        super.init ();

    }

    public void fillToolBar (JToolBar acts,
                             final boolean  fullScreen)
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.viewonlypanel);
        prefix.add (LanguageStrings.toolbar);

        final AbstractViewOnlyEditorPanel _this = this;

        this.doFillToolBar (acts);

        acts.add (this.createToolbarButton (Constants.WORDCOUNT_ICON_NAME,
                                            Environment.getUIString (prefix,
                                                                     LanguageStrings.wordcount,
                                                                     LanguageStrings.tooltip),
                                 //"Click to view the word counts and readability indices",
                                            TOGGLE_WORDCOUNTS_ACTION_NAME));

        if (this.viewer.isSpellCheckingEnabled ())
        {

            acts.add (this.createToolbarButton ("spellchecker-turn-off",
                                                Environment.getUIString (prefix,
                                                                         LanguageStrings.spellcheckoff,
                                                                         LanguageStrings.tooltip),
                                                //"Click to turn the spell checker " + type,
                                                TOGGLE_SPELLCHECK_ACTION_NAME));

        } else {

            acts.add (this.createToolbarButton ("spellchecker-turn-on",
                                                Environment.getUIString (prefix,
                                                                         LanguageStrings.spellcheckon,
                                                                         LanguageStrings.tooltip),
                                                //"Click to turn the spell checker " + type,
                                                TOGGLE_SPELLCHECK_ACTION_NAME));

        }
/*
        String type = (this.viewer.isSpellCheckingEnabled () ? "off" : "on");

        acts.add (this.createToolbarButton ("spellchecker-turn-" + type,
                                            "Click to turn the spell checker " + type,
                                            TOGGLE_SPELLCHECK_ACTION_NAME));
*/
        // Add a tools menu.
        final JButton b = UIUtils.createToolBarButton ("tools",
                                                       Environment.getUIString (prefix,
                                                                                LanguageStrings.tools,
                                                                                LanguageStrings.tooltip),
                                                       //"Click to view the tools such as Print and Edit the text properties",
                                                       "tools",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.project);
                prefix.add (LanguageStrings.viewonlypanel);
                prefix.add (LanguageStrings.tools);

                JPopupMenu m = new JPopupMenu ();

                _this.doFillToolsPopupMenu (ev,
                                            m);

                JMenuItem mi = null;

                m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.textproperties,
                                                                      LanguageStrings.text),
                                            //"Edit Text Properties",
                                             Constants.EDIT_PROPERTIES_ICON_NAME,
                                             EDIT_TEXT_PROPERTIES_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                                                     ActionEvent.CTRL_MASK)));

                m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.find,
                                                                      LanguageStrings.text),
                                            //"Find",
                                             Constants.FIND_ICON_NAME,
                                             Constants.SHOW_FIND_ACTION,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                                     ActionEvent.CTRL_MASK)));

                m.show (b,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);

    }

    public void saveObject ()
                     throws Exception
    {

        throw new UnsupportedOperationException ("Not supported for view only editor panels.");

    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

        final QTextEditor         editor = this.editor;
        final AbstractViewOnlyEditorPanel _this = this;

        Point p = this.editor.getMousePosition ();

        this.lastMousePosition = p;

        JMenuItem mi = null;

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        this.doFillPopupMenu (ev,
                              popup,
                              compress);

    }

}
