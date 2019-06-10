package br.com.mdr.uberclone.helper;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.mdr.uberclone.activity.CadastroActivity;
import br.com.mdr.uberclone.activity.LoginActivity;
import br.com.mdr.uberclone.activity.PassageiroActivity;
import br.com.mdr.uberclone.activity.RequisicoesActivity;
import br.com.mdr.uberclone.model.Usuario;

/**
 * Created by ${USER_NAME} on 16/04/2019.
 */
public class UsuarioFirebase {

    public static String getIdUsuario(){
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return autenticacao.getCurrentUser().getUid();
    }

    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return auth.getCurrentUser();
    }

    public static Usuario getUsuarioLogado() {
        FirebaseUser firebaseUser = getUsuarioAtual();
        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());
        return usuario;
    }

    public static void logOff() {
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.signOut();
    }

    public static boolean atualizarTipoUsuario(String nome) {
        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void redirecionaUsuarioLogado(final Activity activity) {
        FirebaseUser user = getUsuarioAtual();
        if (user != null) {
            DatabaseReference ref = ConfiguracaoFirebase.getFirebase()
                    .child("usuarios")
                    .child(getIdUsuario());

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    Intent i;
                    if (usuario != null) {
                        if (usuario.getTipo().equals("P"))
                            i = new Intent(activity, PassageiroActivity.class);
                        else
                            i = new Intent(activity, RequisicoesActivity.class);

                        i.putExtra("usuario", usuario);
                        activity.startActivity(i);
                        activity.finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
