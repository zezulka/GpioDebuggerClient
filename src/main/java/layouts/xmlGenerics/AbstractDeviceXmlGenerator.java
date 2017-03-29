/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package layouts.xmlGenerics;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
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
 * @author miloslav
 */
public abstract class AbstractDeviceXmlGenerator implements DeviceXmlGenerator {

    private final int height;
    private final int width;
    private final BoardType type;
    private final String deviceName;
    private final Logger xmlLogger = LoggerFactory.getLogger(AbstractDeviceXmlGenerator.class);
    public static final String EXTENSION = ".fxml";

    protected AbstractDeviceXmlGenerator(int height, int width, BoardType type, String deviceName) {
        this.height = height;
        this.width = width;
        this.type = type;
        this.deviceName = deviceName;
    }

    private static void addImports(Document doc) {
        Node[] imports = {doc.createProcessingInstruction("import", "javafx.scene.control.Button"),
            doc.createProcessingInstruction("import", "javafx.scene.layout.AnchorPane"),
            doc.createProcessingInstruction("import", "javafx.scene.layout.BorderPane"),
            doc.createProcessingInstruction("import", "javafx.scene.layout.ColumnConstraints"),
            doc.createProcessingInstruction("import", "javafx.scene.layout.GridPane"),
            doc.createProcessingInstruction("import", "javafx.scene.layout.RowConstraints"),
            doc.createProcessingInstruction("import", "javafx.scene.control.Label"),
            doc.createProcessingInstruction("import", "javafx.scene.text.Font"),
            doc.createProcessingInstruction("import", "javafx.scene.control.RadioButton"),
            doc.createProcessingInstruction("import", "javafx.scene.control.ToggleGroup")
        };
        for (Node node : imports) {
            doc.appendChild(node);
        }
    }

    private Node createButton(Document doc, int row, int col) {
        Element button = doc.createElement("Button");
        int index = row * 2 + 1 + col;
        ClientPin currentPin = PinLayoutFactory.getInstance(type).
                getPinFromIndex(index);
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#sendGpioRequest");
        button.setAttribute("disable", Boolean.toString(!currentPin.isGpio()));
        button.setAttribute("text", (currentPin.isGpio())
                ? currentPin.getName() : currentPin.getName());
        button.setAttribute("GridPane.columnIndex", Integer.toString(col + 1));
        button.setAttribute("GridPane.rowIndex", Integer.toString(row + 1));
        return button;
    }

    private Node createInterfaceButton(Document doc, String interfc, int col) {
        Element button = doc.createElement("Button");
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#sendInterfaceRequest");
        button.setAttribute("text", interfc);
        button.setAttribute("GridPane.rowIndex", Integer.toString(col));
        return button;
    }

    private Node createToggleGroup(Document doc, String groupName) {
        Element toggleGroup = doc.createElement("toggleGroup");
        Element toggleGroupInner = doc.createElement("ToggleGroup");
        toggleGroupInner.setAttribute("fx:id", groupName);
        toggleGroup.appendChild(toggleGroupInner);
        return toggleGroup;
    }

    @Override
    public void createXml() throws IOException {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            addImports(doc);
            //root element
            Element rootElement = doc.createElement("AnchorPane");
            rootElement.setAttribute("id", "AnchorPane");
            rootElement.setAttribute("xmlns", "http://javafx.com/javafx/8.0.111");
            rootElement.setAttribute("xmlns:fx", "http://javafx.com/fxml/1");
            rootElement.setAttribute("fx:controller", "layouts.controllers." + deviceName + "Controller");
            doc.appendChild(rootElement);

            Element children = doc.createElement("children");
            rootElement.appendChild(children);
            //grid pane
            Element gridPane = doc.createElement("GridPane");
            gridPane.setAttribute("layoutX", "0.0");
            gridPane.setAttribute("layoutY", "0.0");
            children.appendChild(gridPane);

            //column constraints
            Element columnConstraints = doc.createElement("columnConstraints");

            for (int i = 0; i < width; i++) {
                Element columnConstraint = doc.createElement("ColumnConstraints");
                columnConstraint.setAttribute("hgrow", "SOMETIMES");
                columnConstraint.setAttribute("minWidth", "10.0");
                columnConstraint.setAttribute("prefWidth", "100.0");
                columnConstraints.appendChild(columnConstraint);
            }
            gridPane.appendChild(columnConstraints);

            //row constraints
            Element rowConstraints = doc.createElement("rowConstraints");

            for (int i = 0; i < height; i++) {
                Element rowConstraint = doc.createElement("RowConstraints");
                rowConstraint.setAttribute("minHeight", "10.0");
                rowConstraint.setAttribute("prefHeight", "30.0");
                rowConstraint.setAttribute("vgrow", "SOMETIMES");
                rowConstraints.appendChild(rowConstraint);
            }

            gridPane.appendChild(rowConstraints);

            //buttons
            Element buttons = doc.createElement("children");
            for (int col = 0; col < width; col++) {
                for (int row = 0; row < height; row++) {
                    buttons.appendChild(this.createButton(doc, row, col));
                }
            }

            String[] interfaces = new String[] {"i2c", "spi", "uart"};
            for(int i = 0; i < interfaces.length; i++) {
                buttons.appendChild(createInterfaceButton(doc, interfaces[i], 
                        this.height - interfaces.length + i));
            }
            gridPane.appendChild(buttons);
            String radioButtonGroup = "operation";
            buttons.appendChild(createStatusBar(doc));
            Element rButton1 = doc.createElement("RadioButton");
            Element rButton2 = doc.createElement("RadioButton");
            rButton1.setAttribute("ellipsisString", "W");
            rButton1.setAttribute("mnemonicParsing", "false");
            rButton1.setAttribute("text", "WRITE");
            rButton1.setAttribute("GridPane.rowIndex", "3");
            rButton1.setAttribute("fx:id", "writeRadioButton");
            rButton1.appendChild(createToggleGroup(doc, radioButtonGroup));

            rButton2.setAttribute("ellipsisString", "R");
            rButton2.setAttribute("mnemonicParsing", "false");
            rButton2.setAttribute("toggleGroup", "$ops");
            rButton2.setAttribute("text", "READ");
            rButton2.setAttribute("GridPane.rowIndex", "2");
            rButton2.setAttribute("selected", "true");
            rButton2.setAttribute("fx:id", "readRadioButton");
            rButton2.setAttribute("toggleGroup", '$' + radioButtonGroup);
            buttons.appendChild(rButton1);
            buttons.appendChild(rButton2);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            doc.normalizeDocument();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("./src/main/resources/fxml/" + deviceName + EXTENSION));

            //transformer formatting magic... taken from StackOverflow
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            xmlLogger.error("parsing error:", pce);
        } catch (TransformerException tfe) {
            xmlLogger.error("transformer error:", tfe);
        }
    }

    private Node createStatusBar(Document doc) {
        Element el = doc.createElement("BorderPane");
        Element left = doc.createElement("left");
        Element label = doc.createElement("Label");
        label.setAttribute("fx:id", "statusBar");
        label.setAttribute("alignment", "BOTTOM_LEFT");
        label.setAttribute("contentDisplay", "TEXT_ONLY");
        label.setAttribute("text", "OK");
        label.setAttribute("textAlignment", "LEFT");
        label.setAttribute("BorderPane.alignment", "CENTER");
        Element font = doc.createElement("font");
        Element fontInner = doc.createElement("Font");
        fontInner.setAttribute("size", "18.0");
        font.appendChild(fontInner);
        label.appendChild(font);
        left.appendChild(label);
        el.appendChild(left);
        return el;
    }
}
