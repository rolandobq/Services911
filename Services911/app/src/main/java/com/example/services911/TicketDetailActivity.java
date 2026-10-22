
package com.example.services911;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TicketDetailActivity extends Activity {

    private DatabaseHelper db;
    private int ticketId = -1;

    private TextView txtCategory;
    private TextView txtFolio;
    private TextView txtPriority;
    private TextView txtStatus;
    private TextView txtIcon;

    private TextView txtPerson;
    private TextView txtPhone;
    private TextView txtAddress;
    private TextView txtDescription;
    private TextView txtDispatch;
    private TextView txtEta;

    private TextView[] pasos;

    private Button btnNext;
    private Button btnFinish;
    private TextView btnEdit;

    private LinearLayout layoutActions;

    private String estadoActual = "";

    private final int azul = Color.parseColor("#168DCE");
    private final int gris = Color.parseColor("#8395A2");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket_detail);

        db = new DatabaseHelper(this);

        ticketId = getIntent().getIntExtra(
                "ticket_id",
                -1
        );

        if (ticketId == -1) {
            Toast.makeText(
                    this,
                    "Reporte no encontrado",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        txtCategory = findViewById(R.id.txtDetailCategory);
        txtFolio = findViewById(R.id.txtDetailFolio);
        txtPriority = findViewById(R.id.txtDetailPriority);
        txtStatus = findViewById(R.id.txtDetailStatus);
        txtIcon = findViewById(R.id.txtDetailIcon);

        txtPerson = findViewById(R.id.txtDetailPerson);
        txtPhone = findViewById(R.id.txtDetailPhone);
        txtAddress = findViewById(R.id.txtDetailAddress);
        txtDescription = findViewById(R.id.txtDetailDescription);
        txtDispatch = findViewById(R.id.txtDetailDispatch);
        txtEta = findViewById(R.id.txtDetailEta);

        pasos = new TextView[]{
                findViewById(R.id.txtStep1),
                findViewById(R.id.txtStep2),
                findViewById(R.id.txtStep3),
                findViewById(R.id.txtStep4),
                findViewById(R.id.txtStep5)
        };

        btnNext = findViewById(R.id.btnDetailNextStatus);
        btnFinish = findViewById(R.id.btnDetailFinish);
        btnEdit = findViewById(R.id.btnEditDetail);

        layoutActions = findViewById(
                R.id.layoutDetailActions
        );

        findViewById(R.id.btnBackDetail)
                .setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> editarTicket());

        btnNext.setOnClickListener(v -> avanzarEstado());

        btnFinish.setOnClickListener(
                v -> confirmarFinalizacion()
        );

        Nav.setup(this);

        cargarReporte();
    }

    private void cargarReporte() {

        String[] datos = db.getTicketById(ticketId);

        if (datos == null) {
            Toast.makeText(
                    this,
                    "El reporte ya no existe",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        String servicio = datos[0];
        String categoria = datos[1];
        String persona = datos[2];
        String telefono = datos[3];
        String descripcion = datos[4];
        String direccion = datos[5];
        String prioridad = datos[7];
        String despacho = datos[8];

        estadoActual = datos[9];

        txtCategory.setText(categoria);
        txtFolio.setText("Ticket #" + ticketId);

        txtPerson.setText(valor(persona));
        txtPhone.setText(valor(telefono));
        txtAddress.setText(valor(direccion));
        txtDescription.setText(valor(descripcion));

        txtDispatch.setText(valor(despacho));

        txtEta.setText(
                "Tiempo estimado simulado: " +
                        datos[10] + " min"
        );

        String prioridadVisual =
                prioridad.equalsIgnoreCase("CRÍTICA")
                        ? "ALTA"
                        : prioridad;

        txtPriority.setText(
                prioridadVisual.toUpperCase(Locale.ROOT)
        );

        String tipo = (
                servicio + " " + categoria
        ).toLowerCase(Locale.ROOT);

        if (tipo.contains("paro") ||
                tipo.contains("cardiac")) {

            txtIcon.setText("♡");

        } else if (tipo.contains("incendio") ||
                tipo.contains("bomber")) {

            txtIcon.setText("♨");

        } else if (tipo.contains("polic") ||
                tipo.contains("robo") ||
                tipo.contains("disparo")) {

            txtIcon.setText("♢");

        } else {

            txtIcon.setText("✚");
        }

        actualizarEstadoVisual();
    }

    private void actualizarEstadoVisual() {

        String estado = estadoActual.toUpperCase(Locale.ROOT);

        switch (estado) {

            case "EN CAMINO":
                txtStatus.setText("● Unidad en camino");
                break;

            case "ATENDIENDO":
                txtStatus.setText("● Emergencia en atención");
                break;

            case "CERRADO":
                txtStatus.setText("✓ Reporte finalizado");
                break;

            default:
                txtStatus.setText("● Reporte registrado");
                break;
        }

        int pasoActual;

        if (estado.equals("CERRADO")) {
            pasoActual = 5;

        } else if (estado.equals("ATENDIENDO")) {
            pasoActual = 4;

        } else if (estado.equals("EN CAMINO")) {
            pasoActual = 3;

        } else {
            pasoActual = 1;
        }

        for (int i = 0; i < pasos.length; i++) {

            boolean completado = i < pasoActual;

            pasos[i].setTextColor(
                    completado ? azul : gris
            );

            pasos[i].setTypeface(
                    null,
                    completado
                            ? Typeface.BOLD
                            : Typeface.NORMAL
            );
        }

        boolean cerrado = estado.equals("CERRADO");

        layoutActions.setVisibility(
                cerrado ? View.GONE : View.VISIBLE
        );

        btnEdit.setVisibility(
                cerrado ? View.INVISIBLE : View.VISIBLE
        );

        if (estado.equals("ACTIVO")) {
            btnNext.setText("Marcar unidad en camino");

        } else if (estado.equals("EN CAMINO")) {
            btnNext.setText("Marcar emergencia en atención");

        } else if (estado.equals("ATENDIENDO")) {
            btnNext.setVisibility(View.GONE);

        } else {
            btnNext.setVisibility(View.GONE);
        }

        if (!cerrado && !estado.equals("ATENDIENDO")) {
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void avanzarEstado() {

        String nuevoEstado;

        if (estadoActual.equalsIgnoreCase("ACTIVO")) {

            nuevoEstado = "EN CAMINO";

        } else if (estadoActual.equalsIgnoreCase("EN CAMINO")) {

            nuevoEstado = "ATENDIENDO";

        } else {

            return;
        }

        db.updateTicketStatus(ticketId, nuevoEstado);

        Toast.makeText(
                this,
                "Estado actualizado",
                Toast.LENGTH_SHORT
        ).show();

        cargarReporte();
    }

    private void editarTicket() {

        if (estadoActual.equalsIgnoreCase("CERRADO")) {
            return;
        }

        Intent intent = new Intent(
                this,
                TicketActivity.class
        );

        intent.putExtra(
                "edit_ticket_id",
                ticketId
        );

        startActivity(intent);
    }

    private void confirmarFinalizacion() {

        new AlertDialog.Builder(this)
                .setTitle("Finalizar reporte")
                .setMessage(
                        "¿Deseas finalizar el ticket #" +
                                ticketId + "?"
                )
                .setPositiveButton(
                        "Finalizar",
                        (dialog, which) -> {

                            db.closeTicket(ticketId);

                            Toast.makeText(
                                    this,
                                    "Reporte finalizado",
                                    Toast.LENGTH_SHORT
                            ).show();

                            cargarReporte();
                        }
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String valor(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return "No registrado";
        }

        return texto;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && ticketId != -1) {
            cargarReporte();
        }
    }
}
