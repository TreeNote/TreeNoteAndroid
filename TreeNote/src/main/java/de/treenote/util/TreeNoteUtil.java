package de.treenote.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.treenote.pojo.TreeNode;

public class TreeNoteUtil {
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    public static Date tomorrow() {
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DATE, 1);
        today.clear(Calendar.HOUR);
        today.clear(Calendar.MINUTE);
        today.clear(Calendar.SECOND);
        return today.getTime();
    }

    public static Date now() {
        Calendar today = Calendar.getInstance();
        return today.getTime();
    }

    public static String dateTimeToText(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMANY);
        return dateFormat.format(cal.getTime());
    }

    public static String dateToText(int year, int month, int day) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return dateFormat.format(calendar.getTime());
    }

    public static String dateToText(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return dateToText(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    public interface Checker {
        boolean check(TreeNode node);
    }

    public static class FutureDateChecker implements Checker {

        @Override
        public boolean check(TreeNode node) {
            return node.getStartDate() != null &&
                    node.getStartDate().getTime() > Calendar.getInstance().getTime().getTime();
        }
    }

    public static class TodayDateChecker implements Checker {

        @Override
        public boolean check(TreeNode node) {
            return node.getStartDate() != null && node.getStartDate() == Calendar.getInstance().getTime();
        }
    }

    public static List<TreeNode> filterTreeNodes(TreeNode node, Checker checker) {
        List<TreeNode> filteredList = new ArrayList<>();
        if (checker.check(node)) {
            filteredList.add(node);
        }
        for (TreeNode child : node.getChildren()) {
            filteredList.addAll(filterTreeNodes(child, checker));
        }
        return filteredList;
    }
}
