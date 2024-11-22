import java.time.LocalDateTime;

public class Historico {
    private final int idTransacao;
    private final String tipoTransacao;
    private final double quantia;
    private final int idOrigem;
    private final int idDestino;
    private final LocalDateTime dataHora;

    public Historico(int idTransacao, String tipoTransacao, double quantia, int idOrigem, int idDestino, LocalDateTime dataHora) {
        this.idTransacao = idTransacao;
        this.tipoTransacao = tipoTransacao;
        this.quantia = quantia;
        this.idOrigem = idOrigem;
        this.idDestino = idDestino;
        this.dataHora = dataHora;
    }

    public int getIdTransacao() {
        return idTransacao;
    }
    public String getTipoTransacao() {
        return tipoTransacao;
    }
    public double getQuantia() {
        return quantia;
    }
    public int getIdOrigem() {
        return idOrigem;
    }
    public int getIdDestino() {
        return idDestino;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }
}