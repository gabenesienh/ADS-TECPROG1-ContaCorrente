import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContaCorrente {
    double saldo = 0;
    double limite = 0;
    ArrayList<Double> transacoes = new ArrayList<Double>();
    HashMap<String, String> chavesPix = new HashMap<String, String>();
    ArrayList<String> chavesPixFavoritas = new ArrayList<String>();

    ContaCorrente(double saldo, double limite) {
        this.saldo = saldo;
        this.limite = limite;
        if (saldo != 0) this.transacoes.add(saldo);
    }

    public double getSaldo() {
        return this.saldo;
    }
    
    public double getLimite() {
        return this.limite;
    }
    
    public void setLimite(double limite) {
        if (limite < 0) throw new IllegalArgumentException("O valor do limite deve ser positivo.");
        
        this.limite = limite;
    }

    public void depositar(double quantia) {
        if (quantia < 0) throw new IllegalArgumentException("O valor do depósito deve ser positivo.");

        this.saldo += quantia;
        this.transacoes.add(quantia);
    }

    public void sacar(double quantia) {
        if (quantia < 0) throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        if (quantia > this.saldo + this.limite) throw new IllegalArgumentException("Saldo insuficiente para saque.");

        this.saldo -= quantia;
        this.transacoes.add(-quantia);
    }

    public boolean transferir(double quantia, String chavePix) {
        if (quantia < 0) throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        if (quantia > this.saldo + this.limite) throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        if (chavePix.length() != 11 || !chavePix.matches("[0-9]{11}")) throw new IllegalArgumentException("Chave Pix inválida.");

        this.transacoes.add(-quantia);
        if (!this.chavesPixFavoritas.contains(chavePix)) {
            this.chavesPixFavoritas.add(chavePix);
        }
        this.saldo -= quantia;
        return true;
    }

    public void adicionarChavePix(String chavePix, String descricao) {
        if (chavePix.length() != 11 || !chavePix.matches("[0-9]{11}")) throw new IllegalArgumentException("Chave Pix inválida.");

        this.chavesPix.put(chavePix, descricao);
    }

    public void adicionarChavePix(String chavePix) {
        adicionarChavePix(chavePix, "");
    }

    public boolean removerChavePix(String chavePix) {
        if (!this.chavesPix.containsKey(chavePix)) return false;
        
        this.chavesPix.remove(chavePix);
        return true;
    }

    public void exibirTransacoes(int maximoTransacoes) {
        for (int i = 0; i < this.transacoes.size(); i++) {
            if (i == maximoTransacoes) break;

            System.out.printf("%d. R$%.2f%n", i + 1, this.transacoes.get(i));
        }
    }

    public void exibirTransacoes() {
        exibirTransacoes(-1);
    }

    public void exibirChavesPix() {
        for (Map.Entry<String, String> chave : this.chavesPix.entrySet()) {
            System.out.print(chave.getKey());
            if (chave.getValue() != "") {
                System.out.print(": \"" + chave.getValue() + "\"");
            }
            System.out.println();
        }
    }

    public void exibirChavesPixFavoritas() {
        for (String chave : this.chavesPixFavoritas) {
            System.out.println(chave);
        }
    }
}