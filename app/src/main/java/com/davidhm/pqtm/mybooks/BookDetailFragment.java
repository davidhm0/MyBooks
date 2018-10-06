package com.davidhm.pqtm.mybooks;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.davidhm.pqtm.mybooks.dummy.DummyContent;
import com.davidhm.pqtm.mybooks.model.BookItem;

/**
 * Fragmento que representa una pantalla de detalle de un libro (Book).
 * Este fragmento está contenido en una {@link BookListActivity} en modo de dos
 * paneles (en tabletas) o en una {@link BookDetailActivity} en los teléfonos.
 */
public class BookDetailFragment extends Fragment {
    /**
     * El argumento del fragmento que representa el ID del elemento que este
     * fragmento representa.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * El contenido de prueba que este fragmento presenta.
     */
    private BookItem mItem;

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
            // Cargua el contenido de prueba especificado por los argumentos
            // del fragmento. En un escenario real, usar un cargador (Loader)
            // para cargar contenido de un proveedor de contenido.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getTitulo());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.book_detail, container, false);

        // Muestra el contenido de prueba como texto en un TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.book_detail)).setText(DummyContent.makeDetails(mItem));
        }

        return rootView;
    }
}
