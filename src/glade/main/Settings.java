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

package glade.main;

import glade.constants.program.FlexData;
import glade.constants.program.GrepData;
import glade.constants.program.PythonData;
import glade.constants.program.PythonWrappedData;
import glade.constants.program.SedData;
import glade.constants.program.XmlData;
import glade.grammar.fuzz.GrammarFuzzer.CombinedMutationSampler;
import glade.grammar.fuzz.GrammarFuzzer.GrammarMutationSampler;
import glade.grammar.fuzz.GrammarFuzzer.GrammarSampler;
import glade.grammar.fuzz.GrammarFuzzer.SampleParameters;
import glade.main.ProgramDataUtils.ProgramData;
import glade.main.ProgramDataUtils.ProgramExamples;

import java.util.ArrayList;
import java.util.Random;

public class Settings {
	public static class GrammarSettings {
		public final String grammarPath;
		public GrammarSettings(String grammarPath) {
			this.grammarPath = grammarPath;
		}
	}
	
	public static class ProgramSettings {
		public final ProgramData data;
		public final ProgramExamples examples;
		public final String name;
		public ProgramSettings(ProgramData data, ProgramExamples examples, String name) {
			this.data = data;
			this.examples = examples;
			this.name = name;
		}
	}
	
	public static class FuzzSettings {
		public final int numMutations;
		public final int numIters;
		public final int maxLength;
		public final SampleParameters sample;
		public final Fuzzer fuzzer;
		public FuzzSettings(int numMutations, int numIters, int maxLength, SampleParameters sample, Fuzzer fuzzer) {
			this.numMutations = numMutations;
			this.numIters = numIters;
			this.maxLength = maxLength;
			this.sample = sample;
			this.fuzzer = fuzzer;
		}
	}
	
	public static enum Program {
		XML, GREP, SED, FLEX, PYTHON, PYTHON_WRAPPED;
		public ProgramSettings getSettings() {
			switch(this) {
			case XML:
				return new ProgramSettings(XmlData.XML_DATA, XmlData.XML_EXAMPLES, XmlData.XML_NAME);
			case PYTHON:
				return new ProgramSettings(PythonData.PYTHON_DATA, PythonData.PYTHON_EXAMPLES, PythonData.PYTHON_NAME);
			case PYTHON_WRAPPED:
				return new ProgramSettings(PythonWrappedData.PYTHON_WRAPPED_DATA, PythonWrappedData.PYTHON_EXAMPLES, PythonWrappedData.PYTHON_WRAPPED_NAME);
			case GREP:
				return new ProgramSettings(GrepData.GREP_DATA, GrepData.GREP_EXAMPLES, GrepData.GREP_NAME);
			case SED:
				return new ProgramSettings(SedData.SED_DATA, SedData.SED_EXAMPLES, SedData.SED_NAME);
			case FLEX:
				return new ProgramSettings(FlexData.FLEX_DATA, FlexData.FLEX_EXAMPLES, FlexData.FLEX_NAME);
			default:
				throw new RuntimeException("Invalid settings!");
			}
		}
	}
	
	public static enum Fuzzer {
		NONE, EMPTY, TEST, TRAIN, NAIVE, GRAMMAR, GRAMMAR_NO_SEED, COMBINED;
		public Iterable<String> getSamples(ProgramSettings program, GrammarSettings grammar, FuzzSettings fuzz, Random random) {
			switch(this) {
			case NONE:
				return new ArrayList<String>();
			case EMPTY:
				return program.examples.getEmptyExamples();
			case TRAIN:
				return program.examples.getTrainExamples();
			case GRAMMAR:
				return new GrammarMutationSampler(GrammarDataUtils.loadAllGrammar(grammar.grammarPath, program.name), fuzz.sample, fuzz.maxLength, fuzz.numMutations, random);
			case GRAMMAR_NO_SEED:
				return new GrammarSampler(GrammarDataUtils.loadAllGrammar(grammar.grammarPath, program.name), fuzz.sample, random);
			case COMBINED:
				Iterable<String> grammarMutationSampler = new GrammarMutationSampler(GrammarDataUtils.loadAllGrammar(grammar.grammarPath, program.name), fuzz.sample, fuzz.maxLength, fuzz.numMutations, random);
				return new CombinedMutationSampler(grammarMutationSampler, fuzz.numMutations, random);
			default:
				throw new RuntimeException("Invalid settings!");
			}
		}
	}
}
