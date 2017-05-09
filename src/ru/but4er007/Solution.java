package ru.but4er007;

import java.util.*;

class Solution {
    private static long startTime;

    private final int shopsCount;
    private final int roadsCount;
    private final int fishTypes;
    private final int shopFishTypes[];
    private final int roads[][];

    // cities, states, { BitMask, Way weight, Way shops }
    private int[][][] foundedStates;
    private boolean[] shopStateUpdatedFlags;
    private boolean[] shopStateUpdatedCachedFlags;
    private int minWayAlreadyFounded = -1;

    Solution(int shopsCount,
             int roadsCount,
             int fishTypes,
             int[] shopFishTypes,
             int[][] roads) {
        this.shopsCount = shopsCount;
        this.roads = roads;
        this.roadsCount = roadsCount;
        this.fishTypes = fishTypes;
        this.shopFishTypes = shopFishTypes;
        this.shopStateUpdatedFlags = new boolean[shopsCount];
        this.shopStateUpdatedCachedFlags = new boolean[shopsCount];
    }

    public static int main(String[] args) {
        final int shopsCount;
        final int roadsCount;
        final int fishTypes;
        final int shopFishTypes[];
        final int roads[][];

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
        return shorterWayForTwoCats;
    }

    // find shorter way for get all types of fish by single cat
    private void processWays() {
        // **** init algorithm
        foundedStates = new int[shopsCount][][];
        foundedStates[0] = new int[1][2];
        int firstShopBitMask = shopFishTypes[0];

        // init first shop state
        foundedStates[0][0][0] = firstShopBitMask;  // have all fish types from first shop
        foundedStates[0][0][1] = 0;                // way weight = 0
        shopStateUpdatedFlags[0] = true;
        // *****

        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < roadsCount; i++) {
                int road[] = roads[i];
                if ((shopStateUpdatedFlags[road[0]] || shopStateUpdatedCachedFlags[road[0]])
                        && foundedStates[road[0]] != null
                        && foundedStates[road[0]].length > 0) {
                    updated = updateRelatedRoad(road[0], road[1], road[2]) || updated;
                }
                if ((shopStateUpdatedFlags[road[1]] || shopStateUpdatedCachedFlags[road[1]])
                        && foundedStates[road[1]] != null
                        && foundedStates[road[1]].length > 0) {
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

        int[][] statesForLastShop = foundedStates[shopsCount - 1];
        for (int[] foundedState1 : statesForLastShop) {
            for (int[] foundedState2 : statesForLastShop) {
                if (checkMaskFullFilled(foundedState1[0] | foundedState2[0])
                        && (Math.max(foundedState1[1], foundedState2[1]) < shorterWayWeight || shorterWayWeight < 0)) {
                    shorterWayWeight = Math.max(foundedState1[1], foundedState2[1]);
                }
            }
        }
        return shorterWayWeight;
    }

    // update states for shop2 by merging states from shop1 if they better
    private boolean updateRelatedRoad(int shop1, int shop2, int weight) {
        int[][] states1 = foundedStates[shop1];
        List<int[]> states2 = new LinkedList<>();
        int[][] states2Temp = foundedStates[shop2];
        if (states2Temp != null) {
            states2.addAll(Arrays.asList(states2Temp));
        }
        boolean updated = false;

        for (int[] state1 : states1) {
            if(minWayAlreadyFounded > 0
                    && state1[1] > minWayAlreadyFounded) {
                continue;
            }

            boolean needToAddMergedState = true;
            HashSet<Integer> states2ToRemove = new HashSet<>();
            int statesToRemoveCount = 0;

            int[] newMergedState = mergeState(state1, shop2, weight);

            if(minWayAlreadyFounded > 0
                    && newMergedState[1] > minWayAlreadyFounded) {
                continue;
            }

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
                    } else if (comparing == -1) { // mask worth
                        if (newMergedState[1] >= states2.get(j)[1])
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

        int[][] updatedStates2 = new int[states2.size()][];
//        for (int i = 0; i < updatedStates2.length; i++) {
//            updatedStates2[i] = new int[states2.get(i).length];
//        }

        foundedStates[shop2] = states2.toArray(updatedStates2);
        shopStateUpdatedFlags[shop2] = updated || shopStateUpdatedFlags[shop2];
        return updated;
    }

    private int[] mergeState(int[] state1, int shop2, int roadWeight) {
        int[] newStateAfterMerge = new int [2];
        // compute new bit mask
        int bitMaskAfterMergeState = state1[0] | shopFishTypes[shop2];
        // compute new weight
        int wayWeightAfterMerge = state1[1] + roadWeight;

        newStateAfterMerge[0] = bitMaskAfterMergeState;
        newStateAfterMerge[1] = wayWeightAfterMerge;
        return newStateAfterMerge;
    }

    // fishTypes > type >=0
    static int setBitMaskType(int mask, int type) {
        return mask | (1 << type);
    }

    boolean checkMaskFullFilled(int mask) {
        for (int i = 0; i < fishTypes; i++) {
            if ((mask & (1 << i)) == 0) return false;
        }
        return true;
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
