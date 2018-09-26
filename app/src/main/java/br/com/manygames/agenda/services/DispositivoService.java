package br.com.manygames.agenda.services;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DispositivoService {

    @POST("firebase/dispositivo")
    Call<Void> enviaToekn(@Header("token") String token);
}
