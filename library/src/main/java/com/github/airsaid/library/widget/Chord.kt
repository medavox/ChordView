/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.github.airsaid.library.widget

import java.util.Arrays

/**
 * Chords. 
 * According to the rule, when the product data is -1, it means closed string, 
 * and when it is 0, it means empty string.
 *
 * @author airsaid
 */
data class Chord @JvmOverloads constructor(
        var frets:IntArray=intArrayOf(), var fingers:IntArray=intArrayOf()) {
    companion object {
        @JvmStatic
        val defaultC  = Chord(intArrayOf(-1, 3, 2, 0, 1, 0))

    }
    @JvmField
    val STRING:Int = if(frets.isNotEmpty()) {
        frets.size
    }else {
        6
    }

    /**
     * Returns whether there is an empty string in the sum.
     *
     * @return Returns true if there is an empty string in the chord, 
     * otherwise returns false.
     */
    fun isEmptyString():Boolean {
        for (fret in frets) {
            if (fret == 0) return true
        }
        return false
    }

    /**
     * Returns whether there is a closed string in the sum.
     *
     * @return Return true if there is a closed string in the chord, 
     * false otherwise
     */
    fun isClosedString():Boolean {
        for (fret in frets) {
            if (fret == -1) return true
        }
        return false
    }

    /**
     * Get the smallest fret in the current chord.
     *
     * @return The smallest fret, when it is not found, will return -1. 
     * (This situation generally does not occur 
     * unless there is a problem with the frets data)
     */
    fun getLeastFret():Int {
        var leastFret = -1
        for (i in frets.indices) {
            val fret = frets[i]
            // Do not handle less than 1 fret
            if (fret < 1) {
                continue
            }
            //Assigning a value to leastFret for the first time
            if (leastFret == -1) {
                leastFret = fret
                continue
            }
            //Compare the following numbers in order to get the smallest product
            if (fret < leastFret) {
                leastFret = fret
            }
        }
        return leastFret
    }

    /**
     * 获取和弦中最大的品。
     *
     * @return The largest product, will return -1 when not found. 
     * (This situation generally does not occur unless there is a problem 
     * with the frets data)
     */
    fun getLargestFret():Int {
        var largest = -1
        for (i in frets.indices) {
            val fret = frets[i]
            // Do not handle less than 1 item
            if (fret < 1) {
                continue
            }
            // 第一次为 largest 赋值
            if (largest == -1) {
                largest = fret
                continue
            }
            //Compare the following numbers in order to get the largest product.
            if (fret > largest) {
                largest = fret
            }
        }
        return largest
    }

    /**
     * Get the fret at the corresponding position by the specified string.
     *
     * @param string string
     * @return fret
     */
    fun getFret(string:Int):Int {
        return frets[STRING - string]
    }

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
    fun getBarreChordData():IntArray? {
        val data = IntArray(2)

        // Determine if there is an empty string
        if (isEmptyString()) {
            val withFirstString = getWithFirstString()
            // Determine if the 1 string is the lowest fret in the chord,
            // and the higher string is the same as the 1 string and connected
            if (firstStringLeast() && withFirstString > 1) {
                // barre: 1 string -> the highest string connected
                data[0] = getLeastFret()
                data[1] = withFirstString
            } else {
                // Non-horizontal
                return null
            }
        } else {
            // Determine if there is a closed string
            if (isClosedString()) {
                // Horizontal press: 1 string -> highest string 
                // (if the highest string is closed, 
                // then rewind a string until the non-closed string)
                data[0] = getLeastFret()
                data[1] = getMaxUnClosedString()
            } else {
                //Horizontal press: 1 string -> the highest string of the guitar
                data[0] = getLeastFret()
                data[1] = STRING
            }
        }

        return data
    }

    /**
     * Get the item at the 1 string is the smallest item in the entire chord.
     *
     * @param chord Chord object
     * @return Returns true if the 1st string is the smallest item,
     * otherwise returns false.
     */
    fun firstStringLeast():Boolean {
        return frets[frets.size - 1] == getLeastFret()
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
    fun getWithFirstString():Int {
        var string = 1
        while (frets[frets.size - 1] == frets[frets.size - 1 - string]) {
            if(fingers!=null){
                if(fingers[fingers.size-1]== fingers[fingers.size-1-string]){
                    string+=1
                }
                return string
            }else{
                string += 1
            }

        }
        return string
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
    fun getMaxUnClosedString():Int {
        var string = frets.size
        while (frets[frets.size - string] == -1) {
            string -= 1
        }
        return string
    }


    override fun toString():String {
        return "Chord{" +
                "frets=" + Arrays.toString(frets) +
                ", fingers=" + Arrays.toString(fingers) +
                "}"
    }
}
