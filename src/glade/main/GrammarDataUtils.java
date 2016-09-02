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

import glade.grammar.GrammarSerializer;
import glade.grammar.GrammarUtils.Grammar;
import glade.grammar.GrammarUtils.Node;
import glade.grammar.synthesize.GrammarSynthesis;
import glade.main.ProgramDataUtils.ProgramData;
import glade.main.ProgramDataUtils.ProgramExamples;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrammarDataUtils {
	public static void clearGrammarDirectory(String grammarPath, String name) {
		File dir = new File(grammarPath + File.separator + name);
		if(dir.exists()) {
			for(File file : dir.listFiles()) {
				file.delete();
			}
		}
	}
	
	private static String getGrammarFilename(String grammarPath, String name, int index) {
		return grammarPath + File.separator + name + File.separator + "example" + index + ".gram";
	}
	
	private static String getAllGrammarFilename(String grammarPath, String name) {
		return grammarPath + File.separator + name + File.separator + "all.gram";
	}
	
	public static void saveGrammar(String filename, Grammar grammar) {
		try {
			File file = new File(filename);
			File parent = file.getParentFile();
			if(parent != null) {
				parent.mkdirs();
			}
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
			GrammarSerializer.serialize(grammar, dos);
		} catch(IOException e) {
			throw new RuntimeException("Error opening file during grammar save: " + filename, e);
		} catch(RuntimeException e) {
			throw new RuntimeException(e.getMessage() + "\nError serializing grammar: " + filename, e);
		}
	}
	
	public static Grammar loadGrammar(String filename) {
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filename));
			return GrammarSerializer.deserializeNodeWithMerges(dis);
		} catch(IOException e) {
			throw new RuntimeException("Error opening grammar file during grammar load: " + filename, e);
		} catch(RuntimeException e) {
			throw new RuntimeException(e.getMessage() + "\nError deserializing grammar: " + filename, e);
		}
	}
	
	public static void saveGrammar(String grammarPath, String name, int index, Grammar grammar) {
		saveGrammar(getGrammarFilename(grammarPath, name, index), grammar);
	}
	
	public static void saveAllGrammar(String grammarPath, String name, Grammar grammar) {
		saveGrammar(getAllGrammarFilename(grammarPath, name), grammar);
	}
	
	public static Grammar loadGrammar(String grammarPath, String name, int index) {
		return loadGrammar(getGrammarFilename(grammarPath, name, index));
	}
	
	public static Grammar loadAllGrammar(String grammarPath, String name) {
		return loadGrammar(getAllGrammarFilename(grammarPath, name));
	}

	public static void learnGrammar(String grammarPath, String name, ProgramData data, ProgramExamples examples, int index) {
		String example = examples.getTrainExamples().get(index);
		Grammar grammar = GrammarSynthesis.getGrammarSingle(example, ProgramDataUtils.getQueryOracle(data));
		saveGrammar(grammarPath, name, index, grammar);
	}
	
	public static void mergeGrammar(String grammarPath, String name, ProgramData data, ProgramExamples examples) {
		List<Node> roots = new ArrayList<Node>();
		for(int i=0; i<examples.getTrainExamples().size(); i++) {
			roots.add(GrammarDataUtils.loadGrammar(grammarPath, name, i).node);
		}
		Grammar grammar = GrammarSynthesis.getGrammarMultipleFromRoots(roots, ProgramDataUtils.getQueryOracle(data));
		saveAllGrammar(grammarPath, name, grammar);
	}
	
	public static void learnAllGrammar(String grammarPath, String name, ProgramData data, ProgramExamples examples) {
		clearGrammarDirectory(grammarPath, name);
		for(int i=0; i<examples.getTrainExamples().size(); i++) {
			learnGrammar(grammarPath, name, data, examples, i);
		}
		mergeGrammar(grammarPath, name, data, examples);
	}
}
