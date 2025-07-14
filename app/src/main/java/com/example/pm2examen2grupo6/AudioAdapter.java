package com.example.pm2examen2grupo6;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AudioAdapter extends ArrayAdapter<Audio> {
    private final Context context;
    private final List<Audio> audios;

    public AudioAdapter(Context context, List<Audio> audios) {
        super(context, R.layout.item_audio, audios);
        this.context = context;
        this.audios = audios;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View item = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false);

        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        ImageView ivImagen = item.findViewById(R.id.ivImagen);
        TextView tvLatLng = item.findViewById(R.id.tvLatLng);
        Button btnEliminar = item.findViewById(R.id.btnEliminar);

        Audio audio = audios.get(position);
        tvDescripcion.setText(audio.descripcion);
        tvLatLng.setText("Lat: " + audio.lat + " | Lng: " + audio.lng);

        try {
            if (audio.imagen != null && audio.imagen.contains(",")) {
                String base64 = audio.imagen.split(",")[1];
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                ivImagen.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        } catch (Exception ignored) {}

        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar audio?")
                    .setMessage("¿Deseas eliminar este registro?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        ((ListaAudiosActivity) context).eliminarAudio(audio.id);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return item;
    }
}
