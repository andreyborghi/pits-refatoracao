package br.edu.pits.servico;

import br.edu.pits.dominio.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/** Política pura: não consulta repositórios, não grava e não controla dispositivos. */
public final class PoliticaAutorizacao {
    private static final LocalTime INICIO_ACESSO_ALUNO = LocalTime.of(6, 0);
    private static final LocalTime FIM_ACESSO_ALUNO = LocalTime.of(23, 0);
    private final ZoneId zonaCampus;

    public PoliticaAutorizacao(ZoneId zonaCampus) {
        this.zonaCampus = Objects.requireNonNull(zonaCampus);
    }

    public DecisaoAcesso avaliar(SolicitacaoAcesso solicitacao, Credencial credencial,
                                 Ambiente ambiente, Instant agora) {
        String codigo = solicitacao.identificadorCredencial();
        if (codigo == null || codigo.isBlank()) return DecisaoAcesso.negar(MotivoAcesso.CREDENCIAL_VAZIA);
        if (credencial == null) return DecisaoAcesso.negar(MotivoAcesso.CREDENCIAL_INEXISTENTE);
        if (credencial.usuario() == null) return DecisaoAcesso.negar(MotivoAcesso.USUARIO_AUSENTE);
        Usuario usuario = credencial.usuario();
        if (!usuario.estaAtivo()) return DecisaoAcesso.negar(MotivoAcesso.USUARIO_INATIVO);
        if (credencial.estaExpirada(agora)) return DecisaoAcesso.negar(MotivoAcesso.CREDENCIAL_EXPIRADA);
        if (ambiente == null || !ambiente.estaDisponivel()) return DecisaoAcesso.negar(MotivoAcesso.AMBIENTE_INDISPONIVEL);
        if (TipoEventoAcesso.doTexto(solicitacao.tipoInformado()) == null) return DecisaoAcesso.negar(MotivoAcesso.EVENTO_INVALIDO);
        if (usuario.ehAluno() && foraDoHorarioAluno(agora)) return DecisaoAcesso.negar(MotivoAcesso.ALUNO_FORA_HORARIO);
        return DecisaoAcesso.autorizar();
    }

    private boolean foraDoHorarioAluno(Instant agora) {
        LocalTime hora = LocalTime.ofInstant(agora, zonaCampus);
        return hora.isBefore(INICIO_ACESSO_ALUNO) || hora.isAfter(FIM_ACESSO_ALUNO);
    }
}
