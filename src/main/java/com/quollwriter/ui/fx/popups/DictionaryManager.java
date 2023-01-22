package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class DictionaryManager extends PopupContent
{

    public static final String POPUP_ID = "dictionarymanager";

    private UserDictionaryProvider dictProv = null;

    public DictionaryManager (AbstractViewer         viewer,
                              UserDictionaryProvider dictProv)
    {

        super (viewer);

        this.dictProv = dictProv;

        VBox b = new VBox ();

/*

        File userDict = DictionaryProvider.getUserDictionaryFilePath ().toFile ();

        Set<String> words = new TreeSet<> ();

        String w = null;

        try
        {

            w = IOUtils.getFile (userDict);

        } catch (Exception e)
        {

            w = "";

            Environment.logError ("Unable to get user dictionary file: " +
                                  userDict,
                                  e);

        }

        StringTokenizer tt = new StringTokenizer (w,
                                                  String.valueOf ('\n'));

        while (tt.hasMoreTokens ())
        {

            words.add (tt.nextToken ());

        }

        return words;
*/
        List<String> words = new ArrayList<> (dictProv.getWords ());

        Collections.sort (words);

        ObservableList<String> _words = FXCollections.observableArrayList (words);

        TextItemManager man = TextItemManager.builder ()
            .addTitle (getUILanguageStringProperty (dictionary,manage,newwords,title))
            .addDescription (getUILanguageStringProperty (dictionary,manage,newwords,text))
            .currentItemsTitle (getUILanguageStringProperty (dictionary,manage,table,title))
            .items (_words)
            .build ();

        b.getChildren ().add (man);

        man.setOnItemRemoved (ev ->
        {

            String v = ev.getOldValue ();

            this.dictProv.removeWord (v);

        });

        man.setOnItemAdded (ev ->
        {

            String v = ev.getNewValue ();

            this.addWord (v);

        });

        man.setOnItemChanged (ev ->
        {

            String v = ev.getNewValue ();
            String n = ev.getOldValue ();

            this.removeWord (n);
            this.addWord (v);

        });

        b.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (manageitems,finish)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ());

        this.getChildren ().add (b);

    }

    private void addWord (String w)
    {

        this.dictProv.addWord (w);

        this.viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                      ProjectEvent.Action.addword,
                                      w);

    }

    private void removeWord (String w)
    {

        this.dictProv.removeWord (w);

        this.viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                      ProjectEvent.Action.removeword,
                                      w);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (dictionary,manage,title)
            .styleClassName (StyleClassNames.DICTIONARYMANAGER)
            .headerIconClassName (StyleClassNames.DICTIONARY)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
