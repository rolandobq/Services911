
package com.example.services911;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class Nav {

    public static void setup(Activity activity) {

        View btnMap = activity.findViewById(R.id.btnNavMap);
        View btnTicket = activity.findViewById(R.id.btnNavTicket);
        View btnProfile = activity.findViewById(R.id.btnNavProfile);

        if (btnMap == null || btnTicket == null || btnProfile == null) {
            return;
        }

        btnMap.setOnClickListener(v ->
                navegar(activity, MapActivity.class)
        );

        btnTicket.setOnClickListener(v ->
                navegar(activity, ActiveTicketActivity.class)
        );

        btnProfile.setOnClickListener(v ->
                navegar(activity, ProfileActivity.class)
        );

        btnMap.setBackground(null);
        btnTicket.setBackground(null);
        btnProfile.setBackground(null);

        if (activity instanceof MapActivity) {

            btnMap.setBackgroundResource(
                    R.drawable.nav_item_selected
            );

        } else if (activity instanceof ActiveTicketActivity ||
                activity instanceof TicketActivity ||
                activity instanceof TicketDetailActivity) {

            btnTicket.setBackgroundResource(
                    R.drawable.nav_item_selected
            );

        } else if (activity instanceof ProfileActivity) {

            btnProfile.setBackgroundResource(
                    R.drawable.nav_item_selected
            );
        }
    }

    private static void navegar(
            Activity activity,
            Class<? extends Activity> destino
    ) {

        if (destino.isInstance(activity)) {
            return;
        }

        Intent intent = new Intent(activity, destino);

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        activity.startActivity(intent);
    }
}
