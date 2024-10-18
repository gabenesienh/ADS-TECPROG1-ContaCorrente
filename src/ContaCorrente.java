//TODO: implementar funcionalidades pix com o banco de dados

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContaCorrente {
    int numero = 0;
    double saldo = 0;
    double limite = 0;
    //HashMap<String, String> chavesPix = new HashMap<String, String>();
    //ArrayList<String> chavesPixFavoritas = new ArrayList<String>();

    ContaCorrente(int numero, double saldo, double limite) {
        this.numero = numero;
        this.saldo = saldo;
        this.limite = limite;
    }

    public int getNumero() {
        return this.numero;
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
    }

    public void sacar(double quantia, boolean consulta) {
        if (quantia < 0) throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        if (quantia > this.saldo + this.limite) throw new IllegalArgumentException("Saldo insuficiente para saque.");

        // se consulta for verdadeiro, apenas verificar se o saque seria possível
        if (!consulta) {
            this.saldo -= quantia;
        }
    }

    /*
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
    */
}