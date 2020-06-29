package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChangeProjectItemPreviewDisplayPopup extends PopupContent
{

    public static final String POPUP_ID = "projectitempreview";

    public ChangeProjectItemPreviewDisplayPopup (AbstractViewer viewer)
    {

        super (viewer);

        VBox b = new VBox ();
        this.getChildren ().add (b);

        b.getChildren ().add (BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,text))
            .build ());

        // TODO Add controls for bold/italic?  Add menu?  Add tag menu?
        QuollTextArea format = QuollTextArea.builder ()
            .styleClassName (StyleClassNames.FORMAT)
            .withViewer (viewer)
            .formattingEnabled (true)
            .placeholder (project,sidebar,chapters,preview,edit,LanguageStrings.popup,tooltip)
            .build ();
        b.getChildren ().add (format);

        b.getChildren ().add (Header.builder ()
            .styleClassName (StyleClassNames.EXAMPLE)
            .title (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,examplechapter,title))
            .build ());

        final Chapter _bogus = new Chapter ();
        _bogus.setKey (1L);
        _bogus.setDescription (new StringWithMarkup (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,examplechapter,description).getValue ()));
        //"This chapter will be really, really good once I actually start to write it."));
        _bogus.setText (new StringWithMarkup (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,examplechapter,text).getValue ()));
        //"Once upon a time there was a little chapter that wanted to be written."));

        final ProjectViewer _bogusPV = new ProjectViewer ()
        {

           public ChapterCounts getChapterCounts (Chapter c)
           {

               ChapterCounts cc = new ChapterCounts (_bogus.getChapterText ());

               return cc;

           }

           public Set<Word> getSpellingErrors (Chapter c)
           {

               Set<Word> ret = new HashSet ();
               return ret;

           }

           public Set<Issue> getProblems (Chapter c)
           {

               Set<Issue> ret = new HashSet ();
               return ret;

           }

       };

        // TODO Wrap in another box to allow for centering/padding.
        VBox exb = new VBox ();

        exb.getStyleClass ().addAll (StyleClassNames.EXAMPLE);

        BasicHtmlTextFlow info = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.INFO)
            .withHandler (this.viewer)
            .build ();

        format.getTextEditor ().multiRichChanges ().subscribe (ch ->
        {

            info.setText (UIUtils.getChapterInfoPreview (_bogus,
                                                         format.getTextWithMarkup (),
                                                         _bogusPV));

        });

        String prevformat = UserProperties.get (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
                                                Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT);

        format.setText (prevformat);

        exb.getChildren ().addAll (info);

        b.getChildren ().add (exb);

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.APPLY)
                        .label (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,buttons,save))
                        .onAction (ev ->
                        {

                            UserProperties.set (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
                                                format.getTextWithMarkup ().getText ());

                            this.close ();

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.OTHER)
                        .label (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,usedefault))
                        .onAction (ev ->
                        {

                            format.setText (UserProperties.get (Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT));

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                        .label (getUILanguageStringProperty (project,sidebar,chapters,preview,edit,LanguageStrings.popup,buttons,cancel))
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ();

        b.getChildren ().add (bb);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (project,sidebar,chapters,preview,edit,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.PROJECTITEMPREVIEW)
            .styleSheet (StyleClassNames.PROJECTITEMPREVIEW)
            .headerIconClassName (StyleClassNames.EDIT)
            .popupId (POPUP_ID)
            .content (this)
            .withClose (true)
            .withViewer (this.viewer)
            .hideOnEscape (true)
            .removeOnClose (true)
            .show ()
            .build ();

        p.requestFocus ();

        return p;

    }

}
