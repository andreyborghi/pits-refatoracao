package br.edu.pits.porta;

import br.edu.pits.dominio.RegistroAcesso;
import java.util.List;

public interface RepositorioAcesso {
    long proximoId();
    /** Implementações persistentes devem salvar evento e auditoria na mesma transação. */
    void salvar(RegistroAcesso registro);
    /** Snapshot não modificável da coleção. */
    List<RegistroAcesso> listar();
}
