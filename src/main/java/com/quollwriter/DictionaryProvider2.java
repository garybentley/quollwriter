package com.quollwriter;

import java.io.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.text.*;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.SpellCheckConfiguration;
import com.softcorporation.suggester.Suggestion;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;

public interface DictionaryProvider2
{

    public void addDictionaryChangedListener (DictionaryChangedListener l);

    public void removeDictionaryChangedListener (DictionaryChangedListener l);

    public SpellChecker getSpellChecker ();

    public void removeWord (String word);

    public void addWord (String word);

}
