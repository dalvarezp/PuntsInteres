package net.infobosccoma.puntsinteres;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements View.OnClickListener {

    private static final String URL_DATA = "http://www.infobosccoma.net/pmdm/pois.php";//URL on hi ha les dades
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button btnCercar;
    private EditText etEntrada;
    private String textEntrat;
    private LatLngBounds.Builder centrarMapa;
    private LatLng positionOfPois;
    private DescarregarDades download;
    private ArrayList<Pois> dades; //Descarregar totes les dades del servidor, objectes de la classe Pois
    private ArrayList<Pois> coincidencies; //Per les dades filtrades
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        btnCercar = (Button) findViewById(R.id.btnCercar);
        btnCercar.setOnClickListener(this);
        etEntrada = (EditText) findViewById(R.id.editTextEntrada);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        baixarDades();//Baixo les dades quan s'obre l'aplicació i les guardo a l'ArrayList dades
    }


    private void baixarDades() {
        //creo l'objecte de tasca asíncrona
        download = new DescarregarDades();
        try {
            //l'executo
            download.execute(URL_DATA);
        } catch (IllegalStateException ex) {
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        // Si ha l'ArrayList coincidencies hi ha dades (ArrayList on carrego les dades que coincideixen
        if (coincidencies.size() > 0) {
            //vaig carregant els punts a l'objecte centrarMapa perquè mostri la pantalla centrada entre els
            //punts a mostrar i els marcadors amb la posició i el nom a mostrar
            centrarMapa = new LatLngBounds.Builder();
            for (int i = 0; i < coincidencies.size(); i++) {
                positionOfPois = new LatLng(coincidencies.get(i).getLatitude(), coincidencies.get(i).getLongitude());
                centrarMapa.include(positionOfPois);
                //Afageixo els marcadors al mapa que hi ha a l'ArrayList cercar
                mMap.addMarker(new MarkerOptions().position(positionOfPois).title(coincidencies.get(i).getName()));
            }
            //Mostra la situació actual del mòbil
            mMap.setMyLocationEnabled(true);
            //Faig l'acostament als punts centrats de forma animada amb un padding de 100
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centrarMapa.build(), 100));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(centrarMapa.build(), 100));

        } else {
            //Si l'string entrat a la caixa de text no coincideix amb cap ciutat
            //de les que disposem mostro un missatge
            Toast.makeText(getBaseContext(), "No hi ha cap ciutat amb aquest nom.",
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCercar) {
            //oculta el teclat després de clicar el botó
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(etEntrada.getWindowToken(), 0);
            //carrego les dades que mostrarà el mapa
            accioCercar();
        }
    }


    /**
     * Mètode que comprova si tenim dades
     */
    private void accioCercar() {
        //M'asseguro que s'ha pogut baixar les dades, pot no haber-hi conexió
        // o que no hagi tingut temps a executar-se la tasca asíncrona.
        if (dades == null) {
            Toast.makeText(getBaseContext(), "Encara no s'han carregat les dades.",
                    Toast.LENGTH_SHORT).show();
        } else {
            textEntrat = etEntrada.getText().toString();
            carregaPuntsAlMapa(textEntrat);
        }
    }


    /**
     * Mètode que rep un string amb la població que es vol buscar i mostra els punts
     * que coincideixen en el mapa
     * @param textEntrat
     */
    private void carregaPuntsAlMapa(String textEntrat) {
        coincidencies = new ArrayList<Pois>();
        //Sinó s'entra cap text mostra tots els punts
        if (textEntrat.equals("")) {
            coincidencies = dades;
        } else {
            //guardo a l'ArrayList cercar les dades que coincideixen de l'ArrayList dades
            for (int i = 0; i < dades.size(); i++) {
                if (textEntrat.compareToIgnoreCase(dades.get(i).getCity()) == 0) {
                    coincidencies.add(dades.get(i));
                }
            }
        }
        setUpMap();
    }


    /**
     * Classe asíncrona per descarregar les dades del servidor
     */
    class DescarregarDades extends AsyncTask<String, Void, ArrayList<Pois>> { //torna un arraylist d'objectes Pois

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Pois> doInBackground(String... params) {
            ArrayList<Pois> llistaDades = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httppostreq = new HttpPost(URL_DATA);
            HttpResponse httpresponse = null;
            try {
                //conexió al servidor
                httpresponse = httpclient.execute(httppostreq);
                String responseText = EntityUtils.toString(httpresponse.getEntity());
                //carrego les dades del servidor a la llista
                llistaDades = tractarJSON(responseText);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return llistaDades;
        }

        @Override
        protected void onPostExecute(ArrayList<Pois> llista) {
            //copio a l'ArrayList dades el que s'ha descarregat
            dades = llista;
            progressBar.setVisibility(View.GONE);
        }


        /**
         * Mètode que fa el tractament del Json, converteix Json en objectes Java
         * @param json
         * @return
         */
        private ArrayList<Pois> tractarJSON(String json) {
            Gson converter = new Gson();
            return converter.fromJson(json, new TypeToken<ArrayList<Pois>>() {
            }.getType());
        }

    }


}
