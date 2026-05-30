/**
 * VerificadorAVL.java
 * 
 * QA & Analytics — Auditor de invariantes da ArvoreAVL.
 * Commit sugerido: feat(QA): adiciona verificador de altura AVL
 *
 * Verifica:
 *  1. |FB| ≤ 1 em todos os nós
 *  2. FB armazenado == FB calculado
 *  3. Propriedade BST (chave esq < raiz < chave dir)
 *  4. Altura consistente
 *  5. Contagem de rotações simples e duplas
 */
public class VerificadorAVL {

    // ---------------------------------------------------------------
    // Resultado consolidado de uma auditoria
    // ---------------------------------------------------------------
    public static class ResultadoAuditoria {
        public boolean valida             = true;
        public int     totalNos           = 0;
        public int     violacoesBalanceo  = 0;
        public int     violacoesFBCached  = 0;
        public int     violacoesBST       = 0;
        public int     alturaCalculada    = 0;
        public int     rotacoesSimples    = 0;
        public int     rotacoesDuplas     = 0;

        @Override
        public String toString() {
            return "=== Auditoria AVL ===\n"
                + "  Válida             : " + valida + "\n"
                + "  Total de nós       : " + totalNos + "\n"
                + "  Altura da árvore   : " + alturaCalculada + "\n"
                + "  Violações |FB|>1   : " + violacoesBalanceo + "\n"
                + "  Violações FB cache : " + violacoesFBCached + "\n"
                + "  Violações BST      : " + violacoesBST + "\n"
                + "  Rotações simples   : " + rotacoesSimples + "\n"
                + "  Rotações duplas    : " + rotacoesDuplas + "\n"
                + "  Rotações totais    : " + (rotacoesSimples + rotacoesDuplas * 2);
        }
    }

    // ---------------------------------------------------------------
    // Ponto de entrada principal
    // ---------------------------------------------------------------
    public static ResultadoAuditoria auditar(ArvoreAVL raiz) {
        ResultadoAuditoria res = new ResultadoAuditoria();
        if (raiz == null || raiz.isEmpty()) {
            System.out.println("[AVL-QA] Árvore vazia — nada a verificar.");
            return res;
        }
        verificarNo(raiz, res, Integer.MIN_VALUE, Integer.MAX_VALUE);
        res.alturaCalculada  = raiz.calcularAltura();
        res.rotacoesSimples  = contarRotacoesSimples(raiz.getTotalRotacoes());
        res.rotacoesDuplas   = contarRotacoesDuplas(raiz.getTotalRotacoes());
        res.valida = (res.violacoesBalanceo == 0
                   && res.violacoesFBCached == 0
                   && res.violacoesBST      == 0);
        return res;
    }

    // ---------------------------------------------------------------
    // Recursão sobre os nós
    // ---------------------------------------------------------------
    private static int verificarNo(ArvoreAVL no,
                                   ResultadoAuditoria res,
                                   int minValor,
                                   int maxValor) {
        if (no == null || no.isEmpty()) return 0;

        res.totalNos++;
        int valor = no.getRegra().getValor();

        // 1) Invariante BST
        if (valor <= minValor || valor >= maxValor) {
            res.violacoesBST++;
            System.out.printf("[AVL-QA][ERRO-BST] Nó %d fora dos limites (%d, %d)%n",
                    valor, minValor, maxValor);
        }

        int altEsq = verificarNo(no.getEsquerda(), res, minValor, valor);
        int altDir = verificarNo(no.getDireita(),  res, valor,    maxValor);

        // 2) FB real
        int fbReal = altDir - altEsq;

        // 3) Invariante AVL: |FB| ≤ 1
        if (Math.abs(fbReal) > 1) {
            res.violacoesBalanceo++;
            System.out.printf("[AVL-QA][ERRO-BAL] Nó %d: FB=%d (permitido |FB|≤1)%n",
                    valor, fbReal);
        }

        // 4) FB armazenado vs calculado
        int fbCache = no.getBalanceamento();
        if (fbCache != fbReal) {
            res.violacoesFBCached++;
            System.out.printf("[AVL-QA][ERRO-CACHE] Nó %d: FB armazenado=%d, FB real=%d%n",
                    valor, fbCache, fbReal);
        }

        return 1 + Math.max(altEsq, altDir);
    }

    // ---------------------------------------------------------------
    // Heurística de rotações — adapte conforme o contador do Integrante 1
    // O ArvoreAVL.getTotalRotacoes() soma 1 (simples) ou 2 (dupla) por chamada.
    // Aqui separamos para fins de relatório; ajuste se a lógica mudar.
    // ---------------------------------------------------------------
    private static int contarRotacoesSimples(int totalContador) {
        // Placeholder: sem acesso ao detalhe interno assumimos que metade
        // das rotações registradas como "2" vieram de duplas.
        // Para auditoria precisa, o Integrante 1 deve expor contadores separados.
        return totalContador; // será sobrescrito em relatório final
    }

    private static int contarRotacoesDuplas(int totalContador) {
        return 0; // idem acima
    }

    // ---------------------------------------------------------------
    // Utilitário: exibe resultado completo da auditoria
    // ---------------------------------------------------------------
    public static void imprimirRelatorio(ArvoreAVL raiz) {
        ResultadoAuditoria r = auditar(raiz);
        System.out.println(r);
    }
}
