package com.davidhm.pqtm.mybooks.dummy;

import com.davidhm.pqtm.mybooks.model.BookItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    //Un Array de elementos de prueba.
    public static final List<BookItem> ITEMS = new ArrayList<BookItem>();

    // Un Map de elementos de prueba, por Identificador.
    public static final Map<String, BookItem> ITEM_MAP = new HashMap<String, BookItem>();

    // Un Map de imágenes de prueba, por URL.
    public static final Map<String, String> IMAGE_MAP = new HashMap<String, String>();

    // El número de elementos de la lista de prueba a crear.
    private static final int COUNT = 2;

    static {
        // Añade algunos elementos de prueba.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createBookItem(i));
            addImage(i);
        }
    }

    /**
     * Añade un elemento a la lista de elementos y al mapa.
     *
     * @param item  el elemento a añadir
     */
    private static void addItem(BookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.getIdentificador()), item);
    }

    /**
     * Crea una nueva instancia de prueba de la clase BookItem (nuevo libro).
     * En función de la posición del elemento en la lista, genera de forma
     * automática todos los parámetros necesarios para crear el nuevo elemento.
     *
     * @param position  la posición del elemento en la lista de elementos
     * @return la instancia del elemento BookItem creada
     */
    private static BookItem createBookItem(int position) {
        return new BookItem(position, "Title" + position, "Author" + position,
                            makeDate(position), "Description " + position,
                            "URL_book_" + position);
    }

    /**
     * Añade la imagen de portada del elemento i al mapa de imágenes.
     *
     * @param i número del elemento
     **/
    private static void addImage(int i) {
        IMAGE_MAP.put(ITEMS.get(i-1).getUrlImagenDePortada(), "book_image_" + i);
    }

    /**
     * Devuelve una fecha de publicación ficticia (elemento tipo Date) para
     * los elementos BookItem, a partir de la posición del elemento en la
     * lista. Por simplificar (solo sirve para hacer pruebas) devuelve fechas
     * correlativas a partir de una fecha fija concreta.
     *
     * @param position  la posición del elemento en la lista de elementos
     * @return  una fecha ficticia de publicación del libro
     */
    private static Date makeDate(int position) {
        Calendar calendar = new GregorianCalendar(2016, 6, 29 + position);
        return calendar.getTime();
    }
}
