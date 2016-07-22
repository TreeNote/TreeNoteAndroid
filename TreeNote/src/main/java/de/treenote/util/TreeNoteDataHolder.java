package de.treenote.util;

import java.util.HashMap;
import java.util.Map;

import de.treenote.pojo.TreeNode;
import de.treenote.pojo.TreeNodeRoot;
import lombok.Getter;

/**
 * Aufgaben:
 * <p>
 * Hält alle TreeNodes
 * Verteilung der IDs der TreeNodes
 * <p>
 * siehe http://stackoverflow.com/questions/4878159/whats-the-best-way-to-share-data-between-activities
 */
public class TreeNoteDataHolder {

    private static Map<Integer, TreeNode> treeNodeMap = new HashMap<>();

    private static int highestID;

    @Getter
    private static TreeNodeRoot treeNodeRoot;

    private TreeNoteDataHolder() {
    }

    /**
     * Speichert alle vorhanden TreeNodes in die treeNodeMap. Beim Setzen wird die treeNodeMap geleert.
     *
     * @param treeNodeRoot mit allen vorhandenen Einträgen als Kinder
     */
    public static void setData(TreeNodeRoot treeNodeRoot) {
        treeNodeMap.clear();
        highestID = 0;
        TreeNoteDataHolder.treeNodeRoot = treeNodeRoot;
        putAllTreeNodeChildrenInMap(treeNodeRoot);
    }

    public static void deleteTreeNode(int treeNodeID) {
        TreeNode treeNode = getTreeNode(treeNodeID);
        treeNode.getParent().removeChild(treeNode);
        treeNodeMap.remove(treeNodeID);
    }

    public static TreeNode getTreeNode(int treeNodeID) {
        return treeNodeMap.get(treeNodeID);
    }

    /**
     * Setzt in der TreeNode eine ID und fügt sie der treeNodeMap hinzu
     *
     * @param treeNode die, eine ID erhält und der Map hinzugefügt werden soll
     */
    public static void addTreeNode(TreeNode treeNode) {
        treeNode.setID(++highestID);
        treeNodeMap.put(treeNode.getID(), treeNode);
    }

    private static void putAllTreeNodeChildrenInMap(TreeNode treeNode) {
        treeNodeMap.put(treeNode.getID(), treeNode);
        checkHighestID(treeNode.getID());
        if (treeNode.hasChildren()) {
            for (TreeNode treeNodeChild : treeNode.getChildren()) {
                putAllTreeNodeChildrenInMap(treeNodeChild);
            }
        }
    }

    private static void checkHighestID(int id) {
        highestID = Math.max(id, highestID);
    }
}
