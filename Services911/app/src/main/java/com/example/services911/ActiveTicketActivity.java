
package com.example.services911;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ActiveTicketActivity extends Activity {

    private ListView listTickets;

    private TextView txtTituloTickets;
    private TextView txtCantidad;

    private Button btnActivos;
    private Button btnHistorial;
    private Button btnNuevoTicket;

    private DatabaseHelper db;

    private ArrayList<String> tickets;
    private ArrayList<Integer> ticketIds;

    private boolean mostrandoHistorial = false;

    private final int azul = Color.parseColor("#168DCE");
    private final int gris = Color.parseColor("#405C6E");
    private final int blanco = Color.WHITE;
    private final int fondoPestanas = Color.parseColor("#E5EDF3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_ticket);

        db = new DatabaseHelper(this);

        listTickets = findViewById(R.id.listTickets);

        txtTituloTickets = findViewById(R.id.txtTituloTickets);
        txtCantidad = findViewById(R.id.txtCantidad);

        btnActivos = findViewById(R.id.btnActivos);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnNuevoTicket = findViewById(R.id.btnNuevoTicket);

        Nav.setup(this);

        txtTituloTickets.setText("Emergencias");

        btnNuevoTicket.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ActiveTicketActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
        });

        btnActivos.setOnClickListener(
                v -> mostrarActivos()
        );

        btnHistorial.setOnClickListener(
                v -> mostrarHistorial()
        );

        listTickets.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (ticketIds == null ||
                            position < 0 ||
                            position >= ticketIds.size()) {
                        return;
                    }

                    int ticketId = ticketIds.get(position);

                    Intent intent = new Intent(
                            ActiveTicketActivity.this,
                            TicketDetailActivity.class
                    );

                    intent.putExtra("ticket_id", ticketId);

                    startActivity(intent);
                }
        );

        mostrarActivos();
    }

    private void actualizarPestanas() {

        if (mostrandoHistorial) {

            btnActivos.setBackgroundTintList(
                    ColorStateList.valueOf(fondoPestanas)
            );
            btnActivos.setTextColor(gris);

            btnHistorial.setBackgroundTintList(
                    ColorStateList.valueOf(blanco)
            );
            btnHistorial.setTextColor(azul);

        } else {

            btnActivos.setBackgroundTintList(
                    ColorStateList.valueOf(blanco)
            );
            btnActivos.setTextColor(azul);

            btnHistorial.setBackgroundTintList(
                    ColorStateList.valueOf(fondoPestanas)
            );
            btnHistorial.setTextColor(gris);
        }

        btnActivos.setText("Activos");
        btnHistorial.setText("Historial");
    }

    private void mostrarActivos() {

        mostrandoHistorial = false;

        txtTituloTickets.setText("Emergencias");

        int cantidad = db.countActiveTickets();

        txtCantidad.setText(
                cantidad == 1
                        ? "1 ticket"
                        : cantidad + " tickets"
        );

        tickets = db.getActiveTickets();
        ticketIds = db.getActiveTicketIds();

        if (tickets.isEmpty()) {

            tickets.add(
                    "No tienes reportes activos.\n\n" +
                            "Pulsa + Nuevo para crear uno."
            );
        }

        TicketAdapter adapter = new TicketAdapter(
                this,
                tickets
        );

        listTickets.setAdapter(adapter);

        actualizarPestanas();
    }

    private void mostrarHistorial() {

        mostrandoHistorial = true;

        txtTituloTickets.setText("Emergencias");

        tickets = db.getClosedTickets();
        ticketIds = db.getClosedTicketIds();

        int cantidad = tickets.size();

        txtCantidad.setText(
                cantidad == 1
                        ? "1 finalizado"
                        : cantidad + " finalizados"
        );

        if (tickets.isEmpty()) {

            tickets.add(
                    "Todavía no hay reportes finalizados."
            );
        }

        TicketAdapter adapter = new TicketAdapter(
                this,
                tickets
        );

        listTickets.setAdapter(adapter);

        actualizarPestanas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && listTickets != null) {

            if (mostrandoHistorial) {
                mostrarHistorial();
            } else {
                mostrarActivos();
            }
        }
    }
}
