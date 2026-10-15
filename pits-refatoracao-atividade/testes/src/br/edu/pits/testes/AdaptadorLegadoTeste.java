package br.edu.pits.testes;

import br.edu.pits.legado.AcessoLegado;
import java.time.Clock;

public final class AdaptadorLegadoTeste implements ContratoTeste.Sistema {
    private final AcessoLegado legado;

    public AdaptadorLegadoTeste(Clock clock) {
        legado = new AcessoLegado(clock);
        DadosTeste.credenciais(clock).forEach((codigo, dado) -> {
            AcessoLegado.CredencialLegada c = new AcessoLegado.CredencialLegada();
            c.id = dado.id();
            c.identificador = dado.identificador();
            c.tipo = dado.tipo();
            c.expiracao = dado.expiracao();
            if (dado.usuario() != null) {
                var u = dado.usuario();
                c.usuario = new AcessoLegado.UsuarioLegado();
                c.usuario.id = u.id();
                c.usuario.nome = u.nome();
                c.usuario.status = u.status();
                c.usuario.categoria = u.categoria();
            }
            legado.credenciais.put(codigo, c);
        });
        DadosTeste.ambientes().forEach((id, dado) -> {
            AcessoLegado.AmbienteLegado a = new AcessoLegado.AmbienteLegado();
            a.id = dado.id();
            a.nome = dado.nome();
            a.tipo = dado.tipo();
            a.status = dado.status();
            legado.ambientes.put(id, a);
        });
    }

    @Override
    public ContratoTeste.RegistroObservado processar(String codigo, Long ambiente, String tipo,
                                                    String metodo, String ip) {
        var r = legado.processarEvento(codigo, ambiente, tipo, metodo, ip);
        var a = legado.auditorias.get(legado.auditorias.size() - 1);
        var auditoria = new ContratoTeste.AuditoriaObservada(a.eventoId, a.dataHora, a.usuarioId,
                                                           a.operacao, a.resultado, a.motivo);
        return new ContratoTeste.RegistroObservado(r.id, r.dataHora, r.codigoCredencial, r.usuarioId,
                 r.ambienteId, r.tipoInformado, r.metodo, r.ipDispositivo, r.resultado, r.motivo, auditoria);
    }

    @Override public int totalEventos() { return legado.registros.size(); }
    @Override public int totalAuditorias() { return legado.auditorias.size(); }
}
