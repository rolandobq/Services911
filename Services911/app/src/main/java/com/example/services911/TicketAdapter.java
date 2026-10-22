package com.example.services911;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class TicketAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final ArrayList<String> tickets;

    public TicketAdapter(Activity activity, ArrayList<String> tickets) {
        super(activity, R.layout.item_ticket, tickets);
        this.activity = activity;
        this.tickets = tickets;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(activity)
                    .inflate(R.layout.item_ticket, parent, false);
        }

        View sideBar = view.findViewById(R.id.ticketSideBar);

        TextView icon = view.findViewById(R.id.txtTicketIcon);
        TextView id = view.findViewById(R.id.txtTicketId);
        TextView service = view.findViewById(R.id.txtTicketService);
        TextView status = view.findViewById(R.id.txtTicketStatus);
        TextView priority = view.findViewById(R.id.txtTicketPriority);
        TextView body = view.findViewById(R.id.txtTicketBody);
        TextView action = view.findViewById(R.id.txtTicketAction);

        String texto = tickets.get(position);

        if (texto.startsWith("No hay")
                || texto.startsWith("No tienes")
                || texto.startsWith("Todavía")
                || texto.startsWith("Sin tickets")) {

            sideBar.setBackgroundColor(Color.parseColor("#168DCE"));

            icon.setText("✓");
            icon.setTextColor(Color.parseColor("#168DCE"));

            id.setText("");
            service.setText("Sin reportes");
            body.setText(texto);

            status.setVisibility(View.GONE);
            priority.setVisibility(View.GONE);
            action.setVisibility(View.GONE);

            return view;
        }

        status.setVisibility(View.VISIBLE);
        priority.setVisibility(View.VISIBLE);
        action.setVisibility(View.VISIBLE);

        String ticketId = obtenerPrimeraLinea(texto);

        String servicio = obtenerValor(texto, "Servicio:");
        String categoria = obtenerValor(texto, "Categoría:");
        String estado = obtenerValor(texto, "Estado:");
        String prioridad = obtenerValor(texto, "Prioridad:");

        String persona = obtenerValor(texto, "Para:");
        String ubicacion = obtenerValor(texto, "Ubicación:");
        String fecha = obtenerValor(texto, "Fecha:");
        String eta = obtenerValor(texto, "ETA:");

        String categoriaMostrar = categoria;

        if (categoriaMostrar.isEmpty()) {
            categoriaMostrar = servicio;
        }

        if (categoriaMostrar.isEmpty()) {
            categoriaMostrar = "Reporte de emergencia";
        }

        service.setText(categoriaMostrar);

        id.setText(ticketId);

        if (!fecha.isEmpty()) {
            id.append("  ·  " + fecha);
        }

        StringBuilder cuerpo = new StringBuilder();

        if (!persona.isEmpty()) {
            cuerpo.append("Persona: ").append(persona);
        }

        if (!ubicacion.isEmpty()) {
            if (cuerpo.length() > 0) {
                cuerpo.append("\n");
            }
            cuerpo.append("⌖  ").append(ubicacion);
        }

        if (!eta.isEmpty()) {
            if (cuerpo.length() > 0) {
                cuerpo.append("\n");
            }
            cuerpo.append("Tiempo estimado: ").append(eta);
        }

        if (cuerpo.length() == 0) {
            cuerpo.append(obtenerResumen(texto));
        }

        body.setText(cuerpo.toString());

        String tipo = (categoria + " " + servicio)
                .toLowerCase(Locale.ROOT);

        if (tipo.contains("paro")
                || tipo.contains("cardiac")
                || tipo.contains("cardíac")) {

            icon.setText("♡");

        } else if (tipo.contains("disparo")
                || tipo.contains("robo")
                || tipo.contains("polic")
                || tipo.contains("agresión")) {

            icon.setText("♢");

        } else if (tipo.contains("incendio")
                || tipo.contains("fuego")
                || tipo.contains("bomber")
                || tipo.contains("gas")) {

            icon.setText("♨");

        } else if (tipo.contains("diabetes")
                || tipo.contains("diabét")) {

            icon.setText("✚");

        } else if (tipo.contains("desmayo")) {

            icon.setText("♙");

        } else if (tipo.contains("accidente")) {

            icon.setText("⚠");

        } else {

            icon.setText("✚");
        }

        icon.setTextColor(Color.parseColor("#168DCE"));

        String prioridadNormalizada = prioridad
                .trim()
                .toUpperCase(Locale.ROOT);

        int colorPrioridad;
        int fondoPrioridad;

        if (prioridadNormalizada.contains("CRÍTICA")
                || prioridadNormalizada.contains("CRITICA")
                || prioridadNormalizada.contains("ALTA")) {

            priority.setText("ALTA");

            colorPrioridad = Color.parseColor("#E5484D");
            fondoPrioridad = Color.parseColor("#FDECEF");

        } else if (prioridadNormalizada.contains("MEDIA")) {

            priority.setText("MEDIA");

            colorPrioridad = Color.parseColor("#E9A23B");
            fondoPrioridad = Color.parseColor("#FFF3DF");

        } else {

            priority.setText("BAJA");

            colorPrioridad = Color.parseColor("#168DCE");
            fondoPrioridad = Color.parseColor("#E6F4FD");
        }

        sideBar.setBackgroundColor(colorPrioridad);

        priority.setTextColor(colorPrioridad);

        priority.setBackgroundTintList(
                ColorStateList.valueOf(fondoPrioridad)
        );

        String estadoNormalizado = estado
                .trim()
                .toUpperCase(Locale.ROOT);

        if (estadoNormalizado.equals("ACTIVO")) {

            status.setText("● Activo");

            status.setTextColor(
                    Color.parseColor("#E5484D")
            );

        } else if (estadoNormalizado.equals("EN CAMINO")) {

            status.setText("● En camino");

            status.setTextColor(
                    Color.parseColor("#22A06B")
            );

        } else if (estadoNormalizado.equals("ATENDIENDO")) {

            status.setText("● Atendiendo");

            status.setTextColor(
                    Color.parseColor("#E9A23B")
            );

        } else if (estadoNormalizado.equals("CERRADO")
                || estadoNormalizado.equals("FINALIZADO")) {

            status.setText("✓ Finalizado");

            status.setTextColor(
                    Color.parseColor("#22A06B")
            );

        } else {

            status.setText(
                    estado.isEmpty() ? "● Registrado" : "● " + estado
            );

            status.setTextColor(
                    Color.parseColor("#607D8B")
            );
        }

        if (estadoNormalizado.equals("CERRADO")
                || estadoNormalizado.equals("FINALIZADO")
                || texto.contains("✓")) {

            action.setText("Reporte finalizado");

            action.setTextColor(
                    Color.parseColor("#8395A2")
            );

        } else {

            action.setText("Ver y administrar  ›");

            action.setTextColor(
                    Color.parseColor("#168DCE")
            );
        }

        return view;
    }

    private String obtenerPrimeraLinea(String texto) {

        String[] lineas = texto.split("\n");

        if (lineas.length > 0) {
            return lineas[0].replace("✓ ", "").trim();
        }

        return "Ticket";
    }

    private String obtenerValor(String texto, String clave) {

        String[] lineas = texto.split("\n");

        for (String linea : lineas) {

            String limpia = linea.trim();

            if (limpia.toLowerCase(Locale.ROOT)
                    .startsWith(clave.toLowerCase(Locale.ROOT))) {

                return limpia.substring(clave.length()).trim();
            }
        }

        return "";
    }

    private String obtenerResumen(String texto) {

        String[] lineas = texto.split("\n");

        StringBuilder resultado = new StringBuilder();

        for (int i = 1; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.isEmpty()
                    || linea.startsWith("Estado:")
                    || linea.startsWith("Prioridad:")
                    || linea.startsWith("Servicio:")
                    || linea.startsWith("Categoría:")) {
                continue;
            }

            if (resultado.length() > 0) {
                resultado.append("\n");
            }

            resultado.append(linea);

            if (resultado.length() > 140) {
                break;
            }
        }

        return resultado.toString();
    }
}
