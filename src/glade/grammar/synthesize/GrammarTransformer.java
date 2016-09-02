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

import glade.grammar.GrammarUtils.AlternationNode;
import glade.grammar.GrammarUtils.ConstantNode;
import glade.grammar.GrammarUtils.Context;
import glade.grammar.GrammarUtils.MultiAlternationNode;
import glade.grammar.GrammarUtils.MultiConstantNode;
import glade.grammar.GrammarUtils.Node;
import glade.grammar.GrammarUtils.RepetitionNode;
import glade.util.CharacterUtils;
import glade.util.Log;
import glade.util.CharacterUtils.CharacterGeneralization;
import glade.util.OracleUtils.DiscriminativeOracle;
import glade.util.Utils.Maybe;
import glade.util.Utils.MultivalueMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GrammarTransformer {
	public static Node getTransform(Node node, DiscriminativeOracle oracle) {
		Node transformFlatten = getTransform(node, new FlattenTransformer());
		return getTransform(transformFlatten, new ConstantTransformer(oracle, getMultiAlternationRepetitionConstantNodes(transformFlatten)));
	}
	
	private static interface NodeTransformer {
		public abstract Node transformConstant(ConstantNode node);
		public abstract Node transformMultiConstant(MultiConstantNode node);
		public abstract Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond);
		public abstract Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd);
		public abstract Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren);
	}
	
	private static Node getTransform(Node node, NodeTransformer transformer) {
		if(node instanceof ConstantNode) {
			return transformer.transformConstant((ConstantNode)node);
		} else if(node instanceof MultiConstantNode) {
			return transformer.transformMultiConstant((MultiConstantNode)node);
		} else if(node instanceof AlternationNode) {
			AlternationNode altNode = (AlternationNode)node;
			Node newFirst = getTransform(altNode.first, transformer);
			Node newSecond = getTransform(altNode.second, transformer);
			return transformer.transformAlternation(altNode, newFirst, newSecond);
		} else if(node instanceof MultiAlternationNode) {
			List<Node> newChildren = new ArrayList<Node>();
			for(Node child : node.getChildren()) {
				newChildren.add(getTransform(child, transformer));
			}
			return transformer.transformMultiAlternation((MultiAlternationNode)node, newChildren);
		} else if(node instanceof RepetitionNode) {
			RepetitionNode repNode = (RepetitionNode)node;
			Node newStart = getTransform(repNode.start, transformer);
			Node newRep = getTransform(repNode.rep, transformer);
			Node newEnd = getTransform(repNode.end, transformer);
			return transformer.transformRepetition(repNode, newStart, newRep, newEnd);
		} else {
			throw new RuntimeException("Invalid node type: " + node.getClass().getName());
		}
	}
	
	private static MultiConstantNode generalizeConstant(ConstantNode node, DiscriminativeOracle oracle) {
		String example = node.getData().example;
		Context context = node.getData().context;
		if(example.length() != 0) {
			Log.info("GENERALIZING CONST: " + example + " ## " + context.pre + " ## " + context.post);
		}
		List<List<Character>> characterOptions = new ArrayList<List<Character>>();
		List<List<Character>> characterChecks = new ArrayList<List<Character>>();
		for(int i=0; i<example.length(); i++) {
			List<Character> characterOption = new ArrayList<Character>();
			List<Character> characterCheck = new ArrayList<Character>();
			char curC = example.charAt(i);
			Context curContext = new Context(context, example.substring(0, i), example.substring(i+1), example.substring(0, i), example.substring(i+1));
			characterOption.add(curC);
			characterCheck.add(curC);
			for(CharacterGeneralization generalization : CharacterUtils.getGeneralizations()) {
				if(generalization.triggers.contains(curC)) {
					List<String> checks = new ArrayList<String>();
					for(char c : generalization.checks) {
						if(curC != c) {
							checks.add("" + c);
						}
					}
					if(GrammarSynthesis.getCheck(oracle, curContext, checks)) {
						for(char c : generalization.characters) {
							if(curC != c) {
								characterOption.add(c);
							}
						}
						for(char c : generalization.checks) {
							if(curC != c) {
								characterCheck.add(c);
							}
						}
					}
				}
			}
			characterOptions.add(characterOption);
			characterChecks.add(characterCheck);
		}
		return new MultiConstantNode(node.getData(), characterOptions, characterChecks);
	}
	
	private static boolean isContained(String example, MultiConstantNode mconstNode) {
		if(example.length() != mconstNode.characterOptions.size()) {
			return false;
		}
		for(int i=0; i<example.length(); i++) {
			if(!mconstNode.characterOptions.get(i).contains(example.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isContained(String example, List<MultiConstantNode> mconstNodes) {
		for(MultiConstantNode mconstNode : mconstNodes) {
			if(isContained(example, mconstNode)) {
				return true;
			}
		}
		return false;
	}
	
	private static MultiAlternationNode generalizeMultiAlternationConstant(MultiAlternationNode node, MultivalueMap<MultiAlternationNode,ConstantNode> multiAlternationNodeConstantChildren, DiscriminativeOracle oracle) {
		List<MultiConstantNode> curConsts = new ArrayList<MultiConstantNode>();
		Log.info("GENERALIZING MULTI ALT: " + node.getData().example);
		for(Node child : multiAlternationNodeConstantChildren.get(node)) {
			if(!isContained(child.getData().example, curConsts)) {
				curConsts.add(generalizeConstant((ConstantNode)child, oracle));
			}
		}
		return new MultiAlternationNode(node.getData(), new ArrayList<Node>(curConsts));
	}
	
	private static class ConstantTransformer implements NodeTransformer {
		private final DiscriminativeOracle oracle;
		private final MultivalueMap<MultiAlternationNode,ConstantNode> multiAlternationNodeConstantChildren;
		private final Set<ConstantNode> ignoredConstants = new HashSet<ConstantNode>();;
		private ConstantTransformer(DiscriminativeOracle oracle, MultivalueMap<MultiAlternationNode,ConstantNode> multiAlternationNodeConstantChildren) {
			this.oracle = oracle;
			this.multiAlternationNodeConstantChildren = multiAlternationNodeConstantChildren;
			for(MultiAlternationNode maltNode : multiAlternationNodeConstantChildren.keySet()) {
				this.ignoredConstants.addAll(multiAlternationNodeConstantChildren.get(maltNode));
			}
		}
		public Node transformConstant(ConstantNode node) {
			return this.ignoredConstants.contains(node) ? node : generalizeConstant(node, this.oracle);
		}
		public Node transformMultiConstant(MultiConstantNode node) {
			throw new RuntimeException("Invalid node: " + node);
		}
		public Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond) {
			return new AlternationNode(node.getData(), newFirst, newSecond);
		}
		public Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren) {
			return this.multiAlternationNodeConstantChildren.containsKey(node) ? generalizeMultiAlternationConstant(node, this.multiAlternationNodeConstantChildren, this.oracle) : new MultiAlternationNode(node.getData(), newChildren);
		}
		public Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd) {
			return new RepetitionNode(node.getData(), newStart, newRep, newEnd);
		}
	}
	
	private static class FlattenTransformer implements NodeTransformer {
		public Node transformConstant(ConstantNode node) {
			return node;
		}
		public Node transformMultiConstant(MultiConstantNode node) {
			return node;
		}
		public Node transformAlternation(AlternationNode node, Node newFirst, Node newSecond) {
			List<Node> newChildren = new ArrayList<Node>();
			if(newFirst instanceof MultiAlternationNode) {
				newChildren.addAll(((MultiAlternationNode)newFirst).getChildren());
			} else {
				newChildren.add(newFirst);
			}
			if(newSecond instanceof MultiAlternationNode) {
				newChildren.addAll(((MultiAlternationNode)newSecond).getChildren());
			} else {
				newChildren.add(newSecond);
			}
			return new MultiAlternationNode(node.getData(), newChildren);
		}
		public Node transformMultiAlternation(MultiAlternationNode node, List<Node> newChildren) {
			throw new RuntimeException("Invalid node: " + node);
		}
		public Node transformRepetition(RepetitionNode node, Node newStart, Node newRep, Node newEnd) {
			return new RepetitionNode(node.getData(), newStart, newRep, newEnd);
		}
	}
	
	private static void getMultiAlternationRepetitionConstantNodesHelper(Node node, MultivalueMap<MultiAlternationNode,ConstantNode> result, boolean isParentRep) {
		Maybe<List<Node>> constantChildren = GrammarSynthesis.getMultiAlternationRepetitionConstantChildren(node, isParentRep);
		if(constantChildren.hasT()) {
			for(Node child : constantChildren.getT()) {
				result.add((MultiAlternationNode)node, (ConstantNode)child);
			}
		} else if(node instanceof RepetitionNode) {
			RepetitionNode repNode = (RepetitionNode)node;
			getMultiAlternationRepetitionConstantNodesHelper(repNode.start, result, false);
			getMultiAlternationRepetitionConstantNodesHelper(repNode.rep, result, true);
			getMultiAlternationRepetitionConstantNodesHelper(repNode.end, result, false);
		} else {
			for(Node child : node.getChildren()) {
				getMultiAlternationRepetitionConstantNodesHelper(child, result, false);
			}
		}
	}
	
	private static MultivalueMap<MultiAlternationNode,ConstantNode> getMultiAlternationRepetitionConstantNodes(Node root) {
		MultivalueMap<MultiAlternationNode,ConstantNode> result = new MultivalueMap<MultiAlternationNode,ConstantNode>();
		getMultiAlternationRepetitionConstantNodesHelper(root, result, false);
		return result;
	}
}
