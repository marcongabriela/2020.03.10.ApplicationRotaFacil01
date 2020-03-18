package com.example.a20200310applicationrotafacil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GPS_REQUEST_CODE = 1001;
    private Button permitirAcesso;
    private Button ligarGPS;
    private Button desligarGPS;
    private Button inicioCaminho;
    private Button fimCaminho;

    private TextView labelPercorrida;
    private TextView tempoTotal;
    private EditText EditText;
    private FloatingActionButton botaoPesquisa;
    private Location anterior;
    double latitude;
    double longitude;
    Location atual = new Location("atual");
    Intent intent;

    private float distPercorrida = 0f;
    private int tickCounter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Chronometer chronometer;
    Context context;
    boolean GpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permitirAcesso = (Button)findViewById( R.id.permitirAcesso );
        ligarGPS = (Button)findViewById( R.id.ligarGPS );
        desligarGPS = (Button)findViewById( R.id.desligarGPS );
        inicioCaminho = (Button)findViewById( R.id.inicioCaminho );
        fimCaminho = (Button)findViewById( R.id.fimCaminho );

        labelPercorrida = (TextView)findViewById( R.id.labelPercorrida );
        chronometer = (Chronometer)findViewById(R.id.cronometro);
        EditText = (EditText)findViewById( R.id.EditText );
        botaoPesquisa = (FloatingActionButton)findViewById( R.id.botaoPesquisa );

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location){
                atual.setLatitude(location.getLatitude());
                atual.setLongitude(location.getLongitude());
                try {
                    labelPercorrida.setText(String.format("+" + atual.distanceTo(anterior)));
                } catch ( Exception e) {}

                String exibir = String.format(distPercorrida+" m");

                labelPercorrida.setText(exibir);
                try {
                    anterior.setLatitude(atual.getLatitude());
                    anterior.setLongitude(atual.getLongitude());
                } catch ( Exception e) {}
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}



        };

        permitirAcesso.setOnClickListener( this);
        ligarGPS.setOnClickListener( this);
        desligarGPS.setOnClickListener( this);
        inicioCaminho.setOnClickListener( this);
        fimCaminho.setOnClickListener( this);
        botaoPesquisa.setOnClickListener( this);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            permitirAcesso.setClickable(false);
            permitirAcesso.setTextColor(this.getResources().getColor(R.color.disabled_color));
        }
    }

    public void onClick(View v) {
        if (v == permitirAcesso) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            {
                permitirAcesso.setClickable(false);
                permitirAcesso.setTextColor(this.getResources().getColor(R.color.disabled_color));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        GPS_REQUEST_CODE);
            }
        } else if (v == ligarGPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, getString(R.string.gpsLigado), Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            } else {
                Toast.makeText(this, getString(R.string.permissaoNecessaria), Toast.LENGTH_SHORT).show();
            }

        } else if ( v == desligarGPS ) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else {
                Toast.makeText(this, getString(R.string.gpsDesligado), Toast.LENGTH_SHORT).show();
            }
        } else if ( v == inicioCaminho ) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, locationListener);
                labelPercorrida.setText("0m");
                long systemCurrTime = SystemClock.elapsedRealtime();
                chronometer.setBase(systemCurrTime);
                chronometer.start();
            } else {
                Toast.makeText(this, getString(R.string.gpsDesativado), Toast.LENGTH_SHORT).show();
            }
        } else if ( v == fimCaminho ) {
            locationManager.removeUpdates(locationListener);
            chronometer.stop();
            distPercorrida = 0f;

        } else if ( v == botaoPesquisa ) {
            Uri uri = Uri.parse(String.format(Locale.getDefault(), "geo:%f,%f?q="
                    + String.format(String.valueOf(EditText.getText())), atual.getLatitude(), atual.getLongitude())
            );
            Intent intent = new Intent (Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == GPS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    );
                    Toast.makeText(this, getString(R.string.permissao_concedida), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}


