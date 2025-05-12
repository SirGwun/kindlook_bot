package com.application.serves;

import com.application.Model.Button;
import com.application.Model.Phrase;
import com.application.Model.PhraseButton;
import com.application.Model.TopLevelButton;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class ButtonXmlLoader {
    private final Path xmlPath;

    public ButtonXmlLoader(Path xmlPath) {
        this.xmlPath = xmlPath;
    }

    public List<Button> loadButtons() {
        List<Button> buttons = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlPath.toFile());
            Element root = doc.getDocumentElement();
            NodeList buttonNodes = root.getChildNodes();

            for (int i = 0; i < buttonNodes.getLength(); i++) {
                Node node = buttonNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("button")) {
                    buttons.add(parseButton((Element) node));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении XML", e);
        }
        return buttons;
    }

    private Button parseButton(Element buttonElement) {
        String name = buttonElement.getAttribute("name");

        List<Button> subButtons = new ArrayList<>();
        List<Phrase> phrases = new ArrayList<>();

        NodeList children = buttonElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element element = (Element) node;
            switch (element.getNodeName()) {
                case "phrase":
                    phrases.add(parsePhrase(element));
                    break;
                case "button":
                    subButtons.add(parseButton(element));
                    break;
            }
        }

        if (!phrases.isEmpty() && !subButtons.isEmpty()) {
            throw new IllegalArgumentException("Button '" + name + "' cannot contain both phrases and sub-buttons.");
        }

        if (!subButtons.isEmpty()) {
            return new TopLevelButton(name, subButtons);
        } else {
            return new PhraseButton(name, phrases);
        }
    }


    private Phrase parsePhrase(Element phraseElement) {
        String text = phraseElement.getTextContent().trim();
        String imagesAttr = phraseElement.getAttribute("image");


        List<Path> imagePaths = new ArrayList<>();
        if (imagesAttr != null && !imagesAttr.isBlank()) {
            String[] parts = imagesAttr.split(",");
            for (String part : parts) {
                imagePaths.add(Path.of("data", "images", part.trim()));
            }
        }

        return new Phrase(text, imagePaths);
    }
}


