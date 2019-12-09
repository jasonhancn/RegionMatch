package com.noobear.regionmatch;

import lombok.Data;

/**
 * @author Noobear
 * @version 2019/12/7 15:52
 **/
@Data
public class MatchResult {
    private double probability = 0D;
    private Region region = null;
}
