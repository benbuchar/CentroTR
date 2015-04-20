package sunyoswego.centrotr;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class BusRoute {

    String routeName;
    ArrayList<BusStop> busStops = new ArrayList<BusStop>();
    ArrayList<LatLng> routePoints = new ArrayList<LatLng>();

    public BusRoute(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteName() {
        return routeName;
    }

    public ArrayList<BusStop> getBusStops() {
        return busStops;
    }

    public ArrayList<LatLng> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(ArrayList<LatLng> routePoints) {
        this.routePoints = routePoints;
    }

    public void setBusStops(ArrayList<BusStop> busStops) {
        this.busStops = busStops;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    // a class to wrap one string & one map because THERE MUST BE ONLY ONE PARAMETER
    private class Wrapper {
        public String s;
        public GoogleMap map;

        public Wrapper(String s, GoogleMap map) {
            this.s = s;
            this.map = map;
        }
    }

    public void loadRoute(GoogleMap map) throws IOException {
        // Download the stops from a server (using Async)
        DownloadStopsTask downloadTask = new DownloadStopsTask();
        downloadTask.execute(map);
    }

    private class DownloadStopsTask extends AsyncTask<GoogleMap, Void, Wrapper> {
        @Override
        protected Wrapper doInBackground(GoogleMap... map) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl("http://www.oswego.edu/~hafner/Bus_stops.txt");
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return new Wrapper(data, map[0]);
        }

        @Override
        protected void onPostExecute(Wrapper w) {
            super.onPostExecute(w);
            Scanner sc = new Scanner(w.s);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] params = line.split(",");
                LatLng coordinates = new LatLng(Double.parseDouble(params[1]), Double.parseDouble(params[2]));
                busStops.add(new BusStop(params[0], coordinates));
            }
            for (BusStop stop : getBusStops()) {
                w.map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1))
                        .title(stop.getName())
                        .snippet("2*2")
                        .position(stop.getCoordinates()));
            }
        }
    }

    public class HttpConnection {
        public String readUrl(String xmlUrl) throws IOException {
            String data = "", color = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(xmlUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.length() > 4 && line.substring(0, 5).equals("route")) {
                        color = line.substring(6);
                    } else if (color.equals("blue")) {
                        sb.append(line + "\n");

                    }
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception!!", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            Log.d("DATA: ",data);
            return data;
        }
    }
}




























































//package sunyoswego.centrotr;
//
//import com.google.android.gms.maps.model.LatLng;
//
//import java.util.ArrayList;
//
//public class BusRoute {
//
//    String routeName;
//    ArrayList<BusStop> busStops = new ArrayList<BusStop>();
//    ArrayList<LatLng> routePoints = new ArrayList<LatLng>();;
//
//    public BusRoute(String routeName){
//        this.routeName = routeName;
//    }
//
//    public String getRouteName() {
//        return routeName;
//    }
//
//    public ArrayList<BusStop> getBusStops() {
//        return busStops;
//    }
//
//    public ArrayList<LatLng> getRoutePoints() {
//        return routePoints;
//    }
//
//    public void setRoutePoints(ArrayList<LatLng> routePoints) {
//        this.routePoints = routePoints;
//    }
//
//    public void setBusStops(ArrayList<BusStop> busStops) {
//        this.busStops = busStops;
//    }
//
//    public void setRouteName(String routeName) {
//        this.routeName = routeName;
//    }
//
//    public void loadRoute(){
//        loadBusStops();
//        loadRoutePoints();
//    }
//
//    public int getBusStopIndex(String busStopName) {
//        for(int i = 0; i < this.getBusStops().size(); i++) {
//            if(this.getBusStops().get(i).getName().equals(busStopName)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public void loadBusStops(){
//
//        if(routeName.equals("blueRoute")) {
//            //BLUE ROUTE BUS STOPS
//            busStops.add(new BusStop("Campus Center", new LatLng(43.453838, -76.540628),new LatLng(43.4539526, -76.5405475), 1));
//            busStops.add(new BusStop("Mackin", new LatLng(43.454804, -76.53475284576416),new LatLng(43.4549406, -76.5348609), 2));
//            busStops.add(new BusStop("Johnson", new LatLng(43.45713231914716, -76.53761744499207),new LatLng(43.4571612, -76.5372736), 3));
//            busStops.add(new BusStop("Library", new LatLng(43.45426628708711, -76.54450535774231),new LatLng(43.454331, -76.5434586), 4));
//            busStops.add(new BusStop("Mary Walker", new LatLng(43.455475, -76.542743),new LatLng(43.4554639, -76.5424972), 5));
//            busStops.add(new BusStop("Shineman", new LatLng(43.454282, -76.539160),new LatLng(43.4548819, -76.5390339), 6));
//            busStops.add(new BusStop("Village", new LatLng(43.44699935247679, -76.54906511306763),new LatLng(43.4469709, -76.5488651), 7));
//            busStops.add(new BusStop("Oneida", new LatLng(43.44964763983892, -76.55072271823883),new LatLng(43.44964763983892, -76.55072271823883), 8));
//            busStops.add(new BusStop("Rudolph Road Stop", new LatLng(43.45140790787459, -76.54988050460815),new LatLng(43.45140790787459, -76.54988050460815), 9));
//            busStops.add(new BusStop("Rudolph Road Stop", new LatLng(43.45266966140318, -76.54818534851074),new LatLng(43.45266966140318, -76.54818534851074), 10));
//            busStops.add(new BusStop("Library", new LatLng(43.4542507104548, -76.54474139213562),new LatLng(43.4542507104548, -76.54474139213562), 11));
//            busStops.add(new BusStop("Library", new LatLng(43.45430522865038, -76.543599),new LatLng(43.45430522865038, -76.543599), 12));
//            busStops.add(new BusStop("Waterbury", new LatLng(43.45654432636253, -76.54002070426941),new LatLng(43.45654432636253, -76.54002070426941), 13));
//
//
//
//        }
//        else if(routeName.equals("greenRoute")) {
//            //GREEN ROUTE BUS STOPS
//            busStops.add(new BusStop("Campus Center", new LatLng(43.453838, -76.540628),new LatLng(43.453838, -76.540628), 14));
//            busStops.add(new BusStop("Romney", new LatLng(43.447918, -76.534195),new LatLng(43.447918, -76.534195), 15));
//            busStops.add(new BusStop("Laker", new LatLng(43.446368, -76.53462409973145),new LatLng(43.446368, -76.53462409973145), 16));
//            busStops.add(new BusStop("Laker", new LatLng(43.44528569298516, -76.53571844100952),new LatLng(43.44528569298516, -76.53571844100952), 17));
//        }
//    }
//
//    public void loadRoutePoints() {
//
//        if(routeName.equals("blueRoute")) {
//            //BLUE ROUTE HIGHLIGHTING
//            routePoints.add(new LatLng(43.453838, -76.540628)); // CAMPUS_CENTER // Origin
//            routePoints.add(new LatLng(43.453838, -76.540628)); // CAMPUS_CENTER // Destination
//            routePoints.add(new LatLng(43.453523, -76.541181)); // CIRCLE
//            routePoints.add(new LatLng(43.457295865792744, -76.53929114341736)); // RIGGS_HALL
//            routePoints.add(new LatLng(43.450535, -76.549731)); // ONONDAGA
//            routePoints.add(new LatLng(43.44699935247679, -76.54906511306763)); // VILLAGE
//            routePoints.add(new LatLng(43.454309, -76.543996)); // PENFIELD_LIBRARY
//            routePoints.add(new LatLng(43.454282, -76.539160)); // SHINEMAN
//
//        } else if(routeName.equals("greenRoute")) {
//            //GREEN ROUTE HIGHLIGHTING
//            routePoints.add(new LatLng(43.453838, -76.540628)); // CAMPUS_CENTER
//            routePoints.add(new LatLng(43.453838, -76.540628)); // CAMPUS_CENTER
//            routePoints.add(new LatLng(43.446368, -76.53462409973145)); //LAKER
//            routePoints.add(new LatLng(43.44528569298516, -76.53548240661621)); //LAKER2
//            routePoints.add(new LatLng(43.44598674137218, -76.53653383255005)); //LAKER3
//            routePoints.add(new LatLng(43.44676567449565, -76.53561115264893)); //LAKER4
//            routePoints.add(new LatLng(43.4462048436578, -76.53455972671509)); //LAKER5
//            routePoints.add(new LatLng(43.44531685086377, -76.53541803359985)); //LAKER6
//            routePoints.add(new LatLng(43.447918, -76.534195)); //ROMNEY
//            routePoints.add(new LatLng(43.45357312306545, -76.53239250183105)); //FIFTHAVE
//
//
//
//        } else {
//            //return error
//        }
//    }
//}
