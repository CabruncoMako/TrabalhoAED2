public class PacketRule {
    private final int id;
    private String ipOrigem;
    private String ipDestino;
    private final int prioridade;

    public PacketRule(int id, String ipOrigem, String ipDestino, int prioridade) {
        this.id = id;
        this.ipOrigem = ipOrigem;
        this.ipDestino = ipDestino;
        this.prioridade = prioridade;
    }

    public int getId()               { return id; }
    public String getIpOrigem()      { return ipOrigem; }
    public String getIpDestino()     { return ipDestino; }
    public int getPrioridade()       { return prioridade; }

    public void setIpOrigem(String ipOrigem)     { this.ipOrigem = ipOrigem; }
    public void setIpDestino(String ipDestino)   { this.ipDestino = ipDestino; }

    public int getValor() {
        return this.prioridade;
    }

    @Override
    public String toString() {
        return "PacketRule{id=" + id
                + ", origem=" + ipOrigem
                + ", destino=" + ipDestino
                + ", prioridade=" + prioridade + "}";
    }
}
