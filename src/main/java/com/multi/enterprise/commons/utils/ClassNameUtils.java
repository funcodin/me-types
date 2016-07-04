package com.multi.enterprise.commons.utils;

import java.util.ArrayList;
import java.util.List;

public class ClassNameUtils {
    public static String[] toWordList( final String className ) {
        final List<String> words = new ArrayList<String>();
        final char[] chars = className.toCharArray();

        int lastEndIndex = 0;
        boolean seenLowerCase = false;
        for ( int i = 0; i < chars.length; i++ ) {
            if ( chars[i] >= 'A' && chars[i] <= 'Z' ) {
                // starting of a new word with a uppowercase after a lowercase and word-length >= 1
                if ( seenLowerCase == true && i > lastEndIndex ) {
                    words.add( className.substring( lastEndIndex, i ) );
                    seenLowerCase = false;
                    lastEndIndex = i;
                    // starting of a new word if next character is a lowercase and word-length >= 1
                } else if ( i + 1 < chars.length && chars[i + 1] >= 'a' && chars[i + 1] <= 'z' && i > lastEndIndex ) {
                    words.add( className.substring( lastEndIndex, i ) );
                    seenLowerCase = false;
                    lastEndIndex = i;
                }
            } else if ( chars[i] >= 'a' && chars[i] <= 'z' ) {
                seenLowerCase = true;
            }
        }
        words.add( className.substring( lastEndIndex, chars.length ) );
        return words.toArray( new String[0] );
    }

    protected static boolean containsLowerCase( final String word ) {
        final char[] chars = word.toCharArray();
        for ( final char c : chars ) {
            if ( c >= 'a' && c <= 'z' ) {
                return true;
            }
        }
        return false;
    }

    protected static String toVariableWord( final String word ) {
        if ( !containsLowerCase( word ) ) {
            return word.toLowerCase();
        }
        return Character.toLowerCase( word.charAt( 0 ) ) + word.substring( 1, word.length() );
    }

    public static String toVariableName( final String className ) {
        final String[] words = toWordList( className );
        words[0] = toVariableWord( words[0] );
        final StringBuilder sb = new StringBuilder();
        for ( final String word : words ) {
            sb.append( word );
        }
        return sb.toString();
    }

}