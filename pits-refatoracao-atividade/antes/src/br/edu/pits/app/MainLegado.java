package br.edu.pits.app;

import br.edu.pits.legado.AcessoLegado;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** Executa o mesmo exemplo de uso mostrado no Main da versão refatorada. */
public final class MainLegado {
    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-10-08T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        AcessoLegado sistema = new AcessoLegado(clock);
        var aluno = new AcessoLegado.UsuarioLegado();
        aluno.id = 1L;
        aluno.nome = "Aluno Exemplo";
        aluno.status = "ATIVO";
        aluno.categoria = "ALUNO";
        var credencial = new AcessoLegado.CredencialLegada();
        credencial.id = 1L;
        credencial.identificador = "aluno-ok";
        credencial.tipo = "SENHA";
        credencial.usuario = aluno;
        credencial.expiracao = Instant.parse("2026-10-09T15:00:00Z");
        var ambiente = new AcessoLegado.AmbienteLegado();
        ambiente.id = 10L;
        ambiente.nome = "Sala A";
        ambiente.tipo = "SALA";
        ambiente.status = "ATIVO";
        sistema.credenciais.put(credencial.identificador, credencial);
        sistema.ambientes.put(ambiente.id, ambiente);
        System.out.println("\nANTES DA REFATORAÇÃO | 08/10/2026 às 12h em São Paulo");
        for (String codigo : new String[] {"aluno-ok", "desconhecida", null}) {
            var evento = sistema.processarEvento(codigo, 10L, "ENTRADA_SALA", "MANUAL", "127.0.0.1");
            System.out.printf("Evento %d | credencial=%s | %s | motivo=%s%n",
                    evento.id, codigo, evento.resultado, evento.motivo);
        }
        System.out.println("Eventos registrados: " + sistema.registros.size());
        System.out.println("Auditorias registradas: " + sistema.auditorias.size());
    }
}
