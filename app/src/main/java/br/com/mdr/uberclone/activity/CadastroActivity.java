package br.com.mdr.uberclone.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.helper.UsuarioFirebase;
import br.com.mdr.uberclone.model.Usuario;

public class CadastroActivity extends AppCompatActivity {
    private Button btnCadastrar;
    private TextInputEditText edtNome, edtEmail, edtSenha;
    private Switch tipoUsuario;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        iniciaComponentes();
    }

    private void iniciaComponentes() {
        btnCadastrar = findViewById(R.id.btnCadastrar);
        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        tipoUsuario = findViewById(R.id.switchAcesso);
    }

    public void onClick(View v) {
        String nome = edtNome.getText().toString();
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);
            usuario.setTipo(getTipoUsuario());

            cadastraUsuario(usuario);
        } else {
            mostraMensagem("Preencha todos os campos.");
        }
    }

    private void cadastraUsuario(final Usuario usuario) {
        auth.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String idUsuario = task.getResult().getUser().getUid();
                            usuario.setId(idUsuario);
                            usuario.salvar();
                            UsuarioFirebase.atualizarTipoUsuario(getTipoUsuario());

                            mostraMensagem("Sua conta foi criada com sucesso!");
                            Intent i;

                            if (usuario.getTipo().equals("P"))
                                i = new Intent(CadastroActivity.this, MapsActivity.class);
                            else
                                i = new Intent(CadastroActivity.this, RequisicoesActivity.class);

                            i.putExtra("usuario", usuario);
                            startActivity(i);
                            finish();
                        } else {
                            String excecao;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = "Digite uma senha mais forte!";
                                edtSenha.setError(excecao);
                                edtSenha.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Digite um e-mail válido.";
                                edtEmail.setError(excecao);
                                edtEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = "Conta já cadastrada com este e-mail!";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar conta: " + e.getMessage();
                            }
                            mostraMensagem(excecao);
                        }
                    }
                });
    }

    private void mostraMensagem(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    private String getTipoUsuario() {
        return tipoUsuario.isChecked() ? "M" : "P";
    }
}
