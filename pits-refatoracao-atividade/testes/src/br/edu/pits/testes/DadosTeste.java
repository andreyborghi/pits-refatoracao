package br.edu.pits.testes;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Apenas dados de entrada; não replica nenhuma regra de autorização. */
public final class DadosTeste {
    public record UsuarioDado(Long id, String nome, String status, String categoria) { }
    public record CredencialDado(Long id, String identificador, String tipo,
                                UsuarioDado usuario, Instant expiracao) { }
    public record AmbienteDado(Long id, String nome, String tipo, String status) { }

    private DadosTeste() { }

    public static Map<String, CredencialDado> credenciais(Clock clock) {
        UsuarioDado aluno = new UsuarioDado(1L, "Aluno Exemplo", "ATIVO", "ALUNO");
        UsuarioDado inativo = new UsuarioDado(2L, "Aluno Inativo", "INATIVO", "ALUNO");
        UsuarioDado professor = new UsuarioDado(3L, "Professor Exemplo", "ATIVO", "PROFESSOR");
        UsuarioDado operador = new UsuarioDado(4L, "Operador Exemplo", "ATIVO", "OPERADOR");
        UsuarioDado statusNulo = new UsuarioDado(5L, "Usuário Sem Status", null, "ALUNO");
        Instant futuro = clock.instant().plusSeconds(86400);
        Instant passado = clock.instant().minusSeconds(1);
        Map<String, CredencialDado> map = new LinkedHashMap<>();
        adicionar(map, 1L, "aluno-ok", aluno, futuro);
        adicionar(map, 2L, "inativo", inativo, futuro);
        adicionar(map, 3L, "sem-usuario", null, futuro);
        adicionar(map, 4L, "expirada", aluno, passado);
        adicionar(map, 5L, "vence-agora", aluno, clock.instant());
        adicionar(map, 6L, "sem-vencimento", aluno, null);
        adicionar(map, 7L, "professor-ok", professor, futuro);
        adicionar(map, 8L, "operador-ok", operador, futuro);
        adicionar(map, 9L, "inativo-expirada", inativo, passado);
        adicionar(map, 10L, "status-nulo", statusNulo, futuro);
        return Map.copyOf(map);
    }

    public static Map<Long, AmbienteDado> ambientes() {
        return Map.of(10L, new AmbienteDado(10L, "Sala A", "SALA", "ATIVO"),
                      20L, new AmbienteDado(20L, "Sala B", "SALA", "INATIVO"),
                      30L, new AmbienteDado(30L, "Sala Sem Status", "SALA", null));
    }

    private static void adicionar(Map<String, CredencialDado> map, Long id, String codigo,
                                  UsuarioDado usuario, Instant expiracao) {
        map.put(codigo, new CredencialDado(id, codigo, "SENHA", usuario, expiracao));
    }
}
