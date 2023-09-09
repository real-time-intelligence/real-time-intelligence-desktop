package ru.rti.desktop.view.structure.action;

import lombok.Data;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Data
public class ActionsContainer {
  private Map<String, KeyStroke> keyStrokeMap = new HashMap<>();
}
