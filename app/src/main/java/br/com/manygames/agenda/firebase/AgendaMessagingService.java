package br.com.manygames.agenda.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Map;

import br.com.manygames.agenda.dao.AlunoDAO;
import br.com.manygames.agenda.dto.AlunoSync;
import br.com.manygames.agenda.event.AtualizaListaAlunoEvent;

public class AgendaMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> mensagem = remoteMessage.getData();
        //Log.i("Msg Recebida", String.valueOf(mensagem));

        converteParaAluno(mensagem);
    }

    private void converteParaAluno(Map<String, String> mensagem) {
        String chave = "alunoSync";
        if(mensagem.containsKey(chave)){
            String json = mensagem.get(chave);
            ObjectMapper mapper = new ObjectMapper();
            try {
                AlunoSync alunoSync = mapper.readValue(json, AlunoSync.class);
                AlunoDAO dao = new AlunoDAO(this);
                dao.sincroniza(alunoSync.getAlunos());
                dao.close();
                EventBus bus = EventBus.getDefault();
                bus.post(new AtualizaListaAlunoEvent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
