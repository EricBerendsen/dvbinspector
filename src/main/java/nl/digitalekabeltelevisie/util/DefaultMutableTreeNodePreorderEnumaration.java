package nl.digitalekabeltelevisie.util;

import java.util.*;

import nl.digitalekabeltelevisie.controller.KVP;

public class DefaultMutableTreeNodePreorderEnumaration implements Enumeration<KVP>{

    private final Deque<Enumeration<KVP>> stack = new ArrayDeque<>();

    public DefaultMutableTreeNodePreorderEnumaration(KVP rootNode) {
        super();
        Vector<KVP> v = new Vector<>(1);
        v.addElement(rootNode);     // PENDING: don't really need a vector
        stack.push(v.elements());
    }

    
	@Override
    public boolean hasMoreElements() {
        return (!stack.isEmpty() && stack.peek().hasMoreElements());
    }

	@Override
	public KVP nextElement() {
		Enumeration<?> enumer = stack.peek();
		KVP node = (KVP) enumer.nextElement();
		Enumeration<?> children = node.children();

		if (!enumer.hasMoreElements()) {
			stack.pop();
		}
		if (children.hasMoreElements()) {
			Vector<KVP> v = new Vector<>();
			while (children.hasMoreElements()) {
				Object nextChild = children.nextElement();
				if (nextChild instanceof KVP childKvp) {
					v.addElement(childKvp);
				}
			}
			if (!v.isEmpty()) {
				stack.push(v.elements());
			}
		}
		return node;
	}

}
