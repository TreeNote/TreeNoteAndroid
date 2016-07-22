package de.treenote.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.testng.annotations.Test;

import java.util.ArrayList;

import de.treenote.R;
import de.treenote.pojo.TreeNode;
import mockit.Mock;
import mockit.MockUp;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TreeNodeViewTest {

    private static Context CONTEXT_MOCK = mock(Context.class);

    @Test
    public void testRemoveChild() throws Exception {

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

            @Mock
            int getResourceColor(int colorID) {
                return 0;
            }
        };

        TreeNode root = new TreeNode("Root");
        root.addChild(new TreeNode("1"));
        root.addChild(new TreeNode("2"));
        root.addChild(new TreeNode("3"));
        root.addChild(new TreeNode("4"));

        ArrayList<TreeNodeView> treeNodeViewChildren = new ArrayList<>();
        treeNodeViewChildren.add(new TreeNodeView(CONTEXT_MOCK, new TreeNode("1"), 1));
        treeNodeViewChildren.add(new TreeNodeView(CONTEXT_MOCK, new TreeNode("2"), 1));
        treeNodeViewChildren.add(new TreeNodeView(CONTEXT_MOCK, new TreeNode("3"), 1));
        treeNodeViewChildren.add(new TreeNodeView(CONTEXT_MOCK, new TreeNode("4"), 1));

        TreeNodeView rootView = new TreeNodeView(CONTEXT_MOCK, root, 0);
        rootView.setTreeNodeViewChildren(treeNodeViewChildren);

        assertEquals(rootView.getTreeNodeViewChildren().size(), 4);
        assertTrue(rootView.removeTreeNodeViewChild(new TreeNodeView(CONTEXT_MOCK, new TreeNode("2"), 1)));
        assertEquals(rootView.getTreeNodeViewChildren().size(), 3);
    }
}