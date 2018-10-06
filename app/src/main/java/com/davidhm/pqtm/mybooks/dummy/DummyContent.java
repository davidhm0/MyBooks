package com.davidhm.pqtm.mybooks.dummy;

import com.davidhm.pqtm.mybooks.model.BookItem;

import java.text.SimpleDateFormat;
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

    // El número de elementos de la lista de prueba a crear.
    private static final int COUNT = 25;

    static {
        // Añade algunos elementos de prueba.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createBookItem(i));
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
                            makeDate(position), "Description " + position, makeUrl(position));
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

    /**
     * Devuelve un String para simular una URL ficticia, solo a efectos de
     * prueba.
     *
     * @param position  la posición del elemento en la lista de elementos
     * @return  una URL ficticia
     */
    private static String makeUrl(int position) {
        return ("URL_book_" + position);
    }

    /**
     * Devuelve un String con los detalles del libro.
     *
     * @param book  el libro
     * @return  los detalles del libro
     */
    public static String makeDetails(BookItem book) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return book.getAutor() +
                "\n" + dateFormat.format(book.getDataDePublicacion()) +
                "\n" + book.getDescripcion() +
                "\n" + book.getUrlImagenDePortada();
    }
}
