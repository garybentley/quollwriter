package com.quollwriter.synonyms;

import java.util.*;

import com.quollwriter.*;


public interface SynonymProvider
{

    public boolean isLanguageSupported (String language);

    public void init (String language)
                      throws GeneralException;

    public Synonyms getSynonyms (String word)
                          throws GeneralException;

    public String getWordTypes (String word)
                         throws GeneralException;

    public void setUseCache (boolean v);

    public boolean hasSynonym (String word)
                               throws GeneralException;

}
