package br.com.mdr.uberclone.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;

/**
 * Created by Marlon D. Rocha on 30/04/2019.
 */
public class Requisicao implements Serializable {

    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "Aguardando";
    public static final String STATUS_A_CAMINHO = "A caminho";
    public static final String STATUS_VIAGEM = "Viagem";
    public static final String STATUS_FINALIZADA = "Finalizada";
    public static final String STATUS_ENCERRADA = "Encerrada";
    public static final String STATUS_CANCELADA = "Cancelada";

    public Requisicao() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId( idRequisicao );

        requisicoes.child( getId() ).setValue(this);

    }

    public void atualizar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("motorista", getMotorista() );
        objeto.put("status", getStatus());

        requisicao.updateChildren( objeto );

    }

    public void atualizarStatus(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren( objeto );

    }

    public void atualizarLocalizacaoMotorista(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference requisicoes = firebaseRef
                .child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId())
                .child("motorista");

        Map objeto = new HashMap();
        objeto.put("latitude", getMotorista().getLatitude() );
        objeto.put("longitude", getMotorista().getLongitude());

        requisicao.updateChildren( objeto );

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}