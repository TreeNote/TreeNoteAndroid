package de.treenote.pojo;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.treenote.listener.TreeNodeChangeListener;
import de.treenote.util.CurrentFilterHolder;
import de.treenote.util.TreeNoteUtil;
import lombok.Data;

@Data
public class TreeNode implements Serializable {

    private int ID;

    private TreeNodeType type = TreeNodeType.NOTE;
    private String text;
    private Date startDate;
    private String imageFileName;

    private Date creationDatetime;
    private Date editDatetime;

    private Double latitude;
    private Double longitude;

    private transient TreeNode parent;

    private transient TreeNodeChangeListener treeNodeChangeListener;

    private List<TreeNode> children;

    public TreeNode(String text) {
        this.text = text;
        creationDatetime = TreeNoteUtil.now();
        editDatetime = TreeNoteUtil.now();
        children = new ArrayList<>();
    }

    public void addChild(TreeNode childToAdd) {
        children.add(childToAdd);
    }

    public void addChild(TreeNode childToAdd, int index) {
        children.add(index, childToAdd);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean removeChild(TreeNode child) {
        return children.remove(child);
    }

    @Override
    public String toString() {
        return "TreeNode " + text + "|Parent: " + parent.getText();
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    public int getPositionOfChild(TreeNode treeNodeChild) {
        return children.indexOf(treeNodeChild);
    }

    public void setText(String text) {
        editDatetime = TreeNoteUtil.now();
        this.text = text;
        if (treeNodeChangeListener != null) {
            treeNodeChangeListener.onTextChangedListener(this);
        }
    }

    public void setType(TreeNodeType newType) {
        editDatetime = TreeNoteUtil.now();
        type = newType;
    }

    public void setStartDate(Date date) {
        editDatetime = TreeNoteUtil.now();
        startDate = date;
    }

    public void setLatitude(Double lat) {
        editDatetime = TreeNoteUtil.now();
        latitude = lat;
    }

    public void setImageFileName(String fileName) {
        editDatetime = TreeNoteUtil.now();
        imageFileName = fileName;
    }

    /**
     * @return true, wenn diese treeNode die logische TreeNodeRoot ist (unabhängig von der root in der View). <br>
     * false, andernfalls
     */
    public boolean isTreeNodeRoot() {
        return false;
    }

    public boolean isVisibleForCurrentSearch() {
        if (CurrentFilterHolder.currentFilter == CurrentFilterHolder.Filter.None) {
            return true;
        }

        for (TreeNode child : children) {
            if (child.isVisibleForCurrentSearch()) {
                return true;
            }
        }
        if (CurrentFilterHolder.currentFilter == CurrentFilterHolder.Filter.Today) {
            return startDate != null && startDate.before(TreeNoteUtil.tomorrow());
        } else if (CurrentFilterHolder.currentFilter == CurrentFilterHolder.Filter.Todos) {
            return type == TreeNodeType.TODO;
        } else { // CurrentFilterHolder.currentFilter == CurrentFilterHolder.Filter.Upcoming
            return startDate != null;
        }
    }
}
