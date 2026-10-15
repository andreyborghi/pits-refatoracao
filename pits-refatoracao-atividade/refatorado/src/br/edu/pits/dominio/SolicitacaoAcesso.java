package br.edu.pits.dominio;

/** DTO da fronteira; strings inválidas são preservadas para registrar a tentativa negada. */
public record SolicitacaoAcesso(String identificadorCredencial, Long ambienteId,
                               String tipoInformado, TipoIdentificacao metodo,
                               String ipDispositivo) {
    public SolicitacaoAcesso {
        if (metodo == null) metodo = TipoIdentificacao.MANUAL;
    }
}
