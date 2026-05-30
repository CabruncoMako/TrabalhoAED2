# Code Review — Branch `Verificação`

**Autor da revisão:** Integrante 3 — QA & Analytics  
**Commit sugerido:** `review(QA): auditoria do código de inserção AVL e RBT`  
**Data:** 2025  
**Arquivos revisados:** `ArvoreAVL.java`, `ArvoreRubroNegra.java`, `PacketRule.java`

---

## 1. PacketRule.java ✅

| Item | Status | Observação |
|------|--------|------------|
| Encapsulamento (getters/setters) | ✅ OK | Todos os campos privados com acesso via métodos |
| `getValor()` retorna `prioridade` | ✅ OK | Chave de ordenação correta para as árvores |
| `toString()` legível | ✅ OK | Formato claro para debug |
| Imutabilidade da chave | ⚠️ ATENÇÃO | `setId`, `setPrioridade` etc. são públicos. Em árvores BST, **alterar a chave de um nó já inserido quebra a invariante BST** sem que a árvore saiba. Recomendação: tornar `id` e `prioridade` `final`, ou remover os setters de `id` e `prioridade`. |

---

## 2. ArvoreAVL.java

### 2.1 Inserção

| Item | Status | Observação |
|------|--------|------------|
| Recursão correta (esq/dir) | ✅ OK | Navegação BST correta |
| Retorno da subárvore rebalanceada | ✅ OK | `inserir()` retorna `this.verificarBalanceamento()` |
| Duplicatas ignoradas | ✅ OK | `else if` sem o `==` caso — duplicatas simplesmente não entram |
| `calcularBalanceamento()` chamado antes de `verificarBalanceamento()` | ✅ OK | Ordem correta |

### 2.2 Remoção

| Item | Status | Observação |
|------|--------|------------|
| Caso folha (sem filhos) | ✅ OK | Retorna `null` |
| Caso 1 filho | ✅ OK | Retorna o filho existente |
| Caso 2 filhos (predecessor) | ⚠️ ATENÇÃO | O predecessor é encontrado corretamente (`aux` vai para o maior nó da subárvore esquerda). Porém, a remoção subsequente usa `aux.setRegra(elem)` para "devolver" o elemento ao lugar certo antes de chamar `this.esq.remover(elem)`. Isso funciona, mas é frágil: se `elem` já tiver sido modificado externamente (ver note sobre `setters` em PacketRule), o valor pode ser diferente. Considere usar o valor numérico diretamente. |
| Rebalanceamento pós-remoção | ✅ OK | `calcularBalanceamento()` + `verificarBalanceamento()` chamados ao retornar |

### 2.3 Rotações

| Item | Status | Observação |
|------|--------|------------|
| `rotacaoSimplesDireita()` (rotação LL) | ⚠️ REVISAR NOMENCLATURA | O método é chamado quando `bal >= 2` (subárvore **direita** pesada). Uma rotação neste sentido move o filho direito para cima, o que na literatura é chamado de **rotação à esquerda** (left rotation). A nomenclatura `rotacaoSimplesDireita` pode causar confusão. Funcionalmente o código parece correto, mas a nomenclatura está invertida em relação à convenção CLRS/Sedgewick. |
| `rotacaoDuplaDireita()` | ⚠️ REVISAR | Mesmo problema de nomenclatura. Confirmar se o caso `dir.bal < 0` (filho direito com subárvore esquerda pesada) está sendo tratado. |
| `totalRotacoes` incrementado | ✅ OK | Incrementado em cada rotação simples (+1) e dupla (+2) |

### 2.4 `calcularBalanceamento()`

| Item | Status | Observação |
|------|--------|------------|
| Recursão nos filhos | ⚠️ ATENÇÃO | `calcularBalanceamento()` recalcula os filhos recursivamente **após** calcular o próprio nó. Isso garante consistência mas tem custo O(n) por chamada, tornando inserção/remoção O(n log n) no pior caso em vez de O(log n). Para um trabalho acadêmico é aceitável, mas em produção o FB deveria ser atualizado incrementalmente durante a rotação. |

### 2.5 Buscas

| Item | Status | Observação |
|------|--------|------------|
| `busca(int valor)` | ✅ OK | Navegação BST correta |
| `buscarRegra(int valor)` | ✅ OK | Retorna o objeto correto |

---

## 3. ArvoreRubroNegra.java

### 3.1 Nó sentinela `nil`

| Item | Status | Observação |
|------|--------|------------|
| `nil` inicializado como PRETO | ✅ OK | Atende P3 (folhas NIL são pretas) |
| `nil` compartilhado globalmente | ✅ OK | Padrão correto para RBT |
| `nil.pai` pode ser referenciado | ⚠️ ATENÇÃO | Em `corrigirInsercao`, há acesso a `z.pai.pai` sem verificar se `z.pai == nil`. Se `z` for a raiz após subir (`z = z.pai.pai`), `z.pai` pode ser `nil`, e `nil.pai` não está setado. A linha `raiz.cor = PRETO` ao final protege parcialmente, mas o loop pode acessar `nil.pai.esq` causando NullPointerException em casos extremos. **Recomendação:** adicionar guarda `z.pai != nil && z.pai.pai != nil` no while. |

### 3.2 Inserção e `corrigirInsercao()`

| Item | Status | Observação |
|------|--------|------------|
| 3 casos de correção (tio V, tio P + filho dir, tio P + filho esq) | ✅ OK | Implementação correta dos 3 casos CLRS |
| Atualização do pai antes de inserir | ✅ OK | `novo.pai = y` feito corretamente |
| Raiz forçada para PRETO ao final | ✅ OK | `raiz.cor = PRETO` — atende P2 |
| Duplicatas substituem o valor | ✅ OK | `x.regra = regra` sem reinserir o nó |

### 3.3 Remoção e `corrigirRemocao()`

| Item | Status | Observação |
|------|--------|------------|
| 4 casos de correção (irmão V, irmão P filhos P, irmão P filho dir P, irmão P filho dir V) | ✅ OK | Cobre todos os casos CLRS cap. 13 |
| `transplantar()` atualiza `v.pai` | ✅ OK | Ponteiro pai corretamente mantido |
| `x.cor = PRETO` ao final | ✅ OK | Encerra o "nó duplamente preto" |

### 3.4 Rotações

| Item | Status | Observação |
|------|--------|------------|
| `rotacaoEsquerda()` | ✅ OK | Ponteiros pai/filho corretos |
| `rotacaoDireita()` | ✅ OK | Simétrico, correto |
| `totalRotacoes` incrementado | ✅ OK | +1 por rotação simples |

### 3.5 Observações gerais RBT

| Item | Status | Observação |
|------|--------|------------|
| `imprimirArvore()` expõe estrutura | ✅ OK | Facilita auditoria externa |
| `getTotalRecoloracoes()` exposto | ✅ OK | Permite coleta de métricas |
| Classe `No` privada | ⚠️ LIMITAÇÃO | Impede verificação direta via `instanceof` ou reflexão simples. Para testes unitários robustos, considere tornar `No` package-private ou adicionar método `exportar()`. |

---

## 4. Resumo Executivo

### Severidade dos problemas encontrados

| Severidade | Quantidade | Itens |
|-----------|-----------|-------|
| 🔴 Crítico | 0 | — |
| 🟡 Médio | 3 | Mutabilidade da chave em PacketRule; guarda em `corrigirInsercao`; nomenclatura de rotações AVL |
| 🟢 Baixo | 2 | `calcularBalanceamento()` O(n); No privada dificulta testes |

### Veredito

> **O merge pode ser aprovado** com a condição de que os itens de severidade 🟡 Médio sejam documentados e discutidos pela equipe. A lógica central de inserção, remoção e balanceamento está **correta** em ambas as implementações.

---

**Assinatura QA:** _____________________ (Integrante 3)  
**Data de revisão:** ___________________
