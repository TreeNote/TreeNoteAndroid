package de.treenote.pojo;

import android.location.Location;

import java.util.Date;

/**
 * Hier werden die TreeNodes der obersten Ebene gespeichert, damit keine Liste von obersten TreeNodes erstellt werden
 * muss.
 */
public class TreeNodeRoot extends TreeNode {

    public TreeNodeRoot() {
        super("ROOT");
    }

    public boolean isTreeNodeRoot() {
        return true;
    }

    @Override
    public void setType(TreeNodeType type) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }

    @Override
    public void setText(String text) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }

    @Override
    public void setStartDate(Date startDate) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }

    @Override
    public void setCreationDatetime(Date creationDatetime) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }

    @Override
    public void setEditDatetime(Date editDatetime) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }

    @Override
    public void setParent(TreeNode parent) {
        throw new RuntimeException("TreeNodeRoot can not be modified!");
    }
}
