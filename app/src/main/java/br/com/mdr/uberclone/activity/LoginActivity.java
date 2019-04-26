package br.com.mdr.uberclone.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.helper.UsuarioFirebase;
import br.com.mdr.uberclone.model.Usuario;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtEmail, edtSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        iniciaComponentes();
    }

    private void iniciaComponentes() {
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
    }

    public void validaUsuario(View v) {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        if (!email.isEmpty() && !senha.isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(senha);
            logaUsuario(usuario);
        } else {
            if (email.isEmpty())
                edtEmail.setError("Preencha o e-mail.");
            if (senha.isEmpty())
                edtSenha.setError("Digite sua senha.");
        }
    }

    private void logaUsuario(final Usuario usuario) {
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);
                        } else {
                            String excecao;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = "Digite uma senha mais forte!";
                                edtSenha.setError(excecao);
                                edtSenha.requestFocus();
                            } catch (FirebaseAuthEmailException e) {
                                excecao = "Digite um e-mail válido.";
                                edtEmail.setError(excecao);
                                edtEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = "Conta já cadastrada com este e-mail!";
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Usuário ou senha inválidos";
                            } catch (Exception e) {
                                excecao = "Erro ao logar conta: " + e.getMessage();
                            }
                            mostraMensagem(excecao);
                        }
                    }
                });
    }

    private void mostraMensagem(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}
