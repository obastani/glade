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

package glade.grammar.synthesize;

import glade.grammar.GrammarUtils;
import glade.grammar.GrammarUtils.AlternationNode;
import glade.grammar.GrammarUtils.ConstantNode;
import glade.grammar.GrammarUtils.MultiAlternationNode;
import glade.grammar.GrammarUtils.MultiConstantNode;
import glade.grammar.GrammarUtils.Node;
import glade.grammar.GrammarUtils.NodeMerges;
import glade.grammar.GrammarUtils.RepetitionNode;
import glade.util.Log;
import glade.util.OracleUtils.DiscriminativeOracle;
import glade.util.Utils.MultivalueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MergesSynthesis {
	public static NodeMerges getMergesMultiple(List<Node> roots, DiscriminativeOracle oracle) {
		NodeMerges merges = new NodeMerges();
		NodeMerges processed = new NodeMerges();
		for(Node first : roots) {
			for(Node second : roots) {
				if(processed.contains(first, second)) {
					continue;
				}
				processed.add(first, second);
				merges.addAll(getMergesSingle(first, second, oracle));
			}
		}
		return merges;
	}
	
	public static NodeMerges getMergesSingle(Node firstRoot, Node secondRoot, DiscriminativeOracle oracle) {
		NodeMerges merges = new NodeMerges();
		NodeMerges processedMerges = new NodeMerges();
		MultivalueMap<Node,String> pairFirst = getAllExamples(firstRoot);
		MultivalueMap<Node,String> pairSecond = getAllExamples(secondRoot);
		for(Node first : GrammarUtils.getAllNodes(firstRoot)) {
			for(Node second : GrammarUtils.getAllNodes(secondRoot)) {
				if(processedMerges.contains(first, second)) {
					continue;
				}
				processedMerges.add(first, second);
				getMergesHelper(first, second, pairFirst, pairSecond, oracle, merges);
			}
		}
		return merges;
	}
	
	private static void getMergesHelper(Node first, Node second, MultivalueMap<Node,String> firstExampleMap, MultivalueMap<Node,String> secondExampleMap, DiscriminativeOracle oracle, NodeMerges merges) {
		if(first.equals(second)) {
			return;
		}
		if(!(first instanceof RepetitionNode) || !(second instanceof RepetitionNode)) {
			return;
		}
		Node firstRep = ((RepetitionNode)first).rep;
		Node secondRep = ((RepetitionNode)second).rep;
		if(firstRep instanceof ConstantNode || firstRep instanceof MultiConstantNode || secondRep instanceof ConstantNode || secondRep instanceof MultiConstantNode) {
			return;
		}
		if(isMultiAlternationRepetitionConstant(firstRep, true) || isMultiAlternationRepetitionConstant(secondRep, true)) {
			return;
		}
		List<String> firstExamplesSimple = new ArrayList<String>();
		List<String> secondExamplesSimple = new ArrayList<String>();
		firstExamplesSimple.add(secondRep.getData().example + secondRep.getData().example);
		secondExamplesSimple.add(firstRep.getData().example + firstRep.getData().example);
		if(!GrammarSynthesis.getCheck(oracle, firstRep.getData().context, firstExamplesSimple) || !GrammarSynthesis.getCheck(oracle, secondRep.getData().context, secondExamplesSimple)) {
			return;
		}
		List<String> firstExamples = new ArrayList<String>();
		for(String example : secondExampleMap.get(secondRep)) {
			firstExamples.add(example + example);
		}
		List<String> secondExamples = new ArrayList<String>();
		for(String example : firstExampleMap.get(firstRep)) {
			secondExamples.add(example + example);
		}
		if((isStructuredExample(firstRep) && isStructuredExample(secondRep))
				|| (GrammarSynthesis.getCheck(oracle, firstRep.getData().context, firstExamples) && GrammarSynthesis.getCheck(oracle, secondRep.getData().context, secondExamples))) {
			Log.info("MERGE NODE FIRST:\n" + firstRep.getData().context.pre + " ## " + firstRep.getData().example + " ## " + firstRep.getData().context.post);
			Log.info("MERGE NODE SECOND:\n" + secondRep.getData().context.pre + " ## " + secondRep.getData().example + " ## " + secondRep.getData().context.post);
			merges.add(firstRep, secondRep);
		}
	}
	
	private static void getAllExamplesHelper(Node node, MultivalueMap<Node,String> examples) {
		for(Node child : node.getChildren()) {
			getAllExamplesHelper(child, examples);
		}
		if(node instanceof RepetitionNode) {
			RepetitionNode repNode = (RepetitionNode)node;
			for(String example : examples.get(repNode.start)) {
				examples.add(repNode, example + repNode.rep.getData().example + repNode.end.getData().example);
			}
			for(String example : examples.get(repNode.rep)) {
				examples.add(repNode, repNode.start.getData().example + example + repNode.end.getData().example);
			}
			for(String example : examples.get(repNode.end)) {
				examples.add(repNode, repNode.start.getData().example + repNode.rep.getData().example + example);
			}
		} else if(node instanceof MultiConstantNode) {
			MultiConstantNode mconstNode = (MultiConstantNode)node;
			String example = mconstNode.getData().example;
			for(int i=0; i<mconstNode.characterChecks.size(); i++) {
				String pre = example.substring(0, i);
				String post = example.substring(i+1);
				for(char c : mconstNode.characterChecks.get(i)) {
					examples.add(mconstNode, pre + c + post);
				}
			}
		} else if(node instanceof AlternationNode) {
			AlternationNode altNode = (AlternationNode)node;
			for(String example : examples.get(altNode.first)) {
				examples.add(altNode, example);
			}
			for(String example : examples.get(altNode.second)) {
				examples.add(altNode, example);
			}
		} else if(node instanceof ConstantNode) {
			ConstantNode constNode = (ConstantNode)node;
			examples.add(constNode, constNode.getData().example);
		} else if(node instanceof MultiAlternationNode) {
			MultiAlternationNode maltNode = (MultiAlternationNode)node;
			for(Node child : maltNode.getChildren()) {
				for(String example : examples.get(child)) {
					examples.add(maltNode, example);
				}
			}
		} else {
			throw new RuntimeException("Invalid node type: " + node.getClass().getName());
		}
	}
	
	private static MultivalueMap<Node,String> getAllExamples(Node root) {
		MultivalueMap<Node,String> allExamples = new MultivalueMap<Node,String>();
		getAllExamplesHelper(root, allExamples);
		return allExamples;
	}

	
	private static boolean isMultiAlternationRepetitionConstant(Node node, boolean isParentRep) {
		return GrammarSynthesis.getMultiAlternationRepetitionConstantChildren(node, isParentRep).hasT();
	}
	
	private static boolean isStructuredExample(Node node) {
		for(Node descendant : GrammarUtils.getDescendants(node)) {
			if(!(descendant instanceof MultiConstantNode)) {
				continue;
			}
			MultiConstantNode mconstNode = (MultiConstantNode)descendant;
			for(Set<Character> checks : mconstNode.characterChecks) {
				if(checks.size() == 1) {
					return true;
				}
			}
		}
		return false;
	}
}
