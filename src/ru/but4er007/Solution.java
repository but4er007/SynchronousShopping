package ru.but4er007;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

class Solution {
    private final int shopsCount;
    private final int roadsCount;
    private final int shopFishTypes[];
    private final int roads[][];

    // cities, states, { BitMask, Way weight, Way shops }
    private LinkedList<int[]>[] foundedStates;
    private boolean[] shopStateUpdatedFlags;
    private boolean[] shopStateUpdatedCachedFlags;
    private int minWayAlreadyFounded = -1;

    private final long fullTypeMask;

    Solution(int shopsCount,
             int roadsCount,
             int fishTypes,
             int[] shopFishTypes,
             int[][] roads) {
        this.shopsCount = shopsCount;
        this.roads = roads;
        this.roadsCount = roadsCount;
        this.shopFishTypes = shopFishTypes;
        this.shopStateUpdatedFlags = new boolean[shopsCount];
        this.shopStateUpdatedCachedFlags = new boolean[shopsCount];
        long fullTypeMask = 0;
        for (int i = 0; i < fishTypes; i++) {
            fullTypeMask = fullTypeMask | (1 << i);
        }
        this.fullTypeMask = fullTypeMask;
    }

    public static int main(String[] args) {
        final int shopsCount;
        final int roadsCount;
        final int fishTypes;
        final int shopFishTypes[];
        final int roads[][];

        long startTime = System.currentTimeMillis();

        Scanner in;
        if (args != null && args.length > 0)
            in = new Scanner(args[0]);
        else
            in = new Scanner(System.in);
        shopsCount = in.nextInt();
        roadsCount = in.nextInt();
        fishTypes = in.nextInt();
        shopFishTypes = new int[shopsCount];
        roads = new int[roadsCount][3];
        // read shop fish types
        for (int i = 0; i < shopsCount; i++) {
            int shopTypesCount = in.nextInt();
            int typeMask = 0;
            for (int j = 0; j < shopTypesCount; j++) {
                typeMask = setBitMaskType(typeMask, in.nextInt() - 1);
            }
            shopFishTypes[i] = typeMask;
        }
        // read roads
        for (int i = 0; i < roadsCount; i++) {
            roads[i][0] = in.nextInt() - 1; // 1st shop
            roads[i][1] = in.nextInt() - 1; // 2nd shop
            roads[i][2] = in.nextInt(); // road length
        }

        Solution solution = new Solution(shopsCount, roadsCount, fishTypes, shopFishTypes, roads);
        solution.processWays();

        int shorterWayForTwoCats = solution.findFastestWayFoTwoCats();
        System.out.println(shorterWayForTwoCats);
        System.out.println("Working time: " + (System.currentTimeMillis() - startTime));
        return shorterWayForTwoCats;
    }

    // find shorter way for get all types of fish by single cat
    private void processWays() {
        // **** init algorithm
        foundedStates = (LinkedList<int[]>[]) Array.newInstance(LinkedList.class, shopsCount);
        for (int i = 0; i < shopsCount; i++) {
            foundedStates[i] = new LinkedList<>();
        }
        int firstShopBitMask = shopFishTypes[0];

        // init first shop state
        int[] state = new int[2];
        state[0] = firstShopBitMask;  // have all fish types from first shop
        state[1] = 0;                // way weight = 0
        foundedStates[0].add(state);
        shopStateUpdatedFlags[0] = true;
        // *****

        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < roadsCount; i++) {
                int road[] = roads[i];
                if ((shopStateUpdatedCachedFlags[road[0]] || shopStateUpdatedFlags[road[0]])
                        && !foundedStates[road[0]].isEmpty()) {
                    updated = updateRelatedRoad(road[0], road[1], road[2]) || updated;
                }
                if ((shopStateUpdatedCachedFlags[road[1]] || shopStateUpdatedFlags[road[1]])
                        && !foundedStates[road[1]].isEmpty()) {
                    updated = updateRelatedRoad(road[1], road[0], road[2]) || updated;
                }
            }
            if (shopStateUpdatedFlags[shopsCount - 1]) {
                minWayAlreadyFounded = findFastestWayFoTwoCats();
            }
            shopStateUpdatedCachedFlags = shopStateUpdatedFlags;
            shopStateUpdatedFlags = new boolean[shopsCount];
        } while (updated);
    }

    private int findFastestWayFoTwoCats() {
        int shorterWayWeight = -1;

        LinkedList<int[]> statesForLastShop = foundedStates[shopsCount - 1];
        for (int[] foundedState1 : statesForLastShop) {
            for (int[] foundedState2 : statesForLastShop) {
                int maxWay;
                if (checkMaskFullFilled(foundedState1[0] | foundedState2[0])
                        && (shorterWayWeight > (maxWay = Math.max(foundedState1[1], foundedState2[1]))
                        || shorterWayWeight < 0)) {
                    shorterWayWeight = maxWay;
                }
            }
        }
        return shorterWayWeight;
    }

    // update states for shop2 by merging states from shop1 if they better
    private boolean updateRelatedRoad(int shop1, int shop2, int weight) {
        LinkedList<int[]> states1 = foundedStates[shop1];
        LinkedList<int[]> states2 = foundedStates[shop2];
        boolean updated = false;

        for (int[] state1 : states1) {
            if (minWayAlreadyFounded > 0
                    && state1[1] >= minWayAlreadyFounded) {
                continue;
            }

            int[] newMergedState = mergeState(state1, shop2, weight);

            if (minWayAlreadyFounded > 0
                    && newMergedState[1] >= minWayAlreadyFounded) {
                continue;
            }
            boolean needToAddMergedState = true;

            // find more optimized state
            Iterator iter = states2.iterator();
            while (iter.hasNext()) {
                int[] state2 = (int[]) iter.next();
                if (minWayAlreadyFounded > 0
                        && state2[1] > minWayAlreadyFounded) {
                    iter.remove();
                    continue;
                }

                if (newMergedState[0] == state2[0]) { // bit masks equals
                    if (newMergedState[1] < state2[1]) {
                        iter.remove();
                    } else {
                        needToAddMergedState = false;
                    }
                } else {
                    int comparing = compareBitMasks(newMergedState[0], state2[0]);
                    if (comparing == 1) { // new mask better
                        if (newMergedState[1] <= state2[1]) { // new way better
                            iter.remove();
                        }
                    } else if (comparing == -1) { // mask worth
                        if (newMergedState[1] >= state2[1])
                            needToAddMergedState = false;
                    }
                }
            }
            // add new state
            if (needToAddMergedState) {
                states2.add(newMergedState);
                updated = true;
            }
        }

        shopStateUpdatedFlags[shop2] = updated || shopStateUpdatedFlags[shop2];
        return updated;
    }

    private int[] mergeState(int[] state1, int shop2, int roadWeight) {
        int[] newStateAfterMerge = new int[2];
        // compute new bit mask
        newStateAfterMerge[0] = state1[0] | shopFishTypes[shop2];
        // compute new weight
        newStateAfterMerge[1] = state1[1] + roadWeight;

        return newStateAfterMerge;
    }

    // fishTypes > type >=0
    static int setBitMaskType(int mask, int type) {
        return mask | (1 << type);
    }

    boolean checkMaskFullFilled(int mask) {
        return mask == fullTypeMask;
    }

    // return 1 if mask1 better
    // return -1 if mask1 worth or equal
    // return 0 if masks different
    int compareBitMasks(int mask1, int mask2) {
        if ((mask1 | mask2) == mask2) return -1;
        if ((mask1 | mask2) == mask1) return 1;
        return 0;
    }
}
