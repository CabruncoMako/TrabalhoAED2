public class ArvoreAVL {
    private Elemento ele;
    private ArvoreAVL dir;
    private ArvoreAVL esq;
    private int bal;

    public ArvoreAVL() {
        this.ele = null;
        this.esq = null;
        this.dir = null;
        this.bal = 0;
    }

    public ArvoreAVL(Elemento elem) {
        this.ele = elem;
        this.esq = null;
        this.dir = null;
        this.bal = 0;
    }

    public Elemento getElemento()           { return this.ele; }
    public void setElemento(Elemento elem)  { this.ele = elem; }

    public ArvoreAVL getDireita()           { return this.dir; }
    public void setDir(ArvoreAVL dir)       { this.dir = dir; }

    public ArvoreAVL getDireito()           { return this.dir; }

    public ArvoreAVL getEsquerda()          { return this.esq; }
    public void setEsq(ArvoreAVL esq)       { this.esq = esq; }

    public int getBalanceamento()           { return this.bal; }

    public boolean isEmpty() {
        return (this.ele == null);
    }

    public ArvoreAVL inserir(Elemento novo) {
        if (isEmpty()) {
            this.ele = novo;
        } else {
            ArvoreAVL novaArvore = new ArvoreAVL(novo);
            if (novo.getValor() < this.ele.getValor()) {
                if (this.esq == null) {
                    this.esq = novaArvore;
                } else {
                    this.esq = this.esq.inserir(novo);
                }
            } else if (novo.getValor() > this.ele.getValor()) {
                if (this.dir == null) {
                    this.dir = novaArvore;
                } else {
                    this.dir = this.dir.inserir(novo);
                }
            }
        }
        this.calcularBalanceamento();
        return this.verificarBalanceamento();
    }

    public ArvoreAVL remover(Elemento elem) {
        if (elem == null || this.isEmpty()) {
            return this;
        }

        if (elem.getValor() == this.ele.getValor()) {
            if (this.esq == null && this.dir == null) {
                return null;
            } else if (this.esq == null) {
                return this.dir;
            } else if (this.dir == null) {
                return this.esq;
            } else {
                ArvoreAVL predecessor = this.esq;
                while (predecessor.dir != null) {
                    predecessor = predecessor.dir;
                }
                this.ele = predecessor.getElemento();
                this.esq = this.esq.remover(predecessor.getElemento());
            }
        } else if (elem.getValor() < this.ele.getValor()) {
            if (this.esq != null) {
                this.esq = this.esq.remover(elem);
            }
        } else {
            if (this.dir != null) {
                this.dir = this.dir.remover(elem);
            }
        }

        this.calcularBalanceamento();
        return this.verificarBalanceamento();
    }

    public boolean busca(int valor) {
        if (isEmpty()) {
            return false;
        }
        if (this.ele.getValor() == valor) {
            return true;
        } else if (valor < this.ele.getValor()) {
            if (this.esq == null) return false;
            return this.esq.busca(valor);
        } else {
            if (this.dir == null) return false;
            return this.dir.busca(valor);
        }
    }

    public void imprimirPreOrdem() {
        if (!isEmpty()) {
            System.out.print(this.ele.getValor() + " ");
            if (this.esq != null) this.esq.imprimirPreOrdem();
            if (this.dir != null) this.dir.imprimirPreOrdem();
        }
    }

    public void imprimirEmOrdem() {
        if (!isEmpty()) {
            if (this.esq != null) this.esq.imprimirEmOrdem();
            System.out.print(this.ele.getValor() + " ");
            if (this.dir != null) this.dir.imprimirEmOrdem();
        }
    }

    public void imprimirPosOrdem() {
        if (!isEmpty()) {
            if (this.esq != null) this.esq.imprimirPosOrdem();
            if (this.dir != null) this.dir.imprimirPosOrdem();
            System.out.print(this.ele.getValor() + " ");
        }
    }

    public int calcularAltura() {
        if (this.isEmpty()) {
            return 0;
        }
        int alturaEsq = (this.esq == null ? 0 : this.esq.calcularAltura());
        int alturaDir = (this.dir == null ? 0 : this.dir.calcularAltura());
        return 1 + Math.max(alturaEsq, alturaDir);
    }

    private int alturaNo(ArvoreAVL no) {
        return (no == null || no.isEmpty()) ? 0 : no.calcularAltura();
    }

    public void calcularBalanceamento() {
        if (this.isEmpty()) {
            this.bal = 0;
        } else {
            this.bal = alturaNo(this.dir) - alturaNo(this.esq);
        }
        if (this.esq != null) this.esq.calcularBalanceamento();
        if (this.dir != null) this.dir.calcularBalanceamento();
    }

    public ArvoreAVL verificarBalanceamento() {
        if (this.isEmpty()) {
            return this;
        }

        if (this.bal > 1) {
            if (this.dir != null && this.dir.getBalanceamento() < 0) {
                this.dir = this.dir.rotacaoSimplesDireita();
            }
            return this.rotacaoSimplesEsquerda();
        } else if (this.bal < -1) {
            if (this.esq != null && this.esq.getBalanceamento() > 0) {
                this.esq = this.esq.rotacaoSimplesEsquerda();
            }
            return this.rotacaoSimplesDireita();
        }

        if (this.esq != null) this.esq = this.esq.verificarBalanceamento();
        if (this.dir != null) this.dir = this.dir.verificarBalanceamento();
        return this;
    }

    public ArvoreAVL rotacaoSimplesDireita() {
        ArvoreAVL novoTopo = this.esq;
        ArvoreAVL filho = novoTopo.dir;

        novoTopo.setDir(this);
        this.setEsq(filho);

        this.calcularBalanceamento();
        novoTopo.calcularBalanceamento();
        return novoTopo;
    }

    public ArvoreAVL rotacaoSimplesEsquerda() {
        ArvoreAVL novoTopo = this.dir;
        ArvoreAVL filho = novoTopo.esq;

        novoTopo.setEsq(this);
        this.setDir(filho);

        this.calcularBalanceamento();
        novoTopo.calcularBalanceamento();
        return novoTopo;
    }

    public ArvoreAVL rotacaoDuplaDireita() {
        if (this.dir != null) {
            this.dir = this.dir.rotacaoSimplesDireita();
        }
        return this.rotacaoSimplesEsquerda();
    }

    public ArvoreAVL rotacaoDuplaEsquerda() {
        if (this.esq != null) {
            this.esq = this.esq.rotacaoSimplesEsquerda();
        }
        return this.rotacaoSimplesDireita();
    }

    public String printArvore(int level) {
        String str = this.toString() + "\n";
        for (int i = 0; i < level; i++) str += "\t";
        if (this.esq != null) {
            str += "+-ESQ: " + this.esq.printArvore(level + 1);
        } else {
            str += "+-ESQ: NULL";
        }
        str += "\n";
        for (int i = 0; i < level; i++) str += "\t";
        if (this.dir != null) {
            str += "+-DIR: " + this.dir.printArvore(level + 1);
        } else {
            str += "+-DIR: NULL";
        }
        str += "\n";
        return str;
    }

    @Override
    public String toString() {
        return "[" + this.ele.getValor() + "] (" + this.bal + ")";
    }
}
