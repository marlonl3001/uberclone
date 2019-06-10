package br.com.mdr.uberclone.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;

/**
 * Created by Marlon D. Rocha on 26/04/2019.
 */
public class Usuario implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;
    private String latitude;
    private String longitude;

    public void salvar() {
        DatabaseReference ref = ConfiguracaoFirebase.getFirebase()
                .child("usuarios")
                .child(getId());
        ref.setValue(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude //Exclui a senha no momento de salvar o usuário no banco de dados
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
