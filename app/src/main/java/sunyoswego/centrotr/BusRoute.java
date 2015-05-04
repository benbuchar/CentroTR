package sunyoswego.centrotr;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class BusRoute {

    String routeName, waypoints;
    int routeColor;
    ArrayList<BusStop> busStops = new ArrayList<>();
    ArrayList<LatLng> routePoints = new ArrayList<>();

    public BusRoute() {
        this.routeName = "blue";
        routeColor = Color.BLUE;
        waypoints = "43.454658,-76.543228|43.447010,-76.549129|43.450234,-76.549708";
    }

    public ArrayList<BusStop> getBusStops() {
        return busStops;
    }

//    public ArrayList<LatLng> getRoutePoints() {
//        return routePoints;
//    }

    public void change(GoogleMap map, String requestedRoute) throws IOException {
        if (!routeName.equals(requestedRoute)) {
            routeName = requestedRoute;
            if (requestedRoute.equals("blue")) {
                routeColor = Color.BLUE;
                waypoints = "43.454658,-76.543228|43.447010,-76.549129|43.450234,-76.549708";
            } else if (requestedRoute.equals("green")) {
                routeColor = Color.GREEN;
                waypoints = "43.453242,-76.532441|43.448449,-76.534237|43.446447,-76.536587|43.446205,-76.534613|43.448449,-76.534237|43.453242,-76.532441";
            }
            map.clear();
            loadRoute(map);
        }
    }

    private class Wrapper {
        public String s;
        public GoogleMap map;
        public List<List<HashMap<String,String>>> routes;

        public Wrapper(String s, GoogleMap map) {
            this.s = s;
            this.map = map;
        }

        public Wrapper(List<List<HashMap<String,String>>> routes, GoogleMap map) {
            this.routes = routes;
            this.map = map;
        }
    }

    public void loadRoute(GoogleMap map) throws IOException {
        busStops = new ArrayList<>();
        // Download the stops from a server (using Async)
        DownloadStopsTask downloadTask = new DownloadStopsTask();
        downloadTask.execute(map);
    }

    private class DownloadStopsTask extends AsyncTask<GoogleMap, Void, Wrapper> {
        @Override
        protected Wrapper doInBackground(GoogleMap... map) {
            String data = "";
            try {
                ConnectionForStops http = new ConnectionForStops();
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
                        .position(stop.getCoordinates()));
            }
            String url = getMapsApiDirectionsUrl();
            ReadTask downloadTask = new ReadTask();
            downloadTask.execute(new Wrapper(url, w.map));
        }
    }

    public class ConnectionForStops {
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
                    } else if (color.equals(routeName)) {
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



    private String getMapsApiDirectionsUrl() {
        // Building the parameters to the web service
        String params = "origin=43.453599,-76.541275&destination=43.453498,-76.541211&sensor=false&waypoints="+waypoints;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + params;
        return url;
    }

    private class ReadTask extends AsyncTask<Wrapper, Void, Wrapper> {
        @Override
        protected Wrapper doInBackground(Wrapper... w) {
            String data = "";
            try {
                ConnectionForDirections http = new ConnectionForDirections();
                data = http.readUrl(w[0].s);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return new Wrapper(data, w[0].map);
        }

        @Override
        protected void onPostExecute(Wrapper w) {
            super.onPostExecute(w);
            new ParserTask().execute(w);
        }
    }

    private class ParserTask extends AsyncTask<Wrapper, Integer, Wrapper> {

        @Override
        protected Wrapper doInBackground(Wrapper... w) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(w[0].s);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Wrapper(routes,w[0].map);
        }

        @Override
        protected void onPostExecute(Wrapper r) {
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            List<List<HashMap<String, String>>> routes = r.routes;
            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(6);
                polyLineOptions.color(routeColor);
            }
            r.map.addPolyline(polyLineOptions);
        }
    }

    public class ConnectionForDirections {
        public String readUrl(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception!!", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                    .get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
            return routes;
        }

        /**
         * Method Courtesy :
         * jeffreysambells.com/2010/05/27
         * /decoding-polylines-from-google-maps-direction-api-with-java
         * */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
}