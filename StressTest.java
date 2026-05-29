import java.util.Random;

public class StressTest {

    private static final long SEED      = 42L;
    private static final int[] VOLUMES = { 10_000, 100_000 };
    private static final int N_BUSCAS   = 1_000;

    private static long[] tempoInsercaoAVL = new long[VOLUMES.length];
    private static long[] tempoInsercaoRBT = new long[VOLUMES.length];
    private static long[] tempoBuscaAVL    = new long[VOLUMES.length];
    private static long[] tempoBuscaRBT    = new long[VOLUMES.length];
    private static long[] tempoRemocaoAVL  = new long[VOLUMES.length];
    private static long[] tempoRemocaoRBT  = new long[VOLUMES.length];
    private static int[]  rotacoesAVL      = new int[VOLUMES.length];
    private static int[]  rotacoesRBT      = new int[VOLUMES.length];
    private static int[]  recoloracoesRBT  = new int[VOLUMES.length];

    public static void main(String[] args) {
        cabecalho();
        for (int i = 0; i < VOLUMES.length; i++) {
            int n = VOLUMES[i];
            System.out.println("\n══════════════════════════════════════════════════════");
            System.out.printf("  VOLUME: %,d entradas%n", n);
            System.out.println("══════════════════════════════════════════════════════");
            executarTeste(i, n);
        }
        imprimirTabelaFinal();
        imprimirCSV();
        imprimirPostMortemDados();
    }

    private static void executarTeste(int idx, int n) {
        int nRemocao = (int)(n * 0.20);
        int[] valores  = gerarValores(n, SEED);
        int[] buscas   = gerarValores(N_BUSCAS, SEED + 1);
        int[] remocoes = gerarValores(nRemocao, SEED + 2);

        System.out.println("\n  [AVL] Inserindo " + n + " regras...");
        ArvoreAVL avl = new ArvoreAVL();
        long inicio = System.nanoTime();
        for (int i = 0; i < n; i++) {
            PacketRule r = new PacketRule(i + 1, "10." + (valores[i] % 256) + ".0.1", "192.168." + (valores[i] % 256) + ".1", valores[i]);
            avl = avl.inserir(r);
        }
        tempoInsercaoAVL[idx] = System.nanoTime() - inicio;
        rotacoesAVL[idx] = avl.getTotalRotacoes();
        System.out.printf("  [AVL] Inserção: %,d ns | Rotações: %,d%n", tempoInsercaoAVL[idx], rotacoesAVL[idx]);

        inicio = System.nanoTime();
        for (int v : buscas) avl.buscarRegra(v);
        tempoBuscaAVL[idx] = (System.nanoTime() - inicio) / N_BUSCAS;
        System.out.printf("  [AVL] Busca média: %,d ns%n", tempoBuscaAVL[idx]);

        System.out.printf("  [AVL] Removendo %,d nós (20%%)...%n", nRemocao);
        inicio = System.nanoTime();
        for (int v : remocoes) avl = avl.remover(new PacketRule(0, "", "", v));
        tempoRemocaoAVL[idx] = System.nanoTime() - inicio;
        System.out.printf("  [AVL] Remoção: %,d ns%n", tempoRemocaoAVL[idx]);
        try { VerificadorAVL.imprimirRelatorio(avl); System.out.println("  [AVL] Invariante OK"); }
        catch (IllegalStateException e) { System.out.println("  [AVL] FALHA: " + e.getMessage()); }

        System.out.println("\n  [RBT] Inserindo " + n + " regras...");
        ArvoreRubroNegra rbt = new ArvoreRubroNegra();
        inicio = System.nanoTime();
        for (int i = 0; i < n; i++) {
            PacketRule r = new PacketRule(i + 1, "10." + (valores[i] % 256) + ".0.1", "192.168." + (valores[i] % 256) + ".1", valores[i]);
            rbt.inserir(r);
        }
        tempoInsercaoRBT[idx] = System.nanoTime() - inicio;
        rotacoesRBT[idx] = rbt.getTotalRotacoes();
        recoloracoesRBT[idx] = rbt.getTotalRecoloracoes();
        System.out.printf("  [RBT] Inserção: %,d ns | Rotações: %,d | Recolorações: %,d%n", tempoInsercaoRBT[idx], rotacoesRBT[idx], recoloracoesRBT[idx]);

        inicio = System.nanoTime();
        for (int v : buscas) rbt.buscar(v);
        tempoBuscaRBT[idx] = (System.nanoTime() - inicio) / N_BUSCAS;
        System.out.printf("  [RBT] Busca média: %,d ns%n", tempoBuscaRBT[idx]);

        System.out.printf("  [RBT] Removendo %,d nós (20%%)...%n", nRemocao);
        inicio = System.nanoTime();
        for (int v : remocoes) rbt.remover(v);
        tempoRemocaoRBT[idx] = System.nanoTime() - inicio;
        System.out.printf("  [RBT] Remoção: %,d ns%n", tempoRemocaoRBT[idx]);
        try { VerificadorRBT.imprimirRelatorio(rbt); System.out.println("  [RBT] Invariante OK"); }
        catch (IllegalStateException e) { System.out.println("  [RBT] FALHA: " + e.getMessage()); }
    }

    private static int[] gerarValores(int n, long seed) {
        Random rng = new Random(seed);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rng.nextInt(10_000_000);
        return arr;
    }

    private static void imprimirTabelaFinal() {
        System.out.println("\n\n╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println(  "║             TABELA COMPARATIVA — AVL vs Red-Black                       ║");
        System.out.println(  "╚══════════════════════════════════════════════════════════════════════════╝");
        System.out.printf("  %-12s %-10s %-18s %-18s %-18s %-12s%n", "Volume", "Estrutura", "Inserção (ns)", "Busca média (ns)", "Remoção (ns)", "Rotações");
        System.out.println("  " + "─".repeat(92));
        for (int i = 0; i < VOLUMES.length; i++) {
            System.out.printf("  %-12s %-10s %,18d %,18d %,18d %,12d%n", String.format("%,d", VOLUMES[i]), "AVL", tempoInsercaoAVL[i], tempoBuscaAVL[i], tempoRemocaoAVL[i], rotacoesAVL[i]);
            System.out.printf("  %-12s %-10s %,18d %,18d %,18d %,12d%n", "", "RBT", tempoInsercaoRBT[i], tempoBuscaRBT[i], tempoRemocaoRBT[i], rotacoesRBT[i]);
            System.out.println("  " + "─".repeat(92));
        }
    }

    private static void imprimirCSV() {
        System.out.println("\n\n── CSV ──────────────────────────────────────────────────────────────────────");
        System.out.println("volume,estrutura,insercao_ns,busca_ns,remocao_ns,rotacoes,recoloracoes");
        for (int i = 0; i < VOLUMES.length; i++) {
            System.out.printf("%d,AVL,%d,%d,%d,%d,0%n", VOLUMES[i], tempoInsercaoAVL[i], tempoBuscaAVL[i], tempoRemocaoAVL[i], rotacoesAVL[i]);
            System.out.printf("%d,RBT,%d,%d,%d,%d,%d%n", VOLUMES[i], tempoInsercaoRBT[i], tempoBuscaRBT[i], tempoRemocaoRBT[i], rotacoesRBT[i], recoloracoesRBT[i]);
        }
    }

    private static void imprimirPostMortemDados() {
        System.out.println("\n\n── DADOS PARA O POST-MORTEM ─────────────────────────────────────────────────");
        System.out.println("  Seed: " + SEED + " | Volumes: 10k | 100k | 1M | Remoção: 20%");
        for (int i = 0; i < VOLUMES.length; i++) {
            System.out.printf("  Volume %,10d → Inserção: %s | Busca: %s | Remoção: %s%n",
                VOLUMES[i],
                tempoInsercaoAVL[i] < tempoInsercaoRBT[i] ? "AVL" : "RBT",
                tempoBuscaAVL[i]    < tempoBuscaRBT[i]    ? "AVL" : "RBT",
                tempoRemocaoAVL[i]  < tempoRemocaoRBT[i]  ? "AVL" : "RBT");
        }
        System.out.println("  Assinatura: _____________________ (Integrante 2)");
    }

    private static void cabecalho() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     SDN-Scale — Stress Test & Coleta de Métricas   ║");
        System.out.println("║     Integrante 2 — DevOps & SRE                    ║");
        System.out.println("║     Seed: 42 | Volumes: 10k | 100k | 1M            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}