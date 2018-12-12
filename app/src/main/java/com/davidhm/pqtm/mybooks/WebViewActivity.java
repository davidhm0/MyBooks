package com.davidhm.pqtm.mybooks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Formulario para la compra de un libro del catáogo
 */
public class WebViewActivity extends AppCompatActivity {

    private static final String TAG ="MyWebViewActivity";

    // Ruta del formulario de compra
    private static final String FORM_URL = "file:///android_asset/form.html";

    // WebView donde se carga el formulario
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // Carga el WebView
        webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl(FORM_URL);

        // Crea un nuevo cliente WebView
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Para API level menor de 21
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Termina si los datos del formulario son correcgtos
                    exitIfValidData(isFormDataValid(Uri.parse(url)));
                    return true;
                }
            });
        } else {
            // Para API level mayor o igual a 21
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    // Termina si los datos del formulario son correcgtos
                    exitIfValidData(isFormDataValid(request.getUrl()));
                    return true;
                }
            });
        }
    }

    /**
     * Comprueba si se han rellenado todos los datos del formulario.
     *
     * @param uri   Uri con los datos del formulario enviado.
     * @return      true si e formulario es válido, false en caso contrario.
     */
    private boolean isFormDataValid(Uri uri) {
        for (String param: uri.getQueryParameterNames()) {
            if (uri.getQueryParameter(param).trim().isEmpty()) {
                Log.d(TAG, "Parámetro \"" + param + "\" está vacío");
                return false;
            }
        }
        return true;
    }

    /**
     * Muestra una alerta indicando si el formulario es correcto o no.
     *
     * @param exit  valor booleano que indica si el formulario es correcto
     */
    private void exitIfValidData(boolean exit) {
        if (exit) {
            // El formulario se ha cumplimentado correctamente
            showMessage(getString(R.string.txt_purchase_successful));
        } else {
            //El formulario es incorrecto
            showMessage(getString(R.string.txt_data_form_error));
        }
    }

    /**
     * Muestra una alerta con el resultado de la compra del libro, y finaliza
     * la actividad si esta se completó con éxito.
     *
     * @param msg   el mensaje a mostrar
     */
    private void showMessage(final String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
        builder.setCancelable(false).setMessage(msg).setPositiveButton(R.string.txt_alert_dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (msg.equalsIgnoreCase(getString(R.string.txt_purchase_successful))) {
                            finish();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}