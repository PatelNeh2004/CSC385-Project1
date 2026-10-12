package csc241.project1.io;

import csc241.project1.model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Two passes allow doors to reference rooms appearing later in the XML file. */
public final class WorldLoader {
    public World load(Path path) throws IOException, WorldFormatException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override public void error(SAXParseException ex) throws SAXException { throw ex; }
                @Override public void fatalError(SAXParseException ex) throws SAXException { throw ex; }
            });
            Element root = builder.parse(path.toFile()).getDocumentElement();
            if (!root.getTagName().equals("xml") && !root.getTagName().equals("world")) {
                throw new WorldFormatException("Expected an <xml> or <world> root element.");
            }
            World world = new World();
            Map<Element, Room> definitions = new LinkedHashMap<>();
            for (Element element : children(root)) {
                if (!element.getTagName().equals("room")) {
                    throw new WorldFormatException("Unexpected element: " + element.getTagName());
                }
                Room room = new Room(required(element, "name"), required(element, "description"),
                        RoomState.parse(required(element, "state")));
                world.addRoom(room);
                definitions.put(element, room);
            }
            if (definitions.isEmpty()) throw new WorldFormatException("The world must contain rooms.");
            for (Map.Entry<Element, Room> entry : definitions.entrySet()) {
                Element element = entry.getKey();
                Room room = entry.getValue();
                for (Direction direction : Direction.values()) {
                    if (!element.hasAttribute(direction.toString())) continue;
                    String neighborName = required(element, direction.toString());
                    Room neighbor = world.room(neighborName);
                    if (neighbor == null) throw new WorldFormatException("Room " + room.name()
                            + " has an unknown " + direction + " neighbor: " + neighborName);
                    room.connect(direction, neighbor);
                }
                for (Element child : children(element)) {
                    String name = required(child, "name");
                    String description = required(child, "description");
                    if (!children(child).isEmpty()) throw new WorldFormatException("Creatures cannot contain elements.");
                    Creature creature;
                    switch (child.getTagName().toLowerCase(Locale.ROOT)) {
                        case "pc": creature = new Player(name, description); break;
                        case "npc": creature = new Npc(name, description); break;
                        case "animal": creature = new Animal(name, description); break;
                        default: throw new WorldFormatException("Unknown creature type: " + child.getTagName());
                    }
                    world.addCreature(creature, room);
                }
            }
            if (world.player() == null) throw new WorldFormatException("The world must contain exactly one PC.");
            return world;
        } catch (SAXParseException ex) {
            throw new WorldFormatException("Invalid XML at line " + ex.getLineNumber() + ": " + ex.getMessage(), ex);
        } catch (SAXException | ParserConfigurationException | IllegalArgumentException ex) {
            throw new WorldFormatException(ex.getMessage(), ex);
        }
    }

    private String required(Element element, String attribute) throws WorldFormatException {
        if (!element.hasAttribute(attribute) || element.getAttribute(attribute).isBlank()) {
            throw new WorldFormatException("<" + element.getTagName() + "> needs a nonempty " + attribute + " attribute.");
        }
        return element.getAttribute(attribute).trim();
    }

    private List<Element> children(Element parent) throws WorldFormatException {
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) result.add((Element) node);
            else if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE)
                    && !node.getTextContent().isBlank()) {
                throw new WorldFormatException("Unexpected text inside <" + parent.getTagName() + ">.");
            }
        }
        return result;
    }
}
