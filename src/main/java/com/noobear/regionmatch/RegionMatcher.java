package com.noobear.regionmatch;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Noobear
 * @version 2019/12/7 15:20
 **/
public class RegionMatcher {
    @Setter
    private static boolean compareImportance = true;
    @Setter
    private static boolean matchUnavailable = true;
    @Setter
    private static boolean fitUnavailable = true;
    @Setter
    private static boolean removeSuffix = true;
    @Setter
    private static boolean shuffle = true;

    private RegionMatcher() {
    }

    private static Map<Integer, Region> regionMap = new HashMap<>();
    private static Map<Character, Set<Integer>> provinceMap = new HashMap<>();
    private static Map<Character, Set<Integer>> cityMap = new HashMap<>();
    private static Map<Character, Set<Integer>> districtMap = new HashMap<>();
    //private static Map<Character, Set<Integer>> fullMap = new HashMap<>();

    private static RegionComparator defaultComparator = new RegionComparator(-1D, -1D);

    static {
        InputStream inputStream = RegionMatcher.class.getClassLoader()
                .getResourceAsStream("region.csv");
        assert inputStream != null;
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(
                        inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (isEmpty(line)) {
                    continue;
                }
                Region region = loadRegion(line);
                regionMap.put(region.getCode(), region);
                int code = region.getCode();
                buildIndex(code, region.getProvinceName(), provinceMap);
                buildIndex(code, region.getCityName(), cityMap);
                //buildIndex(code, region.getProvinceName(), fullMap);
                //buildIndex(code, region.getCityName(), fullMap);
                for (String str : region.getHistoryCityNames()) {
                    buildIndex(code, str, cityMap);
                    //buildIndex(code, str, fullMap);
                }
                buildIndex(code, region.getDistrictName(), districtMap);
                for (String str : region.getHistoryDistrictNames()) {
                    buildIndex(code, str, districtMap);
                    //buildIndex(code, str, fullMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        new RegionMatcher();
        compareImportance = true;
        matchUnavailable = true;
        fitUnavailable = true;
        removeSuffix = true;
        shuffle = true;
    }

    public static MatchResult byCode(int code) {
        Region region = regionMap.get(code);
        MatchResult matchResult = new MatchResult();
        if (region != null) {
            matchResult.setRegion(region);
            matchResult.setProbability(1.0D);
        }
        return matchResult;
    }

    public static MatchResult byDetail(String province,
                                       String city,
                                       String district) {
        return byDetail(province, city, district, -1D, -1D);
    }

    public static MatchResult byDetail(String province,
                                       String city,
                                       String district,
                                       final double longitude,
                                       final double latitude) {
        List<MatchResult> resultList = doMatch(province, city, district);
        if (resultList.isEmpty() && shuffle) {
            resultList = shuffleMatch(province, city, district);
        }
        if (resultList.isEmpty()) {
            return new MatchResult();
        }
        if (longitude > 0D && latitude > 0D) {
            Collections.sort(resultList, new RegionComparator(longitude, latitude));
        } else {
            Collections.sort(resultList, defaultComparator);
        }
        MatchResult matchResult = resultList.get(0);
        if (matchResult.getRegion() == null) {
            return new MatchResult();
        }
        if (fitUnavailable && !matchResult.getRegion().isAvailable()) {
            int code = matchResult.getRegion().getCode();
            int fitCode = code - code % 100;
            Region fit = regionMap.get(fitCode);
            if (fit == null || !fit.isAvailable()) {
                fitCode = code - code % 10000;
            }
            if (regionMap.containsKey(fitCode)) {
                matchResult.setRegion(regionMap.get(fitCode));
            } else {
                return new MatchResult();
            }
        }
        return matchResult;
    }

    @SuppressWarnings("ConstantConditions")
    private static List<MatchResult> shuffleMatch(String province,
                                                  String city,
                                                  String district) {
        boolean hasProvince = !isEmpty(province);
        boolean hasCity = !isEmpty(city);
        boolean hasDistrict = !isEmpty(district);
        // 用Set即可去重，因为重写了HashCode和Equals
        Set<MatchResult> resultSet = new HashSet<>();
        if (hasProvince && !hasCity && !hasDistrict) {
            // 省->市
            resultSet.addAll(doMatch("", province, ""));
            // 省->区
            resultSet.addAll(doMatch("", "", province));
        } else if (!hasProvince && hasCity && !hasDistrict) {
            // 市->区
            resultSet.addAll(doMatch("", "", city));
        } else if (!hasProvince && !hasCity && hasDistrict) {
            // 区->市
            resultSet.addAll(doMatch("", district, ""));
        } else if (hasProvince && hasCity && !hasDistrict) {
            // 省市->省区
            resultSet.addAll(doMatch(province, "", city));
            // 省市->市区
            resultSet.addAll(doMatch("", "", ""));
        } else if (hasProvince && !hasCity && hasDistrict) {
            // 省区->省市
            resultSet.addAll(doMatch(province, district, ""));
            // 省区->市区
            resultSet.addAll(doMatch("", province, district));
        } else if (!hasProvince && hasCity && hasDistrict) {
            // 市区->省区
            resultSet.addAll(doMatch(city, "", district));
            // 市区->省市
            resultSet.addAll(doMatch(city, district, ""));
        }
        List<MatchResult> resultList = new ArrayList<>(resultSet.size());
        for (MatchResult matchResult : resultSet) {
            // 顺序不对的，概率减半
            matchResult.setProbability(matchResult.getProbability() * 0.5);
            resultList.add(matchResult);
        }
        return resultList;
    }

    private static List<MatchResult> doMatch(String province,
                                             String city,
                                             String district) {
        List<MatchResult> resultList = processMatch(province, city, district);
        if (resultList.isEmpty() && removeSuffix) {
            resultList = processMatch(removeProvinceSuffix(province),
                    removeCitySuffix(city),
                    removeDistrictSuffix(district));
        }
        return resultList;
    }

    private static String removeProvinceSuffix(String province) {
        return province.replace("省", "")
                .replace("市", "")
                .replace("自治区", "")
                .replace("行政区", "")
                .replace("特别行政区", "")
                .replace("地区", "");
    }

    private static String removeCitySuffix(String city) {
        return city.replace("市", "")
                .replace("县", "")
                .replace("岛", "")
                .replace("盟", "")
                .replace("自治州", "")
                .replace("地区", "")
                .replace("地方", "");
    }

    private static String removeDistrictSuffix(String district) {
        return district.replace("区", "")
                .replace("乡", "")
                .replace("县", "")
                .replace("城", "")
                .replace("岛", "")
                .replace("市", "")
                .replace("旗", "")
                .replace("部", "")
                .replace("镇", "");
    }

    private static List<MatchResult> processMatch(String province,
                                                  String city,
                                                  String district) {
        Set<Integer> resultSet = new HashSet<>();
        Set<Integer> provinceSet = searchIndex(province, provinceMap);
        if (!provinceSet.isEmpty()) {
            resultSet.addAll(provinceSet);
        }
        Set<Integer> citySet = searchIndex(city, cityMap);
        if (resultSet.isEmpty()) {
            resultSet.addAll(citySet);
        } else if (!citySet.isEmpty()) {
            resultSet.retainAll(citySet);
        }
        Set<Integer> districtSet = searchIndex(district, districtMap);
        if (resultSet.isEmpty()) {
            resultSet.addAll(districtSet);
        } else if (!districtSet.isEmpty()) {
            resultSet.retainAll(districtSet);
        }
        List<MatchResult> matchResults = new ArrayList<>();
        for (int code : resultSet) {
            Region region = regionMap.get(code);
            if (region == null || (!matchUnavailable && !region.isAvailable())) {
                continue;
            }
            MatchResult matchResult = calcProbability(region, province, city, district);
            if (matchResult != null) {
                matchResults.add(matchResult);
            }
        }
        return matchResults;
    }

    private static Set<Integer> searchIndex(String str,
                                            Map<Character, Set<Integer>> map) {
        if (isEmpty(str)) {
            return Collections.emptySet();
        }
        Set<Integer> set = new HashSet<>();
        for (Character c : str.toCharArray()) {
            Set<Integer> matchedSet = map.get(c);
            if (matchedSet == null) {
                continue;
            }
            if (set.isEmpty()) {
                set.addAll(matchedSet);
            } else {
                set.retainAll(matchedSet);
            }
        }
        return set;
    }

    private static MatchResult calcProbability(Region region,
                                               String province,
                                               String city,
                                               String district) {
        double probability = 1.0D;
        boolean hasProvince = !isEmpty(province);
        boolean hasCity = !isEmpty(city);
        boolean hasDistrict = !isEmpty(district);
        // 维度1：少一项减10%的概率，一次线性关系，y=0.1x+0.7
        probability = hasProvince ? probability : probability - 0.1D;
        probability = hasCity ? probability : probability - 0.1D;
        probability = hasDistrict ? probability : probability - 0.1D;
        if (hasProvince) {
            // 维度2：编辑距离概率，一次线性关系，y=x
            double innerProbability = calcWordProbability(region.getProvinceName(), province);
            // 顺序不对的时候，概率就归0了，后续操作全部不会进行
            probability *= innerProbability;
        }
        if (hasCity && probability > 0D) {
            double innerProbability = calcWordProbability(region.getCityName(), city);
            int length = region.getHistoryCityNames().size() + 1;
            for (int i = 1; i < length; i++) {
                String historyCityName = region.getHistoryCityNames().get(i - 1);
                // 维度3：历史名称，二次线性关系，y=sqrt(1-x^2)
                double historyProbability = calcWordProbability(historyCityName, city)
                        * (Math.sqrt(1.0D - Math.pow((double) i / length, 2)));
                // 在最新的和历史中，选一个最大的概率作为这个code的概率
                innerProbability = Math.max(innerProbability, historyProbability);
            }
            probability *= innerProbability;
        }
        if (hasDistrict && probability > 0D) {
            double innerProbability = calcWordProbability(region.getDistrictName(), district);
            int length = region.getHistoryDistrictNames().size() + 1;
            for (int i = 1; i < length; i++) {
                String historyDistrictName = region.getHistoryDistrictNames().get(i - 1);
                double historyProbability = calcWordProbability(historyDistrictName, district)
                        * (Math.sqrt(1.0D - Math.pow((double) i / length, 2)));
                innerProbability = Math.max(innerProbability, historyProbability);
            }
            probability *= innerProbability;
        }
        if (probability == 0) {
            return null;
        }
        MatchResult matchResult = new MatchResult();
        matchResult.setRegion(region);
        matchResult.setProbability(probability);
        return matchResult;
    }

    /**
     * 检查输入的子串是不是按顺序的
     *
     * @param full  完整句子
     * @param input 子串
     * @return 是否按顺序
     */
    private static boolean checkOrder(String full, String input) {
        char[] fullChars = full.toCharArray();
        char[] inputChars = input.toCharArray();
        int position = 0;
        for (char c : fullChars) {
            if (c == inputChars[position]) {
                position++;
            }
            if (position == input.length()) {
                return true;
            }
        }
        return false;
    }

    private static double calcWordProbability(String full, String input) {
        // 用编辑距离求概率，编辑距离相对最大编辑距离（最长的句子长度）越小，概率越高
        if (checkOrder(full, input)) {
            return 1 - (editDistance(full, input)
                    / (double) Math.max(full.length(), input.length()));
        } else {
            return 0.0D;
        }
    }

    /**
     * 这是LeetCode给的官方标准编辑距离解法
     *
     * @param word1 词1
     * @param word2 词1
     * @return 编辑距离
     */
    private static int editDistance(String word1, String word2) {
        int n = word1.length();
        int m = word2.length();
        if (n * m == 0) {
            return n + m;
        }
        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }
        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int leftDown = d[i - 1][j - 1];
                if (word1.charAt(i - 1) != word2.charAt(j - 1)) {
                    leftDown += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, leftDown));
            }
        }
        return d[n][m];
    }

    private static Region loadRegion(String line) {
        String[] split = line.split(",");
        Region region = new Region();
        region.setCode(Integer.parseInt(split[0]));
        region.setProvinceName(split[1]);
        region.setCityName(split[2]);
        region.setDistrictName(split[4]);
        region.setLongitude(Double.parseDouble(split[6]));
        region.setLatitude(Double.parseDouble(split[7]));
        region.setAvailable(Boolean.parseBoolean(split[8]));
        region.setAvailableUntil(split[9]);
        if (!isEmpty(split[3])) {
            String[] historyCityNames = split[3].split("\\|");
            region.getHistoryCityNames().addAll(Arrays.asList(historyCityNames));
        }
        if (!isEmpty(split[5])) {
            String[] historyDistrictNames = split[5].split("\\|");
            region.getHistoryDistrictNames().addAll(Arrays.asList(historyDistrictNames));
        }
        return region;
    }

    private static void buildIndex(int regionCode,
                                   String str,
                                   Map<Character, Set<Integer>> map) {
        if (isEmpty(str)) {
            return;
        }
        for (Character c : str.toCharArray()) {
            if (map.containsKey(c)) {
                map.get(c).add(regionCode);
            } else {
                Set<Integer> set = new HashSet<>();
                set.add(regionCode);
                map.put(c, set);
            }
        }
    }

    @AllArgsConstructor
    private static class RegionComparator implements Comparator<MatchResult> {
        private double longitude;
        private double latitude;

        @Override
        public int compare(MatchResult o1, MatchResult o2) {
            // 第1步：判断概率
            // 第1步：判断该区域代码的可用时间，越近的越有效
            // 第3步：省内重要性（后四位越小越好）
            // 第4步：距自己的距离
            int result = compareProbability(o1, o2);
            if (result == 0) {
                result = compareAvailableUntil(o1, o2);
            }
            if (result == 0 && compareImportance) {
                result = compareImportance(o1, o2);
            }
            if (result == 0 && longitude > 0D && latitude > 0D) {
                result = compareDistance(o1, o2, longitude, latitude);
            }
            return result;
        }

        private static int compareProbability(MatchResult o1, MatchResult o2) {
            if (o2.getProbability() == o1.getProbability()) {
                return 0;
            } else {
                return o2.getProbability() > o1.getProbability() ? 1 : -1;
            }
        }

        private static int compareAvailableUntil(MatchResult o1, MatchResult o2) {
            if (o1.getRegion().getAvailableUntil().equals(o2.getRegion().getAvailableUntil())) {
                return 0;
            } else {
                String[] split1 = o1.getRegion().getAvailableUntil().split("\\.");
                String[] split2 = o2.getRegion().getAvailableUntil().split("\\.");
                int year1 = Integer.parseInt(split1[0]);
                int year2 = Integer.parseInt(split2[0]);
                int month1 = Integer.parseInt(split1[1]);
                int month2 = Integer.parseInt(split2[1]);
                // 时间降序
                if (year1 != year2) {
                    return year2 > year1 ? 1 : -1;
                } else if (month1 != month2) {
                    return month2 > month1 ? 1 : -1;
                } else {
                    return 0;
                }
            }
        }

        private static int compareImportance(MatchResult o1, MatchResult o2) {
            int code1 = o1.getRegion().getCode() % 10000;
            int code2 = o2.getRegion().getCode() % 10000;
            if (code1 == code2) {
                return 0;
            }
            // 优先直辖市和一些特殊城市
            int cityCode1 = o1.getRegion().getCode() - o1.getRegion().getCode() % 100;
            int cityCode2 = o2.getRegion().getCode() - o2.getRegion().getCode() % 100;
            if (isSpecialCity(cityCode1) && !isSpecialCity(cityCode2)) {
                return -1;
            } else if (!isSpecialCity(cityCode1) && isSpecialCity(cityCode2)) {
                return 1;
            }
            // 编号升序
            return code1 > code2 ? 1 : -1;
        }

        private static boolean isSpecialCity(int cityCode) {
            // 北京、天津、上海、重庆、广州、深圳优先
            return cityCode == 110000 || cityCode == 110100
                    || cityCode == 120000 || cityCode == 120100
                    || cityCode == 310000 || cityCode == 310100
                    || cityCode == 500000 || cityCode == 500100
                    || cityCode == 440100 || cityCode == 440300;
        }

        private static int compareDistance(MatchResult o1,
                                           MatchResult o2,
                                           double longitude,
                                           double latitude) {
            double distance1 = Math.pow(longitude - o1.getRegion().getLongitude(), 2)
                    + Math.pow(latitude - o1.getRegion().getLatitude(), 2);
            double distance2 = Math.pow(longitude - o2.getRegion().getLongitude(), 2)
                    + Math.pow(latitude - o2.getRegion().getLatitude(), 2);
            if (distance1 == distance2) {
                return 0;
            }
            // 距离升序
            return distance1 > distance2 ? 1 : -1;
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }
}