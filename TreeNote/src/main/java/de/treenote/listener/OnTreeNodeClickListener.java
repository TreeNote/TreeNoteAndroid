package de.treenote.listener;

import de.treenote.views.TreeNodeView;

public interface OnTreeNodeClickListener {

    void onTreeNodeClick(TreeNodeView onClickedTreeNodeView);

    void onTreeNodeLongClick(TreeNodeView onLongClickedTreeNodeView);

    void onTreeNodeFlingFromRightToLeft(TreeNodeView treeNodeView);

    void onTreeNodeFlingFromLeftToRight(TreeNodeView treeNodeView);
}
