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

package glade.grammar;

import glade.grammar.GrammarUtils.MultiAlternationNode;
import glade.grammar.GrammarUtils.MultiConstantNode;
import glade.grammar.GrammarUtils.Node;
import glade.grammar.GrammarUtils.RepetitionNode;
import glade.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeUtils {
	public static interface ParseTreeNode {
		public abstract String getExample();
		public abstract Node getNode();
		public abstract List<ParseTreeNode> getChildren();
	}
	
	public static class ParseTreeRepetitionNode implements ParseTreeNode {
		private final RepetitionNode node;
		private final String example;

		public final ParseTreeNode start;
		public final List<ParseTreeNode> rep;
		public final ParseTreeNode end;
		
		public ParseTreeRepetitionNode(RepetitionNode node, ParseTreeNode start, List<ParseTreeNode> rep, ParseTreeNode end) {
			this.node = node;
			this.start = start;
			this.rep = rep;
			this.end = end;
			StringBuilder sb = new StringBuilder();
			sb.append(start.getExample());
			for(ParseTreeNode repNode : rep) {
				sb.append(repNode.getExample());
			}
			sb.append(end.getExample());
			this.example = sb.toString();
		}
		
		@Override
		public Node getNode() {
			return this.node;
		}

		@Override
		public String getExample() {
			return this.example;
		}

		@Override
		public List<ParseTreeNode> getChildren() {
			List<ParseTreeNode> children = new ArrayList<ParseTreeNode>();
			children.add(this.start);
			children.addAll(this.rep);
			children.add(this.end);
			return children;
		}
		
		@Override
		public String toString() {
			return this.example;
		}
	}
	
	public static class ParseTreeMultiAlternationNode implements ParseTreeNode {
		private final MultiAlternationNode node;
		private final String example;
		
		public final ParseTreeNode choice;
		
		public ParseTreeMultiAlternationNode(MultiAlternationNode node, ParseTreeNode choice) {
			this.node = node;
			this.example = choice.getExample();
			this.choice = choice;
		}
		
		@Override
		public Node getNode() {
			return this.node;
		}

		@Override
		public String getExample() {
			return this.example;
		}

		@Override
		public List<ParseTreeNode> getChildren() {
			return Utils.getList(this.choice);
		}
		
		@Override
		public String toString() {
			return this.example;
		}
	}
	
	public static class ParseTreeMultiConstantNode implements ParseTreeNode {
		private final MultiConstantNode node;
		private final String example;
		
		public ParseTreeMultiConstantNode(MultiConstantNode node, String example) {
			this.node = node;
			this.example = example;
		}

		@Override
		public Node getNode() {
			return this.node;
		}

		@Override
		public String getExample() {
			return this.example;
		}

		@Override
		public List<ParseTreeNode> getChildren() {
			return new ArrayList<ParseTreeNode>();
		}
		
		@Override
		public String toString() {
			return this.example;
		}
	}
	
	public static class ParseTreeMergeNode implements ParseTreeNode {
		private final Node node;
		private final String example;

		public final ParseTreeNode merge;
		
		public ParseTreeMergeNode(Node node, ParseTreeNode merge) {
			this.node = node;
			this.merge = merge;
			this.example = merge.getExample();
		}

		@Override
		public Node getNode() {
			return this.node;
		}

		@Override
		public String getExample() {
			return this.example;
		}

		@Override
		public List<ParseTreeNode> getChildren() {
			return Utils.getList(this.merge);
		}
		
		@Override
		public String toString() {
			return this.example;
		}
	}
	
	public static List<ParseTreeNode> getParseTreeAlt(MultiAlternationNode node) {
		List<ParseTreeNode> parseTreeNodes = new ArrayList<ParseTreeNode>();
		for(Node child : node.getChildren()) {
			parseTreeNodes.add(new ParseTreeMultiAlternationNode(node, getParseTreeRepConst(child)));
		}
		return parseTreeNodes;
	}
	
	public static ParseTreeNode getParseTreeRepConst(Node node) {
		if(node instanceof RepetitionNode) {
			RepetitionNode repNode = (RepetitionNode)node;
			ParseTreeNode start = getParseTreeRepConst(repNode.start);
			ParseTreeNode end = getParseTreeRepConst(repNode.end);
			if(repNode.rep instanceof MultiAlternationNode) {
				return new ParseTreeRepetitionNode(repNode, start, getParseTreeAlt((MultiAlternationNode)repNode.rep), end);
			} else {
				return new ParseTreeRepetitionNode(repNode, start, Utils.getList(getParseTreeRepConst(repNode.rep)), end);
			}
		} else if(node instanceof MultiConstantNode) {
			return new ParseTreeMultiConstantNode((MultiConstantNode)node, node.getData().example);
		} else {
			throw new RuntimeException("Invalid node type: " + node.getClass().getName());
		}
	}
	
	public static ParseTreeNode getParseTree(Node node) {
		return getParseTreeRepConst(node);
	}
	
	private static void getDescendantsHelper(ParseTreeNode node, List<ParseTreeNode> descendants) {
		descendants.add(node);
		for(ParseTreeNode child : node.getChildren()) {
			getDescendantsHelper(child, descendants);
		}
	}
	
	public static List<ParseTreeNode> getDescendants(ParseTreeNode node) {
		List<ParseTreeNode> descendants = new ArrayList<ParseTreeNode>();
		getDescendantsHelper(node, descendants);
		return descendants;
	}
	
	private static void getDescendantsByTypeHelper(ParseTreeNode node, List<ParseTreeNode>[] descendants) {
		if(node instanceof ParseTreeMultiConstantNode) {
			descendants[0].add(node);
		} else {
			descendants[1].add(node);
		}
		for(ParseTreeNode child : node.getChildren()) {
			getDescendantsByTypeHelper(child, descendants);
		}
	}

	public static List<ParseTreeNode>[] getDescendantsByType(ParseTreeNode node) {
		@SuppressWarnings("unchecked")
		List<ParseTreeNode>[] descendants = new List[2];
		for(int i=0; i<2; i++) {
			descendants[i] = new ArrayList<ParseTreeNode>();
		}
		getDescendantsByTypeHelper(node, descendants);
		return descendants;
	}
	
	public static ParseTreeNode getSubstitute(ParseTreeNode node, ParseTreeNode cur, ParseTreeNode sub) {
		if(node == cur) {
			return sub;
		} else if(node instanceof ParseTreeRepetitionNode) {
			ParseTreeRepetitionNode repNode = (ParseTreeRepetitionNode)node;
			ParseTreeNode newStart = getSubstitute(repNode.start, cur, sub);
			List<ParseTreeNode> newRep = new ArrayList<ParseTreeNode>();
			for(ParseTreeNode rep : repNode.rep) {
				newRep.add(getSubstitute(rep, cur, sub));
			}
			ParseTreeNode newEnd = getSubstitute(repNode.end, cur, sub);
			return new ParseTreeRepetitionNode(repNode.node, newStart, newRep, newEnd);
		} else if(node instanceof ParseTreeMultiAlternationNode) {
			ParseTreeNode newChoice = getSubstitute(((ParseTreeMultiAlternationNode)node).choice, cur, sub);
			return new ParseTreeMultiAlternationNode(((ParseTreeMultiAlternationNode)node).node, newChoice);
		} else if(node instanceof ParseTreeMultiConstantNode) {
			return node;
		} else {
			throw new RuntimeException("Unrecognized node type: " + node.getClass().getName());
		}
	}
}
