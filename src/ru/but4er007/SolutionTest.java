package ru.but4er007;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolutionTest {

    // example https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Cycle_graph_C5.png/240px-Cycle_graph_C5.png
    @Test
    public void testSimpleCircle() {
        String[] args = {"5 5 5 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "1 4 " +
                "1 5 " +
                "1 2 10 " +
                "1 3 10 " +
                "2 4 10 " +
                "3 5 10 " +
                "4 5 10" };

        assertEquals(30, Solution.main(args));
    }

    // example http://www.cs.ecu.edu/karl/3300/spr15/assignments/Assignment5/graph2.gif
    @Test
    public void testSimpleCircleWithEndVertex() {
        String[] args = {"6 6 6 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "1 4 " +
                "1 5 " +
                "1 6 " +
                "1 2 1 " +
                "1 3 1 " +
                "2 4 10 " +
                "2 5 1 " +
                "3 6 1 " +
                "4 6 10" };

        assertEquals(21, Solution.main(args));
    }

    @Test
    public void testNoBranches() {
        String[] args = {"6 5 6 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "1 4 " +
                "1 5 " +
                "1 6 " +
                "1 2 1 " +
                "2 3 1 " +
                "3 4 10 " +
                "4 5 1 " +
                "5 6 1 " };

        assertEquals(14, Solution.main(args));
    }

    @Test
    public void testMoreTypesInShop() {
        String[] args = {"6 6 6 " +
                "1 1 " +
                "1 2 " +
                "2 3 4 " +
                "1 4 " +
                "1 5 " +
                "1 6 " +
                "1 2 1 " +
                "1 3 1 " +
                "2 4 10 " +
                "2 5 1 " +
                "3 6 1 " +
                "4 6 10" };

        assertEquals(6, Solution.main(args));
    }

    @Test
    public void testSunLikeGraph() {
        String[] args = {"7 6 10 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "2 4 8 " +
                "2 5 9 " +
                "2 6 10 " +
                "1 7 " +
                "1 2 1 " +
                "1 3 1 " +
                "1 4 1 " +
                "1 5 1 " +
                "1 6 1 " +
                "1 7 1 " };

        assertEquals(7, Solution.main(args));
    }

    @Test
    public void testSunLikeGraph2() {
        String[] args = {"7 6 10 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "2 4 8 " +
                "2 5 9 " +
                "2 6 10 " +
                "1 7 " +
                "1 2 1 " +
                "1 3 1000 " +
                "1 4 1 " +
                "1 5 1 " +
                "1 6 9 " +
                "1 7 1 " };

        assertEquals(2001, Solution.main(args));
    }

    @Test
    public void testVertexWithoutTypes() {
        String[] args = {"6 6 6 " +
                "1 1 " +
                "0 " +
                "1 3 " +
                "1 4 " +
                "2 5 2 " +
                "1 6 " +
                "1 2 1 " +
                "1 3 1 " +
                "2 4 10 " +
                "2 5 1 " +
                "3 6 1 " +
                "4 6 10" };

        assertEquals(21, Solution.main(args));
    }

    @Test
    public void testVertexWithoutInHardWay() {
        String[] args = {"6 6 6 " +
                "1 1 " +
                "1 2 " +
                "1 3 " +
                "0 " +
                "2 5 4 " +
                "1 6 " +
                "1 2 1 " +
                "1 3 1 " +
                "2 4 10 " +
                "2 5 1 " +
                "3 6 1 " +
                "4 6 10" };

        assertEquals(6, Solution.main(args));
    }

    @Test
    public void longCircleWithBigRoadCost() {
        StringBuilder builder = new StringBuilder();
        builder.append("1000 1000 10 ");

        // fill types
        int currentType = 1;
        for (int i = 1; i <= 1000; i++) {
            builder.append(" 1 ")
                    .append(currentType);
            currentType = (currentType) % 10 + 1;
        }

        // fill roads
        for (int i = 1; i < 1000; i++) {
            builder.append(" ")
                    .append(i)
                    .append(" ")
                    .append(i + 1)
                    .append(" ")
                    .append(1000);
        }
        builder.append(" 1 1000 1000 ");

        assertEquals(9000, Solution.main(new String[]{builder.toString()}));
    }

    @Test
    public void longCircleWithBigRoadCostMoreRoads() {
        StringBuilder builder = new StringBuilder();
        builder.append("1000 2000 10 ");

        // fill types
        int currentType = 1;
        for (int i = 1; i <= 1000; i++) {
            builder.append(" 1 ")
                    .append(currentType);
            currentType = (currentType) % 10 + 1;
        }

        // fill roads
        for (int i = 1; i < 999; i++) {
            builder.append(" ")
                    .append(i)
                    .append(" ")
                    .append(i + 1)
                    .append(" ")
                    .append(1000);
            builder.append(" ")
                    .append(i)
                    .append(" ")
                    .append(i + 2)
                    .append(" ")
                    .append(1);
        }
        builder.append(" 999 1000 1000 ");
        builder.append(" 999 1 1000 ");
        builder.append(" 1000 2 1000 ");
        builder.append(" 1 1000 1000 ");

        assertEquals(1008, Solution.main(new String[]{builder.toString()}));
    }

    // region test bit mask operations
    @Test
    public void testBitMaskPutType() {
        Solution solution = new Solution(0, 0, 0, null, null);
        long mask = 0;
        mask = solution.setBitMaskType(mask, 0);
        assertEquals(1, mask);

        mask = 0;
        mask = solution.setBitMaskType(mask, 4);
        assertEquals(16, mask);

        mask = 0;
        mask = solution.setBitMaskType(mask, 1);
        assertEquals(2, mask);

        mask = 0;
        mask = solution.setBitMaskType(mask, 10);
        assertEquals(1024, mask);

        mask = 0;
        mask = solution.setBitMaskType(mask, 1);
        mask = solution.setBitMaskType(mask, 4);
        mask = solution.setBitMaskType(mask, 5);
        assertEquals(50, mask);
    }

    @Test
    public void testBitMaskLastBeing() {
        Solution solution = new Solution(10, 0, 10, null, null);
        long mask = 0;
        mask = solution.setBitMaskLast(mask);
        assertEquals(1024, mask);

        assertEquals(true, solution.getBeingLast(mask));
    }

    @Test
    public void testBitMaskCompare() {
        Solution solution = new Solution(0, 0, 0, null, null);
        assertEquals(0, solution.compareBitMasks(0b110011, 0b001100));
        assertEquals(-1, solution.compareBitMasks(0b110011, 0b110011));
        assertEquals(1, solution.compareBitMasks(0b110011, 0b110001));
        assertEquals(-1, solution.compareBitMasks(0b110011, 0b110111));
    }

    @Test
    public void testCheckBitMaskFilled() {
        Solution solution = new Solution(5, 0, 5, null, null);
        assertEquals(false, solution.checkMaskFullFilled(0b00000));
        assertEquals(false, solution.checkMaskFullFilled(0b11111));
        assertEquals(true, solution.checkMaskFullFilled(0b111111));
        assertEquals(false, solution.checkMaskFullFilled(0b101111));
        assertEquals(false, solution.checkMaskFullFilled(0b111110));
    }
    // endregion

}