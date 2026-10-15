package br.edu.pits.dominio;

public record Usuario(Long id, String nome, StatusEntidade status, CategoriaUsuario categoria) {
    public boolean estaAtivo() { return status == StatusEntidade.ATIVO; }
    public boolean ehAluno() { return categoria == CategoriaUsuario.ALUNO; }
}
