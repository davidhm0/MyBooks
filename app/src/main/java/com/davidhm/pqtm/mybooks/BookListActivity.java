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
import com.google.firebase.database.ValueEventListener;

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

        // ============ INICIO CODIGO A COMPLETAR (ejercicio 2) ===============

        // Comprueba si hay acceso a la Red
        if (!isNetworkConnected()) {
            // No hay acceso -> muestra un mensaje al usuario
            Log.d(TAG, "onCreate:no hay acceso a la red");
            showMessage("NO HAY ACCESO A LA RED");
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

        // ============ FIN CODIGO A COMPLETAR ===============
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
                        }
                    }
                });
    }

    /**
     * Pide la lista de libros al servidor Firebase, y asigna un listener a
     * la referencia obtenida. El método onDataChange es invocado por primera
     * vez cuando se asigna el listener, y cada vez que hay modificaciones en
     * los datos a los que apunta la referencia.
     */
    private void getFirebaseBookList() {
        database.getReference("books").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Recibe modificaciones de la lista de libros de Firebase
                // Muestra un mensaje.
                Log.d(TAG, "onDataChange:recibida lista de libros de Firebase");
                Toast.makeText(BookListActivity.this, "Recibida lista de libros de Firebase",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error en el acceso a la base de datos Firebase.
                Log.w(TAG, "onCancelled:error en acceso a base de datos de Firebase", databaseError.toException());
                Toast.makeText(BookListActivity.this, "Error en acceso a base de datos de Firebase",
                        Toast.LENGTH_LONG).show();
            }
        });
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
            holder.mTitleView.setText(mValues.get(position).getTitle());
            holder.mAuthorView.setText(mValues.get(position).getAuthor());

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
