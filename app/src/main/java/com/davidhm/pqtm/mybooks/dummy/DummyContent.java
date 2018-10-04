package com.davidhm.pqtm.mybooks.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase auxiliar para proporcionar contenido de prueba para interfaces de
 * usuario creadas por asistentes de plantillas de Android.
 * <p>
 * TODO: Reemplazar todos los usos de esta clase antes de publicar la aplicación.
 */
public class DummyContent {

    /**
     * Un Array de elementos de prueba(dummy).
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * Un Map de elementos de prueba(dummy), por ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        // Añade algunos elementos de prueba.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * Un elemento de prueba (dummy) que representa una pieza de contenido.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
