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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.hhmm.SegToken;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class ChineseWordFilter extends TokenFilter {

  /**
   * 分词主程序，WordTokenizer初始化时加载。
   */
  private WordSegmenter wordSegmenter;
  private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
  private OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
  boolean isIncremental;
  
  Queue<SegToken> tokens;

  /**
   * 设计上是SentenceTokenizer的下一处理层。将SentenceTokenizer的句子读出，
   * 利用HHMMSegment主程序将句子分词，然后将分词结果返回。
   * 
   * @param in 句子的Token
   * @param smooth 平滑函数
   * @param dataPath 装载核心字典与二叉字典的目录
   * @see init()
   */
  public ChineseWordFilter(TokenStream in, WordSegmenter wordSegmenter) {
    super(in);
    this.wordSegmenter = wordSegmenter;
  }

  @Override
  public boolean incrementToken() throws IOException {
	if(isIncremental = input.incrementToken()) {
	  String sentence = new String(termAttr.buffer(), 0, termAttr.length());
	  int startOffset = offsetAttr.startOffset();
	  List<SegToken> list = wordSegmenter.segmentSentence(sentence, 1);
	  for(SegToken token : list) {
		  token.startOffset += startOffset;
		  token.endOffset += startOffset;
	  }
	  tokens.addAll(list);
	}
	if (!isIncremental && tokens.size() == 0) return false;
	if(tokens.size() > 0) {
		SegToken token = tokens.poll();
		termAttr.setEmpty().copyBuffer(token.charArray, 0, token.charArray.length);
		offsetAttr.setOffset(token.startOffset, token.endOffset);
	}
	return true;
  }
  
  @Override
  public void reset() throws IOException {
    super.reset();
    tokens = new LinkedList<SegToken>();
  }

}
