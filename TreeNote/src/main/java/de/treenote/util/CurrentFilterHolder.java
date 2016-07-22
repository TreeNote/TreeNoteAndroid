package de.treenote.util;

public class CurrentFilterHolder {

    private CurrentFilterHolder() {
    }

    public enum Filter {
        None, Today, Upcoming, Todos
    }

    public static Filter currentFilter = Filter.None;
}
