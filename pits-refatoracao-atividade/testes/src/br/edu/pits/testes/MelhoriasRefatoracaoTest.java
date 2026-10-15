package br.edu.pits.testes;

import br.edu.pits.dominio.*;
import br.edu.pits.integracao.Identidade;
import br.edu.pits.integracao.Identificador;
import br.edu.pits.porta.RepositorioAcesso;
import br.edu.pits.servico.AccessEventService;
import br.edu.pits.servico.PoliticaAutorizacao;
import java.time.Clock;
import java.util.List;

public final class MelhoriasRefatoracaoTest {
    private static final Clock CLOCK = ContratoTeste.casos().get(0).clock();

    public static void main(String[] args) {
        colecaoNaoPodeSerAlterada();
        historicoPreservaNegativasESnapshot();
        variasTentativasRecebemRegistrosDistintos();
        solicitacaoNulaTambemEhRegistrada();
        metodoNuloMantemPadraoManual();
        identificadorMockNaoAlteraServico();
        falhaPersistenciaNaoRetornaAutorizacao();
        System.out.println("MELHORIAS: 7/7 testes aprovados.");
    }

    private static void colecaoNaoPodeSerAlterada() {
        var s = novo();
        s.processar("aluno-ok", 10L, "ENTRADA_SALA", "MANUAL", null);
        try {
            s.repositorio().listar().clear();
            throw new AssertionError("E01 deveria impedir alteração externa dos registros");
        } catch (UnsupportedOperationException esperada) {
            ContratoTeste.igual(1, s.totalEventos(), "E01 coleção preservada");
        }
        ok("E01 coleção de registros não modificável");
    }

    private static void historicoPreservaNegativasESnapshot() {
        var s = novo();
        s.processar("aluno-ok", 10L, "ENTRADA_SALA", "MANUAL", null);
        var anterior = s.servico().obterHistorico(1L);
        s.processar("aluno-ok", 20L, "ENTRADA_SALA", "MANUAL", null);
        var atual = s.servico().obterHistorico(1L);
        ContratoTeste.igual(1, anterior.size(), "E02 snapshot anterior estável");
        ContratoTeste.igual(2, atual.size(), "E02 histórico inclui negativa");
        ContratoTeste.igual(ResultadoAcesso.NEGADO, atual.get(1).resultado(), "E02 acesso negado no histórico");
        ok("E02 histórico inclui negativas e preserva snapshots");
    }

    private static void variasTentativasRecebemRegistrosDistintos() {
        var s = novo();
        var a = s.processar("aluno-ok", 10L, "ENTRADA_SALA", "MANUAL", null);
        var b = s.processar("desconhecida", 10L, "ENTRADA_SALA", "MANUAL", null);
        ContratoTeste.igual(1L, a.id(), "E03 primeiro evento");
        ContratoTeste.igual(2L, b.id(), "E03 segundo evento");
        ContratoTeste.igual(2, s.totalEventos(), "E03 eventos");
        ContratoTeste.igual(2, s.totalAuditorias(), "E03 auditorias");
        ContratoTeste.igual(2L, b.auditoria().eventoId(), "E03 vínculo da segunda auditoria");
        ok("E03 cada tentativa recebe evento e auditoria próprios");
    }

    private static void solicitacaoNulaTambemEhRegistrada() {
        var s = novo();
        EventoAcesso e = s.servico().processarEvento(null);
        ContratoTeste.igual(MotivoAcesso.CREDENCIAL_VAZIA, e.motivo(), "E04 solicitação nula");
        ContratoTeste.igual(1, s.totalEventos(), "E04 registro");
        ContratoTeste.igual(1, s.totalAuditorias(), "E04 auditoria");
        ok("E04 objeto de solicitação nulo produz negativa auditada");
    }

    private static void metodoNuloMantemPadraoManual() {
        var antes = new AdaptadorLegadoTeste(CLOCK).processar("aluno-ok", 10L, "ENTRADA_SALA", null, null);
        var depois = novo().processar("aluno-ok", 10L, "ENTRADA_SALA", null, null);
        ContratoTeste.igual(antes, depois, "E05 método nulo mantém compatibilidade");
        ContratoTeste.igual("MANUAL", depois.metodo(), "E05 método padrão");
        ok("E05 método ausente mantém identificação MANUAL");
    }

    private static void identificadorMockNaoAlteraServico() {
        for (TipoIdentificacao metodo : List.of(TipoIdentificacao.CARTAO_RFID, TipoIdentificacao.BIOMETRIA)) {
            var s = novo();
            // Mock de contrato, sem leitor físico, biometria real ou SDK externo.
            Identificador mock = () -> new Identidade("aluno-ok", metodo, "192.0.2.10");
            EventoAcesso e = s.servico().processarEvento(mock.identificar().paraSolicitacao(10L, "ENTRADA_SALA"));
            ContratoTeste.igual(ResultadoAcesso.AUTORIZADO, e.resultado(), "E06 autorização " + metodo);
            ContratoTeste.igual(metodo, e.metodo(), "E06 método normalizado " + metodo);
            ContratoTeste.igual(1, s.totalAuditorias(), "E06 auditoria " + metodo);
        }
        ok("E06 contratos RFID/Fingerprint simulados sem acoplar hardware");
    }

    private static void falhaPersistenciaNaoRetornaAutorizacao() {
        RepositorioAcesso falho = new RepositorioAcesso() {
            @Override public long proximoId() { return 1L; }
            @Override public void salvar(RegistroAcesso registro) { throw new IllegalStateException("Persistência indisponível"); }
            @Override public List<RegistroAcesso> listar() { return List.of(); }
        };
        var s = novo();
        var servico = new AccessEventService(s.catalogo(), new PoliticaAutorizacao(CLOCK.getZone()), falho, CLOCK);
        try {
            servico.processarEvento(new SolicitacaoAcesso("aluno-ok", 10L, "ENTRADA_SALA", null, null));
            throw new AssertionError("E07 não pode retornar autorização sem persistência");
        } catch (IllegalStateException esperada) {
            ContratoTeste.igual("Persistência indisponível", esperada.getMessage(), "E07 falha propagada");
        }
        ok("E07 falha de persistência não é mascarada como sucesso");
    }

    private static AdaptadorRefatoradoTeste novo() { return new AdaptadorRefatoradoTeste(CLOCK); }
    private static void ok(String texto) { System.out.println("[OK] " + texto); }
}
