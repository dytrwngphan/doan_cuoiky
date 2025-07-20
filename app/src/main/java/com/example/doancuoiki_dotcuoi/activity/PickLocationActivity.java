    package com.example.doancuoiki_dotcuoi.activity;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.StrictMode;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import com.example.doancuoiki_dotcuoi.R;

    import org.json.JSONArray;
    import org.json.JSONObject;
    import org.osmdroid.config.Configuration;
    import org.osmdroid.util.GeoPoint;
    import org.osmdroid.views.MapView;
    import org.osmdroid.views.overlay.Marker;
    import org.osmdroid.views.overlay.MapEventsOverlay;
    import org.osmdroid.events.MapEventsReceiver;

    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.URL;

    public class PickLocationActivity extends AppCompatActivity {
        private MapView map;
        private GeoPoint pickedPoint = null;
        private Marker marker;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Cho phép network trên main thread (demo, không dùng khi production)
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Configuration.getInstance().setUserAgentValue(getPackageName());
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_pick_location);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            map = findViewById(R.id.map);
            map.setMultiTouchControls(true);
            map.getController().setZoom(16.0);
            map.getController().setCenter(new GeoPoint(21.028511, 105.804817)); // Hà Nội

            marker = null;

            // Bắt sự kiện tap map
            MapEventsOverlay overlay = new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    setMarkerAt(p);
                    return true;
                }
                @Override public boolean longPressHelper(GeoPoint p) { return false; }
            });
            map.getOverlays().add(overlay);

            // Xử lý nút chọn vị trí
            Button btnSelect = findViewById(R.id.btnSelectLocation);
            btnSelect.setOnClickListener(v -> {
                if (pickedPoint != null) {
                    Intent result = new Intent();
                    result.putExtra("lat", pickedPoint.getLatitude());
                    result.putExtra("lng", pickedPoint.getLongitude());
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Toast.makeText(this, "Hãy chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
                }
            });

            // Xử lý tìm kiếm địa chỉ
            EditText edtSearch = findViewById(R.id.edtSearch);
            Button btnSearch = findViewById(R.id.btnSearch);

            btnSearch.setOnClickListener(view -> {
                String address = edtSearch.getText().toString().trim();
                if (address.isEmpty()) {
                    Toast.makeText(this, "Nhập địa chỉ để tìm kiếm", Toast.LENGTH_SHORT).show();
                    return;
                }
                GeoPoint geo = searchLocation(address);
                if (geo != null) {
                    map.getController().setZoom(18.0);
                    map.getController().setCenter(geo);
                    setMarkerAt(geo);
                } else {
                    Toast.makeText(this, "Không tìm thấy địa chỉ!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Hàm đặt marker lên map
        private void setMarkerAt(GeoPoint point) {
            if (marker != null) map.getOverlays().remove(marker);
            marker = new Marker(map);
            marker.setPosition(point);
            marker.setTitle("Vị trí đã chọn");
            map.getOverlays().add(marker);
            pickedPoint = point;
            map.invalidate();
        }

        // Hàm gọi Nominatim (OpenStreetMap) để tìm lat/lng theo địa chỉ
        private GeoPoint searchLocation(String address) {
            try {
                String urlStr = "https://nominatim.openstreetmap.org/search?format=json&q=" + address.replace(" ", "%20");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible)");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JSONArray arr = new JSONArray(response.toString());
                if (arr.length() > 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    return new GeoPoint(lat, lon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
