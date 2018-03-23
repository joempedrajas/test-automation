package com.test.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

import com.automationlabs.rest.utils.file.ExcelFile;
import com.utils.Utils;

public class DataReaderUtils {

	private static final Logger LOG = Logger.getLogger(DataReaderUtils.class);

	private DataReaderUtils(){}

	public static Object[][] getTestFlowData(String path, String sheet, String testFlowId) throws Exception{
		try{
			ExcelFile file = new ExcelFile(path);
			Cell cell;
			Object[][] obj;
			List<String> headers;
			if (file.hasSheet(sheet)) {
				cell = file.getCell(sheet, testFlowId);
				obj = file.getDataUntilBlank(sheet, cell.getRowIndex(), cell.getColumnIndex());
				headers = file.getAllCellsInRow(sheet, cell.getRowIndex() - 1);
			}else
			{
				LOG.error("Sheet " + sheet + " not found in " + path);
				throw new Exception("Sheet " + sheet + " not found in " + path);
			}
			file.close();
			return Utils.objectToMap(obj, headers);
		}catch(IOException ioe){
			LOG.error("Problem reading excel file " + path);
			throw ioe;
		}catch(NullPointerException npe){
			LOG.error("Test Flow " + testFlowId + " not found.");
			throw npe;
		}catch(Exception e){
			LOG.error(e.getMessage());
			throw e;
		}
	}

	public static Map<String, String> getTestCaseData(String path, String sheet, String testFlowId, String testCaseId) throws Exception{
		ExcelFile file = new ExcelFile(path);

		List<String> headers;
		List<String> datas;

		if (file.hasSheet(sheet)) {
			Cell cellTestCase = file.getCell(sheet, testCaseId);
			Cell cellTestFlow = file.getCell(sheet, testFlowId);

			if(cellTestCase == null || cellTestFlow == null)
				throw new Exception("Test Case or Test Flow ID not found.");

			datas = file.getAllCellsInRow(sheet, cellTestCase.getRowIndex());
			headers = file.getAllCellsInRow(sheet, cellTestFlow.getRowIndex() - 1);
		}else
		{
			LOG.error("Sheet " + sheet + " not found in " + path);
			throw new Exception("Sheet " + sheet + " not found in " + path);
		}
		file.close();
		return Utils.toMap(headers, datas);
	}



}
