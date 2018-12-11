package com.davidhm.pqtm.mybooks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidhm.pqtm.mybooks.model.BookContent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Fragmento que representa una pantalla de detalle de un libro (Book).
 * Este fragmento está contenido en una {@link BookListActivity} en modo de dos
 * paneles (en tabletas) o en una {@link BookDetailActivity} en los teléfonos.
 */
public class BookDetailFragment extends Fragment {

    // El argumento del fragmento que representa el ID del elemento que este
    // fragmento representa.
    public static final String ARG_ITEM_ID = "item_id";

    // El contenido que este fragmento presenta.
    private BookContent.BookItem mItem;

    // La vista que almacenará la imagen del elemento
    private ImageView imageView;

    // Botón para iniciar el formulario de compra de un libro
    private FloatingActionButton fab;

    /**
     * Constructor vacío obligatorio para que el gestor de fragmentos
     * instancie el fragmento (por ejemplo, en los cambios de orientación de
     * la pantalla).
     */
    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Cargua el contenido especificado por los argumentos del fragmento.
            // Utiliza el ID del elemento para buscarlo en la base de datos local.
            mItem = BookContent.BookItem.findById(BookContent.BookItem.class,
                    Integer.valueOf(getArguments().getString(ARG_ITEM_ID)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Evita que se pierda el título del detalle cuando se gira la pantalla
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mItem.getTitle());
        }

        // El botón para lanzar el formulario de compra de un libro
        fab = (FloatingActionButton) activity.findViewById(R.id.fab_tablet);
        if (fab != null) {
            fab.show();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Pulsado botón fab_tablet en BookDetailFragment", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        View rootView = inflater.inflate(R.layout.book_detail, container, false);

        // Muestra el contenido.
        if (mItem != null) {
            //Recupera la url de imagen de portada.
            String url =  mItem.getUrlImage();
            imageView = (ImageView) rootView.findViewById(R.id.book_image);
            new downloadImage().execute(url);
            //Obtiene la fecha, en formato dd/MM/yyyy.
            String date = new SimpleDateFormat("dd/MM/yyyy").format(mItem.getPublicationDate());
            // Muestra los detalles del contenido
            ((TextView) rootView.findViewById(R.id.book_author)).
                    setText(mItem.getAuthor());
            ((TextView) rootView.findViewById(R.id.book_date)).
                    setText(date);
            ((TextView) rootView.findViewById(R.id.book_description)).
                    setText(mItem.getDescription());
        }

        return rootView;
    }

    /**
     * Descarga asíncronamente una imagen, a partir de un URL dado, y la coloca
     * en la vista correspondiente.
     */
    private class downloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap image = null;
            URL imageUrl;
            try {
                imageUrl = new URL(url[0]);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                image = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap map) {
            super.onPostExecute(map);
            // Coloca la imagen descargada en la vista
            imageView.setImageBitmap(map);
        }
    }
}
