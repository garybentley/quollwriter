package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class AllProjectsViewer extends AbstractViewer
{

    public static final String VIEWER_STATE_ID = "allprojects.state";
    private State state = null;
    private Map<String, PanelContent> panels = null;
    private StringProperty titleProp = null;
    private StackPane content = null;

    public interface CommandId extends AbstractViewer.CommandId
    {
        String findprojects = "findprojects";
        String changeprojectdetails = "changeprojectdetails";
    }

    public AllProjectsViewer ()
                       throws GeneralException
    {

        final AllProjectsViewer _this = this;

        this.panels = new HashMap<> ();
        this.content = new StackPane ();

        // Create a projects panel.
        this.addPanel (new ProjectsPanel<AllProjectsViewer> (this));
        this.addPanel (new NoProjectsPanel (this));
        this.addPanel (new ImportFilePanel (this));

        this.showPanel (ProjectsPanel.PANEL_ID);

        this.setContent (this.content);

        this.titleProp = new SimpleStringProperty ();
        // TODO Create a binding that
        this.titleProp.bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUIString (LanguageStrings.allprojects, LanguageStrings.title), Environment.formatNumber (Environment.allProjectsProperty ().size ()));

        },
        UILanguageStringsManager.uilangProperty (),
        Environment.objectTypeNameChangedProperty (),
        Environment.allProjectsProperty ()));

        Environment.allProjectsProperty ().addListener ((val, oldv, newv) -> this.update ());

        this.initKeyMappings ();

        this.initActionMappings ();
/*
TODO READD
        this.setOnDragExited (ev ->
        {

            // Will be a null source since it is coming from outside the window.
            if (ev.getGestureSource () == null)
            {

                _this.showProjectsPanel ();

                ev.consume ();

            }

        });

        this.setOnDragEntered (ev ->
        {

            _this.checkDragFileImport (ev);

        });

        this.setOnDragOver (ev ->
        {

            _this.checkDragFileImport (ev);

        });

        this.setOnDragDropped (ev ->
        {

            List<File> files = ev.getDragboard ().getFiles ();

            if (files != null)
            {

                if (files.size () > 1)
                {

                    return;

                }

                File f = files.get (0);

                if (f.getName ().endsWith (Constants.DOCX_FILE_EXTENSION))
                {

                    ev.setDropCompleted (true);

                    System.out.println ("HERE");

                    this.showProjectsPanel ();

                    // TODO Show the import.

                    ev.consume ();

                }

            }

        });
*/
        this.update ();

    }

    public boolean showPanel (String id)
    {

        PanelContent pc = this.panels.get (id);

        if (pc == null)
        {

            return false;

        }

        pc.getPanel ().toFront ();

        this.fireEvent (new Panel.PanelEvent (pc.getPanel (),
                                              Panel.PanelEvent.SHOW_EVENT));

        return true;

    }

    public void removePanel (String id)
    {

        PanelContent pc = this.panels.remove (id);

        if (pc == null)
        {

            throw new IllegalArgumentException ("Unable to find panel: " + id);

        }

        this.content.getChildren ().remove (pc.getPanel ());

        if (this.state != null)
        {

            this.state.set (id,
                            pc.getState ());

        }

        this.fireEvent (new Panel.PanelEvent (pc.getPanel (),
                                              Panel.PanelEvent.CLOSE_EVENT));

        this.showPanel (ProjectsPanel.PANEL_ID);

    }

    private void addPanel (PanelContent p)
                    throws GeneralException
    {

        Panel panel = p.getPanel ();

        if (this.panels.containsKey (panel.getPanelId ()))
        {

            throw new IllegalArgumentException ("Already have a panel with id: " + panel.getPanelId ());

        }

        this.content.getChildren ().add (panel);

        this.panels.put (panel.getPanelId (),
                         p);

        if (this.state != null)
        {

            State s = this.state.getAs (panel.getPanelId (),
                                        State.class);

            p.init (s);

        }

    }

    private void checkDragFileImport (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if (files != null)
        {

            if (files.size () > 1)
            {

                return;

            }

            File f = files.get (0);

            if (f.getName ().endsWith (Constants.DOCX_FILE_EXTENSION))
            {

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

                this.showImportPanel ();

                ev.consume ();

            }

        }

    }

    private void showProjectsPanel ()
    {

        this.showPanel (ProjectsPanel.PANEL_ID);

    }

    private void showImportPanel ()
    {

        this.showPanel (ImportFilePanel.PANEL_ID);

    }

    @Override
    public StringProperty titleProperty ()
    {

        return this.titleProp;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.ALLPROJECTS;

    }

    @Override
    public SideBar getMainSideBar ()
    {

        return null;

    }

    private void initKeyMappings ()
    {

    }

    private void initActionMappings ()
    {

        final AllProjectsViewer _this = this;

        this.addActionMapping (() ->
        {

            new FindProjectsPopup (_this).show ();

        },
        CommandId.findprojects);

        this.addActionMapping (() ->
        {

            new ImportPopup (_this).show ();

        },
        CommandId.importfile);

        this.addActionMapping (() ->
        {

            try
            {

                _this.viewTargets ();

            } catch (Exception e) {

                Environment.logError ("Unable to view targets",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (targets,actionerror));
                                                  //"Unable to view targets.");

            }

        },
        CommandId.viewtargets,
        CommandId.targets);

        this.addActionMapping (() ->
        {

            try
            {

                _this.viewAchievements ();

            } catch (Exception e) {

                Environment.logError ("Unable to view achievements",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (achievements,actionerror));

            }

        },
        CommandId.viewachievements,
        CommandId.achievements);

        this.addActionMapping (() ->
        {

            try
            {

                _this.showOptions (null);

            } catch (Exception e) {

                Environment.logError ("Unable to view options",
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (options,actionerror));

            }

        },
        CommandId.showoptions,
        CommandId.options);

        this.addActionMapping (new CommandWithArgs ((args) ->
        {

            String chartType = null;

            if (args != null)
            {

                if (args.length == 1)
                {

                    chartType = (String) args[0];

                }

            }

           try
           {

               _this.viewStatistics (chartType);

           } catch (Exception e) {

               Environment.logError ("Unable to view the statistics",
                                     e);

               ComponentUtils.showErrorMessage (_this,
                                                getUILanguageStringProperty (statistics,actionerror));
                                                 //"Unable to view the statistics");

           }

        },
        AbstractViewer.CommandId.showstatistics,
        AbstractViewer.CommandId.statistics,
        AbstractViewer.CommandId.charts));

        this.addActionMapping (() ->
        {

            try
            {

                ProjectStatusItemManager man = new ProjectStatusItemManager (_this);

                man.show ();

            } catch (Exception e) {

                Environment.logError ("Unable to view the project statuses manager",
                                      e);

            }

        },
        CommandId.manageprojectstatuses);

    }

    public void update ()
    {

        if (Environment.allProjectsProperty ().size () == 0)
        {

            this.showPanel (NoProjectsPanel.PANEL_ID);

        } else {

            this.showPanel (ProjectsPanel.PANEL_ID);

        }

    }

    @Override
    public void close (Runnable doAfterClose)
    {

        State s = this.getState ();

        try
        {

            UserProperties.set (VIEWER_STATE_ID,
                                s.asString ());

        } catch (Exception e) {

            Environment.logError ("Unable to save/set viewer state",
                                  e);

        }

        super.close (doAfterClose);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        for (PanelContent p : this.panels.values ())
        {

            s.set (p.getPanelId (),
                   p.getState ());

        }

        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (s == null)
        {

            String vs = UserProperties.get (VIEWER_STATE_ID);

            if (vs != null)
            {

                s = new State (vs);

            } else {

                // Defaults.
                s = new State ();

                // Legacy, pre v3
                int wh = UserProperties.getAsInt ("landing-window-height");

                s.set (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
                       wh);

                int ww = UserProperties.getAsInt ("landing-window-width");

                s.set (Constants.WINDOW_WIDTH_PROPERTY_NAME,
                       ww);

            }

        }

        for (PanelContent pc : this.panels.values ())
        {

            State v = s.getAs (pc.getPanelId (),
                               State.class);

            if (v != null)
            {

                pc.init (v);

            }

        }

        super.init (s);

        this.state = s;

    }

    private void updateSortProjectsBy (String sortBy)
	{

		UserProperties.set (Constants.SORT_PROJECTS_BY_PROPERTY_NAME,
							sortBy);

	}

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        final AllProjectsViewer _this = this;

        return new Supplier<> ()
        {

            @Override
            public Set<MenuItem> get ()
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                java.util.List<String> prefix = Arrays.asList (allprojects,settingsmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,newproject))
                    .styleClassName (StyleClassNames.NEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.newproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .styleClassName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,findprojects))
                    .styleClassName (StyleClassNames.FIND)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.findprojects);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,showcontacts))
                    .styleClassName (StyleClassNames.CONTACTS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.contacts);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .styleClassName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .styleClassName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.targets);

                    })
                    .build ());

/*
TODO Removed, not valid here.
                items.add (QuollMenu.builder ()
                    .label (Utils.newList (prefix,sortprojects))
                    .styleClassName (StyleClassNames.SORTMENU)
                    .items (() ->
                    {

                        Set<MenuItem> sitems = new LinkedHashSet<> ();

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortlastedited))
                            .styleClassName (StyleClassNames.LASTEDITED)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("lastEdited");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortname))
                            .styleClassName (StyleClassNames.NAME)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("name");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortstatus))
                            .styleClassName (StyleClassNames.STATUS)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("status");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortwordcount))
                            .styleClassName (StyleClassNames.WORDCOUNT)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("wordCount");

                            })
                            .build ());

                        return sitems;

                    })
                    .build ());
*/
                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,managestatuses))
                    .styleClassName (StyleClassNames.MANAGESTATUSES)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.manageprojectstatuses);

                    })
                    .build ());
/*
TODO Removed as not appropriate
                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,changedisplay))
                    .styleClassName (StyleClassNames.CHANGEDISPLAY)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandIds.changeprojectdetails);

                    })
                    .build ());
                    */
/*
TODO Remove as not appropriate.
                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,selectbackground))
                    .styleClassName (StyleClassNames.SELECTBG)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.selectbackground);

                    })
                    .build ());
*/
                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .styleClassName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandId.warmup);

                    })
                    .build ());

                return items;

            }

        };

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        final AllProjectsViewer _this = this;

        return () ->
        {

            List<String> prefix = Arrays.asList (allprojects,headercontrols,items);

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (QuollButton.builder ()
                .tooltip (prefix,add,tooltip)
                .styleClassName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.runCommand (AllProjectsViewer.CommandId.newproject);

                })
                .build ());

            controls.add (QuollButton.builder ()
                .tooltip (prefix,importfile,tooltip)
                .styleClassName (StyleClassNames.IMPORT)
                .onAction (ev ->
                {

                    this.runCommand (AllProjectsViewer.CommandId.importfile);

                })
                .build ());

            controls.add (_this.getTitleHeaderControl (HeaderControl.contacts));

            return controls;

        };

    }

    @Override
    public void showOptions (String section)
                      throws GeneralException
    {

        if (this.showPanel (OptionsPanel.PANEL_ID))
        {

            return;

        }

        Set<Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (actions,clicktoclose))
            .onAction (ev ->
            {

                this.removePanel (OptionsPanel.PANEL_ID);

            })
            .build ());

        OptionsPanel a = new OptionsPanel (this,
                                           cons,
                                           Options.Section.Id.look,
   										   Options.Section.Id.naming,
   										   Options.Section.Id.editing,
                                           Options.Section.Id.assets,
   										   Options.Section.Id.start,
   										   Options.Section.Id.editors,
   										   Options.Section.Id.itemsAndRules,
   										   Options.Section.Id.warmups,
   										   Options.Section.Id.achievements,
   										   Options.Section.Id.problems,
   										   Options.Section.Id.betas,
                                           Options.Section.Id.website);                                           

        a.showSection (section);

        UIUtils.doOnKeyReleased (a,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removePanel (OptionsPanel.PANEL_ID);

                                 });
        this.addPanel (a);
        this.showPanel (a.getPanelId ());


    }

    public void showChart (String chartType)
                    throws GeneralException
    {

        // TODO

    }

    public void viewStatistics (String chartType)
                         throws GeneralException
    {

        if (this.showPanel (StatisticsPanel.PANEL_ID))
        {

            return;

        }

        Set<Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (actions,clicktoclose))
            .onAction (ev ->
            {

                this.removePanel (StatisticsPanel.PANEL_ID);

            })
            .build ());

        StatisticsPanel a = new StatisticsPanel (this,
                                                 cons,
                                                 new SessionWordCountChart (this));
        UIUtils.doOnKeyReleased (a,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removePanel (StatisticsPanel.PANEL_ID);

                                 });
        this.addPanel (a);
        this.showPanel (a.getPanelId ());

        if (chartType != null)
        {

            a.showChart (chartType);

        }

    }

    private void viewAchievements ()
                            throws GeneralException
    {

        if (this.showPanel (AchievementsPanel.PANEL_ID))
        {

            return;

        }

        Set<Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (actions,clicktoclose))
            .onAction (ev ->
            {

                this.removePanel (AchievementsPanel.PANEL_ID);

            })
            .build ());

        AchievementsPanel a = new AchievementsPanel (this,
                                                     cons);
        UIUtils.doOnKeyReleased (a,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removePanel (AchievementsPanel.PANEL_ID);

                                 });
        this.addPanel (a);
        this.showPanel (a.getPanelId ());

    }

    private void viewTargets ()
    {

        SideBar sb = this.getSideBarById (TargetsSideBar.SIDEBAR_ID);

        if (sb == null)
        {

            this.addSideBar (new TargetsSideBar (this));

        }

        this.showSideBar (TargetsSideBar.SIDEBAR_ID);

    }

}
