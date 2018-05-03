package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.Element;
import org.jdom.JDOMException;

public class ParagraphReadabilityRule extends AbstractParagraphRule
{

    public class XMLConstants
    {

        public static final String fleschKincaid = "fleschKincaid";
        public static final String fleschReading = "fleschReading";
        public static final String gunningFog = "gunningFog";

    }

    private int fleschKincaid = 0;
    private int fleschReading = 0;
    private int gunningFog = 0;
    private JSpinner gfF = null;
    private JSpinner fkF = null;
    private JSpinner frF = null;

    public ParagraphReadabilityRule ()
    {

    }

    public ParagraphReadabilityRule (int     fleschKincaid,
                                     int     fleschReading,
                                     int     gunningFog,
                                     boolean user)
    {

        this.fleschKincaid = fleschKincaid;
        this.fleschReading = fleschReading;
        this.gunningFog = gunningFog;
        this.setUserRule (user);

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        d = StringUtils.replaceString (d,
                                       "[FLESCH_KINCAID]",
                                       this.fleschKincaid + "");

        d = StringUtils.replaceString (d,
                                       "[FLESCH_READING]",
                                       this.fleschReading + "");

        d = StringUtils.replaceString (d,
                                       "[GUNNING_FOG]",
                                       this.gunningFog + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = StringUtils.replaceString (super.getSummary (),
                                              "[FLESCH_KINCAID]",
                                              this.fleschKincaid + "");

        t = StringUtils.replaceString (t,
                                       "[FLESCH_READING]",
                                       this.fleschReading + "");

        t = StringUtils.replaceString (t,
                                       "[GUNNING_FOG]",
                                       this.gunningFog + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.fleschKincaid = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.fleschKincaid);
        this.fleschReading = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.fleschReading);
        this.gunningFog = JDOMUtils.getAttributeValueAsInt (root,
                                                            XMLConstants.gunningFog);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.fleschKincaid,
                           this.fleschKincaid + "");
        root.setAttribute (XMLConstants.fleschReading,
                           this.fleschReading + "");
        root.setAttribute (XMLConstants.gunningFog,
                           this.gunningFog + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Paragraph paragraph)
    {

        List<Issue> issues = new ArrayList ();

        //int wc = TextUtilities.getWordCount (paragraph);

        if (paragraph.getWordCount () > 100)
        {

            List<String> pref = new ArrayList ();
            pref.add (LanguageStrings.problemfinder);
            pref.add (LanguageStrings.issues);
            pref.add (LanguageStrings.paragraphreadability);

            ReadabilityIndices ri = new ReadabilityIndices ();
            ri.add (paragraph.getText ());

            if ((this.fleschKincaid > 0)
                &&
                (ri.getFleschKincaidGradeLevel () > this.fleschKincaid)
               )
            {

                Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                               LanguageStrings.fkfull),
                                                      Environment.formatNumber (ri.getFleschKincaidGradeLevel ()),
                                                      Environment.formatNumber (this.fleschKincaid)),
                                                      //"Paragraph has a Flesch Kincaid grade level of: <b>" + Environment.formatNumber (ri.getFleschKincaidGradeLevel ()) + "</b>.  (Max is: " + Environment.formatNumber (this.fleschKincaid) + ")",
                                       paragraph,
                                       paragraph.getAllTextStartOffset () + "-fkgl-" + ri.getFleschKincaidGradeLevel (),
                                       this);

                issues.add (iss);

            }

            if ((this.fleschReading > 0)
                &&
                (ri.getFleschReadingEase () > this.fleschReading)
               )
            {

                Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                               LanguageStrings.frfull),
                                                      Environment.formatNumber (ri.getFleschReadingEase ()),
                                                      Environment.formatNumber (this.fleschReading)),
                                                      //"Paragraph has a Flesch Reading ease level of: <b>" + Environment.formatNumber (ri.getFleschReadingEase ()) + "</b>.  (Max is: " + Environment.formatNumber (this.fleschReading) + ")",
                                       paragraph,
                                       paragraph.getAllTextStartOffset () + "-fre-" + ri.getFleschReadingEase (),
                                       this);

                issues.add (iss);

            }

            if ((this.gunningFog > 0)
                &&
                (ri.getGunningFogIndex () > this.gunningFog)
               )
            {

                Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                               LanguageStrings.gffull),
                                                      Environment.formatNumber (ri.getGunningFogIndex ()),
                                                      Environment.formatNumber (this.gunningFog)),
                                                      //"Paragraph has a Gunning Fog index of: <b>" + Environment.formatNumber (ri.getGunningFogIndex ()) + "</b>.  (Max is: " + Environment.formatNumber (this.gunningFog) + ")",
                                       paragraph,
                                       paragraph.getAllTextStartOffset () + "-gfi-" + ri.getGunningFogIndex (),
                                       this);

                issues.add (iss);

            }

        }

        return issues;

    }

    @Override
    public String getCategory ()
    {

        return Rule.PARAGRAPH_CATEGORY;

    }

    @Override
    public Set<FormItem> getFormItems ()
    {

        List<String> pref = new ArrayList ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.paragraphreadability);
        pref.add (LanguageStrings.labels);

        Set<FormItem> items = new LinkedHashSet ();

        this.fkF = new JSpinner (new SpinnerNumberModel (this.fleschKincaid,
                                                         0,
                                                         30,
                                                         1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.fkF);
        b.add (Box.createHorizontalGlue ());

        this.fkF.setMaximumSize (this.fkF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.fk),
                                    //"Flesch Kincaid Grade level",
                                    b));

        this.frF = new JSpinner (new SpinnerNumberModel (this.fleschReading,
                                                         0,
                                                         30,
                                                         1));

        b = new Box (BoxLayout.X_AXIS);

        b.add (this.frF);
        b.add (Box.createHorizontalGlue ());

        this.frF.setMaximumSize (this.frF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.fr),
                                    //"Flesch Reading ease level",
                                    b));

        this.gfF = new JSpinner (new SpinnerNumberModel (this.gunningFog,
                                                         0,
                                                         30,
                                                         1));

        b = new Box (BoxLayout.X_AXIS);

        b.add (this.gfF);
        b.add (Box.createHorizontalGlue ());

        this.gfF.setMaximumSize (this.gfF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.gf),
                                    //"Gunning Fog index",
                                    b));

        return items;

    }

    @Override
    public String getFormError ()
    {

        return null;

    }

    @Override
    public void updateFromForm ()
    {

        this.fleschKincaid = ((SpinnerNumberModel) this.fkF.getModel ()).getNumber ().intValue ();
        this.fleschReading = ((SpinnerNumberModel) this.frF.getModel ()).getNumber ().intValue ();
        this.gunningFog = ((SpinnerNumberModel) this.gfF.getModel ()).getNumber ().intValue ();

    }

}
