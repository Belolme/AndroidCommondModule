package com.billin.www.commondmodual.utils.multilanguage;

import android.text.TextUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 订阅多语言脚本
 */
public class SubsMultiLanguageJsonGenerator {

    private static final String FILE_NAME = "subs_20190506.xlsx";

    public static void main(String[] args) {

        String excelFilePath;
        File file = new File("");
        try {
            excelFilePath = file.getCanonicalPath() + File.separator
                    + "multi-language" + File.separator
                    + "excel" + File.separator
                    + FILE_NAME;
        } catch (IOException e) {
            System.out.println("Read module path fail: " + e.toString());
            return;
        }
        File excelFile = new File(excelFilePath);
        if (!excelFile.exists()) {
            System.out.println("File not found: filePath = " + excelFilePath);
            return;
        }
        Workbook workbook = readExcel(excelFilePath);
        if (workbook == null) {
            System.out.println("Read excel fail.");
            return;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            System.out.println("No sheet in this excel.");
            return;
        }
        int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount <= 1) {
            System.out.println("Invalid format.");
            return;
        }


        String targetDirPath;
        try {
            targetDirPath = new File("").getCanonicalPath()
                    + File.separator + "module_subscription"
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "assets";
        } catch (IOException e) {
            System.out.println("Create targetDirPath fail.");
            return;
        }

        for (int i = 1; i <= 19; i++) {
            /* 解析一种语言，并拼接到sb对象中 */
            int column = i;
            System.out.println(getCellString(sheet.getRow(0).getCell(column)) + ":");

            StringBuilder sb = new StringBuilder();
            // 开头
            sb.append("{\"datas\":[");

//            /*********************************************** 样式1 BEGIN ***********************************************/
//            sb.append("{");
//
//            // 模块ID
//            sb.append(getKeyValue("moduleId", 11));
//            // banner
//            sb.append(getKeyValue("banner", null));
//            // 名称
//            sb.append(getKeyValue("name", null));
//            // 标题
//            sb.append(getKeyValue("title", getCellString(sheet.getRow(1).getCell(column))));
//            // 描述信息
//            sb.append(getKeyValue("description", getCellString(sheet.getRow(2).getCell(column)) + "#"
//                    + getCellString(sheet.getRow(3).getCell(column)) + "#"
//                    + getCellString(sheet.getRow(4).getCell(column)) + "#"
//                    + getCellString(sheet.getRow(5).getCell(column))));
//            // 图标名称
//            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(6).getCell(column))));
//            // 图标描述信息
//            sb.append(getKeyValue("icon_desc", null));
//            // "更多"显示名
//            sb.append(getKeyValue("more_name", getCellString(sheet.getRow(7).getCell(column))));
//            // "按钮"显示名
//            sb.append(getKeyValue("button_name", null));
//            // 视频地址
//            sb.append(getLastKeyValue("cparams", null));
//
//            sb.append("},");
//            /************************************************ 样式1 END ************************************************/


//            /*********************************************** 样式4 BEGIN ***********************************************/
//            /* 样式4—按钮1 */
//            sb.append("{");
//
//            // 模块ID
//            sb.append(getKeyValue("moduleId", 41));
//            // banner
//            sb.append(getKeyValue("banner", null));
//            // 名称
//            sb.append(getKeyValue("name", null));
//            // 标题
//            sb.append(getKeyValue("title", null));
//            // 描述信息
//            sb.append(getKeyValue("description", null));
//            // 图标名称
//            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(3).getCell(column))));
//            // 图标描述信息
//            sb.append(getKeyValue("icon_desc", null));
//            // "更多"显示名
//            sb.append(getKeyValue("more_name", null));
//            // "按钮"显示名
//            sb.append(getKeyValue("button_name", null));
//            // 视频地址
//            sb.append(getLastKeyValue("cparams", null));
//
//            sb.append("},");
//
//            /* 样式4—按钮2 */
//            sb.append("{");
//
//            // 模块ID
//            sb.append(getKeyValue("moduleId", 42));
//            // banner
//            sb.append(getKeyValue("banner", null));
//            // 名称
//            sb.append(getKeyValue("name", null));
//            // 标题
//            sb.append(getKeyValue("title", getCellString(sheet.getRow(2).getCell(column))));
//            // 描述信息
//            sb.append(getKeyValue("description", null));
//            // 图标名称
//            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(4).getCell(column))));
//            // 图标描述信息
//            sb.append(getKeyValue("icon_desc", getCellString(sheet.getRow(5).getCell(column))));
//            // "更多"显示名
//            sb.append(getKeyValue("more_name", getCellString(sheet.getRow(6).getCell(column))));
//            // "按钮"显示名
//            sb.append(getKeyValue("button_name", null));
//            // 视频地址
//            sb.append(getLastKeyValue("cparams", null));
//
//            sb.append("},");
//            /************************************************ 样式4 END ************************************************/


            /*********************************************** 样式5 BEGIN ***********************************************/
            sb.append("{");

            // 模块ID
            sb.append(getKeyValue("moduleId", 51));
            // banner
            sb.append(getKeyValue("banner", null));
            // 名称
            sb.append(getKeyValue("name", getCellString(sheet.getRow(9).getCell(column))));
            // 标题
            sb.append(getKeyValue("title", null));
            // 描述信息
            sb.append(getKeyValue("description", getCellString(sheet.getRow(10).getCell(column))));
            // 图标名称
            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(11).getCell(column))));
            // 图标描述信息
            sb.append(getKeyValue("icon_desc", null));
            // "更多"显示名
            sb.append(getKeyValue("more_name", null));
            // "按钮"显示名
            sb.append(getKeyValue("button_name", null));
            // 视频地址
            sb.append(getLastKeyValue("cparams", null));

            sb.append("},");
            /************************************************ 样式5 END ************************************************/


            /*********************************************** 样式6 BEGIN ***********************************************/
            sb.append("{");

            // 模块ID
            sb.append(getKeyValue("moduleId", 61));
            // banner
            sb.append(getKeyValue("banner", null));
            // 名称
            sb.append(getKeyValue("name", null));
            // 标题
            sb.append(getKeyValue("title", getCellString(sheet.getRow(13).getCell(column))));
            // 描述信息
            sb.append(getKeyValue("description", getCellString(sheet.getRow(14).getCell(column))));
            // 图标名称
            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(15).getCell(column))));
            // 图标描述信息
            sb.append(getKeyValue("icon_desc", null));
            // "更多"显示名
            sb.append(getKeyValue("more_name", getCellString(sheet.getRow(16).getCell(column))));
            // "按钮"显示名
            sb.append(getKeyValue("button_name", null));
            // 视频地址
            sb.append(getLastKeyValue("cparams", null));

            sb.append("},");
            /************************************************ 样式6 END ************************************************/

            /*********************************************** 样式7 BEGIN ***********************************************/
            /* 样式7—按钮1 */
            sb.append("{");

            // 模块ID
            sb.append(getKeyValue("moduleId", 71));
            // banner
            sb.append(getKeyValue("banner", null));
            // 名称
            sb.append(getKeyValue("name", null));
            // 标题
            sb.append(getKeyValue("title", null));
            // 描述信息
            sb.append(getKeyValue("description", null));
            // 图标名称
            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(3).getCell(column))));
            // 图标描述信息
            sb.append(getKeyValue("icon_desc", null));
            // "更多"显示名
            sb.append(getKeyValue("more_name", null));
            // "按钮"显示名
            sb.append(getKeyValue("button_name", null));
            // 视频地址
            sb.append(getLastKeyValue("cparams", null));

            sb.append("},");

            /* 样式7—按钮2 */
            sb.append("{");

            // 模块ID
            sb.append(getKeyValue("moduleId", 72));
            // banner
            sb.append(getKeyValue("banner", null));
            // 名称
            sb.append(getKeyValue("name", null));
            // 标题
            sb.append(getKeyValue("title", getCellString(sheet.getRow(2).getCell(column))));
            // 描述信息
            sb.append(getKeyValue("description", null));
            // 图标名称
            sb.append(getKeyValue("icon_name", getCellString(sheet.getRow(4).getCell(column))));
            // 图标描述信息
            sb.append(getKeyValue("icon_desc", getCellString(sheet.getRow(5).getCell(column))));
            // "更多"显示名
            sb.append(getKeyValue("more_name", getCellString(sheet.getRow(7).getCell(column))));
            // "按钮"显示名
            sb.append(getKeyValue("button_name", getCellString(sheet.getRow(6).getCell(column))));
            // 视频地址
            sb.append(getLastKeyValue("cparams", null));

            sb.append("},");
            /************************************************ 样式7 END ************************************************/


            /*********************************************** 样式8 BEGIN ***********************************************/
            sb.append("{");

            // 模块ID
            sb.append(getKeyValue("moduleId", 81));
            // banner
            sb.append(getKeyValue("banner", null));
            // 名称
            sb.append(getKeyValue("name", null));
            // 标题
            sb.append(getKeyValue("title", getCellString(sheet.getRow(18).getCell(column))));
            // 描述信息
            sb.append(getKeyValue("description", getCellString(sheet.getRow(19).getCell(column))));
            // 图标名称
            sb.append(getKeyValue("icon_name", null));
            // 图标描述信息
            sb.append(getKeyValue("icon_desc", null));
            // "更多"显示名
            sb.append(getKeyValue("more_name", getCellString(sheet.getRow(21).getCell(column))));
            // "按钮"显示名
            sb.append(getKeyValue("button_name", getCellString(sheet.getRow(20).getCell(column))));
            // 视频地址
            sb.append(getLastKeyValue("cparams", null));

            sb.append("},");
            /************************************************ 样式8 END ************************************************/

            // 结束
            sb.deleteCharAt(sb.length() - 1); //去掉最后的','
            sb.append("]}");
            System.out.println(sb.toString());


            /* 开始写入对应的语言文件中 */
            String langAndRegion = getCellString(sheet.getRow(0).getCell(column));
            String language = langAndRegion.split("-")[0];
            String region = langAndRegion.split("-").length > 1 ? langAndRegion.split("-")[1] : null;
            String targetFilePath = targetDirPath + File.separator
                    + "SplashResources_"
                    + language.toLowerCase()
                    + (TextUtils.isEmpty(region) ? "" : ("_" + region.toUpperCase()))
                    + ".json";
            System.out.println("targetFilePath = " + targetFilePath);
            try {
                FileOutputStream fileOutputStream;
                fileOutputStream = new FileOutputStream(targetFilePath);
                fileOutputStream.write(sb.toString().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                System.out.println("Write data fail: " + e.toString());
                return;
            }

            System.out.println();
        }
    }

    private static String getKeyValue(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            throw new RuntimeException("Key is empty.");
        }

        return "\"" + key + "\":" + value + ",";
    }

    private static String getKeyValue(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            throw new RuntimeException("Key is empty.");
        }

        if (!TextUtils.isEmpty(value)) {
            return "\"" + key + "\":\"" + value + "\",";
        } else {
            return "\"" + key + "\":\"\",";
        }
    }

    private static String getLastKeyValue(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            throw new RuntimeException("Key is empty.");
        }

        if (!TextUtils.isEmpty(value)) {
            return "\"" + key + "\":\"" + value + "\"";
        } else {
            return "\"" + key + "\":\"\"";
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
//        String string = cell.getRichStringCellValue().getString();
//        string = string.replaceAll("\n", "\\\n");
//        return string;
        return cell.getRichStringCellValue().getString().trim();
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
