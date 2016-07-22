package de.treenote.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import de.treenote.R;
import de.treenote.pojo.TreeNode;
import de.treenote.pojo.TreeNodeRoot;
import mockit.Mock;
import mockit.MockUp;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class TreeViewTest {

    private static final Context DEFAULT_CONTEXT = mock(Context.class);

    /**
     * In diesem Test wird überprüft, ob beim Erstellen des Baumes die treePositions richtig
     * gesetzt werden
     */
    @Test
    public void testSetTreeNodes() throws Exception {

        final List<TreeNodeView> addedTreeNodeViews = new ArrayList<>();

        // DragLinearLayout muss gemockt werden, da sonst im Test Fehler auftreten
        final DragLinearLayout dragLinearLayoutMockedInstance = new MockUp<DragLinearLayout>() {

            @Mock
            public void addDragView(View child, View dragHandle) {
                addedTreeNodeViews.add((TreeNodeView) child);
            }

            @Mock
            public void addDragView(View child, View dragHandle, int viewIndex) {
                addedTreeNodeViews.add((TreeNodeView) child);
            }
        }.getMockInstance();

        new MockUp<TreeNodeView>() {
            @Mock
            View findViewById(int i) {
                switch (i) {
                    case R.id.treeNodeLabelText:
                    case R.id.dateTextView:
                        return mock(TextView.class);
                    case R.id.treeNodeViewRelativeLayout:
                        return mock(RelativeLayout.class);
                    case R.id.expandChildrenButton:
                        return mock(ImageButton.class);
                    case R.id.treeLinesContainer:
                        return mock(ViewGroup.class);
                    case R.id.treeNodeCheckBox:
                        return mock(CheckBox.class);
                    default:
                        return mock(View.class);
                }
            }
        };

        TreeView treeView = new TreeView(DEFAULT_CONTEXT);

        new MockUp<TreeView>(treeView) {

            @Mock
            View findViewById(int i) {
                switch (i) {
                    case R.id.dragLinearLayout:
                        return dragLinearLayoutMockedInstance;
                    case R.id.treeNodeLabelText:
                        return mock(TextView.class);
                    default:
                        return mock(View.class);
                }
            }
        };

        new MockUp<TreeNodeView>() {
            @Mock
            int getResourceColor(int colorID) {
                return 0;
            }
        };

        TreeNodeRoot treeNodes = new TreeNodeRoot();

        for (int i = 1; i < 4; i++) {
            TreeNode treeNode = treeNode(i);
            treeNodes.addChild(treeNode);
            for (int ii = 1; ii < 7; ii++) {
                TreeNode treeNodeChild = treeNode(i, ii);
                treeNode.addChild(treeNodeChild);
                for (int iii = 1; iii < 6; iii++) {
                    TreeNode treeNodeChildOfChild = treeNode(i, ii, iii);
                    treeNodeChild.addChild(treeNodeChildOfChild);
                }
            }
        }

        treeView.setTreeNodeRootInView(treeNodes);
        assertEquals(addedTreeNodeViews.size(), 111);

        for (TreeNodeView treeNodeView : addedTreeNodeViews) {
            int depthInTreeView = treeNodeView.getDepthInTreeView();
            int expectedDepth = computeDepth(treeNodeView.getAssociatedTreeNode().getText());
            assertEquals(depthInTreeView, expectedDepth);
        }
    }

    /**
     * @param label der TextView
     * @return Anzahl der Punkte (".") -1
     */
    private int computeDepth(String label) {
        return label.split("\\.").length - 1;
    }

    private static TreeNode treeNode(Integer... treePosition) {
        return new TreeNode(toTreePositionString(treePosition));
    }

    private static String toTreePositionString(Integer... treePosition) {
        return Joiner.on(".").join(treePosition);
    }
}