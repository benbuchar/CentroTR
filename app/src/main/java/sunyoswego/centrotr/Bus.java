package sunyoswego.centrotr;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Bus {

    GoogleMap map;
    BusTracker position;
    Marker theBus, theBusArrow;

    public Bus(GoogleMap map) {
        this.map = map;
        position = new BusTracker();
        theBusArrow = this.map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Bus")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.our_busarrow)));
        theBusArrow.setAnchor(0.5f, 0.5f);
        theBus = this.map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Bus")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.our_busicon)));
        theBus.setAnchor(0.5f, 0.5f);
    }

    public void track() {
        position.start();
    }

    private class BusTracker extends Thread {

        @Override
        public void run() {
            for (; ; ) {
                new XMLParser(map, theBus, theBusArrow, "").execute();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.i("Bus Thread", "Thread interrupted");
                    return;
                }
            }
        }
    }
}
