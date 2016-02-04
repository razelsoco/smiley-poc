package dbs.smileytown.poc.utils;


import android.content.Context;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import dbs.smileytown.poc.receiver.FileDownloader;

/**
 * Created by razelsoco on 26/1/16.
 */
public class ExcelParser {
    private static ExcelParser mExcelFileParser;

    public static ExcelParser getInstance(){
        if(mExcelFileParser == null)
            mExcelFileParser = new ExcelParser();

        return mExcelFileParser;
    }

    HashMap<String, BalanceData> mBalanceDataMap = new HashMap<String, BalanceData>();


    public void parse(Context c) {
        FileLogger.getInstance().writeLogs("Balance data file parsing start");
        try {
            mBalanceDataMap.clear();
            //InputStream is = c.getAssets().open("data.xlsx");
            InputStream is = new FileInputStream(FileDownloader.getFile(c));
            Workbook wb = WorkbookFactory.create(is); //new XSSFWorkbook(is);

            final Sheet sheet = wb.getSheetAt(0);

            for (final Row row : sheet) {
                BalanceData balanceData = new BalanceData();
                if(row.getRowNum() == 0) continue;
                for (Cell cell : row) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    if (cell.getColumnIndex() == 0) {
                        balanceData.date = cell.getStringCellValue().trim();
                    } else if (cell.getColumnIndex() == 1) {
                        balanceData.cardNumber = cell.getRichStringCellValue().getString();
                    } else if (cell.getColumnIndex() == 2) {
                        balanceData.balance = cell.getRichStringCellValue().getString();
                    }
                }
                mBalanceDataMap.put(balanceData.cardNumber, balanceData);
            }

            FileLogger.getInstance().writeLogs("Balance data file parsing finish SIZE => "+ mBalanceDataMap.size());
        } catch (IOException e) {
            e.printStackTrace();
            FileLogger.getInstance().writeLogs("PARSE ERROR 1:"+ e.getMessage());
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            FileLogger.getInstance().writeLogs("PARSE ERROR 2:" + e.getMessage());
        } catch (Exception e){
            FileLogger.getInstance().writeLogs("PARSE ERROR 3:" + e.getMessage());
        }
    }

    public HashMap<String, BalanceData> getBalanceDataMap() {
        return mBalanceDataMap;
    }
}
