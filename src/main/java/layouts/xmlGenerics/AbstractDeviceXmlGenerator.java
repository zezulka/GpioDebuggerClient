/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import protocol.ClientPin;
import layouts.PinLayoutFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.BoardType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Convenient class for dealing with fxml files generation. Extending classes
 * only need to supply specific parameters (such as number of rows / columns of
 * pins), this abstract class takes care of everything else. Gist of generation
 * : DOM tree representing resulting fxml is progressively built and then
 * written to file specified by extending class.
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
    private static final String PREF_WIDTH_BUTTON = "75";
    private static final String PREF_HEIGHT_BUTTON = "25";
    private static Document DOC;

    protected AbstractDeviceXmlGenerator(int height, int width, BoardType type, String deviceName) {
        this.height = height;
        this.width = width;
        this.type = type;
        this.deviceName = deviceName;
        try {
            DOC = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            System.exit(1);
        }
    }

    @Override
    public void createFxml() throws IOException {
        try {
            addImports();
            createInMemoryTreeStructure();
            createXmlFileFromTreeStructure();
        } catch (TransformerException tfe) {
            xmlLogger.error("transformer error:", tfe);
        }
    }

    private void addImports() {
        String importString = "import";
        List<String> importPaths = new ArrayList<>(Arrays.asList(
                "javafx.geometry.*",
                "javafx.scene.web.*",
                "javafx.scene.image.*",
                "java.lang.*",
                "javafx.scene.control.*",
                "javafx.scene.layout.*",
                "javafx.scene.text.*"
        ));
        importPaths.forEach(t -> DOC.appendChild(DOC.createProcessingInstruction(importString, t)));
    }

    private Node createRootTabElement() {
        Element rootElement = DOC.createElement("Tab");
        //caused bugs, variable name should have first letter lowercase (opposed to supplied deviceName variable)
        rootElement.setAttribute("id", deviceName.toLowerCase() + "Tab");
        rootElement.setAttribute("fx:id", deviceName.toLowerCase() + "Tab");
        rootElement.setAttribute("xmlns", "http://javafx.com/javafx/8.0.111");
        rootElement.setAttribute("xmlns:fx", "http://javafx.com/fxml/1");
        rootElement.setAttribute("fx:controller", "layouts.controllers." + deviceName + "Controller");
        return rootElement;
    }

    private void createInMemoryTreeStructure() {
        Node result = createRootTabElement();
        Node rootWrapper = createRootElementWrapper();
        result.appendChild(rootWrapper);
        DOC.appendChild(result);
    }

    private Node createRootElementWrapper() {
        Element content = DOC.createElement("content");
        Element tabPane = DOC.createElement("TabPane");
        tabPane.setAttribute("side", "RIGHT");
        tabPane.appendChild(createAllSubtabs());
        content.appendChild(tabPane);
        return content;
    }

    private Node createAllSubtabs() {
        Element tabs = DOC.createElement("tabs");
        tabs.appendChild(createGpioTab());
        tabs.appendChild(importSpiTab());
        tabs.appendChild(importI2cTab());
        tabs.appendChild(importInterruptTab());
        return tabs;
    }

    private Node createGpioTab() {
        Element tab = DOC.createElement("Tab");
        tab.setAttribute("closable", "false");
        tab.setAttribute("text", "GPIO");
        tab.appendChild(createGpioContents());
        return tab;
    }
    
    private Node createGpioContents() {
        Element contents = DOC.createElement("content");
        Element gridPane = DOC.createElement("GridPane");
        //cacheHint="SPEED" layoutX="0.0" layoutY="0.0" prefHeight="500.0" prefWidth="700.0"
        gridPane.setAttribute("cacheHint", "SPEED");
        gridPane.setAttribute("layoutX", "0.0");
        gridPane.setAttribute("layoutY", "0.0");
        gridPane.setAttribute("prefHeight", "500.0");
        gridPane.setAttribute("prefWidth", "700.0");
        gridPane.appendChild(createGpioGridPaneColumnConstraints());
        gridPane.appendChild(createGpioGridPaneRowConstraints());
        gridPane.appendChild(createGpioGridPaneChildren());
        contents.appendChild(gridPane);
        return contents;
    }
    
    private Node createGpioGridPaneRowConstraints() {
        Element rowConstraints = DOC.createElement("rowConstraints");

        for (int i = 0; i < height + 1; i++) {
            Element rowConstraint = DOC.createElement("RowConstraints");
            rowConstraint.setAttribute("minHeight", "26.0");
            rowConstraint.setAttribute("prefHeight", "26.0");
            rowConstraint.setAttribute("vgrow", "ALWAYS");
            rowConstraints.appendChild(rowConstraint);
        }
        return rowConstraints;
    }
    
    private Node createGpioGridPaneColumnConstraints() {
        Element columnConstraints = DOC.createElement("columnConstraints");
        columnConstraints.appendChild(getColumnConstraint("85.0", null, null));
        columnConstraints.appendChild(getColumnConstraint("90.0", null, null));
        columnConstraints.appendChild(getColumnConstraint("396.0", "RIGHT", null));
        columnConstraints.appendChild(getColumnConstraint("153.0", "RIGHT", null));
        columnConstraints.appendChild(getColumnConstraint("168.0", "LEFT", "ALWAYS"));
        return columnConstraints;
    }
    
    private Node getColumnConstraint(String prefWidth, String halignment, String hgrow) {
         Element result = DOC.createElement("ColumnConstraints");
         result.setAttribute("prefWidth", prefWidth);
         if(halignment != null) {
              result.setAttribute("halignment", halignment);
         }
         if(hgrow != null) {
              result.setAttribute("hgrow", hgrow);
         }
         return result;
    }
    
    private Node createGpioGridPaneChildren() {
        Element children = DOC.createElement("children");
        for(int row = 0; row < height; row++) {
            for(int col = 0; col < width; col++) {
                children.appendChild(createGpioButton(row, col));
            }
        }
        children.appendChild(createReadRadioButton());
        children.appendChild(createWriteRadioButton());
        children.appendChild(createImageView());
        return children;
    }
    
    private Node createGpioButton(int row, int col) {
        Element button = DOC.createElement("Button");
        int rowOffset = 3;
        int columnOffset = 2;
        int index = row * 2 + 1 + col;
        ClientPin currentPin = PinLayoutFactory.getInstance(type).
                getPinFromIndex(index);
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#mouseClickedHandler");
        button.setAttribute("onKeyPressed", "#keyPressedHandler");
        button.setAttribute("disable", Boolean.toString(!currentPin.isGpio()));
        button.setAttribute("text", (currentPin.isGpio())
                ? currentPin.getName() : currentPin.getName());
        button.setAttribute("GridPane.columnIndex", Integer.toString(col + columnOffset));
        button.setAttribute("GridPane.rowIndex", Integer.toString(row + rowOffset));
        button.setAttribute("prefWidth", PREF_WIDTH_BUTTON);
        button.setAttribute("prefHeight", PREF_HEIGHT_BUTTON);
        return button;
    }

    private Node createWriteRadioButton() {
        Element rButton1 = DOC.createElement("RadioButton");
        rButton1.setAttribute("ellipsisString", "W");
        rButton1.setAttribute("mnemonicParsing", "false");
        rButton1.setAttribute("text", "WRITE");
        rButton1.setAttribute("GridPane.rowIndex", "4");
        rButton1.setAttribute("GridPane.columnIndex", "1");
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
        rButton2.setAttribute("GridPane.rowIndex", "3");
        rButton2.setAttribute("GridPane.columnIndex", "1");
        rButton2.setAttribute("selected", "true");
        rButton2.setAttribute("fx:id", "readRadioButton");
        rButton2.setAttribute("toggleGroup", "$op");
        return rButton2;
    }
    
    private Node createImageView() {
        Element imageView = DOC.createElement("ImageView");
        imageView.setAttribute("disable", "true");
        imageView.setAttribute("fitHeight", "613.0");
        imageView.setAttribute("fitWidth", "737.0");
        imageView.setAttribute("GridPane.columnSpan", "999");
        imageView.setAttribute("GridPane.halignment", "LEFT");
        imageView.setAttribute("GridPane.hgrow", "ALWAYS");
        imageView.setAttribute("GridPane.vgrow", "ALWAYS");
        imageView.setAttribute("GridPane.rowSpan", "999");
        imageView.setAttribute("GridPane.valignment", "BASELINE");
        Element imagePath = DOC.createElement("Image");
        imagePath.setAttribute("url", "@../" + deviceName + ".png");
        Element image = DOC.createElement("image");
        image.appendChild(imagePath);
        imageView.appendChild(image);
        Element gridPaneMargin = DOC.createElement("GridPane.margin");
        Element insets = DOC.createElement("Insets");
        insets.setAttribute("top", "26.0");
        gridPaneMargin.appendChild(insets);
        imageView.appendChild(gridPaneMargin);
        return imageView;
    }
    
    private Node importI2cTab() {
        return importTab("I2cRequestForm");
    }

    private Node importSpiTab() {
        return importTab("SpiRequestForm");
    }

    private Node importInterruptTab() {
        return  importTab("InterruptTable");
    }
    
    private Node importTab(String sourceWithoutExtension) {
        Element importEl = DOC.createElement("fx:include");
        importEl.setAttribute("source", sourceWithoutExtension + EXTENSION);
        return importEl;
    }

    private void createXmlFileFromTreeStructure() throws TransformerException {
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
    }
}
