package com.quollwriter.ui.fx.panels;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.css.*;
import javafx.css.converter.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.concurrent.*;

import org.reactfx.*;
import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import com.quollwriter.StringWithMarkup;
import com.quollwriter.UserProperties;
import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.Constants;
import com.quollwriter.LanguageStrings;
import com.quollwriter.Environment;
import com.quollwriter.GeneralException;
import com.quollwriter.DictionaryProvider2;
import com.quollwriter.Utils;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.text.TextIterator;
import com.quollwriter.text.Word;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProjectChapterEditorPanelContent extends ChapterEditorWithMarginPanelContent<ProjectViewer> implements ToolBarSupported
{

    //private IconColumn iconColumn = null;
    //private QTextEditor editor = null;
    private boolean isScrolling = false;
    private ProblemFinder problemFinder = null;

    public ProjectChapterEditorPanelContent (ProjectViewer viewer,
                                             Chapter       chapter)
                                      throws GeneralException
    {

        super (viewer,
               chapter);

        this.editor.setEditable (true);

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               "chapteredit");
/*
TODO
        this.addChangeListener (chapter.editPositionProperty (),
                                (pr, oldv, newv) ->
        {

            this.recreateVisibleParagraphs ();

        });

        this.addChangeListener (chapter.editCompleteProperty (),
                                               (pr, oldv, newv) ->
        {

            this.recreateVisibleParagraphs ();

        });
*/

/*
TODO
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        InputEvent.CTRL_DOWN_MASK),
                EDIT_TEXT_PROPERTIES_ACTION_NAME);
*/

        this.editor.readyForUseProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {

                return;

            }

            UIUtils.forceRunLater (() ->
            {

                this.object.getScenes ().stream ()
                    .forEach (s ->
                    {

                        this.createTextPosition (s);

                        s.getOutlineItems ().stream ()
                            .forEach (o -> this.createTextPosition (o));

                    });

                this.object.getOutlineItems ().stream ()
                    .forEach (o -> this.createTextPosition (o));

                this.object.getNotes ().stream ()
                    .forEach (n -> this.createTextPosition (n));

            });

        });

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.P, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 this.showProblemFinder ();

                                             }));

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 this.viewer.createNewScene (this.object,
                                                                             this.editor.getCaretPosition ());

                                             }));

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                  this.viewer.createNewOutlineItem (this.object,
                                                                                    this.editor.getCaretPosition ());

                                             }));
        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                   this.viewer.createNewEditNeededNote (this.object,
                                                                                        this.editor.getCaretPosition ());

                                             }));

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.N, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                   this.viewer.createNewNote (this.object,
                                                                              this.editor.getCaretPosition ());

                                             }));

    }

    @Override
    public Node getMarginNodeForChapterItem (ChapterItem ci)
    {

        if ((ci instanceof com.quollwriter.data.Scene)
            ||
            (ci instanceof OutlineItem)
           )
        {

            IconBox riv = IconBox.builder ()
                .iconName (((ci instanceof com.quollwriter.data.Scene) ? StyleClassNames.SCENE : StyleClassNames.OUTLINEITEM))
                .build ();

            SimpleStringProperty prop =  new SimpleStringProperty ();
            prop.bind (javafx.beans.binding.Bindings.createStringBinding (() ->
            {

                if (ci.getDescription () == null)
                {

                    return null;

                }

                return ci.getDescription ().getMarkedUpText ();

            },
            ci.descriptionProperty ()));

            UIUtils.setTooltip (riv,
                                prop);

            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showItem (ci,
                               true);
                ev.consume ();

            });

            return riv;

        }

        if (ci instanceof Note)
        {

            Note n = (Note) ci;

            IconBox riv = IconBox.builder ()
                .iconName ((n.isEditNeeded () ? StyleClassNames.EDITNEEDEDNOTE : StyleClassNames.NOTE))
                .build ();

            riv.setOnMouseDragged (ev ->
            {

                riv.requestFocus ();

            });
            riv.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showItem (n,
                               true);

                ev.consume ();

            });

            return riv;

        }

        throw new UnsupportedOperationException ("Object not supported: " + ci);

    }

    @Override
    public Set<MenuItem> getMarginContextMenuItems (int cpos)
    {

        List<String> prefix = Arrays.asList (iconcolumn,doubleclickmenu,items);

        Set<MenuItem> items = new LinkedHashSet<> ();

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Scene.OBJECT_TYPE)))
            .iconName (StyleClassNames.SCENE)
            .accelerator (new KeyCharacterCombination ("S",
                                                       KeyCombination.SHORTCUT_DOWN,
                                                       KeyCombination.SHIFT_DOWN))
            .onAction (eev ->
            {

                this.viewer.createNewScene (this.object,
                                            cpos);

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.OutlineItem.OBJECT_TYPE)))
            .iconName (StyleClassNames.OUTLINEITEM)
            .accelerator (new KeyCharacterCombination ("O",
                                                       KeyCombination.SHORTCUT_DOWN,
                                                       KeyCombination.SHIFT_DOWN))
            .onAction (eev ->
            {

                this.viewer.createNewOutlineItem (this.object,
                                                  cpos);

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Note.OBJECT_TYPE)))
            .iconName (StyleClassNames.NOTE)
            .accelerator (new KeyCharacterCombination ("N",
                                                       KeyCombination.SHORTCUT_DOWN,
                                                       KeyCombination.SHIFT_DOWN))
            .onAction (eev ->
            {

                this.viewer.createNewNote (this.object,
                                           cpos);

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.editneedednote)))
            .accelerator (new KeyCharacterCombination ("E",
                                                       KeyCombination.SHORTCUT_DOWN,
                                                       KeyCombination.SHIFT_DOWN))
            .iconName (StyleClassNames.EDITNEEDEDNOTE)
            .onAction (eev ->
            {

                this.viewer.createNewEditNeededNote (this.object,
                                                     cpos);

            })
            .build ());

         return items;

    }

    @Override
    public void createTextPosition (ChapterItem ci)
    {

        if (ci instanceof com.quollwriter.data.Scene)
        {

            com.quollwriter.data.Scene s = (com.quollwriter.data.Scene) ci;
            s.getOutlineItems ().stream ()
                .forEach (o -> this.createTextPosition (o));

        }

        super.createTextPosition (ci);

    }

    @Override
    public Map<KeyCombination, Runnable> getActionMappings ()
    {

        Map<KeyCombination, Runnable> ret = new HashMap<> ();

        ret.put (new KeyCodeCombination (KeyCode.ENTER,
                                         KeyCombination.SHORTCUT_DOWN),
                 () ->
                 {

                     this.insertSectionBreak ();

                 });

        ret.put (new KeyCodeCombination (KeyCode.L,
                                         KeyCombination.SHORTCUT_DOWN),
                 () ->
                 {

                     this.runCommand (ProjectViewer.CommandId.togglespellchecking);

                 });

        ret.put (new KeyCodeCombination (KeyCode.W,
                                         KeyCombination.SHORTCUT_DOWN),
                 () ->
                 {

                     this.viewer.runCommand (ProjectViewer.CommandId.showwordcounts);

                 });

        return ret;

    }

    @Override
    public Node getEditorWrapper (VirtualizedScrollPane<TextEditor> scrollPane)
    {

        this.problemFinder = new ProblemFinder (this);
        this.problemFinder.setVisible (false);
        VBox b = new VBox ();
        b.getChildren ().add (scrollPane);
        VBox.setVgrow (scrollPane,
                       Priority.ALWAYS);
        b.getChildren ().add (this.problemFinder);

        return b;

    }

    public void recreateVisibleParagraphs ()
    {

        int s = this.editor.getVisibleParagraphs ().size ();

        IntStream.range (0,
                         s)
            .forEach (i ->
            {

                if (i < s)
                {

                    this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                }

            });

    }

    private void updateParagraphForPosition (int pos)
    {

        if (pos < 0)
        {

            return;

        }

        int paraNo = this.editor.getParagraphForOffset (pos);

        this.editor.recreateParagraphGraphic (paraNo);

    }

    public void updateParagraphForItem (ChapterItem i)
    {

        if (i != null)
        {

            this.updateParagraphForPosition (i.getPosition ());

/*
            // Inform the margins.
            for (ParagraphIconMargin m : this.margins.keySet ())
            {

                if (m.getParagraph () == paraNo)
                {

                    m.requestLayout ();

                }

            }
*/
        }

    }

    @Override
    public boolean isToolBarConfigurable ()
    {

        return true;

    }

    @Override
    public Set<Node> getToolBarItems ()
    {

        List<String> prefix = Arrays.asList (project,editorpanel,LanguageStrings.toolbar);

        Set<Node> its = new LinkedHashSet<> ();

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,save,tooltip))
            .iconName (StyleClassNames.SAVE)
            .buttonId ("save")
            .onAction (ev ->
            {

                this.saveObject ();

            })
            .build ());

        its.add (QuollMenuButton.builder ()
            .tooltip (Utils.newList (prefix,_new,tooltip))
            .iconName (StyleClassNames.NEW)
            .buttonId ("new")
            .items (() ->
            {

                List<String> mprefix = Arrays.asList (project,editorpanel,popupmenu,_new,items);

                Set<MenuItem> items = new LinkedHashSet<> ();

                /*
                mi.setMnemonic ('S');
                mi.setToolTipText (pref + "S");
                */
                String pref = getUILanguageStringProperty (general,shortcutprefix).getValue ();

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, com.quollwriter.data.Scene.OBJECT_TYPE,text)))
                    .iconName (com.quollwriter.data.Scene.OBJECT_TYPE)
                    //.tooltip (new SimpleStringProperty (pref + "S"))
                    .accelerator (Environment.getNewObjectTypeKeyCombination (com.quollwriter.data.Scene.OBJECT_TYPE))
                    .onAction (eev ->
                    {

                        this.viewer.createNewScene (this.object,
                                                    this.editor.getCaretPosition ());

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, OutlineItem.OBJECT_TYPE,text)))
                    .iconName (OutlineItem.OBJECT_TYPE)
                    //.tooltip (new SimpleStringProperty (pref + "O"))
                    .accelerator (Environment.getNewObjectTypeKeyCombination (OutlineItem.OBJECT_TYPE))
                    .onAction (eev ->
                    {

                        this.viewer.createNewOutlineItem (this.object,
                                                          this.editor.getCaretPosition ());

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.OBJECT_TYPE,text)))
                    .iconName (Note.OBJECT_TYPE)
                    .accelerator (Environment.getNewObjectTypeKeyCombination (Note.OBJECT_TYPE))
                    .onAction (eev ->
                    {

                        this.viewer.createNewNote (this.object,
                                                   this.editor.getCaretPosition ());

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.EDIT_NEEDED_OBJECT_TYPE,text)))
                    .iconName (StyleClassNames.EDITNEEDEDNOTE)
                    .accelerator (Environment.getNewObjectTypeKeyCombination (Note.EDIT_NEEDED_OBJECT_TYPE))
                    .onAction (eev ->
                    {

                        this.viewer.createNewEditNeededNote (this.object,
                                                             this.editor.getCaretPosition ());

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
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

                items.addAll (UIUtils.getNewAssetMenuItems (this.viewer));

                return items;

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,showchapterinfo,tooltip))
            .iconName (StyleClassNames.INFO)
            .buttonId ("chapterinfo")
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.showchapterinfo,
                                        this.object);

            })
            .build ());

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,wordcount,tooltip))
            .iconName (StyleClassNames.WORDCOUNT)
            .buttonId ("wordcount")
            .onAction (ev ->
            {

                this.viewer.runCommand (AbstractProjectViewer.CommandId.showwordcounts);

            })
            .build ());

        QuollButton sb = QuollButton.builder ()
            .tooltip (Utils.newList (prefix,this.viewer.isSpellCheckingEnabled () ? spellcheckoff : spellcheckon,tooltip))
            .iconName (StyleClassNames.SPELLCHECK)
            .buttonId ("spellcheck")
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

        its.add (UIUtils.createTagsMenuButton (this.object,
                                               getUILanguageStringProperty (Utils.newList (prefix,tags,tooltip)),
                                               this.viewer));

        its.add (QuollButton.builder ()
            .tooltip (Utils.newList (prefix,delete,tooltip))
            .iconName (StyleClassNames.DELETE)
            .buttonId ("delete")
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.deletechapter,
                                        this.object);

            })
            .build ());

        its.add (QuollMenuButton.builder ()
            .tooltip (Utils.newList (prefix,tools,tooltip))
            .iconName (StyleClassNames.TOOLS)
            .buttonId ("other")
            .items (() ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                List<String> mprefix = Arrays.asList (project,editorpanel,tools);

                if (this.viewer.isProjectLanguageEnglish ())
                {

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix,problemfinder,text)))
                        .iconName (StyleClassNames.PROBLEMFINDER)
                        .accelerator (new KeyCharacterCombination ("P",
                                                                   KeyCombination.SHORTCUT_DOWN,
                                                                   KeyCombination.SHIFT_DOWN))
                        .onAction (ev ->
                        {

                            this.showProblemFinder ();

                        })
                        .build ());

                }

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
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        this.getPanel ().addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                                          ev ->
        {

            this.itemsSubscription.unsubscribe ();
            this.itemsPositionSubscription.unsubscribe ();
            this.textPositions.keySet ().stream ()
                .forEach (ci ->
                {

                    ci.positionProperty ().unbind ();
                    this.textPositions.get (ci).dispose ();
                });
            this.endTextPositions.keySet ().stream ()
                .forEach (ci ->
                {

                    ci.endPositionProperty ().unbind ();
                    TextEditor.Position p = this.endTextPositions.get (ci);

                    if (p != null)
                    {

                        p.dispose ();

                    }

                });
            this.textPositions.clear ();
            this.endTextPositions.clear ();

            this.editor.dispose ();

        });

    }

    @Override
    public void saveObject ()
    {

        try
        {

            super.saveObject ();

        } catch (Exception e) {

            Environment.logError ("Unable to save chapter: " + this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (project,editorpanel,actions,save,actionerror));

        }

    }

    @Override
    public Set<MenuItem> getContextMenuItems (boolean    compress)
    {

        Set<MenuItem> ret = this.editor.getSpellingSynonymItemsForContextMenu (this.viewer);//new LinkedHashSet<> ();

        final ProjectChapterEditorPanelContent _this = this;

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

        List<String> prefix = Arrays.asList (project,editorpanel,popupmenu,Chapter.OBJECT_TYPE,items);

        if (compress)
        {

            List<Node> row1 = new ArrayList<> ();
            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.SAVE)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,save,tooltip)))
                .onAction (ev ->
                {

                    this.saveObject ();

                })
                .build ());

            if ((this.editor.getCaretPosition () > 0)
                ||
                (this.editor.getSelection ().getStart () > 0)
               )
            {

                if (this.editor.getCaretPosition () < this.editor.getTextWithMarkup ().getText ().length ())
               {

                   row1.add (QuollButton.builder ()
                       .iconName (StyleClassNames.SPLIT)
                       .tooltip (getUILanguageStringProperty (Utils.newList (prefix,splitchapter,tooltip)))
                       .onAction (ev ->
                       {

                           this.showSplitChapter ();

                       })
                       .build ());

               }

            }

            row1.add (QuollButton.builder ()
                .iconName (StyleClassNames.PROBLEMFINDER)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,problemfinder,tooltip)))
                .onAction (ev ->
                {

                    this.showProblemFinder ();

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

            List<Node> row2 = new ArrayList<> ();

            if (this.viewer.getCurrentChapterText (this.object).length () > 0)
            {

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.SETEDITPOSITION)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,seteditposition,tooltip)))
                    .onAction (ev ->
                    {

                        this.viewer.setChapterEditPosition (this.object,
                                                            pos);

                        this.recreateVisibleParagraphs ();

                    })
                    .build ());

            }

            if (this.object.getEditPosition () > 0)
            {

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.GOTOEDITPOSITION)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,gotoeditposition,tooltip)))
                    .onAction (ev ->
                    {

                        this.editor.moveTo (this.object.getEditPosition ());
                        this.editor.requestFollowCaret ();

                    })
                    .build ());

                row2.add (QuollButton.builder ()
                    .iconName (StyleClassNames.REMOVEEDITPOSITION)
                    .tooltip (getUILanguageStringProperty (Utils.newList (prefix,removeeditposition,tooltip)))
                    .onAction (ev ->
                    {

                        this.viewer.removeChapterEditPosition (this.object);

                        this.recreateVisibleParagraphs ();

                    })
                    .build ());

            }

            if (this.viewer.getCurrentChapterText (this.object).length () > 0)
            {

                if (!this.object.isEditComplete ())
                {

                    row2.add (QuollButton.builder ()
                        .iconName (StyleClassNames.EDITCOMPLETE)
                        .tooltip (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete,tooltip)))
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (this.object,
                                                                true);

                            IntStream.range (0,
                                             this.editor.getVisibleParagraphs ().size ())
                                .forEach (i ->
                                {

                                    this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                                });

                        })
                        .build ());

                } else {

                    row2.add (QuollButton.builder ()
                        .iconName (StyleClassNames.EDITNEEDED)
                        .tooltip (getUILanguageStringProperty (Utils.newList (prefix,seteditneeded,tooltip)))
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (this.object,
                                                                false);

                        })
                        .build ());

                }

            }

            CustomMenuItem n = UIUtils.createCompressedMenuItem (getUILanguageStringProperty (project,editorpanel,popupmenu,Chapter.OBJECT_TYPE,compresstext),
                                                                 row1,
                                                                 row2);

            ret.add (n);

            ret.add (new SeparatorMenuItem ());

            ret.add (this.getCompressedNewItemsForContextMenu (pos,
                                                               null,
                                                               null));

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

                    this.saveObject ();

                })
                .build ());

            Set<MenuItem> citems = new LinkedHashSet<> ();

            if ((this.editor.getCaretPosition () > 0)
                ||
                (this.editor.getSelection ().getStart () > 0)
               )
            {

                if (this.editor.getCaretPosition () < this.editor.getTextWithMarkup ().getText ().length ())
               {

                   citems.add (QuollMenuItem.builder ()
                       .iconName (StyleClassNames.SPLIT)
                       .label (getUILanguageStringProperty (Utils.newList (prefix,splitchapter,text)))
                       .onAction (ev ->
                       {

                           this.showSplitChapter ();

                       })
                       .build ());

               }

            }

            if (this.viewer.getCurrentChapterText (this.object).length () > 0)
            {

                citems.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.SETEDITPOSITION)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,seteditposition,text)))
                    .onAction (ev ->
                    {

                        this.viewer.setChapterEditPosition (this.object,
                                                            pos);

                        IntStream.range (0,
                                         this.editor.getVisibleParagraphs ().size ())
                            .forEach (i ->
                            {

                                this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                            });

                    })
                    .build ());

            }

            if (this.object.getEditPosition () > 0)
            {

                citems.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.GOTOEDITPOSITION)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,gotoeditposition,text)))
                    .onAction (ev ->
                    {

                        this.editor.moveTo (this.object.getEditPosition ());
                        this.editor.requestFollowCaret ();

                    })
                    .build ());

                citems.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.REMOVEEDITPOSITION)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,removeeditposition,text)))
                    .onAction (ev ->
                    {

                        this.viewer.removeChapterEditPosition (this.object);

                        IntStream.range (0,
                                         this.editor.getVisibleParagraphs ().size ())
                            .forEach (i ->
                            {

                                this.editor.recreateParagraphGraphic (this.editor.visibleParToAllParIndex (i));

                            });

                    })
                    .build ());

            }

            if (this.viewer.getCurrentChapterText (this.object).length () > 0)
            {

                if (!this.object.isEditComplete ())
                {

                    citems.add (QuollMenuItem.builder ()
                        .iconName (StyleClassNames.EDITCOMPLETE)
                        .label (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete,text)))
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (this.object,
                                                                true);

                        })
                        .build ());

                } else {

                    citems.add (QuollMenuItem.builder ()
                        .iconName (StyleClassNames.EDITNEEDED)
                        .label (getUILanguageStringProperty (Utils.newList (prefix,seteditneeded,text)))
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (this.object,
                                                                false);

                        })
                        .build ());

                }

            }

            citems.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.PROBLEMFINDER)
                .label (getUILanguageStringProperty (Utils.newList (prefix,problemfinder,text)))
                .accelerator (new KeyCodeCombination (KeyCode.P,
                                                      KeyCombination.SHORTCUT_DOWN,
                                                      KeyCombination.SHIFT_DOWN))
                .onAction (ev ->
                {

                    this.showProblemFinder ();

                })
                .build ());

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

            int cpos = (pos > -1 ? pos : this.editor.getCaretPosition ());

            citems.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (mprefix, com.quollwriter.data.Scene.OBJECT_TYPE,text)))
                .iconName (com.quollwriter.data.Scene.OBJECT_TYPE)
                //.tooltip (new SimpleStringProperty (pref + "S"))
                .accelerator (Environment.getNewObjectTypeKeyCombination (com.quollwriter.data.Scene.OBJECT_TYPE))
                .onAction (eev ->
                {

                    this.viewer.createNewScene (this.object,
                                                cpos);

                })
                .build ());

            citems.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (mprefix, OutlineItem.OBJECT_TYPE,text)))
                .iconName (OutlineItem.OBJECT_TYPE)
                //.tooltip (new SimpleStringProperty (pref + "O"))
                .accelerator (Environment.getNewObjectTypeKeyCombination (OutlineItem.OBJECT_TYPE))
                .onAction (eev ->
                {

                    this.viewer.createNewOutlineItem (this.object,
                                                      cpos);

                })
                .build ());

            citems.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.OBJECT_TYPE,text)))
                .iconName (Note.OBJECT_TYPE)
                .accelerator (Environment.getNewObjectTypeKeyCombination (Note.OBJECT_TYPE))
                .onAction (eev ->
                {

                    this.viewer.createNewNote (this.object,
                                               cpos);

                })
                .build ());

            citems.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.EDIT_NEEDED_OBJECT_TYPE,text)))
                .iconName (StyleClassNames.EDITNEEDEDNOTE)
                .accelerator (Environment.getNewObjectTypeKeyCombination (Note.EDIT_NEEDED_OBJECT_TYPE))
                .onAction (eev ->
                {

                    this.viewer.createNewEditNeededNote (this.object,
                                                         cpos);

                })
                .build ());

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

            citems.addAll (UIUtils.getNewAssetMenuItems (this.viewer));

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

    public Set<ChapterItem> getStructureItemsForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        List<ChapterItem> sitems = new ArrayList<> ();

        this.object.getScenes ().stream ()
            .forEach (s ->
            {

                sitems.add (s);
                sitems.addAll (s.getOutlineItems ());

            });

        sitems.addAll (this.object.getOutlineItems ());

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        items.addAll (sitems.stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                // See if we are in the same paragraph.
                if (this.editor.getParagraphForOffset (i.getPosition ()) != paraNo)
                {

                    return false;

                }

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toList ()));

        return items;

    }

    public Set<ChapterItem> getNotesForPosition (int p)
    {

        Bounds cb = this.editor.getBoundsForPosition (p);

        if (cb == null)
        {

            return new HashSet<> ();

        }

        int paraNo = this.editor.getParagraphForOffset (p);

        double y = cb.getMinY ();

        Set<ChapterItem> ret = new TreeSet<> (new ChapterItemSorter ());

        ret.addAll (this.object.getNotes ().stream ()
            // Only interested in those that have the same y value.  i.e. on the same line.
            .filter (i ->
            {

                // See if we are in the same paragraph.
                if (this.editor.getParagraphForOffset (i.getPosition ()) != paraNo)
                {

                    return false;

                }

                Bounds b = this.editor.getBoundsForPosition (i.getPosition ());

                return (b != null) && b.getMinY () == y;

            })
            .collect (Collectors.toSet ()));

        return ret;

    }

    private boolean checkForDistractionFreeMode ()
    {

        if (this.viewer.isDistractionFreeModeEnabled ())
        {

            List<String> prefix = Arrays.asList (iconcolumn,viewitem,distractionfreemode,popup);

            this.viewer.showNotificationPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                //"Function unavailable",
                                               getUILanguageStringProperty (Utils.newList (prefix,text)),
                                                //"Sorry, you cannot view {Notes}, {Plot Outline Items} and {Scenes} while distraction free mode is enabled.<br /><br /><a href='help:full-screen-mode/distraction-free-mode'>Click here to find out why</a>",
                                               10);

            return true;

        }

        return false;

    }

    public void editItem (ChapterItem item)
    {

        this.editItem (item,
                       null);

    }

    public void editItem (ChapterItem item,
                          Node        showAt)
    {

        if (this.checkForDistractionFreeMode ())
        {

            return;

        }

        if ((item instanceof com.quollwriter.data.Scene)
            ||
            (item instanceof OutlineItem)
           )
        {

            QuollPopup qp = this.viewer.getPopupById (AddEditStructureItemPopup.getPopupIdForChapterItem (item));

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            qp = new AddEditStructureItemPopup (this.viewer,
                                                item,
                                                this.object).getPopup ();

            this.showPopupForItem (item,
                                   qp);

            return;

        }

        if (item instanceof Note)
        {

            Note n = (Note) item;

            QuollPopup qp = this.viewer.getPopupById (AddEditNotePopup.getPopupIdForNote (n));

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            qp = new AddEditNotePopup (this.viewer,
                                       n,
                                       this.object).getPopup ();

            this.showPopupForItem (n,
                                   qp);

        }

    }

    public void showAddNewScene (int pos)
    {

        com.quollwriter.data.Scene s = new com.quollwriter.data.Scene ();
        s.setPosition (pos);

        this.showAddNewStructureItem (s);

    }

    public void showAddNewNote (int pos,
                                boolean editNeeded)
    {

        com.quollwriter.data.Note item = new com.quollwriter.data.Note ();
        item.setPosition (pos);

        if (editNeeded)
        {

            item.setType (Note.EDIT_NEEDED_NOTE_TYPE);

        }

        // Here we generate a bogus negative key so that the hashCode/equals still works.
        item.setKey (-1 * System.currentTimeMillis ());

        this.newItems.add (item);

        AddEditNotePopup p = new AddEditNotePopup (this.viewer,
                                                   item,
                                                   this.object);
        p.setOnCancel (ev ->
        {

            this.newItems.remove (item);

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        p.setOnClose (() ->
        {

            this.newItems.remove (item);

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        QuollPopup qp = p.getPopup ();

        int ind = this.editor.getParagraphForOffset (item.getPosition ());

        this.editor.recreateParagraphGraphic (ind);

        UIUtils.forceRunLater (() ->
        {

            this.showPopupForItem (item,
                                   qp);

        });

    }

    private void showAddNewStructureItem (ChapterItem item)
    {

        // Here we generate a bogus negative key so that the hashCode/equals still works.
        item.setKey (-1 * System.currentTimeMillis ());

        this.newItems.add (item);

        AddEditStructureItemPopup p = new AddEditStructureItemPopup (this.viewer,
                                                                     item,
                                                                     this.object);
        p.setOnCancel (ev ->
        {

            //this.getParagraphIconMargin (item).removeItem (item);
            this.newItems.remove (item);

            this.editor.recreateParagraphGraphic (this.editor.getParagraphForOffset (item.getPosition ()));

        });

        p.setOnClose (() ->
        {

            //this.getParagraphIconMargin (item).removeItem (item);
            this.newItems.remove (item);

            int ind = this.editor.getParagraphForOffset (item.getPosition ());

            if (ind > -1)
            {

                this.editor.recreateParagraphGraphic (ind);

            }

        });

        QuollPopup qp = p.getPopup ();

        int ind = this.editor.getParagraphForOffset (item.getPosition ());

        this.editor.recreateParagraphGraphic (ind);

        UIUtils.forceRunLater (() ->
        {

            this.showPopupForItem (item,
                                   qp);

        });

    }

    public void showAddNewOutlineItem (int pos)
    {

        OutlineItem o = new OutlineItem ();
        o.setPosition (pos);

        this.showAddNewStructureItem (o);

    }

    @Override
    public void showItem (ChapterItem item,
                          boolean     showAllForLine)
    {

        if (this.checkForDistractionFreeMode ())
        {

            return;

        }

        if (item instanceof Note)
        {

            ChapterItem top = item;
            Set<ChapterItem> items = null;

            if (showAllForLine)
            {

                items = this.getNotesForPosition (item.getPosition ());

                if (items.size () == 0)
                {

                    return;

                }

                top = items.iterator ().next ();

            } else {

                items = new LinkedHashSet<> ();
                items.add (item);

            }

            QuollPopup qp = this.viewer.getPopupById (ViewChapterItemPopup.getPopupIdForChapterItem (top));

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            qp = new ViewChapterItemPopup (this.viewer,
                                           items).getPopup ();

            this.showPopupForItem (top,
                                   qp);

        }

        if ((item instanceof com.quollwriter.data.Scene)
            ||
            (item instanceof OutlineItem)
           )
        {

            ChapterItem top = item;
            Set<ChapterItem> items = null;

            if (showAllForLine)
            {

                items = this.getStructureItemsForPosition (item.getPosition ());

                if (items.size () == 0)
                {

                    return;

                }

                top = items.iterator ().next ();

            } else {

                items = new LinkedHashSet<> ();
                items.add (item);

            }

            QuollPopup qp = this.viewer.getPopupById (ViewChapterItemPopup.getPopupIdForChapterItem (top));

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            qp = new ViewChapterItemPopup (this.viewer,
                                           items).getPopup ();

            this.showPopupForItem (top,
                                   qp);

        }

    }

    public void showProblemFinderRuleConfig ()
    {

         this.viewer.showProblemFinderRuleConfig ();

    }

    public void closeProblemFinder ()
    {

        this.problemFinder.close ();

    }

    public void showProblemFinder ()
    {

       if (!this.viewer.isLanguageFunctionAvailable ())
       {

          return;

       }

       // Disable typewriter scrolling when the problem finder is active.
       this.setUseTypewriterScrolling (false);
       this.editor.setHighlightWritingLine (false);

       this.problemFinder.setVisible (true);

       this.viewer.fireProjectEvent (ProjectEvent.Type.problemfinder,
                                     ProjectEvent.Action.show);

       try
       {

           this.problemFinder.start ();

       } catch (Exception e)
       {

          Environment.logError ("Unable to start problem finding",
                                e);

          ComponentUtils.showErrorMessage (this.viewer,
                                           getUILanguageStringProperty (project,editorpanel,actions,problemfinder,actionerror));

          return;

       }

     }

/*
TODO
    public void updateViewportPositionForTypewriterScrolling ()
    {

        if (!this.viewer.typeWriterScrollingEnabledProperty ().getValue ())
        {

            return;

        }

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.updateViewportPositionForTypewriterScrolling ();

            });

        }

        final ProjectChapterEditorPanelContent _this = this;

        int dot = _this.editor.getCaret ().getDot ();

        Rectangle r = null;

        try
        {

            r = SwingUIUtils.getRectForOffset (_this.editor,
                                               dot);

        } catch (Exception e) {

            // Ignore.
            return;

        }

        if (r == null)
        {

            return;

        }

        Insets i = _this.editor.getMargin ();
        int hh = _this.scrollPane.getSize ().height / 2;
        int y = r.y;

        if (i != null)
        {

            y -= i.top;

        }

        if ((y - hh) < 0)
        {

            _this.editor.setMargin (new Insets (-1 * (y - hh),
                                                      _this.origEditorMargin.left,
                                                      _this.origEditorMargin.bottom,
                                                      _this.origEditorMargin.right));

            _this.scrollPane.getViewport ().setViewPosition (new Point (0, 0));

        } else {

            if (y > (_this.editor.getSize ().height - hh - i.bottom - (r.height / 2)))
            {

                _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                    _this.origEditorMargin.left,
                                                    //hh - (_this.editor.getSize ().height - y - i.bottom - (r.height / 2)),
                                                    hh - (_this.editor.getSize ().height - y - i.bottom - Math.round ((float) r.height / 2f)),
                                                    _this.origEditorMargin.right));

            } else {

                _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                    _this.origEditorMargin.left,
                                                    _this.origEditorMargin.bottom,
                                                    _this.origEditorMargin.right));

            }

            Point p = new Point (0, y - hh + (r.height /2));

            _this.scrollPane.getViewport ().setViewPosition (p);

        }

        _this.scrollPane.validate ();
        _this.scrollPane.repaint ();

    }
*/
/*
TODO
    public void scrollToPosition (final int p)
                           throws GeneralException
    {

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                try
                {

                    this.scrollToPosition (p);

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to scroll to position: " +
                                                p);

                }

            });

            return;

        }


        if (this.viewer.typeWriterScrollingEnabledProperty ().getValue ())
        {

            // Not compatible with typewriter scrolling.
            return;

        }

        if (this.isScrolling)
        {

            final ProjectChapterEditorPanelContent _this = this;

            try
            {

                _this.scrollToPosition (p);

            } catch (Exception e) {

                Environment.logError ("Unable to scroll to: " + p,
                                      e);

            }

            return;

        }

        try
        {

            this.isScrolling = true;

            Rectangle r = null;

            try
            {

                r = SwingUIUtils.getRectForOffset (this.editor,
                                                   p);

            } catch (Exception e)
            {

                // BadLocationException!
                throw new GeneralException ("Position: " +
                                            p +
                                            " is not valid.",
                                            e);

            }

            if (r == null)
            {

                throw new GeneralException ("Position: " +
                                            p +
                                            " is not valid.");

            }

            int y = r.y - r.height;

            if (y < 0)
            {

                y = 0;

            }

            this.scrollPane.getVerticalScrollBar ().setValue (y);

        } finally {

            this.isScrolling = false;

        }

    }
*/
/*
TODO
    public int getTextPositionForMousePosition (Point p)
    {

       Point pp = p;

       if (this.iconColumn.getMousePosition () != null)
       {

          pp = new Point (0,
                          p.y);

       }

       return this.editor.viewToModel2D (pp);

    }
*/

    public void showSplitChapter ()
    {

        QuollPopup qp = this.viewer.getPopupById (SplitChapterPopup.getPopupIdForChapter (this.object));

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        qp = new SplitChapterPopup (this.viewer,
                                    this.object).getPopup ();

        this.viewer.showPopup (qp);

    }

    private MenuItem getCompressedNewItemsForContextMenu (int    pos,
                                                          String name,
                                                          String desc)
    {

        List<String> prefix = Arrays.asList (project,editorpanel,popupmenu,_new,items);

        List<Node> buts = new ArrayList<> ();

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,com.quollwriter.data.Scene.OBJECT_TYPE,tooltip)))
            .iconName (StyleClassNames.SCENE)
            .onAction (ev ->
            {

                this.viewer.createNewScene (this.object,
                                            pos);

            })
            .build ());

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,OutlineItem.OBJECT_TYPE,tooltip)))
            .iconName (StyleClassNames.OUTLINEITEM)
            .onAction (ev ->
            {

                this.viewer.createNewOutlineItem (this.object,
                                                  pos);

            })
            .build ());

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,Note.OBJECT_TYPE,tooltip)))
            .iconName (StyleClassNames.NOTE)
            .onAction (ev ->
            {

                this.viewer.createNewNote (this.object,
                                           pos);

            })
            .build ());

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,Note.EDIT_NEEDED_OBJECT_TYPE,tooltip)))
            .iconName (StyleClassNames.EDITNEEDEDNOTE)
            .onAction (ev ->
            {

                this.viewer.createNewEditNeededNote (this.object,
                                                     pos);

            })
            .build ());

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,Chapter.OBJECT_TYPE,tooltip)))
            .iconName (StyleClassNames.CHAPTER)
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.newchapter);

            })
            .build ());

        List<Node> buts2 = new ArrayList<> ();

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            QuollButton b = QuollButton.builder ()
                .tooltip (getUILanguageStringProperty (Arrays.asList (assets,add,button,tooltip),
                                                       type.getObjectTypeName ()))
                .onAction (ev ->
                {

                    Asset a = null;

                    try
                    {

                        a = Asset.createAsset (type);

                    } catch (Exception e) {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              type,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                                      type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (a == null)
                    {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              type);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                                      type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (name != null)
                    {

                        a.setName (name);

                    }

                    if (desc != null)
                    {

                        a.setDescription (new StringWithMarkup (desc));

                    }

                    this.viewer.showAddNewAsset (a);

                })
                .build ();

            b.setGraphic (new ImageView (type.getIcon16x16 ()));

            buts2.add (b);

        }

        return UIUtils.createCompressedMenuItem (getUILanguageStringProperty (project,editorpanel,popupmenu,_new,text),
                                                 buts,
                                                 buts2);

    }
/*
            if (popup instanceof JPopupMenu)
            {

               JPopupMenu pm = (JPopupMenu) popup;

               pm.addSeparator ();

               popup.add (SwingUIUtils.createPopupMenuButtonBar (Environment.getUIString (LanguageStrings.project,
                                                                                     LanguageStrings.editorpanel,
                                                                                     LanguageStrings.popupmenu,
                                                                                     LanguageStrings._new,
                                                                                     LanguageStrings.text),
                                                                                     //"New",
                                                            pm,
                                                            buts));

               UIUtils.addNewAssetItemsAsToolbarToPopupMenu (pm,
                                                             null,
                                                             (ProjectViewer) this.projectViewer,
                                                             null,
                                                             null);

               pm.addSeparator ();

            }

        } else {

            String pref = Environment.getUIString (LanguageStrings.general,LanguageStrings.shortcutprefix);

            //"Shortcut: Ctrl+Shift+";

            JMenuItem mi = null;

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               Scene.OBJECT_TYPE,
                                                               LanguageStrings.text),
//Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                      Scene.OBJECT_TYPE,
                                      NEW_SCENE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('S');
            mi.setToolTipText (pref + "S");

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               OutlineItem.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                                      OutlineItem.OBJECT_TYPE,
                                      NEW_OUTLINE_ITEM_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('O');
            mi.setToolTipText (pref + "O");

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               Note.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Note.OBJECT_TYPE,
                                      NEW_NOTE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('N');
            mi.setToolTipText (pref + "N");

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               Note.EDIT_NEEDED_OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Note.EDIT_NEEDED_NOTE_TYPE + " " + Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                      NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               Chapter.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                                      Chapter.OBJECT_TYPE,
                                      NEW_CHAPTER_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('E');
            mi.setToolTipText (pref + "E");

            UIUtils.addNewAssetItemsToPopupMenu (popup,
                                                 showAt,
                                                 this.projectViewer,
                                                 null,
                                                 null);

        }

    }
*/

}
