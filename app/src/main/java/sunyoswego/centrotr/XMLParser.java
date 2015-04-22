package sunyoswego.centrotr;


import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLParser extends AsyncTask<String, Void, String> {

    final String urlBlueRoute = "http://moxie.cs.oswego.edu/~osubus/blueRouteVehicle.xml";
    GoogleMap map;
    Document doc;
    String url;
    Marker vehicleMarker, vehicleArrow;

    //XML Data
    int id;
    double lat, lon;

    String vehicleName;
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;
    float direction;

    public XMLParser(GoogleMap mMap, Marker vehicleMarker, Marker vehicleArrow, String vehicleName) {
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            this.vehicleMarker = vehicleMarker;
            this.vehicleName = vehicleName;
            this.vehicleArrow = vehicleArrow;
            map = mMap;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String...urls) {
        parse();
        return "Executed";
    }

    private void parse(){
        try {
            url = urlBlueRoute;
            doc = dBuilder.parse(new URL(url).openStream());
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("busResponse");
            Node nNode = nList.item(0);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                direction = Float.parseFloat(eElement.getElementsByTagName("hdg").item(0).getTextContent().trim());

                id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent().trim());

                lat = Double.parseDouble(eElement.getElementsByTagName("lat").item(0).getTextContent().trim());

                lon = Double.parseDouble(eElement.getElementsByTagName("lon").item(0).getTextContent().trim());
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        vehicleMarker.setPosition(new LatLng(lat, lon));
        vehicleArrow.setPosition(new LatLng(lat, lon));
        vehicleArrow.setRotation(direction);
    }
}
