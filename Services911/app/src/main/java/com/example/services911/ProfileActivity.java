
package com.example.services911;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ProfileActivity extends Activity {

    private DatabaseHelper db;

    private EditText name;
    private EditText relation;
    private EditText phone;
    private EditText blood;
    private EditText disease;
    private EditText allergy;
    private EditText address;

    private TextView txtFormTitle;

    private LinearLayout layoutProfiles;
    private LinearLayout layoutProfileForm;

    private Button btnSaveProfile;
    private Button btnAddProfile;
    private Button btnCancelProfile;

    private int editingProfileId = -1;

    private final int azul = Color.parseColor("#168DCE");
    private final int oscuro = Color.parseColor("#18364A");
    private final int gris = Color.parseColor("#738694");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        name = findViewById(R.id.etName);
        relation = findViewById(R.id.etRelation);
        phone = findViewById(R.id.etPhone);
        blood = findViewById(R.id.etBlood);
        disease = findViewById(R.id.etDisease);
        allergy = findViewById(R.id.etAllergy);
        address = findViewById(R.id.etProfileAddress);

        txtFormTitle = findViewById(R.id.txtFormTitle);

        layoutProfiles = findViewById(R.id.layoutProfiles);
        layoutProfileForm = findViewById(R.id.layoutProfileForm);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddProfile = findViewById(R.id.btnAddProfile);
        btnCancelProfile = findViewById(R.id.btnCancelProfile);

        btnAddProfile.setOnClickListener(v -> abrirNuevoPerfil());

        btnCancelProfile.setOnClickListener(v -> cerrarFormulario());

        btnSaveProfile.setOnClickListener(v -> guardarPerfil());

        Nav.setup(this);

        actualizarPerfiles();
    }

    private void abrirNuevoPerfil() {

        editingProfileId = -1;

        limpiarFormulario();

        txtFormTitle.setText("Agregar familiar");
        btnSaveProfile.setText("Guardar perfil");

        layoutProfileForm.setVisibility(View.VISIBLE);

        layoutProfileForm.post(() -> {
            layoutProfileForm.requestFocus();
            desplazarAlFormulario();
        });
    }

    private void editarPerfil(int profileId) {

        String[] perfil = db.getProfileById(profileId);

        if (perfil == null) {

            Toast.makeText(
                    this,
                    "No se encontró el perfil",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        editingProfileId = profileId;

        name.setText(perfil[1]);
        relation.setText(perfil[2]);
        phone.setText(perfil[3]);
        blood.setText(perfil[4]);
        disease.setText(perfil[5]);
        allergy.setText(perfil[6]);
        address.setText(perfil[7]);

        txtFormTitle.setText("Editar familiar");
        btnSaveProfile.setText("Guardar cambios");

        layoutProfileForm.setVisibility(View.VISIBLE);

        layoutProfileForm.post(this::desplazarAlFormulario);
    }


    private void desplazarAlFormulario() {
        layoutProfileForm.post(() -> {
            android.widget.ScrollView scroll =
                    findViewById(android.R.id.content)
                            .findViewWithTag("scrollPerfil");

            if (scroll != null) {
                scroll.smoothScrollTo(
                        0,
                        layoutProfileForm.getTop()
                );
            }
        });
    }

    private void guardarPerfil() {

        String nombre = obtenerTexto(name);
        String relacion = obtenerTexto(relation);
        String telefono = obtenerTexto(phone);
        String sangre = obtenerTexto(blood);
        String enfermedades = obtenerTexto(disease);
        String alergias = obtenerTexto(allergy);
        String direccion = obtenerTexto(address);

        if (nombre.isEmpty()) {

            name.setError("Escribe el nombre completo");
            name.requestFocus();
            return;
        }

        if (relacion.isEmpty()) {

            relation.setError("Indica el parentesco");
            relation.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);

        try {

            boolean guardado;

            if (editingProfileId == -1) {

                long nuevoId = db.addProfile(
                        nombre,
                        relacion,
                        telefono,
                        sangre,
                        enfermedades,
                        alergias,
                        direccion
                );

                guardado = nuevoId != -1;

            } else {

                guardado = db.updateProfile(
                        editingProfileId,
                        nombre,
                        relacion,
                        telefono,
                        sangre,
                        enfermedades,
                        alergias,
                        direccion
                );
            }

            if (guardado) {

                cerrarFormulario();
                actualizarPerfiles();

                Toast.makeText(
                        this,
                        "Perfil guardado correctamente",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "No se pudo guardar el perfil",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Error al guardar el perfil",
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            btnSaveProfile.setEnabled(true);
        }
    }

    private void eliminarPerfil(int profileId, String nombre) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar familiar")
                .setMessage(
                        "¿Deseas eliminar el perfil de " +
                                nombre + "?"
                )
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    boolean eliminado = db.deleteProfile(profileId);

                    if (eliminado) {

                        if (editingProfileId == profileId) {
                            cerrarFormulario();
                        }

                        actualizarPerfiles();

                        Toast.makeText(
                                this,
                                "Perfil eliminado correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo eliminar el perfil",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verPerfil(String[] perfil) {

        View vista = getLayoutInflater().inflate(
                R.layout.dialog_profile_detail,
                null
        );

        TextView avatar =
                vista.findViewById(R.id.txtDialogAvatar);

        TextView nombre =
                vista.findViewById(R.id.txtDialogName);

        TextView relacion =
                vista.findViewById(R.id.txtDialogRelation);

        TextView telefono =
                vista.findViewById(R.id.txtDialogPhone);

        TextView direccion =
                vista.findViewById(R.id.txtDialogAddress);

        TextView sangre =
                vista.findViewById(R.id.txtDialogBlood);

        TextView enfermedades =
                vista.findViewById(R.id.txtDialogDisease);

        TextView alergias =
                vista.findViewById(R.id.txtDialogAllergy);

        Button cerrar =
                vista.findViewById(R.id.btnDialogClose);

        avatar.setText(obtenerIniciales(perfil[1]));

        nombre.setText(perfil[1]);

        relacion.setText(valor(perfil[2]));

        telefono.setText(valor(perfil[3]));

        sangre.setText(valor(perfil[4]));

        enfermedades.setText(valor(perfil[5]));

        alergias.setText(valor(perfil[6]));

        direccion.setText(valor(perfil[7]));

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(vista)
                .create();

        cerrar.setOnClickListener(v -> dialogo.dismiss());


        dialogo.show();

        if (dialogo.getWindow() != null) {

            dialogo.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            int ancho = (int) (
                    getResources().getDisplayMetrics().widthPixels * 0.92f
            );

            dialogo.getWindow().setLayout(
                    ancho,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


    private void actualizarPerfiles() {

        layoutProfiles.removeAllViews();

        ArrayList<String[]> perfiles = db.getProfiles();

        if (perfiles.isEmpty()) {

            TextView vacio = crearTexto(
                    "Todavía no hay familiares guardados",
                    14,
                    gris,
                    false
            );

            vacio.setPadding(
                    dp(14),
                    dp(22),
                    dp(14),
                    dp(22)
            );

            vacio.setGravity(Gravity.CENTER);

            vacio.setBackground(
                    crearFondo(
                            Color.WHITE,
                            14,
                            "#E5EDF3"
                    )
            );

            layoutProfiles.addView(vacio);

            return;
        }

        for (String[] perfil : perfiles) {
            agregarTarjeta(perfil);
        }
    }

    private void agregarTarjeta(String[] perfil) {

        int profileId = Integer.parseInt(perfil[0]);

        LinearLayout tarjeta = new LinearLayout(this);

        tarjeta.setOrientation(LinearLayout.VERTICAL);

        tarjeta.setPadding(
                dp(15),
                dp(15),
                dp(15),
                dp(12)
        );

        tarjeta.setBackground(
                crearFondo(
                        Color.WHITE,
                        14,
                        "#E5EDF3"
                )
        );

        tarjeta.setElevation(dp(2));

        LinearLayout.LayoutParams paramsTarjeta =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        paramsTarjeta.bottomMargin = dp(12);

        layoutProfiles.addView(
                tarjeta,
                paramsTarjeta
        );

        LinearLayout cabecera = new LinearLayout(this);

        cabecera.setOrientation(LinearLayout.HORIZONTAL);

        cabecera.setGravity(Gravity.CENTER_VERTICAL);

        tarjeta.addView(cabecera);

        TextView avatar = crearTexto(
                obtenerIniciales(perfil[1]),
                15,
                azul,
                true
        );

        avatar.setGravity(Gravity.CENTER);

        avatar.setBackground(
                crearFondo(
                        Color.parseColor("#E6F4FD"),
                        12,
                        "#E6F4FD"
                )
        );

        cabecera.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                )
        );

        LinearLayout informacion = new LinearLayout(this);

        informacion.setOrientation(LinearLayout.VERTICAL);

        informacion.setPadding(
                dp(12),
                0,
                0,
                0
        );

        LinearLayout.LayoutParams paramsInfo =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        cabecera.addView(informacion, paramsInfo);

        TextView nombre = crearTexto(
                perfil[1],
                16,
                oscuro,
                true
        );

        informacion.addView(nombre);

        TextView parentesco = crearTexto(
                valor(perfil[2]),
                12,
                gris,
                false
        );

        informacion.addView(parentesco);

        LinearLayout datos = new LinearLayout(this);

        datos.setOrientation(LinearLayout.VERTICAL);

        datos.setPadding(
                0,
                dp(12),
                0,
                dp(10)
        );

        tarjeta.addView(datos);

        TextView telefono = crearTexto(
                "☎  " + valor(perfil[3]),
                13,
                gris,
                false
        );

        datos.addView(telefono);

        TextView sangre = crearTexto(
                "✚  Sangre: " + valor(perfil[4]),
                13,
                gris,
                false
        );

        LinearLayout.LayoutParams paramsSangre =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        paramsSangre.topMargin = dp(5);

        datos.addView(
                sangre,
                paramsSangre
        );

        View separador = new View(this);

        separador.setBackgroundColor(
                Color.parseColor("#E9EFF3")
        );

        tarjeta.addView(
                separador,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                )
        );

        LinearLayout acciones = new LinearLayout(this);

        acciones.setOrientation(LinearLayout.HORIZONTAL);

        acciones.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams paramsAcciones =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        paramsAcciones.topMargin = dp(8);

        tarjeta.addView(
                acciones,
                paramsAcciones
        );

        Button ver = crearBoton(
                "Ver",
                "#168DCE",
                "#E6F4FD"
        );

        Button editar = crearBoton(
                "Editar",
                "#168DCE",
                "#E6F4FD"
        );

        Button eliminar = crearBoton(
                "Eliminar",
                "#D94C5C",
                "#FDECEF"
        );

        LinearLayout.LayoutParams paramsVer =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        paramsVer.setMarginEnd(dp(5));

        acciones.addView(
                ver,
                paramsVer
        );

        LinearLayout.LayoutParams paramsEditar =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        paramsEditar.setMarginEnd(dp(5));

        acciones.addView(
                editar,
                paramsEditar
        );

        acciones.addView(
                eliminar,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        ver.setOnClickListener(
                v -> {
                    String[] perfilActual =
                            db.getProfileById(profileId);

                    if (perfilActual != null) {
                        verPerfil(perfilActual);
                    } else {
                        Toast.makeText(
                                this,
                                "El perfil ya no existe",
                                Toast.LENGTH_SHORT
                        ).show();

                        actualizarPerfiles();
                    }
                }
        );

        editar.setOnClickListener(
                v -> editarPerfil(profileId)
        );

        eliminar.setOnClickListener(
                v -> eliminarPerfil(
                        profileId,
                        perfil[1]
                )
        );
    }

    private Button crearBoton(
            String texto,
            String colorTexto,
            String colorFondo
    ) {

        Button boton = new Button(this);

        boton.setText(texto);

        boton.setTextSize(12);

        boton.setTextColor(
                Color.parseColor(colorTexto)
        );

        boton.setPadding(
                0,
                0,
                0,
                0
        );

        boton.setMinHeight(dp(48));

        boton.setMinimumHeight(dp(48));

        boton.setBackground(
                crearFondo(
                        Color.parseColor(colorFondo),
                        10,
                        colorFondo
                )
        );

        return boton;
    }

    private TextView crearTexto(
            String texto,
            int tamano,
            int color,
            boolean negrita
    ) {

        TextView vista = new TextView(this);

        vista.setText(texto);

        vista.setTextSize(tamano);

        vista.setTextColor(color);

        if (negrita) {
            vista.setTypeface(null, Typeface.BOLD);
        }

        return vista;
    }

    private GradientDrawable crearFondo(
            int color,
            int radio,
            String borde
    ) {

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(color);

        fondo.setCornerRadius(dp(radio));

        fondo.setStroke(
                dp(1),
                Color.parseColor(borde)
        );

        return fondo;
    }

    private String obtenerIniciales(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "U";
        }

        String[] partes = nombre.trim().split("\\s+");

        String primera = partes[0].substring(0, 1);

        if (partes.length > 1 && !partes[1].isEmpty()) {
            primera += partes[1].substring(0, 1);
        }

        return primera.toUpperCase(Locale.ROOT);
    }

    private String obtenerTexto(EditText campo) {

        return campo.getText()
                .toString()
                .trim();
    }

    private String valor(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return "No registrado";
        }

        return texto;
    }

    private int dp(int valor) {

        return (int) (
                valor *
                        getResources()
                                .getDisplayMetrics()
                                .density + 0.5f
        );
    }

    private void limpiarFormulario() {

        name.setText("");
        relation.setText("");
        phone.setText("");
        blood.setText("");
        disease.setText("");
        allergy.setText("");
        address.setText("");
    }

    private void cerrarFormulario() {

        editingProfileId = -1;

        limpiarFormulario();

        layoutProfileForm.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && layoutProfiles != null) {
            actualizarPerfiles();
        }
    }
}
