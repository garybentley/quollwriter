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
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class AllProjectsViewer extends AbstractViewer
{

    public static final String VIEWER_STATE_ID = "allprojects.state";
    private ProjectsPanel<AllProjectsViewer> projectsPanel = null;
    private ImportFilePanel importPanel = null;
    private NoProjectsPanel noProjectsPanel = null;
    private StringProperty titleProp = null;
    private StackPane content = null;

    public interface CommandIds extends AbstractViewer.CommandIds
    {
        String findprojects = "findprojects";
        String changeprojectdetails = "changeprojectdetails";
        String newprojectstatus = "newprojectstatus";
    }

    public AllProjectsViewer ()
    {

        final AllProjectsViewer _this = this;

        // Create a projects panel.
        this.projectsPanel = new ProjectsPanel<> (this);
        this.noProjectsPanel = new NoProjectsPanel (this);
        this.importPanel = new ImportFilePanel (this);

        this.content = new StackPane ();
        this.content.getChildren ().add (this.projectsPanel.getPanel ());
        this.content.getChildren ().add (this.importPanel.getPanel ());
        this.importPanel.getPanel ().setVisible (false);

        this.setContent (this.content);

        this.titleProp = new SimpleStringProperty ();
        // TODO Create a binding that
        this.titleProp.bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUIString (LanguageStrings.allprojects, LanguageStrings.title), Environment.formatNumber (Environment.allProjectsProperty ().size ()));

        },
        UILanguageStringsManager.uilangProperty (),
        Environment.allProjectsProperty ()));

        Environment.allProjectsProperty ().addListener ((val, oldv, newv) -> this.update ());

        this.initKeyMappings ();

        this.initActionMappings ();

        this.setOnDragExited (ev ->
        {

            _this.showProjectsPanel ();

        });

        this.setOnDragEntered (ev ->
        {

            List<File> files = ev.getDragboard ().getFiles ();

            if (files != null)
            {

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

                _this.showImportPanel ();

            }

            ev.consume ();

        });

        this.setOnDragOver (ev ->
        {

            List<File> files = ev.getDragboard ().getFiles ();

            // TODO Checck the file type.
            if (files != null)
            {

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

                _this.showImportPanel ();

            }

            ev.consume ();

        });

        this.setOnDragDropped (ev ->
        {

            // TODO Show the import file...

        });

        this.update ();

    }

    private void showProjectsPanel ()
    {

        this.importPanel.getPanel ().setVisible (false);

    }

    private void showImportPanel ()
    {

        this.importPanel.getPanel ().setVisible (true);

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
        CommandIds.findprojects);

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
        CommandIds.viewtargets,
        CommandIds.targets);

        this.addActionMapping (() -> _this.viewAchievements (),
                               CommandIds.viewachievements,
                               CommandIds.achievements);

        this.addActionMapping (() -> _this.showOptions (null),
                               CommandIds.showoptions,
                               CommandIds.options);

        this.addActionMapping (() ->
        {

           try
           {

               _this.viewStatistics ();

           } catch (Exception e) {

               Environment.logError ("Unable to view the statistics",
                                     e);

               ComponentUtils.showErrorMessage (_this,
                                                getUILanguageStringProperty (statistics,actionerror));
                                                 //"Unable to view the statistics");

           }

        },
        AbstractViewer.CommandIds.showstatistics,
        AbstractViewer.CommandIds.statistics,
        AbstractViewer.CommandIds.charts);

    }

    public void update ()
    {

        if (Environment.allProjectsProperty ().size () == 0)
        {

            this.projectsPanel.setViewOrder (1d);
            this.noProjectsPanel.setViewOrder (0d);

        } else {

            this.projectsPanel.setViewOrder (0d);
            this.noProjectsPanel.setViewOrder (1d);

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

        super.close ();

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        s.set (ProjectsPanel.PANEL_ID,
               this.projectsPanel.getState ());

        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (s == null)
        {

            s = new State (UserProperties.get (VIEWER_STATE_ID));

        }

        State v = s.getAs (ProjectsPanel.PANEL_ID,
                           State.class);

        if (v != null)
        {

            this.projectsPanel.init (v);

        }

        this.noProjectsPanel.init (s);
        this.importPanel.init (s);

        super.init (s);

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

                        _this.runCommand (AllProjectsViewer.CommandIds.newproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .styleClassName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandIds.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,findprojects))
                    .styleClassName (StyleClassNames.FIND)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandIds.findprojects);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .styleClassName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandIds.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .styleClassName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandIds.targets);

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

                        _this.runCommand (AbstractViewer.CommandIds.manageprojectstatuses);

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

                        _this.runCommand (AbstractViewer.CommandIds.selectbackground);

                    })
                    .build ());
*/
                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .styleClassName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (AbstractViewer.CommandIds.warmup);

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

                    this.runCommand (AllProjectsViewer.CommandIds.newproject);

                })
                .build ());

            controls.add (QuollButton.builder ()
                .tooltip (prefix,importfile,tooltip)
                .styleClassName (StyleClassNames.IMPORT)
                .onAction (ev ->
                {

                    this.runCommand (AllProjectsViewer.CommandIds.importfile);

                })
                .build ());

            controls.add (_this.getTitleHeaderControl (HeaderControl.contacts));

            return controls;

        };

    }

    @Override
    public void showOptions (String section)
    {

        // TODO

    }

    public void showChart (String chartType)
                    throws GeneralException
    {

        // TODO

    }

    public void viewStatistics ()
                         throws GeneralException
    {

        // TODO

    }

    private void viewAchievements ()
    {

        // TODO View the general achievements.

    }

    private void viewTargets ()
    {

        // TODO Show the targets sidebar with just the standard targets.

    }

}
