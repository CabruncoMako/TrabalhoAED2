public class ArvoreAVL {
    private PacketRule regra;
    private ArvoreAVL dir;
    private ArvoreAVL esq;
    private int bal;
    private int totalRotacoes;

    // Construtores
    public ArvoreAVL() {
        this.regra = null;
        this.esq   = null;
        this.dir   = null;
        this.bal   = 0;
        this.totalRotacoes = 0;
    }

    public ArvoreAVL(PacketRule regra) {
        this.regra = regra;
        this.esq   = null;
        this.dir   = null;
        this.bal   = 0;
        this.totalRotacoes = 0;
    }

    // Getters e setters
    public PacketRule getRegra()                    { return this.regra; }
    public void setRegra(PacketRule regra)           { this.regra = regra; }

    public ArvoreAVL getDireita()             { return this.dir; }
    public void setDir(ArvoreAVL dir)         { this.dir = dir; }

    public ArvoreAVL getEsquerda()            { return this.esq; }
    public void setEsq(ArvoreAVL esq)         { this.esq = esq; }

    public int getBalanceamento() { return this.bal; }
    public int getTotalRotacoes() {
        int total = this.totalRotacoes;
        if (this.esq != null) total += this.esq.getTotalRotacoes();
        if (this.dir != null) total += this.dir.getTotalRotacoes();
        return total;
    }

    public boolean isEmpty() { return this.regra == null; }

    // Inserções
    public ArvoreAVL inserir(PacketRule nova) {
        if (isEmpty()) {
            this.regra = nova;
        } else {
            ArvoreAVL novaArvore = new ArvoreAVL(nova);
            if (nova.getValor() < this.regra.getValor()) {
                if (this.esq == null) {
                    this.esq = novaArvore;
                } else {
                    this.esq = this.esq.inserir(nova);
                }
            } else if (nova.getValor() > this.regra.getValor()) {
                if (this.dir == null) {
                    this.dir = novaArvore;
                } else {
                    this.dir = this.dir.inserir(nova);
                }
            }
        }
        this.calcularBalanceamento();
        return this.verificarBalanceamento();
    }

    // Remoções
    public ArvoreAVL remover(PacketRule elem) {
        return remover(elem.getValor());
    }

    public ArvoreAVL remover(int valor) {
        if (this.regra.getValor() == valor) {
            if (this.dir == null && this.esq == null) {   // Caso 1: folha
                return null;
            } else if (this.esq != null && this.dir == null) {   // Caso 2: só filho esq
                return this.esq;
            } else if (this.dir != null && this.esq == null) {   // Caso 3: só filho dir
                return this.dir;
            } else {                                               // Caso 4: dois filhos
                ArvoreAVL aux = this.esq;
                while (aux.dir != null) aux = aux.dir;
                int valorPredecessor = aux.getRegra().getValor();
                this.regra = aux.getRegra();
                this.esq = this.esq.remover(valorPredecessor);
            }
        } else if (valor < this.regra.getValor()) {
            if (this.esq == null) return this;
            this.esq = this.esq.remover(valor);
        } else {
            if (this.dir == null) return this;
            this.dir = this.dir.remover(valor);
        }
        this.calcularBalanceamento();
        return this.verificarBalanceamento();
    }
    // Buscas
    public boolean busca(int valor) {
        if (isEmpty()) return false;
        if (this.regra.getValor() == valor) return true;
        if (valor < this.regra.getValor()) {
            return this.esq != null && this.esq.busca(valor);
        } else {
            return this.dir != null && this.dir.busca(valor);
        }
    }

    public PacketRule buscarRegra(int valor) {
        if (isEmpty()) return null;
        if (this.regra.getValor() == valor) return this.regra;
        if (valor < this.regra.getValor()) {
            return (this.esq != null) ? this.esq.buscarRegra(valor) : null;
        } else {
            return (this.dir != null) ? this.dir.buscarRegra(valor) : null;
        }
    }

    // Ordem do percurso
    public void imprimirPreOrdem() {
        if (!isEmpty()) {
            System.out.print(this.regra.getValor() + " ");
            if (this.esq != null) this.esq.imprimirPreOrdem();
            if (this.dir != null) this.dir.imprimirPreOrdem();
        }
    }

    public void imprimirEmOrdem() {
        if (!isEmpty()) {
            if (this.esq != null) this.esq.imprimirEmOrdem();
            System.out.print(this.regra.getValor() + " ");
            if (this.dir != null) this.dir.imprimirEmOrdem();
        }
    }

    public void imprimirPosOrdem() {
        if (!isEmpty()) {
            if (this.esq != null) this.esq.imprimirPosOrdem();
            if (this.dir != null) this.dir.imprimirPosOrdem();
            System.out.print(this.regra.getValor() + " ");
        }
    }

    // Auto-balanceamento
    public int calcularAltura() {
        if (this.esq == null && this.dir == null) return 1;
        if (this.esq != null && this.dir == null) return 1 + this.esq.calcularAltura();
        if (this.esq == null) return 1 + this.dir.calcularAltura();
        return 1 + Math.max(this.esq.calcularAltura(), this.dir.calcularAltura());
    }

    public void calcularBalanceamento() {
        if (this.dir == null && this.esq == null) {
            this.bal = 0;
        } else if (this.esq == null) {
            this.bal = this.dir.calcularAltura();
        } else if (this.dir == null) {
            this.bal = -this.esq.calcularAltura();
        } else {
            this.bal = this.dir.calcularAltura() - this.esq.calcularAltura();
        }
    }

    public ArvoreAVL verificarBalanceamento() {
        if (this.bal >= 2) {
            int balDir = (this.dir != null) ? this.dir.getBalanceamento() : 0;
            if (balDir >= 0) {
                return rotacaoEsquerda();             // RR
            } else {
                return rotacaoDuplaEsquerdaDireita(); // RL
            }
        }
        if (this.bal <= -2) {
            int balEsq = (this.esq != null) ? this.esq.getBalanceamento() : 0;
            if (balEsq <= 0) {
                return rotacaoDireita();              // LL
            } else {
                return rotacaoDuplaDireitaEsquerda(); // LR
            }
        }
        return this;
    }

    // Rotações
    public ArvoreAVL rotacaoEsquerda() {
        ArvoreAVL filhoDir = this.getDireita();
        if (filhoDir == null) throw new IllegalStateException("rotacaoEsquerda: filho direito nulo");
        ArvoreAVL filhoDoFilho = filhoDir.getEsquerda();
 
        filhoDir.setEsq(this);
        this.setDir(filhoDoFilho);
        totalRotacoes++;
        return filhoDir;
    }

    public ArvoreAVL rotacaoDireita() {
        ArvoreAVL filhoEsq = this.getEsquerda();
        if (filhoEsq == null) throw new IllegalStateException("rotacaoDireita: filho esquerdo nulo");
        ArvoreAVL filhoDoFilho = filhoEsq.getDireita();

        filhoEsq.setDir(this);
        this.setEsq(filhoDoFilho);
        totalRotacoes++;
        return filhoEsq;
    }

    public ArvoreAVL rotacaoDuplaDireitaEsquerda() {
        ArvoreAVL filhoEsq     = this.getEsquerda();
        ArvoreAVL filhoDoFilho = filhoEsq.getDireita();
        ArvoreAVL noInserido   = filhoDoFilho.getEsquerda();

        filhoEsq.setDir(noInserido);
        filhoDoFilho.setEsq(filhoEsq);
        this.setEsq(filhoDoFilho);

        ArvoreAVL novoFilhoEsquerda = this.getEsquerda();
        this.setEsq(null);
        novoFilhoEsquerda.setDir(this);
        totalRotacoes += 2;
        return novoFilhoEsquerda;
    }

    public ArvoreAVL rotacaoDuplaEsquerdaDireita() {
        ArvoreAVL filhoDir     = this.getDireita();
        ArvoreAVL filhoDoFilho = filhoDir.getEsquerda();
        ArvoreAVL noInserido   = filhoDoFilho.getDireita();

        filhoDir.setEsq(noInserido);
        filhoDoFilho.setDir(filhoDir);
        this.setDir(filhoDoFilho);

        ArvoreAVL novoFilhoDireita = this.getDireita();
        this.setDir(null);
        novoFilhoDireita.setEsq(this);
        totalRotacoes += 2;
        return novoFilhoDireita;
    }

    // Print
    public String printArvore(int nivel) {
        String str = this.toString() + "\n";
        String indent = "\t".repeat(nivel);
        str += indent + "+-ESQ: ";
        str += (this.esq != null) ? this.esq.printArvore(nivel + 1) : "NULL\n";
        str += indent + "+-DIR: ";
        str += (this.dir != null) ? this.dir.printArvore(nivel + 1) : "NULL\n";
        return str;
    }

    @Override
    public String toString() {
        return "[" + this.regra.getValor() + "] (FB=" + this.bal + ")";
    }
}
