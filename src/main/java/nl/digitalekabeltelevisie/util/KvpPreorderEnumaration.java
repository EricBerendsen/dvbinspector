package nl.digitalekabeltelevisie.util;

import java.util.*;

import nl.digitalekabeltelevisie.controller.KVP;

/**
 * Enumeration of all KVPs under the root, ignoring other types of Treenode, and their children
 * used for searching DVBTree, without the JTreeLazyList's
 * 
 * Based on DefaultMutableTreeNode.PreorderEnumeration
 */
public class KvpPreorderEnumaration implements Enumeration<KVP>{

    private final Deque<Iterator<KVP>> stack = new ArrayDeque<>();

    public KvpPreorderEnumaration(KVP rootNode) {
        super();
        stack.push(List.of(rootNode).iterator());
    }

    
	@Override
    public boolean hasMoreElements() {
        return (!stack.isEmpty() && stack.peek().hasNext());
    }

	@Override
	public KVP nextElement() {
		Iterator<?> enumer = stack.peek();
		KVP node = (KVP) enumer.next();
		Iterator<?> children = node.children().asIterator();

		if (!enumer.hasNext()) {
			stack.pop();
		}
		if (children.hasNext()) {
			List<KVP> v = new ArrayList<>();
			while (children.hasNext()) {
				Object nextChild = children.next();
				if (nextChild instanceof KVP childKvp) {
					v.add(childKvp);
				}
			}
			if (!v.isEmpty()) {
				stack.push(v.iterator());
			}
		}
		return node;
	}

}
