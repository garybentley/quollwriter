package com.quollwriter.ui.fx;

import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.viewers.*;

public class DebugConsole extends Region
{

    private AbstractViewer viewer = null;

    public DebugConsole (AbstractViewer viewer)
    {
/*
        this.viewer = viewer;

        JTabbedPane tp = new JTabbedPane ();
                b.add (tp);

                try
                {

                    tp.add ("Logs",
                            this.createLogPanel ());

                } catch (Exception e) {

                    e.printStackTrace (Environment.out);

                }

                if (this.viewer instanceof AbstractProjectViewer)
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                    tp.add ("SQL Console",
                            this.createSQLConsolePanel (pv));

                    tp.add ("Properties",
                            this.createPropertiesPanel (pv.getProject ().getProperties ()));

                }

                tp.add ("Achievements",
                        this.createAchievementsPanel ());

                b.add (Box.createVerticalStrut (5));

                java.util.List bs = new ArrayList ();

                final DebugConsole _this = this;

                JButton but = new JButton ("Close");

                but.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.setVisible (false);
                            _this.dispose ();

                        }

                    });

                bs.add (but);

                b.add (UIUtils.createButtonBar2 ((JButton[]) bs.toArray (new JButton[bs.size ()]),
                                                 Component.LEFT_ALIGNMENT));

                this.getContentPane ().setPreferredSize (new Dimension (800,
                                                                        600));

                this.getContentPane ().add (content);

                this.pack ();
                this.pack ();

                this.setVisible (true);
    }

    int max = Environment.getSchemaVersion ();
            int projSchemaVersion = -1;

            try
            {

                projSchemaVersion = pv.getObjectManager ().getSchemaVersion ();

            } catch (Exception e) {

                Environment.logError ("Unable to get project schema version",
                                      e);

            }

            builder.addLabel ("System schema version",
                              cc.xy (1,
                                     row));

            builder.addLabel (max + "",
                              cc.xy (3,
                                     row));

            row += 2;

            builder.addLabel ("Project schema version",
                              cc.xy (1,
                                     row));

            builder.addLabel (projSchemaVersion + "",
                              cc.xy (3,
                                     row));

            row += 2;

            builder.addLabel ("Project db file",
                              cc.xy (1,
                                     row));

            String url = "jdbc:h2:" + pv.getObjectManager ().getDBDir ().toString () + "/" + Constants.PROJECT_DB_FILE_NAME_PREFIX + ".h2.db";

            final JTextField urlf = new JTextField (url);
            urlf.setText (url);
            urlf.setEditable (false);
            urlf.addMouseListener (new MouseAdapter ()
            {

                public void mouseEntered (MouseEvent ev)
                {

                    urlf.grabFocus ();
                    urlf.selectAll ();

                }

            });


            builder.add (urlf,
                         cc.xy (3,
                                row));

            row += 2;

            Box versBox = new Box (BoxLayout.X_AXIS);
            versBox.setAlignmentX (Component.LEFT_ALIGNMENT);

            Vector vers = new Vector ();

            for (int i = max; i > 0; i--)
            {

                vers.add ("  " + i);

            }

            final JComboBox versions = new JComboBox (vers);

            versBox.add (versions);
            versBox.add (Box.createHorizontalStrut (5));

            JButton upgradeButton = UIUtils.createButton ("Run");
            versBox.add (upgradeButton);
            versBox.add (Box.createHorizontalGlue ());

            upgradeButton.addActionListener (new ActionListener ()
            {

                 public void actionPerformed (ActionEvent ev)
                 {

                     int ver = -1;

                     try
                     {

                        ver = Integer.parseInt ((versions.getSelectedItem () + "").trim ());

                     } catch (Exception e) {

                        return;

                     }

                     try
                     {

                        pv.getObjectManager ().forceRunUpgradeScript (ver);

                     } catch (Exception e) {

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upgrade to version: " + ver + " see the log for details.");

                        Environment.logError ("Unable to upgrade to version: " + ver,
                                              e);

                     }

                 }

            });

            builder.addLabel ("Upgrade to version",
                              cc.xy (1,
                                     row));

            builder.add (versBox,
                         cc.xy (3,
                                row));

            row += 2;

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (Component.LEFT_ALIGNMENT);
            p.setBorder (new EmptyBorder (5, 10, 10, 5));

            bprops.add (p);

            JSplitPane sp = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
            sp.setAlignmentX (Component.LEFT_ALIGNMENT);
            sp.setDividerLocation (0.5f);

            //bprops.add (sp);

            final JTextArea console = new JTextArea ();
            console.setMargin (new Insets (5,
                                           5,
                                           5,
                                           5));
            console.setFont (new Font ("Consolas",
                                       Font.PLAIN,
                                       12));
            // console.setBorder (null);

            JScrollPane scroll = new JScrollPane (console);
            scroll.setBorder (null);
            scroll.getViewport ().setBorder (null);
            scroll.setAlignmentX (Component.LEFT_ALIGNMENT);

            Box b = new Box (BoxLayout.PAGE_AXIS);
            b.add (scroll);

            // Add a button bar.

            sp.setTopComponent (b);


            Vector colNames = new Vector ();

            /*
            JTable tab = new JTable (data,
                                     colNames);

            bprops.add (new JScrollPane (tab));
             */
             /*
            return bprops;

        }
*/
/*
        private JComponent createPropertiesPanel (Properties ps)
        {

            Box bprops = new Box (BoxLayout.PAGE_AXIS);
            bprops.setBorder (new EmptyBorder (10,
                                               3,
                                               3,
                                               3));

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

            Vector data = new Vector ();

            for (int i = 0; i < props.size (); i++)
            {

                Vector d = new Vector ();

                AbstractProperty p = (AbstractProperty) props.get (i);

                d.add (p.getID ());

                d.add (ps.getDefinedIn (p).getId ());

                d.add (p.getValue ());

                data.add (d);

            }

            Vector colNames = new Vector ();
            colNames.add ("Name");
            colNames.add ("Scope");
            colNames.add ("Value");

            JTable tab = new JTable (data,
                                     colNames);

            bprops.add (new JScrollPane (tab));

            return bprops;

        }

        private JComponent createAchievementsPanel ()
        {

            Box bprops = new Box (BoxLayout.PAGE_AXIS);
            bprops.setBorder (new EmptyBorder (10,
                                               3,
                                               3,
                                               3));

            Map<String, Set<String>> achieved = null; // TODO Environment.getAchievementsManager ().getAchievedAchievementIds (this.viewer);

            Vector data = new Vector ();

            Set<String> user = achieved.get ("user");

            for (String id : user)
            {

                Vector d = new Vector ();

                d.add ("User");
                d.add (id);

                data.add (d);

            }

            Set<String> project = achieved.get ("project");

            if (project != null)
            {

                for (String id : project)
                {

                    Vector d = new Vector ();

                    d.add ("Project");
                    d.add (id);

                    data.add (d);

                }

            }

            Vector colNames = new Vector ();
            colNames.add ("Type");
            colNames.add ("Id");

            final JTable tab = new JTable (data,
                                           colNames);

            bprops.add (new JScrollPane (tab));

            bprops.add (Box.createVerticalStrut (10));

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

            bprops.add (b);

            return bprops;

        }

        private JComponent createLogPanel ()
                                    throws IOException
        {

            JTabbedPane tp = new JTabbedPane ();

            tp.setBorder (new EmptyBorder (10,
                                           5,
                                           5,
                                           5));

            Box errorlog = new Box (BoxLayout.PAGE_AXIS);
            errorlog.setBorder (new EmptyBorder (3,
                                                 3,
                                                 3,
                                                 3));
                                                 */
    /*
    TODO Remove
            File file = Environment.getErrorLogFile ();

            JTextField f = UIUtils.createTextField ();
            f.setText (file + ", can write: " + file.canWrite () + ", can read: " + file.canRead ());

            f.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             f.getPreferredSize ().height));
            f.setAlignmentX (Component.LEFT_ALIGNMENT);

            errorlog.add (f);

            errorlog.add (Box.createVerticalStrut (5));

            JTextArea t = new JTextArea ();

            try
            {

                t.setText (Utils.getFileContentAsString (file.toPath ()));

            } catch (Exception e)
            {

                try
                {

                    t.setText ("Unable to read log file: " +
                               file +
                               ", Exception: \n" +
                               Utils.getExceptionTraceAsString (e));

                } catch (Exception ee)
                {
                }

            }

            errorlog.add (Box.createVerticalStrut (5));

            errorlog.add (new JScrollPane (t));

            tp.add ("Error",
                    errorlog);

            Box sqllog = new Box (BoxLayout.PAGE_AXIS);
            sqllog.setBorder (new EmptyBorder (3,
                                               3,
                                               3,
                                               3));

            file = Environment.getLogFile ();

            f = UIUtils.createTextField ();
            f.setText (file + ", can write: " + file.canWrite () + ", can read: " + file.canRead ());
            f.setAlignmentX (Component.LEFT_ALIGNMENT);

            f.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             f.getPreferredSize ().height));

            sqllog.add (f);

            sqllog.add (Box.createVerticalStrut (5));

            t = new JTextArea ();

            try
            {

                t.setText (Utils.getFileContentAsString (file.toPath ()));

            } catch (Exception e)
            {

                try
                {

                    t.setText ("Unable to read log file: " +
                               file +
                               ", Exception: \n" +
                               Utils.getExceptionTraceAsString (e));

                } catch (Exception ee)
                {
                }

            }

            sqllog.add (Box.createVerticalStrut (5));

            sqllog.add (new JScrollPane (t));

            tp.add ("SQL",
                    sqllog);

            Box genlog = new Box (BoxLayout.PAGE_AXIS);
            genlog.setBorder (new EmptyBorder (3,
                                               3,
                                               3,
                                               3));

            file = Environment.getGeneralLogFile ();

            f = UIUtils.createTextField ();
            f.setText (file + ", can write: " + file.canWrite () + ", can read: " + file.canRead ());
            f.setAlignmentX (Component.LEFT_ALIGNMENT);

            f.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             f.getPreferredSize ().height));

            genlog.add (f);

            genlog.add (Box.createVerticalStrut (5));

            t = new JTextArea ();

            try
            {

                t.setText (Utils.getFileContentAsString (file.toPath ()));

            } catch (Exception e)
            {

                try
                {

                    t.setText ("Unable to read log file: " +
                               file +
                               ", Exception: \n" +
                               Utils.getExceptionTraceAsString (e));

                } catch (Exception ee)
                {
                }

            }

            genlog.add (Box.createVerticalStrut (5));

            genlog.add (new JScrollPane (t));

            tp.add ("General",
                    genlog);
    */
            //return tp;

        }
}
