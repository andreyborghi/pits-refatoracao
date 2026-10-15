package br.edu.pits.porta;

import br.edu.pits.dominio.Ambiente;
import br.edu.pits.dominio.Credencial;

/** Consulta cadastros; retorna null quando a referência não foi encontrada. */
public interface CatalogoAcesso {
    Credencial buscarCredencial(String identificador);
    Ambiente buscarAmbiente(Long id);
}
