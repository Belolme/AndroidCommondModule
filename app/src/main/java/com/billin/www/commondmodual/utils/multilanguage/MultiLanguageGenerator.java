package com.billin.www.commondmodual.utils.multilanguage;

import android.text.TextUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class MultiLanguageGenerator {

//    private static final String FILE_NAME = "2.3.2_124.xlsx";

    @Nullable
    public String run(String inputFile) {

        String excelFilePath = null;
        File file = new File("");
        try {
            excelFilePath = file.getCanonicalPath() + File.separator
                    + "multi-language" + File.separator
                    + "excel" + File.separator
                    + inputFile;
        } catch (IOException e) {
            System.out.println("Read module path fail: " + e.toString());
            return null;
        }
        File excelFile = new File(excelFilePath);
        if (!excelFile.exists()) {
            System.out.println("File not found: filePath = " + excelFilePath);
            return null;
        }
        Workbook workbook = readExcel(excelFilePath);
        if (workbook == null) {
            System.out.println("Read excel fail.");
            return null;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            System.out.println("No sheet in this excel.");
            return null;
        }
        int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount <= 1) {
            System.out.println("Invalid format.");
            return null;
        }

        /**************************** 把excel的内容转变成字符串数组 BENGIN ****************************/
        StringBuilder[] sbArry = new StringBuilder[Language.values().length];
        Row row;
        String resIdName;
        for (int i = 1; i < rowCount; i++) {
            row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            // 资源ID名
            resIdName = getCellString(row.getCell(0));
            String multiLanguageData;
            for (int j = 0; j < Language.values().length; j++) {
                multiLanguageData = getCellString(row.getCell(j + 1)).replaceAll("'", "\\\\'").trim();
                if (TextUtils.isEmpty(multiLanguageData)) {
                    continue;
                }
                StringBuilder sb = sbArry[j];
                if (sb == null) {
                    sb = new StringBuilder();
                    sbArry[j] = sb;
                }
                sb.append("<string name=\"");
                sb.append(resIdName);
                sb.append("\">");
                /**
                 * 1、把'转换成\'，不然复制到strings.xml中会报错
                 * 2、把字符串首尾的所有空格去掉
                 */
                sb.append(multiLanguageData);
                sb.append("</string>");
                sb.append("\n");
            }
        }
//        for(StringBuilder sb : sbArry) {
//            sb.deleteCharAt(sb.length() - 1);
//        }
        /***************************** 把excel的内容转变成字符串数组 END *****************************/


        /**************************** 把转换好的数据保存到本地文件中 BENGIN ****************************/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH-mm-ss");
        File saveFile = new File(excelFile.getParent() + File.separator
                + inputFile.replaceAll("\\..*", "") + "-result-" + dateFormat.format(System.currentTimeMillis())
                + ".txt");
        try {
            saveFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Create saveFile fail: " + e.toString());
            return null;
        }
        StringBuilder resultSB = new StringBuilder();
        for (int i = 0; i < sbArry.length; i++) {
            if (TextUtils.isEmpty(sbArry[i])) {
                continue;
            }
            resultSB.append("------------------------ " + Language.values()[i].name() + " ------------------------");
            resultSB.append("\n");
            resultSB.append(sbArry[i]);
            resultSB.append("\n");
        }
        resultSB.deleteCharAt(resultSB.length() - 1);
        try {
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(saveFile.getAbsolutePath());
            fileOutputStream.write(resultSB.toString().getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("Write data fail: " + e.toString());
            return null;
        }
        System.out.println("Write data success, filePath = " + saveFile.getAbsolutePath());
        /***************************** 把转换好的数据保存到本地文件中 END *****************************/

        return saveFile.getPath();
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return cell.getRichStringCellValue().getString();
    }

    /**
     * 读取excel
     */
    private static Workbook readExcel(String filePath) {
        if (filePath == null) {
            return null;
        }

        Workbook workbook = null;
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                workbook = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }
}
