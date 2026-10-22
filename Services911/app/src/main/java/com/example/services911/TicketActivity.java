
package com.example.services911;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class TicketActivity extends Activity {

    private String service = "Médica";

    private Spinner cat;
    private Spinner person;
    private Spinner amb;

    private EditText phone;
    private EditText desc;
    private EditText address;

    private TextView txtProfileInfo;
    private TextView title;
    private TextView txtDispatch;
    private TextView txtPriorityInfo;

    private LinearLayout layoutCategoryCards;

    private Button btnCreate;
    private Button btnHigh;
    private Button btnMedium;
    private Button btnLow;

    private DatabaseHelper db;

    private boolean editando = false;
    private boolean prioridadManual = false;
    private boolean esperandoMapa = false;
    private boolean inicializando = true;

    private int ticketId = -1;

    private String personPendiente = "";
    private String categoryPendiente = "";
    private String ambulancePendiente = "";

    private String prioridadSeleccionada = "MEDIA";

    private String[] categorias;

    private final ArrayList<LinearLayout> tarjetas =
            new ArrayList<>();

    private final int azul = Color.parseColor("#168DCE");
    private final int oscuro = Color.parseColor("#18364A");
    private final int gris = Color.parseColor("#738694");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket);

        db = new DatabaseHelper(this);

        ticketId = getIntent().getIntExtra(
                "edit_ticket_id",
                -1
        );

        editando = ticketId != -1;

        String servicioIntent = getIntent()
                .getStringExtra("service");

        if (servicioIntent != null &&
                !servicioIntent.trim().isEmpty()) {
            service = servicioIntent;
        }

        String[] datosExistentes = null;

        if (editando) {
            datosExistentes = db.getTicketById(ticketId);

            if (datosExistentes == null) {
                Toast.makeText(
                        this,
                        "No se encontró el reporte",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }

            service = datosExistentes[0];
            categoryPendiente = datosExistentes[1];
            personPendiente = datosExistentes[2];
            ambulancePendiente = datosExistentes[6];
            prioridadSeleccionada = datosExistentes[7];
            prioridadManual = true;
        }

        title = findViewById(R.id.txtTitle);

        cat = findViewById(R.id.spCategory);
        person = findViewById(R.id.spPerson);
        amb = findViewById(R.id.spAmbType);

        phone = findViewById(R.id.etTicketPhone);
        desc = findViewById(R.id.etDescription);
        address = findViewById(R.id.etAddress);

        txtProfileInfo = findViewById(R.id.txtProfileInfo);
        txtDispatch = findViewById(R.id.txtDispatch);
        txtPriorityInfo = findViewById(R.id.txtPriorityInfo);

        layoutCategoryCards =
                findViewById(R.id.layoutCategoryCards);

        btnHigh = findViewById(R.id.btnPriorityHigh);
        btnMedium = findViewById(R.id.btnPriorityMedium);
        btnLow = findViewById(R.id.btnPriorityLow);

        btnCreate = findViewById(R.id.btnCreateTicket);

        title.setText(
                editando ? "Editar reporte" : "Nuevo reporte"
        );

        btnCreate.setText(
                editando ? "Guardar cambios" : "Generar reporte"
        );

        findViewById(R.id.btnBackTicket)
                .setOnClickListener(v -> finish());

        configurarCategorias();
        configurarAmbulancias();

        if (datosExistentes != null) {

            phone.setText(datosExistentes[3]);
            desc.setText(datosExistentes[4]);
            address.setText(datosExistentes[5]);

            seleccionarSpinner(
                    cat,
                    categoryPendiente
            );

            seleccionarSpinner(
                    amb,
                    ambulancePendiente
            );
        }

        configurarPersonas();

        configurarPrioridades();

        desc.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                if (!inicializando) {
                    actualizarVistaPrevia();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btnMapLocation)
                .setOnClickListener(v -> {

                    esperandoMapa = true;

                    Intent intent = new Intent(
                            this,
                            MapActivity.class
                    );

                    intent.putExtra(
                            "seleccionarUbicacion",
                            true
                    );

                    startActivity(intent);
                });

        btnCreate.setOnClickListener(
                v -> guardarTicket()
        );

        inicializando = false;

        actualizarTarjetas();
        actualizarVistaPrevia();

        Nav.setup(this);
    }

    private void configurarCategorias() {

        if (service.equals("Policía")) {

            categorias = new String[]{
                    "Disparo / arma de fuego",
                    "Robo / asalto",
                    "Violencia",
                    "Persona sospechosa",
                    "Accidente vial",
                    "Otro"
            };

        } else if (service.equals("Bomberos")) {

            categorias = new String[]{
                    "Incendio",
                    "Fuga de gas",
                    "Rescate",
                    "Accidente vehicular",
                    "Material peligroso",
                    "Otro"
            };

        } else {

            categorias = new String[]{
                    "Paro cardiaco",
                    "Desmayo / inconsciente",
                    "Diabetes",
                    "Dificultad respiratoria",
                    "Hemorragia",
                    "Convulsión",
                    "Accidente / trauma",
                    "Disparo / herida de arma",
                    "Otro"
            };
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categorias
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        cat.setAdapter(adapter);

        cat.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        actualizarTarjetas();

                        if (!inicializando) {
                            prioridadManual = false;
                            actualizarVistaPrevia();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        crearTarjetasCategorias();
    }

    private void crearTarjetasCategorias() {

        layoutCategoryCards.removeAllViews();
        tarjetas.clear();

        for (int i = 0; i < categorias.length; i += 2) {

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams paramsFila =
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    );

            paramsFila.bottomMargin = dp(9);

            layoutCategoryCards.addView(
                    fila,
                    paramsFila
            );

            for (int j = i; j < Math.min(i + 2, categorias.length); j++) {

                final int posicion = j;

                LinearLayout tarjeta =
                        new LinearLayout(this);

                tarjeta.setOrientation(
                        LinearLayout.VERTICAL
                );

                tarjeta.setGravity(Gravity.CENTER);

                tarjeta.setPadding(
                        dp(8),
                        dp(12),
                        dp(8),
                        dp(12)
                );

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                0,
                                dp(98),
                                1
                        );

                if (j % 2 == 1) {
                    params.leftMargin = dp(9);
                }

                fila.addView(tarjeta, params);

                TextView icono = new TextView(this);

                icono.setText(
                        obtenerIcono(categorias[j])
                );

                icono.setTextSize(25);
                icono.setTextColor(azul);
                icono.setGravity(Gravity.CENTER);

                tarjeta.addView(icono);

                TextView nombre = new TextView(this);

                nombre.setText(categorias[j]);
                nombre.setTextSize(12);
                nombre.setTextColor(oscuro);
                nombre.setTypeface(null, Typeface.BOLD);
                nombre.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams paramsNombre =
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        );

                paramsNombre.topMargin = dp(6);

                tarjeta.addView(nombre, paramsNombre);

                tarjeta.setOnClickListener(v -> {
                    cat.setSelection(posicion);
                    actualizarTarjetas();
                });

                tarjetas.add(tarjeta);
            }

            if (i + 1 >= categorias.length) {

                View espacio = new View(this);

                fila.addView(
                        espacio,
                        new LinearLayout.LayoutParams(
                                0,
                                dp(1),
                                1
                        )
                );
            }
        }
    }

    private void actualizarTarjetas() {

        if (cat == null || tarjetas.isEmpty()) {
            return;
        }

        int seleccionada = cat.getSelectedItemPosition();

        for (int i = 0; i < tarjetas.size(); i++) {

            boolean activa = i == seleccionada;

            GradientDrawable fondo =
                    new GradientDrawable();

            fondo.setColor(
                    activa
                            ? Color.parseColor("#E6F4FD")
                            : Color.WHITE
            );

            fondo.setCornerRadius(dp(12));

            fondo.setStroke(
                    dp(activa ? 2 : 1),
                    activa
                            ? azul
                            : Color.parseColor("#E0E9EF")
            );

            tarjetas.get(i).setBackground(fondo);
        }
    }

    private String obtenerIcono(String categoria) {

        String texto = categoria.toLowerCase(Locale.ROOT);

        if (texto.contains("paro")) return "♡";
        if (texto.contains("disparo")) return "⊙";
        if (texto.contains("diabetes")) return "✚";
        if (texto.contains("desmayo")) return "♙";
        if (texto.contains("incendio")) return "♨";
        if (texto.contains("robo")) return "♢";
        if (texto.contains("accidente")) return "⚠";
        if (texto.contains("fuga")) return "♨";
        if (texto.contains("respiratoria")) return "♡";
        if (texto.contains("hemorragia")) return "✚";
        if (texto.contains("convulsión")) return "✚";
        if (texto.contains("rescate")) return "⚑";

        return "✚";
    }

    private void configurarPersonas() {

        ArrayList<String> personas = db.getProfileNames();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        personas
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        person.setAdapter(adapter);

        if (editando && !personPendiente.isEmpty()) {
            seleccionarSpinner(
                    person,
                    personPendiente
            );
        }

        person.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        String seleccionado =
                                person.getSelectedItem()
                                        .toString();

                        mostrarInformacionPerfil(
                                seleccionado
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    private void mostrarInformacionPerfil(
            String seleccionado
    ) {

        String[] perfil =
                db.getProfileDetails(seleccionado);

        if (perfil == null) {

            txtProfileInfo.setText(
                    "Puedes capturar los datos de contacto manualmente."
            );

            return;
        }

        String info =
                "Persona: " + perfil[0] +
                        "\nRelación: " + perfil[1] +
                        "\nTeléfono: " + perfil[2] +
                        "\nSangre: " + perfil[3] +
                        "\nCondiciones: " + perfil[4] +
                        "\nAlergias: " + perfil[5];

        txtProfileInfo.setText(info);

        if (!editando || phone.getText()
                .toString().trim().isEmpty()) {

            phone.setText(perfil[2]);
        }
    }

    private void configurarAmbulancias() {

        String[] opciones = {
                "Pública · sin costo simulado",
                "Privada · costo estimado"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        amb.setAdapter(adapter);

        int visible = service.equals("Médica")
                ? View.VISIBLE
                : View.GONE;

        amb.setVisibility(visible);

        findViewById(R.id.lblAmbType)
                .setVisibility(visible);
    }

    private void configurarPrioridades() {

        btnHigh.setOnClickListener(v -> {
            prioridadManual = true;
            prioridadSeleccionada = "CRÍTICA";
            actualizarVistaPrevia();
        });

        btnMedium.setOnClickListener(v -> {
            prioridadManual = true;
            prioridadSeleccionada = "MEDIA";
            actualizarVistaPrevia();
        });

        btnLow.setOnClickListener(v -> {
            prioridadManual = true;
            prioridadSeleccionada = "BAJA";
            actualizarVistaPrevia();
        });
    }

    private String calcularPrioridadAutomatica() {

        String texto = obtenerTextoEmergencia();

        if (texto.contains("disparo") ||
                texto.contains("paro cardiaco") ||
                texto.contains("paro cardíaco") ||
                texto.contains("inconsciente") ||
                texto.contains("incendio") ||
                texto.contains("hemorragia") ||
                texto.contains("fuga de gas")) {

            return "CRÍTICA";
        }

        return "MEDIA";
    }

    private String calcularDespacho() {

        String texto = obtenerTextoEmergencia();

        if (texto.contains("disparo")) {

            return "Policía + Ambulancia";

        } else if (texto.contains("incendio") ||
                texto.contains("fuga de gas")) {

            return "Bomberos + Ambulancia";

        } else if (texto.contains("accidente") &&
                service.equals("Policía")) {

            return "Policía + Ambulancia";
        }

        return service;
    }

    private String obtenerTextoEmergencia() {

        if (cat.getSelectedItem() == null) {
            return "";
        }

        return (
                cat.getSelectedItem().toString() +
                        " " +
                        desc.getText().toString()
        ).toLowerCase(Locale.ROOT);
    }

    private void actualizarVistaPrevia() {

        if (cat.getSelectedItem() == null) {
            return;
        }

        if (!prioridadManual) {
            prioridadSeleccionada =
                    calcularPrioridadAutomatica();
        }

        String despacho = calcularDespacho();

        int eta = prioridadSeleccionada.equals("CRÍTICA")
                ? 6
                : 10;

        String prioridadVisual =
                prioridadSeleccionada.equals("CRÍTICA")
                        ? "ALTA"
                        : prioridadSeleccionada;

        txtPriorityInfo.setText(
                prioridadManual
                        ? "Prioridad seleccionada: " + prioridadVisual
                        : "Prioridad sugerida: " + prioridadVisual
        );

        txtDispatch.setText(
                "Servicios: " + despacho +
                        "\nTiempo estimado simulado: " + eta + " min" +
                        "\nAsignación únicamente para demostración."
        );

        marcarBoton(
                btnHigh,
                prioridadSeleccionada.equals("CRÍTICA"),
                "#E5484D",
                "#FDECEF"
        );

        marcarBoton(
                btnMedium,
                prioridadSeleccionada.equals("MEDIA"),
                "#D58A21",
                "#FFF3DF"
        );

        marcarBoton(
                btnLow,
                prioridadSeleccionada.equals("BAJA"),
                "#168DCE",
                "#E6F4FD"
        );
    }

    private void marcarBoton(
            Button boton,
            boolean seleccionado,
            String color,
            String fondo
    ) {

        boton.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor(
                                seleccionado ? fondo : "#FFFFFF"
                        )
                )
        );

        boton.setTextColor(
                Color.parseColor(
                        seleccionado ? color : "#607D8B"
                )
        );
    }

    private void seleccionarSpinner(
            Spinner spinner,
            String valor
    ) {

        if (valor == null || valor.isEmpty()) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {

            if (spinner.getItemAtPosition(i)
                    .toString().equals(valor)) {

                spinner.setSelection(i);
                return;
            }
        }
    }

    private void guardarTicket() {

        if (cat.getSelectedItem() == null ||
                person.getSelectedItem() == null) {
            return;
        }

        String categoria =
                cat.getSelectedItem().toString();

        String persona =
                person.getSelectedItem().toString();

        String telefono =
                phone.getText().toString().trim();

        String descripcion =
                desc.getText().toString().trim();

        String ubicacion =
                address.getText().toString().trim();

        if (telefono.isEmpty()) {
            phone.setError("Ingresa un teléfono");
            phone.requestFocus();
            return;
        }

        if (descripcion.isEmpty()) {
            desc.setError("Describe la emergencia");
            desc.requestFocus();
            return;
        }

        if (ubicacion.isEmpty()) {
            address.setError("Ingresa la ubicación");
            address.requestFocus();
            return;
        }

        if (!prioridadManual) {
            prioridadSeleccionada =
                    calcularPrioridadAutomatica();
        }

        String despacho = calcularDespacho();

        int eta = prioridadSeleccionada.equals("CRÍTICA")
                ? 6
                : 10;

        String ambulanceType = "No aplica";
        double cost = 0;

        if (service.equals("Médica")) {

            ambulanceType =
                    amb.getSelectedItem().toString();

            if (ambulanceType.startsWith("Privada")) {
                cost = 850;
            }
        }

        boolean exito;

        if (editando) {

            exito = db.updateTicket(
                    ticketId,
                    service,
                    categoria,
                    persona,
                    telefono,
                    descripcion,
                    ubicacion,
                    ambulanceType,
                    prioridadSeleccionada,
                    despacho,
                    eta,
                    cost
            );

        } else {

            long nuevoId = db.addTicket(
                    service,
                    categoria,
                    persona,
                    telefono,
                    descripcion,
                    ubicacion,
                    ambulanceType,
                    prioridadSeleccionada,
                    despacho,
                    eta,
                    cost
            );

            exito = nuevoId != -1;
        }

        if (!exito) {

            Toast.makeText(
                    this,
                    "No se pudo guardar el reporte",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        limpiarUbicacionTemporal();

        Toast.makeText(
                this,
                editando
                        ? "Reporte actualizado correctamente"
                        : "Reporte generado correctamente",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                this,
                ActiveTicketActivity.class
        );

        startActivity(intent);
        finish();
    }

    private void cargarUbicacionDelMapa() {

        SharedPreferences prefs = getSharedPreferences(
                "Services911",
                MODE_PRIVATE
        );

        String ubicacion = prefs.getString(
                "ubicacion_texto",
                ""
        );

        if (!ubicacion.isEmpty()) {
            address.setText(ubicacion);
        }
    }

    private void limpiarUbicacionTemporal() {

        getSharedPreferences(
                "Services911",
                MODE_PRIVATE
        ).edit()
                .remove("ubicacion_texto")
                .remove("latitud")
                .remove("longitud")
                .apply();
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (esperandoMapa && address != null) {

            cargarUbicacionDelMapa();
            esperandoMapa = false;
        }
    }
}
