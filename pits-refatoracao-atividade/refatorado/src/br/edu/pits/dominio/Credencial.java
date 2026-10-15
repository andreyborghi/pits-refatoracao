package br.edu.pits.dominio;

import java.time.Instant;

public record Credencial(Long id, String identificador, TipoCredencial tipo,
                         Usuario usuario, Instant dataExpiracao) {
    /** A expiração é exclusiva: no instante de vencimento a credencial já expirou. */
    public boolean estaExpirada(Instant agora) {
        return dataExpiracao != null && !agora.isBefore(dataExpiracao);
    }
}
