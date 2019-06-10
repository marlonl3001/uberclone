package br.com.mdr.uberclone.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.helper.ConfiguracaoFirebase;
import br.com.mdr.uberclone.helper.Permissoes;
import br.com.mdr.uberclone.helper.UsuarioFirebase;
import br.com.mdr.uberclone.model.Usuario;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin, btnCadastrar;
    private String[] permissoes = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //Valida as permissões
        Permissoes.validarPermissoes(permissoes, this, 0);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCadastrar.setOnClickListener(this);
        //UsuarioFirebase.logOff();
        UsuarioFirebase.redirecionaUsuarioLogado(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin: {
                startActivity(new Intent(this, LoginActivity.class));
                break;
            }
            case R.id.btnCadastrar: {
                startActivity(new Intent(this, CadastroActivity.class));
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int result: grantResults) {
            if (result == PackageManager.PERMISSION_DENIED)
                mostraPermissaoNegada();
        }
    }

    private void mostraPermissaoNegada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões necessárias.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
