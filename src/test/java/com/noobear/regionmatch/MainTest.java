package com.noobear.regionmatch;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jason Han
 * @version 2019/12/7 15:35
 **/
public class MainTest {
    @Test
    public void speed() {
        RegionMatcher.init();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            RegionMatcher.byDetail("", "", "西城区");
        }
        System.out.println("ByDetail-最佳情况耗时-" + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            RegionMatcher.byDetail("", "西城镇", "");
        }
        System.out.println("ByDetail-最差情况耗时-" + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            RegionMatcher.byInput("西城区");
        }
        System.out.println("ByInput-耗时-" + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Test
    public void test1() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "西城");
        Assert.assertEquals(110102, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "华西");
        Assert.assertNull(matchResult.getRegion());
        System.out.println(matchResult);
    }

    @Test
    public void test3() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(230128, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test4() {
        RegionMatcher.init();
        RegionMatcher.setMatchUnavailable(true);
        RegionMatcher.setFitUnavailable(false);
        MatchResult matchResult = RegionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110223, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test5() {
        RegionMatcher.init();
        RegionMatcher.setMatchUnavailable(true);
        MatchResult matchResult = RegionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110000, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test6() {
        RegionMatcher.init();
        RegionMatcher.setCompareImportance(false);
        MatchResult matchResult = RegionMatcher.byDetail("", "", "朝阳", 125.29D, 43.84D);
        Assert.assertEquals(220104, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test7() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "朝阳");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test8() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "朝阳", "");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test9() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "朝阳县");
        Assert.assertEquals(211321, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test10() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "朝阳镇");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_1() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "西城");
        Assert.assertEquals(110102, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_2() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byDetail("", "", "华西");
        Assert.assertNull(matchResult.getRegion());
        System.out.println(matchResult);
    }

    @Test
    public void test2_3() {
        RegionMatcher.init();
        RegionMatcher.setMatchUnavailable(true);
        MatchResult matchResult = RegionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110000, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_4() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byInput("朝阳");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_5() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byInput("朝阳市");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_6() {
        RegionMatcher.init();
        MatchResult matchResult = RegionMatcher.byInput("朝阳区");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }
}
