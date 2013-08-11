/**
 * Copyright 2009 www.imdict.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.analysis.cn.smart.hhmm;

import org.apache.lucene.analysis.cn.smart.WordType;

public class SegToken {
  public char[] charArray;

  public int startOffset;

  public int endOffset;

  public WordType wordType;

  public int weight;

  public int index;

  public SegToken(String word, int start, int end, WordType wordType, int weight) {
    this.charArray = word.toCharArray();
    this.startOffset = start;
    this.endOffset = end;
    this.wordType = wordType;
    this.weight = weight;
  }

  public SegToken(char[] idArray, int start, int end, WordType wordType,
      int weight) {
    this.charArray = idArray;
    this.startOffset = start;
    this.endOffset = end;
    this.wordType = wordType;
    this.weight = weight;
  }

  // public String toString() {
  // return String.valueOf(charArray) + "/s(" + startOffset + ")e("
  // + endOffset + ")/w(" + weight + ")t(" + wordType + ")";
  // }

  /**
   * 判断两个Token相等的充要条件是他们的起始位置相等，因为这样他们的原句中的内容一样，
   * 而pos与weight都可以从词典中查到多个，可以用一对多的方法表示，因此只需要一个Token
   * 
   * @param t
   * @return
   */
  // public boolean equals(RawToken t) {
  // return this.startOffset == t.startOffset
  // && this.endOffset == t.endOffset;
  // }
}
