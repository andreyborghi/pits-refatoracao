package br.edu.pits.servico;

import br.edu.pits.dominio.*;
import br.edu.pits.porta.CatalogoAcesso;
import br.edu.pits.porta.RepositorioAcesso;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Orquestra uma tentativa; só retorna depois de registrar evento e auditoria. */
public final class AccessEventService {
    private final CatalogoAcesso catalogo;
    private final PoliticaAutorizacao politica;
    private final RepositorioAcesso repositorio;
    private final Clock clock;

    public AccessEventService(CatalogoAcesso catalogo, PoliticaAutorizacao politica,
                               RepositorioAcesso repositorio, Clock clock) {
        this.catalogo = Objects.requireNonNull(catalogo);
        this.politica = Objects.requireNonNull(politica);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.clock = Objects.requireNonNull(clock);
    }

    public EventoAcesso processarEvento(SolicitacaoAcesso entrada) {
        SolicitacaoAcesso solicitacao = entrada == null
                ? new SolicitacaoAcesso(null, null, null, null, null) : entrada;
        Instant agora = clock.instant();
        Credencial credencial = catalogo.buscarCredencial(solicitacao.identificadorCredencial());
        Ambiente ambiente = catalogo.buscarAmbiente(solicitacao.ambienteId());
        DecisaoAcesso decisao = politica.avaliar(solicitacao, credencial, ambiente, agora);
        EventoAcesso evento = criarEvento(solicitacao, credencial, decisao, agora);
        repositorio.salvar(RegistroAcesso.doEvento(evento));
        return evento;
    }

    public List<EventoAcesso> obterHistorico(Long usuarioId) {
        return repositorio.listar().stream().map(RegistroAcesso::evento)
                .filter(evento -> Objects.equals(usuarioId, evento.usuarioId())).toList();
    }

    private EventoAcesso criarEvento(SolicitacaoAcesso s, Credencial c,
                                      DecisaoAcesso decisao, Instant agora) {
        Long usuarioId = c == null || c.usuario() == null ? null : c.usuario().id();
        return new EventoAcesso(repositorio.proximoId(), agora, s.identificadorCredencial(), usuarioId,
                s.ambienteId(), s.tipoInformado(), TipoEventoAcesso.doTexto(s.tipoInformado()),
                s.metodo(), s.ipDispositivo(), decisao.resultado(), decisao.motivo());
    }
}
