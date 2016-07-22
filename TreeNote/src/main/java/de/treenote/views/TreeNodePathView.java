package de.treenote.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.treenote.R;
import de.treenote.pojo.TreeNode;

public class TreeNodePathView extends HorizontalScrollView {

    private ViewGroup viewContainer;

    public TreeNodePathView(Context context) {
        super(context);
        createView();
    }

    public TreeNodePathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public TreeNodePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createView();
    }

    private void createView() {
        viewContainer = createContainer();
        addView(viewContainer);
    }

    public void setTreePath(TreeNode treeNode) {
        viewContainer.removeAllViews();

        TreeNode currentParent = treeNode;

        TextView last_added_separator = null;
        while (!currentParent.isTreeNodeRoot()) {
            viewContainer.addView(newTextView(currentParent), 0);
            last_added_separator = newTextView(">");
            viewContainer.addView(last_added_separator, 0);
            currentParent = currentParent.getParent();
        }
        if (last_added_separator != null) {
            viewContainer.removeView(last_added_separator);
            viewContainer.addView(newTextView(getContext().getString(R.string.current_root)), 0);
        }

        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(FOCUS_RIGHT);
            }
        });
    }

    private View newTextView(TreeNode treeNode) {
        return newTextView(treeNode.getText());
    }

    private TextView newTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setPadding(10, 0, 0, 0);
        textView.setText(text);
        return textView;
    }

    private ViewGroup createContainer() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }
}
