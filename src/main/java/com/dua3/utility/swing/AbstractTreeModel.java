package com.dua3.utility.swing;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeModel implements TreeModel {

        private final List<TreeModelListener> modelListeners = new LinkedList<>();

        @Override
        public void addTreeModelListener(TreeModelListener listener) {
            modelListeners.add(listener);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener listener) {
            modelListeners.remove(listener);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            postTreeModelEvent(new TreeModelEvent(this, path));
        }

        private void postTreeModelEvent(TreeModelEvent evt) {
            for (TreeModelListener listener : modelListeners) {
                listener.treeNodesChanged(evt);
            }
        }


        protected abstract List<?> getChildren(Object parent);

        @Override
        public Object getChild(Object parent, int index) {
            return getChildren(parent).get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return getChildren(parent).size();
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return getChildren(parent).indexOf(child);
        }

        @Override
        public boolean isLeaf(Object node) {
            return getChildren(node).isEmpty();
        }
}
