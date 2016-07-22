package de.treenote.pojo;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TreeNodeTest {

    @Test
    public void testRemoveChild() throws Exception {

        TreeNode root = new TreeNode("Root");
        root.addChild(new TreeNode("1"));
        TreeNode child_2 = new TreeNode("2");
        root.addChild(child_2);
        root.addChild(new TreeNode("3"));
        root.addChild(new TreeNode("4"));

        assertEquals(root.getChildren().size(), 4);
        assertTrue(root.removeChild(child_2));
        assertEquals(root.getChildren().size(), 3);
    }
}