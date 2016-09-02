// Copyright 2015-2016 Stanford University
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package glade.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterUtils {
	public static boolean isNewlineOrTabCharacter(char c) {
		return c == '\n' || c == '\t';
	}
	
	public static boolean isNumeric(char c) {
		return c >= '0' && c <= '9';
	}
	
	public static boolean isAlphaUpperCase(char c) {
		return c >= 'A' && c <= 'Z';
	}
	
	public static boolean isAlphaLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}
	
	public static boolean isAlpha(char c) {
		return isAlphaUpperCase(c) || isAlphaLowerCase(c);
	}
	
	public static boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isNumeric(c);
	}
	
	public static boolean isNonAlphaNumeric(char c) {
		return !isNumeric(c) && !isAlphaUpperCase(c) && !isAlphaLowerCase(c);
	}
	
	public static boolean isSingleQuote(char c) {
		return c == 39;
	}
	
	public static boolean isDoubleQuote(char c) {
		return c == 34;
	}
	
	public static class CharacterGeneralization {
		public final Set<Character> triggers;
		public final List<Character> characters;
		public final List<Character> checks;
		public CharacterGeneralization(Collection<Character> triggers, Collection<Character> characters, Collection<Character> checks) {
			this.triggers = new HashSet<Character>(triggers);
			this.characters = new ArrayList<Character>(characters);
			this.checks = new ArrayList<Character>(checks);
		}
	}
	
	private static final List<Character> allCharacters = new ArrayList<Character>();
	private static final List<Character> numericCharacters = new ArrayList<Character>();
	private static final List<Character> alphaUpperCaseCharacters = new ArrayList<Character>();
	private static final List<Character> alphaLowerCaseCharacters = new ArrayList<Character>();
	private static final List<Character> nonAlphaNumericCharacters = new ArrayList<Character>();
	private static final List<Character> numericChecks = new ArrayList<Character>();
	private static final List<Character> alphaUpperCaseChecks = new ArrayList<Character>();
	private static final List<Character> alphaLowerCaseChecks = new ArrayList<Character>();
	private static final List<CharacterGeneralization> generalizations = new ArrayList<CharacterGeneralization>();
	static {
		for(char c=0; c<128; c++) {
			allCharacters.add(c);
			if(isNumeric(c)) {
				numericCharacters.add(c);
			} else if(isAlphaUpperCase(c)) {
				alphaUpperCaseCharacters.add(c);
			} else if(isAlphaLowerCase(c)) {
				alphaLowerCaseCharacters.add(c);
			} else {
				nonAlphaNumericCharacters.add(c);
			}
		}
		numericChecks.add('0');
		numericChecks.add('1');
		numericChecks.add('9');
		alphaUpperCaseChecks.add('E');
		alphaUpperCaseChecks.add('Q');
		alphaLowerCaseChecks.add('e');
		alphaLowerCaseChecks.add('q');
		for(char c : nonAlphaNumericCharacters) {
			List<Character> curC = Utils.getList(c);
			generalizations.add(new CharacterGeneralization(numericCharacters, curC, curC));
			generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, curC, curC));
			generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, curC, curC));
		}
		generalizations.add(new CharacterGeneralization(numericCharacters, numericCharacters, numericChecks));
		generalizations.add(new CharacterGeneralization(numericCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
		generalizations.add(new CharacterGeneralization(numericCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
		generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, numericCharacters, numericChecks));
		generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
		generalizations.add(new CharacterGeneralization(alphaUpperCaseCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
		generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, numericCharacters, numericChecks));
		generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, alphaUpperCaseCharacters, alphaUpperCaseChecks));
		generalizations.add(new CharacterGeneralization(alphaLowerCaseCharacters, alphaLowerCaseCharacters, alphaLowerCaseChecks));
	}
	
	public static List<Character> getAllCharacters() {
		return allCharacters;
	}
	
	public static List<Character> getNumericCharacters() {
		return numericCharacters;
	}
	
	public static List<Character> getAlphaUpperCaseCharacters() {
		return alphaUpperCaseCharacters;
	}
	
	public static List<Character> getAlphaLowerCaseCharacters() {
		return alphaLowerCaseCharacters;
	}
	
	public static List<Character> getNonAlphaNumericCharacters() {
		return nonAlphaNumericCharacters;
	}
	
	public static List<Character> getNumericChecks() {
		return numericChecks;
	}
	
	public static List<Character> getAlphaUpperCaseChecks() {
		return alphaUpperCaseChecks;
	}
	
	public static List<Character> getAlphaLowerCaseChecks() {
		return alphaLowerCaseChecks;
	}
	
	public static List<CharacterGeneralization> getGeneralizations() {
		return generalizations;
	}
}
