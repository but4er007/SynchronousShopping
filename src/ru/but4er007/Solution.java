package ru.but4er007;

import java.util.*;

class Solution {
    private final int shopsCount;
    private final int roadsCount;
    private final int fishTypes;
    private final int shopFishTypes[][];
    private final int roads[][];

    // cities, states, { BitMask, Way weight, Way shops }
    private long[][][] foundedStates;

    Solution(int shopsCount,
             int roadsCount,
             int fishTypes,
             int[][] shopFishTypes,
             int[][] roads) {
        this.shopsCount = shopsCount;
        this.roads = roads;
        this.roadsCount = roadsCount;
        this.fishTypes = fishTypes;
        this.shopFishTypes = shopFishTypes;
    }

    static long main(String[] args) {
        final int shopsCount;
        final int roadsCount;
        final int fishTypes;
        final int shopFishTypes[][];
        final int roads[][];

        Scanner in;
        if (args != null && args.length > 0)
            in = new Scanner(args[0]);
        else
            in = new Scanner(System.in);
        shopsCount = in.nextInt();
        roadsCount = in.nextInt();
        fishTypes = in.nextInt();
        shopFishTypes = new int[shopsCount][];
        roads = new int[roadsCount][3];
        // read shop fish types
        for (int i = 0; i < shopsCount; i++) {
            int shopTypesCount = in.nextInt();
            shopFishTypes[i] = new int[shopTypesCount];
            for (int j = 0; j < shopTypesCount; j++) {
                shopFishTypes[i][j] = in.nextInt() - 1;
            }
        }
        // read roads
        for (int i = 0; i < roadsCount; i++) {
            roads[i][0] = in.nextInt() - 1; // 1st city
            roads[i][1] = in.nextInt() - 1; // 2nd city
            roads[i][2] = in.nextInt(); // road length
        }

        Solution solution = new Solution(shopsCount, roadsCount, fishTypes, shopFishTypes, roads);
        solution.processWays();

        long[][] shorterWayForTwoCats = solution.findFastestWayFoTwoCats();
        if (shorterWayForTwoCats != null) {
            String delimiter = " -> ";
            for (long[] wayState :
                    shorterWayForTwoCats) {
                String separator = "";
                System.out.println();
                System.out.print("Way: ");
                for (int i = 2; i < wayState.length; i++) {
                    long shopNum = wayState[i];
                    System.out.print(separator + (shopNum + 1));
                    separator = delimiter;
                }
            }
            System.out.println();
            System.out.println("Shorter way weight: "
                    + Math.max(shorterWayForTwoCats[0][1], shorterWayForTwoCats[1][1]));

            return Math.max(shorterWayForTwoCats[0][1], shorterWayForTwoCats[1][1]);
        } else {
            System.out.println("error");
        }

        return -1;
    }

    // find shorter way for get all types of fish by single cat
    private void processWays() {
        // **** init algorithm
        foundedStates = new long[shopsCount][][];
        foundedStates[0] = new long[1][3];
        long firstShopBitMask = 0;
        for (int i = 0; i < shopFishTypes[0].length; i++) {
            firstShopBitMask = setBitMaskType(firstShopBitMask, shopFishTypes[0][i]);
        }
        // init first shop state
        foundedStates[0][0][0] = firstShopBitMask;  // have all fish types from first shop
        foundedStates[0][0][1] = 0L;                // way weight = 0
        foundedStates[0][0][2] = 0L;                // way contain only first shop
        // *****

        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < roadsCount; i++) {
                int road[] = roads[i];
                if (foundedStates[road[0]] != null && foundedStates[road[0]].length > 0) {
                    updated = updateRelatedRoad(road[0], road[1], road[2]) || updated;
                }
                if (foundedStates[road[1]] != null && foundedStates[road[1]].length > 0) {
                    updated = updateRelatedRoad(road[1], road[0], road[2]) || updated;
                }
            }
        } while (updated);
    }

    private long[][] findFastestWayFoTwoCats() {
        long shorterWayWeight = -1;
        long[][] shorterWayStates = new long[2][];

        long[][] statesForLastShop = foundedStates[shopsCount - 1];
        for (long[] foundedState1 : statesForLastShop) {
            for (long[] foundedState2 : statesForLastShop) {
                if (checkMaskFullFilled(foundedState1[0] | foundedState2[0])
                        && (Math.max(foundedState1[1], foundedState2[1]) < shorterWayWeight || shorterWayWeight < 0)) {
                    shorterWayWeight = Math.max(foundedState1[1], foundedState2[1]);
                    shorterWayStates[0] = foundedState1;
                    shorterWayStates[1] = foundedState2;
                }
            }
        }
        return shorterWayStates;
    }

    // update states for shop2 by merging states from shop1 if they better
    private boolean updateRelatedRoad(int shop1, int shop2, int weight) {
        long[][] states1 = foundedStates[shop1];
        List<long[]> states2 = new LinkedList<>();
        long[][] states2Temp = foundedStates[shop2];
        if (states2Temp != null) {
            states2.addAll(Arrays.asList(states2Temp));
        }
        boolean updated = false;

        for (long[] state1 : states1) {

            boolean needToAddMergedState = true;
            HashSet<Integer> states2ToRemove = new HashSet<>();
            int statesToRemoveCount = 0;

            long[] newMergedState = mergeState(state1, shop2, weight);

            // find more optimized state
            for (int j = 0; j < states2.size(); j++) {

                if (newMergedState[0] == states2.get(j)[0]) { // bit masks equals
                    if (newMergedState[1] < states2.get(j)[1]) {
                        states2ToRemove.add(j);
                        statesToRemoveCount++;
                    } else {
                        needToAddMergedState = false;
                    }
                } else {
                    int comparing = compareBitMasks(newMergedState[0], states2.get(j)[0]);
                    if (comparing == 1) { // new mask better
                        if (newMergedState[1] <= states2.get(j)[1]) { // new way better
                            states2ToRemove.add(j);
                            statesToRemoveCount++;
                        }
                    } else if (comparing == -1) { // masks different
                        needToAddMergedState = false;
                    }
                }
            }

            // remove old worth states
            if (statesToRemoveCount > 0) {
                // remove states from states2
                Iterator iter = states2.iterator();
                int itemNum = 0;
                while (iter.hasNext()) {
                    iter.next();
                    if (states2ToRemove.contains(itemNum))
                        iter.remove();
                    itemNum++;
                }
                updated = true;
            }
            // add new states
            if (needToAddMergedState) {
                states2.add(newMergedState);
                updated = true;
            }
        }

        long[][] updatedStates2 = new long[states2.size()][];
//        for (int i = 0; i < updatedStates2.length; i++) {
//            updatedStates2[i] = new long[states2.get(i).length];
//        }

        foundedStates[shop2] = states2.toArray(updatedStates2);
        return updated;
    }

    private long[] mergeState(long[] state1, int city2, int roadWeight) {
        long[] newStateAfterMerge = Arrays.copyOf(state1, state1.length + 1);
        // compute new bit mask
        long bitMaskAfterMergeState = state1[0];
        for (int fishType : shopFishTypes[city2]) {
            bitMaskAfterMergeState = setBitMaskType(bitMaskAfterMergeState, fishType);
        }
        if (city2 == shopsCount - 1) {
            // set flag being at last shop
            bitMaskAfterMergeState = setBitMaskLast(bitMaskAfterMergeState);
        }
        // compute new weight
        long wayWeightAfterMerge = state1[1] + roadWeight;

        newStateAfterMerge[0] = bitMaskAfterMergeState;
        newStateAfterMerge[1] = wayWeightAfterMerge;
        newStateAfterMerge[newStateAfterMerge.length - 1] = city2;
        return newStateAfterMerge;
    }

    // fishTypes > type >=0
    long setBitMaskType(long mask, int type) {
        return mask | (1 << type);
    }

    long setBitMaskLast(long mask) {
        return mask | (1 << fishTypes);
    }

    boolean getBeingLast(long mask) {
        return (mask & (1 << fishTypes)) != 0;
    }

    boolean checkMaskFullFilled(long mask) {
        for (int i = 0; i <= fishTypes; i++) {
            if ((mask & (1 << i)) == 0) return false;
        }
        return true;
    }

    // return 1 if mask1 better
    // return -1 if mask1 worth or equal
    // return 0 if masks different
    int compareBitMasks(long mask1, long mask2) {
        if ((mask1 | mask2) == mask2) return -1;
        if ((mask1 | mask2) == mask1) return 1;
        return 0;
    }
}
