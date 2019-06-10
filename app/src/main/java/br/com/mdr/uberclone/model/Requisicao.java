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
    public static final String STATUS_CAMINHO = "A caminho";
    public static final String STATUS_VIAGEM_INICIO = "Viagem Iniciada";
    public static final String STATUS_VIAGEM_FIM = "Viagem Finalizada";

    public void salvar() {
        DatabaseReference ref = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes");
        String reqId = ref.push().getKey();
        setId(reqId);
        ref.child(reqId).setValue(this);
    }

    public void atualizar() {
        DatabaseReference ref = ConfiguracaoFirebase.getFirebase()
                .child("requisicoes").child(getId());

        //Atualiza apenas as propriedades "motorista" e "status" da requisição
        Map objeto = new HashMap();
        objeto.put("motorista", getMotorista());
        objeto.put("status", getStatus());

        ref.updateChildren(objeto);
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
