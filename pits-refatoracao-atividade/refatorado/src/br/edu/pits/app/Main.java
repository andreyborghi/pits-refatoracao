package br.edu.pits.app;

import br.edu.pits.dominio.*;
import br.edu.pits.infra.CatalogoEmMemoria;
import br.edu.pits.infra.RepositorioAcessoEmMemoria;
import br.edu.pits.servico.AccessEventService;
import br.edu.pits.servico.PoliticaAutorizacao;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

public final class Main {
    public static void main(String[] args) {
        ZoneId zona = ZoneId.of("America/Sao_Paulo");
        // Fixo apenas para a demonstração ser reprodutível. Em uso real: Clock.system(zona).
        Clock clock = Clock.fixed(Instant.parse("2026-10-08T15:00:00Z"), zona);
        Usuario aluno = new Usuario(1L, "Aluno Exemplo", StatusEntidade.ATIVO, CategoriaUsuario.ALUNO);
        Credencial c = new Credencial(1L, "aluno-ok", TipoCredencial.SENHA, aluno,
                                      Instant.parse("2026-10-09T15:00:00Z"));
        Ambiente sala = new Ambiente(10L, "Sala A", TipoAmbiente.SALA, StatusEntidade.ATIVO);
        var catalogo = new CatalogoEmMemoria(Map.of(c.identificador(), c), Map.of(sala.id(), sala));
        var repositorio = new RepositorioAcessoEmMemoria();
        var servico = new AccessEventService(catalogo, new PoliticaAutorizacao(zona), repositorio, clock);
        System.out.println("\nDEMONSTRAÇÃO PITS v2.0 | 08/10/2026 às 12h em São Paulo");
        for (String codigo : new String[] {"aluno-ok", "desconhecida", null}) {
            EventoAcesso evento = servico.processarEvento(
                    new SolicitacaoAcesso(codigo, 10L, "ENTRADA_SALA", TipoIdentificacao.MANUAL, "127.0.0.1"));
            System.out.printf("Evento %d | credencial=%s | %s | motivo=%s%n",
                              evento.id(), codigo, evento.resultado(), evento.motivo());
        }
        System.out.println("Eventos registrados: " + repositorio.listar().size());
        System.out.println("Auditorias registradas: " + repositorio.listar().size());
    }
}
