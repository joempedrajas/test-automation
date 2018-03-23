package com.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataReaderUtil {

	private static String PATH;
	private static String SHEET;
	private static String CROSSSHEET;

	public DataReaderUtil(String PATH, String SHEET, String CROSSSHEET){
		DataReaderUtil.PATH = PATH;
		DataReaderUtil.SHEET =  SHEET;
		DataReaderUtil.CROSSSHEET = CROSSSHEET;
	}

	/**
	 * Gets the data from the excel sheet.
	 *
	 * @param map			- Mapping of data from the excel sheet.
	 * @param columnHeader	- Column header of the required data.
	 * @return
	 * @throws Exception
	 */
	public String getData(String testCaseID, String columnHeader) throws Exception{
		String data = null;
		String testFlowId = testCaseID.substring(0, testCaseID.length() - 4);

		Map<String, String> map = DataReaderUtils.getTestCaseData(PATH, SHEET, testFlowId, testCaseID);

		data = map.get(columnHeader);

		if(checkCrossData(data)){
			data = getCrossScriptData(data);
		}
		else if (checkCrossData_yes(data)!=0) {
			int index = checkCrossData_yes(data);
			String newData = data.replaceAll("[\\d]", "");
			data = getCrossScriptData_yes(newData, index);
		}

		return data;
	}

	public void setData(String columnHeader, String value) throws Exception{
		int maxRowCount = getCrossScriptMaxRow();
		int maxColCount = getCrossScriptMaxCol(0);
		for(int colCtr=0, maxCol = maxColCount;colCtr<maxCol;colCtr++) { //iterating column
			String colData = getCrossScriptCellData(0, colCtr);
			if(colData.equals(columnHeader)) { //checking column name
				setCrossScriptNewRowCellValue(maxRowCount-1, colCtr, value);
				break;
			}
		}
	}

	/**
	 * Checks if the data from the cell is a Cross Scripts Data
	 * Data ending with "_DATA"
	 *
	 * @param data	- the initial cell value from the Datasheet sheet.
	 * @return
	 */
	private boolean checkCrossData(String data){
		if (data.endsWith("_DATA"))
			return true;
		else
			return false;
	}

	private static int checkCrossData_yes(String data) {
		int index=0;
		String value;
		Pattern p = Pattern.compile("_DATA[0-9]+$");
		Matcher m = p.matcher(data);
		if(m.find()) {
			value = data.replaceAll("[^\\d]", "");
			index = Integer.valueOf(value);
			return index;
		}
		else
			return index;
	}

	/**
	 * Gets the data from the Cross Script Data sheet.
	 *
	 * @param columnHeader		- Column header of the data.
	 * @return
	 * @throws IOException
	 */
	private String getCrossScriptData(String columnHeader) throws Exception{
		String value = null;
		int maxRowCount = getCrossScriptMaxRow();

		for(int colCtr=0, maxCol = getCrossScriptMaxCol(0);colCtr<maxCol;colCtr++) { //iterating column
			String colData = getCrossScriptCellData(0, colCtr);
			if(colData.equals(columnHeader)) { //checking column name
				for(int rowCtr=1;rowCtr<maxRowCount;rowCtr++){ //iterating row
					String rowDataIsUsed = getCrossScriptCellData(rowCtr, colCtr + 1);
					if(!rowDataIsUsed.equals("Yes"))
					{
						value=getCrossScriptCellData(rowCtr, colCtr);
						setCrossScriptCellValue(rowCtr, colCtr + 1, "Yes");
						break;
					}
				}
				break;
			}
		}
		return value;
	}

	private String getCrossScriptData_yes(String columnHeader, int index) throws Exception{
		String value = null;
		int maxRowCount = getCrossScriptMaxRow();

		for(int colCtr=0, maxCol = getCrossScriptMaxCol(0);colCtr<maxCol;colCtr++) { //iterating column
			String colData = getCrossScriptCellData(0, colCtr);
			if(colData.equals(columnHeader)) { //checking column name
				for(int rowCtr=1;rowCtr<maxRowCount;rowCtr++){ //iterating row
					String rowDataIsUsed = getCrossScriptCellData(rowCtr, colCtr + 1);
					if(rowDataIsUsed.equals("Yes"))
					{
						value=getCrossScriptCellData(rowCtr, colCtr+index);
						break;
					}
				}
				break;
			}
		}
		return value;
	}

	/*
	 * Start of Cross Script Datasheet methods
	 */

    @SuppressWarnings("resource")
    private int getCrossScriptMaxRow() throws Exception {
        int max;
		FileInputStream inputStream = new FileInputStream(new File(PATH));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet datasheet = workbook.getSheet(CROSSSHEET);
        max=datasheet.getPhysicalNumberOfRows();
        return max;
	}

	@SuppressWarnings("resource")
	private int getCrossScriptMaxCol(int row) throws Exception {
		int max;
		FileInputStream inputStream = new FileInputStream(new File(PATH));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet datasheet = workbook.getSheet(CROSSSHEET);
        max=datasheet.getRow(row).getLastCellNum();
        return max;
	}

	private String getCrossScriptCellData(int row, int column) throws Exception {
		String cellValue=null;
		boolean blank=false;
		FileInputStream inputStream = new FileInputStream(new File(PATH));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet datasheet = workbook.getSheet(CROSSSHEET);

        Cell cell = datasheet.getRow(row).getCell(column);
        try {
        	cell.setCellType(Cell.CELL_TYPE_STRING);
		} catch (Exception e) {
			blank=true;
		}

        workbook.close();
        inputStream.close();

        if(blank==false)
        	cellValue = cell.getStringCellValue();
        else
        	cellValue="blank";

        return cellValue;
	}

    @SuppressWarnings("resource")
    private void setCrossScriptCellValue(int row, int column, String value) throws Exception {
    	FileInputStream inputStream= new FileInputStream(new File(PATH));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet datasheet = workbook.getSheet(CROSSSHEET);

        Cell cell = null;
        cell = datasheet.getRow(row).createCell(column);
        cell.setCellValue(value);

        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream(new File(PATH));
        workbook.write(outputStream);
        outputStream.close();
    }

    @SuppressWarnings("resource")
    private void setCrossScriptNewRowCellValue(int row, int column, String value) throws Exception {
    	FileInputStream inputStream= new FileInputStream(new File(PATH));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet datasheet = workbook.getSheet(CROSSSHEET);

        datasheet.createRow(row);
        Cell cell = null;
        cell = datasheet.getRow(row).createCell(column);
        cell.setCellValue(value);

        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream(new File(PATH));
        workbook.write(outputStream);
        outputStream.close();
    }

    /*
	 * End of Cross Script Datasheet methods
	 */
}
