package ru.rti.desktop.view.handler.report.report;

import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jdesktop.swingx.VerticalLayout;
import ru.rti.desktop.helper.FilesHelper;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.view.panel.report.PathPdfInfo;
import ru.rti.desktop.view.panel.report.ReportTabsPane;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Log4j2
@Singleton
public class ReportPanelHandler  implements ActionListener, ListSelectionListener {

    private final ReportTabsPane reportTabsPane;
    private final JFileChooser saveFC;
    private JFrame jFrame;
    private final PathPdfInfo reportPdfPath;
    private final FilesHelper filesHelper;
    private final ReportHelper reportHelper;


    @Inject
    public ReportPanelHandler(@Named("reportTaskPanel") ReportTabsPane reportTabsPane,
                              @Named("reportPdfPath") PathPdfInfo reportPdfPath,
                              FilesHelper filesHelper,
                              ReportHelper reportHelper
    ) {
        this.reportTabsPane = reportTabsPane;
        this.reportPdfPath = reportPdfPath;

        this.filesHelper = filesHelper;
        this.reportHelper = reportHelper;

        this.saveFC = new JFileChooser();
        this.saveFC.setDialogTitle("Сохранить PDF файл");
        this.saveFC.setFileFilter(new FileNameExtensionFilter("PDF файлы", "pdf"));

        this.reportTabsPane.getSaveBtnPDFReport().addActionListener(this);
        this.reportTabsPane.getSavedReportCase().getJxTable().getSelectionModel().addListSelectionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == reportTabsPane.getSaveBtnPDFReport()) {
            int userSelection = saveFC.showSaveDialog(jFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {

                String savePath = saveFC.getSelectedFile().getAbsolutePath();

                try {
                    Path sourcePath = Paths.get(reportPdfPath.getReportPdfPath());
                    Path targetPath = Paths.get(savePath);

                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    JOptionPane.showMessageDialog(null,
                            "PDF file successfully saved to the selected path: " + savePath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void viewFilePdf(String fileName) {
        JPanel jPanelPDF = new JPanel(new VerticalLayout());

        try {
            PDDocument document = PDDocument.load(new File(fileName));
            PDFRenderer renderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 150);
                ImageIcon imageIcon = new ImageIcon(image);

                JLabel labelImage = new JLabel();
                labelImage.setIcon(imageIcon);
                jPanelPDF.add(labelImage);

                JLabel emptyLabel = new JLabel(String.valueOf(pageIndex + 1));
                emptyLabel.setSize(new Dimension(25, 25));

                jPanelPDF.add(emptyLabel);

            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reportTabsPane.getScrollPanePDF().setViewportView(jPanelPDF);
        reportPdfPath.setReportPdfPath(fileName);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");
            } else {

                if (e.getSource() == reportTabsPane.getSavedReportCase().getJxTable().getSelectionModel()) {
                    String designDir = GUIHelper.getNameByColumnName( reportTabsPane.getSavedReportCase().getJxTable(),
                            reportTabsPane.getSavedReportCase().getDefaultTableModel(),
                            listSelectionModel, ProfileColumnNames.NAME.getColName());
                    String dateStr = designDir.substring(designDir.indexOf("-") + 1).trim();
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr, reportHelper.getDateTimeFormatter());
                    String folderDate = dateTime.format(reportHelper.getDateTimeFormatterFused());
                    String folderName = "design_" +folderDate;

                    File designFolder = new File(filesHelper.getDesignDir() + filesHelper.getFileSeparator() + folderName);
                    if (designFolder.exists() && designFolder.isDirectory()) {
                        File[] files = designFolder.listFiles();
                        if (files != null) {
                            for (File file : files) {

                                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                                    String fileRepoPath = filesHelper.getDesignDir()
                                            + filesHelper.getFileSeparator() + folderName
                                            + filesHelper.getFileSeparator() + file.getName();
                                    viewFilePdf(fileRepoPath);
                                    reportTabsPane.getSaveBtnPDFReport().setEnabled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
