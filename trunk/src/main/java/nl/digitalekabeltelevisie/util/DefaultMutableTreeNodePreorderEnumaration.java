package nl.digitalekabeltelevisie.util;

import java.util.*;

import javax.swing.tree.*;

public class DefaultMutableTreeNodePreorderEnumaration implements Enumeration<DefaultMutableTreeNode>{

    private final Stack<Enumeration<DefaultMutableTreeNode>> stack = new Stack<Enumeration<DefaultMutableTreeNode>>();

    public DefaultMutableTreeNodePreorderEnumaration(DefaultMutableTreeNode rootNode) {
        super();
        Vector<DefaultMutableTreeNode> v = new Vector<DefaultMutableTreeNode>(1);
        v.addElement(rootNode);     // PENDING: don't really need a vector
        stack.push(v.elements());
    }

    
	@Override
    public boolean hasMoreElements() {
        return (!stack.empty() && stack.peek().hasMoreElements());
    }

	@Override
	public DefaultMutableTreeNode nextElement() {
        Enumeration<?> enumer = stack.peek();
        DefaultMutableTreeNode    node = (DefaultMutableTreeNode)enumer.nextElement();
        Enumeration<?> children = node.children();

        if (!enumer.hasMoreElements()) {
            stack.pop();
        }
        if (children.hasMoreElements()) {
        	Vector<DefaultMutableTreeNode> v = new Vector<DefaultMutableTreeNode>();
        	while(children.hasMoreElements()){
        		Object nextChild = children.nextElement();
        		if(nextChild instanceof DefaultMutableTreeNode){
        			v.addElement((DefaultMutableTreeNode)nextChild);
        		}
        	}
        	if(!v.isEmpty()){	
        		stack.push(v.elements());
        	}
        }
        return node;
    }

}
