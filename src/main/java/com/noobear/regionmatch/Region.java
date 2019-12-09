package com.noobear.regionmatch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Noobear
 * @version 2019-11-17 16:48
 **/

@Getter
@Setter
@EqualsAndHashCode
public class Region {
    private int code = -1;
    private String provinceName = "";
    private String cityName = "";
    private List<String> historyCityNames = new ArrayList<>();
    private String districtName = "";
    private List<String> historyDistrictNames = new ArrayList<>();
    private double longitude = -1.0D;
    private double latitude = -1.0D;
    private boolean available = false;
    private String availableUntil = "";

    @Override
    public String toString() {
        StringBuilder oldCityNamesBuilder = new StringBuilder();
        for (String oldCityName : historyCityNames) {
            oldCityNamesBuilder.append(oldCityName)
                    .append("|");
        }
        if (oldCityNamesBuilder.length() > 1) {
            oldCityNamesBuilder.deleteCharAt(oldCityNamesBuilder.length() - 1);
        }
        StringBuilder oldDistrictNamesBuilder = new StringBuilder();
        for (String oldDistrictName : historyDistrictNames) {
            oldDistrictNamesBuilder.append(oldDistrictName)
                    .append("|");
        }
        if (oldDistrictNamesBuilder.length() > 1) {
            oldDistrictNamesBuilder.deleteCharAt(oldDistrictNamesBuilder.length() - 1);
        }
        return code + ","
                + provinceName + ","
                + cityName + ","
                + oldCityNamesBuilder.toString() + ","
                + districtName + ","
                + oldDistrictNamesBuilder.toString() + ","
                + longitude + ","
                + latitude + ","
                + available + ","
                + availableUntil;
    }
}
