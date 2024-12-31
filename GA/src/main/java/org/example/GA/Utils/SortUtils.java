package org.example.GA.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortUtils {





    // Helper class to store an element with its original index
    static class IndexedElement<T> {
        T element;
        int originalIndex;

        IndexedElement(T element, int originalIndex) {
            this.element = element;
            this.originalIndex = originalIndex;
        }
    }




    // Pair class to hold the sorted lists
    public static class Pair<T, U> {
        public final T first;
        public final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }




    public static <T extends Comparable<T>, U> Pair<List<T>, List<U>> sortWithShifts(List<T> listToSort, List<U> correspondingList) {
        // Create a list of IndexedElements for the list to sort
        List<IndexedElement<T>> indexedList = new ArrayList<>();
        for (int i = 0; i < listToSort.size(); i++) {
            indexedList.add(new IndexedElement<>(listToSort.get(i), i));
        }

        // Sort indexedList based on the elements
        indexedList.sort(Comparator.comparing(indexed -> indexed.element));

        // Create copies of the input lists to hold sorted values
        List<T> sortedListToSort = new ArrayList<>(listToSort);
        List<U> sortedCorrespondingList = new ArrayList<>(correspondingList);

        // Apply the sorted order to sortedListToSort and sortedCorrespondingList
        for (int i = 0; i < indexedList.size(); i++) {
            // Update sortedListToSort with sorted elements
            sortedListToSort.set(i, indexedList.get(i).element);

            // Update sortedCorrespondingList by mapping the original indices in sorted order
            sortedCorrespondingList.set(i, correspondingList.get(indexedList.get(i).originalIndex));
        }

        // Return both sorted lists as a Pair
        return new Pair<>(sortedListToSort, sortedCorrespondingList);
    }




}
