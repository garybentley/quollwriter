package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.beans.property.*;

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
    private LinkedToPanel linkedTo = null;

    public ChapterInformationSideBar (ProjectViewer viewer,
                                      Chapter       chapter)
    {

        super (viewer,
               chapter);

        final ChapterInformationSideBar _this = this;

        this.desc = ChapterField.builder ()
        // TODO Use a property for the name...
            .name (this.object.getLegacyTypeField (LegacyUserConfigurableObject.DESCRIPTION_LEGACY_FIELD_ID).getFormName ())
            .value (this.object.getDescription ())
            .update (v -> _this.object.setDescription (v))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        this.goals = ChapterField.builder ()
        // TODO Use a property for the name...
            .name (this.object.getLegacyTypeField (Chapter.GOALS_LEGACY_FIELD_ID).getFormName ())
            .value (this.object.getGoals ())
            .update (v -> _this.object.setGoals (v))
            .bulleted (true)
            .styleClassName (StyleClassNames.GOALS)
            .build ();

        this.goals = ChapterField.builder ()
        // TODO Use a property for the name...
            .name (_this.object.getLegacyTypeField (Chapter.PLAN_LEGACY_FIELD_ID).getFormName ())
            .value (_this.object.getPlan ())
            .update (v -> _this.object.setPlan (v))
            .bulleted (true)
            .styleClassName (StyleClassNames.PLAN)
            .build ();

        this.linkedTo = new LinkedToPanel (this.object);

        this.getChildren ().addAll (this.desc, this.goals, this.plan, this.linkedTo);

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
            .sideBarId (ID + this.object.getKey ())
            .build ();

    }

    @Override
    public void init (State state)
    {

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

        State s = this.getState ();

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

        if (this.linkedTo.isContentVisible ())
        {

            its.add (SECT_LINKED);

        }

        s.set (State.Key.open,
               its);

        return s;

    }

}
