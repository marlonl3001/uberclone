package br.com.mdr.uberclone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.model.Requisicao;
import br.com.mdr.uberclone.model.Usuario;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button btnAceitaCorrida;
    private Requisicao requisicao;
    private Usuario motorista;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng driverLoc;
    private Boolean corridaAceita = false;
    private Marker riderMarker;
    private Marker driverMarker;
    private LatLngBounds.Builder bounds;
    private Circle circle;
    private boolean corridaIniciada = false;

    /*
    * Coordenadas para teste
    * Motorista:
    *   -25.430582, -49.273139
    *   -25.431597, -49.272665
    *   -25.432454, -49.272240
    *   -25.433424, -49.271791
    *   -25.432725, -49.269993
    *   -25.431281, -49.266247
    *   -25.430679, -49.264724
    *   -25.429658, -49.265385
    *
    * Usuario:
    *   -25.429658, -49.265573
    * Destino Usuário:
    *   -25.4381052,-49.2687489 (Av. Sete de Setembro, 2775)
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        Intent i = getIntent();
        if (i != null) {
            motorista = (Usuario) i.getSerializableExtra("motorista");
            requisicao = (Requisicao) i.getSerializableExtra("requisicao");
            corridaIniciada = i.getBooleanExtra("corridaIniciada", false);

            if (requisicao.getMotorista() != null)
                motorista = requisicao.getMotorista();
            driverLoc = new LatLng(Double.parseDouble(motorista.getLatitude()),
                    Double.parseDouble(motorista.getLongitude()));
        }
        iniciaComponentes();
    }

    public void aceitarCorrida(View v) {
        corridaAceita = true;
        requisicao.setMotorista(motorista);
        requisicao.setStatus(Requisicao.STATUS_CAMINHO);
        requisicao.atualizar();

        monitoraRequisicao();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 56, 0, 0);
        recuperaLocalizacaoUsuario();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (corridaIniciada) {
            Toast.makeText(this, "Necessário cancelar corrida.", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(this, RequisicoesActivity.class));
        }
        return false;
    }

    private void monitoraRequisicao() {
        DatabaseReference reqRef = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes")
                .child(requisicao.getId());
        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requisicao = dataSnapshot.getValue(Requisicao.class);
                switch (requisicao.getStatus()) {
                    case Requisicao.STATUS_AGUARDANDO: {
                        btnAceitaCorrida.setVisibility(View.VISIBLE);
                        btnAceitaCorrida.setText("Aceitar Corrida");
                        break;
                    }
                    case Requisicao.STATUS_CAMINHO: {
                        motoristaACaminho();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void motoristaACaminho() {
        btnAceitaCorrida.setVisibility(View.GONE);

        if (driverMarker != null)
            driverMarker.remove();

        //Mostra marcador do motorista
        driverLoc = new LatLng(Double.parseDouble(motorista.getLatitude()),
                Double.parseDouble(motorista.getLongitude()));
        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLoc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));

        //Mostra marcador do passageiro
        if (riderMarker != null)
            riderMarker.remove();

        Usuario passageiro = requisicao.getPassageiro();
        LatLng userLoc = new LatLng(Double.parseDouble(passageiro.getLatitude()),
                Double.parseDouble(passageiro.getLongitude()));
        riderMarker = mMap.addMarker(new MarkerOptions()
                .position(userLoc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location)));

        if (bounds == null)
            bounds = new LatLngBounds.Builder();

        bounds.include(driverLoc).include(userLoc);

        animateCameraPosition(bounds.build());

        /*circle = mMap.addCircle(
                new CircleOptions()
                        .center(driverLoc)
                        .strokeWidth(2F)
                        .strokeColor(Color.parseColor("#CC3F9DE9"))
                        .fillColor(Color.parseColor("#663F9DE9"))
                        .radius(0.0));
        circle.setCenter(driverLoc);
        circle.setCenter(userLoc);*/

        //animateCameraPosition();
    }

    private void animateCameraPosition(LatLngBounds bounds) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.1);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        /*int height = getResources().getDisplayMetrics().heightPixels;
        int width = getResources().getDisplayMetrics().widthPixels;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(),
                width, height, 17);

        mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                final float zoom = getZoomLevel(circle);
                float currentZoom = mMap.getCameraPosition().zoom;
                if (currentZoom > zoom && zoom != 0) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                        }
                    }, 100);
                }
            }

            @Override
            public void onCancel() {

            }
        });*/
    }

    private Float getZoomLevel(Circle circle) {
        int zoomLevel = 0;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 500;

            if (radius == 0f)
                return 0f;

            zoomLevel = (int)(16 - Math.log(scale) / Math.log(2.0));
        }

        return zoomLevel - .5f;
    }

    private void recuperaLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Double lat = location.getLatitude();
                Double lon = location.getLongitude();
                driverLoc = new LatLng(lat, lon);
                motorista.setLatitude(String.valueOf(lat));
                motorista.setLongitude(String.valueOf(lon));

                if (driverMarker != null)
                    driverMarker.remove();

                driverMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(driverLoc)
                                    .title("Meu Local")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));

                if (corridaAceita) {
                    requisicao.setMotorista(motorista);
                    requisicao.atualizar();
                    motoristaACaminho();
                } else
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLoc, 16));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    10,
                    locationListener
            );
        }
    }

    private void iniciaComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!corridaIniciada);
        String title = "";
        switch (requisicao.getStatus()) {
            case Requisicao.STATUS_AGUARDANDO: {
                title = "Iniciar Corrida";
                break;
            }
            case Requisicao.STATUS_CAMINHO: {
                title = "A caminho";
                break;
            }
            case Requisicao.STATUS_VIAGEM_INICIO: {
                title = "Viagem Iniciada";
                break;
            }
        }
        getSupportActionBar().setTitle(title);

        btnAceitaCorrida = findViewById(R.id.btnAceitaCorrida);
        if (requisicao != null && requisicao.getMotorista() != null) {
            btnAceitaCorrida.setVisibility(View.GONE);
            corridaAceita = true;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
