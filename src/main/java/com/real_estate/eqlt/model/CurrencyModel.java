package com.real_estate.eqlt.model;

import lombok.Data;

import java.util.Date;

@Data
public class CurrencyModel {
    Integer curId;
    Date date;
    String curAbbreviation;
    Integer curScale;
    String curName;
    Double curOfficialRate;
}
