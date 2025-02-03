package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.nio.file.*;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChaptersOverWordCountTargetPopup extends PopupContent<AbstractProjectViewer>
{

    public static final String POPUP_ID = "chaptersoverwordcounttarget";

    public ChaptersOverWordCountTargetPopup (AbstractProjectViewer viewer)
    {

        super (viewer);

        Set<Chapter> chaps = viewer.getChaptersOverWordTarget ();

        TargetsData projTargets = viewer.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();

        // Chapter | WC | WCover

        GridPane gp = new GridPane ();
        gp.getStyleClass ().add (StyleClassNames.ITEMS);
        ColumnConstraints col0 = new ColumnConstraints ();
        col0.setHgrow (Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints ();
        col1.setHgrow (Priority.NEVER);
        col1.setHalignment (HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints ();
        col2.setHgrow (Priority.NEVER);
        col2.setHalignment (HPos.RIGHT);

        gp.getColumnConstraints ().add (col0);
        gp.getColumnConstraints ().add (col1);
        gp.getColumnConstraints ().add (col2);

        boolean hasOver25 = false;
        int row = 0;

        for (Chapter c : chaps)
        {

            ChapterCounts count = viewer.getChapterCounts (c);

            int diff = count.getWordCount () - tcc;

            int perc = Utils.getPercent (diff, tcc);

            QuollHyperlink l = QuollHyperlink.builder ()
                .label (c.nameProperty ())
                .styleClassName (StyleClassNames.CHAPTER)
                .tooltip (getUILanguageStringProperty (actions,clicktoview))
                .onAction (ev ->
                {

                    viewer.viewObject (c);

                })
                .build ();

            gp.add (l,
                0,
                row);

            if (perc >= 25)
            {

                l.getStyleClass ().add (StyleClassNames.OVER);

            }

            gp.add (QuollLabel.builder ()
                .label (new SimpleStringProperty (String.format ("%1$s",
                                                                 Environment.formatNumber (count.getWordCount ()))))
                .build (),
                1,
                row);

            gp.add (QuollLabel.builder ()
                .styleClassName ((perc >= 25 ? StyleClassNames.OVER : null))
                .label (new SimpleStringProperty (String.format ("+%1$s",
                                                                 Environment.formatNumber (count.getWordCount () - tcc))))
                .build (),
                2,
                row);

            if (perc >= 25)
            {

                hasOver25 = true;

            }

            row++;

        }

        VBox content = new VBox ();

        content.getChildren ().add (BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (Arrays.asList (targets,chaptersoverwcmaximum,detail,LanguageStrings.popup,text),
                                                Environment.formatNumber (tcc)))
            .build ());

        if (hasOver25)
        {

            content.getChildren ().add (BasicHtmlTextFlow.builder ()
                .styleClassName (StyleClassNames.DESCRIPTION)
                .text (getUILanguageStringProperty (Arrays.asList (targets,chaptersoverwcmaximum,detail,LanguageStrings.popup,overlimit)))
                .build ());

        }

        ScrollPane sp = new ScrollPane (gp);
        content.getChildren ().add (sp);

        content.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .label (targets,chaptersoverwcmaximum,detail,LanguageStrings.popup,buttons,close)
                        .buttonType (ButtonBar.ButtonData.OK_DONE)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ());

        this.getChildren ().addAll (content);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (targets,chaptersoverwcmaximum,detail, LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.WORDCOUNT)
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
