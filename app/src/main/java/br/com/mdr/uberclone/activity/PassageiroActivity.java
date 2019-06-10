package br.com.mdr.uberclone.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.helper.UsuarioFirebase;
import br.com.mdr.uberclone.model.Destino;
import br.com.mdr.uberclone.model.Requisicao;
import br.com.mdr.uberclone.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {
    private EditText edtDestino;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localizacao;
    private LinearLayout layoutDestino;
    private Button btnChamada;
    private boolean corridaRequisitada = false;
    private Requisicao requisicaoAtual;
    private Circle circle;
    private MarkerOptions userMarker, riderMarker, destinyMarker;
    private LatLngBounds.Builder bounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        iniciaComponentes();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(convertDpToPx(32), convertDpToPx(70), convertDpToPx(32), convertDpToPx(95));
        mMap.clear();
        recuperaLocalizacaoUsuario();
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao() {
        Usuario usuario = UsuarioFirebase.getUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes");
        Query qry = firebaseRef.orderByChild("passageiro/id")
                .equalTo(usuario.getId());
        qry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Requisicao> requisicoes = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    requisicoes.add(ds.getValue(Requisicao.class));
                }
                Collections.reverse(requisicoes);
                if (requisicoes.size() > 0) {
                    requisicaoAtual = requisicoes.get(0);
                    switch (requisicaoAtual.getStatus()) {
                        case Requisicao.STATUS_AGUARDANDO: {
                            layoutDestino.setVisibility(View.GONE);
                            btnChamada.setText("Cancelar Chamada");
                            corridaRequisitada = true;

                            break;
                        }
                        case Requisicao.STATUS_CAMINHO: {
                            layoutDestino.setVisibility(View.GONE);
                            btnChamada.setText("Cancelar Chamada");

//                            if (bounds == null)
//                                bounds = new LatLngBounds.Builder();
//
//                            LatLng userLoc = new LatLng(
//                                    Double.parseDouble(requisicaoAtual.getPassageiro().getLatitude()),
//                                    Double.parseDouble(requisicaoAtual.getPassageiro().getLongitude())
//                            );
//
//                            if (circle == null) {
//                                circle = mMap.addCircle(
//                                        new CircleOptions()
//                                                .center(userLoc)
//                                                .strokeWidth(2F)
//                                                .strokeColor(Color.parseColor("#CC3F9DE9"))
//                                                .fillColor(Color.parseColor("#663F9DE9"))
//                                                .radius(0.0));
//                            }
//
//                            if (userMarker == null) {
//                                userMarker = new MarkerOptions()
//                                        .position(userLoc)
//                                        .title(requisicaoAtual.getDestino().getRua())
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_my_loation));
//                                mMap.addMarker(userMarker);
//                                bounds.include(userLoc);
//                                circle.setCenter(userLoc);
//                            }
//
//                            LatLng motLoc = new LatLng(
//                                    Double.parseDouble(requisicaoAtual.getMotorista().getLatitude()),
//                                    Double.parseDouble(requisicaoAtual.getMotorista().getLongitude())
//                            );
//                            if (riderMarker == null) {
//                                riderMarker = new MarkerOptions()
//                                        .position(motLoc)
//                                        .title(requisicaoAtual.getMotorista().getNome())
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car));
//                                mMap.addMarker(riderMarker);
//                                bounds.include(motLoc);
//                                circle.setCenter(motLoc);
//                            }
//                            riderMarker.position(motLoc);
//                            animateCameraPosition();
                            break;
                        }
                    }
                    mostraCorridaNoMapa(requisicaoAtual);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mostraCorridaNoMapa(Requisicao requisicao) {
        mMap.clear();
        LatLng destLoc = new LatLng(
                Double.parseDouble(requisicao.getDestino().getLatitude()),
                Double.parseDouble(requisicao.getDestino().getLongitude()));

        localizacao = new LatLng(Double.parseDouble(requisicao.getPassageiro().getLatitude()),
                Double.parseDouble(requisicao.getPassageiro().getLongitude()));

        circle = mMap.addCircle(
                new CircleOptions()
                        .center(localizacao)
                        .strokeWidth(2F)
                        .strokeColor(Color.parseColor("#CC3F9DE9"))
                        .fillColor(Color.parseColor("#663F9DE9"))
                        .radius(0.0));
        circle.setCenter(localizacao);

        //if (userMarker == null) {
            userMarker = new MarkerOptions()
                    .position(localizacao)
                    .title(requisicao.getDestino().getRua())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_my_loation));
            mMap.addMarker(userMarker);
        //}
        userMarker.position(localizacao);

        //if (destinyMarker == null) {
            destinyMarker = new MarkerOptions()
                    .position(destLoc)
                    .title(requisicao.getDestino().getRua())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_destiny));
            mMap.addMarker(destinyMarker);
        //}
        destinyMarker.position(destLoc);

        circle.setCenter(destLoc);

        //if (bounds == null) {
            bounds = new LatLngBounds.Builder();
            bounds.include(localizacao).include(destLoc);
        //}

        if (requisicao.getMotorista() != null) {
            Usuario motorista = requisicaoAtual.getMotorista();
            LatLng motLoc = new LatLng(Double.parseDouble(motorista.getLatitude()),
                    Double.parseDouble(motorista.getLongitude()));
            //Adiciona o carrinho do motorista no mapa
            //if (riderMarker == null) {
                riderMarker = new MarkerOptions()
                        .position(motLoc)
                        .title(motorista.getNome())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car));
                mMap.addMarker(riderMarker);
                bounds.include(motLoc);
            //}
            riderMarker.position(motLoc);
            circle.setCenter(motLoc);
        }

        animateCameraPosition();
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 13));
    }

    private void animateCameraPosition() {
        int height = getResources().getDisplayMetrics().heightPixels;
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
        });
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

    public void chamaCorrida(View v) {
        if (!corridaRequisitada) {
            final String destino = edtDestino.getText().toString();
            if (!destino.isEmpty() || destino != null) {
                Address enderecoDestino = recuperaDestino(destino);

                if (enderecoDestino != null) {
                    final Destino lDestino = new Destino();
                    lDestino.setCidade(enderecoDestino.getSubAdminArea());
                    lDestino.setCep(enderecoDestino.getPostalCode());
                    lDestino.setBairro(enderecoDestino.getSubLocality());
                    lDestino.setRua(enderecoDestino.getThoroughfare());
                    lDestino.setNumero(enderecoDestino.getFeatureName());
                    lDestino.setLatitude(String.valueOf(enderecoDestino.getLatitude()));
                    lDestino.setLongitude(String.valueOf(enderecoDestino.getLongitude()));

                    //Concatena um array de string para criar apenas um ao final
                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Endereço: " + lDestino.getRua());
                    mensagem.append("\nBairro: " + lDestino.getBairro());
                    mensagem.append("\nNúmero: " + lDestino.getNumero());
                    mensagem.append("\nCep: " + lDestino.getCep());
                    mensagem.append("\nCidade: " + lDestino.getCidade());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    salvaRequisicao(lDestino);
                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
            } else {
                Toast.makeText(this, "Informe o endereço de destino.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Cancelar Requisição

        }
    }

    private void salvaRequisicao(Destino destino) {
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario passageiro = UsuarioFirebase.getUsuarioLogado();
        passageiro.setLatitude(String.valueOf(localizacao.latitude));
        passageiro.setLongitude(String.valueOf(localizacao.longitude));
        requisicao.setPassageiro(passageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        layoutDestino.setVisibility(View.GONE);
        btnChamada.setText("Cancelar Uber");
        corridaRequisitada = true;

        //Adiciona um marcador no destino e centraliza a camera mostrando
        // o local do usuário e seu destino
        LatLng locDestino = new LatLng(Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude()));
        MarkerOptions markerDestino = new MarkerOptions()
                .position(locDestino)
                .title(edtDestino.getText().toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_destiny));
        mMap.addMarker(markerDestino);
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(localizacao).include(locDestino);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 16));
    }

    private Address recuperaDestino(String enderecoDestino) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> enderecos = geocoder.getFromLocationName(enderecoDestino, 1);
            if (enderecos != null && enderecos.size() > 0)
                return enderecos.get(0);

        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void recuperaLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!corridaRequisitada) {
                    Double lat = location.getLatitude();
                    Double lon = location.getLongitude();
                    localizacao = new LatLng(lat, lon);

                    mMap.clear();
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(localizacao)
                                    .title("Meu Local")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_my_loation))
                    );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacao, 16));
                }

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
                    10000,
                    10,
                    locationListener
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sair: {
                FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
                auth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void iniciaComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar uma viagem");
        setSupportActionBar(toolbar);

        edtDestino = findViewById(R.id.edtDestino);
        layoutDestino = findViewById(R.id.layoutDestino);
        layoutDestino.setVisibility(View.VISIBLE);
        btnChamada = findViewById(R.id.btnChamaCorrida);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private int convertDpToPx(int dp) {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}
