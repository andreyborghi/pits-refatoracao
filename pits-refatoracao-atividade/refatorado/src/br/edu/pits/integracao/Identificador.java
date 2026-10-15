package br.edu.pits.integracao;

/** Contrato futuro do diagrama. Não inclui regras de autorização nem SDK de hardware. */
@FunctionalInterface
public interface Identificador {
    Identidade identificar();
}
