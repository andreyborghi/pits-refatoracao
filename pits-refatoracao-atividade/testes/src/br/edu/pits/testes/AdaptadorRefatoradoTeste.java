package br.edu.pits.testes;

import br.edu.pits.dominio.*;
import br.edu.pits.infra.CatalogoEmMemoria;
import br.edu.pits.infra.RepositorioAcessoEmMemoria;
import br.edu.pits.servico.AccessEventService;
import br.edu.pits.servico.PoliticaAutorizacao;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

public final class AdaptadorRefatoradoTeste implements ContratoTeste.Sistema {
    private final RepositorioAcessoEmMemoria repositorio = new RepositorioAcessoEmMemoria();
    private final AccessEventService servico;
    private final CatalogoEmMemoria catalogo;

    public AdaptadorRefatoradoTeste(Clock clock) {
        Map<String, Credencial> credenciais = new HashMap<>();
        DadosTeste.credenciais(clock).forEach((codigo, dado) -> {
            Usuario usuario = null;
            if (dado.usuario() != null) {
                var u = dado.usuario();
                usuario = new Usuario(u.id(), u.nome(), status(u.status()), CategoriaUsuario.valueOf(u.categoria()));
            }
            credenciais.put(codigo, new Credencial(dado.id(), dado.identificador(),
                    TipoCredencial.valueOf(dado.tipo()), usuario, dado.expiracao()));
        });
        Map<Long, Ambiente> ambientes = new HashMap<>();
        DadosTeste.ambientes().forEach((id, a) -> ambientes.put(id,
                new Ambiente(a.id(), a.nome(), TipoAmbiente.valueOf(a.tipo()), status(a.status()))));
        catalogo = new CatalogoEmMemoria(credenciais, ambientes);
        servico = new AccessEventService(catalogo, new PoliticaAutorizacao(clock.getZone()), repositorio, clock);
    }

    public AccessEventService servico() { return servico; }
    public RepositorioAcessoEmMemoria repositorio() { return repositorio; }
    public CatalogoEmMemoria catalogo() { return catalogo; }

    @Override
    public ContratoTeste.RegistroObservado processar(String codigo, Long ambiente, String tipo,
                                                    String metodo, String ip) {
        EventoAcesso r = servico.processarEvento(new SolicitacaoAcesso(codigo, ambiente, tipo,
                metodo == null ? null : TipoIdentificacao.valueOf(metodo), ip));
        Auditoria a = repositorio.listar().get(repositorio.listar().size() - 1).auditoria();
        var auditoria = new ContratoTeste.AuditoriaObservada(a.eventoId(), a.dataHora(), a.usuarioId(),
                                                           a.operacao(), a.resultado().name(), a.motivo().name());
        return new ContratoTeste.RegistroObservado(r.id(), r.dataHora(), r.identificadorCredencialInformado(),
                r.usuarioId(), r.ambienteId(), r.tipoInformado(), r.metodo().name(), r.ipDispositivo(),
                r.resultado().name(), r.motivo().name(), auditoria);
    }

    @Override public int totalEventos() { return repositorio.listar().size(); }
    @Override public int totalAuditorias() { return repositorio.listar().size(); }

    private static StatusEntidade status(String valor) {
        return valor == null ? null : StatusEntidade.valueOf(valor);
    }
}
