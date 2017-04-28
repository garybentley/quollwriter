package com.quollwriter.data;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

/**
 * Holds state information about the user session.
 */
public class UserSession extends Session
{

    public boolean sessionTargetReachedPopupShown = false;
    public boolean dailyTargetReachedPopupShown = false;
    private int lastSnapshotSessionWordCount = 0;
    private Date lastSnapshotSessionEnd = null;
    private int currentSessionWordCount = 0;
    private Date dailyTargetReachedPopupShownDate = null;
    private Date weeklyTargetReachedPopupShownDate = null;
    private Date monthlyTargetReachedPopupShownDate = null;

    public UserSession ()
    {

        this.init ();

    }

    public UserSession (Date start,
                        Date end,
                        int  wc)
    {

        super (start,
               end,
               wc);

        this.init ();

    }

    private void init ()
    {

        String dv = UserProperties.get (Constants.TARGET_DAILY_TARGET_REACHED_POPUP_SHOWN_DATE);

        Date currDate = Utils.zeroTimeFields (new Date ());

        if (dv != null)
        {

            try
            {

                this.dailyTargetReachedPopupShownDate = Utils.zeroTimeFields (new Date (Long.parseLong (dv)));

                this.dailyTargetReachedPopupShown = (currDate == this.dailyTargetReachedPopupShownDate);

            } catch (Exception e) {

                Environment.logError ("Unable to parse daily target reached popup shown date",
                                      e);

            }

        }

        dv = UserProperties.get (Constants.TARGET_WEEKLY_TARGET_REACHED_POPUP_SHOWN_DATE);

        if (dv != null)
        {

            try
            {

                this.weeklyTargetReachedPopupShownDate = new Date (Long.parseLong (dv));

            } catch (Exception e) {

                Environment.logError ("Unable to parse weekly target reached popup shown date",
                                      e);

            }

        }

        dv = UserProperties.get (Constants.TARGET_MONTHLY_TARGET_REACHED_POPUP_SHOWN_DATE);

        if (dv != null)
        {

            try
            {

                this.monthlyTargetReachedPopupShownDate = new Date (Long.parseLong (dv));

            } catch (Exception e) {

                Environment.logError ("Unable to parse monthly target reached popup shown date",
                                      e);

            }

        }

    }

    public void shownDailyTargetReachedPopup ()
    {

        Date d = Utils.zeroTimeFields (new Date ());

        this.dailyTargetReachedPopupShownDate = d;
        this.dailyTargetReachedPopupShown = true;

        // Set a user property.
        UserProperties.set (Constants.TARGET_DAILY_TARGET_REACHED_POPUP_SHOWN_DATE,
                            String.valueOf (d.getTime ()));

    }

    public boolean shouldShowDailyTargetReachedPopup ()
    {

        if ((this.dailyTargetReachedPopupShownDate != null)
            &&
            (this.dailyTargetReachedPopupShown)
           )
        {

            Date d = Utils.zeroTimeFields (new Date ());

            return d.after (this.dailyTargetReachedPopupShownDate);

        }

        return true;

    }

    private Date getWeekDate ()
    {

        GregorianCalendar gc = new GregorianCalendar ();

        gc.set (Calendar.DAY_OF_WEEK,
                gc.getFirstDayOfWeek ());

        Date d = Utils.zeroTimeFields (gc.getTime ());

        return d;

    }

    public void shownWeeklyTargetReachedPopup ()
    {

        Date d = this.getWeekDate ();

        this.weeklyTargetReachedPopupShownDate = d;

        // Set a user property.
        UserProperties.set (Constants.TARGET_WEEKLY_TARGET_REACHED_POPUP_SHOWN_DATE,
                            String.valueOf (d.getTime ()));

    }

    public boolean shouldShowWeeklyTargetReachedPopup ()
    {

        if (this.weeklyTargetReachedPopupShownDate != null)
        {

            Date d = this.getWeekDate ();

            return d.after (this.weeklyTargetReachedPopupShownDate);

        }

        return true;

    }

    private Date getMonthDate ()
    {

        GregorianCalendar gc = new GregorianCalendar ();

        gc.set (Calendar.DAY_OF_MONTH,
                1);

        Date d = Utils.zeroTimeFields (gc.getTime ());

        return d;

    }

    public void shownMonthlyTargetReachedPopup ()
    {

        Date d = this.getMonthDate ();

        this.monthlyTargetReachedPopupShownDate = d;

        // Set a user property.
        UserProperties.set (Constants.TARGET_MONTHLY_TARGET_REACHED_POPUP_SHOWN_DATE,
                            String.valueOf (d.getTime ()));

    }

    public boolean shouldShowMonthlyTargetReachedPopup ()
    {

        if (this.monthlyTargetReachedPopupShownDate != null)
        {

            Date d = this.getMonthDate ();

            return d.after (this.monthlyTargetReachedPopupShownDate);

        }

        return true;

    }

    public void shownSessionTargetReachedPopup ()
    {

        this.sessionTargetReachedPopupShown = true;

    }

    public boolean shouldShowSessionTargetReachedPopup ()
    {

        return !this.sessionTargetReachedPopupShown;

    }

    public String toString ()
    {

        return super.toString () + ", lastCurrent: " + this.currentSessionWordCount + ", current: " + this.getCurrentSessionWordCount ();

    }

    @Override
    public int getWordCount ()
    {

        return this.getCurrentSessionWordCount ();

    }

    public void updateCurrentSessionWordCount (int wordCount)
    {

        this.currentSessionWordCount += wordCount;

    }

    public int getCurrentSessionWordCount ()
    {

        Map<ProjectInfo, AbstractProjectViewer> pvs = Environment.getOpenProjects ();

        int c = this.currentSessionWordCount;

        for (AbstractProjectViewer pv : pvs.values ())
        {

            c += pv.getSessionWordCount ();

        }

        return c;

    }

    public UserSession createSnapshot ()
    {

        int wc = this.getCurrentSessionWordCount () - this.lastSnapshotSessionWordCount;

        Date end = new Date ();

        UserSession s = new UserSession ((this.lastSnapshotSessionEnd != null ? this.lastSnapshotSessionEnd : this.getStart ()),
                                         end,
                                         wc);

        this.lastSnapshotSessionEnd = end;
        this.lastSnapshotSessionWordCount = wc;

        return s;

    }

}
