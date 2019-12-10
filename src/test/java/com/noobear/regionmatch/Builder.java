package com.noobear.regionmatch;

import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Jason Han
 * @version 2019/12/7 11:10
 **/
public class Builder {
    private static final Pattern numberPattern = Pattern.compile("^[\\s]*[0-9.]+[\\s]*$");
    private static final DataFormatter dataFormatter = new DataFormatter();

    @Test
    public void build() throws Exception {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook("./region.xlsx");
        Iterator<Sheet> sheetIterator = xssfWorkbook.sheetIterator();
        Sheet sheet;
        LinkedHashMap<String, LinkedHashMap<Integer, String>> codeMap = new LinkedHashMap<>();
        while (sheetIterator.hasNext()) {
            sheet = sheetIterator.next();
            LinkedHashMap<Integer, String> innerMap = new LinkedHashMap<>();
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                int key = -1;
                String value = "";
                boolean match = false;
                Cell cell;
                while (cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    String cellValue = dataFormatter.formatCellValue(cell).trim()
                            .replace(" ", "");
                    if (match) {
                        value = cellValue;
                        match = false;
                    } else if (numberPattern.matcher(cellValue).matches()) {
                        key = Integer.parseInt(cellValue);
                        match = true;
                    }
                }
                if (key > 0 && !"".equals(value)) {
                    innerMap.put(key, value);
                }
            }
            codeMap.put(sheet.getSheetName(), innerMap);
        }
        // String newestKey = "";
        Set<Integer> newestCodes = null;
        Set<Integer> total = new TreeSet<>();
        for (Map.Entry<String, LinkedHashMap<Integer, String>> entry : codeMap.entrySet()) {
            if (newestCodes == null) {
                // newestKey = entry.getKey();
                newestCodes = entry.getValue().keySet();
            }
            //} else {
            //    Set<Integer> result = new HashSet<>(newestCodes);
            //    result.removeAll(entry.getValue().keySet());
            //    System.out.print(newestKey + "比" + entry.getKey() + "多" + result.toString());
            //    result.clear();
            //    result.addAll(entry.getValue().keySet());
            //    result.removeAll(newestCodes);
            //    System.out.println("，少" + result.toString());
            //}
            total.addAll(entry.getValue().keySet());
        }
        // System.out.println(total);
        System.out.println("共" + total.size() + "个编号");
        LinkedHashMap<Integer, Region> regionMap = new LinkedHashMap<>();
        for (int code : total) {
            Region region = new Region();
            if (newestCodes.contains(code)) {
                region.setAvailable(true);
            }
            for (Map.Entry<String, LinkedHashMap<Integer, String>> entry : codeMap.entrySet()) {
                LinkedHashMap<Integer, String> innerMap = entry.getValue();
                int provinceCode = code - code % 10000;
                int cityCode = code - code % 100;
                region.setCode(code);
                if (innerMap.containsKey(code) && "".equals(region.getAvailableUntil())) {
                    String availableUntil = entry.getKey();
                    if (!availableUntil.contains(".")) {
                        availableUntil = availableUntil + ".12";
                    }
                    region.setAvailableUntil(availableUntil);
                    if (innerMap.containsKey(provinceCode)) {
                        region.setProvinceName(innerMap.get(provinceCode));
                    }
                    if (cityCode % 10000 != 0
                            && innerMap.containsKey(cityCode)) {
                        region.setCityName(innerMap.get(cityCode));
                    } else if (region.getProvinceName().endsWith("市")) {
                        region.setCityName(region.getProvinceName());
                    }
                    if (code % 10000 != 0 && code % 100 != 0) {
                        region.setDistrictName(innerMap.get(code));
                    }
                } else {
                    if (innerMap.containsKey(cityCode)) {
                        String oldCityName = innerMap.get(cityCode);
                        if (!region.getCityName().equals("")
                                && !region.getCityName().equals(oldCityName)
                                && !region.getHistoryCityNames().contains(oldCityName)
                                && !oldCityName.equals(region.getCityName())) {
                            region.getHistoryCityNames().add(oldCityName);
                        }
                    }
                    if (innerMap.containsKey(code)) {
                        String oldDistrictName = innerMap.get(code);
                        if (!region.getDistrictName().equals("")
                                && !region.getDistrictName().equals(oldDistrictName)
                                && !region.getHistoryDistrictNames().contains(oldDistrictName)
                                && !oldDistrictName.equals(region.getDistrictName())) {
                            region.getHistoryDistrictNames().add(oldDistrictName);
                        }
                    }
                }
                regionMap.put(code, region);
            }
        }
        // System.out.println(regionMap);
        // 这种读取方式不会忽视BOM，需要一个不带BOM的csv文件
        List<String> geoStrings = Files.readAllLines(Paths.get("./geo.csv"),
                StandardCharsets.UTF_8);
        Map<Integer, Geo> geoMap = new HashMap<>();
        for (String geoString : geoStrings) {
            geoString = geoString.trim();
            if (!"".equals(geoString)) {
                String[] split = geoString.split(",");
                if (split.length == 4) {
                    Geo geo = new Geo(Double.parseDouble(split[3]), Double.parseDouble(split[2]));
                    geoMap.put(Integer.parseInt(split[0]), geo);
                }
            }
        }
        for (Map.Entry<Integer, Region> entry : regionMap.entrySet()) {
            Region region = entry.getValue();
            int code = region.getCode();
            int provinceCode = code - code % 10000;
            int cityCode = code - code % 100;
            int availableCode = 110000;
            if (geoMap.containsKey(region.getCode())) {
                availableCode = code;
            } else if (geoMap.containsKey(cityCode)) {
                availableCode = cityCode;
                //System.out.println(code + "-" + cityCode);
            } else if (geoMap.containsKey(provinceCode)) {
                availableCode = provinceCode;
                //System.out.println(code + "-" + provinceCode);
            } else {
                //System.out.println("ERROR");
            }
            Geo geo = geoMap.get(availableCode);
            region.setLongitude(geo.longitude);
            region.setLatitude(geo.latitude);
        }
        try (FileWriter fileWriter = new FileWriter("./region.csv")) {
            for (Map.Entry<Integer, Region> entry : regionMap.entrySet()) {
                fileWriter.write(entry.getValue().toString());
                fileWriter.write("\n");
            }
        }
    }

    @AllArgsConstructor
    private static class Geo {
        double longitude;
        double latitude;
    }
}
