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

import java.util.Arrays;

/**
 * Chords. 
 * According to the rule, when the product data is -1, it means closed string, 
 * and when it is 0, it means empty string.
 *
 * @author airsaid
 */
public class Chord {

    public static Chord defaultC = new Chord(new int[]{-1, 3, 2, 0, 1, 0});

    /** 弦数 */
    public static final int STRING = 6;

    /** 品 */
    private int[] frets;
    /** 指法 */
    private int[] fingers;

    public Chord() {}

    public Chord(int[] frets) {
        this.frets = frets;
    }

    public Chord(int[] frets, int[] fingers) {
        this.frets = frets;
        this.fingers = fingers;
    }

    public int[] getFrets() {
        return frets;
    }

    public void setFrets(int[] frets) {
        this.frets = frets;
    }

    public int[] getFingers() {
        return fingers;
    }

    public void setFingers(int[] fingers) {
        this.fingers = fingers;
    }

    /**
     * Returns whether there is an empty string in the sum.
     *
     * @return Returns true if there is an empty string in the chord, 
     * otherwise returns false.
     */
    public boolean isEmptyString() {
        for (int fret : frets) {
            if (fret == 0) return true;
        }
        return false;
    }

    /**
     * Returns whether there is a closed string in the sum.
     *
     * @return Return true if there is a closed string in the chord, 
     * false otherwise
     */
    public boolean isClosedString() {
        for (int fret : frets) {
            if (fret == -1) return true;
        }
        return false;
    }

    /**
     * Get the smallest fret in the current chord.
     *
     * @return The smallest fret, when it is not found, will return -1. 
     * (This situation generally does not occur 
     * unless there is a problem with the frets data)
     */
    public int getLeastFret() {
        int leastFret = -1;
        for (int i = 0; i < frets.length; i++) {
            int fret = frets[i];
            // Do not handle less than 1 fret
            if (fret < 1) {
                continue;
            }
            //Assigning a value to leastFret for the first time
            if (leastFret == -1) {
                leastFret = fret;
                continue;
            }
            //Compare the following numbers in order to get the smallest product
            if (fret < leastFret) {
                leastFret = fret;
            }
        }
        return leastFret;
    }

    /**
     * 获取和弦中最大的品。
     *
     * @return The largest product, will return -1 when not found. 
     * (This situation generally does not occur unless there is a problem 
     * with the frets data)
     */
    public int getLargestFret() {
        int largest = -1;
        for (int i = 0; i < frets.length; i++) {
            int fret = frets[i];
            // Do not handle less than 1 item
            if (fret < 1) {
                continue;
            }
            // 第一次为 largest 赋值
            if (largest == -1) {
                largest = fret;
                continue;
            }
            //Compare the following numbers in order to get the largest product.
            if (fret > largest) {
                largest = fret;
            }
        }
        return largest;
    }

    /**
     * Get the fret at the corresponding position by the specified string.
     *
     * @param string string
     * @return fret
     */
    public int getFret(int string) {
        return frets[STRING - string];
    }

    @Override
    public String toString() {
        return "Chord{" +
                "frets=" + Arrays.toString(frets) +
                ", fingers=" + Arrays.toString(fingers) +
                '}';
    }
}
