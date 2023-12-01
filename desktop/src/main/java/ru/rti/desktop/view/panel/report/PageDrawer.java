package ru.rti.desktop.view.panel.report;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.awt.*;
import java.io.IOException;

public class PageDrawer extends org.apache.pdfbox.rendering.PageDrawer {

	PageDrawer(PageDrawerParameters parameters) throws IOException {
		super(parameters);
	}

	/**
	 * Color replacement.
	 */
	@Override
	protected Paint getPaint(PDColor color) throws IOException {
		return super.getPaint(color);
	}

	/**
	 * Filled path bounding boxes.
	 */
	@Override
	public void fillPath(int windingRule) throws IOException {
		super.fillPath(windingRule);
	}

	/**
	 * Custom annotation rendering.
	 */
	@Override
	public void showAnnotation(PDAnnotation annotation) throws IOException {
		super.showAnnotation(annotation);
	}
}
