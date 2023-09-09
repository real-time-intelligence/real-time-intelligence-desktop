package ru.rti.desktop.model.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

@Data
@AllArgsConstructor
public class JXTableCase {
    private JXTable jxTable;
    private DefaultTableModel defaultTableModel;
    private JScrollPane jScrollPane;
}
