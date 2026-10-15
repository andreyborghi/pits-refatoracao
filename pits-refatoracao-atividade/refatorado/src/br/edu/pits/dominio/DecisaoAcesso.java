package br.edu.pits.dominio;

public record DecisaoAcesso(ResultadoAcesso resultado, MotivoAcesso motivo) {
    public static DecisaoAcesso negar(MotivoAcesso motivo) {
        return new DecisaoAcesso(ResultadoAcesso.NEGADO, motivo);
    }
    public static DecisaoAcesso autorizar() {
        return new DecisaoAcesso(ResultadoAcesso.AUTORIZADO, MotivoAcesso.ACESSO_AUTORIZADO);
    }
}
