package sunyoswego.centrotr;

        import android.graphics.Color;
        import android.os.AsyncTask;
        import android.util.Log;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.android.gms.maps.model.PolylineOptions;

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

public class RouteHighlighter {
    ArrayList<LatLng> markerPoints;
    boolean green; //used to tell the lineOptions to make the green route line green
    public GoogleMap map;

    public RouteHighlighter(GoogleMap m){
        markerPoints = new ArrayList<LatLng>();
        green = false;
        this.map = m;
    }

    //give it the app map, the route you want to highlight and a boolean for whether the highlight should be green or not(blue)
    public void enableRoute(BusRoute route, boolean g) {

        green = g;

        // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
        // Up to 8 waypoints are allowed in a query for non-business users
        if (markerPoints.size() >= 10) {
            return;
        }
        for(int i=0; i < route.getRoutePoints().size(); i++) {
            markerPoints.add(route.getRoutePoints().get(i));
        }

        for(int i = 0; i < route.getBusStops().size(); i++) {
            // Add new marker to the Google Map Android API V2
            map.addMarker(new MarkerOptions()
                    .position(route.getBusStops().get(i).getCoordinates())
                    .title(route.getBusStops().get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_share)))
                    .setSnippet("Click to set notification");
        }

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED and
         * for the rest of markers, the color is AZURE
         */
        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                //   waypoints = "waypoints=optimize:false|";
                waypoints = "waypoints=";
            waypoints +=  point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Error downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            PolylineOptions lineOptions_back = null;

            lineOptions = new PolylineOptions();
            lineOptions_back = new PolylineOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                //lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(6);

                lineOptions_back.addAll(points);
                lineOptions_back.width(11);
                lineOptions_back.color(Color.GRAY);
                //lineOptions.geodesic(true);

                if(green){
                    lineOptions.color(0xFF75A800); //green
                }else {
                    lineOptions.color(0xFF24ADDE); // blue
                }
            }


            if(!green)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.453838, -76.540628), (float) 14.5));
            else
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.447918, -76.534195), (float) 14.5));

            // Drawing polyline in the Google Map for the i-th route

            map.addPolyline(lineOptions_back);
            map.addPolyline(lineOptions);


        }
    }
}