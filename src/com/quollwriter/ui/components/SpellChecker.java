package com.quollwriter.ui.components;

import java.util.*;

public interface SpellChecker
{
    
    public boolean isCorrect (String word);
    
    public boolean isIgnored (String word);
    
    public List<String> getSuggestions (String word);
    
}