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

package org.apache.lucene.analysis.cn.smart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * 
 * 包含一个完整句子的Token，从文件中读出，是下一步分词的对象
 * 
 */
public class SentenceTokenizer extends Tokenizer {

  /**
   * 用来切断句子的标点符号 。，！？；,!?;
   */
  public final static String PUNCTION = "。，！？；,!?;";

  private StringBuilder buffer = new StringBuilder();

  private BufferedReader bufferInput;

  private int tokenStart = 0, tokenEnd = 0;
  
  private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
  private OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);

  public SentenceTokenizer(Reader reader) {
	super(reader);
    
  }
  
  @Override
  public void reset() {
	bufferInput = new BufferedReader(this.input, 2048);
	tokenEnd = 0;
	tokenStart = 0;
  }

  public void close() throws IOException {
    bufferInput.close();
  }

@Override
public boolean incrementToken() throws IOException {
	buffer.setLength(0);
    int ci;
    char ch, pch;
    boolean atBegin = true;
    tokenStart = tokenEnd;
    ci = bufferInput.read();
    ch = (char) ci;

    while (true) {
      if (ci == -1) {
        break;
      } else if (PUNCTION.indexOf(ch) != -1) {
        // 找到了句子末尾
        buffer.append(ch);
        tokenEnd++;
        break;
      } else if (atBegin && Utility.SPACES.indexOf(ch) != -1) {
        tokenStart++;
        tokenEnd++;
        ci = bufferInput.read();
        ch = (char) ci;
      } else {
        buffer.append(ch);
        atBegin = false;
        tokenEnd++;
        pch = ch;
        ci = bufferInput.read();
        ch = (char) ci;
        // 如果碰上了两个连续的skip字符，例如两个回车，两个空格或者，
        // 一个回车，一个空格等等，将其视为句子结束，以免句子太长而内存不足
        if (Utility.SPACES.indexOf(ch) != -1
            && Utility.SPACES.indexOf(pch) != -1) {
          // buffer.append(ch);
          tokenEnd++;
          break;
        }
      }
    }
    if (buffer.length() == 0)
      return false;
    else {
      clearAttributes();
      termAttr.setEmpty().append(buffer);
      termAttr.setLength(buffer.length());
      offsetAttr.setOffset(correctOffset(tokenStart), correctOffset(tokenEnd));
      return true;
    }
}

}
