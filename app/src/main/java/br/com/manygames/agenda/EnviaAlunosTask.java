package br.com.manygames.agenda;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import br.com.manygames.agenda.converter.AlunoConversor;
import br.com.manygames.agenda.dao.AlunoDAO;
import br.com.manygames.agenda.modelo.Aluno;

public class EnviaAlunosTask extends AsyncTask<Void, Void, String>{
    private Context context;
    private ProgressDialog dialog;

    public EnviaAlunosTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "Aguarde", "Enviando Alunos...", true, true);
    }

    @Override
    protected String doInBackground(Void... objects) {
        AlunoDAO dao = new AlunoDAO(context);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();
        AlunoConversor conversor = new AlunoConversor();

        String json = conversor.converteParaJson(alunos);

        WebClient client = new WebClient();
        String resposta = client.post(json);

        return resposta;
    }

    @Override
    protected void onPostExecute(String resposta) {
        Toast.makeText(context, "Conseguimos! " + resposta, Toast.LENGTH_LONG).show();
        dialog.dismiss();
    }
}
