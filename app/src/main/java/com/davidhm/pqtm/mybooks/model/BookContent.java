package com.davidhm.pqtm.mybooks.model;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase auxiliar para proporcionar contenido de prueba.
 */
public class BookContent {

    //Un Array de elementos de prueba.
    public static final List<BookItem> ITEMS = new ArrayList<BookItem>();

    // Un Map de elementos de prueba, por Identificador.
    public static final Map<String, BookItem> ITEM_MAP = new HashMap<String, BookItem>();

    // Un Map de imágenes de prueba, por URL.
    public static final Map<String, String> IMAGE_MAP = new HashMap<String, String>();

    // El número de elementos de la lista de prueba a crear.
    private static final int COUNT = 25;

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
     * Por simplificar (solo sirve para hacer pruebas) asigna una imagen a
     * todos los elementos pares, y otra a los impares.
     *
     * @param i número del elemento
     **/
    private static void addImage(int i) {
        IMAGE_MAP.put(ITEMS.get(i-1).getUrlImage(), "book_image_" + (i%2+1));
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
     * Clase que define la estructura de cada uno de los elementos a mostrar en el
     * catálogo de libros.
     */
    public static class BookItem extends SugarRecord {

        @Unique
        private int identificador;
        private String title;
        private String author;
        private Date publicationDate;
        private String description;
        private String urlImage;

        // Constructor por defecto, vacío
        public BookItem() {

        }

        public BookItem(int identificador, String title, String author, Date publicationDate,
                        String description, String urlImage) {
            this.identificador = identificador;
            this.title = title;
            this.author = author;
            this.publicationDate = publicationDate;
            this.description = description;
            this.urlImage = urlImage;
        }

        public int getIdentificador() {
            return identificador;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public Date getPublicationDate() {
            return publicationDate;
        }

        public String getDescription() {
            return description;
        }

        public String getUrlImage() {
            return urlImage;
        }

        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }
    }
}
