package com.quollwriter;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

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
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import java.awt.event.*;

import javafx.collections.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.media.*;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.ListProperty;

import org.dom4j.*;
import org.dom4j.tree.*;

import com.gentlyweb.properties.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.events.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class UserProperties
{

    public static final double DEFAULT_FULL_SCREEN_X_BORDER_WIDTH = 0.3d; //(7f / 100f);
    public static final double DEFAULT_FULL_SCREEN_Y_BORDER_WIDTH = 0.3d; //(7f / 100f);
    public static final double DEFAULT_FULL_SCREEN_OPACITY = 1d;

    public static final double FULL_SCREEN_X_BORDER_WIDTH_ADJUST_INCR = 0.001d;
    public static final double FULL_SCREEN_Y_BORDER_WIDTH_ADJUST_INCR = 0.002d;
    public static final double FULL_SCREEN_MIN_X_BORDER_WIDTH = 0.05d;
    public static final double FULL_SCREEN_MAX_X_BORDER_WIDTH = 0.4d;
    public static final double FULL_SCREEN_MIN_Y_BORDER_WIDTH = 0.05d;
    public static final double FULL_SCREEN_MAX_Y_BORDER_WIDTH = 0.4d;

    public static final String DEFAULT_SEPARATOR = "|";

    private static com.gentlyweb.properties.Properties props = new com.gentlyweb.properties.Properties ();
    private static Map<UserPropertyListener, Object> listeners = null;
    private static Path qwDir = null;

    private static Map<String, SimpleBooleanProperty> mappedBooleanProperties = new HashMap<> ();
    private static Map<String, SimpleStringProperty> mappedProperties = new HashMap<> ();
    private static SimpleStringProperty tabsLocationProp = null;
    private static SimpleStringProperty toolbarLocationProp = null;
    private static SimpleStringProperty projectInfoFormatProp = null;
    private static SimpleObjectProperty<Color> editMarkerColorProp = null;
    private static SimpleStringProperty sortProjectsByProp = null;
    private static ObservableSet<Path> userBGImagePaths = null;
    //private static SimpleSetProperty<Path> userBGImagePathsProp = null;
    private static ObservableSet<Color> userColors = null;
    private static ObservableSet<javafx.beans.property.StringProperty> projectStatuses = null;
    private static SetProperty<javafx.beans.property.StringProperty> projectStatusesProp = null;
    private static SimpleStringProperty noProjectStatusProp = null;
    private static SimpleStringProperty uiLayoutProp = null;
    private static SimpleIntegerProperty chapterAutoSaveTimeProp = null;
    private static SimpleBooleanProperty chapterAutoSaveEnabledProp = null;
    private static SimpleFloatProperty uiBaseFontSizeProp = null;
    private static SimpleObjectProperty<Font> uiBaseFontProp = null;
    private static SimpleBooleanProperty showEditPositionIconInChapterListProp = null;
    private static SimpleBooleanProperty showEditCompleteIconInChapterListProp = null;
    private static SimpleBooleanProperty showEditMarkerInChapterProp = null;
    private static SimpleBooleanProperty showNotesInChapterListProp = null;
    private static SimpleIntegerProperty autoBackupsTimeProp = null;
    private static SimpleIntegerProperty backupsToKeepCountProp = null;
    private static SimpleBooleanProperty autoBackupsEnabledProp = null;
    private static SimpleBooleanProperty keepProjectsWindowWhenProjectOpenedProp = null;
    private static ObservableSet<String> fullScreenHeaderControlButtonIds = null;
    private static ObservableSet<String> projectViewerHeaderControlButtonIds = null;
    private static ObservableSet<String> allProjectsViewerHeaderControlButtonIds = null;
    private static ObservableSet<String> warmupViewerHeaderControlButtonIds = null;
    private static ObservableSet<String> languageStringsEditorHeaderControlButtonIds = null;

    // Just used in the map above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    private static SetChangeListener projectStatusesListener = null;
    private static SetChangeListener<Color> userColorsListener = null;

    private static ObservableSet<javafx.beans.property.StringProperty> noteTypes = null;

    private static SimpleObjectProperty<Color> editNeededNoteChapterHighlightColorProp = null;
    private static SimpleObjectProperty<Color> problemFinderBlockHighlightColorProp = null;
    private static SimpleObjectProperty<Color> problemFinderIssueHighlightColorProp = null;
    private static SimpleObjectProperty<Color> synonymHighlightColorProp = null;
    private static SimpleObjectProperty<Color> findHighlightColorProp = null;
    private static SimpleObjectProperty<Color> editorCommentChapterHighlightColorProp = null;

    private static SimpleDoubleProperty fullScreenXBorderWidthProp = null;
    private static SimpleDoubleProperty fullScreenYBorderWidthProp = null;
    private static SimpleDoubleProperty fullScreenOpacityProp = null;
    private static SimpleObjectProperty<Object> fullScreenBackgroundProp = null;

    private static SimpleObjectProperty<Path> userStyleSheetProp = null;

    private static SimpleBooleanProperty playSoundOnKeyStrokeProp = null;
    private static SimpleObjectProperty<Path> keyStrokeSoundPathProp = null;
    //private static AudioClip keyStrokeSound = null;
    private static javax.sound.sampled.Clip keyStrokeSound = null;
    private static byte[] keyStrokeSoundBytes = null;
    private static UserDictionaryProvider uiTextDictionaryProv = null;
    private static SimpleBooleanProperty autoNightModeProp = null;
    private static SimpleObjectProperty<LocalTime> autoNightModeToProp = null;
    private static SimpleObjectProperty<LocalTime> autoNightModeFromProp = null;
    private static SimpleBooleanProperty permanentNightModeEnabledProp = null;

    static
    {

        UserProperties.listeners = Collections.synchronizedMap (new WeakHashMap<> ());

    }

    private UserProperties ()
    {

    }

    public static void init (com.gentlyweb.properties.Properties props)
                      throws Exception
    {

        if (props == null)
        {

            throw new NullPointerException ("Properties must be specified");

        }

        UserProperties.props = props;

        UserProperties.projectInfoFormatProp = UserProperties.createMappedProperty (Constants.PROJECT_INFO_FORMAT,
                                                                                    Constants.DEFAULT_PROJECT_INFO_FORMAT);
        UserProperties.tabsLocationProp = UserProperties.createMappedProperty (Constants.TABS_LOCATION_PROPERTY_NAME);
        UserProperties.toolbarLocationProp = UserProperties.createMappedProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME);

        UserProperties.playSoundOnKeyStrokeProp = new SimpleBooleanProperty (UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME));

        UserProperties.playSoundOnKeyStrokeProp.addListener ((p, oldv, newv) ->
        {

            UserProperties.set (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                                newv);

            Environment.fireUserProjectEvent (ProjectEvent.Type.typewritersound,
                                              (newv ? ProjectEvent.Action.on : ProjectEvent.Action.off));

        });

        UserProperties.keyStrokeSoundPathProp = new SimpleObjectProperty<Path> ();

        String sf = UserProperties.get (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        try
        {

            UserProperties.setKeyStrokeSoundFilePath ((sf != null ? Paths.get (sf) : null));
            //Utils.getResourceUrl (sf).toURI ()) : null));

        } catch (Exception e) {

            Environment.logError ("Unable to load key stroke sound from: " + sf,
                                  e);

        }

        UserProperties.keyStrokeSoundPathProp.addListener ((p, oldv, newv) ->
        {

            if (newv == null)
            {

                UserProperties.remove (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

                return;

            }

            UserProperties.set (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                                newv.toString ());

            // TODO Add an event?

        });

        UserProperties.userStyleSheetProp = new SimpleObjectProperty<> ();

        String us = UserProperties.get (Constants.USER_STYLE_SHEET_FILE_NAME_PROPERTY_NAME);

        if (us != null)
        {

            Path p = Paths.get (us);

            UserProperties.userStyleSheetProp.setValue (p);

        }

        UserProperties.editMarkerColorProp = new SimpleObjectProperty<> ();
        UserProperties.problemFinderBlockHighlightColorProp = new SimpleObjectProperty<> ();
        UserProperties.problemFinderIssueHighlightColorProp = new SimpleObjectProperty<> ();
        UserProperties.synonymHighlightColorProp = new SimpleObjectProperty<> ();
        UserProperties.findHighlightColorProp = new SimpleObjectProperty<> ();

        String col = UserProperties.get (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME);

        UserProperties.editMarkerColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.editMarkerColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        UserProperties.editNeededNoteChapterHighlightColorProp = new SimpleObjectProperty<> ();

        col = UserProperties.get (Constants.EDIT_NEEDED_NOTE_CHAPTER_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.editNeededNoteChapterHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.editNeededNoteChapterHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.EDIT_NEEDED_NOTE_CHAPTER_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        UserProperties.editorCommentChapterHighlightColorProp = new SimpleObjectProperty<> ();

        col = UserProperties.get (Constants.EDITOR_COMMENT_CHAPTER_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.editorCommentChapterHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.editorCommentChapterHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.EDITOR_COMMENT_CHAPTER_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        UserProperties.permanentNightModeEnabledProp = UserProperties.createMappedBooleanProperty (Constants.NIGHT_MODE_ENABLE_PERMENANTLY_PROPERTY_NAME);

        UserProperties.autoNightModeProp = UserProperties.createMappedBooleanProperty (Constants.AUTO_NIGHT_MODE_ENABLED_PROPERTY_NAME);

        UserProperties.autoNightModeToProp = new SimpleObjectProperty<> ();
        UserProperties.autoNightModeFromProp = new SimpleObjectProperty<> ();

        DateTimeFormatter dFormat = DateTimeFormatter.ofPattern ("kk:mm");

        String v = UserProperties.get (Constants.AUTO_NIGHT_MODE_TO_PROPERTY_NAME);

        if (v != null)
        {

            UserProperties.autoNightModeToProp.setValue (LocalTime.parse (v,
                                                                          dFormat));

        }

        UserProperties.autoNightModeToProp.addListener ((pr, oldv, newv) ->
        {

            String _v = null;

            if (newv != null)
            {

                _v = newv.format (dFormat);

            }

            UserProperties.set (Constants.AUTO_NIGHT_MODE_TO_PROPERTY_NAME,
                                _v);

        });

        v = UserProperties.get (Constants.AUTO_NIGHT_MODE_FROM_PROPERTY_NAME);

        if (v != null)
        {

            UserProperties.autoNightModeFromProp.setValue (LocalTime.parse (v,
                                                                            dFormat));

        }

        UserProperties.autoNightModeFromProp.addListener ((pr, oldv, newv) ->
        {

            String _v = null;

            if (newv != null)
            {

                _v = newv.format (dFormat);

            }

            UserProperties.set (Constants.AUTO_NIGHT_MODE_FROM_PROPERTY_NAME,
                                _v);

        });

        col = UserProperties.get (Constants.PROBLEM_FINDER_BLOCK_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.problemFinderBlockHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.problemFinderBlockHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.PROBLEM_FINDER_BLOCK_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        col = UserProperties.get (Constants.PROBLEM_FINDER_ISSUE_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.problemFinderIssueHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.problemFinderIssueHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.PROBLEM_FINDER_ISSUE_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        col = UserProperties.get (Constants.SYNONYM_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.synonymHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.synonymHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.SYNONYM_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        col = UserProperties.get (Constants.FIND_HIGHLIGHT_COLOR_PROPERTY_NAME);

        UserProperties.findHighlightColorProp.setValue (UIUtils.hexToColor (col));

        UserProperties.findHighlightColorProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.FIND_HIGHLIGHT_COLOR_PROPERTY_NAME,
                                UIUtils.colorToHex (newv));

        });

        UserProperties.uiLayoutProp = UserProperties.createMappedProperty (Constants.UI_LAYOUT_PROPERTY_NAME);

        UserProperties.showEditPositionIconInChapterListProp = UserProperties.createMappedBooleanProperty (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

        UserProperties.showEditCompleteIconInChapterListProp = UserProperties.createMappedBooleanProperty (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

        UserProperties.showEditMarkerInChapterProp = UserProperties.createMappedBooleanProperty (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME);

        UserProperties.showNotesInChapterListProp = UserProperties.createMappedBooleanProperty (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME);

        UserProperties.autoBackupsEnabledProp = UserProperties.createMappedBooleanProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);

        UserProperties.keepProjectsWindowWhenProjectOpenedProp = UserProperties.createMappedBooleanProperty (Constants.KEEP_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME);

        UserProperties.userBGImagePaths = FXCollections.observableSet (new LinkedHashSet<> ());

        try
        {

            UserProperties.initUserBGImagePaths ();

        } catch (Exception e) {

            throw new IOException ("Unable to init user background image paths",
                                   e);

        }

        //UserProperties.userBGImagePathsProp = new SimpleSetProperty<> (UserProperties.userBGImagePaths);

        UserProperties.userBGImagePaths.addListener ((SetChangeListener<Path>) (ev) ->
        {

            // TODO Is this the best palce for this?  The exception gets sort of lost.
            try
            {

                // Encode as json...
                String data = JSONEncoder.encode (UserProperties.userBGImagePaths);

                /*

                Element root = new DefaultElement ("files");

                for (Path p : UserProperties.userBGImagePaths)
                {

                    Element el = new DefaultElement ("f");
                    el.add (new DefaultCDATA (p.toString ()));

                    root.add (el);

                }

                // Get as a string.
                String data = DOM4JUtils.elementAsString (root);
*/
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

        // Have to do this after startup since it may rely on the user ui language strings.
        Environment.startupCompleteProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                UserProperties.initNoteTypes ();

            } catch (Exception e) {

                Environment.logError ("Unable to init note types",
                                      e);

            }

        });

        UserProperties.initProjectStatuses ();

        UserProperties.chapterAutoSaveTimeProp = new SimpleIntegerProperty ();

        String val = UserProperties.get (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME);

        if (val != null)
        {

            try
            {

                UserProperties.chapterAutoSaveTimeProp.setValue (Integer.parseInt (val));

            } catch (Exception e) {

                UserProperties.chapterAutoSaveTimeProp.setValue (Constants.DEFAULT_CHAPTER_AUTO_SAVE_TIME);

            }

        }

        UserProperties.chapterAutoSaveTimeProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME,
                                String.valueOf (newv));

        });

        UserProperties.chapterAutoSaveEnabledProp = new SimpleBooleanProperty ();
        UserProperties.chapterAutoSaveEnabledProp.setValue (UserProperties.getAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME));

        UserProperties.chapterAutoSaveEnabledProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                newv);

        });

        UserProperties.uiBaseFontSizeProp = new SimpleFloatProperty ();

        AbstractProperty a = UserProperties.props.getPropertyObj (Constants.UI_BASE_FONT_SIZE_PROPERTY_NAME);

        if (a != null)
        {

            UserProperties.uiBaseFontSizeProp.setValue (UserProperties.getAsFloat (Constants.UI_BASE_FONT_SIZE_PROPERTY_NAME));

        }

        UserProperties.uiBaseFontSizeProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.UI_BASE_FONT_SIZE_PROPERTY_NAME,
                                newv.floatValue ());

        });

        UserProperties.uiBaseFontProp = new SimpleObjectProperty<> ();

        String f = UserProperties.get (Constants.UI_BASE_FONT_PROPERTY_NAME);

        //Font font = Font.getDefault ();

        if (f != null)
        {

            //font = Font.font (f);
            UserProperties.uiBaseFontProp.setValue (Font.font (f));

        }
/*
        if (font == null)
        {

            font = Font.getDefault ();

        }
*/
        UserProperties.uiBaseFontProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.UI_BASE_FONT_PROPERTY_NAME,
                                newv.getName ());

        });

        UserProperties.autoBackupsTimeProp = new SimpleIntegerProperty ();

        Integer ival = UserProperties.getAsInt (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME);

        if (ival != null)
        {

            UserProperties.autoBackupsTimeProp.setValue (ival);

        } else {

            UserProperties.autoBackupsTimeProp.setValue (Constants.DEFAULT_CHAPTER_AUTO_SAVE_TIME);

        }

        UserProperties.autoBackupsTimeProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                newv.intValue ());

        });

        UserProperties.backupsToKeepCountProp = new SimpleIntegerProperty ();

        ival = UserProperties.getAsInt (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME);

        if (ival != null)
        {

            UserProperties.backupsToKeepCountProp.setValue (ival);

        } else {

            UserProperties.backupsToKeepCountProp.setValue (Constants.DEFAULT_BACKUPS_TO_KEEP);

        }

        UserProperties.backupsToKeepCountProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME,
                                newv.intValue ());

        });

        UserProperties.fullScreenXBorderWidthProp = new SimpleDoubleProperty (UserProperties.constrainFullScreenXBorderWidth (UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME)));
        UserProperties.fullScreenXBorderWidthProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.FULL_SCREEN_BORDER_X_WIDTH_PROPERTY_NAME,
                                newv.floatValue ());

        });

        UserProperties.fullScreenYBorderWidthProp = new SimpleDoubleProperty (UserProperties.constrainFullScreenYBorderWidth (UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME)));
        UserProperties.fullScreenYBorderWidthProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.FULL_SCREEN_BORDER_Y_WIDTH_PROPERTY_NAME,
                                newv.floatValue ());

        });

        UserProperties.fullScreenOpacityProp = new SimpleDoubleProperty (0);
        Float d = UserProperties.getAsFloat (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME);

        UserProperties.fullScreenOpacityProp.setValue (d != null ? d : DEFAULT_FULL_SCREEN_OPACITY);
        UserProperties.fullScreenOpacityProp.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (Constants.FULL_SCREEN_BORDER_OPACITY_PROPERTY_NAME,
                                newv.floatValue ());

        });

        UserProperties.fullScreenBackgroundProp = new SimpleObjectProperty<> ();

        v = UserProperties.get (Constants.FULL_SCREEN_BG_PROPERTY_NAME);

        if (v == null)
        {

            v = Constants.DEFAULT_FULL_SCREEN_BG_IMAGE_FILE_NAME;

        }

        try
        {

            UserProperties.fullScreenBackgroundProp.setValue (BackgroundObject.createBackgroundObjectForId (v));

        } catch (Exception e) {

            Environment.logError ("Unable to set background object to: " +
                                  v);

        }

        UserProperties.fullScreenBackgroundProp.addListener ((pr, oldv, newv) ->
        {

            String id = null;

            if (newv != null)
            {

                BackgroundObject b = new BackgroundObject ();

                try
                {

                    b.update (newv);

                    id = b.getAsString ();

                } catch (Exception e) {

                    Environment.logError ("Unable to update full screen background to: " +
                                          newv,
                                          e);

                }

            }

            UserProperties.set (Constants.FULL_SCREEN_BG_PROPERTY_NAME,
                                id);

        });

         UserProperties.fullScreenHeaderControlButtonIds = FXCollections.observableSet (new LinkedHashSet<> ());

         String nt = UserProperties.get (Constants.FULL_SCREEN_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME);

         if (nt != null)
         {

             StringTokenizer t = new StringTokenizer (nt,
                                                      DEFAULT_SEPARATOR);

             while (t.hasMoreTokens ())
             {

                 String tok = t.nextToken ().trim ();

                 UserProperties.fullScreenHeaderControlButtonIds.add (tok);

             }

         }

         UserProperties.projectViewerHeaderControlButtonIds = FXCollections.observableSet (new LinkedHashSet<> ());

         nt = UserProperties.get (Constants.PROJECT_VIEWER_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME);

         if (nt != null)
         {

             StringTokenizer t = new StringTokenizer (nt,
                                                      DEFAULT_SEPARATOR);

             while (t.hasMoreTokens ())
             {

                 String tok = t.nextToken ().trim ();

                 UserProperties.projectViewerHeaderControlButtonIds.add (tok);

             }

         }

         UserProperties.warmupViewerHeaderControlButtonIds = FXCollections.observableSet (new LinkedHashSet<> ());

         nt = UserProperties.get (Constants.WARMUP_VIEWER_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME);

         if (nt != null)
         {

             StringTokenizer t = new StringTokenizer (nt,
                                                      DEFAULT_SEPARATOR);

             while (t.hasMoreTokens ())
             {

                 String tok = t.nextToken ().trim ();

                 UserProperties.warmupViewerHeaderControlButtonIds.add (tok);

             }

         }

         UserProperties.languageStringsEditorHeaderControlButtonIds = FXCollections.observableSet (new LinkedHashSet<> ());

         nt = UserProperties.get (Constants.LANGUAGE_STRINGS_EDITOR_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME);

         if (nt != null)
         {

             StringTokenizer t = new StringTokenizer (nt,
                                                      DEFAULT_SEPARATOR);

             while (t.hasMoreTokens ())
             {

                 String tok = t.nextToken ().trim ();

                 UserProperties.languageStringsEditorHeaderControlButtonIds.add (tok);

             }

         }

         UserProperties.allProjectsViewerHeaderControlButtonIds = FXCollections.observableSet (new LinkedHashSet<> ());

         nt = UserProperties.get (Constants.ALL_PROJECTS_VIEWER_HEADER_CONTROL_BUTTONS_IDS_PROPERTY_NAME);

         if (nt != null)
         {

             StringTokenizer t = new StringTokenizer (nt,
                                                      DEFAULT_SEPARATOR);

             while (t.hasMoreTokens ())
             {

                 String tok = t.nextToken ().trim ();

                 UserProperties.allProjectsViewerHeaderControlButtonIds.add (tok);

             }

         }

        // TODO Make this configurable
        //UserProperties.uiTextDictionaryProv = new UserDictionaryProvider (Constants.ENGLISH);

    }
/*
    public static UserDictionaryProvider getUITextDictionaryProvider ()
    {

        return UserProperties.uiTextDictionaryProv;

    }
*/

    public static SimpleBooleanProperty keepProjectsWindowWhenProjectOpenedProperty ()
    {

        return UserProperties.keepProjectsWindowWhenProjectOpenedProp;

    }

    public static SimpleObjectProperty<LocalTime> autoNightModeFromProperty ()
    {

        return UserProperties.autoNightModeFromProp;

    }

    public static SimpleObjectProperty<LocalTime> autoNightModeToProperty ()
    {

        return UserProperties.autoNightModeToProp;

    }

    public static LocalTime getAutoNightModeFromTime ()
    {

        return UserProperties.autoNightModeFromProp.getValue ();

    }

    public static LocalTime getAutoNightModeToTime ()
    {

        return UserProperties.autoNightModeToProp.getValue ();

    }

    public static void setAutoNightModeFromTime (LocalTime t)
    {

        UserProperties.autoNightModeFromProp.setValue (t);

    }

    public static void setAutoNightModeToTime (LocalTime t)
    {

        UserProperties.autoNightModeToProp.setValue (t);

    }

    public static SimpleBooleanProperty permanentNightModeEnabledProperty ()
    {

        return UserProperties.permanentNightModeEnabledProp;

    }

    public static SimpleBooleanProperty autoNightModeEnabledProperty ()
    {

        return UserProperties.autoNightModeProp;

    }

    public static void setPermanentlyEnableNightMode (boolean v)
    {

        UserProperties.set (Constants.NIGHT_MODE_ENABLE_PERMENANTLY_PROPERTY_NAME,
                            v);

    }

    public static void setAutoNightModeEnabled (boolean v)
    {

        UserProperties.set (Constants.AUTO_NIGHT_MODE_ENABLED_PROPERTY_NAME,
                            v);

    }

    public static ObservableSet<String> projectViewerHeaderControlButtonIds ()
    {

        return UserProperties.projectViewerHeaderControlButtonIds;

    }

    public static ObservableSet<String> allProjectsViewerHeaderControlButtonIds ()
    {

        return UserProperties.allProjectsViewerHeaderControlButtonIds;

    }

    public static ObservableSet<String> warmupViewerHeaderControlButtonIds ()
    {

        return UserProperties.warmupViewerHeaderControlButtonIds;

    }

    public static ObservableSet<String> fullScreenHeaderControlButtonIds ()
    {

        return UserProperties.fullScreenHeaderControlButtonIds;

    }

    public static ObservableSet<String> languageStringsEditorHeaderControlButtonIds ()
    {

        return UserProperties.languageStringsEditorHeaderControlButtonIds;

    }

    public static void setLanguageStringsEditorHeaderControlButtonIds (Set<String> ids)
    {

        UserProperties.languageStringsEditorHeaderControlButtonIds.clear ();
        UserProperties.languageStringsEditorHeaderControlButtonIds.addAll (ids);

        UserProperties.set (Constants.LANGUAGE_STRINGS_EDITOR_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME,
                            UserProperties.languageStringsEditorHeaderControlButtonIds.stream ()
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    public static void setProjectViewerHeaderControlButtonIds (Set<String> ids)
    {

        UserProperties.projectViewerHeaderControlButtonIds.clear ();
        UserProperties.projectViewerHeaderControlButtonIds.addAll (ids);

        UserProperties.set (Constants.PROJECT_VIEWER_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME,
                            UserProperties.projectViewerHeaderControlButtonIds.stream ()
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    public static void setAllProjectsViewerHeaderControlButtonIds (Set<String> ids)
    {

        UserProperties.allProjectsViewerHeaderControlButtonIds.clear ();
        UserProperties.allProjectsViewerHeaderControlButtonIds.addAll (ids);

        UserProperties.set (Constants.ALL_PROJECTS_VIEWER_HEADER_CONTROL_BUTTONS_IDS_PROPERTY_NAME,
                            UserProperties.allProjectsViewerHeaderControlButtonIds.stream ()
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    public static void setWarmupViewerHeaderControlButtonIds (Set<String> ids)
    {

        UserProperties.warmupViewerHeaderControlButtonIds.clear ();
        UserProperties.warmupViewerHeaderControlButtonIds.addAll (ids);

        UserProperties.set (Constants.WARMUP_VIEWER_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME,
                            UserProperties.warmupViewerHeaderControlButtonIds.stream ()
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    public static void setFullScreenHeaderControlButtonIds (Set<String> ids)
    {

        UserProperties.fullScreenHeaderControlButtonIds.clear ();
        UserProperties.fullScreenHeaderControlButtonIds.addAll (ids);

        UserProperties.set (Constants.FULL_SCREEN_HEADER_CONTROL_BUTTON_IDS_PROPERTY_NAME,
                            UserProperties.fullScreenHeaderControlButtonIds.stream ()
                                .collect (Collectors.joining (DEFAULT_SEPARATOR)));

    }

    public static void setPlaySoundOnKeyStroke (boolean v)
    {

        UserProperties.playSoundOnKeyStrokeProp.setValue (v);

    }

    public static void setKeyStrokeSoundFilePath (Path up)
    {

        Path p = null;

        if (up == null)
        {

            try
            {

                p = Utils.getAsPath (Utils.getResourceUrl (Constants.DEFAULT_KEY_STROKE_SOUND_FILE).toURI ());

            } catch (Exception e) {

                throw new IllegalArgumentException ("Unable to get path for: " + Constants.DEFAULT_KEY_STROKE_SOUND_FILE,
                                                    e);

            }

        } else {

            if (Files.exists (up))
            {

                p = up;

            }

        }

        try
        {

            UserProperties.keyStrokeSoundBytes = Files.readAllBytes (p);
            UserProperties.keyStrokeSoundPathProp.setValue (up);

        } catch (Exception e) {

            Environment.logError ("Unable to set key stroke sound file: " +
                                  p,
                                  e);

            UserProperties.keyStrokeSoundPathProp.setValue (null);

        }

    }

    public static SimpleObjectProperty<Path> keyStrokeSoundPathProperty ()
    {

        return UserProperties.keyStrokeSoundPathProp;

    }

    public static SimpleBooleanProperty playSoundOnKeyStrokeProperty ()
    {

        return UserProperties.playSoundOnKeyStrokeProp;

    }

    public static boolean isPlaySoundOnKeyStroke ()
    {

        return UserProperties.playSoundOnKeyStrokeProp.getValue ();

    }

    public static void playKeyStrokeSound ()
    {

        if (UserProperties.keyStrokeSoundBytes != null)
        {

            Environment.schedule (() ->
            {

                try
                {

                    javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream (new BufferedInputStream (new ByteArrayInputStream (UserProperties.keyStrokeSoundBytes)));
                    javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip ();
                    clip.open (audioInputStream);
                    clip.start ();
                    audioInputStream.close ();

                } catch (Exception e) {

                    e.printStackTrace ();

                }

            },
            -1,
            -1);

        }

    }


    private static double constrainFullScreenXBorderWidth (Float v)
    {

        if (v == null)
        {

            return DEFAULT_FULL_SCREEN_X_BORDER_WIDTH;

        }

        double d = v.doubleValue ();

        if (d > FULL_SCREEN_MAX_X_BORDER_WIDTH)
        {

            d = FULL_SCREEN_MAX_X_BORDER_WIDTH;

        }

        if (d < FULL_SCREEN_MIN_X_BORDER_WIDTH)
        {

            d = FULL_SCREEN_MIN_X_BORDER_WIDTH;

        }

        return d;

    }

    private static double constrainFullScreenYBorderWidth (Float v)
    {

        if (v == null)
        {

            return DEFAULT_FULL_SCREEN_Y_BORDER_WIDTH;

        }

        double d = v.doubleValue ();

        if (d > FULL_SCREEN_MAX_Y_BORDER_WIDTH)
        {

            d = FULL_SCREEN_MAX_Y_BORDER_WIDTH;

        }

        if (d < FULL_SCREEN_MIN_Y_BORDER_WIDTH)
        {

            d = FULL_SCREEN_MIN_Y_BORDER_WIDTH;

        }

        return d;

    }

    public static Object getFullScreenBackground ()
    {

        return UserProperties.fullScreenBackgroundProp.getValue ();

    }

    public static void setFullScreenBackground (Object b)
    {

        UserProperties.fullScreenBackgroundProp.setValue (b);

    }

    public static SimpleObjectProperty<Object> fullScreenBackgroundProperty ()
    {

        return UserProperties.fullScreenBackgroundProp;

    }

    public static double getFullScreenXBorderWidth ()
    {

        return UserProperties.fullScreenXBorderWidthProp.getValue ();

    }

    public static SimpleDoubleProperty fullScreenXBorderWidthProperty ()
    {

        return UserProperties.fullScreenXBorderWidthProp;

    }

    public static double getFullScreenYBorderWidth ()
    {

        return UserProperties.fullScreenYBorderWidthProp.getValue ();

    }

    public static SimpleDoubleProperty fullScreenYBorderWidthProperty ()
    {

        return UserProperties.fullScreenYBorderWidthProp;

    }

    public static double getFullScreenOpacity ()
    {

        return UserProperties.fullScreenOpacityProp.getValue ();

    }

    public static SimpleDoubleProperty fullScreenOpacityProperty ()
    {

        return UserProperties.fullScreenOpacityProp;

    }

    public static void incrementFullScreenXBorderWidth ()
    {

        UserProperties.adjustFullScreenXBorderWidth (FULL_SCREEN_X_BORDER_WIDTH_ADJUST_INCR);

    }

    public static void decrementFullScreenXBorderWidth ()
    {

        UserProperties.adjustFullScreenXBorderWidth (-1 * FULL_SCREEN_X_BORDER_WIDTH_ADJUST_INCR);

    }

    private static void adjustFullScreenXBorderWidth (double incr)
    {

        double v = UserProperties.fullScreenXBorderWidthProp.getValue () + incr;

        if ((v <= FULL_SCREEN_MIN_X_BORDER_WIDTH)
            ||
            (v >= FULL_SCREEN_MAX_X_BORDER_WIDTH)
           )
        {

            return;

        }

        UserProperties.fullScreenXBorderWidthProp.setValue (v);

    }

    public static void incrementFullScreenYBorderWidth ()
    {

        UserProperties.adjustFullScreenYBorderWidth (FULL_SCREEN_Y_BORDER_WIDTH_ADJUST_INCR);

    }

    public static void decrementFullScreenYBorderWidth ()
    {

        UserProperties.adjustFullScreenYBorderWidth (-1 * FULL_SCREEN_Y_BORDER_WIDTH_ADJUST_INCR);

    }

    private static void adjustFullScreenYBorderWidth (double incr)
    {

        double v = UserProperties.fullScreenYBorderWidthProp.getValue () + incr;

        if ((v <= FULL_SCREEN_MIN_Y_BORDER_WIDTH)
            ||
            (v >= FULL_SCREEN_MAX_Y_BORDER_WIDTH)
           )
        {

            return;

        }

        UserProperties.fullScreenYBorderWidthProp.setValue (v);

    }

    public static void setFullScreenOpacity (double v)
    {

        if (v < 0)
        {

            v = 0;

        }

        if (v > 1)
        {

            v = 1;

        }

        UserProperties.fullScreenOpacityProp.setValue (v);

    }

    public static boolean isAutoBackupsEnabled ()
    {

        return UserProperties.autoBackupsEnabledProp.getValue ();

    }

    public static void setAutoBackupsEnabled (boolean v)
    {

        UserProperties.autoBackupsEnabledProp.setValue (v);

    }

    public static int getAutoBackupsTime ()
    {

        return UserProperties.autoBackupsTimeProp.getValue ();

    }

    public static void setAutoBackupsTime (long c)
    {

        UserProperties.autoBackupsTimeProp.setValue ((int) c);

    }

    public static SimpleIntegerProperty autoBackupsTimeProperty ()
    {

        return UserProperties.autoBackupsTimeProp;

    }

    public static int getBackupsToKeepCount ()
    {

        return UserProperties.backupsToKeepCountProp.getValue ();

    }

    public static void setBackupsToKeepCount (int v)
    {

        UserProperties.backupsToKeepCountProp.setValue (v);

    }

    public static SimpleIntegerProperty backupsToKeepCountProperty ()
    {

        return UserProperties.backupsToKeepCountProp;

    }

    public static SimpleFloatProperty uiBaseFontSizeProperty ()
    {

        return UserProperties.uiBaseFontSizeProp;

    }

    public static void setUIBaseFontSize (double v)
    {

        UserProperties.uiBaseFontSizeProp.setValue (v);

    }

    public static float getUIBaseFontSize ()
    {

        float v = UserProperties.uiBaseFontSizeProp.getValue ();

        if (v == 0)
        {

            Label l = new Label ();
            // This is an assumption that may not hold.
            v = (float) l.getFont ().getSize () * (float) (72f/96f);

        }

        return v;

    }

    public static SimpleObjectProperty<Font> uiBaseFontProperty ()
    {

        return UserProperties.uiBaseFontProp;

    }

    public static void setUIBaseFont (Font f)
    {

        UserProperties.uiBaseFontProp.setValue (f);

    }

    public static Font getUIBaseFont ()
    {

        Font f = UserProperties.uiBaseFontProp.getValue ();

        if (f == null)
        {

            f = Font.getDefault ();

        }

        return f;

    }

    public static void setDefaultUILanguageStringsSpellCheckLanguage (String lang)
    {

        UserProperties.set (Constants.UI_LANGUAGE_STRINGS_SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                            lang);

    }

    public static String getDefaultUILanguageStringsSpellCheckLanguage ()
    {

        return UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

    }

    public static void setDefaultSpellCheckLanguage (String lang)
    {

        UserProperties.set (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                            lang);

    }

    public static String getDefaultSpellCheckLanguage ()
    {

        String v = UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        if (("UK English".equals (v))
            ||
            ("UK English".equals (v))
           )
        {

            v = Constants.ENGLISH;

        }

        return v;

    }

    public static void setChapterAutoSaveEnabled (boolean v)
    {

        UserProperties.chapterAutoSaveEnabledProp.setValue (v);

    }

    public static void setChapterAutoSaveTime (int millis)
    {

        UserProperties.chapterAutoSaveTimeProp.setValue (millis);

    }

    public static SimpleIntegerProperty chapterAutoSaveTimeProperty ()
    {

        return UserProperties.chapterAutoSaveTimeProp;

    }

    public static SimpleBooleanProperty chapterAutoSaveEnabledProperty ()
    {

        return UserProperties.chapterAutoSaveEnabledProp;

    }

    public static void saveDefaultProjectProperty (String name,
                                                   String value)
    {

        try
        {

            Environment.saveDefaultProperty (Project.OBJECT_TYPE,
                                              name,
                                              value);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save default " + Project.OBJECT_TYPE + " properties",
                                  e);

            ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                             getUILanguageStringProperty (options,savepropertyerror));
                                      //"Unable to save default project properties");

        }


    }

    public static void saveDefaultProjectProperty (String  name,
                                                   Boolean value)
    {

        try
        {

            Environment.saveDefaultProperty (Project.OBJECT_TYPE,
                                              name,
                                              value);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save default " + Project.OBJECT_TYPE + " properties",
                                  e);

            ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                             getUILanguageStringProperty (options,savepropertyerror));
                                      //"Unable to save default project properties");

        }


    }

    public static void addNoteType (String t)
    {

        UserProperties.noteTypes.add (new SimpleStringProperty (t));

    }

    public static void removeNoteType (String t)
    {

        javafx.beans.property.StringProperty s = null;

        for (javafx.beans.property.StringProperty p : UserProperties.noteTypes)
        {

            if (p.getValue ().equals (t))
            {

                s = p;
                break;

            }

        }

        UserProperties.noteTypes.remove (s);

    }

    public static javafx.beans.property.StringProperty getNoteTypeProperty (String v)
    {

        for (javafx.beans.property.StringProperty p : UserProperties.noteTypes)
        {

            if (p.getValue ().equals (v))
            {

                return p;

            }

        }

        return null;

    }

    public static ObservableSet<javafx.beans.property.StringProperty> getNoteTypes ()
    {

        return UserProperties.noteTypes;

    }

    public static ObservableSet<Color> getUserColors ()
    {

        return UserProperties.userColors;

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

        UserProperties.projectStatusesListener = ev ->
        {

            UserProperties.saveProjectStatuses ();

        };

        // Have to cast here to help out the compiler.
        UserProperties.projectStatusesProp.addListener (UserProperties.projectStatusesListener);

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

        UserProperties.userColors = FXCollections.observableSet (new LinkedHashSet<> ());

        // TODO Use a stream?
        String colors = UserProperties.get (Constants.COLOR_SWATCHES_PROPERTY_NAME);

        StringTokenizer t = new StringTokenizer (colors,
                                                 ",");

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

        UserProperties.userColorsListener = ev ->
        {

            UserProperties.set (Constants.COLOR_SWATCHES_PROPERTY_NAME,
                                UserProperties.userColors.stream ()
                                    .map (c -> UIUtils.colorToHex (c))
                                    .collect (Collectors.joining (",")));

        };

        UserProperties.userColors.addListener (UserProperties.userColorsListener);

    }

    private static void initNoteTypes ()
                                throws Exception
    {

        UserProperties.noteTypes = FXCollections.observableSet (new TreeSet<> ((o1, o2) ->
        {

            return o1.getValue ().compareTo (o2.getValue ());

        }));

        String types = UserProperties.get (Constants.NOTE_TYPES_PROPERTY_NAME);

        if (types == null)
        {

            types = getUILanguageStringProperty (notetypes,defaulttypes).getValue ();

        }

        StringTokenizer t = new StringTokenizer (types,
                                                 "|");

        while (t.hasMoreTokens ())
        {

            String ty = t.nextToken ().trim ();

            UserProperties.noteTypes.add (new SimpleStringProperty (ty));

        }

        UserProperties.noteTypes.addListener ((SetChangeListener<javafx.beans.property.StringProperty>) ev ->
        {

            UserProperties.set (Constants.NOTE_TYPES_PROPERTY_NAME,
                                UserProperties.noteTypes.stream ()
                                    .map (v -> v.getValue ())
                                    .collect (Collectors.joining ("|")));

        });

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
        // Legacy, if starts with <files then assume XML.
        if (bgFiles.startsWith ("<files"))
        {

            Element root = DOM4JUtils.stringAsElement (bgFiles);

            UserProperties.userBGImagePaths.addAll (root.elements ("f").stream ()
                .map (el -> Paths.get (el.getTextTrim ()))
                .filter (p ->
                {

                    return ((Files.exists (p))
                            &&
                            (!Files.isDirectory (p)));

                })
                .collect (Collectors.toList ()));

        } else {

            // Post v3, JSON.
            Object o = JSONDecoder.decode (bgFiles);

            if (o instanceof Collection)
            {

                Collection<?> c = (Collection<?>) o;

                UserProperties.userBGImagePaths.addAll (c.stream ()
                    .map (i -> Paths.get (i.toString ()))
                    .filter (i ->
                    {

                        return ((Files.exists (i))
                                &&
                                (!Files.isDirectory (i)));

                    })
                    .collect (Collectors.toList ()));

            }

        }

    }

    public static void addUserColor (Color c)
    {

        UserProperties.userColors.add (c);

    }

    public static void removeUserColor (Color c)
    {

        UserProperties.userColors.remove (c);

    }

    public static ObservableSet<Color> userColorsProperty ()
    {

        return UserProperties.userColors;

    }

    public static ObservableSet<Path> userBGImagePathsProperty ()
    {

        return UserProperties.userBGImagePaths;

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

    public static URL getDefaultStyleSheetURL ()
    {

        return UserProperties.class.getResource (Constants.DEFAULT_STYLE_SHEET_FILE_NAME);

    }

    public static SimpleObjectProperty<Path> userStyleSheetProperty ()
    {

        return UserProperties.userStyleSheetProp;

    }

    public static void setUserStyleSheet (Path p)
    {

        if (p == null)
        {

            UserProperties.remove (Constants.USER_STYLE_SHEET_FILE_NAME_PROPERTY_NAME);

        } else {

            UserProperties.set (Constants.USER_STYLE_SHEET_FILE_NAME_PROPERTY_NAME,
                                p.toString ());

        }

        UserProperties.userStyleSheetProp.setValue (p);

    }

    public static Path getUserStyleSheet ()
    {

        return UserProperties.userStyleSheetProp.getValue ();

    }

    private static SimpleStringProperty createMappedProperty (String name)
    {

        return UserProperties.createMappedProperty (name,
                                                    null);

    }

    private static SimpleStringProperty createMappedProperty (String name,
                                                              String defaultNameOnNull)
    {

        String v = UserProperties.get (name);

        if (v == null)
        {

            v = UserProperties.get (defaultNameOnNull);

        }

        SimpleStringProperty s = new SimpleStringProperty (v);
        UserProperties.mappedProperties.put (name,
                                             s);

        s.addListener ((pr, oldv, newv) ->
        {

            UserProperties.set (name,
                                newv);

        });

        return s;

    }

    private static SimpleBooleanProperty createMappedBooleanProperty (String name)
    {

        boolean v = UserProperties.getAsBoolean (name);

        SimpleBooleanProperty s = new SimpleBooleanProperty (v);
        UserProperties.mappedBooleanProperties.put (name,
                                                    s);

        return s;

    }

    public static Color getFindHighlightColor ()
    {

        return UserProperties.findHighlightColorProp.getValue ();

    }

    public static Color getSynonymHighlightColor ()
    {

        return UserProperties.synonymHighlightColorProp.getValue ();

    }

    public static Color getProblemFinderBlockHighlightColor ()
    {

        return UserProperties.problemFinderBlockHighlightColorProp.getValue ();

    }

    public static Color getProblemFinderIssueHighlightColor ()
    {

        return UserProperties.problemFinderIssueHighlightColorProp.getValue ();

    }

    public static Color getEditNeededNoteChapterHighlightColor ()
    {

        return UserProperties.editNeededNoteChapterHighlightColorProp.getValue ();

    }

    public static Color getEditorCommentChapterHighlightColor ()
    {

        return UserProperties.editorCommentChapterHighlightColorProp.getValue ();

    }

    public static Color getEditMarkerColor ()
    {

        return UserProperties.editMarkerColorProp.getValue ();

    }

    public static void setEditMarkerColor (Color col)
    {

        UserProperties.editMarkerColorProp.setValue (col);

    }

    public static SimpleObjectProperty<Color> editMarkerColorProperty ()
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
    public static SimpleStringProperty toolbarLocationProperty ()
    {

        return UserProperties.toolbarLocationProp;

    }

    public static void setToolbarLocation (String loc)
    {

        UserProperties.toolbarLocationProp.setValue (loc);

    }

    // TODO Make an enum.
    public static void setTabsLocation (String loc)
    {

        UserProperties.tabsLocationProp.setValue (loc);

    }

    public static boolean isShowEditPositionIconInChapterList ()
    {

        return UserProperties.showEditPositionIconInChapterListProp.getValue ();

    }

    public static void setShowEditPositionIconInChapterList (boolean v)
    {

        UserProperties.set (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                            v);

    }

    public static SimpleBooleanProperty showEditPositionIconInChapterListProperty ()
    {

        return UserProperties.showEditPositionIconInChapterListProp;

    }

    public static boolean isShowNotesInChapterList ()
    {

        return UserProperties.showNotesInChapterListProp.getValue ();

    }

    public static SimpleBooleanProperty showNotesInChapterListProperty ()
    {

        return UserProperties.showNotesInChapterListProp;

    }

    public static SimpleBooleanProperty showEditMarkerInChapterProperty ()
    {

        return UserProperties.showEditMarkerInChapterProp;

    }

    public static boolean isShowEditMarkerInChapter ()
    {

        return UserProperties.showEditMarkerInChapterProp.getValue ();

    }

    public static void setShowEditMarkerInChapter (boolean v)
    {

        UserProperties.set (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME,
                            v);

    }

    public static boolean isShowEditCompleteIconInChapterList ()
    {

        return UserProperties.showEditCompleteIconInChapterListProp.getValue ();

    }

    public static void setShowEditCompleteIconInChapterList (boolean v)
    {

        UserProperties.set (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                            v);

    }

    public static SimpleBooleanProperty showEditCompleteIconInChapterListProperty ()
    {

        return UserProperties.showEditCompleteIconInChapterListProp;

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

        UserProperties.save ();

    }

    public static void set (String  name,
                            boolean value)
    {

        UserProperties.set (name,
                            new BooleanProperty (name,
                                                 value));

        SimpleBooleanProperty p = UserProperties.mappedBooleanProperties.get (name);

        if (p == null)
        {

            p = UserProperties.createMappedBooleanProperty (name);

        }

        if (p != null)
        {

            p.setValue (value);

        }

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

    public static Boolean getAsBoolean (String name,
                                        String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.getAsBoolean (defOnNull);

        }

        return UserProperties.props.getPropertyAsBoolean (name);

    }

    public static Boolean getAsBoolean (String name)
    {

        return UserProperties.props.getPropertyAsBoolean (name);

    }

    public static Integer getAsInt (String name,
                                    String defOnNull)
    {

        AbstractProperty a = UserProperties.props.getPropertyObj (name);

        if (a == null)
        {

            return UserProperties.getAsInt (defOnNull);

        }

        return UserProperties.props.getPropertyAsInt (name);

    }

    public static Integer getAsInt (String name)
    {

        return UserProperties.props.getPropertyAsInt (name);

    }

    public static Path getAsFile (String name)
    {

        File f = UserProperties.props.getPropertyAsFile (name);

        if (f == null)
        {

            return null;

        }

        return f.toPath ();

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
