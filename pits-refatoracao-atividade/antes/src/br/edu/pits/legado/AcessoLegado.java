package br.edu.pits.legado;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Legado DIDÁTICO reconstruído das regras RB01-RB08; não foi fornecido pelo professor. */
public class AcessoLegado {
    public Map<String, CredencialLegada> credenciais = new HashMap<>();
    public Map<Long, AmbienteLegado> ambientes = new HashMap<>();
    public List<RegistroLegado> registros = new ArrayList<>();
    public List<AuditoriaLegada> auditorias = new ArrayList<>();
    public Clock clock;

    public AcessoLegado(Clock clock) {
        this.clock = clock;
    }

    public RegistroLegado processarEvento(String codigo, Long ambienteId, String tipo,
                                         String metodo, String ipDispositivo) {
        Instant agora = clock.instant();
        CredencialLegada c = codigo == null ? null : credenciais.get(codigo);
        AmbienteLegado a = ambienteId == null ? null : ambientes.get(ambienteId);
        String resultado = "NEGADO";
        String motivo;
        // A prioridade abaixo é a baseline do exemplo, caracterizada ANTES da refatoração.
        if (codigo == null || codigo.isBlank()) {
            motivo = "CREDENCIAL_VAZIA";
        } else {
            if (c == null) {
                motivo = "CREDENCIAL_INEXISTENTE";
            } else {
                if (c.usuario == null) {
                    motivo = "USUARIO_AUSENTE";
                } else {
                    if (!"ATIVO".equals(c.usuario.status)) {
                        motivo = "USUARIO_INATIVO";
                    } else {
                        if (c.expiracao != null && !agora.isBefore(c.expiracao)) {
                            motivo = "CREDENCIAL_EXPIRADA";
                        } else {
                            if (a == null || !"ATIVO".equals(a.status)) {
                                motivo = "AMBIENTE_INDISPONIVEL";
                            } else {
                                if (!("ENTRADA_CAMPUS".equals(tipo) || "SAIDA_CAMPUS".equals(tipo)
                                      || "ENTRADA_SALA".equals(tipo) || "SAIDA_SALA".equals(tipo))) {
                                    motivo = "EVENTO_INVALIDO";
                                } else {
                                    LocalTime hora = LocalTime.ofInstant(agora, clock.getZone());
                                    if ("ALUNO".equals(c.usuario.categoria)
                                            && (hora.isBefore(LocalTime.of(6, 0))
                                            || hora.isAfter(LocalTime.of(23, 0)))) {
                                        motivo = "ALUNO_FORA_HORARIO";
                                    } else {
                                        resultado = "AUTORIZADO";
                                        motivo = "ACESSO_AUTORIZADO";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Duplicação intencional para a atividade: registrar também as negativas.
        if ("AUTORIZADO".equals(resultado)) {
            RegistroLegado r = new RegistroLegado();
            r.id = registros.size() + 1L;
            r.dataHora = agora;
            r.codigoCredencial = codigo;
            r.usuarioId = c.usuario.id;
            r.ambienteId = ambienteId;
            r.tipoInformado = tipo;
            r.metodo = metodo == null ? "MANUAL" : metodo;
            r.ipDispositivo = ipDispositivo;
            r.resultado = resultado;
            r.motivo = motivo;
            registros.add(r);
            AuditoriaLegada audit = new AuditoriaLegada();
            audit.eventoId = r.id;
            audit.dataHora = agora;
            audit.usuarioId = r.usuarioId;
            audit.operacao = "PROCESSAR_EVENTO_ACESSO";
            audit.resultado = resultado;
            audit.motivo = motivo;
            auditorias.add(audit);
            return r;
        } else {
            RegistroLegado r = new RegistroLegado();
            r.id = registros.size() + 1L;
            r.dataHora = agora;
            r.codigoCredencial = codigo;
            r.usuarioId = c == null || c.usuario == null ? null : c.usuario.id;
            r.ambienteId = ambienteId;
            r.tipoInformado = tipo;
            r.metodo = metodo == null ? "MANUAL" : metodo;
            r.ipDispositivo = ipDispositivo;
            r.resultado = resultado;
            r.motivo = motivo;
            registros.add(r);
            AuditoriaLegada audit = new AuditoriaLegada();
            audit.eventoId = r.id;
            audit.dataHora = agora;
            audit.usuarioId = r.usuarioId;
            audit.operacao = "PROCESSAR_EVENTO_ACESSO";
            audit.resultado = resultado;
            audit.motivo = motivo;
            auditorias.add(audit);
            return r;
        }
    }

    public static class UsuarioLegado {
        public Long id;
        public String nome;
        public String status;
        public String categoria;
    }

    public static class CredencialLegada {
        public Long id;
        public String identificador;
        public String tipo;
        public UsuarioLegado usuario;
        public Instant expiracao;
    }

    public static class AmbienteLegado {
        public Long id;
        public String nome;
        public String tipo;
        public String status;
    }

    public static class RegistroLegado {
        public Long id;
        public Instant dataHora;
        public String codigoCredencial;
        public Long usuarioId;
        public Long ambienteId;
        public String tipoInformado;
        public String metodo;
        public String ipDispositivo;
        public String resultado;
        public String motivo;
    }

    public static class AuditoriaLegada {
        public Long eventoId;
        public Instant dataHora;
        public Long usuarioId;
        public String operacao;
        public String resultado;
        public String motivo;
    }
}
