package ru.rti.desktop.prompt;

import java.awt.*;
import java.util.ListResourceBundle;

public class Resource extends ListResourceBundle {
    private static final Object[][]
            prtText =
            {
                    {"pName", "Profile name"},
                    {"pDesc", "Profile description"},
                    {"tName", "Task name"},
                    {"tDesc", "Task description"},
                    {"cName", "Connection name"},
                    {"cURL", "Connection URL"},
                    {"cUserName", "User name"},
                    {"cPass", "Password"},
                    {"cJar", "Jar-file path"},
                    {"cDriver","Driver"},
                    {"qName", "Query name"},
                    {"qDesc","Query description"},
                    {"qSqlText","SQL text"},
                    {"metaName", "Query name"},
                    {"loadMeta","Load metadata from database"},
                    {"metricName","Metric name"},
                    {"metricDef","Default"},
                    {"xAxis", "X axis value"},
                    {"yAxis", "Y axis value"},

                    {"btnNew", "New"},
                    {"btnCopy", "Copy"},
                    {"btnDel", "Delete"},
                    {"btnEdit", "Edit"},
                    {"btnSave", "Save"},
                    {"btnCancel", "Cancel"},

                    {"colorBlue", new Color(100, 185, 250)},
                    {"colorRed", new Color(218, 43, 43)}

            };

    @Override
    protected Object[][] getContents() {
        return prtText;
    }
}
