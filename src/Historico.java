import java.time.LocalDateTime;

public class Historico {
    private int idTransacao;
    private String tipoTransacao;
    private double quantia;
    private int idOrigem;
    private int idDestinatario;
    private LocalDateTime dataHora;

    public Historico(int idTransacao, String tipoTransacao, double quantia, int idOrigem, int idDestinatario, LocalDateTime dataHora) {
        this.idTransacao = idTransacao;
        this.tipoTransacao = tipoTransacao;
        this.quantia = quantia;
        this.idOrigem = idOrigem;
        this.idDestinatario = idDestinatario;
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
    public int getIdDestinatario() {
        return idDestinatario;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setIdTransacao(int id_transacao) {
        this.idTransacao = id_transacao;
    }
    public void setTipoTransacao(String tipo_transacao) {
        this.tipoTransacao = tipo_transacao;
    }
    public void setQuantia(double quantia) {
        this.quantia = quantia;
    }
    public void setIdOrigem(int id_origem) {
        this.idOrigem = id_origem;
    }
    public void setIdDestinatario(int id_destinatario) {
        this.idDestinatario = id_destinatario;
    }
    public void setDataHora(LocalDateTime data_hora) {
        this.dataHora = data_hora;
    }
}