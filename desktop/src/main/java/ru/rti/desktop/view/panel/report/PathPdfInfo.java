package ru.rti.desktop.view.panel.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PathPdfInfo {
    private String dirDesignName;
    private String reportPdfPath;
    public String getDateTimeFolder(){
        return dirDesignName.substring(dirDesignName.indexOf("_") + 1);
    }

}
