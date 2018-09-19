package br.com.manygames.agenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import br.com.manygames.agenda.dao.AlunoDAO;
import br.com.manygames.agenda.modelo.Aluno;

public class FormularioActivity extends AppCompatActivity {
    public static final int CODIGO_CAMERA = 567;
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        Intent intent = getIntent();
        Aluno aluno = (Aluno)intent.getSerializableExtra("aluno");

        helper = new FormularioHelper(this);
        if(aluno != null){
            helper.preencheFormulario(aluno);
        }

        Button botaoFoto = (Button)findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                caminhoFoto = getExternalFilesDir(null) + "/"+ System.currentTimeMillis()+".jpg";
                File arquivoFoto = new File(caminhoFoto);
                intentFoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                startActivityForResult(intentFoto, CODIGO_CAMERA);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CODIGO_CAMERA && resultCode == Activity.RESULT_OK){
            helper.carregaImagem(caminhoFoto);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_formulario_ok:
                Aluno aluno = helper.pegaAluno();
                AlunoDAO dao = new AlunoDAO(this);

                if(aluno.getId() != null) {
                    dao.altera(aluno);
                    Log.v("weber", "alterou");
                } else {
                    dao.insere(aluno);
                    Log.v("weber", "inseriu");
                }

                dao.close();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
