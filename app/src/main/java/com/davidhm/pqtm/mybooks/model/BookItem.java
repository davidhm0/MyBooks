package com.davidhm.pqtm.mybooks.model;

import java.util.Date;

/**
 * Clase que define la estructura de cada uno de los elementos a mostrar en el
 * catálogo de libros.
 */
public class BookItem {

    private int identificador;
    private String titulo;
    private String autor;
    private Date dataDePublicacion;
    private String descripcion;
    private String urlImagenDePortada;

    public BookItem(int identificador, String titulo, String autor, Date dataDePublicacion,
                    String descripcion, String urlImagenDePortada) {
        this.identificador = identificador;
        this.titulo = titulo;
        this.autor = autor;
        this.dataDePublicacion = dataDePublicacion;
        this.descripcion = descripcion;
        this.urlImagenDePortada = urlImagenDePortada;
    }

    public int getIdentificador() {
        return identificador;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public Date getDataDePublicacion() {
        return dataDePublicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUrlImagenDePortada() {
        return urlImagenDePortada;
    }
}
