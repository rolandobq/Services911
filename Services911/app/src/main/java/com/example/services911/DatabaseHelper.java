
package com.example.services911;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "services911.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE profiles(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "relation TEXT," +
                        "phone TEXT," +
                        "blood TEXT," +
                        "disease TEXT," +
                        "allergy TEXT," +
                        "address TEXT)"
        );

        db.execSQL(
                "CREATE TABLE tickets(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "service TEXT," +
                        "category TEXT," +
                        "person TEXT," +
                        "phone TEXT," +
                        "description TEXT," +
                        "address TEXT," +
                        "ambulance_type TEXT," +
                        "priority TEXT," +
                        "dispatch TEXT," +
                        "status TEXT," +
                        "eta INTEGER," +
                        "cost REAL," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 2) {

            try {
                db.execSQL(
                        "ALTER TABLE profiles " +
                                "ADD COLUMN phone TEXT DEFAULT ''"
                );
            } catch (Exception ignored) {
            }

            try {
                db.execSQL(
                        "ALTER TABLE tickets " +
                                "ADD COLUMN phone TEXT DEFAULT ''"
                );
            } catch (Exception ignored) {
            }
        }
    }

    public long addProfile(
            String name,
            String relation,
            String phone,
            String blood,
            String disease,
            String allergy,
            String address
    ) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("relation", relation);
        values.put("phone", phone);
        values.put("blood", blood);
        values.put("disease", disease);
        values.put("allergy", allergy);
        values.put("address", address);

        return getWritableDatabase().insert(
                "profiles",
                null,
                values
        );
    }

    public ArrayList<String> getProfileNames() {

        ArrayList<String> profiles = new ArrayList<>();

        profiles.add("Yo / usuario actual");
        profiles.add("Invitado / desconocido");

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT name, relation FROM profiles ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {

                String name = safe(cursor.getString(0));
                String relation = safe(cursor.getString(1));

                profiles.add(name + " (" + relation + ")");
            }
        }

        return profiles;
    }

    public ArrayList<String[]> getProfiles() {

        ArrayList<String[]> profiles = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id,name,relation,phone,blood,disease,allergy,address " +
                        "FROM profiles ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {

                profiles.add(new String[]{
                        String.valueOf(cursor.getInt(0)),
                        safe(cursor.getString(1)),
                        safe(cursor.getString(2)),
                        safe(cursor.getString(3)),
                        safe(cursor.getString(4)),
                        safe(cursor.getString(5)),
                        safe(cursor.getString(6)),
                        safe(cursor.getString(7))
                });
            }
        }

        return profiles;
    }

    public String[] getProfileById(int profileId) {

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id,name,relation,phone,blood,disease,allergy,address " +
                        "FROM profiles WHERE id = ?",
                new String[]{String.valueOf(profileId)}
        )) {

            if (cursor.moveToFirst()) {

                return new String[]{
                        String.valueOf(cursor.getInt(0)),
                        safe(cursor.getString(1)),
                        safe(cursor.getString(2)),
                        safe(cursor.getString(3)),
                        safe(cursor.getString(4)),
                        safe(cursor.getString(5)),
                        safe(cursor.getString(6)),
                        safe(cursor.getString(7))
                };
            }
        }

        return null;
    }

    public boolean updateProfile(
            int profileId,
            String name,
            String relation,
            String phone,
            String blood,
            String disease,
            String allergy,
            String address
    ) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("relation", relation);
        values.put("phone", phone);
        values.put("blood", blood);
        values.put("disease", disease);
        values.put("allergy", allergy);
        values.put("address", address);

        int rows = getWritableDatabase().update(
                "profiles",
                values,
                "id = ?",
                new String[]{String.valueOf(profileId)}
        );

        return rows > 0;
    }

    public boolean deleteProfile(int profileId) {

        int rows = getWritableDatabase().delete(
                "profiles",
                "id = ?",
                new String[]{String.valueOf(profileId)}
        );

        return rows > 0;
    }

    public String profilesText() {

        StringBuilder text = new StringBuilder();

        for (String[] profile : getProfiles()) {

            text.append("• ")
                    .append(profile[1])
                    .append(" · ")
                    .append(profile[2])
                    .append("\n");

            text.append("Teléfono: ")
                    .append(profile[3])
                    .append("\n");

            text.append("Sangre: ")
                    .append(profile[4])
                    .append("\n");

            text.append("Condición: ")
                    .append(profile[5])
                    .append("\n");

            text.append("Alergias: ")
                    .append(profile[6])
                    .append("\n");

            text.append("Dirección: ")
                    .append(profile[7])
                    .append("\n\n");
        }

        if (text.length() == 0) {
            return "Aún no hay familiares guardados.";
        }

        return text.toString();
    }

    public String[] getProfileDetails(String displayName) {

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT name,relation,phone,blood,disease,allergy,address " +
                        "FROM profiles ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {

                String name = safe(cursor.getString(0));
                String relation = safe(cursor.getString(1));

                if ((name + " (" + relation + ")").equals(displayName)) {

                    return new String[]{
                            name,
                            relation,
                            safe(cursor.getString(2)),
                            safe(cursor.getString(3)),
                            safe(cursor.getString(4)),
                            safe(cursor.getString(5)),
                            safe(cursor.getString(6))
                    };
                }
            }
        }

        return null;
    }

    public long addTicket(
            String service,
            String category,
            String person,
            String phone,
            String description,
            String address,
            String ambulance,
            String priority,
            String dispatch,
            int eta,
            double cost
    ) {

        ContentValues values = new ContentValues();

        values.put("service", service);
        values.put("category", category);
        values.put("person", person);
        values.put("phone", phone);
        values.put("description", description);
        values.put("address", address);
        values.put("ambulance_type", ambulance);
        values.put("priority", priority);
        values.put("dispatch", dispatch);
        values.put("status", "ACTIVO");
        values.put("eta", eta);
        values.put("cost", cost);

        return getWritableDatabase().insert(
                "tickets",
                null,
                values
        );
    }

    public boolean updateTicket(
            int ticketId,
            String service,
            String category,
            String person,
            String phone,
            String description,
            String address,
            String ambulance,
            String priority,
            String dispatch,
            int eta,
            double cost
    ) {

        ContentValues values = new ContentValues();

        values.put("service", service);
        values.put("category", category);
        values.put("person", person);
        values.put("phone", phone);
        values.put("description", description);
        values.put("address", address);
        values.put("ambulance_type", ambulance);
        values.put("priority", priority);
        values.put("dispatch", dispatch);
        values.put("eta", eta);
        values.put("cost", cost);

        int rows = getWritableDatabase().update(
                "tickets",
                values,
                "id = ?",
                new String[]{String.valueOf(ticketId)}
        );

        return rows > 0;
    }

    public String[] getTicketById(int ticketId) {

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT service,category,person,phone,description,address," +
                        "ambulance_type,priority,dispatch,status,eta,cost " +
                        "FROM tickets WHERE id = ?",
                new String[]{String.valueOf(ticketId)}
        )) {

            if (cursor.moveToFirst()) {

                return new String[]{
                        safe(cursor.getString(0)),
                        safe(cursor.getString(1)),
                        safe(cursor.getString(2)),
                        safe(cursor.getString(3)),
                        safe(cursor.getString(4)),
                        safe(cursor.getString(5)),
                        safe(cursor.getString(6)),
                        safe(cursor.getString(7)),
                        safe(cursor.getString(8)),
                        safe(cursor.getString(9)),
                        String.valueOf(cursor.getInt(10)),
                        String.valueOf(cursor.getDouble(11))
                };
            }
        }

        return null;
    }

    public ArrayList<String> getActiveTickets() {

        ArrayList<String> tickets = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id,service,category,person,phone,priority,status," +
                        "dispatch,eta,address FROM tickets " +
                        "WHERE status != 'CERRADO' ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {

                String ticket =
                        "Ticket #" + cursor.getInt(0) +
                                "\nEstado: " + safe(cursor.getString(6)) +
                                "\nServicio: " + safe(cursor.getString(1)) +
                                "\nCategoría: " + safe(cursor.getString(2)) +
                                "\nPara: " + safe(cursor.getString(3)) +
                                "\nTeléfono: " + safe(cursor.getString(4)) +
                                "\nPrioridad: " + safe(cursor.getString(5)) +
                                "\nDespacho: " + safe(cursor.getString(7)) +
                                "\nETA: " + cursor.getInt(8) + " min" +
                                "\nUbicación: " + safe(cursor.getString(9));

                tickets.add(ticket);
            }
        }

        return tickets;
    }

    public ArrayList<Integer> getActiveTicketIds() {

        ArrayList<Integer> ids = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM tickets " +
                        "WHERE status != 'CERRADO' ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        }

        return ids;
    }

    public ArrayList<String> getClosedTickets() {

        ArrayList<String> tickets = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id,service,category,person,phone,priority,status," +
                        "created_at,address FROM tickets " +
                        "WHERE status = 'CERRADO' ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {

                String ticket =
                        "✓ Ticket #" + cursor.getInt(0) +
                                "\nEstado: " + safe(cursor.getString(6)) +
                                "\nServicio: " + safe(cursor.getString(1)) +
                                "\nCategoría: " + safe(cursor.getString(2)) +
                                "\nPara: " + safe(cursor.getString(3)) +
                                "\nTeléfono: " + safe(cursor.getString(4)) +
                                "\nPrioridad: " + safe(cursor.getString(5)) +
                                "\nUbicación: " + safe(cursor.getString(8)) +
                                "\nFecha: " + safe(cursor.getString(7));

                tickets.add(ticket);
            }
        }

        return tickets;
    }

    public ArrayList<Integer> getClosedTicketIds() {

        ArrayList<Integer> ids = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM tickets " +
                        "WHERE status = 'CERRADO' ORDER BY id DESC",
                null
        )) {

            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        }

        return ids;
    }

    public void updateTicketStatus(int ticketId, String status) {

        ContentValues values = new ContentValues();
        values.put("status", status);

        getWritableDatabase().update(
                "tickets",
                values,
                "id = ?",
                new String[]{String.valueOf(ticketId)}
        );
    }

    public void closeTicket(int ticketId) {

        updateTicketStatus(ticketId, "CERRADO");
    }

    public int countActiveTickets() {

        int total = 0;

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM tickets " +
                        "WHERE status != 'CERRADO'",
                null
        )) {

            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        }

        return total;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
