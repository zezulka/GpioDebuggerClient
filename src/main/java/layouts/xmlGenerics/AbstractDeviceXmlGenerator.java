/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import layouts.ClientPin;
import layouts.PinLayoutFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Miloslav Zezulka
 */
public abstract class AbstractDeviceXmlGenerator implements DeviceXmlGenerator {

    private final int height;
    private final int width;
    private final BoardType type;
    private final String deviceName;
    private final Logger xmlLogger = LoggerFactory.getLogger(AbstractDeviceXmlGenerator.class);
    public static final String EXTENSION = ".fxml";
    private static final int WIDTH_OFFSET_FOR_TEXT_AREA = 6;
    private static final String PREF_WIDTH_BUTTON = "120";
    private static final String PREF_HEIGHT_BUTTON = "25";
    private final Document DOC;

    protected AbstractDeviceXmlGenerator(int height, int width, BoardType type, String deviceName) throws InstantiationException {
        this.height = height;
        this.width = width;
        this.type = type;
        this.deviceName = deviceName;
        try {
            DOC = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new InstantiationException(ex.getMessage());
        }
    }

    private void addImports() {
        String importString = "import";
        Node[] imports = {DOC.createProcessingInstruction(importString, "javafx.scene.control.Button"),
            DOC.createProcessingInstruction(importString, "javafx.scene.layout.AnchorPane"),
            DOC.createProcessingInstruction(importString, "javafx.scene.layout.BorderPane"),
            DOC.createProcessingInstruction(importString, "javafx.scene.layout.ColumnConstraints"),
            DOC.createProcessingInstruction(importString, "javafx.scene.layout.GridPane"),
            DOC.createProcessingInstruction(importString, "javafx.scene.layout.RowConstraints"),
            DOC.createProcessingInstruction(importString, "javafx.scene.control.Label"),
            DOC.createProcessingInstruction(importString, "javafx.scene.text.Font"),
            DOC.createProcessingInstruction(importString, "javafx.scene.control.RadioButton"),
            DOC.createProcessingInstruction(importString, "javafx.scene.control.ToggleGroup"),
            DOC.createProcessingInstruction(importString, "javafx.scene.control.TextArea")
        };
        for (Node node : imports) {
            DOC.appendChild(node);
        }
    }

    private Node createButton(int row, int col) {
        Element button = DOC.createElement("Button");
        int rowOffset = 2;
        int index = row * 2 + 1 + col;
        col += col % 2 == 0 ? 0 : 1;
        ClientPin currentPin = PinLayoutFactory.getInstance(type).
                getPinFromIndex(index);
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#sendGpioRequest");
        button.setAttribute("disable", Boolean.toString(!currentPin.isGpio()));
        button.setAttribute("text", (currentPin.isGpio())
                ? currentPin.getName() : currentPin.getName());
        button.setAttribute("GridPane.columnIndex", Integer.toString(col));
        button.setAttribute("GridPane.rowIndex", Integer.toString(row + rowOffset));
        button.setAttribute("prefWidth", PREF_WIDTH_BUTTON);
        button.setAttribute("prefHeight", PREF_HEIGHT_BUTTON);
        return button;
    }

    private Node createInterfaceButton(String interfc, String row) {
        Element button = DOC.createElement("Button");
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#sendInterfaceRequest");
        button.setAttribute("text", interfc);
        button.setAttribute("GridPane.columnIndex", row);
        button.setAttribute("prefWidth", PREF_WIDTH_BUTTON);
        button.setAttribute("prefHeight", PREF_HEIGHT_BUTTON);
        return button;
    }


    @Override
    public void createXml() throws IOException {
        try {

            addImports();
            //root element
            Element rootElement = DOC.createElement("AnchorPane");
            rootElement.setAttribute("id", "AnchorPane");
            rootElement.setAttribute("xmlns", "http://javafx.com/javafx/8.0.111");
            rootElement.setAttribute("xmlns:fx", "http://javafx.com/fxml/1");
            rootElement.setAttribute("fx:controller", "layouts.controllers." + deviceName + "Controller");
            DOC.appendChild(rootElement);

            Element children = DOC.createElement("children");
            rootElement.appendChild(children);
            //grid pane
            Element gridPane = DOC.createElement("GridPane");
            gridPane.setAttribute("layoutX", "0.0");
            gridPane.setAttribute("layoutY", "0.0");
            children.appendChild(gridPane);

            //column constraints
            Element columnConstraints = DOC.createElement("columnConstraints");

            for (int i = 0; i < width + WIDTH_OFFSET_FOR_TEXT_AREA; i++) {
                Element columnConstraint = DOC.createElement("ColumnConstraints");
                columnConstraint.setAttribute("hgrow", "ALWAYS");
                columnConstraint.setAttribute("minWidth", "10.0");
                columnConstraint.setAttribute("prefWidth", "100.0");
                columnConstraints.appendChild(columnConstraint);
            }
            gridPane.appendChild(columnConstraints);

            //row constraints
            Element rowConstraints = DOC.createElement("rowConstraints");

            for (int i = 0; i < height + 1; i++) {
                Element rowConstraint = DOC.createElement("RowConstraints");
                rowConstraint.setAttribute("minHeight", "10.0");
                rowConstraint.setAttribute("prefHeight", "30.0");
                rowConstraint.setAttribute("vgrow", "ALWAYS");
                rowConstraints.appendChild(rowConstraint);
            }

            gridPane.appendChild(rowConstraints);

            //buttons for GPIO 
            Element gridPaneChildren = DOC.createElement("children");
            for (int col = 0; col < width; col++) {
                for (int row = 0; row < height; row++) {
                    gridPaneChildren.appendChild(this.createButton(row, col));
                }
            }
            //buttons for interfaces
            String[] interfaces = new String[]{"i2c", "spi"};
            for (int col = 0; col < interfaces.length; col++) {
                gridPaneChildren.appendChild(createInterfaceButton(interfaces[col],
                        String.valueOf(col)));
            }
            gridPaneChildren.appendChild(createOutputArea());
            gridPane.appendChild(gridPaneChildren);

            gridPaneChildren.appendChild(createWriteRadioButton());
            gridPaneChildren.appendChild(createReadRadioButton());

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOC.normalizeDocument();
            DOMSource source = new DOMSource(DOC);
            StreamResult result = new StreamResult(new File("./src/main/resources/fxml/" + deviceName + EXTENSION));

            //transformer formatting magic... taken from StackOverflow
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (TransformerException tfe) {
            xmlLogger.error("transformer error:", tfe);
        }
    }

    private Node createOutputArea() {
        Element el = DOC.createElement("TextArea");
        el.setAttribute("editable", "false");
        el.setAttribute("wrapText", "true");
        el.setAttribute("fx:id", "feedbackArea");
        el.setAttribute("GridPane.columnSpan", Integer.toString(Integer.MAX_VALUE));
        el.setAttribute("GridPane.rowSpan", Integer.toString(Integer.MAX_VALUE));
        el.setAttribute("GridPane.rowIndex", Integer.toString(0));
        el.setAttribute("GridPane.columnIndex", Integer.toString(width + 1));
        el.setAttribute("GridPane.rowIndex", "1");
        return el;
    }

    private Node createWriteRadioButton() {
        Element rButton1 = DOC.createElement("RadioButton");
        rButton1.setAttribute("ellipsisString", "W");
        rButton1.setAttribute("mnemonicParsing", "false");
        rButton1.setAttribute("text", "WRITE");
        rButton1.setAttribute("GridPane.columnIndex", "3");
        rButton1.setAttribute("fx:id", "writeRadioButton");
        Element padding = DOC.createElement("toggleGroup");
        Element inset = DOC.createElement("ToggleGroup");
        inset.setAttribute("fx:id", "op");
        padding.appendChild(inset);
        rButton1.appendChild(padding);
        return rButton1;
    }

    private Node createReadRadioButton() {
        Element rButton2 = DOC.createElement("RadioButton");
        rButton2.setAttribute("ellipsisString", "R");
        rButton2.setAttribute("mnemonicParsing", "false");
        rButton2.setAttribute("text", "READ");
        rButton2.setAttribute("GridPane.columnIndex", "4");
        rButton2.setAttribute("selected", "true");
        rButton2.setAttribute("fx:id", "readRadioButton");
        rButton2.setAttribute("toggleGroup", "$op");
        return rButton2;
    }
}
