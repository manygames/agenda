package br.com.manygames.agenda.dto;

import java.util.List;

import br.com.manygames.agenda.modelo.Aluno;

public class AlunoSync {
    private List<Aluno> alunos;
    private String momentoDaUltimaModificacao;

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public String getMomentoDaUltimaModificacao() {
        return momentoDaUltimaModificacao;
    }
}
