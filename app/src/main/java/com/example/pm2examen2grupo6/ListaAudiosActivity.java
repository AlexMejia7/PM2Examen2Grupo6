package com.example.pm2examen2grupo6;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ListaAudiosActivity extends AppCompatActivity {

    // ① URL base apuntando al host de tu PC desde el emulador
    private static final String BASE_URL = "http://10.0.2.2/PM2E1Grupo6/";

    private SearchView svSearch;
    private ListView listView;
    private ArrayList<Audio> lista = new ArrayList<>();
    private ArrayList<Audio> listaCompleta = new ArrayList<>();
    private AudioAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private final String API_KEY = "ABC123TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_audios);

        // 1) Vincular vistas
        svSearch = findViewById(R.id.svSearch);
        listView = findViewById(R.id.listViewAudios);

        // 2) Inicializar adapter y listas
        adapter = new AudioAdapter(this, lista);
        listView.setAdapter(adapter);
        // copia inicial vacía; se llenará tras el primer GET
        listaCompleta.clear();

        // 3) Configurar filtro de búsqueda
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // no usamos submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String texto = newText.toLowerCase().trim();
                lista.clear();
                if (texto.isEmpty()) {
                    // restaurar todos
                    lista.addAll(listaCompleta);
                } else {
                    // filtrar
                    for (Audio a : listaCompleta) {
                        if (a.descripcion.toLowerCase().contains(texto)) {
                            lista.add(a);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        // 4) Cargar datos del servidor
        cargarAudios();
    }

    public void cargarAudios() {
        Request request = new Request.Builder()
                .url(BASE_URL + "get_all.php")
                .addHeader("API-Key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
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
                        // actualizar copia completa para filtros
                        listaCompleta.clear();
                        listaCompleta.addAll(lista);

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(ListaAudiosActivity.this,
                                        "Error parseando datos: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ListaAudiosActivity.this,
                                    "Error en respuesta: " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ListaAudiosActivity.this,
                                "Sin conexión", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void eliminarAudio(int id) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(
                obj.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request req = new Request.Builder()
                .url(BASE_URL + "delete.php")
                .addHeader("API-Key", API_KEY)
                .post(body)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ListaAudiosActivity.this,
                                "Eliminado", Toast.LENGTH_SHORT).show();
                        cargarAudios();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ListaAudiosActivity.this,
                                    "Error al eliminar: " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ListaAudiosActivity.this,
                                "Error al eliminar", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
