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

import java.util.List;

import org.apache.lucene.analysis.cn.smart.CharType;
import org.apache.lucene.analysis.cn.smart.Utility;
import org.apache.lucene.analysis.cn.smart.WordType;

public class HHMMSegmenter {

  private static WordDictionary wordDict = WordDictionary.getInstance();

  /**
   * 寻找sentence中所有可能的Token，最后再添加两个特殊Token，"始##始",
   * "末##末"，"始##始"Token的起始位置是-1,"末##末"Token的起始位置是句子的长度
   * 
   * @param sentence 输入的句子，不包含"始##始","末##末"等
   * @param coreDict 核心字典
   * @return 所有可能的Token
   * @see MultiTokenMap
   */
  private SegGraph createSegGraph(String sentence) {
    int i = 0, j;
    int length = sentence.length();
    int foundIndex;
    CharType[] charTypeArray = getCharTypes(sentence);
    StringBuffer wordBuf = new StringBuffer();
    SegToken token;
    int frequency = 0; // word的出现次数
    boolean hasFullWidth;
    WordType wordType;
    char[] charArray;

    SegGraph segGraph = new SegGraph();
    while (i < length) {
      hasFullWidth = false;
      switch (charTypeArray[i]) {
        case SPACE_LIKE:
          i++;
          break;
        case HANZI:
          j = i + 1;
          wordBuf.delete(0, wordBuf.length());
          // 不管单个汉字能不能构成词，都将单个汉字存到segGraph中去，否则会造成分此图断字
          wordBuf.append(sentence.charAt(i));
          charArray = new char[] { sentence.charAt(i) };
          frequency = wordDict.getFrequency(charArray);
          token = new SegToken(charArray, i, j, WordType.CHINESE_WORD,
              frequency);
          segGraph.addToken(token);

          foundIndex = wordDict.getPrefixMatch(charArray);
          while (j <= length && foundIndex != -1) {
            if (wordDict.isEqual(charArray, foundIndex) && charArray.length > 1) {
              // 就是我们要找的词， 也就是说找到了从i到j的一个成词SegToken，并且不是单字词
              frequency = wordDict.getFrequency(charArray);
              token = new SegToken(charArray, i, j, WordType.CHINESE_WORD,
                  frequency);
              segGraph.addToken(token);
            }

            while (j < length && charTypeArray[j] == CharType.SPACE_LIKE)
              j++;

            if (j < length && charTypeArray[j] == CharType.HANZI) {
              wordBuf.append(sentence.charAt(j));
              charArray = new char[wordBuf.length()];
              wordBuf.getChars(0, charArray.length, charArray, 0);
              // idArray作为前缀已经找到过(foundWordIndex!=-1),
              // 因此加长过后的idArray只可能出现在foundWordIndex以后,
              // 故从foundWordIndex之后开始查找
              foundIndex = wordDict.getPrefixMatch(charArray, foundIndex);
              j++;
            } else {
              break;
            }
          }
          i++;
          break;
        case FULLWIDTH_LETTER:
          hasFullWidth = true;
        case LETTER:
          j = i + 1;
          while (j < length
              && (charTypeArray[j] == CharType.LETTER || charTypeArray[j] == CharType.FULLWIDTH_LETTER)) {
            if (charTypeArray[j] == CharType.FULLWIDTH_LETTER)
              hasFullWidth = true;
            j++;
          }
          // 找到了从i到j的一个Token，类型为LETTER的字符串
          charArray = Utility.STRING_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          wordType = hasFullWidth ? WordType.FULLWIDTH_STRING : WordType.STRING;
          token = new SegToken(charArray, i, j, wordType, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        case FULLWIDTH_DIGIT:
          hasFullWidth = true;
        case DIGIT:
          j = i + 1;
          while (j < length
              && (charTypeArray[j] == CharType.DIGIT || charTypeArray[j] == CharType.FULLWIDTH_DIGIT)) {
            if (charTypeArray[j] == CharType.FULLWIDTH_DIGIT)
              hasFullWidth = true;
            j++;
          }
          // 找到了从i到j的一个Token，类型为NUMBER的字符串
          charArray = Utility.NUMBER_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          wordType = hasFullWidth ? WordType.FULLWIDTH_NUMBER : WordType.NUMBER;
          token = new SegToken(charArray, i, j, wordType, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        case DELIMITER:
          j = i + 1;
          // 标点符号的weight不用查了，选个最大的频率即可
          frequency = Utility.MAX_FREQUENCE;
          charArray = new char[] { sentence.charAt(i) };
          token = new SegToken(charArray, i, j, WordType.DELIMITER, frequency);
          segGraph.addToken(token);
          i = j;
          break;
        default:
          j = i + 1;
          // 把不认识的字符当作未知串看待，例如GB2312编码之外的字符，每个字符当作一个
          charArray = Utility.STRING_CHAR_ARRAY;
          frequency = wordDict.getFrequency(charArray);
          token = new SegToken(charArray, i, j, WordType.STRING, frequency);
          segGraph.addToken(token);
          i = j;
          break;
      }
    }

    // 为segGraph增加两个新Token： "始##始","末##末"
    charArray = Utility.START_CHAR_ARRAY;
    frequency = wordDict.getFrequency(charArray);
    token = new SegToken(charArray, -1, 0, WordType.SENTENCE_BEGIN, frequency);
    segGraph.addToken(token);

    // "末##末"
    charArray = Utility.END_CHAR_ARRAY;
    frequency = wordDict.getFrequency(charArray);
    token = new SegToken(charArray, length, length + 1, WordType.SENTENCE_END,
        frequency);
    segGraph.addToken(token);

    return segGraph;
  }

  /**
   * 为sentence中的每个字符确定唯一的字符类型
   * 
   * @see Utility.charType(char)
   * @param sentence 输入的完成句子
   * @return 返回的字符类型数组，如果输入为null，返回也是null
   */
  private static CharType[] getCharTypes(String sentence) {
    int length = sentence.length();
    CharType[] charTypeArray = new CharType[length];
    // 生成对应单个汉字的字符类型数组
    for (int i = 0; i < length; i++) {
      charTypeArray[i] = Utility.getCharType(sentence.charAt(i));
    }

    return charTypeArray;
  }

  public List<SegToken> process(String sentence) {
    SegGraph segGraph = createSegGraph(sentence);
    BiSegGraph biSegGraph = new BiSegGraph(segGraph);
    List<SegToken> shortPath = biSegGraph.getShortPath();
    return shortPath;
  }
}
