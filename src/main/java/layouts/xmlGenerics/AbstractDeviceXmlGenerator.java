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
import misc.StringConstants;
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

    private final Logger xmlLogger
            = LoggerFactory.getLogger(AbstractDeviceXmlGenerator.class);

    private final int height;
    private final int width;
    private final BoardType type;
    private final String deviceName;

    public static final String FXML_EXT = ".fxml";
    private static final String PREF_WIDTH_BUTTON = "75";
    private static final String PREF_HEIGHT_BUTTON = "25";
    private static Document doc;

    protected AbstractDeviceXmlGenerator(int height, int width, BoardType type,
            String deviceName) {
        this.height = height;
        this.width = width;
        this.type = type;
        this.deviceName = deviceName;
        try {
            doc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException ex) {
            System.exit(1);
        }
    }

    @Override
    public final void createFxml() throws IOException {
        try {
            addImports();
            createInMemoryTreeStructure();
            createXmlFileFromTreeStructure();
        } catch (TransformerException tfe) {
            xmlLogger.error("transformer error:", tfe);
        }
    }

    private void addImports() {
        String importStr = "import";
        List<String> importPaths = new ArrayList<>(Arrays.asList(
                "javafx.geometry.*",
                "javafx.scene.web.*",
                "javafx.scene.image.*",
                "java.lang.*",
                "javafx.scene.control.*",
                "javafx.scene.layout.*",
                "javafx.scene.text.*"
        ));
        importPaths.forEach((t) -> {
            doc.appendChild(doc.createProcessingInstruction(importStr, t));
        });
    }

    private Node createRootTabElement() {
        Element rootElement = doc.createElement("Tab");
        //caused bugs, variable name should have first letter lowercase
        //(opposed to supplied deviceName variable)
        rootElement.setAttribute("id", deviceName.toLowerCase() + "Tab");
        rootElement.setAttribute("fx:id", deviceName.toLowerCase() + "Tab");
        rootElement.setAttribute("xmlns", "http://javafx.com/javafx/8.0.111");
        rootElement.setAttribute("xmlns:fx", "http://javafx.com/fxml/1");

        rootElement.setAttribute("fx:controller", getPathToController());
        return rootElement;
    }

    private String getPathToController() {
        return "layouts.controllers." + deviceName + "Controller";
    }

    private void createInMemoryTreeStructure() {
        Node result = createRootTabElement();
        Node rootWrapper = createRootElementWrapper();
        result.appendChild(rootWrapper);
        doc.appendChild(result);
    }

    private Node createRootElementWrapper() {
        Element content = doc.createElement("content");
        Element tabPane = doc.createElement("TabPane");
        tabPane.setAttribute("side", "RIGHT");
        tabPane.appendChild(createAllSubtabs());
        content.appendChild(tabPane);
        return content;
    }

    private Node createAllSubtabs() {
        Element tabs = doc.createElement("tabs");
        tabs.appendChild(createGpioTab());
        tabs.appendChild(importSpiTab());
        tabs.appendChild(importI2cTab());
        tabs.appendChild(importInterruptTab());
        return tabs;
    }

    private Node createGpioTab() {
        Element tab = doc.createElement("Tab");
        tab.setAttribute("closable", "false");
        tab.setAttribute("text", "GPIO");
        tab.appendChild(createGpioContents());
        return tab;
    }

    private Node createGpioContents() {
        Element contents = doc.createElement("content");
        Element gridPane = doc.createElement("GridPane");
        gridPane.setAttribute("fx:id", "gpioGridPane");
        gridPane.setAttribute("cacheHint", "SPEED");
        gridPane.setAttribute("layoutX", "0.0");
        gridPane.setAttribute("layoutY", "0.0");
        gridPane.appendChild(createGpioGridPaneColumnConstraints());
        gridPane.appendChild(createGpioGridPaneRowConstraints());
        gridPane.appendChild(createGpioGridPaneChildren());
        contents.appendChild(gridPane);
        return contents;
    }

    private Node createGpioGridPaneRowConstraints() {
        Element rowConstraints = doc.createElement("rowConstraints");

        for (int i = 0; i < height + 1; i++) {
            Element rowConstraint = doc.createElement("RowConstraints");
            rowConstraint.setAttribute("minHeight", "30.0");
            rowConstraint.setAttribute("prefHeight", "30.0");
            rowConstraints.appendChild(rowConstraint);
        }
        return rowConstraints;
    }

    private Node createGpioGridPaneColumnConstraints() {
        final double firstCol = 280.0;
        final double secondCol = 100.0;
        final double thirdCol = 120.0;
        final double fourthCol = thirdCol;
        Element columnConstraints = doc.createElement("columnConstraints");
        columnConstraints.appendChild(getColumnConstraint(firstCol, "CENTER"));
        columnConstraints.appendChild(getColumnConstraint(secondCol, "LEFT"));
        columnConstraints.appendChild(getColumnConstraint(thirdCol, "LEFT"));
        columnConstraints.appendChild(getColumnConstraint(fourthCol, "LEFT"));
        return columnConstraints;
    }

    private Node getColumnConstraint(double prefWidth, String halignment) {
        Element result = doc.createElement("ColumnConstraints");
        result.setAttribute("prefWidth", Double.toString(prefWidth));
        if (halignment != null) {
            result.setAttribute("halignment", halignment);
        }
        return result;
    }

    private Node createGpioGridPaneChildren() {
        Element children = doc.createElement("children");
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                children.appendChild(createGpioButton(row, col));
            }
        }
        children.appendChild(createWriteRadioButton());
        children.appendChild(createReadRadioButton());
        createLegend(children);
        children.appendChild(createImageView());
        return children;
    }

    private void createLegend(Element root) {
        root.appendChild(createLegendLabel("Legend:", "1", "#000000"));
        root.appendChild(createLegendLabel("HIGH", "2", "#55FF55"));
        root.appendChild(createLegendLabel("LOW", "3", "#FF5555"));
    }

    private Node createLegendLabel(String text, String rowIndex,
            String textfill) {

        Element labelEl = doc.createElement("Label");
        labelEl.setAttribute("text", text);
        labelEl.setAttribute("textFill", textfill);
        labelEl.setAttribute("GridPane.rowIndex", rowIndex);
        labelEl.setAttribute("GridPane.columnIndex", "3");
        Element fontChildEl = doc.createElement("Font");
        fontChildEl.setAttribute("name", "SystemBold");
        fontChildEl.setAttribute("size", "15.0");
        Element fontEl = doc.createElement("font");
        fontEl.appendChild(fontChildEl);
        labelEl.appendChild(fontEl);
        return labelEl;
    }

    private Node createGpioButton(int row, int col) {
        Element button = doc.createElement("Button");
        int rowOffset = 1;
        int columnOffset = 1;
        int index = row * 2 + 1 + col;
        ClientPin currentPin = PinLayoutFactory.getInstance(type).
                getPinFromIndex(index);
        button.setAttribute("mnemonicParsing", "false");
        button.setAttribute("onMouseClicked", "#mouseClickedHandler");
        button.setAttribute("onKeyPressed", "#keyPressedHandler");
        button.setAttribute("disable", Boolean.toString(!currentPin.isGpio()));
        button.setAttribute("text", currentPin.toString());
        button.setAttribute("id",
                currentPin.isGpio() ? currentPin.getPinId() : "nonselectable");
        button.setAttribute("GridPane.columnIndex",
                Integer.toString(col + columnOffset));
        button.setAttribute("GridPane.rowIndex",
                Integer.toString(row + rowOffset));
        button.setAttribute("prefWidth", PREF_WIDTH_BUTTON);
        button.setAttribute("prefHeight", PREF_HEIGHT_BUTTON);
        return button;
    }

    private Node createWriteRadioButton() {
        Element rButton1 = doc.createElement("RadioButton");
        rButton1.setAttribute("ellipsisString", "W");
        rButton1.setAttribute("mnemonicParsing", "false");
        rButton1.setAttribute("text", "WRITE");
        rButton1.setAttribute("GridPane.rowIndex", "0");
        rButton1.setAttribute("fx:id", "writeRadioButton");
        Element padding = doc.createElement("toggleGroup");
        Element inset = doc.createElement("ToggleGroup");
        inset.setAttribute("fx:id", "op");
        padding.appendChild(inset);
        rButton1.appendChild(padding);
        return rButton1;
    }

    private Node createReadRadioButton() {
        Element rButton2 = doc.createElement("RadioButton");
        rButton2.setAttribute("ellipsisString", "R");
        rButton2.setAttribute("mnemonicParsing", "false");
        rButton2.setAttribute("text", "READ");
        rButton2.setAttribute("GridPane.rowIndex", "1");
        rButton2.setAttribute("selected", "true");
        rButton2.setAttribute("fx:id", "readRadioButton");
        rButton2.setAttribute("toggleGroup", "$op");
        return rButton2;
    }

    private Node createImageView() {
        Element stackPane = doc.createElement("StackPane");
        stackPane.setAttribute("fx:id", "gpioStackPane");
        Element imageView = doc.createElement("ImageView");
        imageView.setAttribute("disable", "true");
        imageView.setAttribute("fitHeight", "665x.0");
        imageView.setAttribute("fitWidth", "730.0");
        imageView.setAttribute("GridPane.columnSpan", "999");
        imageView.setAttribute("GridPane.halignment", "LEFT");
        imageView.setAttribute("GridPane.hgrow", "ALWAYS");
        imageView.setAttribute("GridPane.vgrow", "ALWAYS");
        imageView.setAttribute("GridPane.rowSpan", "999");
        imageView.setAttribute("GridPane.valignment", "BASELINE");
        Element imagePath = doc.createElement("Image");
        imagePath.setAttribute("url", "@../" + deviceName + ".png");
        Element image = doc.createElement("image");
        Element children = doc.createElement("children");
        image.appendChild(imagePath);
        imageView.appendChild(image);
        children.appendChild(imageView);
        stackPane.appendChild(children);
        return imageView;
    }

    private Node importI2cTab() {
        return importTab("I2cRequestForm");
    }

    private Node importSpiTab() {
        return importTab("SpiRequestForm");
    }

    private Node importInterruptTab() {
        return importTab("InterruptTable");
    }

    private Node importTab(String sourceWithoutExtension) {
        Element importEl = doc.createElement("fx:include");
        importEl.setAttribute("source", sourceWithoutExtension + FXML_EXT);
        return importEl;
    }

    private void createXmlFileFromTreeStructure() throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        doc.normalizeDocument();
        DOMSource source = new DOMSource(doc);
        String pathToDevice =
                StringConstants.PATH_TO_FXML_DIR + deviceName + FXML_EXT;
        StreamResult result = new StreamResult(new File(pathToDevice));

        //transformer formatting magic... taken from StackOverflow
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer
                .setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                        "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }
}
