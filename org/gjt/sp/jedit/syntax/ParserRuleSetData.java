package org.gjt.sp.jedit.syntax;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ParserRuleSetData {
	public String modeName;
	public String setName;
	public Hashtable<String, String> props;
	public KeywordMap keywords;
	public int ruleCount;
	public Map<Character, List<ParserRule>> ruleMap;
	public List<ParserRuleSet> imports;
	/**
	 * The number of chars that can be read before the parsing stops.
	 * &lt;TERMINATE AT_CHAR="1" /&gt;
	 */
	public int terminateChar;
	public boolean ignoreCase;
	public byte defaultToken;
	public ParserRule escapeRule;
	public boolean highlightDigits;
	public Pattern digitRE;
	public String _noWordSep;
	public String noWordSep;
	public boolean builtIn;

	public ParserRuleSetData(int terminateChar, boolean ignoreCase) {
		this.terminateChar = terminateChar;
		this.ignoreCase = ignoreCase;
	}
}