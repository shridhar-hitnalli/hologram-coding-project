package com.hologramsciences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Algorithms {
    /**
     *
     *  Compute the cartesian product of a list of lists of any type T
     *  the result is a list of lists of type T, where each element comes
     *  each successive element of the each list.
     *
     *  https://en.wikipedia.org/wiki/Cartesian_product
     *
     *  For this problem order matters.
     *
     *  Example:
     *
     *   listOfLists = Arrays.asList(
     *                         Arrays.asList("A", "B"),
     *                         Arrays.asList("K", "L")
     *                 )
     *
     *   returns:
     *
     *   Arrays.asList(
     *         Arrays.asList("A", "K"),
     *         Arrays.asList("A", "L"),
     *         Arrays.asList("B", "K"),
     *         Arrays.asList("B", "L")
     *   )
     *
     *
     *
     */
    public static final <T> List<List<T>> cartesianProductForLists(final List<List<T>> listOfLists) {
        if (listOfLists.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<T>> result = Arrays.asList(Collections.emptyList());
        for (List<T> list : listOfLists) {
            result = computeCartesian(result, list);
        }

        return result;
    }

    private static <T> List<List<T>> computeCartesian(List<List<T>> list1, List<T> list2) {
        return list1.stream()
                .flatMap(list -> list2.stream().map(obj -> {
                    List<T> newResults = new ArrayList<>(list);
                    newResults.add(obj);
                    return newResults;
                })).collect(Collectors.toList());
    }
    /**
     *
     *  In the United States there are six coins:
     *  1¢ 5¢ 10¢ 25¢ 50¢ 100¢
     *  Assuming you have an unlimited supply of each coin,
     *  implement a method which returns the number of distinct ways to make totalCents
     */
    public static final long countNumWaysMakeChange(final int totalCents) {
        if (totalCents < 0) {
            return -1;
        }
        int[] coins = new int[] {1, 5, 10, 25, 50, 100};
        long[] combinations = new long[totalCents + 1];
        combinations[0] = 1;  // it means than we are going to include one coin when the coin denomination and the total cents value  is same
        for (int coin : coins) {
            for (int i = 1; i <= totalCents; i++) {
                if (i >= coin) {
                    combinations[i] += combinations[i - coin];
                }
            }
        }
        return combinations[totalCents];
    }
}
