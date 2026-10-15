package br.edu.pits.dominio;

public record Ambiente(Long id, String nome, TipoAmbiente tipo, StatusEntidade status) {
    public boolean estaDisponivel() { return status == StatusEntidade.ATIVO; }
}
