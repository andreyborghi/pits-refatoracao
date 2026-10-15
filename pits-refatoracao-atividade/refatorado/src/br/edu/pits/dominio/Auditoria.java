package br.edu.pits.dominio;

import java.time.Instant;

public record Auditoria(long eventoId, Instant dataHora, Long usuarioId,
                        String operacao, ResultadoAcesso resultado, MotivoAcesso motivo) {
    private static final String OPERACAO_ACESSO = "PROCESSAR_EVENTO_ACESSO";
    public static Auditoria doEvento(EventoAcesso evento) {
        return new Auditoria(evento.id(), evento.dataHora(), evento.usuarioId(),
                             OPERACAO_ACESSO, evento.resultado(), evento.motivo());
    }
}
