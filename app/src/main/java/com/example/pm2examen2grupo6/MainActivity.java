package com.example.pm2examen2grupo6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // ① URL base apuntando al host de tu PC desde el emulador
    private static final String BASE_URL = "http://10.0.2.2/PM2E1Grupo6/";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;
    private static final int REQUEST_LOCATION_PERMISSION      = 202;
    private static final int REQUEST_STORAGE_PERMISSION       = 203;
    private static final int REQUEST_CAMERA_PERMISSION        = 204;
    private static final int REQUEST_CODE_CAMERA              = 101;

    private TextInputEditText etDescripcion;
    private Button btnGrabarAudio, btnDetenerAudio, btnTomarFoto,
            btnObtenerUbicacion, btnSubirDatos, btnVerAudios;
    private TextView tvUbicacion;
    private ImageView ivFoto;

    private MediaRecorder recorder;
    private String audioFilePath;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0, currentLng = 0.0;

    private Bitmap fotoBitmap;
    private String base64Foto = "";
    private String base64Audio = "";

    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias UI
        etDescripcion       = findViewById(R.id.etDescripcion);
        btnGrabarAudio      = findViewById(R.id.btnGrabarAudio);
        btnDetenerAudio     = findViewById(R.id.btnDetenerAudio);
        btnTomarFoto        = findViewById(R.id.btnTomarFoto);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        btnSubirDatos       = findViewById(R.id.btnSubirDatos);
        btnVerAudios        = findViewById(R.id.btnVerAudios);
        tvUbicacion         = findViewById(R.id.tvUbicacion);
        ivFoto              = findViewById(R.id.ivFoto);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Listeners con chequeo de permisos
        btnGrabarAudio.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                iniciarGrabacion();
            }
        });

        btnDetenerAudio.setOnClickListener(v -> detenerGrabacion());

        btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                tomarFoto();
            }
        });

        btnObtenerUbicacion.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                obtenerUbicacion();
            }
        });

        btnSubirDatos.setOnClickListener(v -> subirDatos());

        btnVerAudios.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ListaAudiosActivity.class))
        );

        btnDetenerAudio.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_RECORD_AUDIO_PERMISSION:
                    iniciarGrabacion();
                    break;
                case REQUEST_CAMERA_PERMISSION:
                    tomarFoto();
                    break;
                case REQUEST_LOCATION_PERMISSION:
                    obtenerUbicacion();
                    break;
            }
        } else {
            Toast.makeText(this, "Permiso denegado. No se puede continuar.", Toast.LENGTH_LONG).show();
        }
    }

    private void iniciarGrabacion() {
        try {
            audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/audioGrabado.3gp";
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(audioFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
            recorder.start();

            btnGrabarAudio.setEnabled(false);
            btnDetenerAudio.setEnabled(true);
            Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error al iniciar grabación: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void detenerGrabacion() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            btnGrabarAudio.setEnabled(true);
            btnDetenerAudio.setEnabled(false);
            Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
            base64Audio = audioFileToBase64(audioFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error al detener grabación: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String audioFileToBase64(String path) {
        try (FileInputStream fis = new FileInputStream(new File(path))) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void tomarFoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,       "Nueva Foto");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara");
        photoUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intentCamara, REQUEST_CODE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            try {
                fotoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                ivFoto.setImageBitmap(fotoBitmap);
                base64Foto = bitmapToBase64(fotoBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,
                        "Error al obtener foto: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        // Usamos getCurrentLocation para asegurar fix incluso sin cache previo
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.getToken()
        ).addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                tvUbicacion.setText("Lat: " + currentLat + ", Lng: " + currentLng);
            } else {
                Toast.makeText(this,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this,
                "Error al obtener ubicación: " + e.getMessage(),
                Toast.LENGTH_LONG).show()
        );
    }

    private void subirDatos() {
        String descripcion = etDescripcion.getText().toString().trim();
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        if (base64Audio.isEmpty()) {
            Toast.makeText(this, "Graba un audio antes de subir", Toast.LENGTH_SHORT).show();
            return;
        }
        if (base64Foto.isEmpty()) {
            Toast.makeText(this, "Toma una foto antes de subir", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentLat == 0.0 && currentLng == 0.0) {
            Toast.makeText(this,
                    "Obtén la ubicación antes de subir",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("descripcion", descripcion);
            jsonObject.put("audio", base64Audio);
            jsonObject.put("imagen", base64Foto);
            jsonObject.put("lat", currentLat);
            jsonObject.put("lng", currentLng);

            RequestBody body = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "create.php")
                    .addHeader("API-Key", "ABC123TOKEN")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respuesta = response.body().string();
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Respuesta: " + respuesta,
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error al crear JSON: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
