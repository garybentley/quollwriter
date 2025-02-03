package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import org.dom4j.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

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

    private Spinner<Integer> gfF2 = null;
    private Spinner<Integer> fkF2 = null;
    private Spinner<Integer> frF2 = null;

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

        d = Utils.replaceString (d,
                                       "[FLESCH_KINCAID]",
                                       this.fleschKincaid + "");

        d =Utils.replaceString (d,
                                       "[FLESCH_READING]",
                                       this.fleschReading + "");

        d = Utils.replaceString (d,
                                       "[GUNNING_FOG]",
                                       this.gunningFog + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = Utils.replaceString (super.getSummary (),
                                              "[FLESCH_KINCAID]",
                                              this.fleschKincaid + "");

        t = Utils.replaceString (t,
                                       "[FLESCH_READING]",
                                       this.fleschReading + "");

        t = Utils.replaceString (t,
                                       "[GUNNING_FOG]",
                                       this.gunningFog + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.fleschKincaid = DOM4JUtils.attributeValueAsInt (root,
                                                               XMLConstants.fleschKincaid);
        this.fleschReading = DOM4JUtils.attributeValueAsInt (root,
                                                               XMLConstants.fleschReading);
        this.gunningFog = DOM4JUtils.attributeValueAsInt (root,
                                                            XMLConstants.gunningFog);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.fleschKincaid,
                           this.fleschKincaid + "");
        root.addAttribute (XMLConstants.fleschReading,
                           this.fleschReading + "");
        root.addAttribute (XMLConstants.gunningFog,
                           this.gunningFog + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Paragraph paragraph)
    {

        List<Issue> issues = new ArrayList<> ();

        //int wc = TextUtilities.getWordCount (paragraph);

        if (paragraph.getWordCount () > 100)
        {

            List<String> pref = new ArrayList<> ();
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
    public Set<Form.Item> getFormItems ()
    {

        List<String> pref = Arrays.asList (problemfinder,config,rules,paragraphreadability,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.fkF2 = new Spinner<> (new IntegerSpinnerValueFactory (0, 30, this.fleschKincaid, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,fk)),
                                    //"Flesch Kincaid Grade level",
                                  this.fkF2));

        this.frF2 = new Spinner<> (new IntegerSpinnerValueFactory (0, 30, this.fleschReading, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,fr)),
                                    //"Flesch Reading ease level",
                                  this.frF2));

        this.gfF2 = new Spinner<> (new IntegerSpinnerValueFactory (0, 30, this.gunningFog, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,gf)),
                                    //"Gunning Fog index",
                                  this.gfF2));

        return items;

    }

    @Override
    public void updateFromForm ()
    {

        this.fleschKincaid = this.fkF2.getValue ();
        this.fleschReading = this.frF2.getValue ();
        this.gunningFog = this.gfF2.getValue ();

    }

}
