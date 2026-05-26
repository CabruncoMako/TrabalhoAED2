public class ArvoreRubroNegra {

    private static final boolean VERMELHO = true;
    private static final boolean PRETO    = false;

    private class No {
        PacketRule regra;
        No esq, dir, pai;
        boolean cor;

        No() {
            this.cor  = PRETO;
            this.regra = null;
            this.esq  = null;
            this.dir  = null;
            this.pai  = null;
        }

        No(PacketRule regra) {
            this.regra = regra;
            this.cor   = VERMELHO;
            this.esq   = nil;
            this.dir   = nil;
            this.pai   = nil;
        }

        boolean ehVermelho() { return this.cor == VERMELHO; }
        boolean ehPreto()    { return this.cor == PRETO;    }
    }

    private final No nil;
    private No raiz;
    private int totalRotacoes;
    private int totalRecoloracoes;

    // Construtores
    public ArvoreRubroNegra() {
        nil  = new No();
        raiz = nil;
        totalRotacoes    = 0;
        totalRecoloracoes = 0;
    }

    // Buscas
    private No buscarNo(int valor) {
        No atual = raiz;
        while (atual != nil) {
            int chave = atual.regra.getValor();
            if (valor == chave) return atual;
            atual = (valor < chave) ? atual.esq : atual.dir;
        }
        return nil;
    }

    public PacketRule buscar(int prioridade) {
        No no = buscarNo(prioridade);
        return (no != nil) ? no.regra : null;
    }

    public boolean contem(int prioridade) {
        return buscarNo(prioridade) != nil;
    }

    // Inserção
    public void inserir(PacketRule regra) {
        No novo = new No(regra);

        No y = nil;
        No x = raiz;
        while (x != nil) {
            y = x;
            if (novo.regra.getValor() < x.regra.getValor()) {
                x = x.esq;
            } else if (novo.regra.getValor() > x.regra.getValor()) {
                x = x.dir;
            } else {
                x.regra = regra;
                return;
            }
        }
        novo.pai = y;

        if (y == nil) {
            raiz = novo;
        } else if (novo.regra.getValor() < y.regra.getValor()) {
            y.esq = novo;
        } else {
            y.dir = novo;
        }

        corrigirInsercao(novo);
    }

    private void corrigirInsercao(No z) {
        while (z.pai.ehVermelho()) {
            if (z.pai == z.pai.pai.esq) {
                No tio = z.pai.pai.dir;

                if (tio.ehVermelho()) {
                    // Caso 1: tio vermelho, recoloração
                    z.pai.cor       = PRETO;
                    tio.cor         = PRETO;
                    z.pai.pai.cor   = VERMELHO;
                    totalRecoloracoes++;
                    z = z.pai.pai;

                } else {
                    if (z == z.pai.dir) {
                        // Caso 2: tio preto, z filho direito, rotação esq
                        z = z.pai;
                        rotacaoEsquerda(z);
                    }
                    // Caso 3: tio preto, z filho esquerdo, rotação dir
                    z.pai.cor     = PRETO;
                    z.pai.pai.cor = VERMELHO;
                    totalRecoloracoes++;
                    rotacaoDireita(z.pai.pai);
                }

            } else {
                No tio = z.pai.pai.esq;

                if (tio.ehVermelho()) {
                    // Caso 1
                    z.pai.cor       = PRETO;
                    tio.cor         = PRETO;
                    z.pai.pai.cor   = VERMELHO;
                    totalRecoloracoes++;
                    z = z.pai.pai;

                } else {
                    if (z == z.pai.esq) {
                        // Caso 2
                        z = z.pai;
                        rotacaoDireita(z);
                    }
                    // Caso 3
                    z.pai.cor     = PRETO;
                    z.pai.pai.cor = VERMELHO;
                    totalRecoloracoes++;
                    rotacaoEsquerda(z.pai.pai);
                }
            }
        }
        raiz.cor = PRETO;
    }

    // Remoções
    public void remover(int prioridade) {
        No z = buscarNo(prioridade);
        if (z == nil) return;

        No y = z;
        boolean corOriginalY = y.cor;
        No x;

        if (z.esq == nil) {
            // Caso 1: sem filho esq
            x = z.dir;
            transplantar(z, z.dir);

        } else if (z.dir == nil) {
            // Caso 2: sem filho dir
            x = z.esq;
            transplantar(z, z.esq);

        } else {
            // Caso 3: dois filhos
            y = minimo(z.dir);
            corOriginalY = y.cor;
            x = y.dir;

            if (y.pai == z) {
                x.pai = y;
            } else {
                transplantar(y, y.dir);
                y.dir     = z.dir;
                y.dir.pai = y;
            }
            transplantar(z, y);
            y.esq     = z.esq;
            y.esq.pai = y;
            y.cor     = z.cor;
        }

        if (corOriginalY == PRETO) {
            corrigirRemocao(x);
        }
    }

    private void corrigirRemocao(No x) {
        while (x != raiz && x.ehPreto()) {
            if (x == x.pai.esq) {
                No irmao = x.pai.dir;

                if (irmao.ehVermelho()) {
                    irmao.cor   = PRETO;
                    x.pai.cor   = VERMELHO;
                    totalRecoloracoes++;
                    rotacaoEsquerda(x.pai);
                    irmao = x.pai.dir;
                }
                if (irmao.esq.ehPreto() && irmao.dir.ehPreto()) {
                    irmao.cor = VERMELHO;
                    totalRecoloracoes++;
                    x = x.pai;

                } else {
                    if (irmao.dir.ehPreto()) {
                        irmao.esq.cor = PRETO;
                        irmao.cor     = VERMELHO;
                        totalRecoloracoes++;
                        rotacaoDireita(irmao);
                        irmao = x.pai.dir;
                    }
                    irmao.cor     = x.pai.cor;
                    x.pai.cor     = PRETO;
                    irmao.dir.cor = PRETO;
                    totalRecoloracoes++;
                    rotacaoEsquerda(x.pai);
                    x = raiz;
                }

            } else {
                No irmao = x.pai.esq;

                // Caso 1
                if (irmao.ehVermelho()) {
                    irmao.cor   = PRETO;
                    x.pai.cor   = VERMELHO;
                    totalRecoloracoes++;
                    rotacaoDireita(x.pai);
                    irmao = x.pai.esq;
                }

                // Caso 2
                if (irmao.dir.ehPreto() && irmao.esq.ehPreto()) {
                    irmao.cor = VERMELHO;
                    totalRecoloracoes++;
                    x = x.pai;

                } else {
                    // Caso 3
                    if (irmao.esq.ehPreto()) {
                        irmao.dir.cor = PRETO;
                        irmao.cor     = VERMELHO;
                        totalRecoloracoes++;
                        rotacaoEsquerda(irmao);
                        irmao = x.pai.esq;
                    }
                    // Caso 4
                    irmao.cor     = x.pai.cor;
                    x.pai.cor     = PRETO;
                    irmao.esq.cor = PRETO;
                    totalRecoloracoes++;
                    rotacaoDireita(x.pai);
                    x = raiz;
                }
            }
        }
        x.cor = PRETO;
    }

    // Rotações
    private void rotacaoEsquerda(No x) {
        No y = x.dir;
        x.dir = y.esq;

        if (y.esq != nil) y.esq.pai = x;

        y.pai = x.pai;
        if (x.pai == nil) {
            raiz = y;
        } else if (x == x.pai.esq) {
            x.pai.esq = y;
        } else {
            x.pai.dir = y;
        }

        y.esq  = x;
        x.pai  = y;
        totalRotacoes++;
    }

    private void rotacaoDireita(No y) {
        No x = y.esq;
        y.esq = x.dir;

        if (x.dir != nil) x.dir.pai = y;

        x.pai = y.pai;
        if (y.pai == nil) {
            raiz = x;
        } else if (y == y.pai.dir) {
            y.pai.dir = x;
        } else {
            y.pai.esq = x;
        }

        x.dir  = y;
        y.pai  = x;
        totalRotacoes++;
    }

    // Auxiliares
    private void transplantar(No u, No v) {
        if (u.pai == nil) {
            raiz = v;
        } else if (u == u.pai.esq) {
            u.pai.esq = v;
        } else {
            u.pai.dir = v;
        }
        v.pai = u.pai;
    }

    private No minimo(No no) {
        while (no.esq != nil) no = no.esq;
        return no;
    }

    private No maximo(No no) {
        while (no.dir != nil) no = no.dir;
        return no;
    }

    public boolean isEmpty() { return raiz == nil; }

    public int getTotalRotacoes()     { return totalRotacoes; }
    public int getTotalRecoloracoes() { return totalRecoloracoes; }

    // Ordem do percurso
    public void imprimirEmOrdem() {
        imprimirEmOrdemRec(raiz);
        System.out.println();
    }

    private void imprimirEmOrdemRec(No no) {
        if (no != nil) {
            imprimirEmOrdemRec(no.esq);
            System.out.print(no.regra.getValor()
                    + "(" + (no.cor == VERMELHO ? "V" : "P") + ") ");
            imprimirEmOrdemRec(no.dir);
        }
    }

    public void imprimirPreOrdem() {
        imprimirPreOrdemRec(raiz);
        System.out.println();
    }

    private void imprimirPreOrdemRec(No no) {
        if (no != nil) {
            System.out.print(no.regra.getValor()
                    + "(" + (no.cor == VERMELHO ? "V" : "P") + ") ");
            imprimirPreOrdemRec(no.esq);
            imprimirPreOrdemRec(no.dir);
        }
    }

    public void imprimirPosOrdem() {
        imprimirPosOrdemRec(raiz);
        System.out.println();
    }

    private void imprimirPosOrdemRec(No no) {
        if (no != nil) {
            imprimirPosOrdemRec(no.esq);
            imprimirPosOrdemRec(no.dir);
            System.out.print(no.regra.getValor()
                    + "(" + (no.cor == VERMELHO ? "V" : "P") + ") ");
        }
    }

    // Print
    public String imprimirArvore() {
        if (raiz == nil) return "(árvore vazia)";
        return imprimirArvoreRec(raiz, 0);
    }

    private String imprimirArvoreRec(No no, int nivel) {
        if (no == nil) return "NIL(P)";
        String cor = no.cor == VERMELHO ? "V" : "P";
        String str = "[" + no.regra.getValor() + "](" + cor + ")\n";
        String indent = "\t".repeat(nivel);
        str += indent + "+-ESQ: " + imprimirArvoreRec(no.esq, nivel + 1) + "\n";
        str += indent + "+-DIR: " + imprimirArvoreRec(no.dir, nivel + 1) + "\n";
        return str;
    }

    @Override
    public String toString() {
        return imprimirArvore();
    }
}
