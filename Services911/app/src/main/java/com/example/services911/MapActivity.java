package com.example.services911;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends Activity {

    private static final int PERMISO_UBICACION = 100;

    private MapView map;

    private Button btnMiUbicacion;
    private Button btnSeleccionarLugar;
    private Button btnBuscarDireccion;
    private Button btnConfirmarUbicacion;

    private EditText etBuscarDireccion;

    private TextView txtUbicacion;

    private Marker marcador;

    private boolean seleccionandoMapa = false;

    private LocationManager locationManager;

    private double latitudSeleccionada = 0;
    private double longitudSeleccionada = 0;

    private String direccionSeleccionada = "";

    private boolean hayUbicacionSeleccionada = false;

    private boolean modoSeleccionTicket = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance()
                .setUserAgentValue("Services911/1.3");

        Configuration.getInstance()
                .setUserAgentHttpHeader("User-Agent");

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);

        btnMiUbicacion =
                findViewById(R.id.btnMiUbicacion);

        btnSeleccionarLugar =
                findViewById(R.id.btnSeleccionarLugar);

        btnBuscarDireccion =
                findViewById(R.id.btnBuscarDireccion);

        btnConfirmarUbicacion =
                findViewById(R.id.btnConfirmarUbicacion);

        etBuscarDireccion =
                findViewById(R.id.etBuscarDireccion);

        txtUbicacion =
                findViewById(R.id.txtUbicacion);

        // Saber si llegamos aquí desde la creación de ticket
        modoSeleccionTicket =
                getIntent().getBooleanExtra(
                        "seleccionarUbicacion",
                        false
                );

        configurarMapa();

        configurarSeleccionMapa();

        cargarUbicacionGuardada();

        // -----------------------------
        // BOTÓN MI UBICACIÓN
        // -----------------------------

        btnMiUbicacion.setOnClickListener(
                v -> obtenerMiUbicacion()
        );

        // -----------------------------
        // BOTÓN ELEGIR EN MAPA
        // -----------------------------

        btnSeleccionarLugar.setOnClickListener(
                v -> {

                    seleccionandoMapa = true;

                    txtUbicacion.setText(
                            "Ahora toca en el mapa el lugar de la emergencia"
                    );

                    Toast.makeText(
                            this,
                            "Toca un punto del mapa",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // -----------------------------
        // BUSCAR DIRECCIÓN
        // -----------------------------

        btnBuscarDireccion.setOnClickListener(
                v -> buscarDireccion()
        );

        // -----------------------------
        // CONFIRMAR UBICACIÓN
        // -----------------------------

        btnConfirmarUbicacion.setOnClickListener(
                v -> confirmarUbicacion()
        );

        Nav.setup(this);
    }

    // =========================================================
    // MAPA
    // =========================================================

    private void configurarMapa() {

        map.setTileSource(
                TileSourceFactory.MAPNIK
        );

        map.setMultiTouchControls(true);

        map.setMinZoomLevel(4.0);
        map.setMaxZoomLevel(17.0);

        // Guadalajara como posición inicial
        GeoPoint inicio =
                new GeoPoint(
                        20.6597,
                        -103.3496
                );

        map.getController()
                .setZoom(14.0);

        map.getController()
                .setCenter(inicio);
    }

    // =========================================================
    // TOCAR MAPA
    // =========================================================

    private void configurarSeleccionMapa() {

        MapEventsReceiver receiver =
                new MapEventsReceiver() {

                    @Override
                    public boolean singleTapConfirmedHelper(
                            GeoPoint punto
                    ) {

                        if (!seleccionandoMapa) {
                            return false;
                        }

                        seleccionandoMapa = false;

                        obtenerDireccionDelPunto(
                                punto
                        );

                        return true;
                    }

                    @Override
                    public boolean longPressHelper(
                            GeoPoint punto
                    ) {

                        return false;
                    }
                };

        MapEventsOverlay overlay =
                new MapEventsOverlay(receiver);

        map.getOverlays()
                .add(overlay);
    }

    // =========================================================
    // BUSCAR DIRECCIÓN ESCRITA
    // =========================================================

    private void buscarDireccion() {

        String texto =
                etBuscarDireccion
                        .getText()
                        .toString()
                        .trim();

        if (texto.isEmpty()) {

            Toast.makeText(
                    this,
                    "Escribe una dirección",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ocultarTeclado();

        txtUbicacion.setText(
                "Buscando dirección..."
        );

        new Thread(() -> {

            try {

                Geocoder geocoder =
                        new Geocoder(
                                MapActivity.this,
                                Locale.getDefault()
                        );

                List<Address> resultados =
                        geocoder.getFromLocationName(
                                texto,
                                1
                        );

                runOnUiThread(() -> {

                    if (
                            resultados == null
                                    ||
                                    resultados.isEmpty()
                    ) {

                        txtUbicacion.setText(
                                "No se encontró esa dirección"
                        );

                        Toast.makeText(
                                MapActivity.this,
                                "Intenta escribir más datos de la dirección",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Address resultado =
                            resultados.get(0);

                    GeoPoint punto =
                            new GeoPoint(
                                    resultado.getLatitude(),
                                    resultado.getLongitude()
                            );

                    String direccion;

                    if (
                            resultado.getMaxAddressLineIndex()
                                    >=
                                    0
                    ) {

                        direccion =
                                resultado.getAddressLine(0);

                    } else {

                        direccion =
                                texto;
                    }

                    seleccionarPunto(
                            punto,
                            direccion
                    );

                    map.getController()
                            .animateTo(punto);

                    map.getController()
                            .setZoom(17.0);
                });

            } catch (IOException e) {

                runOnUiThread(() -> {

                    txtUbicacion.setText(
                            "No se pudo buscar la dirección"
                    );

                    Toast.makeText(
                            MapActivity.this,
                            "Revisa tu conexión a internet",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

        }).start();
    }

    // =========================================================
    // CONVERTIR PUNTO DEL MAPA A DIRECCIÓN
    // =========================================================

    private void obtenerDireccionDelPunto(
            GeoPoint punto
    ) {

        txtUbicacion.setText(
                "Obteniendo dirección..."
        );

        new Thread(() -> {

            String direccion;

            try {

                Geocoder geocoder =
                        new Geocoder(
                                MapActivity.this,
                                Locale.getDefault()
                        );

                List<Address> resultados =
                        geocoder.getFromLocation(
                                punto.getLatitude(),
                                punto.getLongitude(),
                                1
                        );

                if (
                        resultados != null
                                &&
                                !resultados.isEmpty()
                ) {

                    Address resultado =
                            resultados.get(0);

                    if (
                            resultado.getMaxAddressLineIndex()
                                    >=
                                    0
                    ) {

                        direccion =
                                resultado.getAddressLine(0);

                    } else {

                        direccion =
                                crearTextoCoordenadas(
                                        punto.getLatitude(),
                                        punto.getLongitude()
                                );
                    }

                } else {

                    direccion =
                            crearTextoCoordenadas(
                                    punto.getLatitude(),
                                    punto.getLongitude()
                            );
                }

            } catch (IOException e) {

                direccion =
                        crearTextoCoordenadas(
                                punto.getLatitude(),
                                punto.getLongitude()
                        );
            }

            String direccionFinal =
                    direccion;

            runOnUiThread(() -> {

                seleccionarPunto(
                        punto,
                        direccionFinal
                );

                map.getController()
                        .animateTo(punto);
            });

        }).start();
    }

    // =========================================================
    // GPS
    // =========================================================

    private void obtenerMiUbicacion() {

        if (
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                        !=
                        PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISO_UBICACION
            );

            return;
        }

        buscarUbicacion();
    }

    private void buscarUbicacion() {

        locationManager =
                (LocationManager)
                        getSystemService(
                                Context.LOCATION_SERVICE
                        );

        if (
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                        !=
                        PackageManager.PERMISSION_GRANTED
        ) {

            return;
        }

        Location ultima =
                locationManager
                        .getLastKnownLocation(
                                LocationManager.GPS_PROVIDER
                        );

        if (ultima == null) {

            ultima =
                    locationManager
                            .getLastKnownLocation(
                                    LocationManager.NETWORK_PROVIDER
                            );
        }

        if (ultima != null) {

            mostrarUbicacionActual(
                    ultima
            );

            return;
        }

        Toast.makeText(
                this,
                "Buscando ubicación...",
                Toast.LENGTH_SHORT
        ).show();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1,
                locationListener
        );
    }

    private final LocationListener locationListener =
            new LocationListener() {

                @Override
                public void onLocationChanged(
                        Location location
                ) {

                    mostrarUbicacionActual(
                            location
                    );

                    if (
                            locationManager != null
                                    &&
                                    checkSelfPermission(
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                            ==
                                            PackageManager.PERMISSION_GRANTED
                    ) {

                        locationManager.removeUpdates(
                                this
                        );
                    }
                }
            };

    private void mostrarUbicacionActual(
            Location location
    ) {

        GeoPoint punto =
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                );

        obtenerDireccionDelPunto(
                punto
        );

        map.getController()
                .animateTo(punto);

        map.getController()
                .setZoom(16.0);
    }

    // =========================================================
    // MARCADOR
    // =========================================================

    private void seleccionarPunto(
            GeoPoint punto,
            String direccion
    ) {

        if (marcador != null) {

            map.getOverlays()
                    .remove(marcador);
        }

        marcador =
                new Marker(map);

        marcador.setPosition(punto);

        marcador.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        marcador.setTitle(
                "Emergencia"
        );

        marcador.setSnippet(
                direccion
        );

        map.getOverlays()
                .add(marcador);

        map.invalidate();

        latitudSeleccionada =
                punto.getLatitude();

        longitudSeleccionada =
                punto.getLongitude();

        direccionSeleccionada =
                direccion;

        hayUbicacionSeleccionada =
                true;

        etBuscarDireccion.setText(
                direccion
        );

        txtUbicacion.setText(
                "Ubicación seleccionada\n"
                        + direccion
                        + "\n\nLat: "
                        + String.format(
                        Locale.US,
                        "%.6f",
                        latitudSeleccionada
                )
                        + " · Lon: "
                        + String.format(
                        Locale.US,
                        "%.6f",
                        longitudSeleccionada
                )
        );

        guardarUbicacion();
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    private void guardarUbicacion() {

        if (!hayUbicacionSeleccionada) {
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        "Services911",
                        MODE_PRIVATE
                );

        prefs.edit()

                .putString(
                        "latitud",
                        String.valueOf(
                                latitudSeleccionada
                        )
                )

                .putString(
                        "longitud",
                        String.valueOf(
                                longitudSeleccionada
                        )
                )

                .putString(
                        "ubicacion_texto",
                        direccionSeleccionada
                )

                .apply();
    }

    // =========================================================
    // CONFIRMAR Y REGRESAR AL TICKET
    // =========================================================

    private void confirmarUbicacion() {

        if (!hayUbicacionSeleccionada) {

            Toast.makeText(
                    this,
                    "Primero selecciona una ubicación",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        guardarUbicacion();

        Toast.makeText(
                this,
                "Ubicación confirmada",
                Toast.LENGTH_SHORT
        ).show();

        if (modoSeleccionTicket) {

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Ubicación guardada para el próximo ticket",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // CARGAR ÚLTIMA UBICACIÓN
    // =========================================================

    private void cargarUbicacionGuardada() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "Services911",
                        MODE_PRIVATE
                );

        String lat =
                prefs.getString(
                        "latitud",
                        ""
                );

        String lon =
                prefs.getString(
                        "longitud",
                        ""
                );

        String direccion =
                prefs.getString(
                        "ubicacion_texto",
                        ""
                );

        if (
                lat.isEmpty()
                        ||
                        lon.isEmpty()
        ) {

            return;
        }

        try {

            double latitud =
                    Double.parseDouble(lat);

            double longitud =
                    Double.parseDouble(lon);

            GeoPoint punto =
                    new GeoPoint(
                            latitud,
                            longitud
                    );

            seleccionarPunto(
                    punto,
                    direccion.isEmpty()
                            ? crearTextoCoordenadas(
                            latitud,
                            longitud
                    )
                            : direccion
            );

            map.getController()
                    .setCenter(punto);

        } catch (
                NumberFormatException ignored
        ) {

        }
    }

    private String crearTextoCoordenadas(
            double latitud,
            double longitud
    ) {

        return "Lat: "
                + String.format(
                Locale.US,
                "%.6f",
                latitud
        )
                + ", Lon: "
                + String.format(
                Locale.US,
                "%.6f",
                longitud
        );
    }

    private void ocultarTeclado() {

        View vista =
                getCurrentFocus();

        if (vista == null) {
            return;
        }

        InputMethodManager teclado =
                (InputMethodManager)
                        getSystemService(
                                INPUT_METHOD_SERVICE
                        );

        teclado.hideSoftInputFromWindow(
                vista.getWindowToken(),
                0
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (
                requestCode
                        ==
                        PERMISO_UBICACION
        ) {

            if (
                    grantResults.length > 0
                            &&
                            grantResults[0]
                                    ==
                                    PackageManager.PERMISSION_GRANTED
            ) {

                buscarUbicacion();

            } else {

                Toast.makeText(
                        this,
                        "Necesitas permitir la ubicación",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (map != null) {
            map.onPause();
        }
    }
}