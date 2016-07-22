package de.treenote.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.jmedeisis.draglinearlayout.DragAndDropListener;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.treenote.R;
import de.treenote.listener.OnTreeNodeClickListener;
import de.treenote.pojo.TreeNode;
import lombok.Setter;

/**
 * Die TreeView ist technisch gesehen eine ScrollView mit einem DragLinearLayout. die ScrollView
 * dient hierbei nur als Container für das DragLinearLayout, damit bei größeren Views gescrollt
 * werden kann.
 * Die TreeView stellt in der Oberfläche eine Baumstruktur dar.
 */
public class TreeView extends ScrollView implements OnTreeNodeClickListener {

    private static final int LOWEST_DEPTH = 0;
    private TreeNode treeNodeRoot;

    @Nullable
    private TreeNodeView potentialNewParent;
    private DragLinearLayout dragLinearLayout;
    private View newEntrySpaceHolder;

    public TreeView(Context context) {
        super(context);
    }

    public TreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Setter
    private OnTreeNodeClickListener onTreeNodeClickListener;

    public void setTreeNodeRootInView(TreeNode treeNodeRoot) {
        createView(treeNodeRoot);
    }

    private void createView(TreeNode treeNodeRoot) {
        this.treeNodeRoot = treeNodeRoot;

        if (dragLinearLayout != null) {
            // View wurde bereits initialisiert
            dragLinearLayout.removeAllViews();
        } else {
            View.inflate(getContext(), R.layout.tree_view_content, this);
            dragLinearLayout = (DragLinearLayout) findViewById(R.id.dragLinearLayout);
            newEntrySpaceHolder = findViewById(R.id.newEntrySpaceHolder);
        }

        dragLinearLayout.setContainerScrollView(this);

        int startDepth = LOWEST_DEPTH;
        for (TreeNode treeNode : treeNodeRoot.getChildren()) {
            if (treeNode.isVisibleForCurrentSearch()) {
                addTreeNodeView(treeNode, startDepth);
                treeNode.setParent(treeNodeRoot);
            }
        }

        addListeners();
    }

    private void addListeners() {

        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View draggedView, View newAboveView, View newBelowView) {
                determineNewPotentialParentWithoutCast(draggedView, newAboveView, newBelowView);
            }
        });

        dragLinearLayout.setDragAndDropListener(new DragAndDropListener() {
            @Override
            public void onDragStart(View draggedView) {
                if (draggedView instanceof TreeNodeView) {
                    TreeNodeView draggedTreeView = (TreeNodeView) draggedView;
                    draggedTreeView.hideChildren();
                    changeNewPotentialParent(draggedTreeView, draggedTreeView.getTreeNodeViewParent());
                }
            }

            @Override
            public void onDrop(View droppedView, View droppedOnView, View aboveView, View belowView) {
                if (droppedView instanceof TreeNodeView && (belowView == null || belowView instanceof TreeNodeView)) {
                    ((TreeNodeView) droppedView)
                            .changeParent(potentialNewParent, (TreeNodeView) belowView, treeNodeRoot);

                    if (potentialNewParent != null) {
                        potentialNewParent.unmarkAsNewPotentialParent();
                    }
                    potentialNewParent = null;
                }
            }

            @Override
            public void onDragOverEnter(View draggedView, View draggedOverView) {
                if (draggedView instanceof TreeNodeView && draggedOverView instanceof TreeNodeView) {
                    TreeNodeView draggedOverTreeView = (TreeNodeView) draggedOverView;
                    TreeNodeView draggedTreeView = (TreeNodeView) draggedView;

                    changeNewPotentialParent(draggedTreeView, draggedOverTreeView);
                }
            }

            @Override
            public void onDragOverLeave(View draggedView,
                                        View formerDraggedOverView,
                                        View newAboveView,
                                        View newBelowView) {

                determineNewPotentialParentWithoutCast(draggedView, newAboveView, newBelowView);
            }
        });
    }

    private void determineNewPotentialParentWithoutCast(View draggedView, View newAboveView, View newBelowView) {
        if (draggedView instanceof TreeNodeView) {
            // Es wurde zwischen 2 Einträgen gedropped, nicht auf einen drauf
            if ((newAboveView == null || newAboveView instanceof TreeNodeView)
                    && (newBelowView == null || newBelowView instanceof TreeNodeView)) {

                determineNewPotentialParent(
                        (TreeNodeView) draggedView,
                        (TreeNodeView) newAboveView,
                        (TreeNodeView) newBelowView
                );
            }
        }
    }

    /**
     * In dieser Methode wird der ehemalige potenzielle parent durch den potentiellen neuen treeNodeViewParent ersetzt.
     * Beide Views werden angepasst.
     * Die neue Tiefe des Childs wird ebenfalls berücksichtigt
     *
     * @param treeNodeViewParent neuer potentieller parent für gedraggte view
     */
    private void changeNewPotentialParent(TreeNodeView draggedChildView, @Nullable TreeNodeView treeNodeViewParent) {
        if (potentialNewParent != null) {
            potentialNewParent.unmarkAsNewPotentialParent();
        }
        if (treeNodeViewParent != null) {
            treeNodeViewParent.markAsNewPotentialParent();
            draggedChildView.setNewTreeDepth(treeNodeViewParent.getDepthInTreeView() + 1);
        } else {
            draggedChildView.setNewTreeDepth(LOWEST_DEPTH);
        }
        potentialNewParent = treeNodeViewParent;
    }

    private void determineNewPotentialParent(TreeNodeView draggedView, TreeNodeView aboveView, TreeNodeView belowView) {
        if (aboveView == null) {
            // draggedView befindet sich ganz oben oder ist die einzige Node in der TreeView
            // draggedView wird ein neues Element an oberster Stelle
            changeNewPotentialParent(draggedView, null);
        } else if (belowView == null) {
            // draggedView befindet sich ganz unten
            // draggedView wird Kind des Parents des untersten Eintrags
            changeNewPotentialParent(draggedView, aboveView.getTreeNodeViewParent());
        } else {
            // draggedView befindet sich zwischen 2 Einträgen
            if (aboveView.getDepthInTreeView() >= belowView.getDepthInTreeView()) {
                // draggedView befindet sich zwischen 2 Einträgen gleicher Tiefe oder oberer Eintrag ist tiefer im Baum
                // draggedView wird Kind dieses des Parents des oberen Eintrags
                changeNewPotentialParent(draggedView, aboveView.getTreeNodeViewParent());
            } else {
                // Der unterer Eintrag ist tiefer im Baum als oberer.
                // draggedView wird Kind vom oberen Eintrag
                changeNewPotentialParent(draggedView, aboveView);
            }
        }
    }

    @Override
    public void onTreeNodeClick(TreeNodeView onClickedTreeNodeView) {
        if (onTreeNodeClickListener != null) {
            onTreeNodeClickListener.onTreeNodeClick(onClickedTreeNodeView);
        }
    }

    @Override
    public void onTreeNodeLongClick(TreeNodeView onLongClickedTreeNodeView) {
        if (onTreeNodeClickListener != null) {
            onTreeNodeClickListener.onTreeNodeLongClick(onLongClickedTreeNodeView);
        }
    }

    @Override
    public void onTreeNodeFlingFromRightToLeft(TreeNodeView treeNodeView) {
        if (onTreeNodeClickListener != null) {
            onTreeNodeClickListener.onTreeNodeFlingFromRightToLeft(treeNodeView);
        }
    }

    @Override
    public void onTreeNodeFlingFromLeftToRight(TreeNodeView treeNodeView) {
        if (onTreeNodeClickListener != null) {
            onTreeNodeClickListener.onTreeNodeFlingFromLeftToRight(treeNodeView);
        }
    }

    public void showNewEntrySpaceHolder() {
        newEntrySpaceHolder.setVisibility(VISIBLE);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                TreeView.this.fullScroll(FOCUS_DOWN);
            }
        }, 500);
    }

    public void hideNewEntrySpaceHolder() {
        newEntrySpaceHolder.setVisibility(GONE);
    }

    private TreeNodeView addTreeNodeView(TreeNode treeNode, int startDepth) {
        return addTreeNodeView(treeNode, startDepth, null);
    }

    /**
     * Fügt der TreeView einen neuen Eintrag samt Kindereinträgen hinzu
     *
     * @param treeNode   die hinzugefügt werden soll
     * @param startDepth der treeNode
     * @param viewIndex  Index im LinearLayout, in dem die TreeNodeView hinzugefügt werden soll
     * @return die erzeugte TreeNodeView
     */
    private TreeNodeView addTreeNodeView(TreeNode treeNode, int startDepth, @Nullable Integer viewIndex) {
        TreeNodeView treeNodeView = new TreeNodeView(getContext(), treeNode, startDepth);
        treeNodeView.setOnTreeNodeClickListener(this);

        if (viewIndex != null) {
            dragLinearLayout.addDragView(treeNodeView, treeNodeView.getDragHandlerView(), viewIndex);
        } else {
            dragLinearLayout.addDragView(treeNodeView, treeNodeView.getDragHandlerView());
        }

        if (treeNode.hasChildren()) {
            List<TreeNodeView> treeNodeViewChildren = new ArrayList<>();

            // Die Kinder befinden sich eine Ebene tiefer als treeNodeView, deshalb muss die Tiefe um 1 erhöht werden
            for (TreeNode child : treeNode.getChildren()) {
                if (child.isVisibleForCurrentSearch()) {
                    TreeNodeView treeNodeViewChild = addTreeNodeView(child, startDepth + 1, null);
                    treeNodeViewChildren.add(treeNodeViewChild);
                    treeNodeViewChild.setTreeNodeViewParent(treeNodeView);
                    child.setParent(treeNode);
                }
            }
            treeNodeView.setTreeNodeViewChildren(treeNodeViewChildren);
        }
        return treeNodeView;
    }

    public TreeNodeView addNewEntry() {
        TreeNode newTreeNode = new TreeNode("New Entry");
        newTreeNode.setParent(treeNodeRoot);
        return addTreeNodeView(newTreeNode, LOWEST_DEPTH);
    }

    public void removeTreeNodeView(TreeNodeView currentEditingTreeNoteView) {
        dragLinearLayout.removeDragView(currentEditingTreeNoteView);
    }
}
