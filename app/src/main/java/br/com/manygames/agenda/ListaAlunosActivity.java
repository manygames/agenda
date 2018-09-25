package br.com.manygames.agenda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import br.com.manygames.agenda.adapter.AlunosAdapter;
import br.com.manygames.agenda.dao.AlunoDAO;
import br.com.manygames.agenda.dto.AlunoSync;
import br.com.manygames.agenda.modelo.Aluno;
import br.com.manygames.agenda.retrofit.RetroFitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;
    private SwipeRefreshLayout swipe;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        swipe = findViewById(R.id.lista_alunos_swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                buscaAlunos();
            }
        });

        listaAlunos = findViewById(R.id.lista_alunos);

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);

                Intent vaiProForm = new Intent(ListaAlunosActivity.this, FormularioActivity.class);

                vaiProForm.putExtra("aluno", aluno);

                startActivity(vaiProForm);
            }
        });

        Button novoAluno = findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vaiProForm = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(vaiProForm);
            }
        });

        registerForContextMenu(listaAlunos);
        buscaAlunos();
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();

        for (Aluno aluno :
             alunos) {
            Log.i("Id do aluno", String.valueOf(aluno.getId()));
        }
        
        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        //ArrayAdapter<Aluno> adapter = new ArrayAdapter<Aluno>(this, R.layout.item_lista, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    private void buscaAlunos() {
        Call<AlunoSync> call = new RetroFitInicializador().getAlunoService().lista();
        call.enqueue(new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync alunoSync = response.body();
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.sincroniza(alunoSync.getAlunos());
                dao.close();
                carregaLista();
                swipe.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {
                swipe.setRefreshing(false);
                Log.e("onFailure: ", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;
            case R.id.menu_baixar_provas:
                Intent vaiParaProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiParaProvas);
                break;
            case R.id.menu_mapa:
                Intent vaiParaMapa = new Intent(this, MapaActivity.class);
                startActivity(vaiParaMapa);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);


        MenuItem ligar = menu.add("Ligar");
        ligar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE}, 123);
                } else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
        });

        MenuItem sms = menu.add("Enviar SMS");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
        sms.setIntent(intentSMS);


        MenuItem mapa = menu.add("Localizar endereço");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);

        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        mapa.setIntent(intentMapa);

        MenuItem site = menu.add("Visitar Site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);


        String siteDoAluno = aluno.getSite();
        if(!siteDoAluno.startsWith("http://"))
            siteDoAluno = "http://" + siteDoAluno;

        intentSite.setData(Uri.parse(siteDoAluno));
        site.setIntent(intentSite);

        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Call<Void> call = new RetroFitInicializador().getAlunoService().deleta(aluno.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);
                        AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                        dao.deleta(aluno);
                        dao.close();
                        carregaLista();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ListaAlunosActivity.this, "Não foi possível remover o aluno", Toast.LENGTH_LONG);
                    }
                });
                return false;
            }
        });
    }
}
