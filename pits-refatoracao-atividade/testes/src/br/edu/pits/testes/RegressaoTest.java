package br.edu.pits.testes;

public final class RegressaoTest {
    public static void main(String[] args) {
        ContratoTeste.executar("REFATORADO", AdaptadorRefatoradoTeste::new);
        int comparacoes = 0;
        for (var caso : ContratoTeste.casos()) {
            var antes = ContratoTeste.executarCaso(new AdaptadorLegadoTeste(caso.clock()), caso);
            var depois = ContratoTeste.executarCaso(new AdaptadorRefatoradoTeste(caso.clock()), caso);
            ContratoTeste.igual(antes, depois, caso.id() + " equivalência completa antes/depois");
            comparacoes++;
        }
        System.out.println("EQUIVALÊNCIA: " + comparacoes + "/" + ContratoTeste.casos().size()
                           + " comparações de evento e auditoria aprovadas; 0 divergências.");
    }
}
