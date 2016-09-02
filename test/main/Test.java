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

package main;

import glade.grammar.fuzz.GrammarFuzzer.GrammarMutationSampler;
import glade.grammar.fuzz.GrammarFuzzer.SampleParameters;
import glade.grammar.GrammarUtils.Grammar;
import glade.grammar.synthesize.GrammarSynthesis;
import glade.util.Log;
import glade.util.OracleUtils.DiscriminativeOracle;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Test {
	// encodes language of matching parentheses over \Sigma = { '(', ')', '[', ']', '{', '}' }
	public static class TestOracle implements DiscriminativeOracle {
		// returns true if query is a string of matching parentheses
		public boolean query(String query) {
			Stack<Character> stack = new Stack<Character>();
			for(int i=0; i<query.length(); i++) {
				char c = query.charAt(i);
				if(c == '(' || c == '[' || c == '{') {
					// handle open parentheses
					stack.push(c);
				} else if(c == ')' || c == ']' || c == '}') {
					// handle closed parentheses
					if(stack.isEmpty()) {
						return false; 
					}
					char d = stack.pop();
					if((d == '(' && c != ')') || (d == '[' && c != ']') || (d == '{' && c != '}')) {
						return false;
					}
				} else {
					return false;
				}
			}
			return stack.isEmpty();
		}
	}

	public static List<String> getTrainExamples() {
		return Arrays.asList(new String[]{"{([][])([][])}{[()()][()()]}"});
	}

	public static SampleParameters getSampleParameters() {
		return new SampleParameters(new double[]{0.2, 0.2, 0.2, 0.4}, // (multinomial) distribution of repetitions
				0.8,                                          // probability of using recursive production
				0.1,                                          // probability of a uniformly random character (vs. a special character)
				100);                                         // max number of steps before timing out
	}


	public static void main(String[] args) {
		// number of samples to print
		int numSamples = 10;

		// log settings
		String logName = "log.txt";
		boolean verbose = true;

		// seed for random number generator
		int seed = 0;

		// fuzzer settings
		int maxLen = 1000;                                     // max length of a sample
		int numMut = 20;                                       // number of mutations to seed input
		SampleParameters sampleParams = getSampleParameters(); // sampling parameters

		// enable logging
		Log.init(logName, verbose);

		// input: query oracle
		DiscriminativeOracle oracle = new TestOracle();

		// input: (positive) training examples
		List<String> examples = getTrainExamples();

		// learn grammar
		Grammar grammar = GrammarSynthesis.getGrammarMultiple(examples, oracle);

		// fuzz using grammar
		Iterable<String> samples = new GrammarMutationSampler(grammar, sampleParams, maxLen, numMut, new Random(seed));

		int pass = 0;
		int count = 0;
		for(String sample : samples) {
			Log.info("SAMPLE: " + sample);
			if(oracle.query(sample)) {
				Log.info("PASS");
				pass++;
			} else {
				Log.info("FAIL");
			}
			Log.info("");
			count++;
			if(count >= numSamples) {
				break;
			}
		}
		Log.info("PASS RATE: " + (float)pass/numSamples);
	}
}