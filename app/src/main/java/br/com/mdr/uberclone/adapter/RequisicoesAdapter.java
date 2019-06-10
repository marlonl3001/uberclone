package br.com.mdr.uberclone.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.mdr.uberclone.R;
import br.com.mdr.uberclone.model.Requisicao;
import br.com.mdr.uberclone.model.Usuario;

/**
 * Created by Marlon D. Rocha on 02/05/2019.
 */
public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Usuario motorista;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Usuario motorista) {
        this.requisicoes = requisicoes;
        this.motorista = motorista;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.requisicao_item,
                parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
        Requisicao req = requisicoes.get(position);
        Usuario passageiro = req.getPassageiro();
        myViewHolder.txtNome.setText(passageiro.getNome());
        myViewHolder.txtDistancia.setText(passageiro.getEmail());
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtNome, txtDistancia;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDistancia = itemView.findViewById(R.id.txtDistancia);
        }
    }
}
