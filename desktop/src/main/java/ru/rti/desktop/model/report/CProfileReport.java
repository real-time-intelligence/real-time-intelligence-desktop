package ru.rti.desktop.model.report;

import lombok.*;
import org.fbase.model.profile.CProfile;
@EqualsAndHashCode(callSuper = true)
@Data
public class CProfileReport extends CProfile {

    private String comment;
}
