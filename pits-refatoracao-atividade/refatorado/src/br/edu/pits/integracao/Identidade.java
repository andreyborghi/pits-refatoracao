package br.edu.pits.integracao;

import br.edu.pits.dominio.SolicitacaoAcesso;
import br.edu.pits.dominio.TipoIdentificacao;

/** Saída normalizada de um adaptador; não representa uma autorização de acesso. */
public record Identidade(String identificadorCredencial, TipoIdentificacao metodo,
                         String ipDispositivo) {
    public SolicitacaoAcesso paraSolicitacao(Long ambienteId, String tipoEvento) {
        return new SolicitacaoAcesso(identificadorCredencial, ambienteId, tipoEvento, metodo, ipDispositivo);
    }
}
