package com.xwtz.platform.dealing;

import com.xwtz.platform.ui.dialog.MainFrameDialog;

public class DataDealRunner implements IDataDealRunner {
	private final MainFrameDialog mainFrameDialog;
	// private final DataDeal dataDeal;
	private boolean cancelled;
	private final String fileName;

	public DataDealRunner(MainFrameDialog mainFrameDialog, String fileName) {
		this.mainFrameDialog = mainFrameDialog;
		this.fileName = fileName;
	}

	@Override
	public void cancel() {
		// mainFrameDialog.cancel();
		mainFrameDialog.showProgress("Stopping back test...");
		cancelled = true;
	}

	@Override
	public void run() {
		try {
			mainFrameDialog.enableProgress();
			mainFrameDialog.showProgress("Scanning historical data file...");
			DataDeal dataDeal = new DataDeal(fileName, mainFrameDialog);
			long marketDepthCounter = 0;
			long size = dataDeal.getTotalSizes();
			System.out.println("size," + size);
			// marketDepthCounter = dataDeal.getLineNumber();
			// if (!cancelled) {
			mainFrameDialog.showProgress("Running back test...");
			for (int i = 0; i < size; i++) {
				marketDepthCounter++;
				if (marketDepthCounter % 1000 == 0) {
					mainFrameDialog.setProgress(marketDepthCounter, size, "Running back test");
				}
			}
			// }
			// mainFrameDialog.showProgress("完成");
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			// mainFrameDialog.dispose();
		}
	}

}
