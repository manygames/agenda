package br.com.manygames.agenda.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.manygames.agenda.modelo.Aluno;

public class AlunoDAO extends SQLiteOpenHelper{

    public AlunoDAO(Context context) {
        super(context, "Agenda", null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (" +
                "id CHAR(36) PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "endereco TEXT, " +
                "telefone TEXT, " +
                "site TEXT, " +
                "nota REAL, " +
                "caminhoFoto TEXT," +
                "sincronizado INT DEFAULT 0," +
                "desativado INT DEFAULT 0)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        switch(oldVersion){
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
                db.execSQL(sql);
            case 2:
                String novaTabela = "CREATE TABLE Alunos_novo (" +
                        "id CHAR(36) PRIMARY KEY, " +
                        "nome TEXT NOT NULL, " +
                        "endereco TEXT, " +
                        "telefone TEXT, " +
                        "site TEXT, " +
                        "nota REAL, " +
                        "caminhoFoto TEXT)";
                db.execSQL(novaTabela);

                String migraTabela = "INSERT INTO Alunos_novo " +
                        "(id, nome, endereco, telefone, site, nota, caminhoFoto)" +
                        "SELECT id, nome, endereco, telefone, site, nota, caminhoFoto FROM Alunos";
                db.execSQL(migraTabela);

                String removeTabelaAntiga = "DROP TABLE Alunos";
                db.execSQL(removeTabelaAntiga);

                String alteraNomeTabela = "ALTER TABLE Alunos_novo RENAME TO Alunos";
                db.execSQL(alteraNomeTabela);
            case 3:
                String buscaAlunos = "SELECT * FROM Alunos";
                Cursor c = db.rawQuery(buscaAlunos, null);
                List<Aluno> alunos = populaAlunos(c);

                String atualizaIdAlunos = "UPDATE Alunos SET id = ? where id = ?";
                for (Aluno aluno:
                     alunos) {
                    db.execSQL(atualizaIdAlunos, new String[]{geraUUID(), aluno.getId()});
                }
            case 4:
                String adicionaCampoSincroniado = "ALTER TABLE Alunos ADD COLUMN sincronizado INT DEFAULT 0";
                db.execSQL(adicionaCampoSincroniado);
            case 5:
                String adicionaCampoDesativado = "ALTER TABLE Alunos ADD COLUMN desativado INT DEFAULT 0";
                db.execSQL(adicionaCampoDesativado);
        }
    }

//    private void corrigeIdsNulos(SQLiteDatabase db, String query) {
//        String buscaAlunos = "SELECT * FROM Alunos";
//        Cursor c = db.rawQuery(buscaAlunos, null);
//        List<Aluno> alunos = populaAlunos(c);
//
//        for (Aluno aluno:
//                alunos) {
//            db.execSQL(query, new String[]{geraUUID()});
//        }
//    }

    private String geraUUID() {
        return UUID.randomUUID().toString();
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos WHERE desativado = 0";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        List<Aluno> alunos = populaAlunos(c);
        c.close();
        return alunos;
    }

    @NonNull
    private List<Aluno> populaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<Aluno>();
        while(c.moveToNext()){
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));
            aluno.setSincronizado(c.getInt(c.getColumnIndex("sincronizado")));
            aluno.setDesativado(c.getInt(c.getColumnIndex("desativado")));

            alunos.add(aluno);
        }
        return alunos;
    }

    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        String[] params =  {aluno.getId().toString()};

        if(aluno.estaDesativado()){
            db.delete("Alunos", "id = ?", params);
            Log.i("DELETADO!","PRA SEMPRE!");
        } else {
            aluno.desativa();
            altera(aluno);
        }
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = pegaDadosDoAluno(aluno);
        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params);
    }

    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        insereIdSeNecessario(aluno);

        ContentValues dados = pegaDadosDoAluno(aluno);
        db.insert("Alunos", null, dados);
        //aluno.setId(id);
    }

    private void insereIdSeNecessario(Aluno aluno) {
        if(aluno.getId() == null)
            aluno.setId(geraUUID());
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        dados.put("sincronizado", aluno.getSincronizado());
        dados.put("desativado", aluno.getDesativado());
        return dados;
    }

    public boolean ehAluno(String telefone){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Alunos WHERE telefone = ?", new String[]{telefone});
        int resultados = c.getCount();
        c.close();
        return resultados > 0;
    }

    public void sincroniza(List<Aluno> alunos) {
        for (Aluno aluno:
             alunos) {

            aluno.sincroniza();

            if(existe(aluno))
                if(aluno.estaDesativado()){
                    deleta(aluno);
                } else {
                    altera(aluno);
                }
            else if (!aluno.estaDesativado()) {
                insere(aluno);
            }
        }
    }

    private boolean existe(Aluno aluno) {
        SQLiteDatabase db = getReadableDatabase();
        String existe = "SELECT id FROM Alunos where id = ? LIMIT 1";
        Cursor c = db.rawQuery(existe, new String[]{aluno.getId()});
        return c.getCount() > 0;
    }

    public List<Aluno> listaNaoSincronizados(){
        SQLiteDatabase db = getWritableDatabase();
        String sincronizados = "SELECT * FROM Alunos WHERE sincronizado = 0";
        Cursor c = db.rawQuery(sincronizados, null);
        List<Aluno> alunos = populaAlunos(c);
        return alunos;
    }
}
