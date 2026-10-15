package br.edu.pits.testes;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Casos independentes da implementação: mesmos dados, resultados e verificações. */
public final class ContratoTeste {
    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    public record AuditoriaObservada(Long eventoId, Instant dataHora, Long usuarioId,
                                    String operacao, String resultado, String motivo) { }
    public record RegistroObservado(Long id, Instant dataHora, String credencial, Long usuarioId,
                                    Long ambienteId, String tipoInformado, String metodo,
                                    String ip, String resultado, String motivo,
                                    AuditoriaObservada auditoria) { }
    public interface Sistema {
        RegistroObservado processar(String codigo, Long ambienteId, String tipo,
                                   String metodo, String ip);
        int totalEventos();
        int totalAuditorias();
    }
    public record Caso(String id, String descricao, String hora, String codigo,
                       Long ambienteId, String tipo, String resultado, String motivo,
                       Long usuarioId) {
        public Clock clock() {
            Instant instante = OffsetDateTime.parse("2026-10-08T" + hora + "-03:00").toInstant();
            return Clock.fixed(instante, ZONA);
        }
    }

    private ContratoTeste() { }

    public static List<Caso> casos() {
        List<Caso> c = new ArrayList<>();
        c.add(ok("T01", "aluno com acesso válido", "12:00:00", "aluno-ok", 10L, "ENTRADA_SALA", 1L));
        c.add(no("T02", "credencial desconhecida", "12:00:00", "desconhecida", 10L, "ENTRADA_SALA", "CREDENCIAL_INEXISTENTE", null));
        c.add(no("T03", "credencial nula", "12:00:00", null, 10L, "ENTRADA_SALA", "CREDENCIAL_VAZIA", null));
        c.add(no("T04", "credencial vazia", "12:00:00", "", 10L, "ENTRADA_SALA", "CREDENCIAL_VAZIA", null));
        c.add(no("T05", "credencial com espaços", "12:00:00", "   ", 10L, "ENTRADA_SALA", "CREDENCIAL_VAZIA", null));
        c.add(no("T06", "usuário inativo", "12:00:00", "inativo", 10L, "ENTRADA_SALA", "USUARIO_INATIVO", 2L));
        c.add(no("T07", "usuário ausente", "12:00:00", "sem-usuario", 10L, "ENTRADA_SALA", "USUARIO_AUSENTE", null));
        c.add(no("T08", "credencial expirada", "12:00:00", "expirada", 10L, "ENTRADA_SALA", "CREDENCIAL_EXPIRADA", 1L));
        c.add(no("T09", "instante exato da expiração", "12:00:00", "vence-agora", 10L, "ENTRADA_SALA", "CREDENCIAL_EXPIRADA", 1L));
        c.add(ok("T10", "credencial sem vencimento", "12:00:00", "sem-vencimento", 10L, "ENTRADA_SALA", 1L));
        c.add(no("T11", "ambiente inativo", "12:00:00", "aluno-ok", 20L, "ENTRADA_SALA", "AMBIENTE_INDISPONIVEL", 1L));
        c.add(no("T12", "ambiente inexistente", "12:00:00", "aluno-ok", 999L, "ENTRADA_SALA", "AMBIENTE_INDISPONIVEL", 1L));
        c.add(no("T13", "ambiente nulo", "12:00:00", "aluno-ok", null, "ENTRADA_SALA", "AMBIENTE_INDISPONIVEL", 1L));
        c.add(no("T14", "evento desconhecido", "12:00:00", "aluno-ok", 10L, "ABRIR_PORTA", "EVENTO_INVALIDO", 1L));
        c.add(no("T15", "evento nulo", "12:00:00", "aluno-ok", 10L, null, "EVENTO_INVALIDO", 1L));
        c.add(no("T16", "evento em letras minúsculas", "12:00:00", "aluno-ok", 10L, "entrada_sala", "EVENTO_INVALIDO", 1L));
        c.add(no("T17", "aluno antes de 06h", "05:59:59", "aluno-ok", 10L, "ENTRADA_SALA", "ALUNO_FORA_HORARIO", 1L));
        c.add(ok("T18", "limite 06h inclusive", "06:00:00", "aluno-ok", 10L, "ENTRADA_SALA", 1L));
        c.add(ok("T19", "imediatamente após 06h", "06:00:00.000000001", "aluno-ok", 10L, "ENTRADA_SALA", 1L));
        c.add(ok("T20", "imediatamente antes de 23h", "22:59:59.999999999", "aluno-ok", 10L, "ENTRADA_SALA", 1L));
        c.add(ok("T21", "limite 23h inclusive", "23:00:00", "aluno-ok", 10L, "ENTRADA_SALA", 1L));
        c.add(no("T22", "imediatamente após 23h", "23:00:00.000000001", "aluno-ok", 10L, "ENTRADA_SALA", "ALUNO_FORA_HORARIO", 1L));
        c.add(ok("T23", "professor fora do horário de aluno", "02:00:00", "professor-ok", 10L, "ENTRADA_SALA", 3L));
        c.add(ok("T24", "operador fora do horário de aluno", "02:00:00", "operador-ok", 10L, "ENTRADA_SALA", 4L));
        c.add(no("T25", "credencial prevalece sobre demais falhas", "02:00:00", "desconhecida", 20L, "INVALIDO", "CREDENCIAL_INEXISTENTE", null));
        c.add(no("T26", "usuário inativo prevalece sobre expiração", "02:00:00", "inativo-expirada", 20L, "INVALIDO", "USUARIO_INATIVO", 2L));
        c.add(no("T27", "expiração prevalece sobre ambiente e evento", "02:00:00", "expirada", 20L, "INVALIDO", "CREDENCIAL_EXPIRADA", 1L));
        c.add(no("T28", "ambiente prevalece sobre evento e horário", "02:00:00", "aluno-ok", 20L, "INVALIDO", "AMBIENTE_INDISPONIVEL", 1L));
        c.add(no("T29", "evento prevalece sobre horário", "02:00:00", "aluno-ok", 10L, "INVALIDO", "EVENTO_INVALIDO", 1L));
        c.add(no("T30", "status nulo de usuário", "12:00:00", "status-nulo", 10L, "ENTRADA_SALA", "USUARIO_INATIVO", 5L));
        c.add(no("T31", "status nulo de ambiente", "12:00:00", "aluno-ok", 30L, "ENTRADA_SALA", "AMBIENTE_INDISPONIVEL", 1L));
        c.add(ok("T32", "tipo ENTRADA_CAMPUS reconhecido", "12:00:00", "aluno-ok", 10L, "ENTRADA_CAMPUS", 1L));
        c.add(ok("T33", "tipo SAIDA_CAMPUS reconhecido", "12:00:00", "aluno-ok", 10L, "SAIDA_CAMPUS", 1L));
        c.add(ok("T34", "tipo SAIDA_SALA reconhecido", "12:00:00", "aluno-ok", 10L, "SAIDA_SALA", 1L));
        c.add(no("T35", "código de credencial diferencia maiúsculas", "12:00:00", "ALUNO-OK", 10L, "ENTRADA_SALA", "CREDENCIAL_INEXISTENTE", null));
        c.add(no("T36", "todos os dados de entrada nulos", "12:00:00", null, null, null, "CREDENCIAL_VAZIA", null));
        return List.copyOf(c);
    }

    public static int executar(String nome, Function<Clock, Sistema> criar) {
        int aprovados = 0;
        for (Caso caso : casos()) {
            Sistema sistema = criar.apply(caso.clock());
            RegistroObservado r = executarCaso(sistema, caso);
            verificar(caso, sistema, r);
            System.out.println("[OK] " + nome + " " + caso.id() + " " + caso.descricao());
            aprovados++;
        }
        System.out.println(nome + ": " + aprovados + "/" + casos().size() + " testes aprovados.");
        return aprovados;
    }

    public static RegistroObservado executarCaso(Sistema sistema, Caso caso) {
        return sistema.processar(caso.codigo(), caso.ambienteId(), caso.tipo(), "MANUAL", "127.0.0.1");
    }

    private static void verificar(Caso c, Sistema s, RegistroObservado r) {
        igual(c.resultado(), r.resultado(), c.id() + " resultado");
        igual(c.motivo(), r.motivo(), c.id() + " motivo");
        igual(1L, r.id(), c.id() + " identificador");
        igual(c.clock().instant(), r.dataHora(), c.id() + " data/hora");
        igual(c.codigo(), r.credencial(), c.id() + " credencial informada");
        igual(c.usuarioId(), r.usuarioId(), c.id() + " usuário");
        igual(c.ambienteId(), r.ambienteId(), c.id() + " ambiente informado");
        igual(c.tipo(), r.tipoInformado(), c.id() + " tipo informado");
        igual("MANUAL", r.metodo(), c.id() + " método");
        igual("127.0.0.1", r.ip(), c.id() + " IP");
        igual(1, s.totalEventos(), c.id() + " registro da tentativa");
        igual(1, s.totalAuditorias(), c.id() + " auditoria da tentativa");
        AuditoriaObservada a = r.auditoria();
        igual(r.id(), a.eventoId(), c.id() + " vínculo da auditoria");
        igual(r.dataHora(), a.dataHora(), c.id() + " instante da auditoria");
        igual(r.usuarioId(), a.usuarioId(), c.id() + " usuário da auditoria");
        igual("PROCESSAR_EVENTO_ACESSO", a.operacao(), c.id() + " operação auditada");
        igual(r.resultado(), a.resultado(), c.id() + " resultado auditado");
        igual(r.motivo(), a.motivo(), c.id() + " motivo auditado");
    }

    public static void igual(Object esperado, Object obtido, String contexto) {
        if (!Objects.equals(esperado, obtido)) {
            throw new AssertionError(contexto + ": esperado=" + esperado + "; obtido=" + obtido);
        }
    }

    private static Caso ok(String id, String descricao, String hora, String codigo,
                            Long ambiente, String tipo, Long usuario) {
        return new Caso(id, descricao, hora, codigo, ambiente, tipo, "AUTORIZADO", "ACESSO_AUTORIZADO", usuario);
    }

    private static Caso no(String id, String descricao, String hora, String codigo,
                            Long ambiente, String tipo, String motivo, Long usuario) {
        return new Caso(id, descricao, hora, codigo, ambiente, tipo, "NEGADO", motivo, usuario);
    }
}
