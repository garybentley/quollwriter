package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.nio.file.*;

import org.jdom.*;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChaptersOverReadabilityTargetPopup extends PopupContent<AbstractProjectViewer>
{

    public static final String POPUP_ID = "chaptersoverreadabilitytarget";

    public ChaptersOverReadabilityTargetPopup (AbstractProjectViewer viewer)
    {

        super (viewer);

        Set<Chapter> chaps = viewer.getChaptersOverReadabilityTarget ();

        TargetsData projTargets = viewer.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();
        int tfk = projTargets.getReadabilityFK ();
        int tgf = projTargets.getReadabilityGF ();

        // Chapter | FK | FKdiff | GF | GFdiff

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

        ColumnConstraints col3 = new ColumnConstraints ();
        col3.setHgrow (Priority.NEVER);
        col3.setHalignment (HPos.RIGHT);

        ColumnConstraints col4 = new ColumnConstraints ();
        col4.setHgrow (Priority.NEVER);
        col4.setHalignment (HPos.RIGHT);

        gp.getColumnConstraints ().add (col0);
        gp.getColumnConstraints ().add (col1);
        gp.getColumnConstraints ().add (col2);
        gp.getColumnConstraints ().add (col3);
        gp.getColumnConstraints ().add (col4);

        int row = 0;

        gp.add (QuollLabel.builder ()
                    .styleClassName (StyleClassNames.TITLE)
                    .label (targets,chaptersoverreadabilitymaximum,detail,LanguageStrings.popup,labels,fk)
                    .build (),
                1,
                row);

        gp.add (QuollLabel.builder ()
                    .styleClassName (StyleClassNames.TITLE)
                    .label (targets,chaptersoverreadabilitymaximum,detail,LanguageStrings.popup,labels,gf)
                    .build (),
                3,
                row);

        row++;

        for (Chapter c : chaps)
        {

            gp.add (QuollHyperlink.builder ()
                .label (c.nameProperty ())
                .styleClassName (StyleClassNames.CHAPTER)
                .tooltip (getUILanguageStringProperty (actions,clicktoview))
                .onAction (ev ->
                {

                    viewer.viewObject (c);

                })
                .build (),
                0,
                row);

            ReadabilityIndices ri = viewer.getReadabilityIndices (c);

            float fk = ri.getFleschKincaidGradeLevel ();
            float gf = ri.getGunningFogIndex ();

            gp.add (QuollLabel.builder ()
                .label (new SimpleStringProperty (String.format ("%1$s",
                                                                 Environment.formatNumber (fk))))
                .build (),
                1,
                row);

            gp.add (QuollLabel.builder ()
                .label (new SimpleStringProperty (String.format ("%1$s",
                                                                 Environment.formatNumber (gf))))
                .build (),
                3,
                row);

            float diffFK = fk - tfk;
            float diffGF = gf - tgf;

            Label diffFKl = null;

            if (diffFK > 0)
            {

                diffFKl = QuollLabel.builder ()
                    .styleClassName (StyleClassNames.OVER)
                    .label (new SimpleStringProperty (String.format ("+%1$s",
                                                                     Environment.formatNumber (diffFK))))
                    .build ();

            } else {

                diffFKl = QuollLabel.builder ()
                    .label (new SimpleStringProperty ("-"))
                    .build ();

            }

            gp.add (diffFKl,
                    2,
                    row);

            Label diffGFl = null;

            if (diffGF > 0)
            {

                diffGFl = QuollLabel.builder ()
                    .styleClassName (StyleClassNames.OVER)
                    .label (new SimpleStringProperty (String.format ("+%1$s",
                                                                     Environment.formatNumber (diffGF))))
                    .build ();

            } else {

                diffGFl = QuollLabel.builder ()
                    .label (new SimpleStringProperty ("-"))
                    .build ();


            }

            gp.add (diffGFl,
                    4,
                    row);

            row++;

        }

        VBox content = new VBox ();

        content.getChildren ().add (gp);

        content.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .label (targets,chaptersoverreadabilitymaximum,detail,LanguageStrings.popup,buttons,close)
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
            .title (getUILanguageStringProperty (targets,chaptersoverreadabilitymaximum,detail, LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.READABILITY)
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
