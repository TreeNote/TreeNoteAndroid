package de.treenote.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.treenote.R;
import de.treenote.listener.OnTreeNodeClickListener;
import de.treenote.listener.TreeNodeChangeListener;
import de.treenote.pojo.TreeNode;
import de.treenote.pojo.TreeNodeType;
import de.treenote.util.Constants;
import lombok.Getter;
import lombok.Setter;

/**
 * Diese Klasse repräsentiert nur die View eines einzelnen TreeNodes.
 * Die Kinder des TreeNodes werden nicht betrachet
 * TreeNodeView wird in der Klasse TreeView benutzt
 */
@SuppressLint("ViewConstructor")
public class TreeNodeView extends RelativeLayout {

    public static final int EXPAND_BUTTON_NOT_VISIBLE = INVISIBLE;
    @Getter
    private TreeNode associatedTreeNode;

    @Getter
    private boolean expanded = false;

    @Getter
    @Setter
    private TreeNodeView treeNodeViewParent;

    @Setter
    @Getter
    private List<TreeNodeView> treeNodeViewChildren;

    @Getter
    private int depthInTreeView;

    @Getter
    private View dragHandlerView;

    @Setter
    private OnTreeNodeClickListener onTreeNodeClickListener;

    @Getter
    private TextView labelTextView;
    private ImageButton expandChildrenButton;
    private ViewGroup treeLinesContainer;
    private TextView dateTextView;
    private CheckBox treeNodeCheckBox;

    public TreeNodeView(Context context, TreeNode associatedTreeNode, int depthInTreeView) {
        super(context);

        checkDepthInTreeView(depthInTreeView);

        this.depthInTreeView = depthInTreeView;
        this.associatedTreeNode = associatedTreeNode;
        this.expanded = true;
        treeNodeViewChildren = new ArrayList<>();
        View.inflate(context, R.layout.tree_node_view_layout, this);
        createView();
    }

    private void createView() {
        setBackgroundColor(getResourceColor(R.color.tree_node_view_background));

        labelTextView = (TextView) findViewById(R.id.treeNodeLabelText);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        treeLinesContainer = (ViewGroup) findViewById(R.id.treeLinesContainer);
        treeNodeCheckBox = (CheckBox) findViewById(R.id.treeNodeCheckBox);

        setText(associatedTreeNode.getText());
        setNewTreeDepth(depthInTreeView);

        if (associatedTreeNode.getStartDate() != null){
            CharSequence dateAsString = DateFormat.format("dd.MM.yyyy", associatedTreeNode.getStartDate());
            dateTextView.setText(dateAsString);
            dateTextView.setVisibility(VISIBLE);
        }

        if (associatedTreeNode.getType() != TreeNodeType.NOTE){
            treeNodeCheckBox.setChecked(associatedTreeNode.getType() == TreeNodeType.DONE);
            treeNodeCheckBox.setVisibility(VISIBLE);
        }

        dragHandlerView = findViewById(R.id.dragButton);
        dragHandlerView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        expandChildrenButton = (ImageButton) findViewById(R.id.expandChildrenButton);
        expandChildrenButton.setBackgroundColor(Color.argb(0, 0, 0, 0));
        expandChildrenButton.setVisibility(associatedTreeNode.hasChildren() ? VISIBLE : EXPAND_BUTTON_NOT_VISIBLE);

        addGestureListener();
        addListeners();
    }

    private void addGestureListener() {
        final GestureDetector gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (onTreeNodeClickListener != null && Math.abs(velocityY) < Constants.MAX_VELOCITY_Y) {
                    if (velocityX < -Constants.MIN_VELOCITY_X) {
                        // fling von rechts nach links
                        onTreeNodeClickListener.onTreeNodeFlingFromRightToLeft(TreeNodeView.this);
                        return true;

                    } else if (velocityX > Constants.MIN_VELOCITY_X) {
                        // fling von links nach rechts
                        onTreeNodeClickListener.onTreeNodeFlingFromLeftToRight(TreeNodeView.this);
                        return true;

                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void addListeners() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTreeNodeClickListener != null) {
                    onTreeNodeClickListener.onTreeNodeClick(TreeNodeView.this);
                }
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onTreeNodeClickListener != null) {
                    onTreeNodeClickListener.onTreeNodeLongClick(TreeNodeView.this);
                }
                return true;
            }
        });

        expandChildrenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expanded) {
                    hideChildren();
                    setExpanded(false);
                } else {
                    showChildren();
                    setExpanded(true);
                }
            }
        });

        associatedTreeNode.setTreeNodeChangeListener(new TreeNodeChangeListener() {
            @Override
            public void onTextChangedListener(TreeNode treeNode) {
                labelTextView.setText(treeNode.getText());
            }
        });

        treeNodeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                associatedTreeNode.setType(isChecked ? TreeNodeType.DONE : TreeNodeType.TODO);
            }
        });
    }

    private void setExpanded(boolean expanded) {
        int newImage = expanded ? R.drawable.ic_expand_more_grey_800_36dp : R.drawable.ic_chevron_right_grey_800_36dp;
        expandChildrenButton.setImageResource(newImage);
        this.expanded = expanded;
    }

    private void showChildren() {
        DragLinearLayout dragLinearLayoutParent = getDragLinearLayoutParent();
        if (dragLinearLayoutParent != null) {
            showChildren(dragLinearLayoutParent);
        }
    }

    public void showChildren(DragLinearLayout dragLinearLayoutParent) {
        if (treeNodeViewChildren != null && !treeNodeViewChildren.isEmpty()) {
            int parentIndex = getIndexOfView(this, dragLinearLayoutParent);
            for (TreeNodeView treeNodeViewChild : treeNodeViewChildren) {
                treeNodeViewChild.setNewTreeDepth(depthInTreeView + 1);
                dragLinearLayoutParent.addDragView(treeNodeViewChild,
                        treeNodeViewChild.getDragHandlerView(),
                        ++parentIndex
                );
            }
            setExpanded(true);
        }
    }

    private int getIndexOfView(TreeNodeView treeNodeView, DragLinearLayout dragLinearLayoutParent) {
        int indexOfChild = dragLinearLayoutParent.indexOfChild(treeNodeView);
        if (indexOfChild >= 0) {
            return indexOfChild;
        } else {
            throw new RuntimeException(
                    "Child of " + treeNodeView.associatedTreeNode.getText() + " not found");
        }
    }

    public void hideChildren() {
        DragLinearLayout dragLinearLayoutParent = getDragLinearLayoutParent();
        if (dragLinearLayoutParent != null) {
            hideChildren(dragLinearLayoutParent);
        }
    }

    private void hideChildren(DragLinearLayout dragLinearLayoutParent) {
        if (treeNodeViewChildren != null && !treeNodeViewChildren.isEmpty()) {
            for (TreeNodeView treeNodeViewChild : treeNodeViewChildren) {
                dragLinearLayoutParent.removeDragView(treeNodeViewChild);
                treeNodeViewChild.hideChildren(dragLinearLayoutParent);
            }
            setExpanded(false);
        }
    }

    private DragLinearLayout getDragLinearLayoutParent() {
        return (DragLinearLayout) getParent();
    }

    public View getEditTextView() {
        return findViewById(R.id.treeNodeEditText);
    }

    public void setNewTreeDepth(int depthInTreeView) {

        checkDepthInTreeView(depthInTreeView);
        this.depthInTreeView = depthInTreeView;

        treeLinesContainer.removeAllViews();

        for (int i = 0; i < depthInTreeView; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.transparent);
            treeLinesContainer.addView(imageView);
        }
    }

    private int getResourceColor(int colorID) {
        //noinspection deprecation
        return getResources().getColor(colorID);
    }

    public void setText(String text) {
        associatedTreeNode.setText(text);
        labelTextView.setText(text);
    }

    private void checkDepthInTreeView(int depthInTreeView) {
        if (depthInTreeView < 0) {
            throw new IllegalArgumentException(
                    "depthInTreeView must be greater or equal 0, current value: " + depthInTreeView);
        }
    }

    public boolean removeTreeNodeViewChild(TreeNodeView child) {
        boolean result = treeNodeViewChildren.remove(child);
        result &= associatedTreeNode.removeChild(child.getAssociatedTreeNode());
        if (treeNodeViewChildren.size() == 0) {
            expandChildrenButton.setVisibility(EXPAND_BUTTON_NOT_VISIBLE);
        }
        return result;
    }

    public void addTreeNodeViewChild(TreeNodeView child) {
        treeNodeViewChildren.add(child);
        if (treeNodeViewChildren.size() == 1) { // Die TreeNodeView hat ab jetzt Kinder
            expanded = true;
            expandChildrenButton.setVisibility(VISIBLE);
        } else if (!expanded) { // Kinder werden nicht angezeigt
            hideChildren();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNodeView that = (TreeNodeView) o;

        return associatedTreeNode.equals(that.associatedTreeNode);
    }

    @Override
    public int hashCode() {
        return associatedTreeNode.hashCode();
    }

    public void unmarkAsNewPotentialParent() {
        setBackgroundColor(getResourceColor(R.color.tree_node_view_background));
    }

    public void markAsNewPotentialParent() {
        setBackgroundColor(getResourceColor(R.color.tree_node_view_mark_as_potential_parent));
    }

    /**
     * In dieser Methode wird die neue Elternschaft umgesetzt.
     * This bekommt ein neues Parent bekommt
     * Ist newParentView null, so wird treeNodeRoot der neue Parent der associatedTreeNode
     * <p/>
     * belowTreeNodeView muss angegeben werden, damit die Reihenfolge der Einträge beachtet wird
     * <p/>
     * Der ehemalige Elter verliert this als Kind
     * Die Logik (associatedTreeNode wird ebenfalls behandelt)
     *
     * @param newTreeNodeViewParent, die this als neues Kind bekommt
     * @param belowTreeNodeView,     die unter this ist
     * @param treeNodeRoot           wird als Parent des associatedTreeNode gesetzt, falls newTreeNodeViewParent
     */
    public void changeParent(@Nullable TreeNodeView newTreeNodeViewParent,
                             TreeNodeView belowTreeNodeView,
                             TreeNode treeNodeRoot) {

        TreeNodeView oldViewParent = this.getTreeNodeViewParent();

        //remove this from old parent in view
        if (oldViewParent != null) {
            oldViewParent.removeTreeNodeViewChild(this);
        }

        //remove this from old parent in logic
        associatedTreeNode.getParent().removeChild(associatedTreeNode);

        // neuem Elter das Kind hinzufügen in der View
        if (newTreeNodeViewParent != null) {
            newTreeNodeViewParent.addTreeNodeViewChild(this);
        }

        TreeNode newParent
                = newTreeNodeViewParent == null ? treeNodeRoot : newTreeNodeViewParent.getAssociatedTreeNode();

        // neue Position in der Liste der Kinder bestimmen
        int positionOfChild;
        if (belowTreeNodeView != null) {
            TreeNode belowTreeNode = belowTreeNodeView.getAssociatedTreeNode();
            positionOfChild = newParent.getPositionOfChild(belowTreeNode);
        } else {
            positionOfChild = -1;
        }

        // neuem Elter das Kind hinzufügen in der Logik
        if (positionOfChild >= 0) {
            newParent.addChild(this.associatedTreeNode, positionOfChild);
        } else {
            newParent.addChild(this.associatedTreeNode);
        }

        // dem Kind neue Eltern setzen in der View
        this.treeNodeViewParent = newTreeNodeViewParent;

        // dem Kind neue Eltern setzen in der Logik
        this.associatedTreeNode.setParent(newParent);
    }

    /**
     * Entfernt diesen und alle untergeordneten Knoten vom Baum in der View und in der Logik
     */
    public void deleteFromTree() {
        deleteFromTree(getDragLinearLayoutParent());
    }

    private void deleteFromTree(DragLinearLayout dragLinearLayoutParent) {

        if (treeNodeViewParent != null) {
            treeNodeViewParent.removeTreeNodeViewChild(this);
        } else {
            // wird normalerweise in removeTreeNodeViewChild gemacht
            associatedTreeNode.getParent().removeChild(associatedTreeNode);
        }

        dragLinearLayoutParent.removeDragView(this);

        if (treeNodeViewChildren != null && !treeNodeViewChildren.isEmpty()) {
            // copyOf muss gemacht werden, da es sont zu einer ConcurrentModificationException
            for (TreeNodeView treeNodeViewChild : ImmutableList.copyOf(treeNodeViewChildren)) {
                treeNodeViewChild.deleteFromTree(dragLinearLayoutParent);
            }
        }
    }
}
