package com.gionee.note.ai.algorithm;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DFA {
    private String mCharset = "GBK";
    private ByteBuffer mKeywordBuffer = ByteBuffer.allocate(128);
    private TreeNode mRootNode = new TreeNode();

    public void createKeywordTree(List<String> keywordList) throws UnsupportedEncodingException {
        for (String keyword : keywordList) {
            if (keyword != null) {
                addKeyword(keyword.trim());
            }
        }
    }

    public void addKeyword(String keyword) throws UnsupportedEncodingException {
        byte[] bytes = keyword.getBytes(this.mCharset);
        TreeNode tempNode = this.mRootNode;
        for (int i = 0; i < bytes.length; i++) {
            int index = bytes[i] & 255;
            TreeNode node = tempNode.getSubNode(index);
            if (node == null) {
                node = new TreeNode();
                tempNode.setSubNode(index, node);
            }
            tempNode = node;
            if (i == bytes.length - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    public ArrayList<String> searchKeyword(String text) throws UnsupportedEncodingException {
        return merge(searchKeyword(text.getBytes(this.mCharset)));
    }

    private ArrayList<String> merge(ArrayList<String> keywords) {
        ArrayList<String> target = new ArrayList();
        Iterator i$ = keywords.iterator();
        while (i$.hasNext()) {
            String k = (String) i$.next();
            if (!isExist(target, k)) {
                target.add(k);
            }
        }
        return target;
    }

    private boolean isExist(ArrayList<String> list, String key) {
        Iterator i$ = list.iterator();
        while (i$.hasNext()) {
            if (key.equals((String) i$.next())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> searchKeyword(byte[] bytes) {
        ArrayList words = new ArrayList();
        if (!(bytes == null || bytes.length == 0)) {
            TreeNode tempNode = this.mRootNode;
            int rollback = 0;
            int position = 0;
            ByteBuffer keywordBuffer = this.mKeywordBuffer;
            keywordBuffer.clear();
            while (position < bytes.length) {
                int index = bytes[position] & 255;
                keywordBuffer.put(bytes[position]);
                tempNode = tempNode.getSubNode(index);
                if (tempNode == null) {
                    position -= rollback;
                    rollback = 0;
                    tempNode = this.mRootNode;
                    keywordBuffer.clear();
                } else if (tempNode.isKeywordEnd()) {
                    keywordBuffer.flip();
                    String keyword = Charset.forName(this.mCharset).decode(keywordBuffer).toString();
                    keywordBuffer.limit(keywordBuffer.capacity());
                    words.add(keyword);
                    rollback = 1;
                } else {
                    rollback++;
                }
                position++;
            }
        }
        return words;
    }

    public void setmCharset(String mCharset) {
        this.mCharset = mCharset;
    }
}
