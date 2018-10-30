package com.davidhm.pqtm.mybooks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

        // Comprueba si hay acceso a la Red
        if (!isNetworkConnected()) {
            // No hay acceso -> muestra un mensaje al usuario
            Log.d(TAG, "onCreate:no hay acceso a la red");
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
                Log.d(TAG, "onCreate:usuario autenticado previamente");
                getFirebaseBookList();
            }
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
                Toast.makeText(BookListActivity.this, "Recibida lista de libros de Firebase",
                        Toast.LENGTH_LONG).show();
                updateLocalDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error en el acceso a la base de datos Firebase.
                Log.w(TAG, "onCancelled:error en acceso a base de datos de Firebase", databaseError.toException());
                Toast.makeText(BookListActivity.this, "Error en acceso a base de datos de Firebase",
                        Toast.LENGTH_LONG).show();
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
            // ============ INICIO CODIGO A COMPLETAR ejercicio 3 ===============

            // Configura la nueva lista
            mValues.clear();
            mValues.addAll(items);
            // Notifica al Adapter que los datos han cambiado
            notifyDataSetChanged();
            // Muestra un mensaje en pantalla si la lista está vacía
            if (mValues.isEmpty())
                showMessage("EL CATÁLOGO DE LIBROS ESTÁ VACÍO");

            // ============ FIN CODIGO A COMPLETAR ===============
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
            holder.mTitleView.setText(mValues.get(position).getTitle());
            holder.mAuthorView.setText(mValues.get(position).getAuthor());

            holder.itemView.setTag(mValues.get(position));
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
