package br.com.mdr.uberclone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.adapter.RequisicoesAdapter;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.helper.RecyclerItemClickListener;
import br.com.mdr.uberclone.helper.UsuarioFirebase;
import br.com.mdr.uberclone.model.Requisicao;
import br.com.mdr.uberclone.model.Usuario;

public class RequisicoesActivity extends AppCompatActivity {
    private RecyclerView recyclerRequisicoes;
    private TextView txtAguardando;
    private DatabaseReference databaseReference;
    private List<Requisicao> requisicoes = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motorista;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicao);

        iniciaComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatusRequisicoes();
    }

    private void iniciaComponentes() {
        motorista = UsuarioFirebase.getUsuarioLogado();
        getSupportActionBar().setTitle("Requisições");
        adapter = new RequisicoesAdapter(requisicoes, motorista);
        recyclerRequisicoes = findViewById(R.id.recyclerRequisicoes);
        recyclerRequisicoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(adapter);
        recyclerRequisicoes.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerRequisicoes,
                        new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Requisicao requisicao = requisicoes.get(position);
                        abreRequisicao(requisicao, false);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                })
        );

        txtAguardando = findViewById(R.id.txtAguardando);

        recuperaRequisicoes();
        recuperaLocalizacaoUsuario();
    }

    private void abreRequisicao (Requisicao requisicao, boolean requisicaoAtiva) {
        Intent i = new Intent(RequisicoesActivity.this, CorridaActivity.class);
        i.putExtra("idRequisicao", requisicao.getId());
        i.putExtra("motorista", motorista);
        i.putExtra("requisicaoAtiva", requisicaoAtiva);
        startActivity(i);
    }

    private void recuperaLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Double lat = location.getLatitude();
                Double lon = location.getLongitude();

                //Atualiza Localização do Geofire
                UsuarioFirebase.atualizaLocalizacao(lat, lon);

                motorista.setLatitude(String.valueOf(lat));
                motorista.setLongitude(String.valueOf(lon));
                //Remove a atualização de posição do motorista
                locationManager.removeUpdates(locationListener);
                adapter.notifyDataSetChanged();
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
                    0,
                    0,
                    locationListener
            );
        }
    }

    private void verificaStatusRequisicoes() {
        databaseReference = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes");
        Query query = databaseReference.orderByChild("motorista/id")
                .equalTo(motorista.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    if( requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                            || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)
                            || requisicao.getStatus().equals(Requisicao.STATUS_FINALIZADA)){
                        abreRequisicao(requisicao, true);
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperaRequisicoes() {
        databaseReference = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes");
        Query qry = databaseReference.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);
        qry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    requisicoes.clear();
                    txtAguardando.setVisibility(View.GONE);
                    recyclerRequisicoes.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        requisicoes.add(ds.getValue(Requisicao.class));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    txtAguardando.setVisibility(View.VISIBLE);
                    recyclerRequisicoes.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
