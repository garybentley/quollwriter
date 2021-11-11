package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import java.text.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.sidebars.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WarmupProjectViewer extends AbstractProjectViewer
{

    private SimpleDateFormat sdf = null;
    private ObservableList<Warmup> warmups = null;
    private WarmupsSideBar sidebar = null;
    private ObjectProperty<Warmup> currentWarmupProp = new SimpleObjectProperty<> ();

    public interface CommandId extends AbstractProjectViewer.CommandId
    {

        String convertwarmuptoproject = "convertwarmuptoproject";
        String renamewarmup = "renamewarmup";
        String deletewarmup = "deletewarmup";
        String deletechapter = "deletechapter";

    }

    public WarmupProjectViewer ()
    {

        super ();

        this.sidebar = new WarmupsSideBar (this);

        this.initActionMappings ();

        this.sdf = new SimpleDateFormat ("dd MMM yyyy");

        this.addChangeListener (this.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            Warmup w = null;

            if ((newv != null)
                &&
                (newv.getContent () instanceof WarmupEditorPanelContent)
               )
            {

                w = ((WarmupEditorPanelContent) newv.getContent ()).getWarmup ();

            }

            this.currentWarmupProp.setValue (w);

        });


    }

    public ObjectProperty<Warmup> currentWarmupProperty ()
    {

        return this.currentWarmupProp;

    }

    public void showTimer (Warmup w)
    {

        Node n = this.getTitleHeaderControlByButtonId (HeaderControlButtonIds.timer);

        if ((n == null)
            ||
            (!(n instanceof WordCountTimerButton))
           )
        {

            Environment.logError ("Unable to find word count timer button.");

            return;

        }

        WordCountTimerButton b = (WordCountTimerButton) n;

        this.addTitleHeaderControl (b,
                                    HPos.LEFT);

        b.start (w.getMins (),
                 w.getWords ());

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.WARMUP;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        // We do this last because the sidebars will be restored by the super.
        super.init (s);

        this.getWindowedContent ().getHeader ().getControls ().setVisibleItems (UserProperties.warmupViewerHeaderControlButtonIds ());

        this.getWindowedContent ().getHeader ().getControls ().setOnConfigurePopupClosed (ev ->
        {

            UserProperties.setWarmupViewerHeaderControlButtonIds (this.getWindowedContent ().getHeader ().getControls ().getVisibleItemIds ());

        });

        this.getBinder ().addSetChangeListener (UserProperties.warmupViewerHeaderControlButtonIds (),
                                                ev ->
        {

            this.getWindowedContent ().getHeader ().getControls ().setVisibleItems (UserProperties.warmupViewerHeaderControlButtonIds ());

        });

        this.setMainSideBar (this.sidebar);

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        WarmupProjectViewer _this = this;

        return new Supplier<> ()
        {

            @Override
            public Set<MenuItem> get ()
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                java.util.List<String> prefix = Arrays.asList (LanguageStrings.warmups,settingsmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,openproject))
                    .iconName (StyleClassNames.OPEN)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.openproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,newproject))
                    .iconName (StyleClassNames.NEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.newproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,renameproject))
                    .iconName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.renameproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .iconName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .iconName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.targets);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,createbackup))
                    .iconName (StyleClassNames.CREATEBACKUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.createbackup);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,closeproject))
                    .iconName (StyleClassNames.CLOSE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.closeproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,deleteproject))
                    .iconName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.deleteproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .iconName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.warmup);

                    })
                    .build ());

                return items;

            }

        };

    }

    @Override
    public void openPanelForId (String id)
                         throws GeneralException
    {

        super.openPanelForId (id);

    }

    public void addNewWarmup (Warmup w)
                       throws Exception
    {

        Book b = this.project.getBooks ().get (0);

        Date d = new Date ();

        String name = this.sdf.format (d);

        int max = 0;

        // See if we already have a chapter with that name.
        for (Chapter c : b.getChapters ())
        {

            if (c.getName ().startsWith (name))
            {

                // See if there is a number at the end.
                int ind = c.getName ().indexOf ("(",
                                                name.length ());

                if (ind != -1)
                {

                    // Get the number (if present)
                    String n = c.getName ().substring (ind + 1);

                    n = Utils.replaceString (n,
                                                   "(",
                                                   "");
                    n = Utils.replaceString (n,
                                                   ")",
                                                   "");

                    try
                    {

                        int m = Integer.parseInt (n);

                        if (m > max)
                        {

                            max = m;

                        }

                    } catch (Exception e)
                    {

                        // Ignore.

                    }

                } else
                {

                    if (max == 0)
                    {

                        max = 1;

                    }

                }

            }

        }

        if (max > 0)
        {

            max++;

            name = name + " (" + max + ")";

        }

        // Create a new chapter for the book.
        Chapter c = new Chapter (b,

                                 // Default to todays date.
                                 name);

        b.addChapter (c);

        // Create a new warmup.
        w.setChapter (c);

        java.util.List objs = new ArrayList ();
        objs.add (c);
        objs.add (w);

        // Add the chapter.
        try
        {

            this.saveObjects (objs,
                              true);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save new chapter/warmup",
                                        e);

        }

        this.warmups.add (w);

        this.viewObject (w);

        WarmupEditorPanelContent wep = (WarmupEditorPanelContent) this.getEditorForWarmup (w);

        wep.startWarmup ();

        this.fireProjectEvent (ProjectEvent.Type.warmup,
                               ProjectEvent.Action._new,
                               w);

    }

    public WarmupEditorPanelContent getEditorForWarmup (Warmup w)
    {

        NamedObjectPanelContent p = this.getPanelForObject (w.getChapter ());

        return (WarmupEditorPanelContent) p;

    }

    @Override
    public void handleNewProject ()
                           throws Exception
    {

        if ((this.project.getBooks () == null) ||
            (this.project.getBooks ().size () == 0) ||
            (this.project.getBooks ().get (0).getChapters ().size () == 0))
        {

            Book b = new Book (this.project,
                               this.project.getName ());

            this.project.addBook (b);

        }

        this.handleOpenProject ();

    }

    @Override
    public void handleOpenProject ()
                            throws Exception
    {

        // Get all the warmups.
        this.warmups = FXCollections.observableList ((List<Warmup>) this.dBMan.getObjects (Warmup.class,
                                                                                           this.project,
                                                                                           null,
                                                                                           true));

        this.project.getBooks ().get (0).setChapterSorter (new ChapterSorter (ChapterSorter.DATE_CREATED,
                                                                              ChapterSorter.DESC));

    }

    @Override
    public Set<FindResultsBox> findText (String t)
    {

        Set<FindResultsBox> res = new LinkedHashSet<> ();

        FindResultsBox chres = this.findTextInChapters (t);

        if (chres != null)
        {

            res.add (chres);

        }

        return res;

    }

    @Override
    public void viewObject (DataObject d,
                            Runnable   doAfterView)
    {

        if (d instanceof Warmup)
        {

            this.editWarmup ((Warmup) d,
                             doAfterView);

            return;

        }

        if (d instanceof Chapter)
        {

            // Get the warmup for the chapter.
            Warmup w = this.getWarmupForChapter ((Chapter) d);

            if (w == null)
            {

                throw new IllegalArgumentException ("No warmup found for chapter: " +
                                                    d);

            }

            this.editWarmup (w,
                             doAfterView);
            return;

        }

        throw new IllegalArgumentException ("Object: " +
                                            d +
                                            " not yet supported.");

    }

    public void editWarmup (Warmup   w,
                            Runnable doAfterView)
    {

        String pid = ProjectChapterEditorPanelContent.getPanelIdForChapter (w.getChapter ());

        if (this.showPanel (pid))
        {

            UIUtils.runLater (doAfterView);

            return;

        }

        try
        {

            WarmupEditorPanelContent p = new WarmupEditorPanelContent (this,
                                                                       w);

            this.addPanel (p);

            this.editWarmup (w,
                             doAfterView);

        } catch (Exception e) {

            Environment.logError ("Unable to edit warmup: " +
                                  w,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (LanguageStrings.warmups,actions,editwarmup,actionerror)));

        }

    }

    public Warmup getWarmupForChapter (Chapter c)
    {

        for (Warmup w : this.warmups)
        {

            if (w.getChapter () == c)
            {

                return w;

            }

        }

        return null;

    }

    public void convertWarmupToProject (Warmup w)
    {

        String popupId = "convertoproject" + w.getChapter ().getId ();

        if (this.getPopupById (popupId) != null)
        {

            return;

        }

        List<String> prefix = Arrays.asList (LanguageStrings.warmups,actions,convertwarmup);

        NewProjectPanel projPanel = new NewProjectPanel (this,
                                                         getUILanguageStringProperty (Utils.newList (prefix,text)),
                                                         true,
                                                         true);

        QuollPopup p = QuollPopup.builder ()
            .title (newproject, LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.CREATEPROJECT)
            .styleSheet (StyleClassNames.CREATEPROJECT)
            .hideOnEscape (true)
            .withClose (true)
            .content (projPanel)
            .popupId (popupId)
            .withViewer (this)
            .removeOnClose (true)
            .build ();

        projPanel.setOnProjectCreated (proj ->
        {

            Book b = proj.getBook (0);

            // Get the default chapter.
            Chapter c = b.getChapter (0);

            WarmupEditorPanelContent wep = (WarmupEditorPanelContent) this.getEditorForWarmup (w);

            c.setName (w.getChapter ().getName ());
            c.setText (wep.getEditor ().getTextWithMarkup ());

            AbstractProjectViewer pj = Environment.getProjectViewer (proj);

            ChapterEditorPanelContent cp = pj.getEditorForChapter (c);

            cp.getEditor ().setText (c.getText ());

            try
            {

                pj.saveObject (c,
                               true);

            } catch (Exception e) {

                Environment.logError ("Unable to save project.",
                                      e);

            }

            this.fireProjectEvent (ProjectEvent.Type.warmup,
                                   ProjectEvent.Action.converttoproject,
                                   w);

            pj.createActionLogEntry (proj,
                                     String.format ("Project created from warmup, prompt id: %1$s and chapter: %2$s (%3$s)",
                                                    w.getPrompt ().getId (),
                                                    w.getChapter ().getName (),
                                                    w.getChapter ().getKey ()));

            p.close ();

        });

        projPanel.setOnCancel (ev ->
        {

            p.close ();

        });

        UIUtils.forceRunLater (() ->
        {

            projPanel.requestFocus ();

        });

        p.show ();

    }

    public void deleteWarmup (Warmup w)
    {

        try
        {

            // Get the warmup and delete it.
            if (w == null)
            {

                Environment.logError ("Unable to find warmup for chapter: " +
                                      w);

                ComponentUtils.showErrorMessage (this,
                                                 getUILanguageStringProperty (LanguageStrings.warmups,actions,deletewarmup,actionerror));

                return;

            }

            Chapter c = w.getChapter ();

            Set<DataObject> toDelete = new LinkedHashSet<> ();

            toDelete.add (w);
            toDelete.add (c);

            this.dBMan.deleteObjects (toDelete,
                                      null);

            this.warmups.remove (w);

            // Remove the chapter from the book.
            Book b = c.getBook ();

            b.removeChapter (c);

            this.fireProjectEvent (ProjectEvent.Type.warmup,
                                   ProjectEvent.Action.delete,
                                   w);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete warmup: " + w,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.warmups,actions,deletewarmup,actionerror));

            return;

        }

    }

    private void initActionMappings ()
    {

        this.addActionMapping (new CommandWithArgs<DataObject> (objs ->
        {

            DataObject o = null;

            if ((objs != null)
                &&
                (objs.length > 0)
               )
            {

                o = objs[0];

            }

            if (o == null)
            {

                throw new IllegalArgumentException ("No object provided.");

            }

            Warmup w = null;

            if (!(o instanceof Warmup))
            {

                throw new IllegalArgumentException ("Object is not a warmup is: " +
                                                    o.getClass ().getName ());

            }

            w = (Warmup) o;

            this.deleteWarmup (w);

        },
        CommandId.deletewarmup,
        CommandId.deletechapter));

    }

/*
    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        Set<Node> pcons = super.getTitleHeaderControlsSupplier ().get ();

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (QuollButton.builder ()
                .tooltip (LanguageStrings.warmups,title,toolbar,buttons,_new,tooltip)
                .iconName (StyleClassNames.WARMUP)
                .buttonId ("dowarmup")
                .onAction (ev ->
                {

                    this.runCommand (CommandId.warmup);

                })
                .build ());

            this.wordCountTimerButton = WordCountTimerButton.builder ()
                .inViewer (this)
                .buttonTooltip (getUILanguageStringProperty (LanguageStrings.warmups,title,toolbar,buttons,wordcounttimer,tooltip))
                .buttonId ("timer")
                .build ();

            controls.add (this.wordCountTimerButton);

            controls.addAll (pcons);

            return controls;

        };

    }
*/
}
