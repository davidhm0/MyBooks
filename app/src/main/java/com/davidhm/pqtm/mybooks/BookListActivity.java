package com.davidhm.pqtm.mybooks;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.davidhm.pqtm.mybooks.model.BookContent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
    // Adapter del RecyclerView
    private SimpleItemRecyclerViewAdapter adapter;

    // Layout para el refresco del listado de libros
    private SwipeRefreshLayout swipeContainer;

    // Instancias de autenticación y base de datos de Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    // Parámetros para autenticación en Firebase
    private static final String mEmail = "davidhm0@yahoo.es";
    private static final String mPassword = "pqtm-davidhm";

    // Etiqueta para Logs
    private static final String TAG = "MyBooks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Inicializa instancias de Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Configura elementos de la UI
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

        View recyclerView = findViewById(R.id.book_list);
        assert recyclerView != null;

        // Establece el Adapter para cargar los elementos de la lista de libros
        adapter = new SimpleItemRecyclerViewAdapter(new ArrayList<BookContent.BookItem>());
        ((RecyclerView)recyclerView).setAdapter(adapter);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Establece el Listener para el refresco del listado de libros
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Pide los libros al servidor para refrescar los datos
                getFirebaseBookList();
                swipeContainer.setRefreshing(false);
                Toast.makeText(BookListActivity.this, "LISTADO REFRESCADO", Toast.LENGTH_LONG).show();
            }
        });

        // Selecciona la acción a ejecutar
        if (getIntent() == null || getIntent().getAction() == null) {
            // Ejecuta la autenticación e intenta cargar los libros desde el servidor
            executeMain();
        } else {
            Log.d(TAG, "onCreate(): Intent action = " + getIntent().getAction());
            // Ejecuta la acción asociada al Intent
            executeIntent();
        }
    }

    /**
     * Modo de ejecución cuando se arranca la app.
     * Comprueba si hay acceso a la red y, en ese caso, intenta autenticarse
     * en el servidor de Firebase y descargar el catálogo de libros.
     * Si no es posible, muestra el contenido de la base de datos local.
     */
    private void executeMain() {
        Log.d(TAG, "Se ejecuta executeMain()");
        // Comprueba si hay acceso a la Red
        if (!isNetworkConnected()) {
            // No hay acceso -> muestra un mensaje al usuario
            Log.d(TAG, "executeMain(): no hay acceso a la red");
            showMessage("NO HAY ACCESO A LA RED");
            // Carga la lista actual de la base de datos local, en el Adapter
            adapter.setItems(BookContent.getBooks());
        } else {
            // Comprueba si el usuario ya está autenticado en Firebase
            if (mAuth.getCurrentUser() == null) {
                // No hay mingún usuario autenticado -> intenta hacer login
                signIn(mEmail, mPassword);
            } else {
                // El usuario ya está autenticado -> pide libros al servidor
                Log.d(TAG, "executeMain(): usuario autenticado previamente");
                getFirebaseBookList();
            }
        }
    }

    /**
     * Selecciona la acción a ejecutar en función del Intent recibido.
     */
    private void executeIntent() {
        // Elimina la notificación de la barra de notificaciones
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(MyFirebaseMessagingService.EXPANDED_NOTIFICATION_ID);
        // selecciona la acción a realizar
        switch (getIntent().getAction()) {
            case MyFirebaseMessagingService.ACTION_DELETE:
                // Ación de eliminar un libro de la base de datos
                Log.d(TAG, "executeIntent(): Accion eliminar libro " +
                        getIntent().getStringExtra(MyFirebaseMessagingService.BOOK_POSITION));
                deleteBook(getIntent().getStringExtra(MyFirebaseMessagingService.BOOK_POSITION));
                break;
            case MyFirebaseMessagingService.ACTION_SHOW_DETAIL:
                // Acción de mostrar el detalle de un libro
                Log.d(TAG, "executeIntent(): Accion mostrar detalle libro " +
                        getIntent().getStringExtra(MyFirebaseMessagingService.BOOK_POSITION));
                showBookDetail(getIntent().getStringExtra(MyFirebaseMessagingService.BOOK_POSITION));
                break;
            default:
                // Caso android.intent.action.MAIN (inicio de la app)
                executeMain();
        }
    }

    /**
     * Elimina de la base de datos local el libro indicado en el Intent, y
     * muestra en la actividad principal el listado de libros que quedan.
     *
     * @param bookPosition  Posición del libro seleccionado
     */
    private void deleteBook(String bookPosition) {
        Log.d(TAG, "Se ejecuta deleteBook()");

        // Si no existe el libro indicado, carga los libros en la actividad principal y termina
        String bookId = matchBook(bookPosition);
        if (bookId == null) {
            // Mensaje de aviso
            Toast.makeText(BookListActivity.this,
                    "NO SE HA ENCONTRADO EL LIBRO SOLICITADO", Toast.LENGTH_LONG).show();
            adapter.setItems(BookContent.getBooks());
            return;
        }

        // Elimina el libro de la base de datos local
        BookContent.BookItem book = BookContent.BookItem.findById(BookContent.BookItem.class,
                Integer.valueOf(bookId));
        book.delete();

        // Visualiza la lista de libros restantes en la base de datos local
        adapter.setItems(BookContent.getBooks());
    }

    /**
     * Muestra el detalle del libro indicado en el Intent. El detalle se
     * muestra bien en el fragmento de detalle (modo tablet), bien en la
     * actividad BookDeatilActivity (modo smartphone).
     *
     * @param bookPosition  Posición del libro seleccionado
     */
    private void showBookDetail(String bookPosition) {
        Log.d(TAG, "Se ejecuta showBookDetail()");
        // Carga los libros de la base de datos en la actividad principal
        adapter.setItems(BookContent.getBooks());

        // Si no existe el libro indicado, muestra un mensaje y termina
        String bookId = matchBook(bookPosition);
        if (bookId == null) {
            Toast.makeText(BookListActivity.this,
                    "NO SE HA ENCONTRADO EL LIBRO SOLICITADO", Toast.LENGTH_LONG).show();
            return;
        }

        // Visualiza el detalle del libro indicado en el Intent
        if (findViewById(R.id.book_detail_container) != null) {
            // En el fragmento de detalle (modo tablet)
            Bundle arguments = new Bundle();
            // Identifica el libro por su identificador (único)
            arguments.putString(BookDetailFragment.ARG_ITEM_ID, bookId);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            BookListActivity.this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.book_detail_container, fragment)
                    .commit();
        } else {
            // En la actividad BookDetailActivity (modo smartphone)
            Intent intent = new Intent(this, BookDetailActivity.class);
            // Identifica el libro por su identificador (único)
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, bookId);
            startActivity(intent);
        }
    }

    /**
     * Comprueba que el valor de la posición del libro pasado como parámetro
     * está dentro del rango de valores aceptable para la lista de libros de
     * la base de datos local.
     * Si es correcto, devuelve el valor (convertido a String) del identificador
     * del libro que se encuentra en esa posición.
     *
     * @param bookPosition  La posición del libro en la base de datos
     * @return  el identificador del libro (como String) o null si está fuera de rango
     */
    private String matchBook(String bookPosition) {
        int pos;
        if (bookPosition == null) {
            return null;
        } else {
            pos = Integer.valueOf(bookPosition);
        }
        if (pos < 1 || pos > BookContent.getBooks().size()) {
            return null;
        } else {
            return String.valueOf(BookContent.getBooks().get(pos-1).getIdentificador());
        }
    }

    /**
     * Comprueba si hay acceso a la red.
     *
     * @return  true si hay acceso a la red; false en otro caso
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
    }

    /**
     * Muestra en la pantalla un cuadro de diálogo con un mensaje.
     *
     * @param msg   el mensaje a mostrar
     */
    public void showMessage(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);
        builder.setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Intenta hacer login en el servidor Firebase.
     * Si la autenticación tiene éxito, pide la lista de libros al servidor.
     * Si falla, carga en el Adapter la lista de libros guardada en la base
     * de datos local.
     *
     * @param email     email de autenticación en Firebase
     * @param password  contraseña de autenticación en Firebase
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Autenticación en Firebase correcta -> pide libros al servidor
                            Log.d(TAG, "signIn:usuario autenticado correctamente");
                            getFirebaseBookList();
                        } else {
                            // La autenticación falla -> muestra un mensaje al usuario.
                            Log.w(TAG, "signIn:error de autenticación", task.getException());
                            Toast.makeText(BookListActivity.this,
                                    "ERROR DE AUTENTICACIÓN EN FIREBASE", Toast.LENGTH_LONG).show();
                            // Carga la lista actual de la base de datos local, en el Adapter
                            adapter.setItems(BookContent.getBooks());
                        }
                    }
                });
    }

    /**
     * Pide la lista de libros al servidor Firebase, y asigna un listener a
     * la referencia obtenida. El método onDataChange es invocado por primera
     * vez cuando se asigna el listener, y cada vez que hay modificaciones en
     * los datos a los que apunta la referencia. El método onCancelled es
     * invocado si se produce un error en el acceso al servidor.
     * Cuando onDataChange es invocado, actualiza la base de datos local.
     * Cuando onCancelled es invocado, carga en el Adaptar la base de datos
     * local actual.
     */
    private void getFirebaseBookList() {
        database.getReference("books").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Recibe modificaciones de la lista de libros de Firebase
                // y actualiza la base de datos local.
                Log.d(TAG, "onDataChange:recibida lista de libros de Firebase");
                updateLocalDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error en el acceso a la base de datos Firebase.
                Log.w(TAG, "onCancelled:error en acceso a base de datos de Firebase", databaseError.toException());
                // Carga la lista actual de la base de datos local, en el Adapter
                adapter.setItems(BookContent.getBooks());
            }
        });
    }

    /**
     * Actualiza la base de datos local con la lista recibida de Firebase y
     * carga la nueva lista en el Adapter.
     * Solo añade a la base de datos aquellos libros que no estaban
     * previamente almacenados.
     *
     * @param dataSnapshot la lista de libros de Firebase
     */
    private void updateLocalDatabase(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<List<BookContent.BookItem>> gtiBookList =
                new GenericTypeIndicator<List<BookContent.BookItem>>() {};
        // Convierte datos de Firebase a un List de BookItems
        List<BookContent.BookItem> bookList = dataSnapshot.getValue(gtiBookList);
        if (bookList != null) {
            // Actualiza la base de datos local
            for (BookContent.BookItem fbBook : bookList) {
                if (!BookContent.exists(fbBook)) {
                    // Añade un nuevo libro a la base de datos local
                    fbBook.save();
                    // Asigna el id del nuevo registro al campo 'identificador' del libro (único)
                    fbBook.setIdentificador(fbBook.getId().intValue());
                    fbBook.update();
                    // Log
                    Log.d(TAG, "updateLocalDatabase:añadido nuevo libro a BD: SugarID = " + fbBook.getId()
                            + "; identificador = " + fbBook.getIdentificador()
                            + "; title = " + fbBook.getTitle()
                            + "; author = " + fbBook.getAuthor()
                            + "; publicationDate = " + fbBook.getPublicationDate()
                            + "; urlImage = " + fbBook.getUrlImage()
                            + "; description = " + fbBook.getDescription().substring(0,15) + " ...");
                }
            }
        }
        // Carga la lista de libros, una vez actualizada, en el Adapter
        adapter.setItems(BookContent.getBooks());
        Log.d(TAG, "updateLocalDatabase:Libros en la base de datos SugarORM tras actualización: "
                + BookContent.BookItem.count(BookContent.BookItem.class));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<BookContent.BookItem> mValues;
        private final int EVEN = 2, ODD = 1;

        SimpleItemRecyclerViewAdapter(List<BookContent.BookItem> items) {
            mValues = items;
        }

        /**
         * Actualiza la lista de libros del Adapter con la nueva lista recibida
         * como parámetro.
         *
         * @param items la lista de libros a cargar en el Adapter
         */
        public void setItems(List<BookContent.BookItem> items) {
            // Configura la nueva lista
            mValues.clear();
            mValues.addAll(items);
            // Notifica al Adapter que los datos han cambiado
            notifyDataSetChanged();
            // Muestra un mensaje en pantalla si la lista está vacía
            if (mValues.isEmpty())
                showMessage("EL CATÁLOGO DE LIBROS ESTÁ VACÍO");
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mTitleView.setText(mValues.get(position).getTitle());
            holder.mAuthorView.setText(mValues.get(position).getAuthor());

            holder.itemView.setTag(mValues.get(position));

            // Pasa el identificador del libro actual a la actividad de detalle
            // Este identificador siempre coincide con su ID en la base de datos local
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookContent.BookItem item = (BookContent.BookItem) v.getTag();
                    if (findViewById(R.id.book_detail_container) != null) {
                        Bundle arguments = new Bundle();
                        // Identifica el libro por su identificador (único)
                        arguments.putString(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments(arguments);
                        BookListActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.book_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BookDetailActivity.class);
                        // Identifica el libro por su identificador (único)
                        intent.putExtra(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));

                        context.startActivity(intent);
                    }
                }
            });
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
