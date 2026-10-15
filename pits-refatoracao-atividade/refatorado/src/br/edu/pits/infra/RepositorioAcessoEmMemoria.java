package br.edu.pits.infra;

import br.edu.pits.dominio.RegistroAcesso;
import br.edu.pits.porta.RepositorioAcesso;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RepositorioAcessoEmMemoria implements RepositorioAcesso {
    private final List<RegistroAcesso> registros = new ArrayList<>();
    private long sequencia;

    @Override public synchronized long proximoId() { return ++sequencia; }
    @Override public synchronized void salvar(RegistroAcesso registro) {
        registros.add(Objects.requireNonNull(registro));
    }
    @Override public synchronized List<RegistroAcesso> listar() { return List.copyOf(registros); }
}
