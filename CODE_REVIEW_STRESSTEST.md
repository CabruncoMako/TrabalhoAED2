# Code Review — StressTest.java

**Autor da revisão:** Integrante 3 — QA & Analytics  
**Commit sugerido:** `review(QA): auditoria do stress test Integrante 2`  
**Arquivo revisado:** `StressTest.java`  
**Branch auditada:** `feature/stress-test`

---

## 1. Configuração Geral do Teste

| Item | Status | Observação |
|------|--------|------------|
| Seed fixa (`SEED = 42L`) | ✅ OK | Garante reprodutibilidade — ambas as árvores recebem os mesmos dados |
| Seed separada para buscas (`SEED + 1`) e remoções (`SEED + 2`) | ✅ OK | Boa prática: evita que os valores buscados/removidos coincidam sempre com os inseridos |
| Volumes testados: 10k e 100k | ⚠️ ATENÇÃO | O cabeçalho e `imprimirPostMortemDados()` mencionam **1M** (`"Volumes: 10k \| 100k \| 1M"`), mas o array `VOLUMES` só contém `{ 10_000, 100_000 }`. O volume de 1.000.000 está **ausente** no código. Isso torna o cabeçalho enganoso. **Recomendação:** adicionar `1_000_000` ao array ou remover a menção ao 1M. |
| Remoção de 20% | ✅ OK | `nRemocao = (int)(n * 0.20)` — cálculo correto |

---

## 2. Medição de Tempo

| Item | Status | Observação |
|------|--------|------------|
| Uso de `System.nanoTime()` | ✅ OK | Correto para medições de alta precisão em Java |
| Tempo total de inserção medido em ns | ✅ OK | Mede o bloco completo de inserção |
| Busca média calculada dividindo pelo número de buscas | ✅ OK | `/ N_BUSCAS` está correto |
| Tempo total de remoção medido em ns | ✅ OK | Bloco de remoção isolado corretamente |
| JVM warm-up não considerado | ⚠️ ATENÇÃO | Na JVM, as primeiras execuções são mais lentas porque o JIT (compilador Just-In-Time) ainda não otimizou o código. Isso afeta especialmente o volume de 10k, que roda primeiro. Os números de 10k tendem a ser **artificialmente mais lentos** que o esperado em produção. Para um trabalho acadêmico é aceitável, mas deve ser **mencionado como limitação** no artigo. |
| Sem `Thread.sleep()` entre testes | 🟢 OK | Não é necessário para este cenário |

---

## 3. Geração de Dados

| Item | Status | Observação |
|------|--------|------------|
| Valores gerados com `rng.nextInt(10_000_000)` | ⚠️ ATENÇÃO | O `AuditoriaComparativa.java` do Integrante 3 usa `rng.nextInt(100_000)` (100 mil). O StressTest usa `rng.nextInt(10_000_000)` (10 milhões). Os intervalos são **diferentes** — isso não é um erro, mas significa que os dados dos dois testes não são diretamente comparáveis. Deve ser documentado no artigo. |
| Mesmo array de valores para AVL e RBT | ✅ OK | `valores[]` é reaproveitado nos dois blocos — comparação justa |
| IDs únicos por posição (`i + 1`) | ✅ OK | Sem colisão de IDs |

---

## 4. Integração com os Verificadores (QA)

| Item | Status | Observação |
|------|--------|------------|
| `VerificadorAVL.imprimirRelatorio(avl)` chamado após remoção | ✅ OK | Auditoria de invariantes pós-stress correta |
| `VerificadorRBT.imprimirRelatorio(rbt)` chamado após remoção | ✅ OK | Idem |
| Captura de `IllegalStateException` | ⚠️ PROBLEMA | `VerificadorAVL` e `VerificadorRBT` **não lançam** `IllegalStateException` — eles apenas imprimem os erros e retornam um `ResultadoAuditoria`. O bloco `try/catch` nunca vai capturar nada, e a mensagem `"Invariante OK"` será impressa **mesmo que existam violações**. **Correção necessária:** substituir o try/catch pela verificação do campo `resultado.valida`: |

```java
// ❌ Como está (incorreto):
try { VerificadorAVL.imprimirRelatorio(avl); System.out.println("  [AVL] Invariante OK"); }
catch (IllegalStateException e) { System.out.println("  [AVL] FALHA: " + e.getMessage()); }

// ✅ Como deveria ser:
VerificadorAVL.ResultadoAuditoria resAVL = VerificadorAVL.auditar(avl);
System.out.println(resAVL);
System.out.println(resAVL.valida ? "  [AVL] ✅ Invariante OK" : "  [AVL] ❌ FALHA DE INVARIANTE");
```

---

## 5. Saída e Relatório

| Item | Status | Observação |
|------|--------|------------|
| Tabela comparativa formatada | ✅ OK | Colunas alinhadas, legível |
| Exportação CSV | ✅ OK | Formato correto para gerar gráficos |
| `imprimirPostMortemDados()` indica vencedor por operação | ✅ OK | Útil para o relatório final |
| Recolorações no CSV | ✅ OK | `recoloracoesRBT` registrado corretamente |
| AVL não tem recolorações no CSV (`0`) | ✅ OK | Correto — AVL não usa recoloração |

---

## 6. Resumo Executivo

### Severidade dos problemas encontrados

| Severidade | Quantidade | Itens |
|-----------|-----------|-------|
| 🔴 Crítico | 1 | `try/catch` nunca captura falha de invariante — resultado sempre mostra "OK" mesmo com erros |
| 🟡 Médio | 2 | Volume 1M no cabeçalho mas ausente no array; intervalo de valores diferente do `AuditoriaComparativa` |
| 🟢 Baixo | 1 | JVM warm-up não mencionado como limitação |

### Veredito

> **O merge NÃO deve ser aprovado** até que o item 🔴 Crítico seja corrigido. A verificação de invariantes pós-remoção está silenciosa — nunca reportará falha mesmo que a árvore esteja corrompida. Os demais itens podem ser documentados como limitações no artigo.

### Correção necessária antes do merge

O Integrante 2 deve substituir os blocos `try/catch` pela verificação correta do `ResultadoAuditoria.valida` conforme o exemplo na seção 4.

---

**Assinatura QA:** _____________________ (Integrante 3)  
**Data de revisão:** ___________________
