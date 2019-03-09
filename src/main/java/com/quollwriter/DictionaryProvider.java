package com.quollwriter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

import com.gentlyweb.utils.*;

import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.events.DictionaryChangedEvent;
import com.quollwriter.ui.events.DictionaryChangedListener;
import com.quollwriter.uistrings.UILanguageStringsManager;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.SpellCheckConfiguration;
import com.softcorporation.suggester.Suggestion;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class DictionaryProvider
{

    private List                           listeners = new ArrayList ();
    private QWSpellDictionaryHashMap       projDict = null;
    private SpellChecker                   checker = null;
    private com.swabunga.spell.event.SpellChecker projectSpellChecker = null;

    // TODO Use a path.
    private static File                           userDictFile = null;
    private static QWSpellDictionaryHashMap       userDict = null;
    private static com.swabunga.spell.event.SpellChecker userSpellChecker = null;
    private String language = null;

    public DictionaryProvider (String       lang,
                               List<String> projWords)
                               throws       Exception
    {

        this.language = lang;

        Path dictFile = DictionaryProvider.getDictionaryFilePath (lang);

        if (Files.notExists (dictFile))
        {

            throw new GeneralException ("Unable to find dictionary file: " +
                                        dictFile);

        }

        BasicDictionary dict = new BasicDictionary ("file://" + dictFile.toFile ().getPath ());

        SpellCheckConfiguration config = new SpellCheckConfiguration ("/com/softcorporation/suggester/spellCheck.config");

        final BasicSuggester suggester = new BasicSuggester (config);
        suggester.attach (dict);

        final DictionaryProvider _this = this;

        this.checker = new SpellChecker ()
        {

            public synchronized boolean isCorrect (Word word)
            {

                if (word.isPunctuation ())
                {

                    return true;

                }

                String w = word.getText ();

                // See if the word is a number.
                try
                {

                    Double.parseDouble (w);

                    return true;

                } catch (Exception e) {

                    // Not a number.

                }

                if (this.isIgnored (word))
                {

                    return true;

                }

                try
                {

                    if (_this.projectSpellChecker.isCorrect (w))
                    {

                        return true;

                    }

                    if (DictionaryProvider.userSpellChecker.isCorrect (w))
                    {

                        return true;

                    }

                    if (suggester.hasExactWord (w))
                    {

                        return true;

                    }

                    int result = suggester.hasWord (w);
                    if (result == Constants.RESULT_ID_MATCH ||
                        result == Constants.RESULT_ID_MATCH_EXACT)
                    {
                      return true;
                    }

                }catch (Exception e) {

                    Environment.logError ("Unable to check word: " +
                                          word,
                                          e);

                }

                return false;

            }

            public synchronized boolean isIgnored (Word word)
            {

                return false;

            }

            public synchronized List<String> getSuggestions (Word word)
            {

                List<String> ret = new ArrayList ();

                if (word == null)
                {

                    return null;

                }

                if (this.isCorrect (word))
                {

                    return null;

                }

                String wt = word.getText ();

                List suggestions = null;

                List jsuggestions = _this.projectSpellChecker.getSuggestions (wt,
                                                                              1);

                if (jsuggestions != null)
                {

                    for (int i = 0; i < jsuggestions.size (); i++)
                    {

                        ret.add (((com.swabunga.spell.engine.Word) jsuggestions.get (i)).getWord ());

                    }

                }

                jsuggestions = DictionaryProvider.userSpellChecker.getSuggestions (wt,
                                                                                   1);

                if (jsuggestions != null)
                {

                    for (int i = 0; i < jsuggestions.size (); i++)
                    {

                        ret.add (((com.swabunga.spell.engine.Word) jsuggestions.get (i)).getWord ());

                    }

                }

                try
                {

                    suggestions = suggester.getSuggestions (wt, 20);

                } catch (Exception e) {

                    e.printStackTrace ();

                }

                if (suggestions != null)
                {

                    for (int i = 0; i < suggestions.size (); i++)
                    {

                        Suggestion s = (Suggestion) suggestions.get (i);

                        ret.add (s.word);

                    }

                }

                if (Character.isUpperCase (wt.charAt (0)))
                {

                    for (int i = 0; i < ret.size (); i++)
                    {

                        String w = ret.get (i);

                        ret.set (i,
                                 Character.toUpperCase (w.charAt (0)) + w.substring (1));

                    }

                }

                return ret;

            }

        };

        this.projectSpellChecker = new com.swabunga.spell.event.SpellChecker ();

        if (projWords != null)
        {

            StringBuilder b = new StringBuilder ();

            for (String i : projWords)
            {

                b.append (i);
                b.append ('\n');

            }

            this.projDict = new QWSpellDictionaryHashMap (new StringReader (b.toString ()));

            this.projectSpellChecker.addDictionary (this.projDict);

            //this.dicts.add (this.projDict);

        }

        if (DictionaryProvider.userDict == null)
        {

            Path userDictFile = DictionaryProvider.getUserDictionaryFilePath ();

            if (Files.notExists (userDictFile))
            {

                Files.createFile (userDictFile);

            }

            DictionaryProvider.userDict = new QWSpellDictionaryHashMap (userDictFile.toFile ());

            DictionaryProvider.userDictFile = userDictFile.toFile ();

            DictionaryProvider.userSpellChecker = new com.swabunga.spell.event.SpellChecker ();

            DictionaryProvider.userSpellChecker.setUserDictionary (DictionaryProvider.userDict);

        }

    }

    public String getLanguage ()
    {

        return this.language;

    }

    public static boolean isLanguageInstalled (String lang)
    {

        Path f = DictionaryProvider.getDictionaryFilePath (lang);

        if ((f != null)
            &&
            (Files.exists (f))
           )
        {

            return true;

        }

        return false;

    }

    /**
     * Checks to see if the directory is an indexed dictionary directory which means:
     *   - It contains a db directory
     *   - It contains a words directory
     *   - It contains a contents file
     *
     * @param dir The directory to check.
     * @return If the checks pass.
     */
    private boolean isIndexedDictionaryDirectory (File dir)
    {

        if (!dir.exists ())
        {

            return false;

        }

        File dbDir = new File (dir, "db");

        if ((!dbDir.exists ())
            ||
            (dbDir.isFile ())
           )
        {

            return false;

        }

        File wordsDir = new File (dir, "words");

        if ((!wordsDir.exists ())
            ||
            (wordsDir.isFile ())
           )
        {

            return false;

        }

        return true;

    }

    public void addDictionaryChangedListener (DictionaryChangedListener l)
    {

        if (this.listeners.contains (l))
        {

            return;

        }

        this.listeners.add (l);

    }

    public void removeDictionaryChangedListener (DictionaryChangedListener l)
    {

        this.listeners.remove (l);

    }

    protected void fireDictionaryEvent (DictionaryChangedEvent ev)
    {

        for (int i = 0; i < this.listeners.size (); i++)
        {

            DictionaryChangedListener dcl = (DictionaryChangedListener) this.listeners.get (i);

            dcl.dictionaryChanged (ev);

        }

    }

    public SpellChecker getSpellChecker ()
    {

        return this.checker;

    }

    public static void addUserWord (String word)
    {

        if (!DictionaryProvider.userDict.isCorrect (word))
        {

            DictionaryProvider.userDict.addWord (word);
/*
            this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                  DictionaryChangedEvent.WORD_ADDED,
                                                                  word));
*/
        }

    }

    public static void removeUserWord (String word)
    {

        if (DictionaryProvider.userDict != null)
        {

            DictionaryProvider.userDict.removeWord (word);

            try
            {

                DictionaryProvider.userDict.saveDictionaryToFile (DictionaryProvider.userDictFile);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save user dictionary file",
                                      e);

            }

            /*
            this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                  DictionaryChangedEvent.WORD_REMOVED,
                                                                  word));
*/
        }

    }

    public void removeWord (String word,
                            String type)
    {

        if (type.equals ("project"))
        {

            if (this.projDict != null)
            {

                this.projDict.removeWord (word);

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_REMOVED,
                                                                      word));

            }

        }

        if (type.equals ("user"))
        {

            if (DictionaryProvider.userDict != null)
            {

                DictionaryProvider.userDict.removeWord (word);

                try
                {

                    DictionaryProvider.userDict.saveDictionaryToFile (DictionaryProvider.userDictFile);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to save user dictionary file",
                                          e);

                }

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_REMOVED,
                                                                      word));

            }

        }

    }

    public void addWord (String word,
                         String type)
    {

        if (type.equals ("project"))
        {

            if (this.projDict == null)
            {

                try
                {

                    this.projDict = new QWSpellDictionaryHashMap (new StringReader (""));

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create project dictionary for word: " +
                                          word,
                                          e);

                    return;

                }

            }

            this.projDict.addWord (word);

            this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                  DictionaryChangedEvent.WORD_ADDED,
                                                                  word));

        }

        if (type.equals ("user"))
        {

            if (!DictionaryProvider.userDict.isCorrect (word))
            {

                DictionaryProvider.userDict.addWord (word);

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_ADDED,
                                                                      word));

            }

        }

    }

    public static void downloadDictionaryFiles (String               lang,
                                                final AbstractViewer parent,
                                                final Runnable       onComplete)
    {

        if (UILanguageStrings.isEnglish (lang))
        {

            lang = com.quollwriter.Constants.ENGLISH;

        }

        final String langOrig = lang;
        final String language = lang;

        String fileLang = lang;

        // Legacy, if the user doesn't have the language file but DOES have a thesaurus then just
        // download the English-dictionary-only.zip.
        if ((UILanguageStrings.isEnglish (lang))
            &&
            (Files.notExists (DictionaryProvider.getDictionaryFilePath (lang)))
            &&
            (DictionaryProvider.hasSynonymsDirectory (lang))
           )
        {

            fileLang = "English-dictionary-only";

        }

        URL url = null;

        try
        {

            url = new URL (Environment.getQuollWriterWebsite () + "/" + StringUtils.replaceString (UserProperties.get (com.quollwriter.Constants.QUOLL_WRITER_LANGUAGE_FILES_URL_PROPERTY_NAME),
                                                                                                   "[[LANG]]",
                                                                                                   StringUtils.replaceString (fileLang,
                                                                                                                              " ",
                                                                                                                              "%20")));

        } catch (Exception e) {

            Environment.logError ("Unable to download language files, cant create url",
                                  e);

            ComponentUtils.showErrorMessage (parent,
                                             getUILanguageStringProperty (dictionary,download,actionerror));
                                      //"Unable to download language files");

            return;

        }

        Environment.logDebugMessage ("Downloading language file(s) from: " + url + ", for language: " + lang);

        File _file = null;

        // Create a temp file for it.
        try
        {

            _file = File.createTempFile ("quollwriter-language-" + fileLang,
                                         null);

        } catch (Exception e) {

            Environment.logError ("Unable to download language files, cant create temp file",
                                  e);

            ComponentUtils.showErrorMessage (parent,
                                             getUILanguageStringProperty (dictionary,download,actionerror));
                                    //"Unable to download language files");

            return;

        }

        _file.deleteOnExit ();

        final File file = _file;

        VBox b = new VBox ();

        Text text = new Text ();
        text.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (dictionary,download,notification),
                                                                language));

        final ProgressBar prog = new ProgressBar ();

        b.getChildren ().addAll (text, prog);

        final Notification n = parent.addNotification (b,
                                                       StyleClassNames.DOWNLOAD,
                                                       -1,
                                                       null);

        final UrlDownloader downloader = new UrlDownloader (url,
                                                            file,
                                                            new DownloadListener ()
                                                            {

                                                                @Override
                                                                public void handleError (Exception e)
                                                                {

                                                                    UIUtils.runLater (() ->
                                                                    {

                                                                        n.removeNotification ();

                                                                        Environment.logError ("Unable to download language files",
                                                                                              e);

                                                                        ComponentUtils.showErrorMessage (parent,
                                                                                                         getUILanguageStringProperty (dictionary,download,actionerror));
                                                                                                  //"A problem has occurred while downloading the language files for <b>" + langOrig + "</b>.<br /><br />Please contact Quoll Writer support for assistance.");

                                                                    });

                                                                }

                                                                @Override
                                                                public void progress (final int downloaded,
                                                                                      final int total)
                                                                {

                                                                    UIUtils.runLater (() ->
                                                                    {

                                                                        double val = (double) downloaded / (double) total;

                                                                        prog.setProgress (val);

                                                                    });

                                                                }

                                                                @Override
                                                                public void finished (int total)
                                                                {

                                                                    UIUtils.runLater (() ->
                                                                    {

                                                                        prog.setProgress (-1);

                                                                    });

                                                                    new Thread (() ->
                                                                    {

                                                                        // Now extract the file into the relevant directory.
                                                                        try
                                                                        {

                                                                            Utils.extractZipFile (file,
                                                                                                  Environment.getUserQuollWriterDirPath ().toFile ());

                                                                        } catch (Exception e) {

                                                                            Environment.logError ("Unable to extract language zip file: " +
                                                                                                  file +
                                                                                                  " to: " +
                                                                                                  Environment.getUserQuollWriterDirPath (),
                                                                                                  e);

                                                                             ComponentUtils.showErrorMessage (parent,
                                                                                                              getUILanguageStringProperty (dictionary,download,actionerror));

                                                                            return;

                                                                        } finally {

                                                                            file.delete ();

                                                                        }

                                                                        if (onComplete != null)
                                                                        {

                                                                            UIUtils.runLater (() ->
                                                                            {

                                                                                prog.setProgress (-1);

                                                                                onComplete.run ();

                                                                            });

                                                                        }

                                                                        UIUtils.runLater (() -> n.removeNotification ());

                                                                    }).start ();

                                                                }

                                                            });

        downloader.start ();

// TODO Add a listener to the notification for removal.
/*
        n.addCancelListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                downloader.stop ();

                file.delete ();

            }

        });
*/
    }

    /**
     * Gets the path for the dictionaires.
     */
    public static Path getDictionariesDirPath ()
    {

        return Environment.getUserPath (com.quollwriter.Constants.DICTIONARIES_DIR);

    }

    /**
     * Gets the user dictionary.
     */
    public static Path getUserDictionaryFilePath ()
    {

        return DictionaryProvider.getDictionariesDirPath ().resolve (com.quollwriter.Constants.USER_DICTIONARY_FILE_NAME);

    }

    /**
     * Gets the path for a specific dictionary.
     */
    public static Path getDictionaryFilePath (String lang)
    {

        return DictionaryProvider.getDictionariesDirPath ().resolve (lang + ".zip");

    }

    public static boolean hasSynonymsDirectory (String lang)
    {

        Path f = Environment.getUserPath (com.quollwriter.Constants.THESAURUS_DIR).resolve (lang);

        return Files.exists (f) && Files.isDirectory (f);

    }

}
