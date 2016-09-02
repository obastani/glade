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

import glade.grammar.GrammarUtils.AlternationNode;
import glade.grammar.GrammarUtils.ConstantNode;
import glade.grammar.GrammarUtils.Context;
import glade.grammar.GrammarUtils.Grammar;
import glade.grammar.GrammarUtils.MultiAlternationNode;
import glade.grammar.GrammarUtils.MultiConstantNode;
import glade.grammar.GrammarUtils.Node;
import glade.grammar.GrammarUtils.NodeData;
import glade.grammar.GrammarUtils.NodeMerges;
import glade.grammar.GrammarUtils.RepetitionNode;
import glade.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GrammarSerializer {
	public static void serialize(String string, DataOutputStream dos) throws IOException {
		if(string == null) {
			dos.writeInt(-1);
		} else {
			dos.writeInt(string.length());
			for(int i=0; i<string.length(); i++) {
				dos.writeChar(string.charAt(i));
			}
		}
	}
	
	public static String deserializeString(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		if(length == -1) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<length; i++) {
				sb.append(dis.readChar());
			}
			return sb.toString();
		}
	}
	
	public static void serialize(NodeData data, DataOutputStream dos) throws IOException {
		serialize(data.example, dos);
		serialize(data.context.pre, dos);
		serialize(data.context.post, dos);
		serialize(data.context.extraPre, dos);
		serialize(data.context.extraPost, dos);
	}
	
	public static NodeData deserializeNodeData(DataInputStream dis) throws IOException {
		String example = deserializeString(dis);
		String pre = deserializeString(dis);
		String post = deserializeString(dis);
		String extraPre = deserializeString(dis);
		String extraPost = deserializeString(dis);
		return new NodeData(example, new Context(new Context(), pre, post, extraPre, extraPost));
	}
	
	public static void serialize(Grammar grammar, DataOutputStream dos) throws IOException {
		List<Node> nodes = GrammarUtils.getAllNodes(grammar.node);
		Map<Node,Integer> nodeIds = Utils.getInverse(nodes);
		dos.writeInt(nodes.size()); // 0
		for(Node node : nodes) {
			dos.writeInt(nodeIds.get(node)); // 1
			serialize(node.getData(), dos); // 2
			if(node instanceof ConstantNode) {
				dos.writeInt(0); // 3/1
			} else if(node instanceof AlternationNode) {
				AlternationNode altNode = (AlternationNode)node;
				dos.writeInt(1); // 3/1
				dos.writeInt(nodeIds.get(altNode.first)); // 3/2
				dos.writeInt(nodeIds.get(altNode.second)); // 3/3
			} else if(node instanceof MultiAlternationNode) {
				MultiAlternationNode maltNode = (MultiAlternationNode)node;
				dos.writeInt(2); // 3/1
				dos.writeInt(maltNode.getChildren().size()); // 3/2
				for(Node child : maltNode.getChildren()) {
					dos.writeInt(nodeIds.get(child)); // 3/3
				}
			} else if(node instanceof RepetitionNode) {
				RepetitionNode repNode = (RepetitionNode)node;
				dos.writeInt(3); // 3/1
				dos.writeInt(nodeIds.get(repNode.start)); // 3/2
				dos.writeInt(nodeIds.get(repNode.rep)); // 3/3
				dos.writeInt(nodeIds.get(repNode.end)); // 3/4
			} else if(node instanceof MultiConstantNode) {
				MultiConstantNode mconstNode = (MultiConstantNode)node;
				dos.writeInt(4); // 3/1
				dos.writeInt(mconstNode.characterOptions.size()); // 3/2
				for(int i=0; i<mconstNode.characterOptions.size(); i++) {
					Set<Character> characterOption = mconstNode.characterOptions.get(i);
					dos.writeInt(characterOption.size()); // 3/3
					for(char c : characterOption) {
						dos.writeChar(c); // 3/4
					}
					Set<Character> characterChecks = mconstNode.characterChecks.get(i);
					dos.writeInt(characterChecks.size()); // 3/5
					for(char c : characterChecks) {
						dos.writeChar(c); // 3/6
					}
				}
			} else {
				throw new RuntimeException("Unrecognized node type: " + node.getClass().getName());
			}
		}
		dos.writeInt(grammar.merges.keySet().size()); // 4
		for(Node first : grammar.merges.keySet()) {
			dos.writeInt(grammar.merges.get(first).size()); // 5
			for(Node second : grammar.merges.get(first)) {
				dos.writeInt(nodeIds.get(first)); // 6
				dos.writeInt(nodeIds.get(second)); // 7
			}
		}
	}
	
	private static interface NodeSerialization {
		public abstract NodeData getData();
	}
	
	private static class ConstantNodeSerialization implements NodeSerialization {
		private final NodeData data;
		private ConstantNodeSerialization(NodeData data) {
			this.data = data;
		}
		public NodeData getData() {
			return this.data;
		}
	}
	
	private static class MultiConstantNodeSerialization implements NodeSerialization {
		private final NodeData data;
		private final List<List<Character>> characterOptions;
		private final List<List<Character>> characterChecks;
		private MultiConstantNodeSerialization(NodeData data, List<List<Character>> characterOptions, List<List<Character>> characterChecks) {
			this.data = data;
			this.characterOptions = characterOptions;
			this.characterChecks = characterChecks;
		}
		public NodeData getData() {
			return this.data;
		}
	}
	
	private static class AlternationNodeSerialization implements NodeSerialization {
		private final NodeData data;
		private final int first;
		private final int second;
		private AlternationNodeSerialization(NodeData data, int first, int second) {
			this.data = data;
			this.first = first;
			this.second = second;
		}
		public NodeData getData() {
			return this.data;
		}
	}
	
	private static class MultiAlternationNodeSerialization implements NodeSerialization {
		private final NodeData data;
		private final List<Integer> children;
		private MultiAlternationNodeSerialization(NodeData data, List<Integer> children) {
			this.data = data;
			this.children = children;
		}
		public NodeData getData() {
			return this.data;
		}
	}
	
	private static class RepetitionNodeSerialization implements NodeSerialization {
		private final NodeData data;
		private final int start;
		private final int rep;
		private final int end;
		private RepetitionNodeSerialization(NodeData data, int start, int rep, int end) {
			this.data = data;
			this.start = start;
			this.rep = rep;
			this.end = end;
		}
		public NodeData getData() {
			return this.data;
		}
	}
	
	private static class NodeDeserializer {
		private final List<NodeSerialization> nodeSerializations;
		private final List<Node> nodes;
		private NodeDeserializer(List<NodeSerialization> nodeSerializations) {
			this.nodeSerializations = nodeSerializations;
			this.nodes = new ArrayList<Node>();
			for(int i=0; i<nodeSerializations.size(); i++) {
				this.nodes.add(null);
			}
		}
		private Node deserialize(int index) {
			if(this.nodes.get(index) == null) {
				NodeSerialization nodeSerialization = this.nodeSerializations.get(index);
				if(nodeSerialization instanceof ConstantNodeSerialization) {
					this.nodes.set(index, new ConstantNode(nodeSerialization.getData()));
				} else if(nodeSerialization instanceof AlternationNodeSerialization) {
					AlternationNodeSerialization altNodeSerialization = (AlternationNodeSerialization)nodeSerialization;
					this.nodes.set(index, new AlternationNode(altNodeSerialization.getData(), this.deserialize(altNodeSerialization.first), this.deserialize(altNodeSerialization.second)));
				} else if(nodeSerialization instanceof MultiAlternationNodeSerialization) {
					MultiAlternationNodeSerialization maltNodeSerialization = (MultiAlternationNodeSerialization)nodeSerialization;
					List<Node> children = new ArrayList<Node>();
					for(int childIndex : maltNodeSerialization.children) {
						children.add(this.deserialize(childIndex));
					}
					this.nodes.set(index, new MultiAlternationNode(maltNodeSerialization.getData(), children));
				} else if(nodeSerialization instanceof RepetitionNodeSerialization) {
					RepetitionNodeSerialization repNodeSerialization = (RepetitionNodeSerialization)nodeSerialization;
					this.nodes.set(index, new RepetitionNode(repNodeSerialization.getData(), this.deserialize(repNodeSerialization.start), this.deserialize(repNodeSerialization.rep), this.deserialize(repNodeSerialization.end)));
				} else if(nodeSerialization instanceof MultiConstantNodeSerialization) {
					MultiConstantNodeSerialization mconstNodeSerialization = (MultiConstantNodeSerialization)nodeSerialization;
					return new MultiConstantNode(mconstNodeSerialization.getData(), mconstNodeSerialization.characterOptions, mconstNodeSerialization.characterChecks);
				} else {
					throw new RuntimeException("Unrecognized node type: " + nodeSerialization.getClass().getName());
				}
			}
			return this.nodes.get(index);
		}
		private List<Node> deserialize() {
			for(int i=0; i<this.nodeSerializations.size(); i++) {
				this.deserialize(i);
			}
			return this.nodes;
		}
	}
	
	public static Grammar deserializeNodeWithMerges(DataInputStream dis) throws IOException {
		int numNodes = dis.readInt(); // 0
		List<NodeSerialization> nodeSerializations = new ArrayList<NodeSerialization>(numNodes);
		for(int i=0; i<numNodes; i++) {
			nodeSerializations.add(null);
		}
		for(int i=0; i<numNodes; i++) {
			int id = dis.readInt(); // 1
			NodeData data = deserializeNodeData(dis); // 2
			int type = dis.readInt(); // 3/1
			if(type == 0) {
				nodeSerializations.set(id, new ConstantNodeSerialization(data));
			} else if(type == 1) {
				int first = dis.readInt(); // 3/2
				int second = dis.readInt(); // 3/3
				nodeSerializations.set(id, new AlternationNodeSerialization(data, first, second));
			} else if(type == 2) {
				int numChildren = dis.readInt(); // 3/2
				List<Integer> children = new ArrayList<Integer>();
				for(int j=0; j<numChildren; j++) {
					children.add(dis.readInt()); // 3/3
				}
				nodeSerializations.set(id, new MultiAlternationNodeSerialization(data, children));
			} else if(type == 3) {
				int start = dis.readInt(); // 3/2
				int rep = dis.readInt(); // 3/3
				int end = dis.readInt(); // 3/4
				nodeSerializations.set(id, new RepetitionNodeSerialization(data, start, rep, end));
			} else if(type == 4) {
				int numCharacterOptions = dis.readInt(); // 3/2
				List<List<Character>> characterOptions = new ArrayList<List<Character>>();
				List<List<Character>> characterChecks = new ArrayList<List<Character>>();
				for(int j=0; j<numCharacterOptions; j++) {
					int numCharacterOption = dis.readInt(); // 3/3
					List<Character> characterOption = new ArrayList<Character>();
					for(int k=0; k<numCharacterOption; k++) {
						char c = dis.readChar(); // 3/4
						characterOption.add(c);
					}
					characterOptions.add(characterOption);
					List<Character> characterCheck = new ArrayList<Character>();
					int numCharacterCheck = dis.readInt(); // 3/5
					for(int k=0; k<numCharacterCheck; k++) {
						char c = dis.readChar(); // 3/6
						characterCheck.add(c);
					}
					characterChecks.add(characterCheck);
				}
				nodeSerializations.set(id, new MultiConstantNodeSerialization(data, characterOptions, characterChecks));
			} else {
				throw new RuntimeException("Invalid node type: " + type);
			}
		}
		List<Node> nodes = new NodeDeserializer(nodeSerializations).deserialize();
		NodeMerges merges = new NodeMerges();
		int numMerges = dis.readInt(); // 4
		for(int i=0; i<numMerges; i++) {
			int numCurMerges = dis.readInt(); // 5
			for(int j=0; j<numCurMerges; j++) {
				int first = dis.readInt(); // 6
				int second = dis.readInt(); // 7
				merges.add(nodes.get(first), nodes.get(second));
			}
		}		
		return new Grammar(nodes.get(0), merges);
	}
}
