package com.gionee.note.ai.algorithm;

import android.util.SparseArray;

public class TreeNode {
    private boolean end = false;
    private SparseArray<TreeNode> subNodes = new SparseArray();

    void setSubNode(int index, TreeNode node) {
        this.subNodes.put(index, node);
    }

    TreeNode getSubNode(int index) {
        return (TreeNode) this.subNodes.get(index);
    }

    boolean isKeywordEnd() {
        return this.end;
    }

    void setKeywordEnd(boolean end) {
        this.end = end;
    }
}
