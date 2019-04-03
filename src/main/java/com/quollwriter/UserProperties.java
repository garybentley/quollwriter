package com.quollwriter;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import java.util.stream.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import java.awt.event.*;

import javafx.collections.*;
import javafx.scene.paint.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.ListProperty;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.properties.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.events.*;

public class UserProperties
{

    public static final String DEFAULT_SEPARATOR = "|";

    private static com.gentlyweb.properties.Properties props = new com.gentlyweb.properties.Properties ();
    private static Map<UserPropertyListener, Object> listeners = null;
    private static Path qwDir = null;

    private static Map<String, SimpleStringProperty> mappedProperties = new HashMap<> ();
    private static SimpleStringProperty tabsLocationProp = null;
    private static SimpleStringProperty projectInfoFormatProp = null;
    private static SimpleStringProperty editMarkerColorProp = null;
    private static SimpleStringProperty sortProjectsByProp = null;
    private static ObservableSet<Path> userBGImagePaths = null;
    private static SimpleSetProperty<Path> userBGImagePathsProp = null;
    private static ObservableList<Color> userColors = null;
    private static SimpleListProperty<Color> userColorsProp = null;
    private static ObservableSet<javafx.beans.property.StringProperty> projectStatuses = null;
    private static SetProperty<javafx.beans.property.StringProperty> projectStatusesProp = null;
    private static SimpleStringProperty noProjectStatusProp = null;
    private static SimpleStringProperty uiLayoutProp = null;

    // Just used in the map above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    static
    {

        UserProperties.listeners = Collections.synchronizedMap (new WeakHashMap<> ());

    }

    private UserProperties ()
    {

    }

    public static void init (com.gentlyweb.properties.Properties props)
                      throws IOException
    {

        if (props == null)
        {

            throw new NullPointerException ("Properties must be specified");

        }

        UserProperties.props = props;

        String v = UserProperties.get (Constants.PROJECT_INFO_FORMAT);

		if (v == null)
		{

			v = UserProperties.get (Constants.DEFAULT_PROJECT_INFO_FORMAT);

		}

        UserProperties.projectInfoFormatProp = UserProperties.createMappedProperty (Constants.PROJECT_INFO_FORMAT);

        UserProperties.tabsLocationProp = UserProperties.createMappedProperty (Constants.UI_LAYOUT_PROPERTY_NAME);

        UserProperties.editMarkerColorProp = UserProperties.createMappedProperty (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME);

        UserProperties.uiLayoutProp = UserProperties.createMappedProperty (Constants.UI_LAYOUT_PROPERTY_NAME);

        UserProperties.userBGImagePaths = FXCollections.observableSet (new LinkedHashSet<> ());

        try
        {

            UserProperties.initUserBGImagePaths ();

        } catch (Exception e) {

            throw new IOException ("Unable to init user background image paths",
                                   e);

        }

        UserProperties.userBGImagePathsProp = new SimpleSetProperty<> (UserProperties.userBGImagePaths);

        UserProperties.userBGImagePathsProp.addListener ((pr, oldv, newv) ->
        {

            // TODO Is this the best palce for this?  The exception gets sort of lost.
            try
            {

                Element root = new Element ("files");

                for (Path p : UserProperties.userBGImagePaths)
                {

                    Element el = new Element ("f");
                    el.addContent (p.toString ());

                    root.addContent (el);

                }

                // Get as a string.
                String data = JDOMUtils.getElementAsString (root);

                UserProperties.set (Constants.BG_IMAGE_FILES_PROPERTY_NAME,
                                    data);

            } catch (Exception e) {

                Environment.logError ("Unable to update background image files",
                                      e);

            }

        });

        try
        {

            UserProperties.initUserColors ();

        } catch (Exception e) {

            throw new IOException ("Unable to init user colors",
                                   e);

        }

        UserProperties.initProjectStatuses ();

    }

    public static Set<Color> getUserColors ()
    {

        return new LinkedHashSet<> (UserProperties.userColors);

    }

    public static SetProperty<javafx.beans.property.StringProperty> projectStatusesProperty ()
    {

        return UserProperties.projectStatusesProp;

    }

    public static void removeProjectStatus (String val)
    {

        if (val == null)
        {

            return;

        }

        javafx.beans.property.StringProperty p = UserProperties.getProjectStatus (val);

        if (p != null)
        {

            UserProperties.projectStatuses.remove (p);

            p.setValue (null);

        }

    }

    public static javafx.beans.property.StringProperty addProjectStatus (String val)
    {

        if (val == null)
        {

            return null;

        }

        javafx.beans.property.StringProperty sp = UserProperties.getProjectStatus (val);

        if ((sp != null)
            &&
            (sp != UserProperties.noProjectStatusProp)
           )
        {

            return sp;

        }

        sp = new javafx.beans.property.SimpleStringProperty (val);

        sp.addListener ((p, oldv, newv) ->
        {

            UserProperties.saveProjectStatuses ();

        });

        UserProperties.projectStatuses.add (sp);

        return sp;

    }

    public static javafx.beans.property.StringProperty getProjectStatus (String val)
    {

        if (val == null)
        {

            return UserProperties.noProjectStatusProp;

        }

        return UserProperties.projectStatuses.stream ()
            .filter (p -> val.equals (p.getValue ()))
            .findFirst ()
            .orElse (UserProperties.noProjectStatusProp);

    }

    public static javafx.beans.property.StringProperty noProjectStatusProperty ()
    {

        return UserProperties.noProjectStatusProp;

    }

    public static ObservableSet<javafx.beans.property.StringProperty> getProjectStatuses ()
    {

        return UserProperties.projectStatuses;

    }

    private static void initProjectStatuses ()
    {

        UserProperties.noProjectStatusProp = new SimpleStringProperty (null);

        Set<javafx.beans.property.StringProperty> set = new TreeSet<> ((o1, o2) -> o1.get ().toLowerCase ().compareTo (o2.get ().toLowerCase ()));

        UserProperties.projectStatuses = FXCollections.observableSet (set);

        String nt = UserProperties.get (Constants.PROJECT_STATUSES_PROPERTY_NAME);

        if (nt != null)
        {

            StringTokenizer t = new StringTokenizer (nt,
                                                     DEFAULT_SEPARATOR);

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                UserProperties.addProjectStatus (tok);

            }

        }

        UserProperties.projectStatusesProp = new SimpleSetProperty<> (UserProperties.projectStatuses);

        // Have to cast here to help out the compiler.
        UserProperties.projectStatusesProp.addListener ((SetChangeListener) (ev ->
        {

            UserProperties.saveProjectStatuses ();

        }));

    }

    private static void saveProjectStatuses ()
    {

        UserProperties.set (Constants.PROJECT_STATUSES_PROPERTY_NAME,
                            UserProperties.projectStatuses.stream ()
                                .map (p -> p.getValue ())
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    private static void initUserColors ()
                                 throws Exception
    {

        UserProperties.userColors = FXCollections.observableList (new ArrayList<> ());

        // TODO Use a stream?
        String colors = UserProperties.get (Constants.COLOR_SWATCHES_PROPERTY_NAME);

        StringTokenizer t = new StringTokenizer (colors,
                                                 ";");

        while (t.hasMoreTokens ())
        {

            String col = t.nextToken ().trim ();

            Color c = null;

            try
            {

                c = UIUtils.hexToColor (col);

            } catch (Exception e) {

                Environment.logError ("Invalid color: " + c,
                                      e);

                continue;

            }

            if (c != null)
            {

                UserProperties.userColors.add (c);

            }

        }

        UserProperties.userColorsProp = new SimpleListProperty<> (UserProperties.userColors);

        // Casting to help out the compiler.
        UserProperties.userColorsProp.addListener ((ListChangeListener) (ev ->
        {

            UserProperties.set (Constants.COLOR_SWATCHES_PROPERTY_NAME,
                                UserProperties.userColors.stream ()
                                    .map (c -> UIUtils.colorToHex (c))
                                    .collect (Collectors.joining (";")));

        }));

    }

    public static void removeUserBGImagePath (Path p)
    {

        UserProperties.userBGImagePaths.remove (p);

    }

    public static void addUserBGImagePath (Path p)
    {

        UserProperties.userBGImagePaths.add (p);

    }

    private static void initUserBGImagePaths ()
                                     throws Exception
    {

        String bgFiles = UserProperties.get (Constants.BG_IMAGE_FILES_PROPERTY_NAME);

        if (bgFiles == null)
        {

            return;

        }

        // Will be xml.
        Element root = JDOMUtils.getStringAsElement (bgFiles);

        List els = JDOMUtils.getChildElements (root,
                                               "f",
                                               false);

        for (int i = 0; i < els.size (); i++)
        {

            Element el = (Element) els.get (i);

            Path p = Paths.get (JDOMUtils.getChildContent (el));

            if ((Files.notExists (p))
                ||
                (Files.isDirectory (p))
               )
            {

                continue;

            }

            UserProperties.userBGImagePaths.add (p);

        }

    }

    public static void addUserColor (Color c)
    {

        UserProperties.userColors.add (0,
                                       c);

    }

    public static void removeUserColor (Color c)
    {

        UserProperties.userColors.remove (c);

    }

    public static ListProperty<Color> userColorsProperty ()
    {

        return UserProperties.userColorsProp;

    }

    public static SetProperty<Path> userBGImagePathsProperty ()
    {

        return UserProperties.userBGImagePathsProp;

    }

    public static SimpleStringProperty getMappedStringProperty (String name)
    {

        SimpleStringProperty s = UserProperties.mappedProperties.get (name);

        if (s == null)
        {

            s = UserProperties.createMappedProperty (name);

        }

        return s;

    }

    public static void setUILayout (String v)
    {

        UserProperties.uiLayoutProp.setValue (v);

    }

    public static SimpleStringProperty uiLayoutProperty ()
    {

        return UserProperties.uiLayoutProp;

    }

    public static URL getUserStyleSheetURL ()
    {

        // TODO Make a property.
        return UserProperties.class.getResource (Constants.DEFAULT_STYLE_SHEET_FILE_NAME);

    }

    private static SimpleStringProperty createMappedProperty (String name)
    {

        SimpleStringProperty s = new SimpleStringProperty (UserProperties.get (name));
        UserProperties.mappedProperties.put (name,
                                             s);

        return s;

    }

    public static SimpleStringProperty editMarkerColorProperty ()
    {

        return UserProperties.editMarkerColorProp;

    }

    public static SimpleStringProperty projectInfoFormatProperty ()
    {

        return UserProperties.projectInfoFormatProp;

    }

    public static String getProjectInfoFormat ()
    {

        return UserProperties.projectInfoFormatProp.getValue ();

    }

    public static void setProjectInfoFormat (String v)
    {

        UserProperties.projectInfoFormatProp.setValue (v);

    }

    public static SimpleStringProperty tabsLocationProperty ()
    {

        return UserProperties.tabsLocationProp;

    }

    // TODO Make an enum.
    public static void setTabsLocation (String loc)
    {

        UserProperties.tabsLocationProp.setValue (loc);

    }

    public static void removeListener (UserPropertyListener l)
    {

        UserProperties.listeners.remove (l);

    }

    /**
     * Adds a listener for property events.  Warning!  This will be a soft reference that can
     * disappear so make sure you have a strong reference to your listener.
     *
     * @param l The listener.
     */
    public static void addListener (UserPropertyListener l)
    {

        UserProperties.listeners.put (l,
                                      UserProperties.listenerFillObj);

    }

    public static void fireUserPropertyEvent (Object                 source,
                                              String                 name,
                                              AbstractProperty       prop,
                                              UserPropertyEvent.Type action)
    {

        UserProperties.fireUserPropertyEvent (new UserPropertyEvent (source,
                                                                     name,
                                                                     prop,
                                                                     action));

    }

    public static void fireUserPropertyEvent (final UserPropertyEvent ev)
    {

        com.quollwriter.ui.UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent aev)
            {

                Set<UserPropertyListener> ls = null;

                // Get a copy of the current valid listeners.
                synchronized (UserProperties.listeners)
                {

                    ls = new LinkedHashSet<> (UserProperties.listeners.keySet ());

                }

                for (UserPropertyListener l : ls)
                {

                    l.propertyChanged (ev);

                }

            }

        });

    }

    public static void remove (String name)
    {

        UserProperties.props.removeProperty (name);

        UserProperties.fireUserPropertyEvent (UserProperties.listenerFillObj,
                                              name,
                                              null,
                                              UserPropertyEvent.Type.removed);

        UserProperties.save ();

    }

    public static void set (String           name,
                            AbstractProperty prop)
    {

        UserProperties.props.setProperty (name,
                                          prop);

        UserProperties.fireUserPropertyEvent (UserProperties.listenerFillObj,
                                              name,
                                              prop,
                                              UserPropertyEvent.Type.changed);

        UserProperties.save ();

    }

    public static void set (String name,
                            String value)
    {

        UserProperties.set (name,
                            new StringProperty (name,
                                                value));

        SimpleStringProperty p = UserProperties.mappedProperties.get (name);

        if (p == null)
        {

            p = UserProperties.createMappedProperty (name);

        }

        if (p != null)
        {

            p.setValue (value);

        }

    }

    public static void set (String  name,
                            boolean value)
    {

        UserProperties.set (name,
                            new BooleanProperty (name,
                                                 value));

        UserProperties.save ();

    }

    public static void set (String  name,
                            float   value)
    {

        UserProperties.set (name,
                            new FloatProperty (name,
                                               value));

        UserProperties.save ();

    }

    public static void set (String  name,
                            int     value)
    {

        UserProperties.set (name,
                            new IntegerProperty (name,
                                                 value));

        UserProperties.save ();

    }

    public static boolean getAsBoolean (String name,
                                        String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.getAsBoolean (defOnNull);

        }

        return UserProperties.props.getPropertyAsBoolean (name);

    }

    public static boolean getAsBoolean (String name)
    {

        return UserProperties.props.getPropertyAsBoolean (name);

    }

    public static int getAsInt (String name,
                                String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.getAsInt (defOnNull);

        }

        return UserProperties.props.getPropertyAsInt (name);

    }

    public static int getAsInt (String name)
    {

        return UserProperties.props.getPropertyAsInt (name);

    }

    public static File getAsFile (String name)
    {

        return UserProperties.props.getPropertyAsFile (name);

    }

    public static float getAsFloat (String name,
                                    String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.getAsFloat (defOnNull);

        }

        return UserProperties.props.getPropertyAsFloat (name);

    }

    public static float getAsFloat (String name)
    {

        return UserProperties.props.getPropertyAsFloat (name);

    }

    public static String get (String name,
                              String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.get (defOnNull);

        }

        return UserProperties.props.getProperty (name);

    }

    public static String get (String name)
    {

        return UserProperties.props.getProperty (name);

    }

    public static AbstractProperty getProperty (String name)
    {

        return UserProperties.props.getPropertyObj (name);

    }

    public static Properties getProperties ()
    {

        return UserProperties.props;

    }

    private static void save ()
    {

        try
        {

            Environment.saveUserProperties ();

        } catch (Exception e) {

            Environment.logError ("Unable to set user properties",
                                  e);

        }

    }

    public static Path getUserEditorsPropertiesPath ()
    {

        return Environment.getUserPath (Constants.EDITORS_PROPERTIES_FILE_NAME);

    }

    public static Path getUserDefaultProjectPropertiesPath ()
    {

        return Environment.getUserPath (Constants.DEFAULT_PROJECT_PROPERTIES_FILE_NAME);

    }

    /**
     * No longer used, since properties now stored in projects db.
     * This is only used for legacy versions that need to port the properties over
     * to the new storage method.
     */
    public static Path getUserPropertiesPath ()
    {

        return Environment.getUserPath (Constants.PROPERTIES_FILE_NAME);

    }

    public static Path getUserObjectTypeNamesPath ()
    {

        return Environment.getUserPath (Constants.OBJECT_TYPE_NAMES_FILE_NAME);

    }

}
