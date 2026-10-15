package br.edu.pits.dominio;

import java.time.Instant;

/** Snapshot imutável, inclusive quando credencial, usuário ou ambiente não existem. */
public record EventoAcesso(long id, Instant dataHora, String identificadorCredencialInformado,
                           Long usuarioId, Long ambienteId, String tipoInformado,
                           TipoEventoAcesso tipo, TipoIdentificacao metodo,
                           String ipDispositivo, ResultadoAcesso resultado, MotivoAcesso motivo) { }
