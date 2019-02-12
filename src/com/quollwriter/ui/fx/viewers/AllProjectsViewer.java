package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class AllProjectsViewer extends AbstractViewer
{

    private ProjectsPanel<AllProjectsViewer> projectsPanel = null;
    private NoProjectsPanel noProjectsPanel = null;
    private StringProperty titleProp = null;

    public AllProjectsViewer ()
    {

        // Create a projects panel.
        this.projectsPanel = new ProjectsPanel<> (this);
        this.noProjectsPanel = new NoProjectsPanel (this);

        StackPane stack = new StackPane ();

        this.setContent (stack);

        // TODO Add no projects.
        stack.getChildren ().addAll (this.projectsPanel.createPanel ());// this.noProjectsPanel.createPanel ());

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

        this.update ();

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
        Command.viewtargets,
        Command.targets);

        this.addActionMapping (() -> _this.viewAchievements (),
                               Command.viewachievements,
                               Command.achievements);

        this.addActionMapping (() -> _this.showOptions (null),
                               Command.showoptions,
                               Command.options);

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
        Command.showstatistics,
        Command.statistics,
        Command.charts);

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
    public void init (State s)
               throws GeneralException
    {

        // TODO
        this.projectsPanel.init (s);
        this.noProjectsPanel.init (s);
System.out.println ("HERE2");
        super.init (s);

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        // TODO
        return null;

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        // TODO
        return null;

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
