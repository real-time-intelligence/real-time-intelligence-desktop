package ru.rti.desktop.view.panel.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rti.desktop.helper.FilesHelper;

import javax.inject.Named;
import java.time.LocalDateTime;

@Data
public class PathPdfInfo {
      private String dirDesignName;

    public PathPdfInfo(String dirDesignName) {
        this.dirDesignName = dirDesignName;
    }

    public String getReportPdfPath() {
        return System.getProperty("user.dir") + System.getProperty("file.separator")
                +"report"+System.getProperty("file.separator")
                +"design" +System.getProperty("file.separator")+ dirDesignName
                +System.getProperty("file.separator")+ "report_"+getDateTimeFolder()+".pdf";
    }

    public String getDateTimeFolder() {
        return dirDesignName.substring(dirDesignName.indexOf("_") + 1);
    }

}
