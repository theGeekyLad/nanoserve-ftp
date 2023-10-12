package com.thegeekylad.wear_hotspot.presentation;

//import android.util.Log;
//
//import java.io.IOException;
//import java.util.Map;
//
//import fi.iki.elonen.NanoHTTPD;
//// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
////       instead of the above import use the following:
//// import org.nanohttpd.NanoHTTPD;
//
//public class NanoServer extends NanoHTTPD {
//
//    public NanoServer() throws IOException {
//        super(8080);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
//        Log.e("HTTP", "\nRunning! Point your browsers to http://localhost:8080/ \n");
//    }
//
//    public static void main(String[] args) {
//        try {
//            new NanoServer();
//        } catch (IOException ioe) {
//            Log.e("HTTP", "Couldn't start server:\n" + ioe);
//        }
//    }
//
//    @Override
//    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//        return newFixedLengthResponse(msg + "</body></html>\n");
//    }
//}
