package com.davidhm.pqtm.mybooks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.davidhm.pqtm.mybooks.model.BookContent;

import java.util.List;

/**
 * Actividad que representa una lista de libros (Books). Esta actividad tiene
 * diferentes presentaciones para dispositivos tamaño teléfono y tableta.
 * En teléfonos, la actividad presenta una lista de elementos (tipo Book en
 * este caso), que cuando se pulsan, llevan a una {@link BookDetailActivity}
 * que representa los detalles de dicho elemento. En tabletas, la actividad
 * presenta la lista de elementos y de detalles de un elemento una al lado
 * de la otra, utilizando dos paneles verticales.
 */
public class BookListActivity extends AppCompatActivity {

    /**
     * Indica si la actividad está o no en el modo de dos paneles, es decir,
     * si se ejecuta en un dispositivo tipo tableta.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.book_detail_container) != null) {
            // La vista de detalle del elemento estará presente solo en
            // diseños de pantalla grande (res/values-w900dp).
            // Si esta vista está presente, entonces la
            // actividad estará en modo de dos paneles.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.book_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, BookContent.ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final BookListActivity mParentActivity;
        private final List<BookContent.BookItem> mValues;
        private final boolean mTwoPane;
        private final int EVEN = 2, ODD = 1;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookContent.BookItem item = (BookContent.BookItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));
                    BookDetailFragment fragment = new BookDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.book_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(BookListActivity parent,
                                      List<BookContent.BookItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        /**
         * Devuelve el valor entero EVEN para elementos de la lista pares y el
         * valor entero ODD para elementos impares.
         *
         * @param position posición en la lista de elementos
         * @return  EVEN para elementos pares y ODD para elementos impares
         */
        @Override
        public int getItemViewType(int position) {
            if (position % 2 == 0) {
                return EVEN;
            } else {
                return ODD;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Selecciona el layout en función de si es un elemento par o impar de la lista
            View view = null;
            switch(viewType) {
                case EVEN:
                    view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_list_even_item, parent, false);
                    break;
                case ODD:
                    view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_list_odd_item, parent, false);
                    break;
                default:
                    break;
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mTitleView.setText(mValues.get(position).getTitulo());
            holder.mAuthorView.setText(mValues.get(position).getAutor());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mTitleView;
            final TextView mAuthorView;

            ViewHolder(View view) {
                super(view);
                mTitleView = (TextView) view.findViewById(R.id.book_list_title);
                mAuthorView = (TextView) view.findViewById(R.id.book_list_author);
            }
        }
    }
}
