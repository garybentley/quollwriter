package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChapterInformationSideBar extends NamedObjectSideBarContent<ProjectViewer, Chapter>
{

    public static final String SECT_DESC = "desc";
    public static final String SECT_GOALS = "goals";
    public static final String SECT_PLAN = "plan";
    public static final String SECT_LINKED = "linked";
    public static final String ID = "chapterinformation";

    private ChapterField goals = null;
    private ChapterField desc = null;
    private ChapterField plan = null;
    private AccordionItemLinkedToPanel linkedTo = null;

    public ChapterInformationSideBar (ProjectViewer viewer,
                                      Chapter       chapter)
    {

        super (viewer,
               chapter);

        final ChapterInformationSideBar _this = this;

        this.desc = ChapterField.builder ()
        // TODO Use a property for the name...
            .withChapter (this.object)
            .name (this.object.getLegacyTypeField (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID).getFormName ())
            .value (this.object.getDescription ())
            .update (v -> _this.object.setDescription (v))
            .withViewer (viewer)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        this.goals = ChapterField.builder ()
        // TODO Use a property for the name...
            .withChapter (this.object)
            .name (this.object.getLegacyTypeField (Chapter.GOALS_LEGACY_FIELD_ID).getFormName ())
            .value (this.object.getGoals ())
            .update (v -> _this.object.setGoals (v))
            .withViewer (viewer)
            .bulleted (true)
            .styleClassName (StyleClassNames.GOALS)
            .build ();

        this.plan = ChapterField.builder ()
        // TODO Use a property for the name...
            .withChapter (this.object)
            .name (_this.object.getLegacyTypeField (Chapter.PLAN_LEGACY_FIELD_ID).getFormName ())
            .value (_this.object.getPlan ())
            .update (v -> _this.object.setPlan (v))
            .withViewer (viewer)
            .bulleted (true)
            .styleClassName (StyleClassNames.PLAN)
            .build ();

        this.linkedTo = new AccordionItemLinkedToPanel (this.object,
                                                        this.getBinder (),
                                                        this.viewer,
                                                        links ->
        {

            try
            {

                chapter.removeAllLinks ();

                links.stream ()
                    .forEach (o -> chapter.addLinkTo (o));

                viewer.saveObject (chapter,
                                   true);

                return true;

            } catch (Exception e) {

                Environment.logError (String.format ("Unable to save links for chapter: %1$s",
                                                     chapter),
                                      e);

                ComponentUtils.showErrorMessage (_this.viewer,
                                                 linkedto,save,actionerror);

                return false;

            }

        },
        // On cancel
        null);

        VBox.setVgrow (this.linkedTo,
                       Priority.ALWAYS);
                       /*
        VBox.setVgrow (this.goals,
                       Priority.ALWAYS);
        VBox.setVgrow (this.plan,
                       Priority.ALWAYS);
        VBox.setVgrow (this.desc,
                       Priority.ALWAYS);
*/
        VBox content = new VBox ();
        VBox.setVgrow (content,
                       Priority.ALWAYS);

        content.getChildren ().addAll (this.desc, this.goals, this.plan, this.linkedTo);

        this.getChildren ().add (content);

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,chapterinfo,LanguageStrings.title),
                                                            this.object.getName ());

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.CHAPTERINFO)
            .withScrollPane (true)
            .canClose (true)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (ChapterInformationSideBar.getSideBarIdForChapter (this.object))
            .build ();

    }

    public static String getSideBarIdForChapter (Chapter c)
    {

        return ID + c.getKey ();

    }

    @Override
    public void init (State state)
    {

        if (state == null)
        {

            return;

        }

        List open = state.getAs (State.Key.open,
                                 List.class);

        this.desc.setContentVisible (open.contains (SECT_DESC));
        this.goals.setContentVisible (open.contains (SECT_GOALS));
        this.plan.setContentVisible (open.contains (SECT_PLAN));
        this.linkedTo.setContentVisible (open.contains (SECT_LINKED));

        super.init (state);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        List<String> its = new ArrayList<> ();

        if (this.desc.isContentVisible ())
        {

            its.add (SECT_DESC);

        }

        if (this.goals.isContentVisible ())
        {

            its.add (SECT_GOALS);

        }

        if (this.plan.isContentVisible ())
        {

            its.add (SECT_PLAN);

        }

        if (this.linkedTo.isOpen ())
        {

            its.add (SECT_LINKED);

        }

        s.set (State.Key.open,
               its);

        return s;

    }

}
