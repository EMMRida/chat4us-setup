/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.util;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLEditor {
    private Document document;
    private String currentFilePath;

    public XMLEditor() { }

    /**
     * Opens an XML file for manipulation
     */
    public void openFile(String filePath) throws IOException, ParserConfigurationException, SAXException {
        this.currentFilePath = filePath;
        File xmlFile = new File(filePath);

        if (!xmlFile.exists()) {
            throw new IOException(Messages.getString("XMLEditor.EX_FILE_NFOUND") + filePath); //$NON-NLS-1$
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(xmlFile);
        this.document.getDocumentElement().normalize();
    }

    /**
     * Creates a new XML document with root element
     */
    public void createNewDocument(String rootElementName) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.newDocument();

        Element rootElement = this.document.createElement(rootElementName);
        this.document.appendChild(rootElement);
        this.currentFilePath = null;
    }

    /**
     * Saves the document to the current file path or a new path
     */
    public void save(String filePath) throws TransformerException {
        if (filePath != null) {
            this.currentFilePath = filePath;
        }

        if (this.currentFilePath == null) {
            throw new IllegalStateException(Messages.getString("XMLEditor.EX_MISSING_SAVING_PATH")); //$NON-NLS-1$
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no"); //$NON-NLS-1$

        // Use a custom output to control line breaks
        StringWriter writer = new StringWriter();
        DOMSource source = new DOMSource(this.document);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        // Replace double line breaks with single line breaks
        String xmlContent = writer.toString();
        xmlContent = xmlContent.replaceAll("(\r?\n)\\s*(\r?\n)", "$1"); //$NON-NLS-1$ //$NON-NLS-2$

        // Write the cleaned content to file
        try (FileOutputStream fos = new FileOutputStream(this.currentFilePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8")) { //$NON-NLS-1$
            osw.write(xmlContent);
        } catch (IOException e) {
            throw new TransformerException(Messages.getString("XMLEditor.EX_WRITING_FILE") + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    /**
     * Saves to the current file path
     */
    public void save() throws TransformerException {
        save(this.currentFilePath);
    }

    /**
     * Adds a new element at the specified path
     * Path format: /root/child/grandchild
     */
    public void addElement(String path, String value) {
        addElement(path, value, null);
    }

    /**
     * Adds a new element with attributes at the specified path
     */
    public void addElement(String path, String value, Map<String, String> attributes) {
        String[] pathParts = parsePath(path);

        // Start from document root
        Node currentNode = document.getDocumentElement();

        // Verify the root matches
        if (!currentNode.getNodeName().equals(pathParts[0])) {
            throw new IllegalArgumentException(String.format(Messages.getString("XMLEditor.EX_ROOT_ELEMENT_MISMATCH"), currentNode.getNodeName(), pathParts[0])); //$NON-NLS-1$
        }

        // Navigate through the existing path, creating missing elements if needed
        for (int i = 1; i < pathParts.length; i++) {
            String part = pathParts[i];
            Node nextNode = findChildElement(currentNode, part);

            // If element doesn't exist, create it
            if (nextNode == null) {
                Element newElement = document.createElement(part);
                currentNode.appendChild(newElement);
                nextNode = newElement;
            }

            currentNode = nextNode;
        }

        // Now currentNode should be at the parent of the element we want to add
        // The last part of the path is the element name to create
        String elementName = pathParts[pathParts.length - 1];
        Element parentElement = (Element) currentNode;

        // Check if element already exists
        Node existingElement = findChildElement(parentElement, elementName);
        if (existingElement != null) {
            // Update existing element
            existingElement.setTextContent(value != null ? value : ""); //$NON-NLS-1$
            if (attributes != null) {
                Element existingElem = (Element) existingElement;
                for (Map.Entry<String, String> attr : attributes.entrySet()) {
                    existingElem.setAttribute(attr.getKey(), attr.getValue());
                }
            }
        } else {
            // Create new element
            Element newElement = document.createElement(elementName);

            if (value != null && !value.isEmpty()) {
                newElement.setTextContent(value);
            }

            // Add attributes if provided
            if (attributes != null) {
                for (Map.Entry<String, String> attr : attributes.entrySet()) {
                    newElement.setAttribute(attr.getKey(), attr.getValue());
                }
            }

            parentElement.appendChild(newElement);
        }
    }

    /**
     * Edits the value of an element at the specified path
     */
    public void editElement(String path, String newValue) {
        Node element = findElement(path);
        if (element != null) {
            element.setTextContent(newValue);
        } else {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_ELEMENT_NFOUND") + path); //$NON-NLS-1$
        }
    }

    /**
     * Edits or adds attributes to an element
     */
    public void editAttributes(String path, Map<String, String> attributes) {
        Element element = (Element) findElement(path);
        if (element != null) {
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                element.setAttribute(attr.getKey(), attr.getValue());
            }
        } else {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_ELEMENT_NFOUND") + path); //$NON-NLS-1$
        }
    }

    /**
     * Removes an element at the specified path
     */
    public void removeElement(String path) {
        Node element = findElement(path);
        if (element != null) {
            Node parent = element.getParentNode();
            parent.removeChild(element);
        } else {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_ELEMENT_NFOUND") + path); //$NON-NLS-1$
        }
    }

    /**
     * Removes an attribute from an element
     */
    public void removeAttribute(String path, String attributeName) {
        Element element = (Element) findElement(path);
        if (element != null && element.hasAttribute(attributeName)) {
            element.removeAttribute(attributeName);
        } else {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_ELEMENT_ATTRIBUTE_NFOUND") + path); //$NON-NLS-1$
        }
    }

    /**
     * Gets the value of an element at the specified path
     */
    public String getElementValue(String path) {
        Node element = findElement(path);
        return element != null ? element.getTextContent() : null;
    }

    /**
     * Gets all attributes of an element at the specified path
     */
    public Map<String, String> getElementAttributes(String path) {
        Element element = (Element) findElement(path);
        Map<String, String> attributes = new HashMap<>();

        if (element != null) {
            NamedNodeMap attributeNodes = element.getAttributes();
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Node attr = attributeNodes.item(i);
                attributes.put(attr.getNodeName(), attr.getNodeValue());
            }
        }

        return attributes;
    }

    /**
     * Checks if an element exists at the specified path
     */
    public boolean elementExists(String path) {
        return findElement(path) != null;
    }

    /**
     * Finds an element by path
     */
    private Node findElement(String path) {
        String[] pathParts = parsePath(path);
        Node currentNode = document.getDocumentElement();

        // Verify root matches
        if (!currentNode.getNodeName().equals(pathParts[0])) {
            return null;
        }

        // Navigate through path
        for (int i = 1; i < pathParts.length; i++) {
            currentNode = findChildElement(currentNode, pathParts[i]);
            if (currentNode == null) {
                return null;
            }
        }

        return currentNode;
    }

    /**
     * Finds a direct child element with the given name
     */
    private Node findChildElement(Node parent, String childName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                child.getNodeName().equals(childName)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Parses a path string into parts
     */
    private String[] parsePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_NULL_EMPTY_PATH")); //$NON-NLS-1$
        }

        // Remove leading/trailing slashes and split
        String cleanPath = path.replaceAll("^/+|/+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (cleanPath.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("XMLEditor.EX_INVALID_PATH") + path); //$NON-NLS-1$
        }

        return cleanPath.split("/"); //$NON-NLS-1$
    }

    /**
     * Returns the XML as a formatted string
     */
    public String toString() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no"); //$NON-NLS-1$

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            return Messages.getString("XMLEditor.EX_CONVERT_ERROR") + e.getMessage(); //$NON-NLS-1$
        }
    }

    /**
     * Gets all child elements of a given path
     */
    public List<String> getChildElements(String path) {
        List<String> children = new ArrayList<>();
        Node parent = path.isEmpty() ? document.getDocumentElement() : findElement(path);

        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(child.getNodeName());
                }
            }
        }

        return children;
    }
}