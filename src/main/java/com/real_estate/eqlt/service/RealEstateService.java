package com.real_estate.eqlt.service;


import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealEstateService {

    @SneakyThrows
    public InputFile addPicture(String url) {
        URL imageUrl = null;
        try {
            imageUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        InputStream imageStream = null;
        if (imageUrl != null) {
            imageStream = imageUrl.openStream();
        }
        return new InputFile(imageStream, "photo.png");
    }

    public List<List<String>> getExcelRealEstateSheetData(String excelFilePath, int rows) {
        List<List<String>> tableValues = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);
            for (int row = 1; row < rows; row++) {
                List<String> rowValues = new ArrayList<>();
                Row currentRow = sheet.getRow(row);
                rowValues.add(currentRow.getCell(0).toString());
                rowValues.add(currentRow.getCell(1).toString());
                rowValues.add(currentRow.getCell(2).toString());
                rowValues.add(currentRow.getCell(3).toString());
                rowValues.add(String.valueOf((long) Double.parseDouble(currentRow.getCell(4).toString())));
                rowValues.add(currentRow.getCell(5).toString());
                rowValues.add(currentRow.getCell(6).toString());
                rowValues.add(currentRow.getCell(7).toString());
                rowValues.add(currentRow.getCell(8).toString());
                rowValues.add(currentRow.getCell(9).toString());
                tableValues.add(rowValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableValues;
    }


}

