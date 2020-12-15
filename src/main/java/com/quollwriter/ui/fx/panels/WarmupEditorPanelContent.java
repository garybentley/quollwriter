package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WarmupEditorPanelContent extends ChapterEditorPanelContent<WarmupProjectViewer> implements ToolBarSupported
{

    private Warmup warmup = null;

    public WarmupEditorPanelContent (WarmupProjectViewer viewer,
                                     Warmup              warmup)
                              throws GeneralException
    {

        super (viewer,
               warmup.getChapter (),
               viewer.getTextProperties ());

        this.warmup = warmup;

        this.editor.setEditable (true);

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "chapteredit");

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "warmupedit");

    }

    public Warmup getWarmup ()
    {

        return this.warmup;

    }

    public void startWarmup ()
    {

        this.viewer.showTimer (this.warmup);

    }

    @Override
    public Map<KeyCombination, Runnable> getActionMappings ()
    {

        Map<KeyCombination, Runnable> ret = new HashMap<> ();

        return ret;

    }

    public void saveWarmup ()
    {

        try
        {

            this.saveObject ();

        } catch (Exception e) {

            Environment.logError ("Unable to save warmup: " + this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (warmups,actions,save,actionerror));

        }

    }

    @Override
    public Set<Node> getToolBarItems ()
    {

        List<String> prefix = Arrays.asList (warmups,editorpanel,LanguageStrings.toolbar);

        Set<Node> its = new LinkedHashSet<> ();

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,save,tooltip))
            .iconName (StyleClassNames.SAVE)
            .onAction (ev ->
            {

                this.saveWarmup ();

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,_new,tooltip))
            .iconName (StyleClassNames.NEW)
            .onAction (ev ->
            {

                this.viewer.runCommand (WarmupProjectViewer.CommandId.warmup);

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,convert,tooltip))
            .iconName (StyleClassNames.CONVERT)
            .onAction (ev ->
            {

                this.viewer.convertWarmupToProject (this.warmup);

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,wordcount,tooltip))
            .iconName (StyleClassNames.WORDCOUNT)
            .onAction (ev ->
            {

                this.viewer.runCommand (AbstractProjectViewer.CommandId.showwordcounts);

            })
            .build ());

        QuollButton sb = QuollButton.builder ()
            .tooltip (Utils.newList (prefix,this.viewer.isSpellCheckingEnabled () ? spellcheckoff : spellcheckon,tooltip))
            .iconName (StyleClassNames.SPELLCHECK)
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.togglespellchecking);

            })
            .build ();

        this.addChangeListener (this.viewer.spellCheckingEnabledProperty (),
                                (pr, oldv, newv) ->
        {

            sb.setIconName (this.viewer.isSpellCheckingEnabled () ? StyleClassNames.SPELLCHECKOFF : StyleClassNames.SPELLCHECKON);
            //sb.pseudoClassStateChanged (StyleClassNames.ENABLED_PSEUDO_CLASS, this.viewer.isSpellCheckingEnabled ());
            //sb.pseudoClassStateChanged (StyleClassNames.DISABLED_PSEUDO_CLASS, !this.viewer.isSpellCheckingEnabled ());

            UIUtils.setTooltip (sb,
                                getUILanguageStringProperty (Utils.newList (prefix,this.viewer.isSpellCheckingEnabled () ? spellcheckoff : spellcheckon,tooltip)));

        });

        sb.setIconName (this.viewer.isSpellCheckingEnabled () ? StyleClassNames.SPELLCHECKOFF : StyleClassNames.SPELLCHECKON);
        //sb.pseudoClassStateChanged (StyleClassNames.ENABLED_PSEUDO_CLASS, this.viewer.isSpellCheckingEnabled ());
        //sb.pseudoClassStateChanged (StyleClassNames.DISABLED_PSEUDO_CLASS, !this.viewer.isSpellCheckingEnabled ());

        its.add (sb);

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,delete,tooltip))
            .iconName (StyleClassNames.DELETE)
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.deletechapter,
                                        this.object);

            })
            .build ());

        its.add (QuollMenuButton.builder ()
            .tooltip (Utils.newList (prefix,tools,tooltip))
            .iconName (StyleClassNames.TOOLS)
            .items (() ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                List<String> mprefix = Arrays.asList (warmups,editorpanel,tools);

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix,textproperties,text)))
                    .iconName (StyleClassNames.EDITPROPERTIES)
                    .accelerator (new KeyCharacterCombination ("E",
                                                               KeyCombination.SHORTCUT_DOWN))
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.textproperties);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix,find,text)))
                    .iconName (StyleClassNames.FIND)
                    .accelerator (new KeyCharacterCombination ("F",
                                                               KeyCombination.SHORTCUT_DOWN))
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.find);

                    })
                    .build ());
/*
TODO
                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix,print,text)))
                    .styleClassName (StyleClassNames.PRINT)
                    .accelerator (new KeyCharacterCombination ("P",
                                                               KeyCombination.SHORTCUT_DOWN))
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.print,
                                                null,
                                                this.object);

                    })
                    .build ());
*/
                return items;

            })
            .build ());

        return its;

    }

    @Override
    public Set<MenuItem> getContextMenuItems (boolean    compress)
    {

        Set<MenuItem> ret = this.editor.getSpellingSynonymItemsForContextMenu (this.viewer);//new LinkedHashSet<> ();

        final WarmupEditorPanelContent _this = this;

        // Get the mouse position, don't get it later since the mouse could have moved.
        Point2D mP = this.editor.getMousePosition ();
/*
TODO
        if (mP == null)
        {

            mP = this.iconColumn.getMousePosition ();

        }
*/
        //final Point mouseP = mP;

        //int pos = this.getTextPositionForMousePosition (mP);
        int pos = this.editor.getTextPositionForCurrentMousePosition ();

        // This is needed to move to the correct character, the call above seems to get the character
        // before what was clicked on.
        // pos++;

        List<String> prefix = Arrays.asList (warmups,editorpanel,popupmenu,items);

        if (compress)
        {

            List<Node> row1 = new ArrayList<> ();
            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.SAVE)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,save,tooltip)))
                .onAction (ev ->
                {

                    this.saveWarmup ();

                })
                .build ());

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.CONVERT)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,convert,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.convertWarmupToProject (this.warmup);

                })
                .build ());

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.EDITPROPERTIES)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,textproperties,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.textproperties);

                })
                .build ());

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.FIND)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,find,tooltip)))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.find);

                })
                .build ());

            CustomMenuItem n = UIUtils.createCompressedMenuItem (getUILanguageStringProperty (project,editorpanel,popupmenu,Chapter.OBJECT_TYPE,compresstext),
                                                                 row1);

            ret.add (n);

            ret.add (new SeparatorMenuItem ());

            MenuItem fmi = this.editor.getCompressedFormatItemsForContextMenu ();
            MenuItem emi = this.editor.getCompressedEditItemsForContextMenu ();

            if ((fmi != null)
                ||
                (emi != null)
               )
            {

                ret.add (new SeparatorMenuItem ());

                if (fmi != null)
                {

                    ret.add (fmi);

                }

                if (emi != null)
                {

                    ret.add (emi);

                }

            }

        } else {

            // Save.
            ret.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.SAVE)
                .label (getUILanguageStringProperty (Utils.newList (prefix,save,text)))
                .accelerator (new KeyCodeCombination (KeyCode.S,
                                                      KeyCombination.SHORTCUT_DOWN))
                .onAction (ev ->
                {

                    this.saveWarmup ();

                })
                .build ());

            ret.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.CONVERT)
                .label (getUILanguageStringProperty (Utils.newList (prefix,convert,text)))
                .onAction (ev ->
                {

                    this.viewer.convertWarmupToProject (this.warmup);

                })
                .build ());

            Set<MenuItem> citems = new LinkedHashSet<> ();

            // Add the new items.
            // TODO
            citems.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.EDITPROPERTIES)
                .label (getUILanguageStringProperty (Utils.newList (prefix,textproperties,text)))
                .accelerator (new KeyCodeCombination (KeyCode.E,
                                                      KeyCombination.SHORTCUT_DOWN))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.textproperties);

                })
                .build ());

            citems.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.FIND)
                .label (getUILanguageStringProperty (Utils.newList (prefix,find,text)))
                .accelerator (new KeyCodeCombination (KeyCode.F,
                                                      KeyCombination.SHORTCUT_DOWN))
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.find);

                })
                .build ());

            QuollMenu m = QuollMenu.builder ()
                .styleClassName (StyleClassNames.EDIT)
                .label (project,editorpanel,popupmenu,Chapter.OBJECT_TYPE,text)
                .items (citems)
                .build ();

            ret.add (m);

            citems = new LinkedHashSet<> ();

            List<String> mprefix = Arrays.asList (project,editorpanel,popupmenu,_new,items);

            citems.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (mprefix, Chapter.OBJECT_TYPE,text)))
                .iconName (Chapter.OBJECT_TYPE)
                .accelerator (Environment.getNewObjectTypeKeyCombination (Chapter.OBJECT_TYPE))
                .onAction (eev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.newchapter,
                                            null,
                                            this.object);

                })
                .build ());

            m = QuollMenu.builder ()
                .styleClassName (StyleClassNames.NEW)
                .label (project,editorpanel,popupmenu,_new,text)
                .items (citems)
                .build ();

            ret.add (m);

            String sel = this.editor.getSelectedText ();

            if (!sel.equals (""))
            {

                ret.add (new SeparatorMenuItem ());

                List<String> fprefix = Arrays.asList (formatting,format,popupmenu,items);

                // Add the bold/italic/underline.
                ret.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (fprefix,bold,text)))
                    .iconName (StyleClassNames.BOLD)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.BOLD_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.toggleBold ();

                    })
                    .build ());

                ret.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (fprefix,italic,text)))
                    .iconName (StyleClassNames.ITALIC)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.ITALIC_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.toggleItalic ();

                    })
                    .build ());

                ret.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (fprefix,underline,text)))
                    .iconName (StyleClassNames.UNDERLINE)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.UNDERLINE_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.toggleUnderline ();

                    })
                    .build ());

            }

            Set<MenuItem> eitems = new LinkedHashSet<> ();

            List<String> eprefix = Arrays.asList (formatting,edit,popupmenu,items);

            if (!sel.equals (""))
            {

                eitems.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (eprefix,cut,text)))
                    .iconName (StyleClassNames.CUT)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.CUT_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.cut ();

                    })
                    .build ());

                eitems.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (eprefix,copy,text)))
                    .iconName (StyleClassNames.COPY)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.COPY_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.copy ();

                    })
                    .build ());

            }

            // Only show if there is something in the clipboard.
            if (UIUtils.clipboardHasContent ())
            {

                eitems.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (eprefix,paste,text)))
                    .iconName (StyleClassNames.PASTE)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.PASTE_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.paste ();

                    })
                    .build ());

            }

            if (this.editor.getUndoManager ().isUndoAvailable ())
            // TODO if (this.editor.getUndoManager ().canUndo ())
            {

                eitems.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (eprefix,undo,text)))
                    .iconName (StyleClassNames.UNDO)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.UNDO_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.getUndoManager ().undo ();

                    })
                    .build ());

            }

            if (this.editor.getUndoManager ().isRedoAvailable ())
            // TODO if (this.editor.getUndoManager ().canRedo ())
            {

                eitems.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (eprefix,redo,text)))
                    .iconName (StyleClassNames.REDO)
                    //.accelerator (Environment.getActionKeyCombination (QTextEditor.REDO_ACTION_NAME))
                    .onAction (ev ->
                    {

                        this.editor.getUndoManager ().redo ();

                    })
                    .build ());

            }

            if (eitems.size () > 0)
            {

                ret.add (new SeparatorMenuItem ());
                ret.addAll (eitems);

            }

        }

        return ret;

    }

}
