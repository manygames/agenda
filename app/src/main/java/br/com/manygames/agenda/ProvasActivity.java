package br.com.manygames.agenda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import br.com.manygames.agenda.modelo.Prova;

public class ProvasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provas);

        List<String> topicosPort = Arrays.asList("Sujeito", "Predicado", "Objeto direto", "Objeto indireto");
        Prova provaPort = new Prova("Portugues", "01/10/2018", topicosPort);

        List<String> topicosMat = Arrays.asList("Equações", "Limite");
        Prova provaMat = new Prova("Matematica", "06/10/2018", topicosMat);

        List<Prova> provas = Arrays.asList(provaPort, provaMat);
        ArrayAdapter<Prova> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, provas);

        ListView campoListaProvas = findViewById(R.id.lista_provas);
        campoListaProvas.setAdapter(adapter);

        campoListaProvas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prova prova = (Prova) parent.getItemAtPosition(position);
                Toast.makeText(ProvasActivity.this, "Clicou na prova de " + prova, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
