package com.example.services911;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.btnAmbulance)
                .setOnClickListener(v -> abrirTicket("Médica"));

        findViewById(R.id.btnPolice)
                .setOnClickListener(v -> abrirTicket("Policía"));

        findViewById(R.id.btnFire)
                .setOnClickListener(v -> abrirTicket("Bomberos"));

        Nav.setup(this);
    }

    private void abrirTicket(String servicio) {

        Intent intent = new Intent(
                MainActivity.this,
                TicketActivity.class
        );

        intent.putExtra("service", servicio);

        startActivity(intent);
    }
}
