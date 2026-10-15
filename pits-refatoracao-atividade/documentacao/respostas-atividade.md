# Respostas da atividade de refatoração do PITS v2.0

**Atividade de 08/10/2026 — Projeto, Implementação e Teste de Software.**

**Grupo:** Andrey Araújo Borghi; Pedro Henrique Aguiar Carvalho Vieira; Paulo Vinícius Carraro; Matheus Gustavo Pena Luiz.

As respostas se referem ao exemplo Java desta entrega, criado a partir dos anexos. Como não foi fornecido código legado, a versão inicial é uma baseline didática, não uma implementação real da universidade. O diagrama do professor está incluído no projeto e orienta os nomes utilizados.

## 1 O que diferencia refatoração de correção funcional

Refatoração modifica a organização interna do código e preserva o comportamento observado dentro do contrato caracterizado. Correção funcional modifica esse comportamento para eliminar um defeito em relação a um requisito confirmado.

Neste projeto, extrair `PoliticaAutorizacao` de `AcessoLegado`, eliminar os blocos duplicados e separar a persistência são refatorações: os 36 cenários de negócio continuam produzindo o mesmo resultado, motivo, evento e auditoria. A imutabilidade muda a API de encapsulamento para impedir edições diretas, sem alterar as decisões de acesso.

Se uma implementação negasse o acesso exatamente às 06h, alterar a comparação para autorizar nesse limite seria uma correção funcional, pois RB06 determina inclusão do limite. Já estender o horário até 23h59, sem um requisito que determine isso, seria uma alteração de requisito. Cada mudança deve ser registrada e validada separadamente, sem atribuir à refatoração um comportamento que ela não preservou.

## 2 Por que testar antes de refatorar

Os testes de caracterização mostram o que o código inicial realmente faz. Eles oferecem uma referência para detectar se a reorganização mudou uma autorização, uma negativa, o motivo escolhido ou os dados registrados.

Não basta testar somente o caminho de sucesso. O teste T26 combina usuário inativo, credencial expirada, ambiente indisponível e evento inválido; a baseline retorna `USUARIO_INATIVO`. Se o código refatorado validasse a expiração primeiro, a operação continuaria negada, mas o motivo mudaria. O teste revelaria essa diferença.

Executamos os 36 cenários antes de criar a versão refatorada. Depois executamos as mesmas expectativas e comparamos os snapshots completos de evento e auditoria. `Clock.fixed` torna os testes de vencimento e dos limites 06h/23h independentes do horário de execução. O resultado foi 36/36 cenários aprovados em cada versão e nenhuma divergência entre os snapshots comparados.

## 3 Quais princípios SOLID foram realmente úteis

**SRP, responsabilidade única:** `PoliticaAutorizacao` decide; `AccessEventService` coordena; `RepositorioAcesso` persiste. Assim, uma mudança de horário não exige alterar a gravação dos eventos, e a troca de armazenamento não exige reescrever as regras de acesso.

**DIP, inversão de dependência:** o serviço depende de `CatalogoAcesso` e `RepositorioAcesso`, recebidos pelo construtor, e não instancia mapas, banco ou leitores. A implementação em memória pode ser substituída por outro adaptador que cumpra o contrato. O teste E07 injeta um repositório que falha para verificar a propagação do erro.

**OCP, aberto para extensão:** a fronteira `Identificador` permite adicionar um adaptador de leitura que devolva a mesma `Identidade` e gere uma solicitação para o serviço existente. O teste E06 exercita essa fronteira com mocks RFID e biométrico; os adaptadores reais ainda não foram implementados.

Não criamos uma interface para cada classe. A política tem uma implementação concreta porque o recorte não exige múltiplas políticas. ISP e LSP orientam contratos pequenos e substituição correta, mas não os apresentamos como resultados comprovados de implementações externas inexistentes. Para substituir o repositório, por exemplo, a nova versão deverá conservar o salvamento conjunto de evento/auditoria e a leitura não modificável dos registros.

## 4 Que riscos existem em mudar a prioridade das validações

A primeira validação que falha determina o motivo comunicado e auditado. Trocar a ordem pode mudar relatórios, mensagens, diagnósticos e integrações que utilizam esse motivo, mesmo quando o resultado final continua NEGADO.

No exemplo, a sequência é credencial, usuário, expiração, ambiente, tipo de evento e horário do aluno. Ela foi definida para a baseline didática e preservada por guard clauses. T25-T29 verificam as prioridades diante de várias falhas simultâneas. Essa ordem não foi comprovada para um legado real, porque ele não foi fornecido.

Também existe o risco de lançar uma exceção antes de registrar a tentativa. Converter um tipo de evento inválido com `Enum.valueOf` sem tratamento poderia impedir o evento e a auditoria exigidos por RB07. Por isso, `TipoEventoAcesso.doTexto` aceita uma entrada inválida como dado de fronteira, retorna nulo para a política negar no momento correto e preserva o texto original no registro.

Uma mudança intencional de prioridade deve ser discutida como alteração funcional, registrada e acompanhada de novas expectativas de teste. Não deve ocorrer silenciosamente durante a refatoração.

## 5 Como integrar RFID e Fingerprint sem acoplar hardware ao serviço

Cada dispositivo deve ter um adaptador que implemente `Identificador`. O adaptador cuida da comunicação com o equipamento, leitura, erros do SDK e normalização dos dados. Sua saída é uma `Identidade` com identificador da credencial, método de identificação e referência do dispositivo. Esses dados viram uma `SolicitacaoAcesso` para o serviço existente.

O fluxo previsto é: **leitor → adaptador → identidade normalizada → solicitação → `AccessEventService` → `PoliticaAutorizacao` → evento e auditoria**.

`FingerprintAdapter` e `LeitorFingerprint`, presentes no diagrama do professor, permanecem planejados para a evolução do projeto. A comparação biométrica identifica a credencial; a decisão de acesso continua no núcleo. O leitor não deve aplicar as regras de horário, ambiente ou status do usuário.

Exemplo executado no teste E06, usando somente um mock:

```java
Identificador mock = () -> new Identidade(
    "aluno-ok", TipoIdentificacao.BIOMETRIA, "192.0.2.10"
);

EventoAcesso evento = servico.processarEvento(
    mock.identificar().paraSolicitacao(10L, "ENTRADA_SALA")
);
```

O mesmo teste também utiliza `CARTAO_RFID`. O serviço, a política e a persistência continuam iguais nos dois casos. Não há hardware, cadastro biométrico nem serviço externo nesta entrega; o teste demonstra a fronteira de integração e permite desenvolver o núcleo sem um dispositivo físico.
