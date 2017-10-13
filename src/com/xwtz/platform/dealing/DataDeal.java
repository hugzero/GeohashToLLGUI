package com.xwtz.platform.dealing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xwtz.platform.ui.dialog.MainFrameDialog;
import com.xwtz.platform.util.LocationBean;

public class DataDeal implements IDataDealRunner {
	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	private static HashMap<Character, Integer> CHARSMAP;
	private long totalSizes, lineNumber;
	private final MainFrameDialog mainFrameDialog;
	private boolean cancelled;
	private final String rout;

	static {
		CHARSMAP = new HashMap<Character, Integer>();
		for (int i = 0; i < CHARS.length; i++) {
			CHARSMAP.put(CHARS[i], i);
		}
	}

	// public DataDeal(MainFrameDialog mainFrameDialog) {
	// this.mainFrameDialog = mainFrameDialog;
	// }

	public DataDeal(String rout, MainFrameDialog mainFrameDialog) {
		this.mainFrameDialog = mainFrameDialog;
		this.rout = rout;
		// init(rout);
	}

	private void init(String rout) {

		long starttime = System.currentTimeMillis();
		System.out.println(starttime);
		// String geoHash = "wx4en3s4";
		ArrayList<String> columnList = new ArrayList<String>();
		List<String> aList = rout(rout, columnList);

		// 导出到
		// exportData(bean);
		exportData(aList);
		long end = System.currentTimeMillis();
		System.out.println(end);
		System.out.println(end - starttime);

	}

	private void exportData(List<String> aList) {
		lineNumber = 0;
		// 导入到excel
		// 第一步，创建一个webbook，对应一个Excel文件
		XSSFWorkbook wb = new XSSFWorkbook();
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = wb.createSheet("学生表一");
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		XSSFRow row = sheet.createRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		XSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

		XSSFCell cell = row.createCell(0);
		cell.setCellValue("latlng");
		cell.setCellStyle(style);

		for (int i = 0; i < aList.size(); i++) {
			row = sheet.createRow((int) i + 1);
			LocationBean bean = getLocation(aList.get(i));
			row.createCell((short) 0).setCellValue(bean.getLat() + "," + bean.getLng());

		}
		// 第六步，将文件存到指定位置
		try {
			File file = new File("D:/deal_out.xlsx");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(file);
			wb.write(fout);
			mainFrameDialog.showProgress("完成");
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> rout(String rount, ArrayList<String> columnList) {
		// synchronized (columnList) {
		try {
			mainFrameDialog.enableProgress();
			mainFrameDialog.showProgress("Scanning historical datafile...");
			File file = new File(rount);
			long t = System.currentTimeMillis();
			System.out.println("耗时操作：" + t);
			FileInputStream in = new FileInputStream(file);
			XSSFWorkbook wb = new XSSFWorkbook(in);
			System.out.println("耗时操作结束耗时：" + (System.currentTimeMillis() - t));
			// XSSFWorkbook wbq= new XSSFWorkbook()
			// 取得工作表
			Sheet sheet = wb.getSheetAt(0);
			int firstRowNum = sheet.getFirstRowNum();
			int lastRowNum = sheet.getLastRowNum();

			// this.setTotalSizes(lastRowNum);
			Row row = null;
			Cell cell_b = null;
			long marketDepthCounter = 0;
			long size = lastRowNum;

			// // marketDepthCounter = dataDeal.getLineNumber();
			// // if (!cancelled) {
			mainFrameDialog.showProgress("Running back test...");
			for (int i = firstRowNum; i <= lastRowNum; i++) {
				row = sheet.getRow(i); // 取得第i行
				cell_b = row.getCell(1); // 取得i行的第二列
				String cellValue = cell_b.getStringCellValue().trim();
				columnList.add(cellValue);
				marketDepthCounter++;
				if (marketDepthCounter % 500 == 0) {
					// System.out.println(marketDepthCounter++);
					mainFrameDialog.setProgress(marketDepthCounter, size, "Running back test");
					// Thread.sleep(200);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return columnList;
		// }
	}

	/**
	 * @param geoHash
	 * @return
	 * @Author:lulei
	 * @Description: 返回geoHash 对应的坐标
	 */
	public static LocationBean getLocation(String geoHash) {
		String geoHashBinaryStr = getGeoHashBinaryString(geoHash);
		if (geoHashBinaryStr == null) {
			return null;
		}
		StringBuffer lat = new StringBuffer();
		StringBuffer lng = new StringBuffer();
		for (int i = 0; i < geoHashBinaryStr.length(); i++) {
			if (i % 2 != 0) {
				lat.append(geoHashBinaryStr.charAt(i));
			} else {
				lng.append(geoHashBinaryStr.charAt(i));
			}
		}
		double latValue = getGeoHashMid(lat.toString(), LocationBean.MINLAT, LocationBean.MAXLAT);
		double lngValue = getGeoHashMid(lng.toString(), LocationBean.MINLNG, LocationBean.MAXLNG);
		LocationBean location = new LocationBean(latValue, lngValue);
		location.setGeoHash(geoHash);
		return location;
	}

	/**
	 * @param geoHash
	 * @return
	 * @Author:lulei
	 * @Description: 将geoHash转化为二进制字符串
	 */
	private static String getGeoHashBinaryString(String geoHash) {
		if (geoHash == null || "".equals(geoHash)) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < geoHash.length(); i++) {
			char c = geoHash.charAt(i);
			if (CHARSMAP.containsKey(c)) {
				String cStr = getBase32BinaryString(CHARSMAP.get(c));
				if (cStr != null) {
					sb.append(cStr);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @param i
	 * @return
	 * @Author:lulei
	 * @Description: 将数字转化为二进制字符串
	 */
	private static String getBase32BinaryString(int i) {
		if (i < 0 || i > 31) {
			return null;
		}
		String str = Integer.toBinaryString(i + 32);
		return str.substring(1);
	}

	/**
	 * @param binaryStr
	 * @param min
	 * @param max
	 * @return
	 * @Author:lulei
	 * @Description: 返回二进制对应的中间值
	 */
	private static double getGeoHashMid(String binaryStr, double min, double max) {
		if (binaryStr == null || binaryStr.length() < 1) {
			return (min + max) / 2.0;
		}
		if (binaryStr.charAt(0) == '1') {
			return getGeoHashMid(binaryStr.substring(1), (min + max) / 2.0, max);
		} else {
			return getGeoHashMid(binaryStr.substring(1), min, (min + max) / 2.0);
		}
	}

	public long getTotalSizes() {
		return totalSizes;
	}

	public void setTotalSizes(long totalSizes) {
		this.totalSizes = totalSizes;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public void run() {

		init(rout);
		// for (int i = 0; i < size; i++) {
		// marketDepthCounter++;
		// if (marketDepthCounter % 1000 == 0) {
		// System.out.println(marketDepthCounter++);
		// mainFrameDialog.setProgress(marketDepthCounter, size, "Running back
		// test");
		// // Thread.sleep(200);
		// }
		// }

	}

	@Override
	public void cancel() {
		// backTestReader.cancel();
		// mainFrameDialog.showProgress("Stopping back test...");
		cancelled = true;
	}

}
