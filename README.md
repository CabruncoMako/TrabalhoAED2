# TrabalhoAED2
SDN-Scale: AVL vs Red-Black - Otimização de Roteamento e Análise de Trade-offs

> Comparativo de desempenho entre Árvore AVL e Árvore Red-Black aplicado a um sistema de roteamento SDN com regras de firewall em escala de nanossegundos.

Disciplina: Estrutura de Dados II  
Professor: Ricardo Sekeff  
Instituição: iCEV – Instituto de Ensino Superior, Teresina – PI  
Integrante 1: Pablo Canavarro  
Integrante 2: Gabriel Batista  
Integrante 3: Bruno Barbosa

---

## Descrição

Este projeto implementa e compara duas estruturas de dados balanceadas — **AVL** e **Red-Black (Rubro-Negra)** — aplicadas ao gerenciamento de regras de firewall em um Load Balancer SDN (Software-Defined Networking).

Cada regra é representada pelo objeto `PacketRule` (ID, IP de origem, IP de destino, prioridade). A **prioridade** é usada como chave de ordenação nas árvores, simulando o conceito de *Longest Prefix Match* utilizado em tabelas de fluxo SDN reais.

O objetivo é determinar qual estrutura oferece menor latência em escala de nanossegundos para um sistema com alta volatilidade de dados (inserções e remoções frequentes).

---

## Estrutura do Repositório

```
SDN-Scale/
│
├── PacketRule.java               # Modelo de dados: ID, IP origem/destino, prioridade
├── ArvoreAVL.java                # Árvore AVL — invariante |FB| ≤ 1
├── ArvoreRubroNegra.java         # Árvore Red-Black — 5 propriedades de coloração
│
├── StressTest.java               # [Integrante 2] Testes de carga com 100k entradas
├── Graficos.java                 # [Integrante 2] Geração de gráficos comparativos
│
├── VerificadorAVL.java           # [Integrante 3] Verificador de invariantes da AVL
├── VerificadorRBT.java           # [Integrante 3] Verificador das 5 propriedades RBT
├── AuditoriaComparativa.java     # [Integrante 3] Análise comparativa AVL vs RBT
├── CODE_REVIEW.md                # [Integrante 3] Code review do código do Integrante 1
├── CODE_REVIEW_STRESSTEST.md     # [Integrante 3] Code review do stress test do Integrante 2
│
├── postmortem_corrigido.pdf      # Relatório Post-Mortem (modelo SBC)
└── README.md
```

---

## Como Compilar e Executar

**Pré-requisito:** Java 11 ou superior.

```bash
# Compilar todos os arquivos
javac PacketRule.java ArvoreAVL.java ArvoreRubroNegra.java

# Executar o stress test completo
javac StressTest.java
java StressTest

# Executar os verificadores de invariantes
javac VerificadorAVL.java VerificadorRBT.java AuditoriaComparativa.java
java AuditoriaComparativa
```

---

## Metodologia dos Testes

| Parâmetro | Valor |
|---|---|
| Linguagem | Java 21 |
| Seed | `42` |
| Volume de dados | 100.000 entradas ordenadas |
| Remoção simulada | 20% dos nós (expiração de regras) |
| Métrica de tempo | Nanossegundos (ns) |
| Execuções por teste | 5 (média aritmética) |

A mesma seed foi usada em ambas as estruturas para garantir comparabilidade absoluta dos resultados.

---

## Resultados

| Operação | AVL | Red-Black |
|---|---|---|
| Inserção (100k nós) | ~4.200 ns/op | ~2.900 ns/op |
| Busca (avg, 100k) | ~310 ns/op | ~340 ns/op |
| Remoção (20k nós) | ~5.100 ns/op | ~3.200 ns/op |
| Total de rotações | ~28.400 | ~17.600 |
| Altura final | ~17 níveis | ~24 níveis |
| Recolorações | N/A | ~31.000 |
| Invariante pós-remoção | ✅ \|FB\| ≤ 1 | ✅ 5 props |

**Conclusão:** a Red-Black foi ~31% mais rápida na inserção e ~37% mais rápida na remoção. A AVL teve leve vantagem em busca (~9%), mas num sistema write-intensive como este a Red-Black é a escolha correta.

---

## Implementação

### PacketRule
Representa uma regra de firewall com os campos `id`, `ipOrigem`, `ipDestino` e `prioridade`. Os campos `id` e `prioridade` são `final` para impedir mutação após a criação. O método `getValor()` retorna a prioridade, que é a chave de ordenação usada pelas árvores.

### ArvoreAVL
Árvore estritamente balanceada com invariante `|FB| ≤ 1` em todo nó. Implementa:
- Rotação simples à esquerda (caso RR)
- Rotação simples à direita (caso LL)
- Rotação dupla Esquerda-Direita (caso LR)
- Rotação dupla Direita-Esquerda (caso RL)
- Rebalanceamento automático após inserção e remoção
- Contador `totalRotacoes` para auditoria

### ArvoreRubroNegra
Árvore Red-Black com as 5 propriedades fundamentais garantidas. Implementa:
- Nó sentinela NIL único (propriedade 3)
- `corrigirInsercao()` com os 3 casos nos dois espelhos
- `corrigirRemocao()` com os 4 casos de double-black nos dois espelhos
- Contadores `totalRotacoes` e `totalRecoloracoes` para auditoria

### VerificadorAVL
Auditor de invariantes da ArvoreAVL. Verifica:
- `|FB| ≤ 1` em todos os nós
- FB armazenado igual ao FB calculado
- Propriedade BST (chave esq < raiz < chave dir)
- Altura consistente em toda a árvore

### VerificadorRBT
Auditor das 5 propriedades fundamentais da Red-Black Tree. Verifica:
- P2: raiz é preta
- P4: filhos de nó vermelho são sempre pretos
- P5: black-height uniforme em todos os caminhos raiz→NIL
- Propriedade BST

### AuditoriaComparativa
Ponto de entrada unificado para auditoria. Executa inserção e remoção de 20% com seed fixa, valida as invariantes de ambas as árvores e imprime tabela comparativa de rotações e trade-offs.

---

## Divisão de Tarefas

| Branch | Integrante | Responsabilidade |
|---|---|---|
| `integrante-1/estruturas` | Pablo Canavarro | `PacketRule`, `ArvoreAVL`, `ArvoreRubroNegra` |
| `feature/stress-test` | Gabriel Batista | Testes de carga, coleta de tempos em ns, gráficos |
| `Verificação` | Bruno Barbosa | Verificadores de invariantes, code reviews, análise de trade-offs |

O merge de cada branch na `main` só foi autorizado após aprovação do Integrante 3 (code review obrigatório).
