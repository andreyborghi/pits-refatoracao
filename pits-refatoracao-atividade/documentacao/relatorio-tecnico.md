# Relatório técnico da refatoração do PITS v2.0

**Disciplina:** Projeto, Implementação e Teste de Software, Engenharia de Software, Unicesumar.

**Professor:** Cesar Malvezi.

**Integrantes:** Andrey Araújo Borghi; Pedro Henrique Aguiar Carvalho Vieira; Paulo Vinícius Carraro; Matheus Gustavo Pena Luiz.

**Atividade:** aula de 08/10/2026, módulo Acesso e Monitoramento, Java 17.

## Contexto e conclusão

Separamos a política de autorização, a orquestração e a persistência de um exemplo didático baseado no PITS v2.0. Os 36 cenários da baseline passaram na versão refatorada e os registros de evento e auditoria permaneceram equivalentes nos casos comparados. Sete testes adicionais verificaram encapsulamento, histórico, tentativas sucessivas, entrada nula, identificação simulada e propagação de falha de persistência.

Os anexos não fornecem uma implementação Java legada. `AcessoLegado` é uma reconstrução didática deliberadamente concentrada em uma classe, com problemas estruturais para diagnóstico. Portanto, os resultados representam este exemplo; para refatorar um legado real, seria necessário executar a caracterização contra aquele código e conferir sua prioridade efetiva de validação.

Usamos três fontes: a atividade avaliativa em PDF, a atividade prática de 08/10/2026 em DOCX e o diagrama de referência fornecido pelo professor. O DOCX define o escopo obrigatório desta implementação parcial. O diagrama foi incluído em `diagrama-professor.png` e orienta os nomes e as fronteiras do modelo.

## Baseline e ordem das validações

A ordem abaixo foi definida na construção do exemplo, executada no legado e preservada na refatoração. Ela não foi inferida como uma ordem comprovada do sistema real.

| Prioridade | Regra | Comportamento |
| --- | --- | --- |
| 1 | RB01 | Credencial nula, vazia ou somente com espaços: `CREDENCIAL_VAZIA`; não encontrada: `CREDENCIAL_INEXISTENTE` |
| 2 | RB02 | Usuário ausente: `USUARIO_AUSENTE`; status diferente de ATIVO, inclusive nulo: `USUARIO_INATIVO` |
| 3 | RB03 | Credencial vencida: `CREDENCIAL_EXPIRADA` |
| 4 | RB04 | Ambiente nulo, desconhecido ou com status diferente de ATIVO: `AMBIENTE_INDISPONIVEL` |
| 5 | RB05 | Tipo nulo ou diferente dos quatro valores previstos: `EVENTO_INVALIDO` |
| 6 | RB06 | Aluno fora do intervalo autorizado: `ALUNO_FORA_HORARIO` |
| Após decidir | RB07 | Criar evento e auditoria tanto para autorização quanto para negativa |
| Durante a comparação | RB08 | Confirmar que a primeira falha e os registros continuam iguais |

`AUTORIZADO / ACESSO_AUTORIZADO` é retornado somente quando todas as validações passam e o registro foi salvo. Um evento inválido não provoca exceção de conversão de enum: o valor recebido permanece registrado em `tipoInformado`, e `tipo` fica nulo.

### Decisões explicitadas para o exemplo

- O horário é avaliado em `America/Sao_Paulo`. O intervalo adotado é **06:00:00 até 23:00:00, inclusive**; uma fração de segundo após 23:00:00 está fora. A granularidade deve ser confirmada antes de aplicar esta decisão em um legado real.
- A credencial expira **no instante indicado**: `agora >= dataExpiracao` significa vencida. Vencimento nulo significa sem prazo neste exemplo; essa convenção também precisa ser confirmada para uso real.
- Identificadores de credencial e nomes de eventos são sensíveis a maiúsculas e minúsculas. Não introduzimos normalização de strings durante a refatoração.
- `CategoriaUsuario` foi acrescentada ao recorte para identificar alunos e aplicar RB06. Ela é um dado de domínio, não uma regra nova de autorização. Os cadastros da demonstração sempre informam uma categoria conhecida.
- `TipoIdentificacao` é um metadado tipado da solicitação; os métodos reconhecidos do diagrama e o padrão MANUAL estão cobertos. Texto arbitrário no campo de método do legado didático não faz parte do novo contrato Java e não está coberto pela equivalência. Isso é uma restrição explícita da API interna, distinta dos tipos de evento inválidos, que continuam aceitos como entrada para serem negados e auditados.
- Não adicionamos validação de credencial inativa, compatibilidade evento/ambiente, capacidade, máquina de estados ou frequência acadêmica: essas regras não estão definidas entre RB01-RB08 do módulo parcial. Caso sejam incorporadas, exigem evolução funcional com novos testes.

## Etapa A Diagnóstico dos Code Smells

Todas as localizações abaixo se referem a `antes/src/br/edu/pits/legado/AcessoLegado.java`, preservado na entrega.

| ID | Arquivo ou método e localização | Code Smell | Impacto ou risco | Refatoração e justificativa | Evidência ou teste |
| --- | --- | --- | --- | --- | --- |
| CS01 | `processarEvento`, linhas 23 a 116 | Long Method | Uma alteração de regra pode afetar registro ou auditoria no mesmo método | Extract Class e Extract Method: política, serviço e repositório têm motivos diferentes para mudar | T01-T36 e comparação integral antes/depois |
| CS02 | Validações, linhas 31 a 68 | Condicionais profundamente aninhadas | Dificulta enxergar a prioridade e aumenta o risco de deslocar um ramo | Replace Nested Conditional with Guard Clauses, mantendo a mesma sequência | T25-T29 verificam a primeira falha entre várias |
| CS03 | `status`, `categoria`, `tipo` e `resultado` como strings | Primitive Obsession | Estados e tipos sem significado no sistema de tipos | Enums e objetos de domínio; o DTO conserva o tipo de evento bruto para registrar entradas inválidas | T14-T16, T30-T34 |
| CS04 | Literais repetidos nas linhas 28 a 60 e 79 a 112 | Magic Strings | Um erro de grafia pode mudar silenciosamente uma decisão ou registro | `StatusEntidade`, `TipoEventoAcesso`, `ResultadoAcesso`, `MotivoAcesso` e constante da operação auditada | T01-T36 verificam resultado e motivo exatos |
| CS05 | Horários 6 e 23 nas linhas 55 e 56 | Magic Numbers | Os limites estão embutidos em uma expressão extensa | Constantes `INICIO_ACESSO_ALUNO` e `FIM_ACESSO_ALUNO`, com nomes do domínio | T17-T22, incluindo os dois limites |
| CS06 | Campos públicos nas linhas 13 a 17 e 118 a 159 | Mutable Public State | Chamadores podem alterar cadastros, eventos e auditorias sem validação | Records imutáveis, campos privados, cópia dos mapas e snapshots não modificáveis | E01 e E02 |
| CS07 | Dois blocos de registro, linhas 71 a 114 | Duplicated Code | Um novo campo pode ser registrado em autorizações e esquecido nas negativas | Um único caminho `criarEvento` e `RegistroAcesso.doEvento` | Todos os casos conferem um evento e uma auditoria; E03 |
| CS08 | Acesso direto a `c.usuario.status`, `c.usuario.categoria` e `c.expiracao` | Feature Envy | O serviço concentra conhecimento interno de outras entidades | Encapsular comportamento em `Usuario.estaAtivo`, `Usuario.ehAluno` e `Credencial.estaExpirada` | T06-T10, T17-T24 |
| CS09 | Método que consulta, decide, monta evento e grava logs | Mixed Responsibilities | Persistência e regras se misturam; difícil testar isoladamente | `PoliticaAutorizacao` decide; `AccessEventService` coordena; `RepositorioAcesso` salva | Comparações T01-T36 e falha de persistência E07 |
| CS10 | Mapas/listas concretos e duas coleções de log, linhas 13 a 16, 83 a 91 e 105 a 113 | Acoplamento à infraestrutura e Parallel Collections | Trocar armazenamento exige alterar o serviço; coleções podem ficar inconsistentes | Injeção de `CatalogoAcesso` e `RepositorioAcesso`; `RegistroAcesso` reúne evento e auditoria em uma operação | E01, E03 e E07 |

O relógio já é injetado na baseline didática para permitir caracterização determinística. A refatoração conserva essa possibilidade; não atribuímos à versão nova a criação de um recurso que já existia no exemplo inicial.

## Etapa B Testes de caracterização

Primeiro compilamos e executamos os 36 casos com `CaracterizacaoLegadoTest`. Depois utilizamos os mesmos dados e expectativas com `RegressaoTest`. Os adaptadores de teste apenas constroem cadastros e normalizam resultados; as regras de autorização não são reimplementadas nos testes.

Cada cenário verifica resultado, motivo, identificador do evento, instante, credencial informada, usuário, ambiente solicitado, tipo recebido, método e IP. Também confirma um registro e uma auditoria, além do vínculo, instante, usuário, operação, resultado e motivo auditados. Assim, as negativas são verificadas com os mesmos critérios das autorizações.

| Casos | Cobertura |
| --- | --- |
| T01-T05 | Sucesso e credencial desconhecida, nula, vazia ou em branco |
| T06-T10 | Usuário inativo/ausente; vencimento anterior, exato e ausente |
| T11-T16 | Ambiente inativo/desconhecido/nulo; evento inválido/nulo/em minúsculas |
| T17-T22 | Antes de 06h, 06h exatas, após 06h, antes de 23h, 23h exatas e após 23h |
| T23-T24 | Restrição de horário é aplicada a alunos, não a professor/operador |
| T25-T29 | Prioridade das validações diante de falhas simultâneas |
| T30-T31 | Status nulo de usuário e ambiente |
| T32-T34 | Outros tipos válidos de evento |
| T35-T36 | Identificador com caixa diferente e dados todos nulos |

`Clock.fixed` fixa cada instante de teste. Não há dependência do horário em que o avaliador executa a entrega. Falhas lançam `AssertionError` e produzem saída diferente de zero.

## Etapa C Arquitetura refatorada

`AccessEventService` recebe uma solicitação, captura um único instante, consulta `CatalogoAcesso`, solicita a decisão da `PoliticaAutorizacao`, cria `EventoAcesso` e entrega `RegistroAcesso` ao repositório. O serviço retorna depois da gravação. `Auditoria.doEvento` utiliza os mesmos dados, evitando divergência de resultado ou motivo.

`PoliticaAutorizacao` não acessa dispositivos ou persistência. Suas guard clauses preservam a ordem caracterizada. As entidades mantêm consultas de comportamento local, como verificar status e expiração. Os dados de evento e auditoria são snapshots imutáveis.

`RepositorioAcessoEmMemoria` salva o par evento/auditoria como um único elemento de uma lista privada e devolve cópias não modificáveis. Uma futura implementação com banco deve gravar esse par na mesma transação. A versão atual não demonstra durabilidade, recuperação de desastre ou transações reais de banco.

### Relação com o diagrama do professor

| Elemento da referência | Tratamento nesta atividade |
| --- | --- |
| `Usuario` | Uma única representação do recorte de acesso, com categoria para RB06; não duplica a classe por aparecer em duas áreas do desenho |
| `Credencial` | Identificador, tipo, vínculo opcional com usuário e vencimento; cadastro/ativação completos ficam fora do módulo parcial |
| `Ambiente` | Identificação, tipo e disponibilidade; decisão de aceitar o evento fica na política |
| `AccessEventService` | Nome preservado; coordena processamento e consulta histórico; não implementa agora `validarSequencia`, correções ou exportação |
| `EventoAcesso` | Registro imutável da tentativa, com resultado, motivo e dados originais; persistência retirada da entidade |
| `Auditoria` | Registro imutável; gravação coordenada pelo repositório junto com o evento |
| `HistoricoAcesso` | Consulta realizada por `obterHistorico`; não criamos uma classe sem comportamento adicional |
| `DispositivoAcesso` e Fingerprint | Integração futura; `Identificador` e `Identidade` oferecem a fronteira normalizada, com mocks nos testes |
| Acadêmico, Segurança, XML/JSON e Studeo | Documentados na referência; nenhuma integração externa implementada |

As associações com usuário/credencial/ambiente podem estar ausentes em uma tentativa negada. Por isso, o UML do recorte utiliza referências opcionais onde necessário e conserva o identificador recebido. Exigir uma credencial válida para construir todo evento impediria registrar uma credencial inexistente, contrariando RB07.

### Cinco mudanças justificadas e comparação Antes x Depois

| Melhoria | Antes | Depois | Justificativa e evidência |
| --- | --- | --- | --- |
| 1 Separação de responsabilidades | Um método consulta cadastros, valida e grava | Política, serviço e repositório separados | Regras podem evoluir sem alterar armazenamento; T01-T36 e E07 |
| 2 Fluxo explícito | Vários níveis de `if/else` | Guard clauses na mesma prioridade | Facilita leitura sem mudar o primeiro motivo de negativa; T25-T29 |
| 3 Tipos e nomes do domínio | Strings de status, categoria, resultado e evento | Enums; solicitação conserva texto bruto do evento | Reduz erros internos e mantém auditoria de entrada inválida; T14-T16 e T30-T34 |
| 4 Encapsulamento | Eventos, auditorias e listas públicos e mutáveis | Records e snapshots não modificáveis | Protege registros anteriores de alterações por chamadores; E01-E02 |
| 5 Persistência unificada e substituível | Dois blocos duplicados e duas listas | Uma operação com `RegistroAcesso`, atrás de interface | Autorizações e negativas usam o mesmo caminho; E03 e E07 |

## Etapa D Validação e classificação das diferenças

Ambas as versões foram compiladas com Java 17, `--release 17`, `-Xlint:all` e `-Werror`. Não houve erro nem aviso na compilação. O log completo está em `resultado-execucao.txt`.

| Verificação | Antes | Depois |
| --- | --- | --- |
| Cenários de negócio e dados nulos | 36/36 | 36/36 |
| Comparação dos snapshots de evento e auditoria | Baseline | 36/36 equivalentes |
| Testes adicionais | Não aplicável à estrutura antiga | 7/7 |
| Divergências observadas nos 36 cenários | Referência | 0 |

| Diferença ou proposta | Classificação | Tratamento |
| --- | --- | --- |
| Extração da política, guard clauses e retirada de duplicação | Refatoração | Executada; resultados e registros preservados nos casos comparados |
| Records, snapshots e coleção não modificável | Refatoração com mudança da API de encapsulamento | Executada; chamadores já não podem editar diretamente o histórico |
| Enums em metadados e status | Refatoração com restrição explícita da API interna | Executada para valores do domínio; strings arbitrárias de método/cadastro exigem adaptação na fronteira e não foram consideradas equivalentes |
| Correção de defeito nas regras RB01-RB06 | Correção funcional | Nenhuma foi executada entre as duas versões |
| Aceitar `entrada_sala` ou ampliar o fim para 23h59 | Alteração de requisito ou correção após confirmar a baseline real | Não executada; não seria apenas refatoração |
| Implementar estados FORA_CAMPUS/DENTRO_CAMPUS/DENTRO_SALA | Evolução funcional do módulo parcial | Não executada; prevista no projeto completo do PDF |
| Tratar indisponibilidade de um repositório real | Evolução de infraestrutura | Apenas propagação testada; gravação durável/retry não implementados |

O teste E07 confirma que uma falha de gravação não é mascarada por um retorno AUTORIZADO. Nessa situação, não há garantia de registro durável na implementação em memória; o erro é propagado para que uma futura camada de operação trate a indisponibilidade. Não apresentamos esse cenário como prova de cumprimento de RB07 em uma falha real de banco.

## Etapa E Integração futura e defesa técnica

O contrato `Identificador.identificar()` devolve uma `Identidade` normalizada. Um futuro `FingerprintAdapter` encapsularia leitura, qualidade da amostra, integração com o SDK e conversão para um identificador de credencial. Um adaptador RFID faria a mesma normalização para seu leitor. A identidade é convertida em `SolicitacaoAcesso` e enviada ao mesmo serviço, política e repositório.

A biometria não concede acesso diretamente: a política continua verificando credencial, usuário, vencimento, ambiente, tipo e horário. O teste E06 usa lambdas como mocks para CARTAO_RFID e BIOMETRIA, sem hardware ou serviço externo. Ele comprova a compatibilidade do contrato simulado com o módulo; não valida um leitor real.

Para a defesa, apresentar o diagrama original, o UML antes/depois, a tabela das cinco mudanças, a execução de `bash verificar.sh testes` e o exemplo T26 de prioridade. As cinco respostas solicitadas estão em `respostas-atividade.md`. 

## Riscos remanescentes e evolução

O legado real não foi fornecido, então a primeira evolução é substituir a baseline didática pela implementação efetiva e caracterizar seu comportamento. As convenções de expiração, horário, categoria e normalização precisam ser confirmadas nessa etapa. O catálogo interno tipado não é um parser de cadastros externos; dados desconhecidos precisam de validação em uma futura fronteira de entrada.

A persistência em memória perde dados ao encerrar o processo. O módulo não possui autorização de operadores, política de retenção, controle de idempotência, máquina de estados, cálculo de permanência, correções históricas ou adaptadores físicos. Essas funcionalidades exigem requisitos e testes próprios, além de implementação transacional quando houver armazenamento durável. A refatoração prepara as fronteiras do módulo sem declarar essas funcionalidades concluídas.
