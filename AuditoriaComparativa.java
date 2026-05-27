/**
 * AuditoriaComparativa.java
 *
 * QA & Analytics — Ponto de entrada para auditoria e análise de trade-offs.
 * Commit sugerido: docs(QA): adiciona análise comparativa AVL vs RBT
 *
 * Executa:
 *  1. Testes de invariantes em ambas as árvores
 *  2. Contagem de rotações comparativa
 *  3. Relatório de trade-offs técnicos
 *
 * Uso: java AuditoriaComparativa
 */
import java.util.Random;

public class AuditoriaComparativa {

    // ── Semente fixa para reproducibilidade (mesma seed que o Integrante 2) ──
    private static final long SEED       = 42L;
    private static final int  N_INSERCAO = 1000;   // ajuste para testes de carga
    private static final int  N_REMOCAO  = (int)(N_INSERCAO * 0.20); // 20%

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║      SDN-Scale — Auditoria QA & Analytics           ║");
        System.out.println("║      Branch: Verificação                            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        Random rng = new Random(SEED);

        // ── 1. Geração de dados ───────────────────────────────────────────────
        int[] valores = new int[N_INSERCAO];
        for (int i = 0; i < N_INSERCAO; i++) {
            valores[i] = rng.nextInt(100_000);
        }

        // ── 2. Inserção nas duas árvores ─────────────────────────────────────
        ArvoreAVL        avl = new ArvoreAVL();
        ArvoreRubroNegra rbt = new ArvoreRubroNegra();

        int idCounter = 1;
        for (int v : valores) {
            PacketRule regra = new PacketRule(idCounter++,
                    "192.168." + (v % 256) + ".1",
                    "10.0." + (v % 128) + ".1",
                    v);
            avl = avl.inserir(regra);
            rbt.inserir(regra);
        }

        System.out.println("── FASE 1: Verificação pós-inserção (" + N_INSERCAO + " nós) ──\n");
        VerificadorAVL.imprimirRelatorio(avl);
        System.out.println();
        VerificadorRBT.imprimirRelatorio(rbt);

        // ── 3. Remoção de 20% dos nós ─────────────────────────────────────────
        System.out.println("\n── FASE 2: Remoção de " + N_REMOCAO + " nós (20%) ──\n");

        rng = new Random(SEED); // mesma seed para escolher os mesmos valores
        for (int i = 0; i < N_REMOCAO; i++) {
            int v = rng.nextInt(100_000);
            PacketRule dummy = new PacketRule(0, "", "", v);
            avl = avl.remover(dummy);
            rbt.remover(v);
        }

        System.out.println("── FASE 3: Verificação pós-remoção ──\n");
        VerificadorAVL.imprimirRelatorio(avl);
        System.out.println();
        VerificadorRBT.imprimirRelatorio(rbt);

        // ── 4. Análise de trade-offs ──────────────────────────────────────────
        imprimirTradeoffs(avl, rbt);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private static void imprimirTradeoffs(ArvoreAVL avl, ArvoreRubroNegra rbt) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              ANÁLISE DE TRADE-OFFS                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        int rotAVL = avl.getTotalRotacoes();
        int rotRBT = rbt.getTotalRotacoes();
        int recoRBT = rbt.getTotalRecoloracoes();
        int altAVL = avl.calcularAltura();

        System.out.println();
        System.out.printf("  %-30s %10s %10s%n", "Métrica", "AVL", "Red-Black");
        System.out.println("  " + "─".repeat(52));
        System.out.printf("  %-30s %10d %10d%n", "Total de rotações",       rotAVL, rotRBT);
        System.out.printf("  %-30s %10s %10d%n", "Total de recolorações",   "N/A",  recoRBT);
        System.out.printf("  %-30s %10d %10s%n", "Altura da árvore",        altAVL, "≤2·log₂(n+1)");
        System.out.println();

        System.out.println("  CONCLUSÕES:");
        System.out.println();

        if (rotAVL > rotRBT) {
            System.out.printf("  ▶ AVL realizou MAIS rotações (%d vs %d).%n", rotAVL, rotRBT);
            System.out.println("    → Confirma a teoria: AVL mantém balanceamento mais rígido,");
            System.out.println("      pagando custo extra em rotações para manter |FB|≤1.");
        } else {
            System.out.printf("  ▶ RBT realizou MAIS ou IGUAL rotações (%d vs %d).%n", rotRBT, rotAVL);
            System.out.println("    → Verificar implementação; RBT tende a fazer menos rotações.");
        }

        System.out.println();
        System.out.println("  ► Cenários de uso recomendado:");
        System.out.println("    • AVL  → read-intensive (buscas frequentes, poucas inserções)");
        System.out.println("             Altura menor → latência de busca ligeiramente inferior.");
        System.out.println("    • RBT  → write-intensive (muitas inserções/remoções)");
        System.out.println("             Menos rotações → menor custo de manutenção da estrutura.");
        System.out.println();
        System.out.println("  ► Aplicação ao cenário SDN:");
        System.out.println("    Tabelas de fluxo SDN são altamente voláteis (regras expiram");
        System.out.println("    frequentemente). Isso favorece a Red-Black Tree para o");
        System.out.println("    gerenciamento das PacketRules em produção.");
        System.out.println();
        System.out.println("  Assinatura QA: _____________________ (Integrante 3)");
        System.out.println();
    }
}
