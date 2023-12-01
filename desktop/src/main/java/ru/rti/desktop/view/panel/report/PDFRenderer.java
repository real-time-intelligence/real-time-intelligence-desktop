package ru.rti.desktop.view.panel.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.io.IOException;

public class PDFRenderer extends org.apache.pdfbox.rendering.PDFRenderer {
	PDFRenderer(PDDocument document) {
		super(document);
	}

	@Override
	protected org.apache.pdfbox.rendering.PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException {
		return new PageDrawer(parameters);
	}
}