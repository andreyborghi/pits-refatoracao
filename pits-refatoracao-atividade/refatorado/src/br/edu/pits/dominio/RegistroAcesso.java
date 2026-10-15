package br.edu.pits.dominio;

import java.util.Objects;

/** Evento e auditoria compõem uma única unidade de persistência. */
public record RegistroAcesso(EventoAcesso evento, Auditoria auditoria) {
    public RegistroAcesso {
        Objects.requireNonNull(evento);
        Objects.requireNonNull(auditoria);
        if (evento.id() != auditoria.eventoId()) {
            throw new IllegalArgumentException("Auditoria não corresponde ao evento.");
        }
    }
    public static RegistroAcesso doEvento(EventoAcesso evento) {
        return new RegistroAcesso(evento, Auditoria.doEvento(evento));
    }
}
