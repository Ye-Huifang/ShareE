package com.shareE.forum.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

	private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

	private static final String REPLACEMENT = "***";

	// root node
	private TrieNode rootNode = new TrieNode();

	@PostConstruct
	public void init() {
		try (
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		) {
			String keyword;
			while ((keyword = reader.readLine()) != null) {
				// add to TrieTree
				this.addKeyword(keyword);
			}
		} catch (IOException e) {
			logger.error("Failed to load sensitive-words text: " + e.getMessage());
		}
	}

	private void addKeyword(String keyword) {
		TrieNode tempNode = rootNode;
		for (int i = 0; i < keyword.length(); i++) {
			char c = keyword.charAt(i);
			TrieNode subNode = tempNode.getSubNode(c);

			if (subNode == null) {
				subNode = new TrieNode();
				tempNode.addSubNode(c, subNode);
			}

			// point to subNode
			tempNode = subNode;

			// set ending flag
			if (i == keyword.length() - 1) {
				tempNode.setKeywordEnd(true);
			}
		}
	}

	/**
	 * Filter sensitive word
	 * @param text to be filtered
	 * @return filtered text
	 */
	public String filter(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}

		// pointer 1
		TrieNode tempNode = rootNode;
		// pointer 2
		int begin = 0;
		// pointer 3
		int position = 0;
		// result
		StringBuilder sb = new StringBuilder();

		while (position < text.length()) {
			char c = text.charAt(position);

			// skip other characters other than letters like punctuation
			if (isSymbol(c) && position != text.length() - 1) {
				// if pointer 1 points to root node, the character should be return, move pointer 2
				if (tempNode == rootNode) {
					sb.append(c);
					begin++;
				}
				// whether the symbol is at the biginning or in the middle
				// move pointer 3
				position++;
				continue;
			}

			// check child node
			tempNode = tempNode.getSubNode(c);
			if (tempNode == null) {
				// the string starting from "begin" character is not a sensitive word
				sb.append(text.charAt(begin));
				// move pointer 3 and pointer 2
				position = ++begin;
				// point to root node again
				tempNode = rootNode;
			} else if (tempNode.isKeywordEnd) {
				// find sensitive word, replace the string from begin to position with REPLACEMENT
				sb.append(REPLACEMENT);
				begin = ++position;
			} else {
				// check next character
				if (position < text.length() - 1) {
					position++;
				} else {
					position = begin;
				}
			}
		}

		sb.append(text.substring(begin));

		return sb.toString();
	}

	private boolean isSymbol(Character c) {
		// 0x2E80~0x9FFF: asian language
		return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
	}

	private class TrieNode {

		// flag to annotate the ending of sensitive word
		private boolean isKeywordEnd = false;

		// key: child character, value: child node
		private Map<Character, TrieNode> subNodes = new HashMap<>();

		public boolean isKeywordEnd() {
			return isKeywordEnd;
		}

		public void setKeywordEnd(boolean keywordEnd) {
			isKeywordEnd = keywordEnd;
		}

		// add child node
		public void addSubNode(Character c, TrieNode node) {
			subNodes.put(c, node);
		}

		// get child node
		public TrieNode getSubNode(Character c) {
			return subNodes.get(c);
		}
	}

}
