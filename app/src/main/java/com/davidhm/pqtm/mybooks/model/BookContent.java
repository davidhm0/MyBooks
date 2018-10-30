package com.davidhm.pqtm.mybooks.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Clase auxiliar para proporcionar contenido.
 */
public class BookContent {

    // Etiqueta para Logs
    private static final String TAG = "MyBooks";


    /**
     * Devuelve todos los elementos almacenados en la base de datos local.
     *
     * @return  los elementos de la base de datos local
     */
    public static List<BookItem> getBooks(){
        return BookItem.listAll(BookItem.class);
    }

    /**
     * Indica si el elemento pasado como parámetro ya está guardado en la base
     * de datos local.
     * Busca los elementos por título.
     *
     * @param bookItem  el elemento que se busca en la base de datos
     * @return  true si el elemento ya está almacenado; false en caso contrario
     */
    public static boolean exists(BookItem bookItem) {
        if (bookItem == null) return false;
        if (BookItem.find(BookItem.class, "title = ?", bookItem.getTitle()).isEmpty()) {
            return false;
        } else {
            return true;
        }
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

        /**
         * Método utilizado para importar datos de Firebase.
         * Lee el campo 'publicationDate' de la BBDD de Firebase, como cadena
         * de texto con formato dd/MM/yyyy, y lo asgina al mismo campo de
         * BookItem, en formato Date.
         *
         * @param publicationDate   fecha en formato dd/MM/yyyy
         */
        public void setPublicationDate(String publicationDate) {
            try {
                this.publicationDate = new SimpleDateFormat("dd/MM/yyyy").parse(publicationDate);
            }
            catch (ParseException ex)
            {
                Log.w(TAG, "setPublicationDate:formato de fecha incorrecto", ex);
            }
        }
    }
}
