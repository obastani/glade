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

import glade.util.Utils.MultivalueMap;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GrammarUtils {
	public static class Grammar {
		public final Node node;
		public final NodeMerges merges;
		public Grammar(Node node, NodeMerges merges) {
			this.node = node;
			this.merges = merges;
		}
	}
	
	public static final class Context {
		public final String pre;
		public final String post;
		public final String extraPre;
		public final String extraPost;
		public Context() {
			this.pre = "";
			this.post = "";
			this.extraPre = "";
			this.extraPost = "";
		}
		public Context(Context parent, String pre, String post, String extraPre, String extraPost) {
			this.pre = parent.pre + pre;
			this.post = post + parent.post;
			this.extraPre = parent.extraPre + extraPre;
			this.extraPost = extraPost + parent.extraPost;
		}
		public boolean useExtra() {
			return !this.pre.equals(this.extraPre) || !this.post.equals(this.extraPost);
		}
	}
	
	public static class NodeData {
		public final String example;
		public final Context context;
		public NodeData(String example, Context context) {
			this.example = example;
			this.context = context;
		}
	}
	
	public static interface Node {
		public abstract List<Node> getChildren();
		public abstract NodeData getData();
	}
	
	public static class ConstantNode implements Node {
		private final NodeData data;
		public ConstantNode(NodeData data) {
			this.data = data;
		}
		public List<Node> getChildren() {
			return new ArrayList<Node>();
		}
		public NodeData getData() {
			return this.data;
		}
		public String toString() {
			return this.data.example;
		}
	}
	
	public static class MultiConstantNode implements Node {
		private final NodeData data;
		public final List<Set<Character>> characterOptions = new ArrayList<Set<Character>>();
		public final List<Set<Character>> characterChecks = new ArrayList<Set<Character>>();
		public MultiConstantNode(NodeData data, List<List<Character>> characterOptions, List<List<Character>> characterChecks) {
			this.data = data;
			if(characterOptions.size() != characterChecks.size()) {
				throw new RuntimeException("Invalid characters!");
			}
			for(List<Character> characters : characterOptions) {
				this.characterOptions.add(new LinkedHashSet<Character>(characters));
			}
			for(List<Character>characters : characterChecks) {
				this.characterChecks.add(new LinkedHashSet<Character>(characters));
			}
		}
		public List<Node> getChildren() {
			return new ArrayList<Node>();
		}
		public NodeData getData() {
			return this.data;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(Set<Character> characterOption : this.characterOptions) {
				sb.append("(");
				for(char character : characterOption) {
					sb.append(character).append("+");
				}
				sb.replace(sb.length()-1, sb.length(), ")");
			}
			return sb.toString();
		}
	}
	
	public static class AlternationNode implements Node {
		private final NodeData data;
		public final Node first;
		public final Node second;
		public AlternationNode(NodeData data, Node first, Node second) {
			this.data = data;
			this.first = first;
			this.second = second;
		}
		public List<Node> getChildren() {
			List<Node> children = new ArrayList<Node>();
			children.add(this.first);
			children.add(this.second);
			return children;
		}
		public NodeData getData() {
			return this.data;
		}
		public String toString() {
			return "(" + this.first.toString() + ")+(" + this.second.toString(); 
		}
	}
	
	public static class MultiAlternationNode implements Node {
		private final NodeData data;
		private final List<Node> children = new ArrayList<Node>();
		public MultiAlternationNode(NodeData data, List<Node> children) {
			this.data = data;
			this.children.addAll(children);
		}
		public List<Node> getChildren() {
			List<Node> newChildren = new ArrayList<Node>();
			newChildren.addAll(this.children);
			return newChildren;
		}
		public NodeData getData() {
			return this.data;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(Node child : this.children) {
				sb.append("(").append(child.toString()).append(")+");
			}
			return sb.substring(0, sb.length()-1);
		}
	}
	
	public static class RepetitionNode implements Node {
		private final NodeData data;
		public final Node start;
		public final Node rep;
		public final Node end;
		public RepetitionNode(NodeData data, Node start, Node rep, Node end) {
			this.data = data;
			this.start = start;
			this.rep = rep;
			this.end = end;
		}
		public List<Node> getChildren() {
			List<Node> children = new ArrayList<Node>();
			children.add(this.start);
			children.add(this.rep);
			children.add(this.end);
			return children;
		}
		public NodeData getData() {
			return this.data;
		}
		public String toString() {
			return this.start.toString() + "(" + this.rep.toString() + ")*" + this.end.toString();
		}
	}
	
	public static class NodeMerges {
		private final MultivalueMap<Node,Node> merges = new MultivalueMap<Node,Node>();
		public void add(Node first, Node second) {
			this.merges.add(first, second);
			this.merges.add(second, first);
		}
		public void addAll(NodeMerges other) {
			for(Node first : other.keySet()) {
				for(Node second : other.get(first)) {
					this.add(first, second);
				}
			}
		}
		public Set<Node> get(Node node) {
			return this.merges.get(node);
		}
		public Set<Node> keySet() {
			return this.merges.keySet();
		}
		public boolean contains(Node first, Node second) {
			return this.merges.get(first).contains(second);
		}
	}
	
	private static void getAllNodesHelper(Node root, List<Node> nodes) {
		nodes.add(root);
		for(Node child : root.getChildren()) {
			getAllNodesHelper(child, nodes);
		}
	}
	
	public static List<Node> getAllNodes(Node root) {
		List<Node> nodes = new ArrayList<Node>();
		getAllNodesHelper(root, nodes);
		return nodes;
	}
	
	private static void getDescendantsHelper(Node node, List<Node> descendants) {
		descendants.add(node);
		for(Node child : node.getChildren()) {
			getDescendantsHelper(child, descendants);
		}
	}
	
	public static List<Node> getDescendants(Node node) {
		List<Node> descendants = new ArrayList<Node>();
		getDescendantsHelper(node, descendants);
		return descendants;
	}
}
