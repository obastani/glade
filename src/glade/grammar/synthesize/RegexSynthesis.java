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
import glade.grammar.GrammarUtils.Node;
import glade.grammar.GrammarUtils.NodeData;
import glade.grammar.GrammarUtils.RepetitionNode;
import glade.util.Log;
import glade.util.OracleUtils.DiscriminativeOracle;
import glade.util.Utils.Maybe;

import java.util.ArrayList;
import java.util.List;

public class RegexSynthesis {
	public static Node getNode(String example, DiscriminativeOracle oracle) {
		return getNode(new NodeData(example, new Context()), oracle, new NodeType[]{NodeType.REPETITION, NodeType.ALTERNATION}, true);
	}
	
	private static List<String> getAlternationChecks(String first, String second) {
		List<String> checks = new ArrayList<String>();
		checks.add(second + first + second + first);
		checks.add(first + second + first + second);
		checks.add(second + first);
		checks.add(second + second);
		checks.add(first + first);
		checks.add(second);
		checks.add(first);
		checks.add("");
		return checks;
	}
	
	private static List<String> getRepetitionChecks(String start, String rep, String end) {
		List<String> checks = new ArrayList<String>();
		checks.add(start + rep + rep + end);
		checks.add(start + end);
		return checks;
	}
	
	private static class AlternationPartialNode {
		private final NodeData first;
		private final NodeData second;
		private AlternationPartialNode(NodeData first, NodeData second) {
			this.first = first;
			this.second = second;
		}
	}
	
	private static class RepetitionPartialNode {
		private final NodeData start;
		private final NodeData rep;
		private final NodeData end;
		private RepetitionPartialNode(NodeData start, NodeData rep, NodeData end) {
			this.start = start;
			this.rep = rep;
			this.end = end;
		}
	}
	
	private static Maybe<AlternationPartialNode> getAlternationPartialNode(NodeData cur, DiscriminativeOracle oracle) {
		for(int i=1; i<=cur.example.length()-1; i++) {
			String first = cur.example.substring(0, i);
			String second = cur.example.substring(i);
			if(GrammarSynthesis.getCheck(oracle, cur.context, getAlternationChecks(first, second))) {
				NodeData firstData = new NodeData(first, new Context(cur.context, "", second, "", ""));
				NodeData secondData = new NodeData(second, new Context(cur.context, first, "", "", ""));
				Log.info("FOUND ALT: " + first + " ## " + second);
				return new Maybe<AlternationPartialNode>(new AlternationPartialNode(firstData, secondData));
			}
		}
		return new Maybe<AlternationPartialNode>();
	}
	
	private static Maybe<RepetitionPartialNode> getRepetitionPartialNode(NodeData cur, DiscriminativeOracle oracle, boolean isWholeStringRepeatable) {
		for(int init=0; init<=cur.example.length()-1; init++) {
			for(int len=cur.example.length()-init; len>=1; len--) {
				if(len == cur.example.length() && !isWholeStringRepeatable) {
					continue;
				}
				String start = cur.example.substring(0, init);
				String rep = cur.example.substring(init, init+len);
				String end = cur.example.substring(init+len);
				if(GrammarSynthesis.getCheck(oracle, cur.context, getRepetitionChecks(start, rep, end))) {
					NodeData startData = new NodeData(start, new Context(cur.context, "", rep+end, "", end));
					NodeData repData = new NodeData(rep, new Context(cur.context, start, end, start, end));
					NodeData endData = new NodeData(end, new Context(cur.context, start+rep, "", start, ""));
					Log.info("FOUND REP: " + rep + " ## " + start + " ## " + end);
					return new Maybe<RepetitionPartialNode>(new RepetitionPartialNode(startData, repData, endData));
				}
			}
		}
		return new Maybe<RepetitionPartialNode>();
	}
	
	private static Maybe<Node> getConstantNode(NodeData cur, DiscriminativeOracle oracle) {
		return new Maybe<Node>(new ConstantNode(cur));
	}
	
	private static Maybe<Node> getAlternationNode(NodeData cur, DiscriminativeOracle oracle) {
		Maybe<AlternationPartialNode> maybe = getAlternationPartialNode(cur, oracle);
		if(!maybe.hasT()) {
			return new Maybe<Node>();
		}
		Node first = getNode(maybe.getT().first, oracle, new NodeType[]{NodeType.REPETITION}, true);
		Node second = getNode(maybe.getT().second, oracle, new NodeType[]{NodeType.ALTERNATION, NodeType.REPETITION}, true);
		return new Maybe<Node>(new AlternationNode(cur, first, second));
	}
	
	private static Maybe<Node> getRepetitionNode(NodeData cur, DiscriminativeOracle oracle, boolean isWholeStringRepeatable) {
		Maybe<RepetitionPartialNode> maybe = getRepetitionPartialNode(cur, oracle, isWholeStringRepeatable);
		if(!maybe.hasT()) {
			return new Maybe<Node>();
		}
		Node start = getNode(maybe.getT().start, oracle, new NodeType[]{}, true);
		Node rep = getNode(maybe.getT().rep, oracle, new NodeType[]{NodeType.ALTERNATION, NodeType.REPETITION}, false);
		Node end = getNode(maybe.getT().end, oracle, new NodeType[]{NodeType.REPETITION}, true);
		return new Maybe<Node>(new RepetitionNode(cur, start, rep, end));
	}
	
	private static enum NodeType {
		REPETITION, ALTERNATION;
	}
	
	private static Node getNode(NodeData cur, DiscriminativeOracle oracle, NodeType[] types, boolean isWholeStringRepeatable) {
		for(NodeType type : types) {
			switch(type) {
			case REPETITION:
				Maybe<Node> nodeRep = getRepetitionNode(cur, oracle, isWholeStringRepeatable);
				if(nodeRep.hasT()) {
					return nodeRep.getT();
				}
				break;
			case ALTERNATION:
				Maybe<Node> nodeAlt = getAlternationNode(cur, oracle);
				if(nodeAlt.hasT()) {
					return nodeAlt.getT();
				}
				break;
			}
		}
		return getConstantNode(cur, oracle).getT();
	}
}
