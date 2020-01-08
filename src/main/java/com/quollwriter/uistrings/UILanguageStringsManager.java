package com.quollwriter.uistrings;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.util.concurrent.*;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.binding.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.LanguageStringsEditor;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class UILanguageStringsManager
{

    private static Function<List<String>, String> userStringProvider = null;
    private static UILanguageStrings uiLanguageStrings = null;
    private static UILanguageStrings defaultUILanguageStrings = null;
    private static StringProperty uilangProp = new SimpleStringProperty (uiLanguageStrings.ENGLISH_ID);

    private UILanguageStringsManager ()
    {

    }

    public static void init ()
               throws Exception
    {

        // We add a listener to the uilangProp to force recomputation of the value when it changes.
        UILanguageStringsManager.uilangProp.addListener ((pr, oldv, newv) ->
        {

            // Don't care.

        });

        UILanguageStringsManager.defaultUILanguageStrings = new UILanguageStrings (Utils.getResourceFileAsString (Constants.DEFAULT_UI_LANGUAGE_STRINGS_FILE));

        UILanguageStringsManager.uiLanguageStrings = UILanguageStringsManager.defaultUILanguageStrings;

        // Load the user default, if appropriate.
        final String uilangid = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

        if (uilangid != null)
        {

            if (!UILanguageStrings.isEnglish (uilangid))
            {

                UILanguageStrings ls = UILanguageStringsManager.getUILanguageStrings (uilangid);

                if ((ls == null)
                    ||
                    // Have we updated QW and need to get newer versions?
                    ((ls != null)
                     &&
                     (ls.getQuollWriterVersion ().isNewer (Environment.getQuollWriterVersion ()))
                    )
                   )
                {

                    // Something has gone wrong, try and download again.
                    UILanguageStringsManager.downloadUILanguageFile (uilangid,
                                                 // On complete.
                                                 () ->
                                                 {

                                                    try
                                                    {

                                                        UILanguageStringsManager.setUILanguage (uilangid);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                              e);

                                                        ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                         UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,errors,download));
                                                                                         //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                    }

                                                    QuollPopup.messageBuilder ()
                                                        .withViewer (Environment.getFocusedViewer ())
                                                        .title (uilanguage,set,downloading,redownload,confirmpopup,title)
                                                        .message (uilanguage,set,downloading,redownload,confirmpopup,text)
                                                        .build ();

                                                 },
                                                 // On error.
                                                 () -> {

                                                    ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                     UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,redownload,actionerror));
                                                                                     //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                 });

                } else {

                    UILanguageStringsManager.setUILanguage (uilangid);

                    if (!ls.isUser ())
                    {

                        // See if there is an update to the strings.
                        UILanguageStringsManager.downloadUILanguageFile (uilangid,
                                                     // On complete
                                                     () ->
                                                     {

                                                        try
                                                        {

                                                            UILanguageStringsManager.setUILanguage (uilangid);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                             UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,update,actionerror));
                                                                                             //"Warning!  Quoll Writer has been unable to update the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                        }

                                                        QuollPopup.messageBuilder ()
                                                            .withViewer (Environment.getFocusedViewer ())
                                                            .title (uilanguage,set,downloading,update,confirmpopup,title)
                                                            .message (uilanguage,set,downloading,update,confirmpopup,text)
                                                            .build ();

                                                    },
                                                    // On error.
                                                    null);

                    }

                }

            }

        }

    }

    public static StringProperty uilangProperty ()
    {

        return UILanguageStringsManager.uilangProp;

    }

    public static void downloadUILanguageFile (final String   id,
                                               final Runnable onComplete,
                                               final Runnable onError)
    {

        Environment.schedule (() ->
        {

            String lastMod = "";

            UILanguageStrings ls = null;

            try
            {

                ls = UILanguageStringsManager.getUILanguageStrings (id);

            } catch (Exception e) {

                Environment.logError ("Unable to get language strings: " + id,
                                      e);

                UIUtils.runLater (onError);

                return;

            }

            if (ls != null)
            {

                Date d = ls.getLastModified ();

                if (d == null)
                {

                    d = ls.getDateCreated ();

                }

                lastMod = d.getTime () + "";

            }

            String url = UserProperties.get (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

            url = StringUtils.replaceString (url,
                                             Constants.VERSION_TAG,
                                             Environment.getQuollWriterVersion ().toString ());

            url = StringUtils.replaceString (url,
                                             Constants.ID_TAG,
                                             id);

            url = StringUtils.replaceString (url,
                                             Constants.LAST_MOD_TAG,
                                             lastMod);

            try
            {

                String data = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

                if (data.startsWith (Constants.JSON_RETURN_PREFIX))
                {

                    data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

                }

                if (data.trim ().length () == 0)
                {

                    Environment.logError ("No language strings data available for: " + id + ", " + Environment.getQuollWriterVersion ());

                    UIUtils.runLater (onError);

                    return;

                }

                // Will be a collection.
                Collection col = null;

                try
                {

                    col = (Collection) JSONDecoder.decode (data);

                } catch (Exception e) {

                    Environment.logError ("Unable to decode language strings data for id: " + id + ", " + Environment.getQuollWriterVersion (),
                                          e);

                    UIUtils.runLater (onError);

                    return;

                }

                Iterator iter = col.iterator ();

                int updated = 0;

                while (iter.hasNext ())
                {

                    Map m = (Map) iter.next ();

                    String nid = (String) m.get (":id");

                    if (id == null)
                    {

                        throw new GeneralException ("No id found.");

                    }

                    updated++;

                    Path f = UILanguageStringsManager.getUILanguageStringsFilePath (nid);

                    Files.write (f, JSONEncoder.encode (m).getBytes (StandardCharsets.UTF_8));

                }

                UIUtils.runLater (onComplete);

            } catch (Exception e) {

                Environment.logError ("Unable to get user interface files for: " + id + ", " + Environment.getQuollWriterVersion (),
                                      e);

                UIUtils.runLater (onError);

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

    }

    public static void setUILanguage (String id)
                               throws Exception
    {

        UILanguageStrings ls = null;

        ls = UILanguageStringsManager.getUILanguageStrings (id);

        if (ls == null)
        {

            throw new GeneralException ("No language strings found for id: " +
                                        id);

        }

        UILanguageStringsManager.uiLanguageStrings = new UILanguageStrings (ls)
        {

            @Override
            public String getId ()
            {

                return this.getDerivedFrom ().getId ();

            }

            @Override
            public boolean isEnglish ()
            {

                return this.getDerivedFrom ().isEnglish ();

            }

            @Override
            public String getString (List<String> idparts)
            {

                String s = null;

                if (UILanguageStringsManager.userStringProvider != null)
                {

                    s = UILanguageStringsManager.userStringProvider.apply (idparts);

                }

                if (s == null)
                {

                    s = super.getString (idparts);

                }

                return s;

            }

        };

        UserProperties.set (Constants.USER_UI_LANGUAGE_PROPERTY_NAME, id);

        UILanguageStringsManager.uilangProp.setValue (id);

    }

    public static Set<UILanguageStrings> getAllUILanguageStrings ()
                                                           throws Exception
    {

        return UILanguageStringsManager.getAllUILanguageStrings (null);

    }

    public static Set<UILanguageStrings> getAllUILanguageStrings (Version ver)
                                                           throws Exception
    {

        Path d = UILanguageStringsManager.getUILanguageStringsDirPath ();
// TODO wrap Files.walk in try-close.
        // Cycle down all subdirs.
        try (Stream<Path> files = Files.walk (d, 1))
        {

            // Map the file to a UILanguageStrings instance.
            return files.filter (f -> Files.isRegularFile (f))
                .map (f ->
                {

                    try
                    {

                        UILanguageStrings ls = new UILanguageStrings (f.toFile ());

                        if (ver != null)
                        {

                            if (!ls.getQuollWriterVersion ().equals (ver))
                            {

                                ls = null;

                            }

                        }

                        return ls;

                    } catch (Exception e) {

                        throw new RuntimeException ("Unable to create ui language strings from: " +
                                                    f,
                                                    e);

                    }

                })
                // Remove nulls.
                .filter (f -> f != null)
                // Collect to a set.
                .collect (Collectors.toSet ());

        }

/*
        File[] files = this.getUILanguageStringsDir ().listFiles ();

        if (files == null)
        {

            return ret;

        }

        for (int i = 0; i < files.length; i++)
        {

            File f = files[i];

            if (f.isFile ())
            {

                try
                {

                    UILanguageStrings ls = new UILanguageStrings (f);

                    if (ver != null)
                    {

                        if (!ls.getQuollWriterVersion ().equals (ver))
                        {

                            continue;

                        }

                    }

                    ret.add (ls);

                } catch (Exception e) {

                    Environment.logError ("Unable to create strings from: " + f,
                                          e);

                    // Delete the file.
                    f.delete ();

                }

            }

        }

        return ret;
*/
    }

    private static Path getUILanguageStringsDirPath ()
                                       throws IOException
    {

        Path d = Environment.getUserPath (Constants.UI_LANGUAGES_DIR_NAME);

        Files.createDirectories (d);

        return d;

    }

    public static Path getUILanguageStringsFilePath (String id)
                                              throws IOException
    {

        return UILanguageStringsManager.getUILanguageStringsDirPath ().resolve (id);

    }

    public static Path getUserUILanguageStringsFilePath (Version qwVersion,
                                                         String  id)
                                                  throws IOException
    {

        if (id.equals (UILanguageStrings.ENGLISH_ID))
        {

            id = id.substring (1);

        }

        return UILanguageStringsManager.getUserUILanguageStringsDirPath (qwVersion).resolve (id);

    }

    private static Path getUserUILanguageStringsDirPath (Version v)
                                                  throws IOException
    {

        Path p = Environment.getUserPath (Constants.USER_UI_LANGUAGES_DIR_NAME).resolve (v.toString ());

        Files.createDirectories (p);

        return p;

    }

    public static Path getUserUILanguageStringsFilePath (UILanguageStrings ls)
                                                  throws IOException
    {

        return UILanguageStringsManager.getUserUILanguageStringsFilePath (ls.getQuollWriterVersion (),
                                                                          ls.getId ());

    }

    public static UILanguageStrings getUILanguageStrings (String  id,
                                                          Version ver)
                                                   throws Exception
    {

        if (ver == null)
        {

            ver = Environment.getQuollWriterVersion ();

        }

        if (id.startsWith ("user-"))
        {

            id = id.substring ("user-".length ());

            return UILanguageStringsManager.getUserUILanguageStrings (ver,
                                                                      id);

        }

        if (id.equals (UILanguageStrings.ENGLISH_ID))
        {

            return UILanguageStringsManager.getDefaultUILanguageStrings ();

        }

        Path f = UILanguageStringsManager.getUILanguageStringsFilePath (id);

        if (Files.notExists (f))
        {

            return null;

        }

        String data = new String (Files.readAllBytes (f),
                                  StandardCharsets.UTF_8);

        UILanguageStrings s = new UILanguageStrings (data);

        return s;

    }

    public static UILanguageStrings getUILanguageStrings (String id)
                                                   throws Exception
    {

        return UILanguageStringsManager.getUILanguageStrings (id,
                                                              Environment.getQuollWriterVersion ());

    }

    public static Set<UILanguageStrings> getAllUserUILanguageStrings (Version qwVer)
                                                               throws Exception
    {

        Path d = Environment.getUserPath (Constants.USER_UI_LANGUAGES_DIR_NAME).resolve (qwVer.toString ());

        if (Files.notExists (d))
        {

            return new LinkedHashSet<> ();

        }

        // Cycle down all subdirs.
        try (Stream<Path> stream = Files.walk (d, 1))
        {

            return stream.filter (f -> Files.isRegularFile (f))
            // Map the file to a UILanguageStrings instance.
            .map (f ->
            {

                // Grr...
                try
                {

                    UILanguageStrings ls = new UILanguageStrings (f.toFile ());

                    if (ls.isEnglish ())
                    {

                        ls = null;

                    }

                    return ls;

                } catch (Exception e) {

                    throw new RuntimeException ("Unable to create ui language strings from: " +
                                                f,
                                                e);

                }

            })
            // Remove nulls.
            .filter (f ->
            {

                return f != null;

            })
            // Collect to a set.
            .collect (Collectors.toSet ());

        }

    }

    public static Set<UILanguageStrings> getAllUserUILanguageStrings ()
                                                        throws Exception
    {

        Path d = Environment.getUserPath (Constants.USER_UI_LANGUAGES_DIR_NAME);

        // Cycle down all subdirs.
        try (Stream<Path> stream = Files.walk (d))
        {

            return stream.filter (f -> Files.isRegularFile (f))
                // Map the file to a UILanguageStrings instance.
                .map (f ->
                {

                    // Grr...
                    try
                    {

                        UILanguageStrings ls = new UILanguageStrings (f.toFile ());

                        if (ls.isEnglish ())
                        {

                            ls = null;

                        }

                        return ls;

                    } catch (Exception e) {

                        throw new RuntimeException ("Unable to create ui language strings from: " +
                                                    f,
                                                    e);

                    }

                })
                // Remove nulls.
                .filter (f ->
                {

                    return f != null;

                })
                // Collect to a set.
                .collect (Collectors.toSet ());

        }

    }

    private static void deleteUserUILanguageStrings (final UILanguageStrings ls)
    {

        // TODO Change to use a runnable.
        java.awt.event.ActionListener remFile = new java.awt.event.ActionListener ()
        {

            @Override
            public void actionPerformed (java.awt.event.ActionEvent ev)
            {

                try
                {

                    Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (ls.getQuollWriterVersion (),
                                                                                        ls.getId ());

                    Files.deleteIfExists (f);

                } catch (Exception e) {

                    Environment.logError (String.format ("Unable to delete user ui language strings: %s/%s",
                                                         ls.getQuollWriterVersion (),
                                                         ls.getId ()),
                                          e);

                }

            }

        };

        // TODO Change to Environment.doForOpenViewers(LanguageStringsEditor.class) ->

/*
TODO
        for (AbstractViewer v : Environment.openViewersProperty ())
        {

            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if ((lse.getUserLanguageStrings ().getId ().equals (ls.getId ()))
                    &&
                    (lse.getUserLanguageStrings ().getQuollWriterVersion ().equals (ls.getQuollWriterVersion ()))
                   )
                {

                    lse.close (false,
                               remFile);

                    return;

                }

            }

        }
*/
        remFile.actionPerformed (new java.awt.event.ActionEvent (ls, 0, "do"));

        if (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME).equals ("user-" + ls.getId ()))
        {

            try
            {

                // Need to set the language back to English.
                UILanguageStringsManager.setUILanguage (UILanguageStrings.ENGLISH_ID);

            } catch (Exception e) {

                Environment.logError ("Unable to set UI strings.",
                                      e);

                // TODO Check this...
                ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                 new SimpleStringProperty ("Unable to reset user interface language to " + Constants.ENGLISH));

                return;

            }

            // TODO Check this...
            QuollPopup.messageBuilder ()
                .withViewer (Environment.getFocusedViewer ())
                .title (new SimpleStringProperty ("Restart recommended"))
                .message (new SimpleStringProperty ("The user interface language has been reset to " + Constants.ENGLISH + ", a restart is recommended."))
                .build ();

        }

    }

    public static void deleteUserUILanguageStrings (UILanguageStrings ls,
                                                    boolean           allVersions)
                                             throws Exception
    {

        if (!ls.isUser ())
        {

            throw new IllegalArgumentException ("Can only delete user language strings.");

        }

        if (allVersions)
        {

            Set<UILanguageStrings> allLs = UILanguageStringsManager.getAllUserUILanguageStrings ();

            for (UILanguageStrings _ls : allLs)
            {

                if (_ls.getId ().equals (ls.getId ()))
                {

                    UILanguageStringsManager.deleteUserUILanguageStrings (_ls);

                }

            }

        } else {

            UILanguageStringsManager.deleteUserUILanguageStrings (ls);

        }

    }

    public static UILanguageStrings getUserUIEnglishLanguageStrings (Version v)
                                                              throws Exception
    {

        // If the version is the same as the QW version the user is running then
        if (v.equals (Environment.getQuollWriterVersion ()))
        {

            UILanguageStrings def = UILanguageStringsManager.getDefaultUILanguageStrings ();

            UILanguageStringsManager.saveUserUILanguageStrings (def);

            return def;

            //return Environment.getUserUIEnglishLanguageStrings (v);

        }

        // See if there is a user strings file.
        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (v,
                                                                            UILanguageStrings.ENGLISH_ID);

        if (Files.exists (f))
        {

            return new UILanguageStrings (f.toFile ());

        }

        return null;

    }

    public static void saveUserUILanguageStrings (UILanguageStrings ls)
                                           throws Exception
    {

        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (ls);

        Files.createDirectories (f.getParent ());

        String json = JSONEncoder.encode (ls.getAsJSON ());

        Files.write (f,
                     json.getBytes (StandardCharsets.UTF_8));

    }

    public static UILanguageStrings getUserUILanguageStrings (Version v,
                                                              String  id)
                                                       throws Exception
    {

        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (v,
                                                                            id);

        if (Files.exists (f))
        {

            UILanguageStrings ls = new UILanguageStrings (f.toFile ());
            ls.setUser (true);

            return ls;

        }

        return null;

    }

    public static UILanguageStrings getDefaultUILanguageStrings ()
    {

        return UILanguageStringsManager.defaultUILanguageStrings;

    }

    public static UILanguageStrings getCurrentUILanguageStrings ()
    {

        return UILanguageStringsManager.uiLanguageStrings;

    }

    public static void setUserStringProvider (Function<List<String>, String> userStringProvider)
    {

        UILanguageStringsManager.userStringProvider = userStringProvider;

    }

    public static String getUIString (String... ids)
    {

        return UILanguageStringsManager.getUIString (Arrays.asList (ids));

    }

    public static String getUIString (List<String> prefix,
                                      String...    ids)
    {

        List<String> _ids = new ArrayList (prefix);

        for (String s : ids)
        {

            _ids.add (s);

        }

        String s = null;

        if (s == null)
        {

            s = UILanguageStringsManager.uiLanguageStrings.getString (_ids);

        }

        if (s == null)
        {

            s = BaseStrings.toId (_ids);

        }

        return s;

    }
/*
    public static StringProperty getUILanguageStringProperty (List<String> ids)
    {

        return UILanguageStringsManager.getUILanguageStringProperty (ids,
                                                                     null);

    }
*/

    public static StringBinding createStringBinding (Callable<String> func,
                                                     Observable...    deps)
    {

        Set<Observable> _deps = new LinkedHashSet<> ();
        _deps.add ((javafx.beans.Observable) UILanguageStringsManager.uilangProp);
        _deps.add (Environment.objectTypeNameChangedProperty ());
        _deps.addAll (Arrays.asList (deps));

        return Bindings.createStringBinding (func,
                                             _deps.toArray (new Observable[0]));

    }

    public static StringProperty createStringPropertyWithBinding (Callable<String> func,
                                                                  Observable...    deps)
    {

        StringProperty p = new SimpleStringProperty ();
        p.bind (UILanguageStringsManager.createStringBinding (func,
                                                              deps));
        return p;

    }

    /**
     * Creates a string property that is bound to the uilang property.  A binding function is created that uses the <b>ids</b> parm to get
     * a ui language string, the <b>reps</b> parm are then used as replacement values in a call to String.format
     * on the ui language string, i.e.
     *
     * String.format (getUIString (ids), reps)
     *
     * If a rep is a StringProperty object then getValue is called on it to get the replacement value.  Otherwise .toString is called on the
     * rep value.  Nulls can be passed in the list of reps, they are ignored if present.
     *
     * @param ids The ids for the ui string.
     * @param reps The replacements to perform on the ui string identified by <b>ids</b>.
     * @return The string property.
     */
    public static StringProperty getUILanguageStringProperty (List<String> ids,
                                                              Object...    reps)
    {

        List<Observable> listen = new ArrayList<> ();
        listen.add (UILanguageStringsManager.uilangProp);
        listen.add (Environment.objectTypeNameChangedProperty ());

        if (reps != null)
        {

            for (Object o : reps)
            {

                if (o == null)
                {

                    continue;

                }

                if (o instanceof Observable)
                {

                    listen.add ((Observable) o);
                    continue;

                }

                if (o instanceof StringProperty)
                {

                    //_reps.add (((StringProperty) o).getValue ());
                    listen.add ((StringProperty) o);

                    continue;

                }

                if (o instanceof LongProperty)
                {

                    LongProperty p = (LongProperty) o;

                    //_reps.add (Environment.formatNumber (p.longValue ()));
                    listen.add (p);

                    continue;

                }

                if (o instanceof IntegerProperty)
                {

                    IntegerProperty p = (IntegerProperty) o;

                    //_reps.add (Environment.formatNumber (p.intValue ()));
                    listen.add (p);

                    continue;

                }

                if (o instanceof FloatProperty)
                {

                    FloatProperty p = (FloatProperty) o;

                    //_reps.add (Environment.formatNumber (p.floatValue ()));
                    listen.add (p);

                    continue;

                }

                if (o instanceof DoubleProperty)
                {

                    DoubleProperty p = (DoubleProperty) o;

                    //_reps.add (Environment.formatNumber (p.doubleValue ()));
                    listen.add (p);

                    continue;

                }

            }

        }

        SimpleStringProperty prop =  new SimpleStringProperty ();
        prop.bind (Bindings.createStringBinding (() ->
        {

            List<String> _reps = new ArrayList<> ();

            if (reps != null)
            {

                for (Object o : reps)
                {

                    if (o == null)
                    {

                        continue;

                    }

                    if (o instanceof Integer)
                    {

                        _reps.add (Environment.formatNumber ((Integer) o));
                        continue;

                    }

                    if (o instanceof Double)
                    {

                        _reps.add (Environment.formatNumber ((Double) o));
                        continue;

                    }

                    if (o instanceof Float)
                    {

                        _reps.add (Environment.formatNumber ((Float) o));
                        continue;

                    }

                    if (o instanceof StringProperty)
                    {

                        _reps.add (((StringProperty) o).getValue ());

                        continue;

                    }

                    if (o instanceof LongProperty)
                    {

                        LongProperty p = (LongProperty) o;

                        _reps.add (Environment.formatNumber (p.longValue ()));

                        continue;

                    }

                    if (o instanceof IntegerProperty)
                    {

                        IntegerProperty p = (IntegerProperty) o;

                        _reps.add (Environment.formatNumber (p.intValue ()));

                        continue;

                    }

                    if (o instanceof FloatProperty)
                    {

                        FloatProperty p = (FloatProperty) o;

                        _reps.add (Environment.formatNumber (p.floatValue ()));

                        continue;

                    }

                    if (o instanceof DoubleProperty)
                    {

                        DoubleProperty p = (DoubleProperty) o;

                        _reps.add (Environment.formatNumber (p.doubleValue ()));

                        continue;

                    }

                    _reps.add (o.toString ());

                }

            }

            return String.format (UILanguageStringsManager.getUIString (ids),
                                  _reps.toArray ());
        },
        listen.toArray (new Property[] {})));

        return prop;

    }

    public static StringProperty getUILanguageStringProperty (String... prefix)
    {

        return getUILanguageStringProperty (Arrays.asList (prefix));

    }
/*
    public static StringProperty getUILanguageStringProperty (List<String> prefix,
                                                              String...    ids)
    {

        checkInstance ();

        List<String> _ids = new ArrayList<> ();

        if (prefix != null)
        {

            _ids.addAll (prefix);

        }

        if (ids != null)
        {

            _ids.addAll (Arrays.asList (ids));

        }

        return getUILanguageStringProperty (_ids);

    }
*/
/*
    public static Binding<String> getUILanguageBinding (List<String> ids)
    {

        return Bindings.createStringBinding (() -> UILanguageStringsManager.getUIString (ids), UILanguageStringsManager.uilangProp, Environment.objectTypeNameChangedProperty ());

    }

    public static Binding<String> getUILanguageBinding (String... ids)
    {

        return Bindings.createStringBinding (() -> UILanguageStringsManager.getUIString (ids), UILanguageStringsManager.uilangProp, Environment.objectTypeNameChangedProperty ());

    }
*/
    public static boolean isLanguageEnglish (String language)
    {

        if (language == null)
        {

            return false;

        }

        return Constants.ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.BRITISH_ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.US_ENGLISH.equalsIgnoreCase (language);

    }

    public static LanguageStringsEditor editUILanguageStrings (UILanguageStrings userStrings,
                                                               Version           baseQWVersion)
    {

        LanguageStringsEditor lse = UILanguageStringsManager.getUILanguageStringsEditor (userStrings);

        if (lse != null)
        {

            lse.toFront ();

            return lse;

        }

         try
         {

             LanguageStringsEditor _ls = new LanguageStringsEditor (userStrings,
                                                                    baseQWVersion);
             _ls.init ();

             return _ls;

         } catch (Exception e) {

             Environment.logError ("Unable to create language strings editor",
                                   e);

             ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                              getUILanguageStringProperty (uilanguage,edit,actionerror));

            return null;

         }

    }

    public static LanguageStringsEditor getUILanguageStringsEditor (UILanguageStrings ls)
    {

        for (AbstractViewer v : Environment.getOpenViewers ())
        {
/*
TODO
            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if (lse.getUserLanguageStrings ().equals (ls))
                {

                    return lse;

                }

            }
*/
        }

        return null;

    }

}
