package com.example.pm2examen2grupo6;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioAdapter extends ArrayAdapter<Audio> {

    private static final String BASE_URL = "http://10.0.2.2/PM2E1Grupo6/";
    private static final String API_KEY  = "ABC123TOKEN";

    private final Context context;
    private final List<Audio> audios;

    public AudioAdapter(Context context, List<Audio> audios) {
        super(context, R.layout.item_audio, audios);
        this.context = context;
        this.audios  = audios;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View item = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false);

        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        ImageView ivImagen     = item.findViewById(R.id.ivImagen);
        TextView tvLatLng      = item.findViewById(R.id.tvLatLng);
        Button   btnPlay       = item.findViewById(R.id.btnPlay);
        Button   btnEdit       = item.findViewById(R.id.btnEdit);
        Button   btnMap        = item.findViewById(R.id.btnMap);
        Button   btnEliminar   = item.findViewById(R.id.btnEliminar);

        Audio audio = audios.get(position);

        tvDescripcion.setText(audio.descripcion);
        tvLatLng.setText("Lat: " + audio.lat + " | Lng: " + audio.lng);

        if (audio.imagen != null && audio.imagen.contains(",")) {
            try {
                String base64 = audio.imagen.split(",", 2)[1];
                byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
                ivImagen.setImageBitmap(
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                );
                ivImagen.setVisibility(View.VISIBLE);
            } catch (Exception ignored) { }
        }

        /* reproducir */
        btnPlay.setOnClickListener(v -> {
            try {
                String dataStr = audio.audio.contains(",")
                        ? audio.audio.split(",", 2)[1]
                        : audio.audio;

                byte[] data = Base64.decode(dataStr, Base64.DEFAULT);
                File temp   = File.createTempFile("aud", "3gp", context.getCacheDir());
                try (FileOutputStream fos = new FileOutputStream(temp)) {
                    fos.write(data);
                }

                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(temp.getAbsolutePath());
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                Toast.makeText(context, "Error al reproducir audio", Toast.LENGTH_SHORT).show();
            }
        });

        /* editar descripción */
        btnEdit.setOnClickListener(v -> {
            TextInputEditText input = new TextInputEditText(context);
            input.setText(audio.descripcion);

            new AlertDialog.Builder(context)
                    .setTitle("Editar descripción")
                    .setView(input)
                    .setPositiveButton("Guardar", (d, w) -> {
                        try {
                            String nueva = input.getText().toString().trim();
                            JSONObject obj = new JSONObject();
                            obj.put("id", audio.id);
                            obj.put("descripcion", nueva);

                            RequestBody body = RequestBody.create(
                                    obj.toString(),
                                    MediaType.parse("application/json; charset=utf-8")
                            );

                            new OkHttpClient().newCall(
                                    new Request.Builder()
                                            .url(BASE_URL + "update.php")
                                            .addHeader("API-Key", API_KEY)
                                            .post(body)
                                            .build()
                            ).enqueue(new Callback() {
                                @Override public void onFailure(Call c, IOException e) {
                                    ((AppCompatActivity) context).runOnUiThread(() ->
                                            Toast.makeText(context,
                                                    "Error al actualizar: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show());
                                }
                                @Override public void onResponse(Call c, Response r) throws IOException {
                                    String resp = r.body().string();
                                    ((AppCompatActivity) context).runOnUiThread(() ->
                                            Toast.makeText(context,
                                                    "Update response: " + resp,
                                                    Toast.LENGTH_LONG).show());
                                    ((ListaAudiosActivity) context).cargarAudios();
                                }
                            });
                        } catch (Exception ex) {
                            Toast.makeText(context, "JSON inválido", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        /* abrir MapActivity */
        btnMap.setOnClickListener(v -> {
            Intent i = new Intent(context, MapActivity.class);
            i.putExtra("lat", audio.lat);
            i.putExtra("lng", audio.lng);
            context.startActivity(i);
        });

        /* eliminar */
        btnEliminar.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle("¿Eliminar audio?")
                        .setMessage("¿Deseas eliminar este registro?")
                        .setPositiveButton("Sí", (dlg, w) ->
                                ((ListaAudiosActivity) context).eliminarAudio(audio.id))
                        .setNegativeButton("No", null)
                        .show()
        );

        return item;
    }
}
