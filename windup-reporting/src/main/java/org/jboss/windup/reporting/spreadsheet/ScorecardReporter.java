/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.reporting.spreadsheet;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.archetype.version.Version;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;
import org.jboss.windup.reporting.html.ArchiveReport;


public class ScorecardReporter implements Reporter {
	private static final Log LOG = LogFactory.getLog(ScorecardReporter.class);
	
	protected File generateScorecardName(ArchiveMetadata archive, File reportDirectory) {
		Validate.notNull(archive, "Archive is required, but null.");
		Validate.notNull(reportDirectory, "Report directory is required, but null.");
		
		String scorecardName = "scorecard-" + StringUtils.replace(archive.getName(), ".", "-") + ".xlsx";
		String outputName = reportDirectory.getAbsolutePath() + File.separator + scorecardName;
		File output = new File(outputName);
		
		return output;
	}
	
	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		Validate.notNull(archive, "Archive is required, but null.");
		Validate.notNull(reportDirectory, "Report directory is required, but null.");
		
		File output = generateScorecardName(archive, reportDirectory);
		
		List<ArchiveMetadata> results = unwind(archive);
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(output);
			XSSFWorkbook workbook = new XSSFWorkbook();
			// create a new sheet
			XSSFSheet s = workbook.createSheet("MigrationScoreCard");

			s.setColumnWidth(0, 70 * 256);
			s.setColumnWidth(1, 20 * 256);
			s.setColumnWidth(2, 10 * 256);
			s.setColumnWidth(3, 255 * 256);

			appendTitleRow(workbook, s, 0);
			int rownum = 1;
			for (ArchiveMetadata result : results)
			{
				StringBuilder notes = new StringBuilder();

				for (AbstractDecoration dr : result.getDecorations()) {
					if (dr instanceof Version)
					{
						notes.append(dr.toString());
					}
				}

				Set<String> classifications = new HashSet<String>();
				double estimate = 0;
				// calculate the nodes..
				for (FileMetadata ir : result.getEntries()) {
					for (AbstractDecoration dr : ir.getDecorations()) {
						if (dr instanceof Classification) {
							String tempDesc = dr.getDescription();
							tempDesc = StringUtils.removeStart(tempDesc, "Classification: ");
							classifications.add(tempDesc);
						}
						if (dr.getEffort() != null && dr.getEffort() instanceof StoryPointEffort) {
							estimate += ((StoryPointEffort) dr.getEffort()).getHours();
						}
					}

				}

				if (classifications.size() > 0) {
					for (String classification : classifications) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Found: " + classification);
						}
						notes.append(", ").append(classification);
					}
				}
				String notesExtracted = StringUtils.removeStart(notes.toString(), ", ");
				appendNotesRow(workbook, s, rownum, result.getRelativePath(), estimate, notesExtracted);
				rownum++;
			}
			appendTotalRow(workbook, s, rownum++);

			// empty row.
			rownum++;

			appendMentoringTitleRow(workbook, s, rownum++);
			int start = rownum + 1;
			appendNotesRow(workbook, s, rownum++, "JBoss Configuration / Documentation / Mentoring", 80, "");
			appendNotesRow(workbook, s, rownum++, "JBoss Server Setup for Apps", 80, "");
			appendNotesRow(workbook, s, rownum++, "JBoss Operations Network Setup / Documentation / Mentoring", 120, "");
			appendNotesRow(workbook, s, rownum++, "Deployment / Fail Over Plans", 80, "");
			int end = rownum;
			appendMentoringTotalRow(workbook, s, rownum++, start, end);

			// write the workbook to the output stream
			// close our file (don't blow out our file handles
			workbook.write(out);
		}
		catch (IOException e) {
			LOG.error("Exception writing scorecard to: " + output.getAbsolutePath());
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	private List<ArchiveMetadata> unwind(ArchiveMetadata result) {
		List<ArchiveMetadata> unwound = new LinkedList<ArchiveMetadata>();
		unwind(result, unwound);
		
		return unwound;
	}
	
	private void unwind(ArchiveMetadata result, List<ArchiveMetadata> unwound) {
		for(ArchiveMetadata meta : result.getNestedArchives()) {
			unwind(meta, unwound);
		}
		unwound.add(result);
	}
	
	
	private static final String TEST_PADDING = "3";

	private static void appendTotalRow(XSSFWorkbook wb, XSSFSheet sheet, int rowNum) {
		Font boldFont = wb.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldFont.setColor((short) 0x0);

		XSSFCellStyle commentCell = wb.createCellStyle();
		commentCell.setBorderTop(CellStyle.BORDER_THIN);

		XSSFCellStyle totalCell = wb.createCellStyle();
		totalCell.setBorderTop(CellStyle.BORDER_THIN);
		totalCell.setFont(boldFont);

		XSSFCellStyle totalCellRight = wb.createCellStyle();
		totalCellRight.setBorderTop(CellStyle.BORDER_THIN);
		totalCellRight.setAlignment(HorizontalAlignment.RIGHT);
		totalCellRight.setFont(boldFont);

		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell t1 = row.createCell(0);
		t1.setCellValue("Total:");
		t1.setCellStyle(totalCellRight);

		XSSFCell t2 = row.createCell(1);
		t2.setCellFormula("SUM(B1:B" + rowNum + ")*" + TEST_PADDING);
		t2.setCellStyle(totalCell);

		XSSFCell t3 = row.createCell(2);
		t3.setCellStyle(totalCell);

		XSSFCell t4 = row.createCell(3);
		t4.setCellValue("Total with Testing & App Migration Factors");
		t4.setCellStyle(commentCell);
	}

	private static void appendTitleRow(XSSFWorkbook wb, XSSFSheet sheet, int rowNum) {
		XSSFCellStyle titleCell = wb.createCellStyle();
		Color titleCellGrey = new Color(0xECECEC);
		XSSFColor color = new XSSFColor(titleCellGrey);
		titleCell.setFillForegroundColor(color);
		titleCell.setBorderBottom(CellStyle.BORDER_MEDIUM);
		titleCell.setFillPattern(CellStyle.SOLID_FOREGROUND);

		Font titleFormat = wb.createFont();
		titleFormat.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFormat.setColor((short) 0x0);
		titleCell.setFont(titleFormat);

		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell t1 = row.createCell(0);
		t1.setCellValue("Application Migration Estimate");
		t1.setCellStyle(titleCell);

		XSSFCell t2 = row.createCell(1);
		t2.setCellValue("Effort (Points)");
		t2.setCellStyle(titleCell);

		XSSFCell t3 = row.createCell(2);
		t3.setCellStyle(titleCell);

		XSSFCell t4 = row.createCell(3);
		t4.setCellValue("Notes");
		t4.setCellStyle(titleCell);
	}

	private static void appendMentoringTitleRow(XSSFWorkbook wb, XSSFSheet sheet, int rowNum) {
		XSSFCellStyle titleCell = wb.createCellStyle();
		Color titleCellGrey = new Color(0xECECEC);
		XSSFColor color = new XSSFColor(titleCellGrey);
		titleCell.setFillForegroundColor(color);
		titleCell.setBorderBottom(CellStyle.BORDER_MEDIUM);
		titleCell.setBorderTop(CellStyle.BORDER_MEDIUM);
		titleCell.setFillPattern(CellStyle.SOLID_FOREGROUND);

		Font titleFormat = wb.createFont();
		titleFormat.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFormat.setColor((short) 0x0);
		titleCell.setFont(titleFormat);

		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell t1 = row.createCell(0);
		t1.setCellValue("Migration Service Estimate");
		t1.setCellStyle(titleCell);

		XSSFCell t2 = row.createCell(1);
		t2.setCellStyle(titleCell);

		XSSFCell t3 = row.createCell(2);
		t3.setCellStyle(titleCell);

		XSSFCell t4 = row.createCell(3);
		t4.setCellStyle(titleCell);
	}

	private static void appendMentoringTotalRow(XSSFWorkbook wb, XSSFSheet sheet, int rowNum, int start, int end) {
		Font boldFont = wb.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldFont.setColor((short) 0x0);

		XSSFCellStyle commentCell = wb.createCellStyle();
		commentCell.setBorderTop(CellStyle.BORDER_THIN);

		XSSFCellStyle totalCell = wb.createCellStyle();
		totalCell.setBorderTop(CellStyle.BORDER_THIN);
		totalCell.setFont(boldFont);

		XSSFCellStyle totalCellRight = wb.createCellStyle();
		totalCellRight.setBorderTop(CellStyle.BORDER_THIN);
		totalCellRight.setAlignment(HorizontalAlignment.RIGHT);
		totalCellRight.setFont(boldFont);

		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell t1 = row.createCell(0);
		t1.setCellValue("Total:");
		t1.setCellStyle(totalCellRight);

		XSSFCell t2 = row.createCell(1);
		t2.setCellFormula("SUM(B" + start + ":B" + end + ")");
		t2.setCellStyle(totalCell);

		XSSFCell t3 = row.createCell(2);
		t3.setCellStyle(totalCell);

		XSSFCell t4 = row.createCell(3);
		t4.setCellStyle(commentCell);
	}

	private static void appendNotesRow(XSSFWorkbook wb, XSSFSheet sheet, int rowNum, String app, double effort, String notes) {
		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell t1 = row.createCell(0);
		t1.setCellValue(app);

		XSSFCell t2 = row.createCell(1);
		XSSFCellStyle t2s = wb.createCellStyle();
		t2s.setAlignment(HorizontalAlignment.RIGHT);
		t2.setCellStyle(t2s);

		t2.setCellValue(effort);
		row.createCell(2);

		XSSFCell t4 = row.createCell(3);
		t4.setCellValue(notes);
	}

	// create a new file
	public static void generateScoreCard(File output, List<ArchiveReport> results) {
	

	}

}
