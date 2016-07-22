package de.treenote.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import de.treenote.R;
import de.treenote.activities.SingleFragmentActivity;
import de.treenote.listener.OnFloatingActionButtonClickListener;
import de.treenote.listener.OnTreeNodeClickListener;
import de.treenote.pojo.TreeNode;
import de.treenote.services.SyncService;
import de.treenote.util.Constants;
import de.treenote.util.TreeNoteDataHolder;
import de.treenote.views.TreeNodePathView;
import de.treenote.views.TreeNodeView;
import de.treenote.views.TreeView;
import de.treenote.views.TreeViewContainer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.google.common.base.Preconditions.checkNotNull;

public class TreeFragment extends Fragment implements OnTreeNodeClickListener {

    private TreeView treeView;
    private Fragment detailFragment;
    private FrameLayout detailFragmentContainer;
    private FragmentManager fragmentManager;
    private TreeViewContainer treeViewContainer;
    private View floatingActionButtonCheck;
    private View floatingActionButtonClear;
    public TreeNode treeNodeRoot;
    private GestureDetector gestureDetector;
    private TreeNodePathView treeNodePathView;
    public static final String TITLE = "TITLE";

    public static TreeFragment newInstance(String title) {
        TreeFragment fragment = new TreeFragment();
        Bundle args = new Bundle(1);
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        treeNodeRoot = TreeNoteDataHolder.getTreeNodeRoot();

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityX > Constants.MIN_VELOCITY_X && Math.abs(velocityY) < Constants.MAX_VELOCITY_Y) {
                    setParentAsRoot();
                    return true;
                } else {
                    return false;
                }
            }
        };

        gestureDetector = new GestureDetector(getActivity().getApplicationContext(), gestureListener);
    }

    private void setParentAsRoot() {
        // fling von links nach rechts
        if (treeNodeRoot.isTreeNodeRoot()) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    R.string.highest_tree_node_reached,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.going_up, Toast.LENGTH_SHORT).show();
            setTreeNodeRootInTreeFragment(treeNodeRoot.getParent());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drag_tree_layout_content, container, false);
        treeViewContainer = (TreeViewContainer) view.findViewById(R.id.treeViewContainer);
        checkNotNull(treeViewContainer, "TreeViewContainer is null!!!");
        treeView = treeViewContainer.getTreeView();
        treeNodePathView = treeViewContainer.getTreeNodePathView();
        detailFragmentContainer = (FrameLayout) view.findViewById(R.id.detailFragmentContainer);
        view.findViewById(R.id.treeViewInContainer).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getArguments().getString(TITLE));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (treeNodeRoot != null) {
            initializeTreeView(treeNodeRoot);
        }
    }

    private void initializeTreeView(TreeNode treeNodeRoot) {
        setTreeNodeRootInTreeFragment(treeNodeRoot);
        treeView.setOnTreeNodeClickListener(this);

        fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                setLayout();
            }
        });

        floatingActionButtonCheck = getActivity().findViewById(R.id.floatingActionButtonCheck);
        floatingActionButtonClear = getActivity().findViewById(R.id.floatingActionButtonClear);

        treeViewContainer.setOnFloatingActionButtonClickListener(new OnFloatingActionButtonClickListener() {

            private TreeNodeView currentEditingTreeNoteView;

            @Override
            public void onFloatingActionButtonAddClicked(View floatingActionButton) {

                if (currentEditingTreeNoteView != null) {
                    confirmNewEntry();
                }

                floatingActionButtonCheck.setVisibility(View.VISIBLE);
                floatingActionButtonClear.setVisibility(View.VISIBLE);

                currentEditingTreeNoteView = treeView.addNewEntry();
                currentEditingTreeNoteView.getLabelTextView().setVisibility(View.GONE);
                View editTextView = currentEditingTreeNoteView.getEditTextView();
                editTextView.setVisibility(View.VISIBLE);
                editTextView.requestFocus();
                InputMethodManager inputMethodManager
                        = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);

                treeView.showNewEntrySpaceHolder();
            }

            @Override
            public void onFloatingActionButtonCheckClicked(View floatingActionButton) {
                confirmNewEntry();
                closeEditing();
            }

            @Override
            public void onFloatingActionButtonClearClicked(View floatingActionButton) {
                treeView.removeTreeNodeView(currentEditingTreeNoteView);
                closeEditing();
            }

            private void confirmNewEntry() {
                EditText editTextView = (EditText) currentEditingTreeNoteView.getEditTextView();
                String newText = editTextView.getText().toString();
                currentEditingTreeNoteView.setText(newText);
                TreeNode associatedTreeNode = currentEditingTreeNoteView.getAssociatedTreeNode();
                TreeNoteDataHolder.addTreeNode(associatedTreeNode);

                TreeFragment.this.treeNodeRoot.addChild(associatedTreeNode);
                associatedTreeNode.setParent(TreeFragment.this.treeNodeRoot);

                associatedTreeNode.setText(newText);
                editTextView.setVisibility(View.GONE);
                currentEditingTreeNoteView.getLabelTextView().setVisibility(View.VISIBLE);
            }

            private void closeEditing() {
                floatingActionButtonCheck.setVisibility(View.GONE);
                floatingActionButtonClear.setVisibility(View.GONE);

                InputMethodManager inputMethodManager
                        = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentEditingTreeNoteView.getWindowToken(), 0);

                currentEditingTreeNoteView = null;
                treeView.hideNewEntrySpaceHolder();
            }
        });
    }


    @Override
    public void onTreeNodeClick(TreeNodeView onClickedTreeNodeView) {
        TreeNode associatedTreeNode = onClickedTreeNodeView.getAssociatedTreeNode();

        if (getActivity().findViewById(R.id.detailFragmentContainer) != null) {
            // detailFragmentContainer ist vorhanden
            detailFragment = DetailFragment.newInstance(associatedTreeNode);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.detailFragmentContainer, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            fragmentManager.executePendingTransactions();
        } else {
            // detailFragmentContainer ist nicht vorhanden
            Intent intent = new Intent(getActivity(), SingleFragmentActivity.class);
            intent.putExtra(SingleFragmentActivity.FRAGMENT_CLASS, DetailFragment.class);
            intent.putExtra(DetailFragment.CORRESPONDING_TREE_NODE_ID_KEY, associatedTreeNode.getID());
            startActivity(intent);
        }
    }

    @Override
    public void onTreeNodeLongClick(final TreeNodeView onLongClickedTreeNodeView) {
        // nothing
    }

    @Override
    public void onTreeNodeFlingFromRightToLeft(TreeNodeView treeNodeView) {
        Toast.makeText(getActivity().getApplicationContext(), R.string.going_down, Toast.LENGTH_SHORT).show();
        treeNodeRoot = treeNodeView.getAssociatedTreeNode();
        setTreeNodeRootInTreeFragment(treeNodeRoot);
    }

    private void setTreeNodeRootInTreeFragment(TreeNode treeNode) {
        treeNodeRoot = treeNode;
        treeView.setTreeNodeRootInView(treeNode);
        treeNodePathView.setTreePath(treeNode);
    }

    @Override
    public void onTreeNodeFlingFromLeftToRight(TreeNodeView treeNodeView) {
        setParentAsRoot();
    }

    @Override
    public void onStop() {
        super.onStop();

        Intent saveTreeIntent = new Intent(getActivity(), SyncService.class);
        saveTreeIntent.setAction(SyncService.SAVE_ACTION);
        getActivity().startService(saveTreeIntent);
    }

    private void setLayout() {
        // Determine whether the DetailFragment has been added
        if (detailFragment != null) {
            if (!detailFragment.isAdded()) {
                // Make the TreeFragment occupy the entire layout
                treeViewContainer.setLayoutParams(new LayoutParams(
                        MATCH_PARENT, MATCH_PARENT));
                detailFragmentContainer.setLayoutParams(new LayoutParams(0,
                        MATCH_PARENT));
            } else {
                // Make the TreeLayout take 1/2 of the layout's width
                treeViewContainer.setLayoutParams(new LayoutParams(0,
                        MATCH_PARENT, 1f));

                // Make the DetailLayout take 1/2 of the layout's width
                detailFragmentContainer.setLayoutParams(new LayoutParams(0,
                        MATCH_PARENT, 1f));
            }
        }
    }
}
