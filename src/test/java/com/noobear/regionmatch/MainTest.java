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
        RegionMatcher regionMatcher = new RegionMatcher();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            regionMatcher.byDetail("", "", "西城区");
        }
        System.out.println("ByDetail-最佳情况耗时-" + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            regionMatcher.byDetail("", "西城镇", "");
        }
        System.out.println("ByDetail-最差情况耗时-" + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            regionMatcher.byInput("西城区");
        }
        System.out.println("ByInput-耗时-" + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Test
    public void test1() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "西城");
        Assert.assertEquals(110102, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "华西");
        Assert.assertNull(matchResult.getRegion());
        System.out.println(matchResult);
    }

    @Test
    public void test3() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(230128, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test4() {
        RegionMatcher regionMatcher = new RegionMatcher();
        regionMatcher.setMatchUnavailable(true);
        regionMatcher.setFitUnavailable(false);
        MatchResult matchResult = regionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110223, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test5() {
        RegionMatcher regionMatcher = new RegionMatcher();
        regionMatcher.setMatchUnavailable(true);
        MatchResult matchResult = regionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110000, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test6() {
        RegionMatcher regionMatcher = new RegionMatcher();
        regionMatcher.setCompareImportance(false);
        MatchResult matchResult = regionMatcher.byDetail("", "", "朝阳", 125.29D, 43.84D);
        Assert.assertEquals(220104, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test7() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "朝阳");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test8() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "朝阳", "");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test9() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "朝阳县");
        Assert.assertEquals(211321, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test10() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "朝阳镇");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_1() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "西城");
        Assert.assertEquals(110102, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_2() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byDetail("", "", "华西");
        Assert.assertNull(matchResult.getRegion());
        System.out.println(matchResult);
    }

    @Test
    public void test2_3() {
        RegionMatcher regionMatcher = new RegionMatcher();
        regionMatcher.setMatchUnavailable(true);
        MatchResult matchResult = regionMatcher.byDetail("", "", "通县");
        Assert.assertEquals(110000, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_4() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byInput("朝阳");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_5() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byInput("朝阳市");
        Assert.assertEquals(211300, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }

    @Test
    public void test2_6() {
        RegionMatcher regionMatcher = new RegionMatcher();
        MatchResult matchResult = regionMatcher.byInput("朝阳区");
        Assert.assertEquals(110105, matchResult.getRegion().getCode());
        System.out.println(matchResult);
    }
}
