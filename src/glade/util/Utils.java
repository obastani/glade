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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
	public static <V> Map<V,Integer> getInverse(List<V> list) {
		Map<V,Integer> inverse = new HashMap<V,Integer>();
		for(int i=0; i<list.size(); i++) {
			inverse.put(list.get(i), i);
		}
		return inverse;
	}
	
	@SafeVarargs
	public static <T> List<T> getList(T ... ts) {
		List<T> tlist = new ArrayList<T>();
		for(T t : ts) {
			tlist.add(t);
		}
		return tlist;
	}
	
	public static class MultivalueMap<K,V> extends HashMap<K,Set<V>> {
		private static final long serialVersionUID = -6390444829513305915L;

		public void add(K k, V v) {
			ensure(k).add(v);
		}
		
		public Set<V> ensure(K k) {
			Set<V> vSet = super.get(k);
			if(vSet == null) {
				super.put(k, vSet = new HashSet<V>());
			}
			return vSet;
		}

		@Override
		public Set<V> get(Object k) {
			Set<V> vSet = super.get(k);
			return vSet == null ? new HashSet<V>() : vSet;
		}
	}
	
	public static class Maybe<T> {
		private T t;
		public Maybe(T t) {
			this.t = t;
		}
		public Maybe() {
			this.t = null;
		}
		public T getT() {
			if(this.t != null) {
				return t;
			}
			throw new RuntimeException("Invalid access!");
		}
		public boolean hasT() {
			return this.t != null;
		}
		public T getTOr(T t) {
			return this.hasT() ? this.getT() : t;
		}
	}
}
