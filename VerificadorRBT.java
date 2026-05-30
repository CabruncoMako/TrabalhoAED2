/**
 * VerificadorRBT.java
 *
 * QA & Analytics — Auditor de invariantes da ArvoreRubroNegra.
 * Commit sugerido: feat(QA): adiciona verificador de propriedades RBT
 *
 * Verifica as 5 propriedades fundamentais da Red-Black Tree:
 *  P1. Todo nó é VERMELHO ou PRETO.                   (garantido por tipo boolean)
 *  P2. A raiz é PRETA.
 *  P3. Todo nó NIL (folha sentinela) é PRETO.          (garantido pelo nil sentinel)
 *  P4. Filhos de nó VERMELHO são sempre PRETOS.
 *  P5. Todo caminho raiz→NIL tem o mesmo número de nós PRETOS (black-height).
 *  P6. (BST) Chave esq < raiz < chave dir.
 *
 * ATENÇÃO: ArvoreRubroNegra usa classe No interna (privada).
 * Esta classe acessa a árvore via percurso reflexivo ou via API pública.
 * Como No é privada, usamos a saída de imprimirArvore() para parsear a estrutura.
 *
 * Para auditoria profunda (recomendado): o Integrante 1 deve expor um método
 *   public String exportarJSON() ou public No getRaiz() com No pública.
 * Enquanto isso, esta classe verifica via API pública disponível.
 */
public class VerificadorRBT {

    // ---------------------------------------------------------------
    // Resultado consolidado
    // ---------------------------------------------------------------
    public static class ResultadoAuditoria {
        public boolean valida               = true;
        public int     totalRotacoes        = 0;
        public int     totalRecoloracoes    = 0;
        public int     violacoesP2          = 0; // raiz preta
        public int     violacoesP4          = 0; // filho de vermelho é vermelho
        public int     violacoesP5          = 0; // black-height inconsistente
        public int     violacoesBST         = 0;
        public String  blackHeightInfo      = "N/A";

        @Override
        public String toString() {
            return "=== Auditoria Red-Black Tree ===\n"
                + "  Válida                  : " + valida + "\n"
                + "  Total de rotações       : " + totalRotacoes + "\n"
                + "  Total de recolorações   : " + totalRecoloracoes + "\n"
                + "  Black-height            : " + blackHeightInfo + "\n"
                + "  Violações P2 (raiz preta): " + violacoesP2 + "\n"
                + "  Violações P4 (vermelho-vermelho): " + violacoesP4 + "\n"
                + "  Violações P5 (black-height): " + violacoesP5 + "\n"
                + "  Violações BST           : " + violacoesBST;
        }
    }

    // ---------------------------------------------------------------
    // Nó intermediário para auditoria (parseado da saída textual)
    // ---------------------------------------------------------------
    private static class NoAuditoria {
        int    valor;
        char   cor;    // 'V' ou 'P'
        NoAuditoria esq, dir;

        NoAuditoria(int valor, char cor) {
            this.valor = valor;
            this.cor   = cor;
        }

        boolean ehVermelho() { return cor == 'V'; }
        boolean ehPreto()    { return cor == 'P'; }
    }

    // ---------------------------------------------------------------
    // Parser da saída de imprimirArvore()
    // Formato: [valor](cor)\n\t+-ESQ: ...\n\t+-DIR: ...
    // ---------------------------------------------------------------
    private static NoAuditoria parsear(String saida) {
        if (saida == null || saida.startsWith("(árvore vazia)")) return null;
        String[] linhas = saida.split("\n");
        return parsearLinhas(linhas, new int[]{0});
    }

    private static NoAuditoria parsearLinhas(String[] linhas, int[] idx) {
        if (idx[0] >= linhas.length) return null;

        String linha = linhas[idx[0]].trim();

        // Linha NIL: "NIL(P)"
        if (linha.startsWith("NIL")) return null;

        // Linha raiz/nó: "[valor](cor)"
        if (!linha.startsWith("[")) return null;

        int fechaBracket = linha.indexOf(']');
        if (fechaBracket < 0) return null;

        int valor = Integer.parseInt(linha.substring(1, fechaBracket));
        char cor  = linha.charAt(fechaBracket + 2); // "(V)" ou "(P)"
        NoAuditoria no = new NoAuditoria(valor, cor);

        idx[0]++;

        // Próxima linha: "+-ESQ: ..."
        if (idx[0] < linhas.length) {
            String esqLinha = linhas[idx[0]].replaceFirst(".*\\+-ESQ:\\s*", "").trim();
            idx[0]++;
            if (!esqLinha.startsWith("NIL")) {
                // O filho começa nesta string
                String[] sub = esqLinha.split("\n");
                int[] subIdx = {0};
                no.esq = parsearLinhas(sub, subIdx);
            }
        }

        // Próxima linha: "+-DIR: ..."
        if (idx[0] < linhas.length) {
            String dirLinha = linhas[idx[0]].replaceFirst(".*\\+-DIR:\\s*", "").trim();
            idx[0]++;
            if (!dirLinha.startsWith("NIL")) {
                String[] sub = dirLinha.split("\n");
                int[] subIdx = {0};
                no.dir = parsearLinhas(sub, subIdx);
            }
        }

        return no;
    }

    // ---------------------------------------------------------------
    // Ponto de entrada principal
    // ---------------------------------------------------------------
    public static ResultadoAuditoria auditar(ArvoreRubroNegra arvore) {
        ResultadoAuditoria res = new ResultadoAuditoria();

        if (arvore == null || arvore.isEmpty()) {
            System.out.println("[RBT-QA] Árvore vazia — nada a verificar.");
            return res;
        }

        // Coleta contadores expostos pela árvore
        res.totalRotacoes     = arvore.getTotalRotacoes();
        res.totalRecoloracoes = arvore.getTotalRecoloracoes();

        // Parseia estrutura via saída textual
        String saida = arvore.imprimirArvore();
        NoAuditoria raiz = parsear(saida);

        if (raiz == null) {
            System.out.println("[RBT-QA] Não foi possível parsear a árvore.");
            return res;
        }

        // P2: Raiz deve ser preta
        if (raiz.ehVermelho()) {
            res.violacoesP2++;
            System.out.println("[RBT-QA][ERRO-P2] Raiz é VERMELHA — deve ser PRETA.");
        }

        // P4 e P5 via recursão
        int[] blackHeightRef = {-1};
        verificarNo(raiz, res, 0, blackHeightRef, Integer.MIN_VALUE, Integer.MAX_VALUE);
        res.blackHeightInfo = (blackHeightRef[0] >= 0)
                ? String.valueOf(blackHeightRef[0])
                : "inconsistente";

        res.valida = (res.violacoesP2   == 0
                   && res.violacoesP4   == 0
                   && res.violacoesP5   == 0
                   && res.violacoesBST  == 0);
        return res;
    }

    // ---------------------------------------------------------------
    // Recursão: verifica P4, P5, BST e acumula black-height
    // Retorna o black-height do caminho (contando nós pretos) ou -1 se inválido
    // ---------------------------------------------------------------
    private static int verificarNo(NoAuditoria no,
                                   ResultadoAuditoria res,
                                   int blackHeight,
                                   int[] blackHeightRef,
                                   int minValor,
                                   int maxValor) {
        if (no == null) {
            // Folha NIL — preta por definição (P3)
            if (blackHeightRef[0] == -1) {
                blackHeightRef[0] = blackHeight;
            } else if (blackHeightRef[0] != blackHeight) {
                res.violacoesP5++;
                System.out.printf("[RBT-QA][ERRO-P5] Black-height inconsistente: "
                        + "esperado %d, encontrado %d%n",
                        blackHeightRef[0], blackHeight);
            }
            return blackHeight;
        }

        int valor = no.valor;

        // BST
        if (valor <= minValor || valor >= maxValor) {
            res.violacoesBST++;
            System.out.printf("[RBT-QA][ERRO-BST] Nó %d fora dos limites (%d, %d)%n",
                    valor, minValor, maxValor);
        }

        // P4: nó vermelho não pode ter filho vermelho
        if (no.ehVermelho()) {
            if (no.esq != null && no.esq.ehVermelho()) {
                res.violacoesP4++;
                System.out.printf("[RBT-QA][ERRO-P4] Nó %d(V) → filho esq %d(V)%n",
                        valor, no.esq.valor);
            }
            if (no.dir != null && no.dir.ehVermelho()) {
                res.violacoesP4++;
                System.out.printf("[RBT-QA][ERRO-P4] Nó %d(V) → filho dir %d(V)%n",
                        valor, no.dir.valor);
            }
        }

        int bh = blackHeight + (no.ehPreto() ? 1 : 0);
        verificarNo(no.esq, res, bh, blackHeightRef, minValor, valor);
        verificarNo(no.dir, res, bh, blackHeightRef, valor, maxValor);
        return bh;
    }

    // ---------------------------------------------------------------
    // Utilitário: imprime relatório completo
    // ---------------------------------------------------------------
    public static void imprimirRelatorio(ArvoreRubroNegra arvore) {
        ResultadoAuditoria r = auditar(arvore);
        System.out.println(r);
    }
}
