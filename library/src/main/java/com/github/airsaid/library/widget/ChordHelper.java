/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.airsaid.library.widget;

/**
 * @author airsaid
 */
public class ChordHelper {

    /**
     * Get the information you need to cross the chords. 
     * They are the fret and the cut-off string.
     *
     * @param chord Chord object
     * @return The information array, when it is barred, 
     * stores the product at the corner mark 0, 
     * and stores the cut-off string at the corner mark 1. 
     * Returns NULL when not barred.
     */
    public int[] getBarreChordData(Chord chord) {
        int[] data = new int[2];

        // Determine if there is an empty string
        boolean emptyString = chord.isEmptyString();
        if (emptyString) {
            int withFirstString = getWithFirstString(chord);
            // Determine if the 1 string is the lowest fret in the chord,
            // and the higher string is the same as the 1 string and connected
            if (firstStringLeast(chord) && withFirstString > 1) {
                // barre: 1 string -> the highest string connected
                data[0] = chord.getLeastFret();
                data[1] = withFirstString;
            } else {
                // Non-horizontal
                return null;
            }
        } else {
            // Determine if there is a closed string
            if (chord.isClosedString()) {
                // Horizontal press: 1 string -> highest string 
                // (if the highest string is closed, 
                // then rewind a string until the non-closed string)
                data[0] = chord.getLeastFret();
                data[1] = getMaxUnClosedString(chord);
            } else {
                //Horizontal press: 1 string -> the highest string of the guitar
                data[0] = chord.getLeastFret();
                data[1] = Chord.STRING;
            }
        }

        return data;
    }

    /**
     * Get the item at the 1 string is the smallest item in the entire chord.
     *
     * @param chord Chord object
     * @return Returns true if the 1st string is the smallest item, 
     * otherwise returns false.
     */
    public boolean firstStringLeast(Chord chord) {
        int[] frets = chord.getFrets();
        return frets[frets.length - 1] == chord.getLeastFret();
    }

    /**
     * Get the largest chord of the same item at the same 1 chord.
     *
     * For example, if the chord is: 3, 3, 2, 1, 1, 1 then the 1st string is 1,
     * and the 2, 3 strings are the same as the 1 string, 
     * so the largest string is returned: 3 (string).
     *
     * @param chord Chord object
     * @return Maximum string.
     */
    public int getWithFirstString(Chord chord) {
        int string = 1;
        int[] frets = chord.getFrets();
        int[] fingers = chord.getFingers();
        while (frets[frets.length - 1] == frets[frets.length - 1 - string]) {
            if(fingers!=null){
                if(fingers[fingers.length-1]== fingers[fingers.length-1-string]){
                    string+=1;
                }
                return string;
            }else{
                string += 1;
            }

        }
        return string;
    }

    /**
     * Get the largest non-closed string.
     *
     * Suppose the chord is: -1,3,2,1,1,1. 
     * The largest string is the sixth string, 
     * but the sixth string is an empty string. Then look at the 5th string.
     * Since it is not a closed string, it returns 5 .
     *
     * @param chord Chord object
     * @return string.
     */
    public int getMaxUnClosedString(Chord chord) {
        int[] frets = chord.getFrets();
        int string = frets.length;
        while (frets[frets.length - string] == -1) {
            string -= 1;
        }
        return string;
    }

}
