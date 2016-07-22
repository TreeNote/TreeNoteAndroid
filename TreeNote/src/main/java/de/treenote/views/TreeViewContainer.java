package de.treenote.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import de.treenote.R;
import de.treenote.listener.OnFloatingActionButtonClickListener;
import lombok.Getter;

public class TreeViewContainer extends RelativeLayout {

    @Getter
    private TreeView treeView;

    @Getter
    private TreeNodePathView treeNodePathView;

    private OnFloatingActionButtonClickListener onFloatingActionButtonClickListener;

    public TreeViewContainer(Context context) {
        super(context);
        createView();
    }

    public TreeViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    public TreeViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createView();
    }

    private void createView() {
        View.inflate(getContext(), R.layout.tree_view_container, this);
        treeNodePathView = (TreeNodePathView) findViewById(R.id.treeNodePathView);
        treeView = (TreeView) findViewById(R.id.treeViewInContainer);
        findViewById(R.id.floatingActionButtonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFloatingActionButtonClickListener.onFloatingActionButtonAddClicked(view);
            }
        });

        findViewById(R.id.floatingActionButtonCheck).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFloatingActionButtonClickListener.onFloatingActionButtonCheckClicked(view);
            }
        });

        findViewById(R.id.floatingActionButtonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFloatingActionButtonClickListener.onFloatingActionButtonClearClicked(view);
            }
        });
    }

    public void setOnFloatingActionButtonClickListener(OnFloatingActionButtonClickListener listener) {
        this.onFloatingActionButtonClickListener = listener;
    }
}
