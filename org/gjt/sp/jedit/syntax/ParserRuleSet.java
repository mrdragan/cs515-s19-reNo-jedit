/*
 * ParserRuleSet.java - A set of parser rules
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999 mike dillon
 * Portions copyright (C) 2001, 2002 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.syntax;

//{{{ Imports
import java.util.*;
import java.util.regex.Pattern;
//}}}

/**
 * A set of parser rules.
 * @author mike dillon
 * @version $Id$
 */
public class ParserRuleSet
{
	//{{{ getStandardRuleSet() method
	/**
	 * Returns a parser rule set that highlights everything with the
	 * specified token type.
	 * @param id The token type
	 */
	public static ParserRuleSet getStandardRuleSet(byte id)
	{
		return standard[id];
	} //}}}

	//{{{ ParserRuleSet constructor
	public ParserRuleSet(String modeName, String setName)
	{
		this.data.modeName = modeName;
		this.data.setName = setName;
		data.ruleMap = new HashMap<Character, List<ParserRule>>();
		data.imports = new ArrayList<ParserRuleSet>();
	} //}}}

	//{{{ getModeName() method
	public String getModeName()
	{
		return data.modeName;
	} //}}}

	//{{{ getSetName() method
	public String getSetName()
	{
		return data.setName;
	} //}}}

	//{{{ getName() method
	public String getName()
	{
		return data.modeName + "::" + data.setName;
	} //}}}

	//{{{ getProperties() method
	public Hashtable<String, String> getProperties()
	{
		return data.props;
	} //}}}

	//{{{ setProperties() method
	public void setProperties(Hashtable<String, String> props)
	{
		this.data.props = props;
		data._noWordSep = null;
	} //}}}

	//{{{ resolveImports() method
	/**
	 * Resolves all rulesets added with {@link #addRuleSet(ParserRuleSet)}.
	 * @since jEdit 4.2pre3
	 */
	public void resolveImports()
	{
		for (ParserRuleSet ruleset : data.imports)
		{
			if (!ruleset.data.imports.isEmpty())
			{
				//prevent infinite recursion
				ruleset.data.imports.remove(this);
				ruleset.resolveImports();
			}

			for (List<ParserRule> rules : ruleset.data.ruleMap.values())
			{
				for (ParserRule rule : rules)
				{
					addRule(rule);
				}
			}

			if (ruleset.data.keywords != null)
			{
				if (data.keywords == null)
					data.keywords = new KeywordMap(data.ignoreCase);
				data.keywords.add(ruleset.data.keywords);
			}
		}
		data.imports.clear();
	} //}}}

	//{{{ addRuleSet() method
	/**
	 * Adds all rules contained in the given ruleset.
	 * @param ruleset The ruleset
	 * @since jEdit 4.2pre3
	 */
	public void addRuleSet(ParserRuleSet ruleset)
	{
		data.imports.add(ruleset);
	} //}}}

	//{{{ addRule() method
	public void addRule(ParserRule r)
	{
		data.ruleCount++;
		Character[] keys;
		if (null == r.upHashChars)
		{
			keys = new Character[1];
			if ((null == r.upHashChar) || (0 >= r.upHashChar.length))
			{
				keys[0] = null;
			}
			else
			{
				keys[0] = Character.valueOf(r.upHashChar[0]);
			}
		}
		else
		{
			keys = new Character[r.upHashChars.length];
			int i = 0;
			for (char upHashChar : r.upHashChars)
			{
				keys[i++] = upHashChar;
			}
		}
		for (Character key : keys)
		{
			List<ParserRule> rules = data.ruleMap.get(key);
			if (null == rules)
			{
				rules = new ArrayList<ParserRule>();
				data.ruleMap.put(key,rules);
			}
			rules.add(r);
		}
	} //}}}

	//{{{ getRules() method
	public List<ParserRule> getRules(Character key)
	{
		List<ParserRule> rulesForNull = data.ruleMap.get(null);
		boolean emptyForNull = rulesForNull == null || rulesForNull.isEmpty();
		Character upperKey = key == null ? null : Character.valueOf(Character.toUpperCase(key.charValue()));
		List<ParserRule> rulesForKey = upperKey == null ? null : data.ruleMap.get(upperKey);
		boolean emptyForKey = rulesForKey == null || rulesForKey.isEmpty();
		if (emptyForNull && emptyForKey)
		{
			return Collections.emptyList();
		}
		else if (emptyForKey)
		{
			return rulesForNull;
		}
		else if (emptyForNull)
		{
			return rulesForKey;
		}
		else
		{
			int size = rulesForNull.size() + rulesForKey.size();
			List<ParserRule> mixed = new ArrayList<ParserRule>(size);
			mixed.addAll(rulesForKey);
			mixed.addAll(rulesForNull);
			return mixed;
		}
	} //}}}

	//{{{ getRuleCount() method
	public int getRuleCount()
	{
		return data.ruleCount;
	} //}}}

	//{{{ getTerminateChar() method
	/**
	 * Returns the number of chars that can be read before the rule parsing stops.
	 *
	 * @return a number of chars or -1 (default value) if there is no limit
	 */
	public int getTerminateChar()
	{
		return data.terminateChar;
	} //}}}

	//{{{ setTerminateChar() method
	public void setTerminateChar(int atChar)
	{
		data.terminateChar = (atChar >= 0) ? atChar : -1;
	} //}}}

	//{{{ getIgnoreCase() method
	public boolean getIgnoreCase()
	{
		return data.ignoreCase;
	} //}}}

	//{{{ setIgnoreCase() method
	public void setIgnoreCase(boolean b)
	{
		data.ignoreCase = b;
	} //}}}

	//{{{ getKeywords() method
	public KeywordMap getKeywords()
	{
		return data.keywords;
	} //}}}

	//{{{ setKeywords() method
	public void setKeywords(KeywordMap km)
	{
		data.keywords = km;
		data._noWordSep = null;
	} //}}}

	//{{{ getHighlightDigits() method
	public boolean getHighlightDigits()
	{
		return data.highlightDigits;
	} //}}}

	//{{{ setHighlightDigits() method
	public void setHighlightDigits(boolean highlightDigits)
	{
		this.data.highlightDigits = highlightDigits;
	} //}}}

	//{{{ getDigitRegexp() method
	public Pattern getDigitRegexp()
	{
		return data.digitRE;
	} //}}}

	//{{{ setDigitRegexp() method
	public void setDigitRegexp(Pattern digitRE)
	{
		this.data.digitRE = digitRE;
	} //}}}

	//{{{ getEscapeRule() method
	public ParserRule getEscapeRule()
	{
		return data.escapeRule;
	} //}}}

	//{{{ setEscapeRule() method
	public void setEscapeRule(ParserRule escapeRule)
	{
		this.data.escapeRule = escapeRule;
	} //}}}

	//{{{ getDefault() method
	public byte getDefault()
	{
		return data.defaultToken;
	} //}}}

	//{{{ setDefault() method
	public void setDefault(byte def)
	{
		data.defaultToken = def;
	} //}}}

	//{{{ getNoWordSep() method
	public String getNoWordSep()
	{
		if(data._noWordSep == null)
		{
			data._noWordSep = data.noWordSep;
			if(data.noWordSep == null)
				data.noWordSep = "";
			if(data.keywords != null)
				data.noWordSep += data.keywords.getNonAlphaNumericChars();
		}
		return data.noWordSep;
	} //}}}

	//{{{ setNoWordSep() method
	public void setNoWordSep(String noWordSep)
	{
		this.data.noWordSep = noWordSep;
		data._noWordSep = null;
	} //}}}

	//{{{ isBuiltIn() method
	/**
	 * Returns if this is a built-in ruleset.
	 * @since jEdit 4.2pre1
	 */
	public boolean isBuiltIn()
	{
		return data.builtIn;
	} //}}}

	//{{{ toString() method
	@Override
	public String toString()
	{
		return getClass().getName() + '[' + data.modeName + "::" + data.setName + ']';
	} //}}}

	//{{{ Private members
	private static final ParserRuleSet[] standard;

	static
	{
		standard = new ParserRuleSet[Token.ID_COUNT];
		for(byte i = 0; i < Token.ID_COUNT; i++)
		{
			standard[i] = new ParserRuleSet(null,null);
			standard[i].setDefault(i);
			standard[i].data.builtIn = true;
		}
	}

	
private ParserRuleSetData data = new ParserRuleSetData(-1, true);
}
