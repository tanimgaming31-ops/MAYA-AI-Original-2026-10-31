package com.tanimworld.maya;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** Optional explicit user-authorized text-tree help; NOT a secret screen monitor. */
public final class MayaAccessibilityService extends AccessibilityService {
    public static volatile MayaAccessibilityService active;
    @Override protected void onServiceConnected() { super.onServiceConnected(); active = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() { active = null; super.onDestroy(); }

    public String inspect() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return "Screen accessibility tree is unavailable.";
        StringBuilder out = new StringBuilder(); walk(root, out, 0);
        return out.length() == 0 ? "No readable screen text." : out.toString();
    }
    private void walk(AccessibilityNodeInfo n, StringBuilder out, int depth) {
        if (n == null || depth > 18 || out.length() > 3500) return;
        if (n.isVisibleToUser()) {
            CharSequence label = n.getText();
            if ((label == null || label.length() == 0) && n.getContentDescription() != null) label = n.getContentDescription();
            if (label != null && label.length() > 0 && !n.isPassword()) out.append(label.toString().replace('\n',' ')).append(" | ");
        }
        for (int i=0; i<n.getChildCount(); i++) walk(n.getChild(i), out, depth+1);
    }
    public boolean clickVisibleLabel(String label) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || label == null || label.trim().isEmpty()) return false;
        return clickWalk(root, label.toLowerCase().trim(), 0);
    }
    private boolean clickWalk(AccessibilityNodeInfo n, String target, int depth) {
        if (n == null || depth > 18) return false;
        CharSequence text = n.getText() != null ? n.getText() : n.getContentDescription();
        if (n.isVisibleToUser() && text != null && target.equals(text.toString().trim().toLowerCase())) {
            AccessibilityNodeInfo node = n;
            for (int k=0;k<4 && node!=null;k++,node=node.getParent()) {
                if (node.isClickable() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
            }
        }
        for (int i=0;i<n.getChildCount();i++) if (clickWalk(n.getChild(i), target, depth+1)) return true;
        return false;
    }
    public boolean typeIntoFocusedField(String value) {
        AccessibilityNodeInfo root = getRootInActiveWindow(); if (root == null) return false;
        AccessibilityNodeInfo input = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (input == null || !input.isEditable() || input.isPassword() || !input.isVisibleToUser()) return false;
        android.os.Bundle args = new android.os.Bundle(); args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        return input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }
}
