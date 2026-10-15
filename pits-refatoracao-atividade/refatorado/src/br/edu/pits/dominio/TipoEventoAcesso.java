package br.edu.pits.dominio;

public enum TipoEventoAcesso {
    ENTRADA_CAMPUS, SAIDA_CAMPUS, ENTRADA_SALA, SAIDA_SALA;

    /** Entrada inválida permanece inválida; não normaliza caixa nem remove espaços. */
    public static TipoEventoAcesso doTexto(String texto) {
        if (texto == null) return null;
        try {
            return valueOf(texto);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
