package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;

import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.achievements.rules.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AchievementsPopup extends PopupContent
{

    public static final String POPUP_ID = "achievements";
    private VBox content = null;
    private ScheduledFuture hideTimer = null;

    public AchievementsPopup (AbstractViewer viewer)
    {

        super (viewer);

        this.content = new VBox ();
        this.getStyleClass ().add (StyleClassNames.ACHIEVEMENTS);
        this.getChildren ().add (this.content);

    }

    public void showAchievement (AchievementRule ar)
    {

        this.content.getChildren ().add (new AchievementView (ar,
                                                              true));

        this.show (10, 10);

        this.scheduleHide (100000000);

    }

    private void scheduleHide (int hideIn)
    {

        this.hideTimer.cancel (false);

        this.viewer.schedule (() ->
        {

            UIUtils.runLater (() ->
            {

                this.close ();
                this.content.getChildren ().clear ();

            });

        },
        hideIn,
        -1);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (achievementreached, LanguageStrings.title)
            .styleClassName (StyleClassNames.ACHIEVEMENTS)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .build ();

        p.headerProperty ().getValue ().addEventHandler (MouseEvent.MOUSE_RELEASED,
                                                         ev ->
        {

            p.close ();

            this.content.getChildren ().clear ();

        });

        p.addEventHandler (MouseEvent.MOUSE_ENTERED,
                           ev ->
        {

            UIUtils.runLater (() ->
            {

                this.hideTimer.cancel (false);

            });

        });

        p.addEventHandler (MouseEvent.MOUSE_EXITED,
                           ev ->
        {

            UIUtils.runLater (() ->
            {

                this.scheduleHide (2000);

            });

        });

        p.setVisible (false);

        return p;

    }

}
