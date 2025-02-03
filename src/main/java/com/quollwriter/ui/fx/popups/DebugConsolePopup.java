package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import org.josql.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.collections.*;

import com.gentlyweb.properties.*;

import javafx.beans.property.SimpleStringProperty;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class DebugConsolePopup extends PopupContent
{

    public static final String POPUP_ID = "debugconsole";

    public DebugConsolePopup (AbstractViewer viewer)
    {

        super (viewer);

        final DebugConsolePopup _this = this;

        List<String> prefix = Arrays.asList (about,LanguageStrings.popup);

        TabPane tabs = new TabPane ();
        tabs.setTabClosingPolicy (TabPane.TabClosingPolicy.ALL_TABS);
        tabs.setSide (UserProperties.tabsLocationProperty ().getValue ().equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);
        tabs.setTabDragPolicy (TabPane.TabDragPolicy.REORDER);

        Tab tab = new Tab ("Logs");
        tab.setContent (this.createLogsPanel ());

        tabs.getTabs ().add (tab);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            tab = new Tab ("SQL Console");
            tab.setContent (this.createSQLConsolePanel ());

            tabs.getTabs ().add (tab);

            tab = new Tab ("Properties");
            tab.setContent (this.createPropertiesPanel (pv.getProject ().getProperties ()));

            tabs.getTabs ().add (tab);

        }

        tab = new Tab ("Achievements");
        tab.setContent (this.createAchievementsPanel ());

        tabs.getTabs ().add (tab);

        this.getChildren ().add (tabs);

    }

    private Region createLogsPanel ()
    {

        TextArea v = new TextArea ();
        v.setText (this.getLogFilesAsSingleString ());
        v.setWrapText (true);

        return new ScrollPane (v);

    }

    private Pane createSQLConsolePanel ()
    {

        Form.Builder fb = Form.builder ()
            .confirmButton (getUILanguageStringProperty (buttons,close))
            .item (new SimpleStringProperty ("System Schema version"),
                   QuollLabel.builder ()
                        .label (new SimpleStringProperty (Environment.getSchemaVersion () + ""))
                        .build ());

        if (this.viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            int projSchemaVersion = -1;

            try
            {

                projSchemaVersion = pv.getObjectManager ().getSchemaVersion ();

            } catch (Exception e) {

                Environment.logError ("Unable to get project schema version",
                                      e);

            }

            fb.item (new SimpleStringProperty ("Project Schema version"),
                     QuollLabel.builder ()
                        .label (new SimpleStringProperty (projSchemaVersion + ""))
                        .build ());
            fb.item (new SimpleStringProperty ("Project db file"),
                     QuollLabel.builder ()
                        .label (new SimpleStringProperty ("jdbc:h2:" + pv.getObjectManager ().getDBDir ().toString () + "/" + Constants.PROJECT_DB_FILE_NAME_PREFIX + ".h2.db"))
                        .build ());

        }
        fb.inViewer (this.viewer);

        Form f = fb.build ();

        f.setOnConfirm (ev ->
        {

            this.close ();

        });

        return f;

    }

    private Region createPropertiesPanel (com.gentlyweb.properties.Properties ps)
    {

        java.util.List props = ps.getProperties ();

        try
        {

            Query q = new Query ();
            q.parse ("SELECT * FROM " + AbstractProperty.class.getName () + " ORDER BY iD");

            QueryResults qr = q.execute (ps.getProperties ());

            props = qr.getResults ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to execute sort query on properties",
                                  e);

        }

        List<List<String>> rows = new ArrayList<> ();

        for (int i = 0; i < props.size (); i++)
        {

            List<String> d = new ArrayList<> ();

            AbstractProperty p = (AbstractProperty) props.get (i);

            d.add (p.getID ());

            d.add (ps.getDefinedIn (p).getId ());

            d.add (p.getValue ());

            rows.add (d);

        }

        TableView<List<String>> tv = new TableView<> ();
        tv.setItems (FXCollections.observableList (rows));

        TableColumn<List<String>, String> c = new TableColumn<> ("Name");
        tv.getColumns ().add (c);
        c.setCellValueFactory (d -> new SimpleStringProperty (d.getValue ().get (0)));

        c = new TableColumn<> ("Scope");
        tv.getColumns ().add (c);
        c.setCellValueFactory (d -> new SimpleStringProperty (d.getValue ().get (1)));

        c = new TableColumn<> ("Value");
        tv.getColumns ().add (c);
        c.setCellValueFactory (d -> new SimpleStringProperty (d.getValue ().get (2)));

        return new ScrollPane (tv);

    }

    private Region createAchievementsPanel ()
    {

        List<List<String>> rows = new ArrayList<> ();

        Set<AchievementRule> user = Environment.getAchievementsManager ().getUserAchievedRules ();

        for (AchievementRule r : user)
        {

            List<String> d = new ArrayList<> ();

            d.add ("User");
            d.add (r.getId ());

            rows.add (d);

        }

        if (this.viewer instanceof AbstractProjectViewer)
        {

            Set<AchievementRule> project = Environment.getAchievementsManager ().getProjectAchievedIds ((AbstractProjectViewer) this.viewer);

            if (project != null)
            {

                for (AchievementRule r : project)
                {

                    List<String> d = new ArrayList<> ();

                    d.add ("Project");
                    d.add (r.getId ());

                    rows.add (d);

                }

            }

        }

/*
        JButton b = new JButton ("Clear Selected Achievements");

        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        final DebugConsole _this = this;

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Get the selected rows.
                int[] selRows = tab.getSelectedRows ();

                for (int i = selRows.length - 1; i > -1; i--)
                {

                    DefaultTableModel mod = (DefaultTableModel) tab.getModel ();

                    String id = (String) mod.getValueAt (i, 1);

                    try
                    {

                        Environment.getAchievementsManager ().removeAchievedAchievement (((String) mod.getValueAt (i, 0)).toLowerCase (),
                                                                                         id,
                                                                                         null);
                                                                                         // TODO _this.viewer);

                    } catch (Exception e) {

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to remove achievement: " + id);

                        Environment.logError ("Unable to remove achievement: " + id,
                                              e);

                    }

                    mod.removeRow (i);

                }

            }

        });
*/

        TableView<List<String>> tv = new TableView<> ();
        tv.setItems (FXCollections.observableList (rows));

        TableColumn<List<String>, String> c = new TableColumn<> ("Type");
        tv.getColumns ().add (c);
        c.setCellValueFactory (d -> new SimpleStringProperty (d.getValue ().get (0)));

        c = new TableColumn<> ("Id");
        tv.getColumns ().add (c);
        c.setCellValueFactory (d -> new SimpleStringProperty (d.getValue ().get (1)));

        return new ScrollPane (tv);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title ("Debug Console")
            .styleClassName (StyleClassNames.DEBUG)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

    private String getLogFilesAsSingleString ()
    {

        Set<Path> paths = Environment.getLogPaths ();

        StringBuilder b = new StringBuilder ();

        paths.stream ()
            .forEach (p ->
            {

                try
                {

                    b.append ("File: ");
                    b.append (p.toString ());
                    b.append ("\n");
                    b.append (Utils.getFileContentAsString (p));
                    b.append ("\n\n");

                } catch (Exception e) {

                    Environment.logError ("Unable to get log file: " +
                                          p,
                                          e);

                }

            });

        return b.toString ();

    }

}
