package com.davidhm.pqtm.mybooks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * Actividad que representa una pantalla de detalle de un libro (Book).
 * Esta actividad solo se utiliza en dispositivos estrechos. En dispositivos
 * del tamaño de una tableta, los detalles de los elementos se presentan en
 * paralelo con una lista de elementos en una {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity {

    private static final String TAG ="MyBookDetailActivity";

    // Botón para iniciar el formulario de compra de un libro
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lanza el Intent del formulario de compra del libro
                Intent webViewIntent = new Intent(view.getContext(), WebViewActivity.class);
                startActivity(webViewIntent);
            }
        });

        // Muestra el botón 'Up' en la barra de acción.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState es no-nulo cuando el estado del fragmento de
        // detalle está guardado de configuraciones previas de esta actividad
        // (por ejemplo, al girar la pantalla de vertical a horizontal).
        // En ese caso, el fragmento se volverá a agregar automáticamente
        // a su contenedor, por lo que no es necesario agregarlo manualmente.
        if (savedInstanceState == null) {
            // Crea el fragmento de detalle y lo añade a la actividad
            // utilizando una transacción de fragmento.
            Bundle arguments = new Bundle();
            arguments.putString(BookDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(BookDetailFragment.ARG_ITEM_ID));
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.book_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Esta ID representa el botón 'Home' o 'Up'. En el caso de esta
            // actividad, se muestra el botón 'Up'.
            // Se ha configurado para realizar la misma función que el botón de vuelta atrás
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
