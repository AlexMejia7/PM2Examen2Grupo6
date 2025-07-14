package com.example.pm2examen2grupo6;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.*;

public class ListaAudiosActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Audio> lista = new ArrayList<>();
    private AudioAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private final String API_KEY = "ABC123TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_audios);

        listView = findViewById(R.id.listViewAudios);
        adapter = new AudioAdapter(this, lista);
        listView.setAdapter(adapter);

        cargarAudios();
    }

    private void cargarAudios() {
        Request request = new Request.Builder()
                .url("http://192.168.33.202/PM2E1Grupo6/get_all.php")
                .addHeader("API-Key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        lista.clear();
                        String body = response.body().string();
                        JSONObject obj = new JSONObject(body);
                        JSONArray arr = obj.getJSONArray("data");

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject reg = arr.getJSONObject(i);
                            Audio a = new Audio();
                            a.id = reg.getInt("id");
                            a.descripcion = reg.getString("descripcion");
                            a.audio = reg.getString("audio");
                            a.imagen = reg.getString("imagen");
                            a.lat = reg.getDouble("lat");
                            a.lng = reg.getDouble("lng");
                            lista.add(a);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ListaAudiosActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ListaAudiosActivity.this, "Sin conexión", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void eliminarAudio(int id) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(obj.toString(), MediaType.parse("application/json"));
        Request req = new Request.Builder()
                .url("http://192.168.33.202/PM2E1Grupo6/delete.php")
                .addHeader("API-Key", API_KEY)
                .post(body)
                .build();

        client.newCall(req).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ListaAudiosActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                        cargarAudios(); // recargar
                    });
                }
            }

            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ListaAudiosActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
