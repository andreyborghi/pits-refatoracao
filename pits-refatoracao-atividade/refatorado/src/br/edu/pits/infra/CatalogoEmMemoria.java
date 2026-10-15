package br.edu.pits.infra;

import br.edu.pits.dominio.Ambiente;
import br.edu.pits.dominio.Credencial;
import br.edu.pits.porta.CatalogoAcesso;
import java.util.Map;

public final class CatalogoEmMemoria implements CatalogoAcesso {
    private final Map<String, Credencial> credenciais;
    private final Map<Long, Ambiente> ambientes;

    public CatalogoEmMemoria(Map<String, Credencial> credenciais, Map<Long, Ambiente> ambientes) {
        this.credenciais = Map.copyOf(credenciais);
        this.ambientes = Map.copyOf(ambientes);
    }
    @Override public Credencial buscarCredencial(String identificador) {
        return identificador == null ? null : credenciais.get(identificador);
    }
    @Override public Ambiente buscarAmbiente(Long id) {
        return id == null ? null : ambientes.get(id);
    }
}
